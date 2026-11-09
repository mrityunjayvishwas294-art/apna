package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.SecondaryGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuaranteeDetailScreen(
  viewModel: StoreViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val storeConfig by viewModel.storeConfig.collectAsState()

  BackHandler {
    viewModel.navigateTo(Screen.HOME)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "100% सुरक्षित एवं विश्वसनीय सेवा",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        },
        navigationIcon = {
          IconButton(
            onClick = { viewModel.navigateTo(Screen.HOME) },
            modifier = Modifier.testTag("guarantee_back_button")
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
        .padding(14.dp)
    ) {
      // Golden Certificate Header with MV Avatar
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = PrimaryGreenDark),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .background(SecondaryGold, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = storeConfig.founderInitials,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = Color(0xFF432C00)
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = storeConfig.shopName,
              fontWeight = FontWeight.Black,
              fontSize = 20.sp,
              color = Color.White
            )
            Text(
              text = storeConfig.founderHead,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = SecondaryGold
            )
            Text(
              text = storeConfig.tagline,
              fontSize = 11.sp,
              color = Color(0xFFA7F3D0),
              modifier = Modifier.padding(top = 2.dp)
            )
          }
        }
      }

      // Founder Message Box
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "संस्थापक का संदेश (Founder's Message)",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = PrimaryGreenDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "“${storeConfig.founderMsg}”",
              fontSize = 12.sp,
              lineHeight = 18.sp,
              color = Color(0xFF374151)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "— ${storeConfig.founderName} (संस्थापक, ${storeConfig.shopName})",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = PrimaryGreenLight
            )
          }
        }
      }

      // 5 Core Pillars
      item {
        Text(
          text = "हमारा 5-बिंदु विश्वास वादा (Our 5 Pillars):",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = Color(0xFF111827),
          modifier = Modifier.padding(bottom = 8.dp)
        )
      }

      val points = listOf(
        Triple(
          "1. सुबह 8:00 AM पक्की डिलीवरी",
          "रायगढ़ सेंट्रल मंडी से सुबह 5:00 बजे ताज़ा सामान खरीदकर ठीक 8:00 AM पर आपके घर पुंजीपथरा में डिलीवर।",
          Icons.Default.Schedule
        ),
        Triple(
          "2. 100% भरोसेमंद लोकल डिलीवरी",
          "कोई फर्जीवाड़ा नहीं! आपके अपने क्षेत्र के लोगों द्वारा संचालित विश्वसनीय स्थानीय सेवा।",
          Icons.Default.VerifiedUser
        ),
        Triple(
          "3. उचित दर एवं सुरक्षित डिजिटल भुगतान",
          "UPI ID: ${storeConfig.upiId} (${storeConfig.upiName}) पर सुरक्षित भुगतान। डिलीवरी शुल्क ₹${storeConfig.deliveryFee.toInt()} व भारी सामान पर +₹${storeConfig.heavyDeliveryFee.toInt()}।",
          Icons.Default.CheckCircle
        ),
        Triple(
          "4. मौके पर तुरंत वापसी (Zero Risk Return)",
          "यदि कोई भी फल, सब्जी या पैकेट पसंद न आए, तो डिलीवरी बॉय को मौके पर ही तुरंत वापस कर सकते हैं।",
          Icons.Default.ThumbUp
        ),
        Triple(
          "5. मृत्युंजय विश्वास से सीधा संपर्क",
          "WhatsApp (${storeConfig.whatsapp}) और ईमेल (${storeConfig.storeOwnerEmail}) पर 24/7 सहायता उपलब्ध है।",
          Icons.Default.Security
        )
      )

      items(points.size) { index ->
        val (title, desc, icon) = points[index]
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFE8F5E9), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1F2937))
              Spacer(modifier = Modifier.height(2.dp))
              Text(text = desc, fontSize = 12.sp, color = Color(0xFF4B5563), lineHeight = 16.sp)
            }
          }
        }
      }

      // Contact Mrityunjay directly card
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 20.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            Text(
              text = "सीधा संपर्क: ${storeConfig.founderName}",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color(0xFF1E3A8A)
            )
            Text(
              text = "WhatsApp: ${storeConfig.whatsapp}\nUPI: ${storeConfig.upiId} (${storeConfig.upiName})\nEmail: ${storeConfig.storeOwnerEmail}\nरूट: ${storeConfig.routeCorridor}",
              fontSize = 12.sp,
              color = Color(0xFF1D4ED8),
              lineHeight = 18.sp,
              modifier = Modifier.padding(vertical = 6.dp)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  viewModel.openWhatsApp(
                    context,
                    storeConfig.whatsapp,
                    "नमस्ते मृत्युंजय जी, मैं अपना मार्केट ऐप के संबंध में संपर्क कर रहा हूँ।"
                  )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                modifier = Modifier.weight(1f)
              ) {
                Text("WhatsApp चैट", fontWeight = FontWeight.Bold)
              }

              OutlinedButton(
                onClick = { viewModel.callPhone(context, storeConfig.whatsapp) },
                modifier = Modifier.weight(1f)
              ) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = PrimaryGreen)
                Spacer(modifier = Modifier.width(6.dp))
                Text("कॉल करें", color = PrimaryGreen, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}
