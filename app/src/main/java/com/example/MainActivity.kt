package com.example

import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.screens.AdminHubScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CustomRequestScreen
import com.example.ui.screens.GuaranteeDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.MyntraDark
import com.example.ui.theme.MyntraPink
import com.example.ui.theme.SecondaryGold
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val gifImageLoader = ImageLoader.Builder(applicationContext)
      .components {
        if (Build.VERSION.SDK_INT >= 28) {
          add(ImageDecoderDecoder.Factory())
        } else {
          add(GifDecoder.Factory())
        }
      }
      .memoryCache {
        MemoryCache.Builder(applicationContext)
          .maxSizePercent(0.25)
          .build()
      }
      .diskCache {
        DiskCache.Builder()
          .directory(applicationContext.cacheDir.resolve("image_cache"))
          .maxSizeBytes(50L * 1024 * 1024)
          .build()
      }
      .respectCacheHeaders(false)
      .build()
    Coil.setImageLoader(gifImageLoader)

    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(AndroidColor.parseColor("#282C3F"))
    )
    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          EightAmMartApp()
        }
      }
    }
  }
}

@Composable
fun EightAmMartApp(viewModel: StoreViewModel = viewModel()) {
  val currentUser by viewModel.currentUser.collectAsState()
  val currentScreen by viewModel.currentScreen.collectAsState()
  val storeConfig by viewModel.storeConfig.collectAsState()
  val selectedLanguage by viewModel.selectedLanguage.collectAsState()
  var showWelcomeOverlay by remember { mutableStateOf(true) }
  var progress by remember { mutableFloatStateOf(1f) }

  LaunchedEffect(currentUser) {
    if (currentUser != null && showWelcomeOverlay) {
      val totalMs = 5000L
      val stepMs = 50L
      var elapsed = 0L
      while (elapsed < totalMs && showWelcomeOverlay) {
        delay(stepMs)
        elapsed += stepMs
        progress = 1f - (elapsed.toFloat() / totalMs.toFloat())
      }
      showWelcomeOverlay = false
    }
  }

  if (currentUser == null) {
    AuthScreen(
      onSignInSuccess = { user ->
        viewModel.onUserSignedIn(user)
      }
    )
    return
  }

  CompositionLocalProvider(com.example.data.LocalAppLanguage provides selectedLanguage) {
  Box(modifier = Modifier.fillMaxSize()) {
    Crossfade(
      targetState = currentScreen,
      animationSpec = tween(durationMillis = 150),
      label = "ScreenTransition"
    ) { screen ->
      when (screen) {
        Screen.HOME -> HomeScreen(viewModel = viewModel)
        Screen.CART -> CartScreen(viewModel = viewModel)
        Screen.CUSTOM_REQUEST -> CustomRequestScreen(viewModel = viewModel)
        Screen.ORDERS -> OrdersScreen(viewModel = viewModel)
        Screen.ADMIN_HUB -> AdminHubScreen(viewModel = viewModel)
        Screen.GUARANTEE_DETAIL -> GuaranteeDetailScreen(viewModel = viewModel)
      }
    }

    AnimatedVisibility(
      visible = showWelcomeOverlay,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                MyntraDark.copy(alpha = 0.94f),
                Color(0xFF141824).copy(alpha = 0.97f)
              )
            )
          )
          .padding(24.dp)
          .testTag("welcome_5sec_overlay"),
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
        ) {
          Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .background(
                  Brush.radialGradient(listOf(SecondaryGold, MyntraPink)),
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = storeConfig.founderInitials,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
              )
            }

            Text(
              text = "${storeConfig.shopName} में स्वागत है!",
              fontSize = 20.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MyntraDark,
              textAlign = TextAlign.Center
            )

            Surface(
              color = Color(0xFFE0F2FE),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = storeConfig.welcomeMessage,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = Color(0xFF334155),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(14.dp)
              )
            }

            LinearProgressIndicator(
              progress = { progress.coerceIn(0f, 1f) },
              color = MyntraPink,
              trackColor = Color(0xFFF1F5F9),
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
            )

            val secsLeft = ((progress * 5f).toInt() + 1).coerceIn(1, 5)
            Text(
              text = "${secsLeft} सेकंड में अपने आप खुलेगा...",
              fontSize = 11.sp,
              color = Color.Gray
            )

            Spacer(modifier = Modifier.height(2.dp))

            Button(
              onClick = { showWelcomeOverlay = false },
              colors = ButtonDefaults.buttonColors(containerColor = MyntraPink),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("welcome_skip_button")
            ) {
              Text("आगे बढ़ें ➔", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
          }
        }
      }
    }
  }
  }
}
