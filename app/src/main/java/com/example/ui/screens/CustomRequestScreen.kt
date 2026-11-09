package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenDark
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.SecondaryGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRequestScreen(
  viewModel: StoreViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val customRequests by viewModel.customRequests.collectAsState()
  val selectedArea by viewModel.selectedArea.collectAsState()
  val storeConfig by viewModel.storeConfig.collectAsState()

  var itemName by remember { mutableStateOf("") }
  var quantity by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }
  var customerName by remember { mutableStateOf("") }
  var customerPhone by remember { mutableStateOf("") }
  var customerEmail by remember { mutableStateOf("") }

  BackHandler {
    viewModel.navigateTo(Screen.HOME)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "कुछ और मँगाना है? (Custom Request)",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Text(
              text = "रायगढ़ बाज़ार ➔ पुंजीपथरा (सुबह 8:00 AM डिलीवरी)",
              fontSize = 11.sp,
              color = Color.White.copy(alpha = 0.85f)
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = { viewModel.navigateTo(Screen.HOME) },
            modifier = Modifier.testTag("custom_request_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
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
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(Color(0xFFF7F9F7))
        .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      // 1. Explanatory Card with Founder's Guarantee
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F5132)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(SecondaryGold, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = storeConfig.founderInitials,
                  fontWeight = FontWeight.Black,
                  fontSize = 16.sp,
                  color = Color(0xFF432C00)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "${storeConfig.founderName} (संस्थापक)",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = Color(0xFFFCD34D)
                )
                Text(
                  text = "100% सुरक्षित एवं विश्वसनीय सेवा · कोई फर्जीवाड़ा नहीं!",
                  fontSize = 11.sp,
                  color = Color.White
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "यदि आपकी ज़रूरत का कोई सामान ऐप में नहीं मिल रहा (जैसे कोई खास ब्रांड की दवाई, रायगढ़ स्टेशन की प्रसिद्ध मिठाई, प्लांट/फैक्ट्री का छोटा हार्डवेयर, या बल्क किराना), तो नीचे लिखकर भेजें। मृत्युंजय खुद सुबह 5 बजे रायगढ़ मेन मार्केट से खरीदकर सुबह 8 बजे पुंजीपथरा में डिलीवर करेंगे।",
              fontSize = 12.sp,
              color = Color(0xFFD1E7DD),
              lineHeight = 16.sp
            )
          }
        }
      }

      // 2. Request Form Card
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            Text(
              text = "सामान का विवरण (Item Details)",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = Color(0xFF111827)
            )

            OutlinedTextField(
              value = itemName,
              onValueChange = { itemName = it },
              label = { Text("क्या सामान चाहिए? (Item Name)*") },
              placeholder = { Text("उदा: डोलो 650, रायगढ़ स्टेशन पेड़ा, स्पेशल चाय पत्ती...") },
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("custom_item_name_input")
            )

            OutlinedTextField(
              value = quantity,
              onValueChange = { quantity = it },
              label = { Text("मात्रा (Quantity / Weight / Pieces)*") },
              placeholder = { Text("उदा: 1 किग्रा, 2 पैकेट, 1 स्ट्रिप") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("custom_item_qty_input")
            )

            OutlinedTextField(
              value = notes,
              onValueChange = { notes = it },
              label = { Text("रायगढ़ की कोई विशेष दुकान या निर्देश (Shop / Notes)") },
              placeholder = { Text("उदा: कोटरा रोड स्टेशन मार्केट से लाएं") },
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "आपकी जानकारी (Customer Details)",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color(0xFF111827)
            )

            OutlinedTextField(
              value = customerName,
              onValueChange = { customerName = it },
              label = { Text("आपका नाम (Full Name)*") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("custom_name_input")
            )

            OutlinedTextField(
              value = customerPhone,
              onValueChange = { customerPhone = it },
              label = { Text("मोबाइल नंबर (Mobile Number)*") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("custom_phone_input")
            )

            OutlinedTextField(
              value = customerEmail,
              onValueChange = { customerEmail = it },
              label = { Text("ईमेल (Email Address for Direct Reply)") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("custom_email_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Submit Button (App)
            Button(
              onClick = {
                if (itemName.isBlank()) {
                  Toast.makeText(context, "कृपया सामान का नाम दर्ज करें", Toast.LENGTH_SHORT).show()
                  return@Button
                }
                if (quantity.isBlank()) {
                  Toast.makeText(context, "कृपया मात्रा दर्ज करें", Toast.LENGTH_SHORT).show()
                  return@Button
                }
                if (customerName.isBlank() || customerPhone.isBlank()) {
                  Toast.makeText(context, "कृपया नाम व फोन नंबर भरें", Toast.LENGTH_SHORT).show()
                  return@Button
                }

                viewModel.submitCustomRequest(
                  customerName = customerName,
                  customerPhone = customerPhone,
                  customerEmail = customerEmail,
                  itemName = itemName,
                  quantity = quantity,
                  notes = notes,
                  onSuccess = { req ->
                    Toast.makeText(
                      context,
                      "रिक्वेस्ट सबमिट हो गई! आईडी: ${req.id}",
                      Toast.LENGTH_LONG
                    ).show()
                    itemName = ""
                    quantity = ""
                    notes = ""
                  }
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("submit_custom_request_button")
            ) {
              Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
              Spacer(modifier = Modifier.width(8.dp))
              Text("कल सुबह 8 AM डिलीवरी हेतु सबमिट करें", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // WhatsApp Direct Request Option
            Surface(
              color = Color(0xFF25D366),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable {
                  val msg = buildString {
                    append("नमस्ते मृत्युंजय जी, मुझे *अपना मार्केट* से यह स्पेशल सामान मँगाना है:\n\n")
                    append("सामान: ${if (itemName.isNotBlank()) itemName else "[सामान का नाम]"}\n")
                    append("मात्रा: ${if (quantity.isNotBlank()) quantity else "[मात्रा]"}\n")
                    append("निर्देश: $notes\n\n")
                    append("नाम: ${if (customerName.isNotBlank()) customerName else "ग्राहक"}\n")
                    append("फोन: $customerPhone\n")
                    append("स्थान: ${selectedArea.name}\n\n")
                    append("कृपया सुबह 8 बजे डिलीवरी की पुष्टि करें।")
                  }
                  viewModel.openWhatsApp(context, storeConfig.whatsapp, msg)
                }
            ) {
              Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("💬", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "WhatsApp पर भेजें (${storeConfig.whatsapp})",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Direct Email Option
            OutlinedButton(
              onClick = {
                val emailBody = buildString {
                  append("नमस्ते मृत्युंजय जी,\n\n")
                  append("मुझे कल सुबह 8:00 AM डिलीवरी के लिए रायगढ़ से यह स्पेशल सामान चाहिए:\n\n")
                  append("सामान: ${if (itemName.isNotBlank()) itemName else "[सामान]"}\n")
                  append("मात्रा: ${if (quantity.isNotBlank()) quantity else "[मात्रा]"}\n")
                  append("निर्देश/दुकान: $notes\n\n")
                  append("ग्राहक का नाम: $customerName\n")
                  append("फोन: $customerPhone\n")
                  append("डिलीवरी स्थान: ${selectedArea.name}\n\n")
                  append("कृपया पुष्टि करें। धन्यवाद!")
                }

                viewModel.openEmailComposer(
                  context = context,
                  subject = "अपना मार्केट स्पेशल रिक्वेस्ट - ${if (customerName.isNotBlank()) customerName else "ग्राहक"}",
                  body = emailBody
                )
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("email_custom_request_button")
            ) {
              Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = PrimaryGreen)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                "ईमेल भेजें (${storeConfig.storeOwnerEmail})",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = PrimaryGreen
              )
            }
          }
        }
      }

      // 3. Previously Submitted Requests
      item {
        Text(
          text = "आपकी स्पेशल रिक्वेस्ट्स (${customRequests.size})",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = Color(0xFF111827),
          modifier = Modifier.padding(bottom = 6.dp)
        )
      }

      if (customRequests.isEmpty()) {
        item {
          Text(
            text = "अभी तक कोई स्पेशल रिक्वेस्ट नहीं भेजी गई है।",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
          )
        }
      } else {
        items(customRequests, key = { it.id }) { req ->
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = req.itemName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = Color(0xFF1F2937)
                )
                Surface(
                  color = Color(0xFFE0E7FF),
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = req.id,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3730A3),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Text(
                text = "मात्रा: ${req.quantity} • डिलीवरी स्थान: ${req.targetArea}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 2.dp)
              )

              if (req.notes.isNotBlank()) {
                Text(
                  text = "नोट: ${req.notes}",
                  fontSize = 11.sp,
                  color = Color(0xFF4B5563)
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = null,
                  tint = SecondaryAmber,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = req.status,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF92400E)
                )
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}
