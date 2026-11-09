package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.R
import com.example.model.Category
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.components.ActiveBannersList
import com.example.ui.components.AreaPickerBottomSheet
import com.example.ui.components.CategoryStoryStrip
import com.example.ui.components.FestivalSaleOfferBanner
import com.example.ui.components.FounderTrustCard
import com.example.ui.components.MorningDeliveryBanner
import com.example.ui.components.ProductCardItem
import com.example.ui.components.SpecialRequestBanner
import com.example.ui.components.StickyCartBottomBar
import com.example.ui.components.TopCorridorHeader
import com.example.ui.theme.JioMartBlue
import com.example.ui.theme.JioMartBlueDark
import com.example.ui.theme.MyntraDark
import com.example.ui.theme.MyntraPink
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight

@Composable
fun HomeScreen(
  viewModel: StoreViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val storeConfig by viewModel.storeConfig.collectAsState()
  val selectedArea by viewModel.selectedArea.collectAsState()
  val cart by viewModel.cart.collectAsState()
  val cartCount by viewModel.cartItemCount.collectAsState()
  val cartTotal by viewModel.cartTotalPrice.collectAsState()
  val cartSavings by viewModel.cartTotalSavings.collectAsState()
  val filteredProducts by viewModel.filteredProducts.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedCategory by viewModel.selectedCategory.collectAsState()
  val banners by viewModel.banners.collectAsState()

  var showAreaPicker by remember { mutableStateOf(false) }
  var showAdminPinDialog by remember { mutableStateOf(false) }
  var adminPinInput by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      TopCorridorHeader(
        storeConfig = storeConfig,
        selectedArea = selectedArea,
        cartItemCount = cartCount,
        onOpenAreaPicker = { showAreaPicker = true },
        onOpenWhatsApp = {
          viewModel.openWhatsApp(
            context = context,
            message = "नमस्ते मृत्युंजय जी, मैं अपना मार्केट (रायगढ़-पुंजीपथरा) से सामान ऑर्डर करना चाहता हूँ।"
          )
        },
        onOpenEmail = {
          viewModel.openEmailComposer(
            context,
            subject = "अपना मार्केट रायगढ़-पुंजीपथरा पूछताछ",
            body = "नमस्ते मृत्युंजय जी,\n\nमैं अपना मार्केट (रायगढ़ से पुंजीपथरा) से सुबह 8:00 AM डिलीवरी के संबंध में संपर्क कर रहा हूँ।\n"
          )
        },
        onOpenOrders = { viewModel.navigateTo(Screen.ORDERS) },
        onSecretAdminTrigger = {
          adminPinInput = ""
          showAdminPinDialog = true
        },
        onOpenCart = { viewModel.navigateTo(Screen.CART) }
      )
    },
    bottomBar = {
      StickyCartBottomBar(
        cartCount = cartCount,
        totalPrice = cartTotal,
        deliveryFee = storeConfig.deliveryFee,
        totalSavings = cartSavings,
        onOpenCart = { viewModel.navigateTo(Screen.CART) }
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
      // Adaptive columns based on device orientation and screen width
      val columnsCount = when {
        maxWidth < 600.dp -> 2
        maxWidth in 600.dp..960.dp -> 3
        else -> 4
      }
      val heroHeight = if (maxWidth < 600.dp) 160.dp else 220.dp

      Box(
        modifier = Modifier
          .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
      ) {
        LazyVerticalGrid(
          columns = GridCells.Fixed(columnsCount),
          contentPadding = PaddingValues(bottom = 100.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 1200.dp)
            .padding(horizontal = 14.dp)
        ) {
          // 1. Hero Graphic Banner
          item(span = { GridItemSpan(columnsCount) }) {
            Card(
              shape = RoundedCornerShape(16.dp),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(heroHeight)
              ) {
                Image(
                  painter = painterResource(id = R.drawable.hero_banner),
                  contentDescription = "अपना मार्केट - रायगढ़ से पुंजीपथरा",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              }
            }
          }

          // 2. Founder's Trust Card (MV Badge) with Secret Admin trigger on long press
          item(span = { GridItemSpan(columnsCount) }) {
            FounderTrustCard(
              storeConfig = storeConfig,
              onWhatsAppClick = {
                viewModel.openWhatsApp(
                  context = context,
                  message = "नमस्ते मृत्युंजय जी, मैं अपना मार्केट ऐप देखकर WhatsApp पर संपर्क कर रहा हूँ।"
                )
              },
              onCallClick = { viewModel.callPhone(context, storeConfig.whatsapp) },
              onSecretAdminTrigger = {
                adminPinInput = ""
                showAdminPinDialog = true
              }
            )
          }

          // 3. Festival Sale Offer Banner (Controllable by Admin)
          item(span = { GridItemSpan(columnsCount) }) {
            FestivalSaleOfferBanner(storeConfig = storeConfig)
          }

          // 4. Active Promotional Banners List (Controllable by Admin)
          item(span = { GridItemSpan(columnsCount) }) {
            ActiveBannersList(banners = banners)
          }

          // 5. Morning 8 AM Delivery Promise & Schedule
          item(span = { GridItemSpan(columnsCount) }) {
            MorningDeliveryBanner(storeConfig = storeConfig)
          }

          // 6. Special Custom Request Banner ("कुछ और मँगाना है?")
          item(span = { GridItemSpan(columnsCount) }) {
            SpecialRequestBanner(
              onRequestClick = { viewModel.navigateTo(Screen.CUSTOM_REQUEST) }
            )
          }

          // 7. Search Bar (JioMart Style Clean Search)
          item(span = { GridItemSpan(columnsCount) }) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { viewModel.searchQuery.value = it },
              placeholder = {
                Text(
                  text = "सर्च करें 2000+ सामान (काजल, किताबें, पेन, आलू, दूध, तेल...)",
                  fontSize = 13.sp,
                  color = Color(0xFF64748B)
                )
              },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "Search",
                  tint = MyntraPink
                )
              },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                  }
                }
              },
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = MyntraPink,
                unfocusedBorderColor = Color(0xFFEAEAEC)
              ),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("search_bar_input")
            )
          }

          // 8. Myntra-Style Circular Category Story Strip
          item(span = { GridItemSpan(columnsCount) }) {
            CategoryStoryStrip(
              selectedCategory = selectedCategory,
              onSelectCategory = { viewModel.selectedCategory.value = it }
            )
          }

          // 9. Swipeable Trending & Best Deals Carousel (when viewing ALL & no search query)
          if (selectedCategory == Category.ALL && searchQuery.isBlank() && filteredProducts.size >= 6) {
            item(span = { GridItemSpan(columnsCount) }) {
              val trending = remember(filteredProducts) {
                filteredProducts.sortedByDescending { it.discountPercent }.take(10)
              }
              Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "🔥 ट्रेंडिंग व महाबचत डील्स (स्वाइप करें ➔)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MyntraDark
                  )
                }
                LazyRow(
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                  contentPadding = PaddingValues(end = 8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  items(trending, key = { "trend_${it.id}" }) { product ->
                    ProductCardItem(
                      product = product,
                      cart = cart,
                      isFestivalSaleActive = storeConfig.isFestivalSaleActive,
                      onAdd = { variant -> viewModel.addToCart(product, variant) },
                      onRemove = { cartKey -> viewModel.removeFromCart(cartKey) },
                      modifier = Modifier.width(164.dp)
                    )
                  }
                }
              }
            }
          }

          // Section Header
          item(span = { GridItemSpan(columnsCount) }) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "${selectedCategory.hindiName} (${filteredProducts.size} सामान उपलब्ध)",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0F172A)
                )
                Text(
                  text = "आज का सही मंडी रेट · डिलीवरी: ${storeConfig.deliveryTiming} (${storeConfig.deliveryScheduleDays})",
                  fontSize = 11.sp,
                  color = PrimaryGreen,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          // Empty state if search returned 0 items
          if (filteredProducts.isEmpty()) {
            item(span = { GridItemSpan(columnsCount) }) {
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 24.dp)
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(text = "🔍", fontSize = 42.sp)
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "\"$searchQuery\" लिस्ट में नहीं मिला",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E293B)
                  )
                  Text(
                    text = "कोई खास सामान चाहिए? स्पेशल रिक्वेस्ट सबमिट करें, मृत्युंजय सुबह रायगढ़ से लाएंगे!",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                  )
                  Surface(
                    color = JioMartBlue,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.CUSTOM_REQUEST) }
                  ) {
                    Text(
                      text = "रायगढ़ से स्पेशल सामान मँगवाएँ ➔",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                  }
                }
              }
            }
          } else {
            // Products
            items(filteredProducts, key = { it.id }) { product ->
              ProductCardItem(
                product = product,
                cart = cart,
                isFestivalSaleActive = storeConfig.isFestivalSaleActive,
                onAdd = { variant -> viewModel.addToCart(product, variant) },
                onRemove = { cartKey -> viewModel.removeFromCart(cartKey) }
              )
            }
          }

          // Footer info with secret admin trigger on long press
          item(span = { GridItemSpan(columnsCount) }) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "अपना मार्केट · रायगढ़ ➔ पुंजीपथरा",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
              )
              Text(
                text = "संस्थापक: ${storeConfig.founderName} • 100% ओरिजिनल व सुरक्षित",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier
                  .clickable {
                    // Discreet double check: tapping opens PIN dialog
                    adminPinInput = ""
                    showAdminPinDialog = true
                  }
                  .padding(4.dp)
              )
            }
          }
        }
      }
    }
  }

  if (showAreaPicker) {
    AreaPickerBottomSheet(
      currentArea = selectedArea,
      onSelectArea = { viewModel.selectArea(it) },
      onDismiss = { showAreaPicker = false }
    )
  }

  // Admin PIN Dialog (Secret Owner Authentication - Strictly Masked ****)
  if (showAdminPinDialog) {
    AlertDialog(
      onDismissRequest = { showAdminPinDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("🔐 एडमिन लॉगिन (Admin Only)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column {
          Text(
            text = "सभी ग्राहकों के ऑर्डर, उत्पाद, फोटो, रेट व सेल ऑफर केवल एडमिन ही देख और बदल सकते हैं।",
            fontSize = 12.sp,
            color = Color.Gray
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = adminPinInput,
            onValueChange = { adminPinInput = it },
            label = { Text("Admin PIN दर्ज करें") },
            placeholder = { Text("****") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (viewModel.verifyAdminPin(adminPinInput)) {
              showAdminPinDialog = false
              viewModel.navigateTo(Screen.ADMIN_HUB)
            } else {
              Toast.makeText(context, "गलत पिन! केवल एडमिन ही एक्सेस कर सकते हैं।", Toast.LENGTH_LONG).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MyntraPink),
          modifier = Modifier.testTag("admin_pin_submit_button")
        ) {
          Text("अनलॉक करें")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAdminPinDialog = false }) {
          Text("रद्द करें")
        }
      }
    )
  }
}
