package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Order
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.theme.JioMartBlue
import com.example.ui.theme.JioMartBlueDark
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenDark
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.SecondaryGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
  viewModel: StoreViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val allOrders by viewModel.orders.collectAsState()
  val myOrders = remember(allOrders) { allOrders.filter { it.isMyOrder } }
  val storeConfig by viewModel.storeConfig.collectAsState()

  var showQrDialog by remember { mutableStateOf(false) }
  var orderToRate by remember { mutableStateOf<Order?>(null) }

  BackHandler {
    viewModel.navigateTo(Screen.HOME)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "मेरे ऑर्डर्स (${myOrders.size})",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Text(
              text = "रायगढ़ ➔ पुंजीपथरा (WhatsApp: ${storeConfig.whatsapp})",
              fontSize = 11.sp,
              color = Color.White.copy(alpha = 0.85f)
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = { viewModel.navigateTo(Screen.HOME) },
            modifier = Modifier.testTag("orders_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }
        },
        actions = {
          IconButton(onClick = { showQrDialog = true }) {
            Icon(imageVector = Icons.Default.QrCode, contentDescription = "UPI QR", tint = Color.White)
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(Color(0xFFFAFAFC))
    ) {
      // Customer Order Privacy Notice
      Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF282C3F)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(SecondaryGold, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "🔒",
              fontSize = 16.sp
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "आपके निजी ऑर्डर्स (100% गोपनीय)",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = Color(0xFFFCD34D)
            )
            Text(
              text = "यहाँ केवल आपके द्वारा किए गए ऑर्डर्स ही दिखते हैं। अन्य ग्राहकों के ऑर्डर पूरी तरह गुप्त रखे जाते हैं।",
              fontSize = 11.sp,
              color = Color.White
            )
          }
        }
      }

      if (myOrders.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📦", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("आपने अभी तक कोई ऑर्डर नहीं किया है", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
              "रात 8:00 बजे से पहले ऑर्डर करें और अगली सुबह ठीक 8:00 बजे घर पर पाएँ!",
              color = Color.Gray,
              fontSize = 12.sp,
              modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            Button(
              onClick = { viewModel.navigateTo(Screen.HOME) },
              colors = ButtonDefaults.buttonColors(containerColor = JioMartBlue)
            ) {
              Text("सामान खरीदें (Shop Now)")
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          items(myOrders, key = { it.id }) { order ->
            OrderItemCard(
              order = order,
              storeWhatsapp = storeConfig.whatsapp,
              storeEmail = storeConfig.storeOwnerEmail,
              onWhatsAppOwner = {
                val orderMsg = buildString {
                  append("नमस्ते मृत्युंजय जी, मैं *अपना मार्केट* आर्डर #${order.id} के बारे में संपर्क कर रहा हूँ:\n\n")
                  append("*ग्राहक:* ${order.customerName}\n")
                  append("*मोबाइल:* ${order.customerPhone}\n")
                  append("*स्थान:* ${order.area} (${order.addressDetail})\n")
                  append("*कुल बिल:* ₹${order.grandTotal.toInt()}\n")
                  if (order.advancePaid > 0) {
                    append("*अग्रिम डिलीवरी शुल्क जमा:* ₹${order.advancePaid.toInt()} (UPI)\n")
                    append("*सुबह 8 AM देय शेष:* ₹${order.balanceDueAtDelivery.toInt()}\n")
                  }
                  if (order.upiTransactionRef.isNotBlank()) {
                    append("*UPI UTR:* ${order.upiTransactionRef}\n")
                  }
                  append("*डिलीवरी समय:* कल सुबह ठीक 8:00 AM\n")
                }
                viewModel.openWhatsApp(context, storeConfig.whatsapp, orderMsg)
              },
              onEmailOwner = {
                val emailBody = buildString {
                  append("नमस्ते मृत्युंजय जी,\n\n")
                  append("मेरा अपना मार्केट ऑर्डर विवरण:\n")
                  append("Order ID: ${order.id}\n")
                  append("Customer: ${order.customerName} (${order.customerPhone})\n")
                  append("Area: ${order.area} - ${order.addressDetail}\n")
                  append("Grand Total: Rs.${order.grandTotal}\n")
                  append("Advance Paid: Rs.${order.advancePaid}\n")
                  append("Balance At Delivery: Rs.${order.balanceDueAtDelivery}\n")
                  append("Delivery Time: Tomorrow Morning 8:00 AM Sharp\n\n")
                  append("कृपया पुष्टि करें।")
                }
                viewModel.openEmailComposer(
                  context,
                  subject = "अपना मार्केट ऑर्डर #${order.id} - ${order.customerName}",
                  body = emailBody
                )
              },
              onCallOwner = {
                viewModel.callPhone(context, storeConfig.whatsapp)
              },
              onViewQr = {
                showQrDialog = true
              },
              onRateAndTip = {
                orderToRate = order
              }
            )
          }

          item {
            Spacer(modifier = Modifier.height(24.dp))
          }
        }
      }
    }
  }

  // QR Dialog
  if (showQrDialog) {
    AlertDialog(
      onDismissRequest = { showQrDialog = false },
      title = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
          Text("मृत्युंजय विश्वास (Google Pay QR)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Text("UPI ID: ${storeConfig.upiId}", fontSize = 12.sp, color = Color(0xFF1D4ED8))
        }
      },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
          Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.upi_qr_mrityunjay),
              contentDescription = "Mrityunjay Vishwas Google Pay QR Code",
              contentScale = ContentScale.Fit,
              modifier = Modifier.size(260.dp)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Scan to pay with any UPI app · 8982903843",
            fontSize = 12.sp,
            color = Color.Gray
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.openUpiPayment(context, 60.0, "DELIVERY", "Delivery Advance")
            showQrDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
          Text("UPI ऐप खोलें (Pay ₹60)")
        }
      },
      dismissButton = {
        TextButton(onClick = { showQrDialog = false }) {
          Text("बंद करें")
        }
      }
    )
  }

  // Rate & Delivery Tip Dialog
  orderToRate?.let { targetOrder ->
    var selectedStars by remember(targetOrder.id) {
      mutableIntStateOf(if (targetOrder.rating > 0) targetOrder.rating else 5)
    }
    var selectedTip by remember(targetOrder.id) {
      mutableDoubleStateOf(targetOrder.deliveryTip)
    }
    var reviewText by remember(targetOrder.id) {
      mutableStateOf(targetOrder.ratingReview)
    }
    val tipOptions = listOf(0.0, 10.0, 20.0, 30.0, 50.0)

    AlertDialog(
      onDismissRequest = { orderToRate = null },
      title = {
        Text("⭐ रेटिंग व डिलीवरी टिप दें", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "ऑर्डर #${targetOrder.id} के लिए अपना अनुभव साझा करें:",
            fontSize = 12.sp,
            color = Color.Gray
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
          ) {
            (1..5).forEach { star ->
              IconButton(onClick = { selectedStars = star }) {
                Icon(
                  imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                  contentDescription = "$star Star",
                  tint = if (star <= selectedStars) Color(0xFFF59E0B) else Color.Gray,
                  modifier = Modifier.size(32.dp)
                )
              }
            }
          }
          Text(
            text = "डिलीवरी पार्टनर के लिए टिप चुनें (वैकल्पिक):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            tipOptions.forEach { amt ->
              val isSel = selectedTip == amt
              Surface(
                color = if (isSel) Color(0xFF0284C7) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .weight(1f)
                  .clickable { selectedTip = amt }
              ) {
                Text(
                  text = if (amt == 0.0) "₹0" else "₹${amt.toInt()}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSel) Color.White else Color(0xFF1E293B),
                  modifier = Modifier.padding(vertical = 8.dp),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
          OutlinedTextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            label = { Text("आपका संदेश / सुझाव (वैकल्पिक)") },
            placeholder = { Text("बहुत बढ़िया और समय पर डिलीवरी!") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.rateAndTipOrder(targetOrder.id, selectedStars, reviewText, selectedTip)
            if (selectedTip > 0) {
              viewModel.openUpiPayment(context, selectedTip, targetOrder.id, "Delivery Tip")
            }
            orderToRate = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
          Text(if (selectedTip > 0) "सबमिट व ₹${selectedTip.toInt()} टिप दें" else "रेटिंग सबमिट करें")
        }
      },
      dismissButton = {
        TextButton(onClick = { orderToRate = null }) {
          Text("बंद करें")
        }
      }
    )
  }
}

@Composable
fun OrderItemCard(
  order: Order,
  storeWhatsapp: String,
  storeEmail: String,
  onWhatsAppOwner: () -> Unit,
  onEmailOwner: () -> Unit,
  onCallOwner: () -> Unit,
  onViewQr: () -> Unit,
  onRateAndTip: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Top row: ID & Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "ऑर्डर #${order.id}",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = Color(0xFF111827)
          )
          Text(
            text = "पक्की डिलीवरी: ${order.deliveryDate}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreenLight
          )
        }

        Surface(
          color = Color(order.status.badgeColorHex),
          shape = RoundedCornerShape(20.dp)
        ) {
          Text(
            text = order.status.hindiLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      HorizontalDivider(color = Color(0xFFF3F4F6))
      Spacer(modifier = Modifier.height(8.dp))

      // Customer and Route
      Text(
        text = "📍 ${order.area} • ${order.addressDetail}",
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF374151)
      )

      Text(
        text = "👤 ${order.customerName} (फोन: ${order.customerPhone})",
        fontSize = 11.sp,
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 2.dp)
      )

      // Items list summary
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp)
          .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
          .padding(10.dp)
      ) {
        Text(
          text = "सामान सूची (${order.items.size} Items):",
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = Color.Gray
        )
        order.items.forEach { item ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "${item.product.emoji} ${item.product.name} × ${item.quantity}",
              fontSize = 12.sp,
              color = Color(0xFF1F2937),
              modifier = Modifier.weight(1f)
            )
            Text(
              text = "₹${(item.product.ourPrice * item.quantity).toInt()}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF111827)
            )
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "डिलीवरी शुल्क", fontSize = 11.sp, color = Color.Gray)
          Text(text = "₹${order.deliveryCharge.toInt()}${if (order.heavyDeliveryCharge > 0) " + ₹${order.heavyDeliveryCharge.toInt()} (भारी)" else ""}", fontSize = 11.sp, color = Color.Gray)
        }
      }

      // Bill and Payment info
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "कुल बिल: ₹${order.grandTotal.toInt()}",
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            color = PrimaryGreenDark
          )
          if (order.advancePaid > 0) {
            Text(
              text = "अग्रिम जमा: ₹${order.advancePaid.toInt()} · शेष 8 AM पर: ₹${order.balanceDueAtDelivery.toInt()}",
              fontSize = 11.sp,
              color = Color(0xFFB45309),
              fontWeight = FontWeight.Bold
            )
          }
        }

        // WhatsApp, Email & Call buttons
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          // WhatsApp to 8982903843
          Surface(
            color = Color(0xFF25D366),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.clickable { onWhatsAppOwner() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("💬", fontSize = 11.sp)
              Spacer(modifier = Modifier.width(3.dp))
              Text("WhatsApp", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
          }

          // Email button
          Surface(
            color = Color(0xFF2563EB),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.clickable { onEmailOwner() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text("ईमेल", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
          }

          // Call button
          OutlinedButton(
            onClick = onCallOwner,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(12.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      if (order.rating > 0) {
        Surface(
          color = Color(0xFFFEF3C7),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().clickable { onRateAndTip() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "⭐ ${order.rating}/5 रेटिंग${if (order.deliveryTip > 0) " · 💝 टिप: ₹${order.deliveryTip.toInt()}" else ""}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF92400E)
            )
            Text("बदलें", fontSize = 10.sp, color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
          }
        }
      } else {
        OutlinedButton(
          onClick = onRateAndTip,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().height(36.dp).testTag("rate_tip_button_${order.id}")
        ) {
          Text("⭐ रेटिंग व डिलीवरी टिप दें (Rate & Tip)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
        }
      }
    }
  }
}
