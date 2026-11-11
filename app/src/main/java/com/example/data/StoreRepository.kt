package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.model.BannerAd
import com.example.model.CartItem
import com.example.model.Category
import com.example.model.CorridorArea
import com.example.model.CustomCategory
import com.example.model.CustomItemRequest
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.ProductVariant
import com.example.model.Rider
import com.example.model.StoreConfig
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class StoreRepository(
  private val context: Context,
  private val firestore: FirebaseFirestore? = null,
  private val auth: FirebaseAuth? = null
) {
  companion object {
    private const val TAG = "StoreRepository"
    private const val ADMIN_EMAIL = "mrityunjayvishwas294@gmail.com"
    const val COL_STORE_CONFIG = "store_config"
    const val DOC_STORE_CONFIG_MAIN = "main"
    const val COL_PRODUCTS = "products"
    const val COL_BANNERS = "banners"
    const val COL_ORDERS = "orders"
    const val COL_CUSTOM_REQUESTS = "custom_requests"
    const val COL_RIDERS = "riders"
  }

  private val prefs: SharedPreferences =
    context.getSharedPreferences("apna_market_prefs_v5", Context.MODE_PRIVATE)

  fun getLanguage(): AppLanguage =
    AppLanguage.fromCode(prefs.getString("app_language", AppLanguage.HINDI.code))

  fun saveLanguage(lang: AppLanguage) {
    prefs.edit().putString("app_language", lang.code).apply()
  }

  private val _wishlistIds = MutableStateFlow<Set<String>>(
    prefs.getStringSet("wishlist_ids", emptySet())?.toSet() ?: emptySet()
  )
  val wishlistIds: StateFlow<Set<String>> = _wishlistIds.asStateFlow()

  fun toggleWishlist(productId: String) {
    val current = _wishlistIds.value.toMutableSet()
    if (!current.add(productId)) current.remove(productId)
    _wishlistIds.value = current
    prefs.edit().putStringSet("wishlist_ids", current).apply()
  }

  private val lastKnownOrderStatuses = mutableMapOf<String, OrderStatus>()
  private var ordersSnapshotInitialized = false

  private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val listeners = mutableListOf<ListenerRegistration>()

  // Products
  private val _products = MutableStateFlow<List<Product>>(DefaultProducts.getInitialCatalog())
  val products: StateFlow<List<Product>> = _products.asStateFlow()

  // Cart
  private val _cart = MutableStateFlow<Map<String, CartItem>>(emptyMap())
  val cart: StateFlow<Map<String, CartItem>> = _cart.asStateFlow()

  // Orders
  private val _orders = MutableStateFlow<List<Order>>(emptyList())
  val orders: StateFlow<List<Order>> = _orders.asStateFlow()

  // Custom Item Requests
  private val _customRequests = MutableStateFlow<List<CustomItemRequest>>(emptyList())
  val customRequests: StateFlow<List<CustomItemRequest>> = _customRequests.asStateFlow()

  // Selected Corridor Area
  private val _selectedArea = MutableStateFlow<CorridorArea>(
    DefaultProducts.defaultAreas.first()
  )
  val selectedArea: StateFlow<CorridorArea> = _selectedArea.asStateFlow()

  // Store Configuration & Owner controls
  private val _storeConfig = MutableStateFlow(StoreConfig())
  val storeConfig: StateFlow<StoreConfig> = _storeConfig.asStateFlow()

  // Banners & Sale Ads
  private val _banners = MutableStateFlow<List<BannerAd>>(seedDefaultBanners())
  val banners: StateFlow<List<BannerAd>> = _banners.asStateFlow()

  // Hired Delivery Riders (Admin only)
  private val _riders = MutableStateFlow<List<Rider>>(emptyList())
  val riders: StateFlow<List<Rider>> = _riders.asStateFlow()

  init {
    ioScope.launch {
      _products.value = loadProducts()
      _orders.value = loadOrders()
      _customRequests.value = loadCustomRequests()
      _storeConfig.value = loadStoreConfig()
      _banners.value = loadBanners()
      _riders.value = loadRiders()
    }
  }

  fun isUserAdmin(user: FirebaseUser? = auth?.currentUser): Boolean {
    if (user == null) return false
    val ownerEmail = _storeConfig.value.storeOwnerEmail.ifBlank { ADMIN_EMAIL }
    return (user.email.equals(ownerEmail, ignoreCase = true) ||
      user.email.equals(ADMIN_EMAIL, ignoreCase = true)) && user.isEmailVerified
  }

  fun startSync(user: FirebaseUser? = auth?.currentUser) {
    stopSync()
    val db = firestore ?: return
    if (user == null) return
    val admin = isUserAdmin(user)

    // 1. Store Config
    listeners += db.collection(COL_STORE_CONFIG).document(DOC_STORE_CONFIG_MAIN)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "store_config listener error", error)
          return@addSnapshotListener
        }
        if (snapshot != null && snapshot.exists()) {
          docToStoreConfig(snapshot)?.let { cfg ->
            _storeConfig.value = cfg
            saveStoreConfig(cfg)
          }
        } else if (admin) {
          db.collection(COL_STORE_CONFIG).document(DOC_STORE_CONFIG_MAIN)
            .set(storeConfigToMap(_storeConfig.value))
            .addOnFailureListener { e -> Log.w(TAG, "Failed to seed store_config", e) }
        }
      }

    // 2. Products
    listeners += db.collection(COL_PRODUCTS)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "products listener error", error)
          return@addSnapshotListener
        }
        if (snapshot != null) {
          if (snapshot.isEmpty) {
            if (admin) {
              seedProductsToFirestore(db, _products.value)
            }
          } else {
            val remote = snapshot.documents.mapNotNull { docToProduct(it) }
            if (remote.isNotEmpty()) {
              _products.value = remote
              saveProducts(remote)
            }
          }
        }
      }

    // 3. Banners
    listeners += db.collection(COL_BANNERS)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "banners listener error", error)
          return@addSnapshotListener
        }
        if (snapshot != null) {
          if (snapshot.isEmpty) {
            if (admin) {
              seedBannersToFirestore(db, _banners.value)
            }
          } else {
            val remote = snapshot.documents.mapNotNull { docToBanner(it) }
            if (remote.isNotEmpty()) {
              _banners.value = remote
              saveBanners(remote)
            }
          }
        }
      }

    // 4. Orders (strictly isolated: customer sees only own orders; admin sees all)
    val ordersQuery = if (admin) {
      db.collection(COL_ORDERS)
    } else {
      db.collection(COL_ORDERS).whereEqualTo("userId", user.uid)
    }
    listeners += ordersQuery.addSnapshotListener { snapshot, error ->
      if (error != null) {
        Log.w(TAG, "orders listener error", error)
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val remote = snapshot.documents
          .mapNotNull { docToOrder(it, user.uid) }
          .sortedByDescending { it.createdAt }

        if (ordersSnapshotInitialized) {
          remote.forEach { order ->
            val prevStatus = lastKnownOrderStatuses[order.id]
            if (admin && prevStatus == null) {
              val payInfo = if (order.advancePaid > 0 || order.paymentMethod.contains("UPI", true)) {
                "पेमेंट: ${order.paymentMethod} ${if (order.upiTransactionRef.isNotBlank()) "(UTR: ${order.upiTransactionRef})" else ""}"
              } else {
                order.paymentMethod
              }
              NotificationHelper.showOrderNotification(
                context = context,
                title = "💰 नया ऑर्डर व पेमेंट सूचना! ₹${order.grandTotal.toInt()}",
                message = "${order.customerName} (${order.area}) • $payInfo",
                notificationId = order.id.hashCode()
              )
            } else if (!admin && prevStatus != null && prevStatus != order.status) {
              val msg = when (order.status) {
                OrderStatus.PACKED -> "🎉 आपका ऑर्डर #${order.id.takeLast(5)} पैक होकर तैयार है!"
                OrderStatus.OUT_FOR_8AM -> "🛵 राइडर आपका ऑर्डर #${order.id.takeLast(5)} लेकर निकल गया है!"
                OrderStatus.DELIVERED -> "✅ ऑर्डर #${order.id.takeLast(5)} डिलीवर हो गया! अपना मार्केट से जुड़ने के लिए धन्यवाद!"
                OrderStatus.MANDI_PROCUREMENT -> "🛒 आपके ऑर्डर #${order.id.takeLast(5)} का ताज़ा सामान खरीदा जा रहा है।"
                else -> null
              }
              if (msg != null) {
                NotificationHelper.showOrderNotification(
                  context = context,
                  title = "अपना मार्केट · ऑर्डर अपडेट",
                  message = msg,
                  notificationId = order.id.hashCode()
                )
              }
            }
          }
        }
        lastKnownOrderStatuses.clear()
        remote.forEach { lastKnownOrderStatuses[it.id] = it.status }
        ordersSnapshotInitialized = true

        _orders.value = remote
        saveOrders(remote)
      }
    }

    // 5. Custom Requests
    val reqQuery = if (admin) {
      db.collection(COL_CUSTOM_REQUESTS)
    } else {
      db.collection(COL_CUSTOM_REQUESTS).whereEqualTo("userId", user.uid)
    }
    listeners += reqQuery.addSnapshotListener { snapshot, error ->
      if (error != null) {
        Log.w(TAG, "custom_requests listener error", error)
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val remote = snapshot.documents
          .mapNotNull { docToCustomRequest(it) }
          .sortedByDescending { it.timestamp }
        _customRequests.value = remote
        saveCustomRequests(remote)
      }
    }

    // 6. Hired Riders (Admin only)
    if (admin) {
      listeners += db.collection(COL_RIDERS).addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "riders listener error", error)
          return@addSnapshotListener
        }
        if (snapshot != null) {
          val remote = snapshot.documents
            .mapNotNull { docToRider(it) }
            .sortedByDescending { it.createdAt }
          _riders.value = remote
          saveRiders(remote)
        }
      }
    }
  }

  fun stopSync() {
    listeners.forEach { it.remove() }
    listeners.clear()
  }

  private fun seedProductsToFirestore(db: FirebaseFirestore, list: List<Product>) {
    val batch = db.batch()
    for (p in list) {
      batch.set(db.collection(COL_PRODUCTS).document(p.id), productToMap(p))
    }
    batch.commit().addOnFailureListener { e -> Log.w(TAG, "Failed to seed products", e) }
  }

  private fun seedBannersToFirestore(db: FirebaseFirestore, list: List<BannerAd>) {
    val batch = db.batch()
    for (b in list) {
      batch.set(db.collection(COL_BANNERS).document(b.id), bannerToMap(b))
    }
    batch.commit().addOnFailureListener { e -> Log.w(TAG, "Failed to seed banners", e) }
  }

  fun selectArea(area: CorridorArea) {
    _selectedArea.value = area
  }

  fun cartKey(product: Product): String = "${product.id}__${product.unit}"

  // Cart actions
  fun addToCart(product: Product, variant: ProductVariant? = null) {
    val resolvedProduct = if (variant != null) {
      product.copy(
        unit = variant.label,
        mrp = variant.mrp,
        ourPrice = variant.ourPrice
      )
    } else product
    val key = cartKey(resolvedProduct)
    val current = _cart.value.toMutableMap()
    val existing = current[key]
    if (existing != null) {
      current[key] = existing.copy(quantity = existing.quantity + 1)
    } else {
      current[key] = CartItem(product = resolvedProduct, quantity = 1)
    }
    _cart.value = current
  }

  fun removeFromCart(cartKeyOrProductId: String) {
    val current = _cart.value.toMutableMap()
    val resolvedKey = if (current.containsKey(cartKeyOrProductId)) {
      cartKeyOrProductId
    } else {
      current.keys.firstOrNull { it.startsWith("${cartKeyOrProductId}__") } ?: return
    }
    val existing = current[resolvedKey] ?: return
    if (existing.quantity > 1) {
      current[resolvedKey] = existing.copy(quantity = existing.quantity - 1)
    } else {
      current.remove(resolvedKey)
    }
    _cart.value = current
  }

  fun removeCompletelyFromCart(cartKeyOrProductId: String) {
    val current = _cart.value.toMutableMap()
    if (current.containsKey(cartKeyOrProductId)) {
      current.remove(cartKeyOrProductId)
    } else {
      current.keys.filter { it.startsWith("${cartKeyOrProductId}__") }.forEach { current.remove(it) }
    }
    _cart.value = current
  }

  fun clearCart() {
    _cart.value = emptyMap()
  }

  // Admin / Store Owner controls
  fun updateProduct(updated: Product) {
    val list = _products.value.map { if (it.id == updated.id) updated else it }
    _products.value = list
    saveProducts(list)

    firestore?.collection(COL_PRODUCTS)?.document(updated.id)
      ?.set(productToMap(updated))
      ?.addOnFailureListener { e -> Log.w(TAG, "updateProduct remote failed", e) }

    val currentCart = _cart.value.toMutableMap()
    currentCart.entries.filter { it.value.product.id == updated.id }.forEach { (key, cartItem) ->
      val matchedVariant = updated.variants.find { it.label == cartItem.product.unit }
      val updatedCartProduct = if (matchedVariant != null) {
        updated.copy(
          unit = matchedVariant.label,
          mrp = matchedVariant.mrp,
          ourPrice = matchedVariant.ourPrice
        )
      } else {
        updated
      }
      currentCart[key] = cartItem.copy(product = updatedCartProduct)
    }
    _cart.value = currentCart
  }

  fun addProduct(newProduct: Product) {
    val list = listOf(newProduct) + _products.value
    _products.value = list
    saveProducts(list)

    firestore?.collection(COL_PRODUCTS)?.document(newProduct.id)
      ?.set(productToMap(newProduct))
      ?.addOnFailureListener { e -> Log.w(TAG, "addProduct remote failed", e) }
  }

  fun deleteProduct(productId: String) {
    val list = _products.value.filter { it.id != productId }
    _products.value = list
    saveProducts(list)
    removeCompletelyFromCart(productId)

    firestore?.collection(COL_PRODUCTS)?.document(productId)
      ?.delete()
      ?.addOnFailureListener { e -> Log.w(TAG, "deleteProduct remote failed", e) }
  }

  fun toggleProductStock(productId: String) {
    var toggled: Product? = null
    val list = _products.value.map {
      if (it.id == productId) {
        val copy = it.copy(inStock = !it.inStock)
        toggled = copy
        copy
      } else it
    }
    _products.value = list
    saveProducts(list)

    toggled?.let { prod ->
      firestore?.collection(COL_PRODUCTS)?.document(prod.id)
        ?.set(productToMap(prod))
        ?.addOnFailureListener { e -> Log.w(TAG, "toggleProductStock remote failed", e) }
    }
  }

  fun updateStoreConfig(config: StoreConfig) {
    _storeConfig.value = config
    saveStoreConfig(config)

    firestore?.collection(COL_STORE_CONFIG)?.document(DOC_STORE_CONFIG_MAIN)
      ?.set(storeConfigToMap(config))
      ?.addOnFailureListener { e -> Log.w(TAG, "updateStoreConfig remote failed", e) }
  }

  // Banner / Sale Ad management
  fun addBanner(banner: BannerAd) {
    val list = listOf(banner) + _banners.value
    _banners.value = list
    saveBanners(list)

    firestore?.collection(COL_BANNERS)?.document(banner.id)
      ?.set(bannerToMap(banner))
      ?.addOnFailureListener { e -> Log.w(TAG, "addBanner remote failed", e) }
  }

  fun updateBanner(updated: BannerAd) {
    val list = _banners.value.map { if (it.id == updated.id) updated else it }
    _banners.value = list
    saveBanners(list)

    firestore?.collection(COL_BANNERS)?.document(updated.id)
      ?.set(bannerToMap(updated))
      ?.addOnFailureListener { e -> Log.w(TAG, "updateBanner remote failed", e) }
  }

  fun toggleBanner(bannerId: String) {
    var toggled: BannerAd? = null
    val list = _banners.value.map {
      if (it.id == bannerId) {
        val copy = it.copy(isActive = !it.isActive)
        toggled = copy
        copy
      } else it
    }
    _banners.value = list
    saveBanners(list)

    toggled?.let { b ->
      firestore?.collection(COL_BANNERS)?.document(b.id)
        ?.set(bannerToMap(b))
        ?.addOnFailureListener { e -> Log.w(TAG, "toggleBanner remote failed", e) }
    }
  }

  fun deleteBanner(bannerId: String) {
    val list = _banners.value.filter { it.id != bannerId }
    _banners.value = list
    saveBanners(list)

    firestore?.collection(COL_BANNERS)?.document(bannerId)
      ?.delete()
      ?.addOnFailureListener { e -> Log.w(TAG, "deleteBanner remote failed", e) }
  }

  // Orders
  fun placeOrder(
    customerName: String,
    customerPhone: String,
    customerEmail: String,
    areaName: String,
    addressDetail: String,
    advancePaid: Double,
    paymentMethod: String,
    upiRef: String,
    specialNote: String
  ): Order {
    val currentCartItems = _cart.value.values.toList()
    val subtotal = currentCartItems.sumOf { it.product.ourPrice * it.quantity }
    val savings = currentCartItems.sumOf { it.product.savings * it.quantity }
    val config = _storeConfig.value

    val needsAdvance = subtotal <= config.advanceThreshold
    val deliveryFee = if (needsAdvance) config.deliveryFee else 0.0
    val hasHeavy = currentCartItems.any { it.product.isHeavy }
    val heavyFee = if (hasHeavy) config.heavyDeliveryFee else 0.0
    val grandTotal = subtotal + deliveryFee + heavyFee
    val balanceDue = if (advancePaid >= grandTotal) 0.0 else grandTotal - advancePaid

    val uid = auth?.currentUser?.uid ?: ""
    val resolvedEmail = customerEmail.ifBlank { auth?.currentUser?.email ?: "" }

    val order = Order(
      id = "AM-${(1000..9999).random()}",
      userId = uid,
      customerName = customerName,
      customerPhone = customerPhone,
      customerEmail = resolvedEmail,
      area = areaName,
      addressDetail = addressDetail,
      items = currentCartItems,
      subtotal = subtotal,
      totalSavings = savings,
      deliveryCharge = deliveryFee,
      heavyDeliveryCharge = heavyFee,
      grandTotal = grandTotal,
      advancePaid = advancePaid,
      balanceDueAtDelivery = balanceDue,
      paymentMethod = paymentMethod,
      upiTransactionRef = upiRef,
      deliveryDate = "${config.deliveryTiming} (${config.deliveryScheduleDays})",
      createdAt = System.currentTimeMillis(),
      status = OrderStatus.RECEIVED,
      mrityunjayTrustGuarantee = true,
      specialNote = specialNote,
      isMyOrder = true,
      customerLat = _selectedArea.value.latitude,
      customerLng = _selectedArea.value.longitude
    )

    val updatedOrders = listOf(order) + _orders.value
    _orders.value = updatedOrders
    saveOrders(updatedOrders)
    clearCart()

    if (uid.isNotBlank()) {
      firestore?.collection(COL_ORDERS)?.document(order.id)
        ?.set(orderToMap(order, useServerTimestamp = true))
        ?.addOnFailureListener { e -> Log.w(TAG, "placeOrder remote failed", e) }
    }
    return order
  }

  fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
    val list = _orders.value.map {
      if (it.id == orderId) it.copy(status = newStatus) else it
    }
    _orders.value = list
    saveOrders(list)

    firestore?.collection(COL_ORDERS)?.document(orderId)
      ?.update(
        mapOf(
          "status" to newStatus.name,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      )
      ?.addOnFailureListener { e -> Log.w(TAG, "updateOrderStatus remote failed", e) }
  }

  fun rateAndTipOrder(orderId: String, rating: Int, review: String, tipAmount: Double) {
    val clampedRating = rating.coerceIn(1, 5)
    val cleanReview = review.trim()
    val cleanTip = tipAmount.coerceAtLeast(0.0)
    val list = _orders.value.map {
      if (it.id == orderId) {
        it.copy(
          rating = clampedRating,
          ratingReview = cleanReview,
          deliveryTip = cleanTip
        )
      } else it
    }
    _orders.value = list
    saveOrders(list)

    firestore?.collection(COL_ORDERS)?.document(orderId)
      ?.update(
        mapOf(
          "rating" to clampedRating,
          "ratingReview" to cleanReview,
          "deliveryTip" to cleanTip,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      )
      ?.addOnFailureListener { e -> Log.w(TAG, "rateAndTipOrder remote failed", e) }
  }

  // Custom requests
  fun submitCustomRequest(
    customerName: String,
    customerPhone: String,
    customerEmail: String,
    itemName: String,
    quantity: String,
    notes: String,
    area: String
  ): CustomItemRequest {
    val uid = auth?.currentUser?.uid ?: ""
    val resolvedEmail = customerEmail.ifBlank { auth?.currentUser?.email ?: "" }
    val request = CustomItemRequest(
      id = "REQ-${(100..999).random()}",
      userId = uid,
      customerName = customerName,
      customerPhone = customerPhone,
      customerEmail = resolvedEmail,
      itemName = itemName,
      quantity = quantity,
      notes = notes,
      targetArea = area,
      timestamp = System.currentTimeMillis()
    )
    val list = listOf(request) + _customRequests.value
    _customRequests.value = list
    saveCustomRequests(list)

    if (uid.isNotBlank()) {
      firestore?.collection(COL_CUSTOM_REQUESTS)?.document(request.id)
        ?.set(customRequestToMap(request, useServerTimestamp = true))
        ?.addOnFailureListener { e -> Log.w(TAG, "submitCustomRequest remote failed", e) }
    }
    return request
  }

  fun updateCustomRequestStatus(requestId: String, status: String) {
    val list = _customRequests.value.map {
      if (it.id == requestId) it.copy(status = status) else it
    }
    _customRequests.value = list
    saveCustomRequests(list)

    firestore?.collection(COL_CUSTOM_REQUESTS)?.document(requestId)
      ?.update(
        mapOf(
          "status" to status,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      )
      ?.addOnFailureListener { e -> Log.w(TAG, "updateCustomRequestStatus remote failed", e) }
  }

  // Hired Delivery Riders management
  fun addRider(
    name: String,
    phone: String,
    email: String = "",
    area: String = "रायगढ़ ➔ पुंजीपथरा",
    vehicleType: String = "Bike"
  ): Rider {
    val rider = Rider(
      id = "RDR-${(1000..9999).random()}",
      name = name.trim(),
      phone = phone.trim(),
      email = email.trim(),
      area = area.trim().ifBlank { "रायगढ़ ➔ पुंजीपथरा" },
      vehicleType = vehicleType.trim().ifBlank { "Bike" },
      isActive = true,
      createdAt = System.currentTimeMillis()
    )
    val list = listOf(rider) + _riders.value
    _riders.value = list
    saveRiders(list)

    firestore?.collection(COL_RIDERS)?.document(rider.id)
      ?.set(riderToMap(rider))
      ?.addOnFailureListener { e -> Log.w(TAG, "addRider remote failed", e) }
    return rider
  }

  fun toggleRiderActive(riderId: String) {
    var toggled: Rider? = null
    val list = _riders.value.map {
      if (it.id == riderId) {
        val copy = it.copy(isActive = !it.isActive)
        toggled = copy
        copy
      } else it
    }
    _riders.value = list
    saveRiders(list)

    toggled?.let { r ->
      firestore?.collection(COL_RIDERS)?.document(r.id)
        ?.set(riderToMap(r))
        ?.addOnFailureListener { e -> Log.w(TAG, "toggleRiderActive remote failed", e) }
    }
  }

  fun deleteRider(riderId: String) {
    val list = _riders.value.filter { it.id != riderId }
    _riders.value = list
    saveRiders(list)

    firestore?.collection(COL_RIDERS)?.document(riderId)
      ?.delete()
      ?.addOnFailureListener { e -> Log.w(TAG, "deleteRider remote failed", e) }
  }

  fun assignRiderToOrder(orderId: String, rider: Rider) {
    val list = _orders.value.map {
      if (it.id == orderId) {
        it.copy(
          assignedRiderId = rider.id,
          assignedRiderName = rider.name,
          assignedRiderPhone = rider.phone
        )
      } else it
    }
    _orders.value = list
    saveOrders(list)

    firestore?.collection(COL_ORDERS)?.document(orderId)
      ?.update(
        mapOf(
          "assignedRiderId" to rider.id,
          "assignedRiderName" to rider.name,
          "assignedRiderPhone" to rider.phone,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      )
      ?.addOnFailureListener { e -> Log.w(TAG, "assignRiderToOrder remote failed", e) }
  }

  fun updateOrderCollection(orderId: String, mode: String, settled: Boolean) {
    val list = _orders.value.map {
      if (it.id == orderId) {
        it.copy(
          collectedPaymentMode = mode,
          isRiderCashSettled = settled
        )
      } else it
    }
    _orders.value = list
    saveOrders(list)

    firestore?.collection(COL_ORDERS)?.document(orderId)
      ?.update(
        mapOf(
          "collectedPaymentMode" to mode,
          "isRiderCashSettled" to settled,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      )
      ?.addOnFailureListener { e -> Log.w(TAG, "updateOrderCollection remote failed", e) }
  }

  // =========================================================================
  // Direct Suspend / Flow Methods for Testing & Verified Firestore Operations
  // =========================================================================

  suspend fun upsertProductRemote(product: Product) {
    val db = firestore ?: error("Firestore not initialized")
    val a = auth ?: error("Auth not initialized")
    val path = "$COL_PRODUCTS/${product.id}"
    try {
      db.collection(COL_PRODUCTS).document(product.id).set(productToMap(product)).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path, a)
    }
  }

  suspend fun createOrderRemote(order: Order) {
    val db = firestore ?: error("Firestore not initialized")
    val a = auth ?: error("Auth not initialized")
    val path = "$COL_ORDERS/${order.id}"
    try {
      db.collection(COL_ORDERS).document(order.id)
        .set(orderToMap(order, useServerTimestamp = true))
        .await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path, a)
    }
  }

  suspend fun getOrderRemote(orderId: String): Order? {
    val db = firestore ?: error("Firestore not initialized")
    val a = auth ?: error("Auth not initialized")
    val path = "$COL_ORDERS/$orderId"
    return try {
      val doc = db.collection(COL_ORDERS).document(orderId).get().await()
      if (doc.exists()) docToOrder(doc, a.currentUser?.uid ?: "") else null
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, path, a)
    }
  }

  suspend fun rateOrderRemote(orderId: String, rating: Int, review: String, tip: Double) {
    val db = firestore ?: error("Firestore not initialized")
    val a = auth ?: error("Auth not initialized")
    val path = "$COL_ORDERS/$orderId"
    try {
      db.collection(COL_ORDERS).document(orderId).update(
        mapOf(
          "rating" to rating.coerceIn(1, 5),
          "ratingReview" to review.trim(),
          "deliveryTip" to tip.coerceAtLeast(0.0),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path, a)
    }
  }

  fun observeProductsRemote(): Flow<List<Product>> = callbackFlow {
    val db = firestore ?: error("Firestore not initialized")
    val registration = db.collection(COL_PRODUCTS).addSnapshotListener { snap, err ->
      if (err != null) {
        close(err)
        return@addSnapshotListener
      }
      val list = snap?.documents?.mapNotNull { docToProduct(it) }.orEmpty()
      trySend(list)
    }
    awaitClose { registration.remove() }
  }

  // =========================================================================
  // Firestore Map <-> Model Mappers
  // =========================================================================

  private fun productToMap(p: Product): Map<String, Any?> = mapOf(
    "id" to p.id,
    "name" to p.name,
    "hindiName" to p.hindiName,
    "category" to p.category.name,
    "unit" to p.unit,
    "mrp" to p.mrp,
    "ourPrice" to p.ourPrice,
    "inStock" to p.inStock,
    "tag" to p.tag,
    "emoji" to p.emoji,
    "imageUrl" to p.imageUrl,
    "description" to p.description,
    "isHeavy" to p.isHeavy,
    "variants" to p.variants.map { v ->
      mapOf("label" to v.label, "mrp" to v.mrp, "ourPrice" to v.ourPrice)
    },
    "customCategoryId" to p.customCategoryId
  )

  private fun docToProduct(doc: DocumentSnapshot): Product? {
    val id = doc.getString("id") ?: doc.id
    val name = doc.getString("name") ?: return null
    val catName = doc.getString("category") ?: Category.VEGETABLES.name
    val cat = try { Category.valueOf(catName) } catch (_: Exception) { Category.VEGETABLES }
    val rawVariants = doc.get("variants") as? List<*>
    val variants = rawVariants?.mapNotNull { item ->
      val m = item as? Map<*, *> ?: return@mapNotNull null
      val label = m["label"] as? String ?: return@mapNotNull null
      val mrp = (m["mrp"] as? Number)?.toDouble() ?: 0.0
      val ourPrice = (m["ourPrice"] as? Number)?.toDouble() ?: 0.0
      ProductVariant(label = label, mrp = mrp, ourPrice = ourPrice)
    }.orEmpty()

    return Product(
      id = id,
      name = name,
      hindiName = doc.getString("hindiName") ?: name,
      category = cat,
      unit = doc.getString("unit") ?: "1 kg",
      mrp = doc.getDouble("mrp") ?: 0.0,
      ourPrice = doc.getDouble("ourPrice") ?: 0.0,
      inStock = doc.getBoolean("inStock") ?: true,
      tag = doc.getString("tag")?.takeIf { it.isNotBlank() },
      emoji = doc.getString("emoji") ?: "🛒",
      imageUrl = doc.getString("imageUrl")?.takeIf { it.isNotBlank() },
      description = doc.getString("description") ?: "",
      isHeavy = doc.getBoolean("isHeavy") ?: false,
      variants = variants,
      customCategoryId = doc.getString("customCategoryId") ?: ""
    )
  }

  private fun bannerToMap(b: BannerAd): Map<String, Any?> = mapOf(
    "id" to b.id,
    "title" to b.title,
    "subtitle" to b.subtitle,
    "badge" to b.badge,
    "discountTag" to b.discountTag,
    "isFestivalSpecial" to b.isFestivalSpecial,
    "isActive" to b.isActive,
    "mediaUrl" to b.mediaUrl
  )

  private fun docToBanner(doc: DocumentSnapshot): BannerAd? {
    val id = doc.getString("id") ?: doc.id
    val title = doc.getString("title") ?: return null
    return BannerAd(
      id = id,
      title = title,
      subtitle = doc.getString("subtitle") ?: "",
      badge = doc.getString("badge") ?: "OFFER",
      discountTag = doc.getString("discountTag") ?: "FLAT 20% OFF",
      isFestivalSpecial = doc.getBoolean("isFestivalSpecial") ?: false,
      isActive = doc.getBoolean("isActive") ?: true,
      mediaUrl = doc.getString("mediaUrl")?.takeIf { it.isNotBlank() }
    )
  }

  private fun orderToMap(o: Order, useServerTimestamp: Boolean): Map<String, Any?> = mapOf(
    "id" to o.id,
    "userId" to o.userId,
    "customerName" to o.customerName,
    "customerPhone" to o.customerPhone,
    "customerEmail" to o.customerEmail,
    "area" to o.area,
    "addressDetail" to o.addressDetail,
    "items" to o.items.map { item ->
      mapOf(
        "productId" to item.product.id,
        "name" to item.product.name,
        "unit" to item.product.unit,
        "price" to item.product.ourPrice,
        "quantity" to item.quantity,
        "emoji" to item.product.emoji,
        "imageUrl" to (item.product.imageUrl ?: "")
      )
    },
    "subtotal" to o.subtotal,
    "totalSavings" to o.totalSavings,
    "deliveryCharge" to o.deliveryCharge,
    "heavyDeliveryCharge" to o.heavyDeliveryCharge,
    "grandTotal" to o.grandTotal,
    "advancePaid" to o.advancePaid,
    "balanceDueAtDelivery" to o.balanceDueAtDelivery,
    "paymentMethod" to o.paymentMethod,
    "upiTransactionRef" to o.upiTransactionRef,
    "deliveryDate" to o.deliveryDate,
    "createdAt" to if (useServerTimestamp) FieldValue.serverTimestamp() else Timestamp(o.createdAt / 1000, 0),
    "status" to o.status.name,
    "specialNote" to o.specialNote,
    "rating" to o.rating,
    "ratingReview" to o.ratingReview,
    "deliveryTip" to o.deliveryTip,
    "assignedRiderId" to o.assignedRiderId,
    "assignedRiderName" to o.assignedRiderName,
    "assignedRiderPhone" to o.assignedRiderPhone,
    "collectedPaymentMode" to o.collectedPaymentMode,
    "isRiderCashSettled" to o.isRiderCashSettled,
    "customerLat" to o.customerLat,
    "customerLng" to o.customerLng
  )

  private fun docToOrder(doc: DocumentSnapshot, currentUid: String): Order? {
    val id = doc.getString("id") ?: doc.id
    val userId = doc.getString("userId") ?: ""
    val customerName = doc.getString("customerName") ?: return null
    val rawItems = doc.get("items") as? List<*>
    val itemsList = rawItems?.mapIndexedNotNull { idx, item ->
      val m = item as? Map<*, *> ?: return@mapIndexedNotNull null
      val price = (m["price"] as? Number)?.toDouble() ?: 0.0
      val qty = (m["quantity"] as? Number)?.toInt() ?: 1
      val p = Product(
        id = (m["productId"] as? String) ?: "item_$idx",
        name = (m["name"] as? String) ?: "Grocery Item",
        hindiName = "",
        category = Category.ALL,
        unit = (m["unit"] as? String) ?: "1 unit",
        mrp = price,
        ourPrice = price,
        emoji = (m["emoji"] as? String) ?: "🛒",
        imageUrl = (m["imageUrl"] as? String)?.takeIf { it.isNotBlank() }
      )
      CartItem(p, qty)
    }.orEmpty()

    val statusName = doc.getString("status") ?: OrderStatus.RECEIVED.name
    val status = try { OrderStatus.valueOf(statusName) } catch (_: Exception) { OrderStatus.RECEIVED }
    val createdMillis = when (val ts = doc.get("createdAt")) {
      is Timestamp -> ts.toDate().time
      is Number -> ts.toLong()
      else -> System.currentTimeMillis()
    }

    return Order(
      id = id,
      userId = userId,
      customerName = customerName,
      customerPhone = doc.getString("customerPhone") ?: "",
      customerEmail = doc.getString("customerEmail") ?: "",
      area = doc.getString("area") ?: "पुंजीपथरा",
      addressDetail = doc.getString("addressDetail") ?: "",
      items = itemsList,
      subtotal = doc.getDouble("subtotal") ?: 0.0,
      totalSavings = doc.getDouble("totalSavings") ?: 0.0,
      deliveryCharge = doc.getDouble("deliveryCharge") ?: 60.0,
      heavyDeliveryCharge = doc.getDouble("heavyDeliveryCharge") ?: 0.0,
      grandTotal = doc.getDouble("grandTotal") ?: 0.0,
      advancePaid = doc.getDouble("advancePaid") ?: 0.0,
      balanceDueAtDelivery = doc.getDouble("balanceDueAtDelivery") ?: 0.0,
      paymentMethod = doc.getString("paymentMethod") ?: "",
      upiTransactionRef = doc.getString("upiTransactionRef") ?: "",
      deliveryDate = doc.getString("deliveryDate") ?: "Tomorrow Morning 8:00 AM Sharp",
      createdAt = createdMillis,
      status = status,
      specialNote = doc.getString("specialNote") ?: "",
      isMyOrder = userId == currentUid,
      rating = (doc.getLong("rating") ?: 0L).toInt(),
      ratingReview = doc.getString("ratingReview") ?: "",
      deliveryTip = doc.getDouble("deliveryTip") ?: 0.0,
      assignedRiderId = doc.getString("assignedRiderId") ?: "",
      assignedRiderName = doc.getString("assignedRiderName") ?: "",
      assignedRiderPhone = doc.getString("assignedRiderPhone") ?: "",
      collectedPaymentMode = doc.getString("collectedPaymentMode") ?: "",
      isRiderCashSettled = doc.getBoolean("isRiderCashSettled") ?: false,
      customerLat = doc.getDouble("customerLat") ?: 0.0,
      customerLng = doc.getDouble("customerLng") ?: 0.0
    )
  }

  private fun riderToMap(r: Rider): Map<String, Any?> = mapOf(
    "id" to r.id,
    "name" to r.name,
    "phone" to r.phone,
    "email" to r.email,
    "area" to r.area,
    "vehicleType" to r.vehicleType,
    "isActive" to r.isActive,
    "createdAt" to r.createdAt
  )

  private fun docToRider(doc: DocumentSnapshot): Rider? {
    val id = doc.getString("id") ?: doc.id
    val name = doc.getString("name") ?: return null
    val phone = doc.getString("phone") ?: return null
    return Rider(
      id = id,
      name = name,
      phone = phone,
      email = doc.getString("email") ?: "",
      area = doc.getString("area") ?: "रायगढ़ ➔ पुंजीपथरा",
      vehicleType = doc.getString("vehicleType") ?: "Bike",
      isActive = doc.getBoolean("isActive") ?: true,
      createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
    )
  }

  private fun customRequestToMap(r: CustomItemRequest, useServerTimestamp: Boolean): Map<String, Any?> = mapOf(
    "id" to r.id,
    "userId" to r.userId,
    "customerName" to r.customerName,
    "customerPhone" to r.customerPhone,
    "customerEmail" to r.customerEmail,
    "itemName" to r.itemName,
    "quantity" to r.quantity,
    "notes" to r.notes,
    "targetArea" to r.targetArea,
    "createdAt" to if (useServerTimestamp) FieldValue.serverTimestamp() else Timestamp(r.timestamp / 1000, 0),
    "status" to r.status
  )

  private fun docToCustomRequest(doc: DocumentSnapshot): CustomItemRequest? {
    val id = doc.getString("id") ?: doc.id
    val itemName = doc.getString("itemName") ?: return null
    val tsMillis = when (val ts = doc.get("createdAt")) {
      is Timestamp -> ts.toDate().time
      is Number -> ts.toLong()
      else -> System.currentTimeMillis()
    }
    return CustomItemRequest(
      id = id,
      userId = doc.getString("userId") ?: "",
      customerName = doc.getString("customerName") ?: "",
      customerPhone = doc.getString("customerPhone") ?: "",
      customerEmail = doc.getString("customerEmail") ?: "",
      itemName = itemName,
      quantity = doc.getString("quantity") ?: "",
      notes = doc.getString("notes") ?: "",
      targetArea = doc.getString("targetArea") ?: "पुंजीपथरा",
      timestamp = tsMillis,
      status = doc.getString("status") ?: "Reviewing"
    )
  }

  private fun storeConfigToMap(c: StoreConfig): Map<String, Any?> = mapOf(
    "shopName" to c.shopName,
    "tagline" to c.tagline,
    "founderName" to c.founderName,
    "founderInitials" to c.founderInitials,
    "founderHead" to c.founderHead,
    "founderMsg" to c.founderMsg,
    "whatsapp" to c.whatsapp,
    "upiId" to c.upiId,
    "upiName" to c.upiName,
    "deliveryFee" to c.deliveryFee,
    "heavyDeliveryFee" to c.heavyDeliveryFee,
    "advanceThreshold" to c.advanceThreshold,
    "adminPin" to c.adminPin,
    "storeOwnerEmail" to c.storeOwnerEmail,
    "routeCorridor" to c.routeCorridor,
    "deliveryTiming" to c.deliveryTiming,
    "cutoffHourText" to c.cutoffHourText,
    "deliveryScheduleDays" to c.deliveryScheduleDays,
    "launchDaysRemaining" to c.launchDaysRemaining,
    "launchDateText" to c.launchDateText,
    "announcementText" to c.announcementText,
    "isAcceptingOrders" to c.isAcceptingOrders,
    "isFestivalSaleActive" to c.isFestivalSaleActive,
    "festivalSaleHeading" to c.festivalSaleHeading,
    "festivalSaleSubtitle" to c.festivalSaleSubtitle,
    "festivalDiscountBadge" to c.festivalDiscountBadge,
    "welcomeMessage" to c.welcomeMessage,
    "heroBannerUrl" to c.heroBannerUrl,
    "showHeroBanner" to c.showHeroBanner,
    "categoryImages" to c.categoryImages,
    "customCategories" to c.customCategories.map { cc ->
      mapOf("id" to cc.id, "name" to cc.name, "hindiName" to cc.hindiName, "imageUrl" to cc.imageUrl)
    },
    "updatedAt" to FieldValue.serverTimestamp()
  )

  private fun docToStoreConfig(doc: DocumentSnapshot): StoreConfig? {
    val shopName = doc.getString("shopName") ?: return null
    val def = StoreConfig()
    val rawCatImgs = (doc.get("categoryImages") as? Map<*, *>)?.entries
      ?.mapNotNull { (k, v) -> if (k is String && v is String) k to v else null }
      ?.toMap() ?: emptyMap()
    val rawCustomCats = (doc.get("customCategories") as? List<*>)?.mapNotNull { item ->
      val m = item as? Map<*, *> ?: return@mapNotNull null
      val id = m["id"] as? String ?: return@mapNotNull null
      val name = m["name"] as? String ?: return@mapNotNull null
      CustomCategory(
        id = id,
        name = name,
        hindiName = (m["hindiName"] as? String) ?: name,
        imageUrl = (m["imageUrl"] as? String) ?: ""
      )
    } ?: emptyList()

    return StoreConfig(
      shopName = shopName,
      tagline = doc.getString("tagline") ?: def.tagline,
      founderName = doc.getString("founderName") ?: def.founderName,
      founderInitials = doc.getString("founderInitials") ?: def.founderInitials,
      founderHead = doc.getString("founderHead") ?: def.founderHead,
      founderMsg = doc.getString("founderMsg") ?: def.founderMsg,
      whatsapp = doc.getString("whatsapp") ?: def.whatsapp,
      upiId = doc.getString("upiId") ?: def.upiId,
      upiName = doc.getString("upiName") ?: def.upiName,
      deliveryFee = doc.getDouble("deliveryFee") ?: def.deliveryFee,
      heavyDeliveryFee = doc.getDouble("heavyDeliveryFee") ?: def.heavyDeliveryFee,
      advanceThreshold = doc.getDouble("advanceThreshold") ?: def.advanceThreshold,
      adminPin = doc.getString("adminPin") ?: def.adminPin,
      storeOwnerEmail = doc.getString("storeOwnerEmail") ?: def.storeOwnerEmail,
      routeCorridor = doc.getString("routeCorridor") ?: def.routeCorridor,
      deliveryTiming = doc.getString("deliveryTiming") ?: def.deliveryTiming,
      cutoffHourText = doc.getString("cutoffHourText") ?: def.cutoffHourText,
      deliveryScheduleDays = doc.getString("deliveryScheduleDays") ?: def.deliveryScheduleDays,
      launchDaysRemaining = (doc.getLong("launchDaysRemaining") ?: def.launchDaysRemaining.toLong()).toInt(),
      launchDateText = doc.getString("launchDateText") ?: def.launchDateText,
      announcementText = doc.getString("announcementText") ?: def.announcementText,
      isAcceptingOrders = doc.getBoolean("isAcceptingOrders") ?: def.isAcceptingOrders,
      isFestivalSaleActive = doc.getBoolean("isFestivalSaleActive") ?: def.isFestivalSaleActive,
      festivalSaleHeading = doc.getString("festivalSaleHeading") ?: def.festivalSaleHeading,
      festivalSaleSubtitle = doc.getString("festivalSaleSubtitle") ?: def.festivalSaleSubtitle,
      festivalDiscountBadge = doc.getString("festivalDiscountBadge") ?: def.festivalDiscountBadge,
      welcomeMessage = doc.getString("welcomeMessage") ?: def.welcomeMessage,
      heroBannerUrl = doc.getString("heroBannerUrl") ?: def.heroBannerUrl,
      showHeroBanner = doc.getBoolean("showHeroBanner") ?: def.showHeroBanner,
      categoryImages = rawCatImgs,
      customCategories = rawCustomCats
    )
  }

  // JSON Serialization helpers (offloaded to Dispatchers.IO for smooth 60fps UI)
  private fun saveProducts(list: List<Product>) {
    ioScope.launch {
      try {
        val jsonArray = JSONArray()
        for (p in list) {
          val obj = JSONObject().apply {
            put("id", p.id)
            put("name", p.name)
            put("hindiName", p.hindiName)
            put("category", p.category.name)
            put("unit", p.unit)
            put("mrp", p.mrp)
            put("ourPrice", p.ourPrice)
            put("inStock", p.inStock)
            put("tag", p.tag ?: "")
            put("emoji", p.emoji)
            put("imageUrl", p.imageUrl ?: "")
            put("description", p.description)
            put("isHeavy", p.isHeavy)
            val variantsArr = JSONArray()
            for (v in p.variants) {
              variantsArr.put(
                JSONObject().apply {
                  put("label", v.label)
                  put("mrp", v.mrp)
                  put("ourPrice", v.ourPrice)
                }
              )
            }
            put("variants", variantsArr)
            put("customCategoryId", p.customCategoryId)
          }
          jsonArray.put(obj)
        }
        prefs.edit().putString("saved_products_v8", jsonArray.toString()).apply()
      } catch (_: Exception) {}
    }
  }

  private fun loadProducts(): List<Product> {
    val raw = prefs.getString("saved_products_v8", null)
    if (raw.isNullOrBlank()) {
      return DefaultProducts.getInitialCatalog()
    }
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<Product>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val catName = obj.optString("category", Category.VEGETABLES.name)
        val cat = try { Category.valueOf(catName) } catch (_: Exception) { Category.VEGETABLES }
        val variantsList = mutableListOf<ProductVariant>()
        val varArr = obj.optJSONArray("variants")
        if (varArr != null) {
          for (j in 0 until varArr.length()) {
            val vObj = varArr.getJSONObject(j)
            variantsList.add(
              ProductVariant(
                label = vObj.getString("label"),
                mrp = vObj.optDouble("mrp", 0.0),
                ourPrice = vObj.optDouble("ourPrice", 0.0)
              )
            )
          }
        }
        list.add(
          Product(
            id = obj.getString("id"),
            name = obj.getString("name"),
            hindiName = obj.optString("hindiName", obj.getString("name")),
            category = cat,
            unit = obj.optString("unit", "1 kg"),
            mrp = obj.optDouble("mrp", 0.0),
            ourPrice = obj.optDouble("ourPrice", 0.0),
            inStock = obj.optBoolean("inStock", true),
            tag = obj.optString("tag").takeIf { it.isNotBlank() },
            emoji = obj.optString("emoji", "🛒"),
            imageUrl = obj.optString("imageUrl").takeIf { it.isNotBlank() },
            description = obj.optString("description", ""),
            isHeavy = obj.optBoolean("isHeavy", false),
            variants = variantsList,
            customCategoryId = obj.optString("customCategoryId", "")
          )
        )
      }
      if (list.isEmpty()) DefaultProducts.getInitialCatalog() else list
    } catch (_: Exception) {
      DefaultProducts.getInitialCatalog()
    }
  }

  private fun saveBanners(list: List<BannerAd>) {
    try {
      val array = JSONArray()
      for (b in list) {
        val obj = JSONObject().apply {
          put("id", b.id)
          put("title", b.title)
          put("subtitle", b.subtitle)
          put("badge", b.badge)
          put("discountTag", b.discountTag)
          put("isFestivalSpecial", b.isFestivalSpecial)
          put("isActive", b.isActive)
          put("mediaUrl", b.mediaUrl ?: "")
        }
        array.put(obj)
      }
      prefs.edit().putString("saved_banners_v5", array.toString()).apply()
    } catch (_: Exception) {}
  }

  private fun loadBanners(): List<BannerAd> {
    val raw = prefs.getString("saved_banners_v5", null)
    if (raw.isNullOrBlank()) {
      return seedDefaultBanners()
    }
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<BannerAd>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          BannerAd(
            id = obj.getString("id"),
            title = obj.getString("title"),
            subtitle = obj.getString("subtitle"),
            badge = obj.optString("badge", "OFFER"),
            discountTag = obj.optString("discountTag", "FLAT 20% OFF"),
            isFestivalSpecial = obj.optBoolean("isFestivalSpecial", false),
            isActive = obj.optBoolean("isActive", true),
            mediaUrl = obj.optString("mediaUrl").takeIf { it.isNotBlank() }
          )
        )
      }
      if (list.isEmpty()) seedDefaultBanners() else list
    } catch (_: Exception) {
      seedDefaultBanners()
    }
  }

  private fun seedDefaultBanners(): List<BannerAd> = listOf(
    BannerAd(
      id = "ban_1",
      title = "🪔 त्योहार व महाबचत सेल",
      subtitle = "पूजा सामग्री, शुद्ध घी, काजू-बादाम व मिठाइयों पर भारी छूट",
      badge = "त्योहार सेल",
      discountTag = "FLAT 20%-40% OFF",
      isFestivalSpecial = true,
      isActive = true
    ),
    BannerAd(
      id = "ban_2",
      title = "🥦 5 AM ताज़ी मंडी सब्ज़ियाँ",
      subtitle = "रायगढ़ मंडी से सीधी खरीद · सुबह 8:00 AM निश्चित डिलीवरी",
      badge = "सुपर बचत",
      discountTag = "Mandi Rate",
      isFestivalSpecial = false,
      isActive = true
    ),
    BannerAd(
      id = "ban_3",
      title = "🚚 रायगढ़ ➔ पुंजीपथरा कॉरिडोर स्पेशल",
      subtitle = "100% ओरिजिनल सामान, सही वजन व मृत्युंजय विश्वास गारंटी",
      badge = "MV गारंटी",
      discountTag = "100% Trust",
      isFestivalSpecial = false,
      isActive = true
    )
  )

  private fun saveOrders(orders: List<Order>) {
    try {
      val array = JSONArray()
      for (o in orders) {
        val obj = JSONObject().apply {
          put("id", o.id)
          put("userId", o.userId)
          put("customerName", o.customerName)
          put("customerPhone", o.customerPhone)
          put("customerEmail", o.customerEmail)
          put("area", o.area)
          put("addressDetail", o.addressDetail)
          put("subtotal", o.subtotal)
          put("totalSavings", o.totalSavings)
          put("deliveryCharge", o.deliveryCharge)
          put("heavyDeliveryCharge", o.heavyDeliveryCharge)
          put("grandTotal", o.grandTotal)
          put("advancePaid", o.advancePaid)
          put("balanceDueAtDelivery", o.balanceDueAtDelivery)
          put("paymentMethod", o.paymentMethod)
          put("upiTransactionRef", o.upiTransactionRef)
          put("deliveryDate", o.deliveryDate)
          put("createdAt", o.createdAt)
          put("status", o.status.name)
          put("specialNote", o.specialNote)
          put("isMyOrder", o.isMyOrder)
          put("rating", o.rating)
          put("ratingReview", o.ratingReview)
          put("deliveryTip", o.deliveryTip)
          put("assignedRiderId", o.assignedRiderId)
          put("assignedRiderName", o.assignedRiderName)
          put("assignedRiderPhone", o.assignedRiderPhone)
          put("collectedPaymentMode", o.collectedPaymentMode)
          put("isRiderCashSettled", o.isRiderCashSettled)
          put("customerLat", o.customerLat)
          put("customerLng", o.customerLng)

          val itemsArr = JSONArray()
          for (item in o.items) {
            val itm = JSONObject().apply {
              put("productId", item.product.id)
              put("name", item.product.name)
              put("unit", item.product.unit)
              put("price", item.product.ourPrice)
              put("quantity", item.quantity)
              put("emoji", item.product.emoji)
              put("imageUrl", item.product.imageUrl ?: "")
            }
            itemsArr.put(itm)
          }
          put("items", itemsArr)
        }
        array.put(obj)
      }
      prefs.edit().putString("saved_orders_v5", array.toString()).apply()
    } catch (_: Exception) {}
  }

  private fun loadOrders(): List<Order> {
    val raw = prefs.getString("saved_orders_v5", null)
    if (raw.isNullOrBlank()) {
      return emptyList()
    }
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<Order>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val itemsList = mutableListOf<CartItem>()
        val itemsArr = obj.optJSONArray("items")
        if (itemsArr != null) {
          for (j in 0 until itemsArr.length()) {
            val itmObj = itemsArr.getJSONObject(j)
            val p = Product(
              id = itmObj.optString("productId", "item_$j"),
              name = itmObj.optString("name", "Grocery Item"),
              hindiName = "",
              category = Category.ALL,
              unit = itmObj.optString("unit", "1 unit"),
              mrp = itmObj.optDouble("price", 50.0),
              ourPrice = itmObj.optDouble("price", 50.0),
              emoji = itmObj.optString("emoji", "🛒"),
              imageUrl = itmObj.optString("imageUrl").takeIf { it.isNotBlank() }
            )
            itemsList.add(CartItem(p, itmObj.optInt("quantity", 1)))
          }
        }
        val statusName = obj.optString("status", OrderStatus.RECEIVED.name)
        val status = try { OrderStatus.valueOf(statusName) } catch (_: Exception) { OrderStatus.RECEIVED }

        list.add(
          Order(
            id = obj.getString("id"),
            userId = obj.optString("userId", ""),
            customerName = obj.getString("customerName"),
            customerPhone = obj.getString("customerPhone"),
            customerEmail = obj.optString("customerEmail", ""),
            area = obj.optString("area", "पुंजीपथरा"),
            addressDetail = obj.optString("addressDetail", ""),
            items = itemsList,
            subtotal = obj.optDouble("subtotal", 0.0),
            totalSavings = obj.optDouble("totalSavings", 0.0),
            deliveryCharge = obj.optDouble("deliveryCharge", 60.0),
            heavyDeliveryCharge = obj.optDouble("heavyDeliveryCharge", 0.0),
            grandTotal = obj.optDouble("grandTotal", 0.0),
            advancePaid = obj.optDouble("advancePaid", 60.0),
            balanceDueAtDelivery = obj.optDouble("balanceDueAtDelivery", 200.0),
            paymentMethod = obj.optString("paymentMethod", "UPI (Advance ₹60 Paid)"),
            upiTransactionRef = obj.optString("upiTransactionRef", "UPI-OKAXIS-5520"),
            deliveryDate = obj.optString("deliveryDate", "Tomorrow Morning 8:00 AM Sharp"),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            status = status,
            specialNote = obj.optString("specialNote", ""),
            isMyOrder = obj.optBoolean("isMyOrder", false),
            rating = obj.optInt("rating", 0),
            ratingReview = obj.optString("ratingReview", ""),
            deliveryTip = obj.optDouble("deliveryTip", 0.0),
            assignedRiderId = obj.optString("assignedRiderId", ""),
            assignedRiderName = obj.optString("assignedRiderName", ""),
            assignedRiderPhone = obj.optString("assignedRiderPhone", ""),
            collectedPaymentMode = obj.optString("collectedPaymentMode", ""),
            isRiderCashSettled = obj.optBoolean("isRiderCashSettled", false),
            customerLat = obj.optDouble("customerLat", 0.0),
            customerLng = obj.optDouble("customerLng", 0.0)
          )
        )
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun saveCustomRequests(list: List<CustomItemRequest>) {
    try {
      val array = JSONArray()
      for (r in list) {
        val obj = JSONObject().apply {
          put("id", r.id)
          put("userId", r.userId)
          put("customerName", r.customerName)
          put("customerPhone", r.customerPhone)
          put("customerEmail", r.customerEmail)
          put("itemName", r.itemName)
          put("quantity", r.quantity)
          put("notes", r.notes)
          put("targetArea", r.targetArea)
          put("timestamp", r.timestamp)
          put("status", r.status)
        }
        array.put(obj)
      }
      prefs.edit().putString("saved_custom_requests_v5", array.toString()).apply()
    } catch (_: Exception) {}
  }

  private fun loadCustomRequests(): List<CustomItemRequest> {
    val raw = prefs.getString("saved_custom_requests_v5", null) ?: return emptyList()
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<CustomItemRequest>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          CustomItemRequest(
            id = obj.getString("id"),
            userId = obj.optString("userId", ""),
            customerName = obj.getString("customerName"),
            customerPhone = obj.getString("customerPhone"),
            customerEmail = obj.optString("customerEmail", ""),
            itemName = obj.getString("itemName"),
            quantity = obj.getString("quantity"),
            notes = obj.optString("notes", ""),
            targetArea = obj.optString("targetArea", "पुंजीपथरा"),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            status = obj.optString("status", "Reviewing")
          )
        )
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun saveStoreConfig(config: StoreConfig) {
    try {
      val obj = JSONObject().apply {
        put("shopName", config.shopName)
        put("tagline", config.tagline)
        put("founderName", config.founderName)
        put("founderInitials", config.founderInitials)
        put("founderHead", config.founderHead)
        put("founderMsg", config.founderMsg)
        put("whatsapp", config.whatsapp)
        put("upiId", config.upiId)
        put("upiName", config.upiName)
        put("deliveryFee", config.deliveryFee)
        put("heavyDeliveryFee", config.heavyDeliveryFee)
        put("advanceThreshold", config.advanceThreshold)
        put("adminPin", config.adminPin)
        put("storeOwnerEmail", config.storeOwnerEmail)
        put("routeCorridor", config.routeCorridor)
        put("deliveryTiming", config.deliveryTiming)
        put("cutoffHourText", config.cutoffHourText)
        put("deliveryScheduleDays", config.deliveryScheduleDays)
        put("launchDaysRemaining", config.launchDaysRemaining)
        put("launchDateText", config.launchDateText)
        put("announcementText", config.announcementText)
        put("isAcceptingOrders", config.isAcceptingOrders)
        put("isFestivalSaleActive", config.isFestivalSaleActive)
        put("festivalSaleHeading", config.festivalSaleHeading)
        put("festivalSaleSubtitle", config.festivalSaleSubtitle)
        put("festivalDiscountBadge", config.festivalDiscountBadge)
        put("welcomeMessage", config.welcomeMessage)
        put("heroBannerUrl", config.heroBannerUrl)
        put("showHeroBanner", config.showHeroBanner)
        val catImgObj = JSONObject()
        config.categoryImages.forEach { (k, v) -> catImgObj.put(k, v) }
        put("categoryImages", catImgObj)
        val ccArr = JSONArray()
        config.customCategories.forEach { cc ->
          ccArr.put(
            JSONObject().apply {
              put("id", cc.id)
              put("name", cc.name)
              put("hindiName", cc.hindiName)
              put("imageUrl", cc.imageUrl)
            }
          )
        }
        put("customCategories", ccArr)
      }
      prefs.edit().putString("store_config_v5", obj.toString()).apply()
    } catch (_: Exception) {}
  }

  private fun loadStoreConfig(): StoreConfig {
    val raw = prefs.getString("store_config_v5", null) ?: return StoreConfig()
    return try {
      val obj = JSONObject(raw)
      val pin = obj.optString("adminPin", "5520").let { if (it == "8982") "5520" else it }
      val catImgMap = mutableMapOf<String, String>()
      obj.optJSONObject("categoryImages")?.let { catObj ->
        catObj.keys().forEach { k ->
          val v = catObj.optString(k, "")
          if (v.isNotBlank()) catImgMap[k] = v
        }
      }
      val customCats = mutableListOf<CustomCategory>()
      obj.optJSONArray("customCategories")?.let { arr ->
        for (i in 0 until arr.length()) {
          val cObj = arr.optJSONObject(i) ?: continue
          val cid = cObj.optString("id", "")
          val cname = cObj.optString("name", "")
          if (cid.isNotBlank() && cname.isNotBlank()) {
            customCats.add(
              CustomCategory(
                id = cid,
                name = cname,
                hindiName = cObj.optString("hindiName", cname),
                imageUrl = cObj.optString("imageUrl", "")
              )
            )
          }
        }
      }
      StoreConfig(
        shopName = obj.optString("shopName", "अपना मार्केट"),
        tagline = obj.optString("tagline", "रायगढ़ ➔ पुंजीपथरा · सुपरफास्ट होम डिलीवरी"),
        founderName = obj.optString("founderName", "मृत्युंजय विश्वास"),
        founderInitials = obj.optString("founderInitials", "MV"),
        founderHead = obj.optString("founderHead", "100% सुरक्षित एवं विश्वसनीय सेवा"),
        founderMsg = obj.optString("founderMsg", StoreConfig().founderMsg),
        whatsapp = obj.optString("whatsapp", "8982903843"),
        upiId = obj.optString("upiId", "mukkubishwas81-2@okaxis"),
        upiName = obj.optString("upiName", "मृत्युंजय विश्वास"),
        deliveryFee = obj.optDouble("deliveryFee", 60.0),
        heavyDeliveryFee = obj.optDouble("heavyDeliveryFee", 20.0),
        advanceThreshold = obj.optDouble("advanceThreshold", 500.0),
        adminPin = pin,
        storeOwnerEmail = obj.optString("storeOwnerEmail", "mrityunjayvishwas294@gmail.com"),
        routeCorridor = obj.optString("routeCorridor", "रायगढ़ ➔ पुंजीपथरा कॉरिडोर"),
        deliveryTiming = obj.optString("deliveryTiming", "सुबह ठीक 8:00 AM"),
        cutoffHourText = obj.optString("cutoffHourText", "रात 8:00 बजे"),
        deliveryScheduleDays = obj.optString("deliveryScheduleDays", "प्रतिदिन (सोमवार से रविवार)"),
        launchDaysRemaining = obj.optInt("launchDaysRemaining", 15),
        launchDateText = obj.optString("launchDateText", "15 दिन बाद रात 8:00 बजे से बुकिंग"),
        announcementText = obj.optString("announcementText", StoreConfig().announcementText),
        isAcceptingOrders = obj.optBoolean("isAcceptingOrders", true),
        isFestivalSaleActive = obj.optBoolean("isFestivalSaleActive", true),
        festivalSaleHeading = obj.optString("festivalSaleHeading", "🪔 त्योहार महासेल & ऑफर्स"),
        festivalSaleSubtitle = obj.optString("festivalSaleSubtitle", "मंडी रेट से भी कम दाम · सीधे रायगढ़ से पुंजीपथरा आपके द्वार"),
        festivalDiscountBadge = obj.optString("festivalDiscountBadge", "FLAT 15% - 40% OFF"),
        welcomeMessage = obj.optString("welcomeMessage", StoreConfig().welcomeMessage),
        heroBannerUrl = obj.optString("heroBannerUrl", ""),
        showHeroBanner = obj.optBoolean("showHeroBanner", true),
        categoryImages = catImgMap,
        customCategories = customCats
      )
    } catch (_: Exception) {
      StoreConfig()
    }
  }

  private fun saveRiders(list: List<Rider>) {
    try {
      val array = JSONArray()
      for (r in list) {
        val obj = JSONObject().apply {
          put("id", r.id)
          put("name", r.name)
          put("phone", r.phone)
          put("email", r.email)
          put("area", r.area)
          put("vehicleType", r.vehicleType)
          put("isActive", r.isActive)
          put("createdAt", r.createdAt)
        }
        array.put(obj)
      }
      prefs.edit().putString("saved_riders_v1", array.toString()).apply()
    } catch (_: Exception) {}
  }

  private fun loadRiders(): List<Rider> {
    val raw = prefs.getString("saved_riders_v1", null) ?: return emptyList()
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<Rider>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          Rider(
            id = obj.getString("id"),
            name = obj.getString("name"),
            phone = obj.getString("phone"),
            email = obj.optString("email", ""),
            area = obj.optString("area", "रायगढ़ ➔ पुंजीपथरा"),
            vehicleType = obj.optString("vehicleType", "Bike"),
            isActive = obj.optBoolean("isActive", true),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
          )
        )
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }
}
