package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Order
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.components.AreaPickerBottomSheet
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
fun CartScreen(
  viewModel: StoreViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val cart by viewModel.cart.collectAsState()
  val cartItems = cart.values.toList()
  val cartCount by viewModel.cartItemCount.collectAsState()
  val subtotal by viewModel.cartTotalPrice.collectAsState()
  val savings by viewModel.cartTotalSavings.collectAsState()
  val hasHeavy by viewModel.hasHeavyItems.collectAsState()
  val selectedArea by viewModel.selectedArea.collectAsState()
  val storeConfig by viewModel.storeConfig.collectAsState()

  val deliveryFee = storeConfig.deliveryFee
  val heavyFee = if (hasHeavy) storeConfig.heavyDeliveryFee else 0.0
  val grandTotal = subtotal + deliveryFee + heavyFee

  // Requirement: "pahale delevry charge de limit 500 tak"
  val isAdvanceDeliveryChargeMandatory = subtotal in 1.0..storeConfig.advanceThreshold
  val requiredAdvanceAmount = if (isAdvanceDeliveryChargeMandatory) deliveryFee + heavyFee else 0.0
  val remainingBalanceAtDoor = if (grandTotal > requiredAdvanceAmount) grandTotal - requiredAdvanceAmount else 0.0

  var customerName by remember { mutableStateOf("") }
  var customerPhone by remember { mutableStateOf("") }
  var customerEmail by remember { mutableStateOf("") }
  var addressDetail by remember { mutableStateOf("") }
  var specialNote by remember { mutableStateOf("") }
  var upiRefInput by remember { mutableStateOf("") }
  var advanceConfirmedCheckbox by remember { mutableStateOf(false) }

  var selectedPaymentMethod by remember {
    mutableStateOf(if (isAdvanceDeliveryChargeMandatory) "UPI Advance (₹${requiredAdvanceAmount.toInt()}) + Balance At Door" else "UPI Full (${storeConfig.upiId})")
  }

  var showAreaPicker by remember { mutableStateOf(false) }
  var showQrZoomDialog by remember { mutableStateOf(false) }
  var confirmedOrder by remember { mutableStateOf<Order?>(null) }

  BackHandler {
    viewModel.navigateTo(Screen.HOME)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "अपना मार्केट · चेकआउट",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Text(
              text = "डिलीवरी: ${selectedArea.name} (सुबह 8:00 AM)",
              fontSize = 11.sp,
              color = Color.White.copy(alpha = 0.85f)
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = { viewModel.navigateTo(Screen.HOME) },
            modifier = Modifier.testTag("cart_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }
        },
        actions = {
          if (cartItems.isNotEmpty()) {
            TextButton(onClick = { viewModel.clearCart() }) {
              Text("Clear", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = PrimaryGreen,
          titleContentColor = Color.White
        )
      )
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    if (cartItems.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .background(Color(0xFFF7F9F7)),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp)
        ) {
          Text("🛒", fontSize = 56.sp)
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "आपकी टोकरी खाली है",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
          )
          Text(
            text = "सुबह 8 बजे ताज़ा सामान पाने के लिए कार्ट में आइटम जोड़ें।",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
          )
          Button(
            onClick = { viewModel.navigateTo(Screen.HOME) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("cart_shop_now_button")
          ) {
            Text("सामान देखें (Browse Items)", fontWeight = FontWeight.Bold)
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .background(Color(0xFFF7F9F7))
          .padding(horizontal = 14.dp, vertical = 8.dp)
      ) {
        // 1. Mandatory Advance Notice for orders <= 500
        if (isAdvanceDeliveryChargeMandatory) {
          item {
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(SecondaryGold, SecondaryAmber))
              ),
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = null,
                  tint = Color(0xFFB45309),
                  modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "₹500 तक के ऑर्डर पर डिलीवरी चार्ज पहले देय है",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF78350F)
                  )
                  Text(
                    text = "फेक बुकिंग रोकने हेतु ₹${deliveryFee.toInt()} डिलीवरी चार्ज पहले UPI से जमा करें। सामान का मूल्य (₹${subtotal.toInt()}) सुबह 8 बजे सामान मिलने पर दें।",
                    fontSize = 11.sp,
                    color = Color(0xFF92400E),
                    lineHeight = 15.sp
                  )
                }
              }
            }
          }
        }

        // 2. Delivery Slot Promise
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E7)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 10.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(SecondaryGold, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Schedule,
                  contentDescription = null,
                  tint = Color(0xFF432C00),
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "सुबह 8:00 AM पक्की होम डिलीवरी",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = Color(0xFF432C00)
                )
                Text(
                  text = "रायगढ़ सेंट्रल मंडी से सुबह 5 बजे ताज़ा पैकिंग ➔ 8:00 AM पुंजीपथरा",
                  fontSize = 11.sp,
                  color = Color(0xFF78350F)
                )
              }
            }
          }
        }

        // 3. Cart Items Header
        item {
          Text(
            text = "ऑर्डर किए गए सामान ($cartCount)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 6.dp)
          )
        }

        // 4. Cart Items List
        items(cartItems, key = { "${it.product.id}__${it.product.unit}" }) { item ->
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
              ) {
                Text(text = item.product.emoji, fontSize = 24.sp)
              }
              Spacer(modifier = Modifier.width(10.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = item.product.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color(0xFF111827),
                  maxLines = 1
                )
                Text(
                  text = "${item.product.unit} • ₹${item.product.ourPrice.toInt()}",
                  fontSize = 11.sp,
                  color = Color.Gray
                )
                if (item.product.mrp > item.product.ourPrice) {
                  Text(
                    text = "MRP: ₹${item.product.mrp.toInt()}",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    textDecoration = TextDecoration.LineThrough
                  )
                }
              }

              // Stepper
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .background(PrimaryGreen, RoundedCornerShape(6.dp))
                  .padding(horizontal = 4.dp, vertical = 2.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clickable { viewModel.removeFromCart("${item.product.id}__${item.product.unit}") },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                }

                Text(
                  text = "${item.quantity}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp)
                )

                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clickable { viewModel.addToCart(item.product) },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              Text(
                text = "₹${(item.product.ourPrice * item.quantity).toInt()}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = PrimaryGreenDark
              )
            }
          }
        }

        // 5. Customer Delivery Details Section
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 14.dp, bottom = 10.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
            ) {
              Text(
                text = "डिलीवरी का पता (रायगढ़ ➔ पुंजीपथरा)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF111827)
              )

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 8.dp)
                  .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                  .clickable { showAreaPicker = true }
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "${selectedArea.name} (${selectedArea.hindiName})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PrimaryGreenDark
                  )
                }
                Text(
                  text = "बदलें",
                  color = PrimaryGreen,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("आपका पूरा नाम (Full Name)*") },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .testTag("checkout_name_input")
              )

              OutlinedTextField(
                value = customerPhone,
                onValueChange = { customerPhone = it },
                label = { Text("मोबाइल नंबर (सुबह 8 बजे डिलीवरी कॉल हेतु)*") },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .testTag("checkout_phone_input")
              )

              OutlinedTextField(
                value = customerEmail,
                onValueChange = { customerEmail = it },
                label = { Text("ईमेल (वैकल्पिक / Optional)") },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .testTag("checkout_email_input")
              )

              OutlinedTextField(
                value = addressDetail,
                onValueChange = { addressDetail = it },
                label = { Text("मकान नं, गली, कॉलोनी, प्लांट या फ्लैट का पूरा पता*") },
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .testTag("checkout_address_input")
              )

              OutlinedTextField(
                value = specialNote,
                onValueChange = { specialNote = it },
                label = { Text("डिलीवरी निर्देश (उदा: गेट पर गार्ड को दे दें)") },
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
              )
            }
          }
        }

        // 6. OFFICIAL UPI QR CODE & PRE-PAYMENT SECTION
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(PrimaryGreen, SecondaryGold))
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "मृत्युंजय विश्वास का ऑफिशियल UPI QR",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = PrimaryGreenDark
                  )
                  Text(
                    text = "Google Pay • PhonePe • Paytm • BHIM",
                    fontSize = 11.sp,
                    color = Color.Gray
                  )
                }

                Surface(
                  color = Color(0xFFEFF6FF),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.clickable { showQrZoomDialog = true }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("बड़ा देखें", fontSize = 10.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Embedded Official Google Pay QR Image from user
              Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                  .size(200.dp)
                  .clickable { showQrZoomDialog = true }
              ) {
                Image(
                  painter = painterResource(id = R.drawable.upi_qr_mrityunjay),
                  contentDescription = "Mrityunjay Vishwas UPI QR Code",
                  contentScale = ContentScale.Fit,
                  modifier = Modifier.fillMaxSize()
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "UPI ID: ${storeConfig.upiId}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D4ED8)
              )
              Text(
                text = "नाम: ${storeConfig.upiName}",
                fontSize = 11.sp,
                color = Color.Gray
              )

              Spacer(modifier = Modifier.height(10.dp))

              // 1-Tap UPI Pay Buttons
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // Pay Advance Delivery Fee Button
                Button(
                  onClick = {
                    val amountToPay = if (isAdvanceDeliveryChargeMandatory) requiredAdvanceAmount else 60.0
                    viewModel.openUpiPayment(context, amountToPay, "ADVANCE", "Delivery Advance")
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(40.dp)
                ) {
                  Text(
                    text = "डिलीवरी ₹${deliveryFee.toInt()} पे करें",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                }

                // Pay Full Bill Button
                OutlinedButton(
                  onClick = {
                    viewModel.openUpiPayment(context, grandTotal, "FULL", "Apna Market Order")
                  },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(40.dp)
                ) {
                  Text(
                    text = "पूरा ₹${grandTotal.toInt()} पे करें",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = PrimaryGreen
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // UPI Reference / UTR input
              OutlinedTextField(
                value = upiRefInput,
                onValueChange = { upiRefInput = it },
                label = { Text("UPI रेफरेंस / UTR नंबर दर्ज करें (वैकल्पिक)") },
                placeholder = { Text("उदा: 12 अंकों का UTR नंबर या पे किया") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
              )

              if (isAdvanceDeliveryChargeMandatory) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { advanceConfirmedCheckbox = !advanceConfirmedCheckbox }
                    .padding(vertical = 4.dp)
                ) {
                  Checkbox(
                    checked = advanceConfirmedCheckbox,
                    onCheckedChange = { advanceConfirmedCheckbox = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                  )
                  Text(
                    text = "मैंने मृत्युंजय विश्वास के UPI पर ₹${deliveryFee.toInt()} डिलीवरी चार्ज पे कर दिया है।",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreenDark
                  )
                }
              }
            }
          }
        }

        // 7. Bill Details
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 16.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
            ) {
              Text(
                text = "बिल विवरण (Bill Breakdown)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF111827)
              )
              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("सामान का कुल मूल्य (Item Total)", fontSize = 13.sp, color = Color.Gray)
                Text("₹${(subtotal + savings).toInt()}", fontSize = 13.sp, color = Color.Gray)
              }

              if (savings > 0) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("अपना मार्केट डिस्काउंट (बचत)", fontSize = 13.sp, color = PrimaryGreenLight, fontWeight = FontWeight.Bold)
                  Text("-₹${savings.toInt()}", fontSize = 13.sp, color = PrimaryGreenLight, fontWeight = FontWeight.Bold)
                }
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("डिलीवरी शुल्क (रायगढ़ ➔ पुंजीपथरा)", fontSize = 13.sp, color = Color.Gray)
                Text("₹${deliveryFee.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
              }

              if (heavyFee > 0) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("भारी सामान अधिभार (Heavy Fee)", fontSize = 13.sp, color = Color(0xFFB45309))
                  Text("+₹${heavyFee.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                }
              }

              HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "कुल राशि (Grand Total)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  if (isAdvanceDeliveryChargeMandatory) {
                    Text(
                      text = "अग्रिम डिलीवरी: ₹${requiredAdvanceAmount.toInt()} · सामान पर देय: ₹${remainingBalanceAtDoor.toInt()}",
                      fontSize = 11.sp,
                      color = Color(0xFFB45309),
                      fontWeight = FontWeight.Bold
                    )
                  } else {
                    Text(
                      text = "डिलीवरी: कल सुबह ठीक 8:00 AM",
                      fontSize = 11.sp,
                      color = PrimaryGreenLight
                    )
                  }
                }
                Text(
                  text = "₹${grandTotal.toInt()}",
                  fontWeight = FontWeight.Black,
                  fontSize = 20.sp,
                  color = PrimaryGreenDark
                )
              }
            }
          }
        }

        // 8. Place Order Button
        item {
          Button(
            onClick = {
              if (customerName.isBlank()) {
                Toast.makeText(context, "कृपया अपना नाम दर्ज करें", Toast.LENGTH_SHORT).show()
                return@Button
              }
              if (customerPhone.isBlank() || customerPhone.length < 8) {
                Toast.makeText(context, "कृपया वैध मोबाइल नंबर दर्ज करें", Toast.LENGTH_SHORT).show()
                return@Button
              }
              if (addressDetail.isBlank()) {
                Toast.makeText(context, "कृपया डिलीवरी का पूरा पता दर्ज करें", Toast.LENGTH_SHORT).show()
                return@Button
              }

              // Pre-payment validation for <= 500
              if (isAdvanceDeliveryChargeMandatory && !advanceConfirmedCheckbox && upiRefInput.isBlank()) {
                Toast.makeText(
                  context,
                  "कृपया ₹500 तक के ऑर्डर के लिए ₹${deliveryFee.toInt()} डिलीवरी चार्ज UPI से पे करके चेकबॉक्स टिक करें!",
                  Toast.LENGTH_LONG
                ).show()
                return@Button
              }

              viewModel.placeOrder(
                customerName = customerName,
                customerPhone = customerPhone,
                customerEmail = customerEmail,
                addressDetail = addressDetail,
                paymentMethod = if (isAdvanceDeliveryChargeMandatory) "UPI Advance (₹${requiredAdvanceAmount.toInt()}) + Balance (₹${remainingBalanceAtDoor.toInt()} at 8 AM)" else selectedPaymentMethod,
                specialNote = specialNote,
                advancePaid = if (isAdvanceDeliveryChargeMandatory) requiredAdvanceAmount else 0.0,
                upiRef = upiRefInput,
                onSuccess = { order ->
                  confirmedOrder = order
                }
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("confirm_place_order_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isAdvanceDeliveryChargeMandatory) "ऑर्डर बुक करें (अग्रिम ₹${requiredAdvanceAmount.toInt()} जमा)" else "ऑर्डर कन्फर्म करें (₹${grandTotal.toInt()})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // WhatsApp Order Option
          OutlinedButton(
            onClick = {
              val itemsText = cartItems.joinToString("\n") { "• ${it.product.name} × ${it.quantity} = ₹${(it.product.ourPrice * it.quantity).toInt()}" }
              val message = buildString {
                append("नमस्ते मृत्युंजय जी, मुझे *अपना मार्केट* से यह ऑर्डर बुक करना है:\n\n")
                append(itemsText)
                append("\n\n*सामान कुल:* ₹${subtotal.toInt()}")
                append("\n*डिलीवरी शुल्क:* ₹${deliveryFee.toInt()}${if (heavyFee > 0) " + ₹${heavyFee.toInt()} भारी शुल्क" else ""}")
                append("\n*कुल राशि:* ₹${grandTotal.toInt()}")
                if (isAdvanceDeliveryChargeMandatory) {
                  append("\n*अग्रिम डिलीवरी शुल्क जमा:* ₹${requiredAdvanceAmount.toInt()}")
                  append("\n*सुबह 8 AM देय शेष राशि:* ₹${remainingBalanceAtDoor.toInt()}")
                }
                append("\n*नाम:* ${if (customerName.isNotBlank()) customerName else "ग्राहक"}")
                append("\n*स्थान:* ${selectedArea.name} (${addressDetail})")
                append("\n*फोन:* $customerPhone")
                if (upiRefInput.isNotBlank()) append("\n*UPI UTR:* $upiRefInput")
                append("\n*डिलीवरी समय:* कल सुबह ठीक 8:00 AM")
              }
              viewModel.openWhatsApp(context, storeConfig.whatsapp, message)
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text("💬", fontSize = 14.sp)
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "WhatsApp पर भी रसीद भेजें (${storeConfig.whatsapp})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F5132)
              )
            }
          }

          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }

  // Large QR Zoom Dialog
  if (showQrZoomDialog) {
    AlertDialog(
      onDismissRequest = { showQrZoomDialog = false },
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
            text = "Scan to pay with any UPI app",
            fontSize = 12.sp,
            color = Color.Gray
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.openUpiPayment(context, deliveryFee, "ADVANCE", "Delivery Advance")
            showQrZoomDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
          Text("UPI ऐप खोलें (Pay ₹${deliveryFee.toInt()})")
        }
      },
      dismissButton = {
        TextButton(onClick = { showQrZoomDialog = false }) {
          Text("बंद करें")
        }
      }
    )
  }

  // Special Thank You Confirmation Dialog
  confirmedOrder?.let { order ->
    AlertDialog(
      onDismissRequest = {
        confirmedOrder = null
        viewModel.navigateTo(Screen.ORDERS)
      },
      icon = {
        Box(
          modifier = Modifier
            .size(64.dp)
            .background(Color(0xFFFFF0F3), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(text = "🙏", fontSize = 34.sp)
        }
      },
      title = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "दिल से बहुत-बहुत धन्यवाद!",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = Color(0xFF0284C7),
            textAlign = TextAlign.Center
          )
          Text(
            text = "${order.customerName} जी, आपका ऑर्डर कन्फर्म हो गया है ✨",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = PrimaryGreenDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      },
      text = {
        Column {
          Surface(
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = "ऑर्डर आईडी: ${order.id}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = Color(0xFF1F2937)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = "📍 पता: ${order.area} (${order.addressDetail})", fontSize = 12.sp)
              Text(
                text = "⏰ डिलीवरी समय: ${storeConfig.deliveryTiming}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = SecondaryAmber
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "💰 कुल बिल: ₹${order.grandTotal.toInt()} (आपकी बचत: ₹${order.totalSavings.toInt()})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              if (order.advancePaid > 0) {
                Text(
                  text = "✅ अग्रिम डिलीवरी शुल्क: ₹${order.advancePaid.toInt()} (UPI जमा)",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = PrimaryGreen
                )
                Text(
                  text = "💵 सामान मिलने पर देय: ₹${order.balanceDueAtDelivery.toInt()}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFB45309)
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            color = Color(0xFFE8F5E9),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "🙏 मृत्युंजय विश्वास (MV) का विशेष धन्यवाद:\nहम पर भरोसा करने के लिए हृदय से आभार! आपका ताज़ा व 100% शुद्ध सामान ठीक सुबह 8:00 बजे आपके द्वार पर पहुँचेगा।",
              fontSize = 11.sp,
              color = PrimaryGreenDark,
              lineHeight = 16.sp,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            confirmedOrder = null
            viewModel.navigateTo(Screen.ORDERS)
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
        ) {
          Text("मेरे ऑर्डर्स देखें ➔", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            val summary = "Order ID: ${order.id}\nCustomer: ${order.customerName}\nPhone: ${order.customerPhone}\nArea: ${order.area}\nAdvance: Rs.${order.advancePaid}\nBalance: Rs.${order.balanceDueAtDelivery}\nTotal: Rs.${order.grandTotal}"
            viewModel.openEmailComposer(
              context,
              subject = "अपना मार्केट ऑर्डर #${order.id} - ${order.customerName}",
              body = "नमस्ते मृत्युंजय जी,\n\nमेरा अपना मार्केट ऑर्डर विवरण:\n\n$summary\n\nकृपया सुबह 8 बजे डिलीवरी की पुष्टि करें।"
            )
          }
        ) {
          Text("ईमेल भेजें")
        }
      }
    )
  }

  if (showAreaPicker) {
    AreaPickerBottomSheet(
      currentArea = selectedArea,
      onSelectArea = { viewModel.selectArea(it) },
      onDismiss = { showAreaPicker = false }
    )
  }
}
