package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import com.example.data.openLocationInMaps
import com.example.model.CustomCategory
import com.example.ui.components.rememberCoilData
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.DefaultProducts
import com.example.model.BannerAd
import com.example.model.Category
import com.example.model.CustomItemRequest
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.ProductVariant
import com.example.model.Rider
import com.example.model.StoreConfig
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.theme.JioMartBlue
import com.example.ui.theme.JioMartBlueDark
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenDark
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.SecondaryGold
import com.example.ui.theme.TertiaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHubScreen(
  viewModel: StoreViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val products by viewModel.products.collectAsState()
  val orders by viewModel.orders.collectAsState()
  val customRequests by viewModel.customRequests.collectAsState()
  val storeConfig by viewModel.storeConfig.collectAsState()
  val banners by viewModel.banners.collectAsState()
  val riders by viewModel.riders.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabTitles = listOf("उत्पाद व फोटो", "कैटेगरी", "सेल व विज्ञापन", "डिलीवरी टाइमिंग", "8 AM ऑर्डर्स", "राइडर हायर", "कंट्रोल सेटिंग्स")

  var showAddProductDialog by remember { mutableStateOf(false) }
  var productToEdit by remember { mutableStateOf<Product?>(null) }
  var showAddBannerDialog by remember { mutableStateOf(false) }
  var showAddRiderDialog by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }

  BackHandler {
    viewModel.navigateTo(Screen.HOME)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "मृत्युंजय कंट्रोल हब (Admin)",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Text(
              text = "एडमिन पिन: **** · पूरा नियंत्रण आपके हाथ में",
              fontSize = 11.sp,
              color = Color(0xFFBAE6FD)
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = { viewModel.navigateTo(Screen.HOME) },
            modifier = Modifier.testTag("admin_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }
        },
        actions = {
          if (selectedTab == 0) {
            IconButton(
              onClick = { showAddProductDialog = true },
              modifier = Modifier.testTag("admin_add_product_button")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product", tint = Color.White)
            }
          } else if (selectedTab == 1) {
            IconButton(
              onClick = { showAddBannerDialog = true }
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "Add Banner", tint = Color.White)
            }
          } else if (selectedTab == 4) {
            IconButton(
              onClick = { showAddRiderDialog = true },
              modifier = Modifier.testTag("admin_add_rider_button")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "Hire Rider", tint = Color.White)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = JioMartBlueDark,
          titleContentColor = Color.White
        )
      )
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(Color(0xFFF4F6F8))
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 1000.dp)
        ) {
          // Live Business Summary & Store ON/OFF Bar
      val totalRevenue = remember(orders) { orders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.grandTotal } }
      val pendingCount = remember(orders) { orders.count { it.status == OrderStatus.RECEIVED || it.status == OrderStatus.MANDI_PROCUREMENT || it.status == OrderStatus.PACKED } }
      val outOfStockCount = remember(products) { products.count { !it.inStock } }

      Surface(
        color = Color(0xFF0369A1),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              color = Color.White.copy(alpha = 0.14f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                Text("कुल बिक्री", fontSize = 10.sp, color = Color(0xFFBAE6FD))
                Text("₹${totalRevenue.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
              }
            }
            Surface(
              color = Color.White.copy(alpha = 0.14f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                Text("पेंडिंग ऑर्डर", fontSize = 10.sp, color = Color(0xFFBAE6FD))
                Text("$pendingCount", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFDE047))
              }
            }
            Surface(
              color = Color.White.copy(alpha = 0.14f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                Text("स्टॉक आउट", fontSize = 10.sp, color = Color(0xFFBAE6FD))
                Text("$outOfStockCount / ${products.size}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
              }
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (storeConfig.isAcceptingOrders) "🟢 दुकान चालू है (ऑर्डर ले रहे हैं)" else "🔴 दुकान बंद है (ऑर्डर रुके हैं)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Switch(
              checked = storeConfig.isAcceptingOrders,
              onCheckedChange = { accepting ->
                viewModel.updateStoreConfig(storeConfig.copy(isAcceptingOrders = accepting))
              }
            )
          }
        }
      }

      ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = JioMartBlueDark,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
              TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = JioMartBlue
              )
            }
          ) {
            tabTitles.forEachIndexed { index, title ->
              Tab(
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                text = {
                  Text(
                    text = title,
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                  )
                }
              )
            }
          }

          when (selectedTab) {
            0 -> ProductsManagementTab(
              products = products,
              searchQuery = searchQuery,
              onSearchChange = { searchQuery = it },
              onEditProduct = { productToEdit = it },
              onToggleStock = { viewModel.toggleProductStock(it) },
              onDeleteProduct = { viewModel.deleteProduct(it) },
              onAddNew = { showAddProductDialog = true }
            )
            1 -> com.example.ui.components.CategoryManagerTab(
              storeConfig = storeConfig,
              onUpdateCategoryImage = { key, url -> viewModel.updateCategoryImage(key, url) },
              onAddCustomCategory = { n, hn, url -> viewModel.addCustomCategory(n, hn, url) },
              onDeleteCustomCategory = { id -> viewModel.deleteCustomCategory(id) }
            )
            2 -> OffersAndBannersTab(
              storeConfig = storeConfig,
              banners = banners,
              onToggleFestivalSale = { active ->
                viewModel.toggleFestivalSale(active)
              },
              onUpdateFestivalDetails = { head, sub, badge ->
                viewModel.toggleFestivalSale(true, head, sub, badge)
              },
              onToggleBanner = { viewModel.toggleBanner(it) },
              onDeleteBanner = { viewModel.deleteBanner(it) },
              onAddBanner = { showAddBannerDialog = true }
            )
            3 -> DeliveryTimingAndScheduleTab(
              storeConfig = storeConfig,
              onSave = { updated ->
                viewModel.updateStoreConfig(updated)
                Toast.makeText(context, "दुकान, पोस्टर व टाइमिंग सेव हो गई!", Toast.LENGTH_SHORT).show()
              }
            )
            4 -> OrdersManagementTab(
              orders = orders,
              riders = riders,
              onStatusChange = { id, status -> viewModel.updateOrderStatus(id, status) },
              onAssignRider = { orderId, rider -> viewModel.assignRiderToOrder(orderId, rider) },
              onUpdateCollection = { orderId, mode, settled -> viewModel.updateOrderCollection(orderId, mode, settled) },
              onCall = { viewModel.callPhone(context, it) },
              onWhatsApp = { phone, msg -> viewModel.openWhatsApp(context, phone, msg) }
            )
            5 -> RidersManagementTab(
              riders = riders,
              orders = orders,
              onAddRider = { showAddRiderDialog = true },
              onToggleActive = { viewModel.toggleRiderActive(it) },
              onDeleteRider = { viewModel.deleteRider(it) },
              onCall = { viewModel.callPhone(context, it) },
              onWhatsApp = { phone, msg -> viewModel.openWhatsApp(context, phone, msg) }
            )
            6 -> StoreSettingsTab(
              storeConfig = storeConfig,
              onSave = { viewModel.updateStoreConfig(it) }
            )
          }
        }
      }
    }
  }

  // Dialog: Add Product with Image set option
  if (showAddProductDialog) {
    ProductEditorDialog(
      initialProduct = null,
      customCategories = storeConfig.customCategories,
      onDismiss = { showAddProductDialog = false },
      onSave = { newProd ->
        viewModel.addProduct(newProd)
        showAddProductDialog = false
      }
    )
  }

  // Dialog: Edit Product with Image set option
  productToEdit?.let { prod ->
    ProductEditorDialog(
      initialProduct = prod,
      customCategories = storeConfig.customCategories,
      onDismiss = { productToEdit = null },
      onSave = { updated ->
        viewModel.updateProduct(updated)
        productToEdit = null
      }
    )
  }

  if (showAddRiderDialog) {
    AddRiderDialog(
      onDismiss = { showAddRiderDialog = false },
      onSave = { name, phone, email, area, vehicle ->
        viewModel.addRider(name, phone, email, area, vehicle)
        showAddRiderDialog = false
      }
    )
  }

  // Dialog: Add Banner Ad
  if (showAddBannerDialog) {
    BannerEditorDialog(
      onDismiss = { showAddBannerDialog = false },
      onSave = { newBanner ->
        viewModel.addBanner(newBanner)
        showAddBannerDialog = false
      }
    )
  }
}

@Composable
fun ProductsManagementTab(
  products: List<Product>,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  onEditProduct: (Product) -> Unit,
  onToggleStock: (String) -> Unit,
  onDeleteProduct: (String) -> Unit,
  onAddNew: () -> Unit
) {
  var onlyOutOfStock by remember { mutableStateOf(false) }
  val filtered = remember(products, searchQuery, onlyOutOfStock) {
    products.filter {
      val stockOk = !onlyOutOfStock || !it.inStock
      val queryOk = searchQuery.isBlank() ||
          it.name.contains(searchQuery, ignoreCase = true) ||
          it.hindiName.contains(searchQuery, ignoreCase = true) ||
          it.category.name.contains(searchQuery, ignoreCase = true)
      stockOk && queryOk
    }
  }

  Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("उत्पाद खोजें (${products.size} · 10,000+ सपोर्ट)", fontSize = 12.sp) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = JioMartBlue) },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color.White,
          unfocusedContainerColor = Color.White
        ),
        modifier = Modifier.weight(1f).padding(end = 8.dp)
      )

      Button(
        onClick = onAddNew,
        colors = ButtonDefaults.buttonColors(containerColor = JioMartBlue),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("नया", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(6.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      androidx.compose.material3.FilterChip(
        selected = onlyOutOfStock,
        onClick = { onlyOutOfStock = !onlyOutOfStock },
        label = { Text("सिर्फ Stock Out दिखाएँ", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
      )
      Text(
        text = "दिख रहे हैं: ${filtered.size}",
        fontSize = 11.sp,
        color = Color(0xFF64748B)
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(filtered, key = { it.id }) { product ->
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val thumbUrl = product.imageUrl?.takeIf { it.isNotBlank() }
              ?: DefaultProducts.fallbackImageForCategory(product.category)
            val thumbModel = rememberCoilData(thumbUrl)
            AsyncImage(
              model = ImageRequest.Builder(LocalContext.current)
                .data(thumbModel)
                .crossfade(true)
                .build(),
              contentDescription = product.name,
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${product.hindiName} • ${product.unit}",
                fontSize = 11.sp,
                color = Color.Gray
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "रेट: ₹${product.ourPrice.toInt()}",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 13.sp,
                  color = JioMartBlueDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "MRP: ₹${product.mrp.toInt()}",
                  fontSize = 11.sp,
                  color = Color.Gray,
                  textDecoration = TextDecoration.LineThrough
                )
                if (!product.imageUrl.isNullOrBlank()) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(color = Color(0xFFE0F2FE), shape = RoundedCornerShape(4.dp)) {
                    Text("📷 Photo", fontSize = 8.sp, color = JioMartBlue, modifier = Modifier.padding(2.dp))
                  }
                }
                if (product.variants.isNotEmpty()) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Surface(color = Color(0xFFFCE7F3), shape = RoundedCornerShape(4.dp)) {
                    Text("${product.variants.size} साइज़", fontSize = 8.sp, color = Color(0xFFBE185D), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                  }
                }
              }
            }

            // Edit button
            IconButton(onClick = { onEditProduct(product) }) {
              Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = JioMartBlue)
            }

            // Delete button
            IconButton(onClick = { onDeleteProduct(product.id) }) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TertiaryRed)
            }
          }
        }
      }
    }
  }
}

@Composable
fun OffersAndBannersTab(
  storeConfig: StoreConfig,
  banners: List<BannerAd>,
  onToggleFestivalSale: (Boolean) -> Unit,
  onUpdateFestivalDetails: (String, String, String) -> Unit,
  onToggleBanner: (String) -> Unit,
  onDeleteBanner: (String) -> Unit,
  onAddBanner: () -> Unit
) {
  var festivalHead by remember { mutableStateOf(storeConfig.festivalSaleHeading) }
  var festivalSub by remember { mutableStateOf(storeConfig.festivalSaleSubtitle) }
  var festivalBadge by remember { mutableStateOf(storeConfig.festivalDiscountBadge) }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Festival Grand Sale Control Box
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "🪔 त्योहार महासेल (Festival Grand Sale) मोड",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF92400E)
              )
              Text(
                text = "चालू करने पर सभी ग्राहकों को ऊपर महासेल का विशेष बैनर और डिस्काउंट दिखेगा।",
                fontSize = 11.sp,
                color = Color(0xFF78350F)
              )
            }
            Switch(
              checked = storeConfig.isFestivalSaleActive,
              onCheckedChange = onToggleFestivalSale,
              colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFDC2626), checkedTrackColor = Color(0xFFFED7AA))
            )
          }

          if (storeConfig.isFestivalSaleActive) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = festivalHead,
              onValueChange = { festivalHead = it },
              label = { Text("त्योहार सेल हेडिंग") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
              value = festivalSub,
              onValueChange = { festivalSub = it },
              label = { Text("सबटाइटल विवरण") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
              value = festivalBadge,
              onValueChange = { festivalBadge = it },
              label = { Text("डिस्काउंट टैग (उदा: FLAT 25% OFF)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = { onUpdateFestivalDetails(festivalHead, festivalSub, festivalBadge) },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("त्योहार ऑफर विवरण सेव करें")
            }
          }
        }
      }
    }

    // 2. Banner Ads header & Add button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "प्रमोशनल सेल बैनर्स व विज्ञापन",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF1E293B)
          )
          Text(
            text = "होम स्क्रीन पर दिखने वाले विज्ञापन व ऑफर्स",
            fontSize = 11.sp,
            color = Color.Gray
          )
        }
        Button(
          onClick = onAddBanner,
          colors = ButtonDefaults.buttonColors(containerColor = JioMartBlue),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("नया बैनर", fontSize = 11.sp)
        }
      }
    }

    // Banners list
    items(banners, key = { it.id }) { banner ->
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                color = if (banner.isFestivalSpecial) SecondaryAmber else PrimaryGreen,
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = banner.badge,
                  fontSize = 9.sp,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = banner.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1E293B)
              )
            }
            Text(
              text = banner.subtitle,
              fontSize = 11.sp,
              color = Color(0xFF64748B)
            )
            Text(
              text = "ऑफर टैग: ${banner.discountTag}",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = TertiaryRed
            )
          }

          Switch(
            checked = banner.isActive,
            onCheckedChange = { onToggleBanner(banner.id) }
          )

          IconButton(onClick = { onDeleteBanner(banner.id) }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TertiaryRed)
          }
        }
      }
    }
  }
}

@Composable
fun DeliveryTimingAndScheduleTab(
  storeConfig: StoreConfig,
  onSave: (StoreConfig) -> Unit
) {
  var shopName by remember { mutableStateOf(storeConfig.shopName) }
  var tagline by remember { mutableStateOf(storeConfig.tagline) }
  var heroBannerUrl by remember { mutableStateOf(storeConfig.heroBannerUrl) }
  var showHeroBanner by remember { mutableStateOf(storeConfig.showHeroBanner) }
  var timing by remember { mutableStateOf(storeConfig.deliveryTiming) }
  var cutoff by remember { mutableStateOf(storeConfig.cutoffHourText) }
  var days by remember { mutableStateOf(storeConfig.deliveryScheduleDays) }
  var launchDays by remember { mutableStateOf(storeConfig.launchDaysRemaining.toString()) }
  var deliveryFee by remember { mutableStateOf(storeConfig.deliveryFee.toInt().toString()) }
  var threshold by remember { mutableStateOf(storeConfig.advanceThreshold.toInt().toString()) }
  var heavyFee by remember { mutableStateOf(storeConfig.heavyDeliveryFee.toInt().toString()) }

  val tabContext = LocalContext.current
  var pendingHeroCropUri by remember { mutableStateOf<Uri?>(null) }
  val heroPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) pendingHeroCropUri = uri
  }

  pendingHeroCropUri?.let { uri ->
    com.example.ui.components.ImageCropDialog(
      sourceUri = uri,
      defaultWideAspect = true,
      onDismiss = { pendingHeroCropUri = null },
      onCropped = { encoded ->
        heroBannerUrl = encoded
        pendingHeroCropUri = null
        Toast.makeText(tabContext, "पोस्टर क्रॉप होकर सेट हो गया! नीचे सेव दबाएँ।", Toast.LENGTH_SHORT).show()
      }
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "🏪 दुकान का नाम, मुख्य पोस्टर व टाइमिंग (Full Control)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = JioMartBlueDark
          )
          Text(
            text = "यहाँ से आप होम स्क्रीन का मुख्य 'Fresh Indian Grocery' पोस्टर, दुकान का नाम, टैगलाइन और डिलीवरी टाइमिंग सब बदल सकते हैं।",
            fontSize = 11.sp,
            color = Color.Gray
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = shopName,
            onValueChange = { shopName = it },
            label = { Text("दुकान का नाम (Shop Name)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = tagline,
            onValueChange = { tagline = it },
            label = { Text("टैगलाइन (Tagline)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "🖼️ मुख्य होम स्क्रीन पोस्टर दिखाएँ:",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = Color(0xFF1E293B)
            )
            Switch(checked = showHeroBanner, onCheckedChange = { showHeroBanner = it })
          }
          Spacer(modifier = Modifier.height(6.dp))
          if (heroBannerUrl.isNotBlank()) {
            val heroPrev = rememberCoilData(heroBannerUrl)
            AsyncImage(
              model = ImageRequest.Builder(tabContext).data(heroPrev).crossfade(true).build(),
              contentDescription = "Hero Poster Preview",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9))
            )
            Spacer(modifier = Modifier.height(6.dp))
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                heroPicker.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("गैलरी से पोस्टर चुनें (Crop)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            if (heroBannerUrl.isNotBlank()) {
              OutlinedButton(
                onClick = { heroBannerUrl = "" },
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Reset Poster", tint = TertiaryRed, modifier = Modifier.size(16.dp))
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = timing,
            onValueChange = { timing = it },
            label = { Text("डिलीवरी का समय") },
            placeholder = { Text("उदा: 4 घंटे, 12 घंटे, 24 घंटे या सुबह 8:00 AM") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "त्वरित समय चुनें (1-टैप):",
            fontSize = 11.sp,
            color = Color.Gray
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(
              "4 घंटे में" to "4 घंटे में डिलीवरी",
              "12 घंटे में" to "12 घंटे में डिलीवरी",
              "24 घंटे में" to "24 घंटे में डिलीवरी",
              "8 AM" to "सुबह ठीक 8:00 AM"
            ).forEach { (chipLabel, fullValue) ->
              val isSelected = timing == fullValue
              Surface(
                color = if (isSelected) Color(0xFF0284C7) else Color(0xFFE0F2FE),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .weight(1f)
                  .clickable { timing = fullValue }
              ) {
                Text(
                  text = chipLabel,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else Color(0xFF0369A1),
                  textAlign = TextAlign.Center,
                  modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = cutoff,
            onValueChange = { cutoff = it },
            label = { Text("ऑर्डर बुकिंग कटऑफ समय") },
            placeholder = { Text("उदा: रात 8:00 बजे तक") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = days,
            onValueChange = { days = it },
            label = { Text("डिलीवरी के दिन") },
            placeholder = { Text("उदा: प्रतिदिन (सोमवार से रविवार)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = launchDays,
            onValueChange = { launchDays = it },
            label = { Text("15 दिन बाद लॉन्च (शेष दिन संख्या, 0 = लाइव)") },
            placeholder = { Text("15") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "💰 डिलीवरी चार्ज व ₹500 अग्रिम नियम",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF1E293B)
          )

          Spacer(modifier = Modifier.height(6.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = deliveryFee,
              onValueChange = { deliveryFee = it },
              label = { Text("डिलीवरी शुल्क (₹)") },
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = threshold,
              onValueChange = { threshold = it },
              label = { Text("अग्रिम सीमा (₹500)") },
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = heavyFee,
            onValueChange = { heavyFee = it },
            label = { Text("भारी सामान अतिरिक्त चार्ज (₹20)") },
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              val lDays = launchDays.toIntOrNull() ?: 15
              val dFee = deliveryFee.toDoubleOrNull() ?: 60.0
              val th = threshold.toDoubleOrNull() ?: 500.0
              val hFee = heavyFee.toDoubleOrNull() ?: 20.0
              onSave(
                storeConfig.copy(
                  shopName = shopName.trim().ifBlank { storeConfig.shopName },
                  tagline = tagline.trim().ifBlank { storeConfig.tagline },
                  heroBannerUrl = heroBannerUrl.trim(),
                  showHeroBanner = showHeroBanner,
                  deliveryTiming = timing,
                  cutoffHourText = cutoff,
                  deliveryScheduleDays = days,
                  launchDaysRemaining = lDays,
                  launchDateText = if (lDays > 0) "$lDays दिन बाद $cutoff से बुकिंग" else "लाइव बुकिंग चालू है",
                  deliveryFee = dFee,
                  advanceThreshold = th,
                  heavyDeliveryFee = hFee
                )
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = JioMartBlue),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
          ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("सभी ग्राहकों के लिए तुरंत लागू करें", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun OrdersManagementTab(
  orders: List<Order>,
  riders: List<Rider>,
  onStatusChange: (String, OrderStatus) -> Unit,
  onAssignRider: (String, Rider) -> Unit,
  onUpdateCollection: (String, String, Boolean) -> Unit,
  onCall: (String) -> Unit,
  onWhatsApp: (String, String) -> Unit
) {
  if (orders.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
      Text("अभी कोई 8 AM ऑर्डर नहीं है", color = Color.Gray)
    }
    return
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items(orders, key = { it.id }) { order ->
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "ऑर्डर ID: ${order.id}",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = JioMartBlueDark
            )
            Surface(
              color = Color(order.status.badgeColorHex),
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = order.status.hindiLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "ग्राहक: ${order.customerName} (${order.customerPhone})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
          Text(text = "स्थान: ${order.area} • ${order.addressDetail}", fontSize = 11.sp, color = Color.Gray)
          val adminContext = LocalContext.current
          Spacer(modifier = Modifier.height(4.dp))
          Surface(
            color = Color(0xFFE0F2FE),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
              .clickable {
                openLocationInMaps(
                  context = adminContext,
                  area = order.area,
                  addressDetail = order.addressDetail,
                  lat = order.customerLat,
                  lng = order.customerLng
                )
              }
              .testTag("admin_view_map_${order.id}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Open Map",
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (order.customerLat != 0.0) "📍 मैप पर सटीक GPS लोकेशन देखें ➔" else "📍 मैप पर पता देखें ➔",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0369A1)
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "कुल राशि: ₹${order.grandTotal.toInt()} (अग्रिम: ₹${order.advancePaid.toInt()} | बाक़ी: ₹${order.balanceDueAtDelivery.toInt()})", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PrimaryGreen)
          if (order.rating > 0 || order.deliveryTip > 0) {
            Text(
              text = "⭐ रेटिंग: ${order.rating}/5 · टिप: ₹${order.deliveryTip.toInt()}${if (order.ratingReview.isNotBlank()) " (\"${order.ratingReview}\")" else ""}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFB45309)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { onCall(order.customerPhone) },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF1E293B)),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("कॉल", fontSize = 11.sp)
            }

            Button(
              onClick = {
                onWhatsApp(order.customerPhone, "नमस्ते ${order.customerName} जी, आपका अपना मार्केट ऑर्डर ${order.id} कल सुबह 8:00 AM पर डिलीवर होगा। धन्यवाद - मृत्युंजय विश्वास")
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("💬 WhatsApp", fontSize = 11.sp, color = Color.White)
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Status change row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("स्थिति बदलें:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              OrderStatus.values().take(4).forEach { st ->
                Surface(
                  color = if (order.status == st) Color(st.badgeColorHex) else Color(0xFFF1F5F9),
                  shape = RoundedCornerShape(4.dp),
                  modifier = Modifier.clickable { onStatusChange(order.id, st) }
                ) {
                  Text(
                    text = st.hindiLabel.take(6),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (order.status == st) Color.White else Color.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          HorizontalDivider(color = Color(0xFFF1F5F9))
          Spacer(modifier = Modifier.height(6.dp))

          // Rider Assignment Row
          if (order.assignedRiderName.isNotBlank()) {
            Surface(
              color = Color(0xFFE0F2FE),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "🛵 राइडर: ${order.assignedRiderName} (${order.assignedRiderPhone})",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0369A1),
                  modifier = Modifier.weight(1f)
                )
                Text(
                  text = "राइडर को भेजें ➔",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0284C7),
                  modifier = Modifier.clickable {
                    val itemsSummary = order.items.joinToString(", ") { "${it.product.name} x${it.quantity}" }
                    val mapLink = if (order.customerLat != 0.0 && order.customerLng != 0.0) {
                      "\n📍 लोकेशन मैप: https://maps.google.com/?q=${order.customerLat},${order.customerLng}"
                    } else {
                      "\n📍 लोकेशन मैप: https://maps.google.com/?q=${android.net.Uri.encode("${order.area} ${order.addressDetail}".trim())}"
                    }
                    onWhatsApp(
                      order.assignedRiderPhone,
                      "नमस्ते ${order.assignedRiderName}, नया डिलीवरी ऑर्डर ${order.id}:\nग्राहक: ${order.customerName} (${order.customerPhone})\nपता: ${order.area}, ${order.addressDetail}$mapLink\nसामान: $itemsSummary\nकुल बाक़ी राशि: ₹${order.balanceDueAtDelivery.toInt()}"
                    )
                  }
                )
              }
            }
          }

          val activeRiders = riders.filter { it.isActive }
          if (activeRiders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text("राइडर चुनें:", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
              activeRiders.take(3).forEach { r ->
                val isAssigned = order.assignedRiderId == r.id
                Surface(
                  color = if (isAssigned) Color(0xFF0284C7) else Color(0xFFF1F5F9),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.clickable { onAssignRider(order.id, r) }
                ) {
                  Text(
                    text = "🛵 ${r.name.take(10)}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAssigned) Color.White else JioMartBlueDark,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }

          // Cash / UPI Collection & Rider Settlement Row
          if (order.balanceDueAtDelivery > 0 || order.assignedRiderName.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text("कलेक्शन:", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)

              val isCash = order.collectedPaymentMode == "CASH"
              Surface(
                color = if (isCash) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.clickable {
                  onUpdateCollection(order.id, "CASH", order.isRiderCashSettled)
                }
              ) {
                Text(
                  text = "💵 कैश",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isCash) Color(0xFF92400E) else JioMartBlueDark,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }

              val isUpi = order.collectedPaymentMode == "UPI"
              Surface(
                color = if (isUpi) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.clickable {
                  onUpdateCollection(order.id, "UPI", true)
                }
              ) {
                Text(
                  text = "📲 Admin UPI",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isUpi) Color(0xFF166534) else JioMartBlueDark,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }

              if (isCash || order.isRiderCashSettled) {
                Surface(
                  color = if (order.isRiderCashSettled) PrimaryGreen else Color(0xFF0284C7),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.clickable {
                    onUpdateCollection(order.id, order.collectedPaymentMode.ifBlank { "CASH" }, !order.isRiderCashSettled)
                  }
                ) {
                  Text(
                    text = if (order.isRiderCashSettled) "✅ जमा हुआ" else "जमा करें",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun StoreSettingsTab(
  storeConfig: StoreConfig,
  onSave: (StoreConfig) -> Unit
) {
  var pin by remember { mutableStateOf(storeConfig.adminPin) }
  var shopName by remember { mutableStateOf(storeConfig.shopName) }
  var tagline by remember { mutableStateOf(storeConfig.tagline) }
  var founderName by remember { mutableStateOf(storeConfig.founderName) }
  var whatsapp by remember { mutableStateOf(storeConfig.whatsapp) }
  var email by remember { mutableStateOf(storeConfig.storeOwnerEmail) }
  var upiId by remember { mutableStateOf(storeConfig.upiId) }
  var announcement by remember { mutableStateOf(storeConfig.announcementText) }
  var welcomeMsg by remember { mutableStateOf(storeConfig.welcomeMessage) }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "🔐 एडमिन पिन व स्टोर सेटिंग्स",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = JioMartBlueDark
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("Admin PIN बदलें (वर्तमान: ****)") },
            placeholder = { Text("****") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = whatsapp,
            onValueChange = { whatsapp = it },
            label = { Text("WhatsApp नंबर (ऑर्डर प्राप्त करने के लिए)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("ईमेल पता (mrityunjayvishwas294@gmail.com)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = upiId,
            onValueChange = { upiId = it },
            label = { Text("Google Pay UPI ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = announcement,
            onValueChange = { announcement = it },
            label = { Text("अनाउंसमेंट टिकर संदेश") },
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = welcomeMsg,
            onValueChange = { welcomeMsg = it },
            label = { Text("ऐप खुलने पर 5-सेकंड स्वागत संदेश") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              val updated = storeConfig.copy(
                adminPin = pin.ifBlank { "5520" },
                whatsapp = whatsapp,
                storeOwnerEmail = email,
                upiId = upiId,
                announcementText = announcement,
                welcomeMessage = welcomeMsg.ifBlank { StoreConfig().welcomeMessage }
              )
              onSave(updated)
            },
            colors = ButtonDefaults.buttonColors(containerColor = JioMartBlue),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp)
          ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("सेटिंग्स सेव करें", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditorDialog(
  initialProduct: Product?,
  customCategories: List<CustomCategory> = emptyList(),
  onDismiss: () -> Unit,
  onSave: (Product) -> Unit
) {
  var name by remember { mutableStateOf(initialProduct?.name ?: "") }
  var hindiName by remember { mutableStateOf(initialProduct?.hindiName ?: "") }
  var category by remember { mutableStateOf(initialProduct?.category ?: Category.VEGETABLES) }
  var customCategoryId by remember { mutableStateOf(initialProduct?.customCategoryId ?: "") }
  var unit by remember { mutableStateOf(initialProduct?.unit ?: "1 kg") }
  var mrp by remember { mutableStateOf(initialProduct?.mrp?.toInt()?.toString() ?: "50") }
  var ourPrice by remember { mutableStateOf(initialProduct?.ourPrice?.toInt()?.toString() ?: "40") }
  var emoji by remember { mutableStateOf(initialProduct?.emoji ?: "🛒") }
  var imageUrl by remember { mutableStateOf(initialProduct?.imageUrl ?: "") }
  var tag by remember { mutableStateOf(initialProduct?.tag ?: "") }
  var inStock by remember { mutableStateOf(initialProduct?.inStock ?: true) }
  var variants by remember { mutableStateOf(initialProduct?.variants ?: emptyList()) }
  var newVarLabel by remember { mutableStateOf("") }
  var newVarMrp by remember { mutableStateOf("") }
  var newVarPrice by remember { mutableStateOf("") }

  var expandedCategory by remember { mutableStateOf(false) }
  val dialogContext = LocalContext.current
  var pendingProductCropUri by remember { mutableStateOf<Uri?>(null) }

  val galleryPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      pendingProductCropUri = uri
    }
  }

  pendingProductCropUri?.let { uri ->
    com.example.ui.components.ImageCropDialog(
      sourceUri = uri,
      defaultWideAspect = false,
      onDismiss = { pendingProductCropUri = null },
      onCropped = { encoded ->
        imageUrl = encoded
        pendingProductCropUri = null
        Toast.makeText(dialogContext, "फ़ोटो क्रॉप होकर सेट हो गई!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (initialProduct == null) "नया उत्पाद व फोटो जोड़ें" else "उत्पाद व फोटो एडिट करें",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
      )
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth().height(380.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Product Name (English)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          OutlinedTextField(
            value = hindiName,
            onValueChange = { hindiName = it },
            label = { Text("हिंदी नाम") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          val selectedCustomCat = customCategories.find { it.id == customCategoryId }
          val categoryDisplayText = selectedCustomCat?.let { "${it.hindiName} (${it.name})" } ?: category.hindiName
          ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = !expandedCategory }
          ) {
            OutlinedTextField(
              value = categoryDisplayText,
              onValueChange = {},
              readOnly = true,
              label = { Text("श्रेणी (Category)") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = expandedCategory,
              onDismissRequest = { expandedCategory = false }
            ) {
              Category.values().filter { it != Category.ALL }.forEach { cat ->
                DropdownMenuItem(
                  text = { Text("${cat.hindiName} (${cat.displayName})") },
                  onClick = {
                    category = cat
                    customCategoryId = ""
                    expandedCategory = false
                  }
                )
              }
              if (customCategories.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                customCategories.forEach { cCat ->
                  DropdownMenuItem(
                    text = { Text("✨ ${cCat.hindiName} (${cCat.name})") },
                    onClick = {
                      customCategoryId = cCat.id
                      expandedCategory = false
                    }
                  )
                }
              }
            }
          }
        }
        item {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = mrp,
              onValueChange = { mrp = it },
              label = { Text("आज का MRP (₹)") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = ourPrice,
              onValueChange = { ourPrice = it },
              label = { Text("हमारा रेट (₹)") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }
        }
        item {
          OutlinedTextField(
            value = unit,
            onValueChange = { unit = it },
            label = { Text("इकाई / वजन खुद लिखें (उदा: 700 gm, 500 gm, 1 kg, 2 Ltr)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          val previewUrl = imageUrl.takeIf { it.isNotBlank() }
            ?: DefaultProducts.fallbackImageForCategory(category)
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              val previewModel = rememberCoilData(previewUrl)
              AsyncImage(
                model = ImageRequest.Builder(dialogContext)
                  .data(previewModel)
                  .crossfade(true)
                  .build(),
                contentDescription = "Product Preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(72.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFFF1F5F9))
              )
              Button(
                onClick = {
                  galleryPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("गैलरी से फ़ोटो / GIF चुनें", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            OutlinedTextField(
              value = imageUrl,
              onValueChange = { imageUrl = it },
              label = { Text("या फ़ोटो / GIF लिंक (URL) पेस्ट करें") },
              placeholder = { Text("https://... (.jpg, .png, .gif)") },
              singleLine = true,
              trailingIcon = {
                if (imageUrl.isNotBlank()) {
                  IconButton(onClick = { imageUrl = "" }) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "फ़ोटो हटाएं",
                      tint = TertiaryRed,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
        // Preset photo links for instant setup
        item {
          Text("त्वरित फ़ोटो प्रीसेट (1-टैप में फोटो सेट करें):", fontSize = 11.sp, color = Color.Gray)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            val presets = listOf(
              "दूध" to "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400",
              "आलू" to "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=400",
              "टमाटर" to "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=400",
              "घी" to "https://images.unsplash.com/photo-1628088062854-d1870b4553da?w=400"
            )
            presets.forEach { (lbl, url) ->
              Surface(
                color = Color(0xFFE0F2FE),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.clickable { imageUrl = url }
              ) {
                Text(lbl, fontSize = 10.sp, color = JioMartBlueDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
              }
            }
          }
        }
        item {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = emoji,
              onValueChange = { emoji = it },
              label = { Text("इमोजी (Emoji)") },
              singleLine = true,
              modifier = Modifier.weight(0.8f)
            )
            OutlinedTextField(
              value = tag,
              onValueChange = { tag = it },
              label = { Text("टैग (उदा: Mandi Fresh)") },
              singleLine = true,
              modifier = Modifier.weight(1.2f)
            )
          }
        }
        item {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "⚖️ वज़न / साइज़ विकल्प (500 gm, 700 gm, 1 kg, ml):",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = JioMartBlueDark
            )
            // Quick preset buttons for Admin
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              val baseMrp = mrp.toDoubleOrNull() ?: 50.0
              val basePrice = ourPrice.toDoubleOrNull() ?: 40.0
              val quickPresets = listOf(
                Triple("500 gm", (baseMrp * 0.55).toInt(), (basePrice * 0.55).toInt()),
                Triple("700 gm", (baseMrp * 0.75).toInt(), (basePrice * 0.75).toInt()),
                Triple("1 kg", baseMrp.toInt(), basePrice.toInt()),
                Triple("500 ml", (baseMrp * 0.55).toInt(), (basePrice * 0.55).toInt()),
                Triple("700 ml", (baseMrp * 0.75).toInt(), (basePrice * 0.75).toInt()),
                Triple("1 L", baseMrp.toInt(), basePrice.toInt())
              )
              quickPresets.forEach { (lbl, m, p) ->
                Surface(
                  color = Color(0xFFE0F2FE),
                  shape = RoundedCornerShape(4.dp),
                  modifier = Modifier
                    .weight(1f)
                    .clickable {
                      if (variants.none { it.label.equals(lbl, ignoreCase = true) }) {
                        variants = variants + ProductVariant(lbl, m.toDouble(), p.toDouble())
                      }
                    }
                ) {
                  Text(
                    text = "+$lbl",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7),
                    modifier = Modifier.padding(vertical = 4.dp),
                    maxLines = 1
                  )
                }
              }
            }

            variants.forEachIndexed { idx, v ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(v.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(56.dp))
                OutlinedTextField(
                  value = v.mrp.toInt().toString(),
                  onValueChange = { txt ->
                    val updatedM = txt.toDoubleOrNull() ?: v.mrp
                    variants = variants.toMutableList().also { it[idx] = v.copy(mrp = updatedM) }
                  },
                  label = { Text("MRP", fontSize = 9.sp) },
                  singleLine = true,
                  modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                  value = v.ourPrice.toInt().toString(),
                  onValueChange = { txt ->
                    val updatedP = txt.toDoubleOrNull() ?: v.ourPrice
                    variants = variants.toMutableList().also { it[idx] = v.copy(ourPrice = updatedP) }
                  },
                  label = { Text("रेट", fontSize = 9.sp) },
                  singleLine = true,
                  modifier = Modifier.weight(1f)
                )
                IconButton(
                  onClick = { variants = variants.filterIndexed { i, _ -> i != idx } },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = TertiaryRed, modifier = Modifier.size(16.dp))
                }
              }
            }

            // Custom variant add row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = newVarLabel,
                onValueChange = { newVarLabel = it },
                label = { Text("साइज़ (700 gm)", fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.weight(1.1f)
              )
              OutlinedTextField(
                value = newVarMrp,
                onValueChange = { newVarMrp = it },
                label = { Text("MRP", fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.weight(0.8f)
              )
              OutlinedTextField(
                value = newVarPrice,
                onValueChange = { newVarPrice = it },
                label = { Text("रेट", fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.weight(0.8f)
              )
              IconButton(
                onClick = {
                  val lbl = newVarLabel.trim()
                  val p = newVarPrice.toDoubleOrNull()
                  val m = newVarMrp.toDoubleOrNull() ?: p
                  if (lbl.isNotBlank() && p != null && m != null) {
                    variants = variants + ProductVariant(lbl, m, p)
                    newVarLabel = ""
                    newVarMrp = ""
                    newVarPrice = ""
                  }
                }
              ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Variant", tint = JioMartBlue)
              }
            }
          }
        }
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("स्टॉक में उपलब्ध है (In Stock):", fontSize = 13.sp)
            Switch(checked = inStock, onCheckedChange = { inStock = it })
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank()) return@Button
          val p = Product(
            id = initialProduct?.id ?: "custom_prod_${System.currentTimeMillis()}",
            name = name.trim(),
            hindiName = hindiName.trim().ifBlank { name.trim() },
            category = category,
            unit = unit.trim().ifBlank { "1 unit" },
            mrp = mrp.toDoubleOrNull() ?: 50.0,
            ourPrice = ourPrice.toDoubleOrNull() ?: 40.0,
            inStock = inStock,
            tag = tag.trim().takeIf { it.isNotBlank() },
            emoji = emoji.trim().ifBlank { "🛒" },
            imageUrl = imageUrl.trim().takeIf { it.isNotBlank() },
            description = initialProduct?.description ?: "ताज़ा एवं शुद्ध सामान - रायगढ़ से पुंजीपथरा",
            isHeavy = initialProduct?.isHeavy ?: false,
            variants = variants,
            customCategoryId = customCategoryId
          )
          onSave(p)
        },
        colors = ButtonDefaults.buttonColors(containerColor = JioMartBlue)
      ) {
        Text("सेव करें")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("रद्द करें")
      }
    }
  )
}

@Composable
fun BannerEditorDialog(
  onDismiss: () -> Unit,
  onSave: (BannerAd) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var subtitle by remember { mutableStateOf("") }
  var badge by remember { mutableStateOf("महाबचत") }
  var discountTag by remember { mutableStateOf("20% OFF") }
  var mediaUrl by remember { mutableStateOf("") }
  var isFestival by remember { mutableStateOf(false) }
  val dialogContext = LocalContext.current
  var pendingBannerCropUri by remember { mutableStateOf<Uri?>(null) }

  val bannerMediaPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      pendingBannerCropUri = uri
    }
  }

  pendingBannerCropUri?.let { uri ->
    com.example.ui.components.ImageCropDialog(
      sourceUri = uri,
      defaultWideAspect = true,
      onDismiss = { pendingBannerCropUri = null },
      onCropped = { encoded ->
        mediaUrl = encoded
        pendingBannerCropUri = null
        Toast.makeText(dialogContext, "बैनर क्रॉप होकर सेट हो गया!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("नया सेल व विज्ञापन बैनर जोड़ें", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("बैनर मुख्य शीर्षक") },
          placeholder = { Text("उदा: 🪔 छठ पूजा विशेष ऑफर") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = subtitle,
          onValueChange = { subtitle = it },
          label = { Text("उपशीर्षक / विवरण") },
          placeholder = { Text("उदा: फल, घी व खाजा पर 30% तक छूट") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = badge,
            onValueChange = { badge = it },
            label = { Text("बैज (उदा: सेल)") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = discountTag,
            onValueChange = { discountTag = it },
            label = { Text("डिस्काउंट टैग") },
            modifier = Modifier.weight(1f)
          )
        }
        Button(
          onClick = {
            bannerMediaPicker.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("गैलरी से GIF / फ़ोटो चुनें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        if (mediaUrl.isNotBlank()) {
          val bannerPreviewModel = rememberCoilData(mediaUrl)
          AsyncImage(
            model = ImageRequest.Builder(dialogContext)
              .data(bannerPreviewModel)
              .crossfade(true)
              .build(),
            contentDescription = "Banner Preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxWidth()
              .height(90.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFF1F5F9))
          )
        }
        OutlinedTextField(
          value = mediaUrl,
          onValueChange = { mediaUrl = it },
          label = { Text("बैनर GIF / इमेज लिंक (वैकल्पिक)") },
          placeholder = { Text("https://... (.gif, .jpg, .png)") },
          singleLine = true,
          trailingIcon = {
            if (mediaUrl.isNotBlank()) {
              IconButton(onClick = { mediaUrl = "" }) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "फ़ोटो हटाएं",
                  tint = TertiaryRed,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          },
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("त्योहार विशेष (Festival Special):", fontSize = 12.sp)
          Switch(checked = isFestival, onCheckedChange = { isFestival = it })
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isBlank()) return@Button
          val b = BannerAd(
            id = "banner_${System.currentTimeMillis()}",
            title = title.trim(),
            subtitle = subtitle.trim(),
            badge = badge.trim().ifBlank { "OFFER" },
            discountTag = discountTag.trim().ifBlank { "SPECIAL" },
            isFestivalSpecial = isFestival,
            isActive = true,
            mediaUrl = mediaUrl.trim().takeIf { it.isNotBlank() }
          )
          onSave(b)
        },
        colors = ButtonDefaults.buttonColors(containerColor = JioMartBlue)
      ) {
        Text("बैनर जोड़ें")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("रद्द करें")
      }
    }
  )
}

private fun savePickedImageToInternalStorage(context: Context, uri: Uri): String? {
  return try {
    val mimeType = context.contentResolver.getType(uri)?.lowercase() ?: ""
    val dir = File(context.filesDir, "product_images").apply { mkdirs() }

    // Preserve animated GIFs without bitmap re-encoding; sync small GIFs via Firestore Data URI
    if (mimeType.contains("gif") || uri.toString().lowercase().endsWith(".gif")) {
      val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
      if (bytes.size <= 320_000) {
        return "data:image/gif;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
      }
      val gifFile = File(dir, "anim_${System.currentTimeMillis()}.gif")
      FileOutputStream(gifFile).use { it.write(bytes) }
      return "file://${gifFile.absolutePath}"
    }

    // Two-pass OOM-safe decode with inSampleSize
    val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, boundsOpts) }
    val maxDim = 480
    var sampleSize = 1
    while (boundsOpts.outWidth / sampleSize > maxDim * 2 || boundsOpts.outHeight / sampleSize > maxDim * 2) {
      sampleSize *= 2
    }

    val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val original = context.contentResolver.openInputStream(uri)?.use {
      BitmapFactory.decodeStream(it, null, decodeOpts)
    } ?: return null

    val ratio = minOf(maxDim.toFloat() / original.width, maxDim.toFloat() / original.height, 1f)
    val targetW = (original.width * ratio).toInt().coerceAtLeast(1)
    val targetH = (original.height * ratio).toInt().coerceAtLeast(1)
    val resized = if (ratio < 1f) Bitmap.createScaledBitmap(original, targetW, targetH, true) else original

    val baos = ByteArrayOutputStream()
    resized.compress(Bitmap.CompressFormat.JPEG, 75, baos)
    val compressedBytes = baos.toByteArray()

    if (resized != original) original.recycle()
    resized.recycle()

    if (compressedBytes.size <= 320_000) {
      "data:image/jpeg;base64," + Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
    } else {
      val outFile = File(dir, "prod_${System.currentTimeMillis()}.jpg")
      FileOutputStream(outFile).use { it.write(compressedBytes) }
      "file://${outFile.absolutePath}"
    }
  } catch (_: Exception) {
    null
  }
}

@Composable
fun RidersManagementTab(
  riders: List<Rider>,
  orders: List<Order>,
  onAddRider: () -> Unit,
  onToggleActive: (String) -> Unit,
  onDeleteRider: (String) -> Unit,
  onCall: (String) -> Unit,
  onWhatsApp: (String, String) -> Unit
) {
  Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
    Button(
      onClick = onAddRider,
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("hire_new_rider_button")
    ) {
      Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
      Spacer(modifier = Modifier.width(6.dp))
      Text("नया डिलीवरी राइडर हायर करें (Hire Rider)", fontWeight = FontWeight.Bold, color = Color.White)
    }

    Spacer(modifier = Modifier.height(10.dp))

    if (riders.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("🛵", fontSize = 42.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Text("अभी कोई राइडर नहीं जुड़ा है", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = JioMartBlueDark)
          Text(
            "ऊपर बटन दबाकर अपने डिलीवरी राइडर का नाम और मोबाइल नंबर जोड़ें।",
            fontSize = 12.sp,
            color = Color.Gray
          )
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(riders, key = { it.id }) { rider ->
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "🛵 ${rider.name} (${rider.vehicleType})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = JioMartBlueDark
                  )
                  Text(
                    text = "📞 ${rider.phone} · क्षेत्र: ${rider.area}",
                    fontSize = 12.sp,
                    color = Color.Gray
                  )
                  if (rider.email.isNotBlank()) {
                    Text(text = "✉️ ${rider.email}", fontSize = 11.sp, color = Color(0xFF0284C7))
                  }
                }
                Switch(
                  checked = rider.isActive,
                  onCheckedChange = { onToggleActive(rider.id) },
                  colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryGreen)
                )
              }

              val riderOrders = orders.filter { it.assignedRiderId == rider.id }
              val pendingCash = riderOrders
                .filter { it.collectedPaymentMode == "CASH" && !it.isRiderCashSettled }
                .sumOf { it.balanceDueAtDelivery }
              val settledTotal = riderOrders
                .filter { it.isRiderCashSettled }
                .sumOf { it.balanceDueAtDelivery }

              Spacer(modifier = Modifier.height(6.dp))
              Surface(
                color = if (pendingCash > 0) Color(0xFFFEF3C7) else Color(0xFFE0F2FE),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = "📦 ऑर्डर: ${riderOrders.size} · 💰 बकाया कैश: ₹${pendingCash.toInt()} · ✅ जमा: ₹${settledTotal.toInt()}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (pendingCash > 0) Color(0xFF92400E) else Color(0xFF0369A1),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = { onCall(rider.phone) },
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("कॉल", fontSize = 11.sp)
                }

                Button(
                  onClick = {
                    onWhatsApp(
                      rider.phone,
                      "नमस्ते ${rider.name}, आपको अपना मार्केट (रायगढ़ ➔ पुंजीपथरा) में डिलीवरी राइडर के रूप में जोड़ा गया है। सुबह 8:00 AM डिलीवरी के लिए तैयार रहें!"
                    )
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Text("💬 WhatsApp", fontSize = 11.sp, color = Color.White)
                }

                IconButton(onClick = { onDeleteRider(rider.id) }) {
                  Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Rider", tint = Color(0xFFDC2626))
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun AddRiderDialog(
  onDismiss: () -> Unit,
  onSave: (name: String, phone: String, email: String, area: String, vehicleType: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var area by remember { mutableStateOf("रायगढ़ ➔ पुंजीपथरा") }
  var vehicleType by remember { mutableStateOf("Bike") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("नया डिलीवरी राइडर जोड़ें", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("राइडर का पूरा नाम *") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("rider_name_input")
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("मोबाइल / WhatsApp नंबर *") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("rider_phone_input")
        )
        OutlinedTextField(
          value = area,
          onValueChange = { area = it },
          label = { Text("डिलीवरी क्षेत्र (Area)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = vehicleType,
          onValueChange = { vehicleType = it },
          label = { Text("वाहन (Bike / Scooty / Auto)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("ईमेल (वैकल्पिक)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && phone.isNotBlank()) {
            onSave(name, phone, email, area, vehicleType)
          }
        },
        enabled = name.isNotBlank() && phone.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
        modifier = Modifier.testTag("save_rider_button")
      ) {
        Text("राइडर सेव करें", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("रद्द करें")
      }
    }
  )
}
