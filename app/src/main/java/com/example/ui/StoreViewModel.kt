package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.AppLanguage
import com.example.data.DefaultProducts
import com.example.data.StoreRepository
import com.example.ui.screens.signOutGoogle
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.example.model.BannerAd
import com.example.model.CartItem
import com.example.model.Category
import com.example.model.CorridorArea
import com.example.model.CustomItemRequest
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.ProductVariant
import com.example.model.Rider
import com.example.model.StoreConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class Screen {
  HOME,
  CART,
  CUSTOM_REQUEST,
  ORDERS,
  ADMIN_HUB,
  GUARANTEE_DETAIL
}

class StoreViewModel(application: Application) : AndroidViewModel(application) {
  private val firebaseAuth = Firebase.auth
  private val firestoreDb = FirebaseFirestore.getInstance(
    application.getString(R.string.firestore_database_id)
  )
  private val repository = StoreRepository(
    context = application,
    firestore = firestoreDb,
    auth = firebaseAuth
  )

  val currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
  val currentScreen = MutableStateFlow(Screen.HOME)
  val selectedLanguage = MutableStateFlow(repository.getLanguage())

  fun setLanguage(lang: AppLanguage) {
    selectedLanguage.value = lang
    repository.saveLanguage(lang)
  }

  init {
    firebaseAuth.currentUser?.let { repository.startSync(it) }
  }

  fun onUserSignedIn(user: FirebaseUser) {
    currentUser.value = user
    repository.startSync(user)
  }

  fun signOut(context: Context) {
    repository.stopSync()
    viewModelScope.launch {
      signOutGoogle(context)
      currentUser.value = null
      currentScreen.value = Screen.HOME
    }
  }

  override fun onCleared() {
    super.onCleared()
    repository.stopSync()
  }

  val products: StateFlow<List<Product>> = repository.products
  val cart: StateFlow<Map<String, CartItem>> = repository.cart
  val orders: StateFlow<List<Order>> = repository.orders
  val customRequests: StateFlow<List<CustomItemRequest>> = repository.customRequests
  val selectedArea: StateFlow<CorridorArea> = repository.selectedArea
  val storeConfig: StateFlow<StoreConfig> = repository.storeConfig
  val banners: StateFlow<List<BannerAd>> = repository.banners
  val riders: StateFlow<List<Rider>> = repository.riders

  val searchQuery = MutableStateFlow("")
  val selectedCategory = MutableStateFlow(Category.ALL)

  // Derived filtered products
  val filteredProducts: StateFlow<List<Product>> =
    combine(products, searchQuery, selectedCategory) { prodList, query, cat ->
      prodList.filter { product ->
        val matchesCategory = (cat == Category.ALL || product.category == cat)
        val matchesQuery = query.isBlank() ||
            product.name.contains(query, ignoreCase = true) ||
            product.hindiName.contains(query, ignoreCase = true) ||
            product.description.contains(query, ignoreCase = true)
        matchesCategory && matchesQuery
      }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  // Cart aggregates
  val cartItemCount: StateFlow<Int> =
    combine(cart) { array ->
      array[0].values.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

  val cartTotalPrice: StateFlow<Double> =
    combine(cart) { array ->
      array[0].values.sumOf { it.product.ourPrice * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

  val cartTotalSavings: StateFlow<Double> =
    combine(cart) { array ->
      val mrpTotal = array[0].values.sumOf { it.product.mrp * it.quantity }
      val ourTotal = array[0].values.sumOf { it.product.ourPrice * it.quantity }
      if (mrpTotal > ourTotal) mrpTotal - ourTotal else 0.0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

  val hasHeavyItems: StateFlow<Boolean> =
    combine(cart) { array ->
      array[0].values.any { it.product.isHeavy || it.quantity >= 3 }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

  // Advance delivery charge required when cart subtotal is <= advanceThreshold (₹500)
  val isAdvanceDeliveryChargeRequired: StateFlow<Boolean> =
    combine(cartTotalPrice, storeConfig) { price, config ->
      price > 0 && price <= config.advanceThreshold
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

  // Navigation
  fun navigateTo(screen: Screen) {
    currentScreen.value = screen
  }

  // Cart operations
  fun addToCart(product: Product, variant: ProductVariant? = null) {
    repository.addToCart(product, variant)
  }

  fun removeFromCart(cartKeyOrProductId: String) {
    repository.removeFromCart(cartKeyOrProductId)
  }

  fun removeCompletelyFromCart(cartKeyOrProductId: String) {
    repository.removeCompletelyFromCart(cartKeyOrProductId)
  }

  fun clearCart() {
    repository.clearCart()
  }

  fun selectArea(area: CorridorArea) {
    repository.selectArea(area)
  }

  // Order placement
  fun placeOrder(
    customerName: String,
    customerPhone: String,
    customerEmail: String,
    addressDetail: String,
    paymentMethod: String,
    specialNote: String,
    advancePaid: Double = 0.0,
    upiRef: String = "",
    onSuccess: (Order) -> Unit
  ) {
    val order = repository.placeOrder(
      customerName = customerName,
      customerPhone = customerPhone,
      customerEmail = customerEmail,
      areaName = selectedArea.value.name,
      addressDetail = addressDetail,
      paymentMethod = paymentMethod,
      specialNote = specialNote,
      advancePaid = advancePaid,
      upiRef = upiRef
    )
    onSuccess(order)
  }

  // Custom requests
  fun submitCustomRequest(
    customerName: String,
    customerPhone: String,
    customerEmail: String,
    itemName: String,
    quantity: String,
    notes: String,
    onSuccess: (CustomItemRequest) -> Unit
  ) {
    val req = repository.submitCustomRequest(
      customerName = customerName,
      customerPhone = customerPhone,
      customerEmail = customerEmail,
      itemName = itemName,
      quantity = quantity,
      notes = notes,
      area = selectedArea.value.name
    )
    onSuccess(req)
  }

  // Admin PIN verification - supports pin 5520 and configured pin
  fun verifyAdminPin(enteredPin: String): Boolean {
    val clean = enteredPin.trim()
    return clean == storeConfig.value.adminPin.trim() || clean == "5520"
  }

  // Admin / Store Owner controls (Admin Only)
  fun updateProduct(product: Product) {
    repository.updateProduct(product)
    Toast.makeText(getApplication(), "उत्पाद अपडेट: ${product.name}", Toast.LENGTH_SHORT).show()
  }

  fun addProduct(product: Product) {
    repository.addProduct(product)
    Toast.makeText(getApplication(), "नया उत्पाद कैटलॉग में जुड़ा!", Toast.LENGTH_SHORT).show()
  }

  fun deleteProduct(productId: String) {
    repository.deleteProduct(productId)
    Toast.makeText(getApplication(), "उत्पाद हटाया गया", Toast.LENGTH_SHORT).show()
  }

  fun toggleProductStock(productId: String) {
    repository.toggleProductStock(productId)
  }

  fun updateOrderStatus(orderId: String, status: OrderStatus) {
    repository.updateOrderStatus(orderId, status)
    Toast.makeText(getApplication(), "ऑर्डर $orderId स्थिति: ${status.hindiLabel}", Toast.LENGTH_SHORT).show()
  }

  fun rateAndTipOrder(orderId: String, rating: Int, review: String, tipAmount: Double) {
    repository.rateAndTipOrder(orderId, rating, review, tipAmount)
    val tipMsg = if (tipAmount > 0) " और ₹${tipAmount.toInt()} टिप" else ""
    Toast.makeText(getApplication(), "🙏 ${rating}★ रेटिंग$tipMsg के लिए दिल से धन्यवाद!", Toast.LENGTH_SHORT).show()
  }

  fun updateCustomRequestStatus(requestId: String, status: String) {
    repository.updateCustomRequestStatus(requestId, status)
    Toast.makeText(getApplication(), "अनुरोध $requestId अपडेट हुआ", Toast.LENGTH_SHORT).show()
  }

  // Hired Delivery Riders controls
  fun addRider(name: String, phone: String, email: String, area: String, vehicleType: String) {
    val rider = repository.addRider(name, phone, email, area, vehicleType)
    Toast.makeText(getApplication(), "नया राइडर जुड़ा: ${rider.name}", Toast.LENGTH_SHORT).show()
  }

  fun toggleRiderActive(riderId: String) {
    repository.toggleRiderActive(riderId)
  }

  fun deleteRider(riderId: String) {
    repository.deleteRider(riderId)
    Toast.makeText(getApplication(), "राइडर हटाया गया", Toast.LENGTH_SHORT).show()
  }

  fun assignRiderToOrder(orderId: String, rider: Rider) {
    repository.assignRiderToOrder(orderId, rider)
    Toast.makeText(getApplication(), "ऑर्डर $orderId राइडर ${rider.name} को असाइन हुआ!", Toast.LENGTH_SHORT).show()
  }

  fun updateOrderCollection(orderId: String, mode: String, settled: Boolean) {
    repository.updateOrderCollection(orderId, mode, settled)
    val msg = when {
      settled -> "ऑर्डर $orderId का हिसाब पूरा (Settled) हुआ!"
      mode == "CASH" -> "राइडर ने कैश कलेक्ट किया (जमा बाकी)"
      mode == "UPI" -> "सीधे Admin UPI पर पेमेंट प्राप्त हुआ"
      else -> "कलेक्शन स्थिति अपडेट हुई"
    }
    Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
  }

  fun updateStoreConfig(config: StoreConfig) {
    repository.updateStoreConfig(config)
    Toast.makeText(getApplication(), "दुकान नियंत्रण सेटिंग्स सेव हो गई!", Toast.LENGTH_SHORT).show()
  }

  // Banner / Sale Ads controls
  fun addBanner(banner: BannerAd) {
    repository.addBanner(banner)
    Toast.makeText(getApplication(), "नया सेल बैनर जोड़ा गया!", Toast.LENGTH_SHORT).show()
  }

  fun updateBanner(banner: BannerAd) {
    repository.updateBanner(banner)
    Toast.makeText(getApplication(), "बैनर अपडेट हुआ!", Toast.LENGTH_SHORT).show()
  }

  fun toggleBanner(bannerId: String) {
    repository.toggleBanner(bannerId)
  }

  fun deleteBanner(bannerId: String) {
    repository.deleteBanner(bannerId)
    Toast.makeText(getApplication(), "बैनर हटाया गया", Toast.LENGTH_SHORT).show()
  }

  fun toggleFestivalSale(
    active: Boolean,
    heading: String? = null,
    subtitle: String? = null,
    badge: String? = null
  ) {
    val cfg = storeConfig.value
    val updated = cfg.copy(
      isFestivalSaleActive = active,
      festivalSaleHeading = heading ?: cfg.festivalSaleHeading,
      festivalSaleSubtitle = subtitle ?: cfg.festivalSaleSubtitle,
      festivalDiscountBadge = badge ?: cfg.festivalDiscountBadge
    )
    repository.updateStoreConfig(updated)
    val msg = if (active) "त्योहार महासेल चालू कर दी गई!" else "त्योहार महासेल बंद की गई"
    Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
  }

  fun updateDeliverySchedule(
    timing: String,
    cutoff: String,
    scheduleDays: String,
    launchDays: Int
  ) {
    val cfg = storeConfig.value
    val updated = cfg.copy(
      deliveryTiming = timing,
      cutoffHourText = cutoff,
      deliveryScheduleDays = scheduleDays,
      launchDaysRemaining = launchDays,
      launchDateText = if (launchDays > 0) "$launchDays दिन बाद $cutoff से बुकिंग" else "लाइव बुकिंग चालू है"
    )
    repository.updateStoreConfig(updated)
    Toast.makeText(getApplication(), "डिलीवरी टाइमिंग व शेड्यूल सभी ऐप में अपडेट हुआ!", Toast.LENGTH_SHORT).show()
  }

  // WhatsApp Intent helper
  fun openWhatsApp(
    context: Context,
    phone: String = storeConfig.value.whatsapp,
    message: String = "नमस्ते मृत्युंजय जी, मुझे अपना मार्केट (रायगढ़-पुंजीपथरा) से सामान मँगाना है।"
  ) {
    try {
      val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
      val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
      val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(message)}"
      val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: Exception) {
      callPhone(context, phone)
    }
  }

  // UPI payment helper
  fun openUpiPayment(
    context: Context,
    amount: Double,
    orderId: String,
    note: String = "Apna Market Delivery Advance"
  ) {
    val config = storeConfig.value
    val upiUri = Uri.parse(
      "upi://pay?pa=${config.upiId}&pn=${Uri.encode(config.upiName)}&am=${amount}&cu=INR&tn=${Uri.encode("$note $orderId")}"
    )
    val intent = Intent(Intent.ACTION_VIEW, upiUri).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
      context.startActivity(Intent.createChooser(intent, "Pay ₹${amount.toInt()} via UPI (GPay/PhonePe/Paytm)"))
    } catch (_: Exception) {
      Toast.makeText(context, "UPI ID: ${config.upiId} (${config.upiName})", Toast.LENGTH_LONG).show()
    }
  }

  // Email helper
  fun openEmailComposer(
    context: Context,
    subject: String = "अपना मार्केट रायगढ़-पुंजीपथरा ऑर्डर पूछताछ",
    body: String = "नमस्ते मृत्युंजय जी,\n\nमैं अपना मार्केट (रायगढ़ से पुंजीपथरा) से सामान के संबंध में संपर्क कर रहा हूँ।\n\n"
  ) {
    try {
      val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(storeConfig.value.storeOwnerEmail))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(Intent.createChooser(intent, "Send Email to Mrityunjay"))
    } catch (_: Exception) {
      Toast.makeText(context, "Email: ${storeConfig.value.storeOwnerEmail}", Toast.LENGTH_LONG).show()
    }
  }

  fun callPhone(context: Context, phone: String = storeConfig.value.whatsapp) {
    try {
      val cleanPhone = phone.replace(" ", "")
      val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$cleanPhone")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: Exception) {}
  }
}
