package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.util.Base64
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.example.data.AppLanguage
import com.example.data.DefaultProducts
import com.example.data.LocalAppLanguage
import com.example.data.fetchCurrentLocationAddress
import com.example.data.hasLocationPermission
import com.example.data.localizedName
import com.example.data.localizedTitle
import com.example.data.s
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.model.BannerAd
import com.example.model.CartItem
import com.example.model.CorridorArea
import com.example.model.Product
import com.example.model.ProductVariant
import com.example.model.StoreConfig
import com.example.ui.theme.JioMartBlue
import com.example.ui.theme.JioMartBlueDark
import com.example.ui.theme.JioMartBlueLight
import com.example.ui.theme.MyntraDark
import com.example.ui.theme.MyntraPink
import com.example.ui.theme.MyntraPinkLight
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenDark
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.SecondaryGold
import com.example.ui.theme.TertiaryRed

@Composable
fun rememberCoilData(url: String): Any {
  return remember(url) {
    if (url.startsWith("data:image") && url.contains("base64,")) {
      try {
        val base64Part = url.substringAfter("base64,")
        Base64.decode(base64Part, Base64.DEFAULT)
      } catch (_: Exception) {
        url
      }
    } else {
      url
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopCorridorHeader(
  storeConfig: StoreConfig,
  selectedArea: CorridorArea,
  cartItemCount: Int,
  onOpenAreaPicker: () -> Unit,
  onOpenWhatsApp: () -> Unit,
  onOpenEmail: () -> Unit,
  onOpenOrders: () -> Unit,
  onSecretAdminTrigger: () -> Unit,
  onOpenCart: () -> Unit,
  onSignOut: () -> Unit = {},
  onOpenLanguage: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val lang = LocalAppLanguage.current
  Surface(
    color = Color.Transparent,
    contentColor = Color.White,
    shadowElevation = 4.dp,
    modifier = modifier
      .fillMaxWidth()
      .background(
        Brush.verticalGradient(
          listOf(Color(0xFF0284C7), Color(0xFF0369A1))
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      // Brand Name Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(
          modifier = Modifier
            .weight(1f)
            .combinedClickable(
              onClick = { /* normal click */ },
              onLongClick = { onSecretAdminTrigger() }
            )
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = storeConfig.shopName,
              fontSize = 20.sp,
              fontWeight = FontWeight.Black,
              color = Color.White,
              letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              color = MyntraPink,
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "8 AM EXPRESS",
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = storeConfig.tagline,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFD4D5D9),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Language Pill + WhatsApp Shortcut
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            color = Color.White.copy(alpha = 0.16f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .clickable { onOpenLanguage() }
              .testTag("header_language_button")
          ) {
            Text(
              text = "🌐 ${lang.shortLabel}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            )
          }

          Surface(
            color = Color(0xFF25D366),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .clickable { onOpenWhatsApp() }
              .testTag("header_whatsapp_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("💬", fontSize = 12.sp)
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "WhatsApp",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Location & Actions Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Location Selector
        Row(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .clickable { onOpenAreaPicker() }
            .padding(horizontal = 8.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = SecondaryGold,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              text = "${s(lang, "डिलीवरी", "Delivery", "ডেলিভারি")}: ${storeConfig.deliveryTiming}",
              fontSize = 9.sp,
              color = Color.White.copy(alpha = 0.85f)
            )
            Text(
              text = selectedArea.name,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              color = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Public Customer Actions
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          IconButton(
            onClick = onOpenOrders,
            modifier = Modifier.size(36.dp).testTag("orders_history_button")
          ) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = "Orders",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          IconButton(
            onClick = onOpenEmail,
            modifier = Modifier.size(36.dp).testTag("email_support_button")
          ) {
            Icon(
              imageVector = Icons.Default.Email,
              contentDescription = "Email",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          IconButton(
            onClick = onOpenCart,
            modifier = Modifier.size(36.dp).testTag("header_cart_button")
          ) {
            BadgedBox(
              badge = {
                if (cartItemCount > 0) {
                  Badge(
                    containerColor = SecondaryAmber,
                    contentColor = Color.Black
                  ) {
                    Text(
                      text = "$cartItemCount",
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp
                    )
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "Cart",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
          }

          IconButton(
            onClick = onSignOut,
            modifier = Modifier.size(36.dp).testTag("header_logout_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ExitToApp,
              contentDescription = "Sign Out",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FounderTrustCard(
  storeConfig: StoreConfig,
  onWhatsAppClick: () -> Unit,
  onCallClick: () -> Unit,
  onSecretAdminTrigger: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = JioMartBlueDark
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.horizontalGradient(listOf(SecondaryGold, PrimaryGreen))
    ),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 6.dp)
      .testTag("founder_trust_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // MV Circular Avatar - Secret Long Press triggers Admin PIN
        Box(
          modifier = Modifier
            .size(52.dp)
            .background(
              brush = Brush.radialGradient(listOf(SecondaryGold, SecondaryAmber)),
              shape = CircleShape
            )
            .combinedClickable(
              onClick = { /* normal */ },
              onLongClick = { onSecretAdminTrigger() }
            )
            .testTag("founder_mv_badge"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = storeConfig.founderInitials,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = Color(0xFF432C00)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = storeConfig.founderName,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 16.sp,
              color = Color(0xFFFCD34D)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              color = PrimaryGreen,
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "संस्थापक (Founder)",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = storeConfig.founderHead,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Founder Message verbatim
      Surface(
        color = Color(0xFF042F46),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "“${storeConfig.founderMsg}”",
          fontSize = 11.sp,
          color = Color(0xFFE0F2FE),
          lineHeight = 16.sp,
          modifier = Modifier.padding(10.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action buttons: WhatsApp & Call
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          color = Color(0xFF25D366),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .clickable { onWhatsAppClick() }
        ) {
          Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("💬", fontSize = 13.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "WhatsApp: ${storeConfig.whatsapp}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        Surface(
          color = Color.White,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(0.7f)
            .clickable { onCallClick() }
        ) {
          Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Phone,
              contentDescription = null,
              tint = JioMartBlue,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "कॉल करें",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = JioMartBlue
            )
          }
        }
      }
    }
  }
}

@Composable
fun FestivalSaleOfferBanner(
  storeConfig: StoreConfig,
  modifier: Modifier = Modifier
) {
  if (!storeConfig.isFestivalSaleActive) return

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFDC2626)))
    ),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 4.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .background(Color(0xFFFEF2F2), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text("🪔", fontSize = 24.sp)
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = storeConfig.festivalSaleHeading,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = Color(0xFF991B1B)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            color = TertiaryRed,
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = storeConfig.festivalDiscountBadge,
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
          }
        }
        Text(
          text = storeConfig.festivalSaleSubtitle,
          fontSize = 11.sp,
          color = Color(0xFF78350F),
          lineHeight = 15.sp,
          modifier = Modifier.padding(top = 2.dp)
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActiveBannersList(
  banners: List<BannerAd>,
  modifier: Modifier = Modifier
) {
  val activeList = remember(banners) { banners.filter { it.isActive } }
  if (activeList.isEmpty()) return

  val context = LocalContext.current
  val pagerState = rememberPagerState(pageCount = { activeList.size })

  // Smooth Blinkit/Myntra-style auto-swipe (pauses while user drags)
  LaunchedEffect(activeList.size, pagerState.isScrollInProgress) {
    if (activeList.size > 1 && !pagerState.isScrollInProgress) {
      while (true) {
        delay(3500L)
        val next = (pagerState.currentPage + 1) % activeList.size
        pagerState.animateScrollToPage(
          page = next,
          animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
        )
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    HorizontalPager(
      state = pagerState,
      contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
      pageSpacing = 10.dp,
      modifier = Modifier.fillMaxWidth()
    ) { page ->
      val banner = activeList[page]
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (banner.isFestivalSpecial) Color(0xFFFFF7ED) else Color(0xFFF0FDF4)
        ),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          if (!banner.mediaUrl.isNullOrBlank()) {
            val bannerModel = rememberCoilData(banner.mediaUrl)
            val bannerRequest = remember(bannerModel) {
              ImageRequest.Builder(context)
                .data(bannerModel)
                .size(720, 320)
                .precision(Precision.INEXACT)
                .crossfade(120)
                .build()
            }
            AsyncImage(
              model = bannerRequest,
              contentDescription = banner.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxWidth()
                .height(124.dp)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            )
          }
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
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
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
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
                color = Color(0xFF475569),
                modifier = Modifier.padding(top = 2.dp)
              )
            }
            Surface(
              color = if (banner.isFestivalSpecial) TertiaryRed else PrimaryGreenDark,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = banner.discountTag,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }

    if (activeList.size > 1) {
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        repeat(activeList.size) { index ->
          val active = pagerState.currentPage == index
          Box(
            modifier = Modifier
              .height(6.dp)
              .width(if (active) 18.dp else 6.dp)
              .clip(CircleShape)
              .background(if (active) MyntraPink else Color(0xFFCBD5E1))
          )
        }
      }
    }
  }
}

@Composable
fun MorningDeliveryBanner(
  storeConfig: StoreConfig,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFFFFFBEB)
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.horizontalGradient(listOf(SecondaryGold, SecondaryAmber))
    ),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 4.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .background(SecondaryGold, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("${storeConfig.launchDaysRemaining}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF432C00))
          Text("DAYS", fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color(0xFF432C00))
        }
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = storeConfig.launchDateText,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF432C00)
          )
        }
        Text(
          text = "${storeConfig.cutoffHourText} बुकिंग ➔ अगली ${storeConfig.deliveryTiming} निश्चित डिलीवरी (${storeConfig.deliveryScheduleDays})",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = PrimaryGreen,
          modifier = Modifier.padding(top = 1.dp)
        )
        Text(
          text = "2000+ सामान उपलब्ध। ₹${storeConfig.advanceThreshold.toInt()} तक डिलीवरी शुल्क (₹${storeConfig.deliveryFee.toInt()}) पहले UPI से देय।",
          fontSize = 10.sp,
          color = Color(0xFF6B4E00),
          lineHeight = 14.sp
        )
      }
    }
  }
}

@Composable
fun SpecialRequestBanner(
  onRequestClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFFEFF6FF)
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)))
    ),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 4.dp)
      .clickable { onRequestClick() }
      .testTag("custom_item_request_banner")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "🛍️",
        fontSize = 28.sp
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "कुछ और मँगाना है? (Custom Request)",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF1E3A8A)
          )
        }
        Text(
          text = "रायगढ़ बाज़ार की दवाई, प्रसिद्ध मिठाई या कोई भी सामान यहाँ लिखें",
          fontSize = 11.sp,
          color = Color(0xFF1D4ED8),
          lineHeight = 15.sp
        )
      }
      Surface(
        color = Color(0xFF2563EB),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text(
          text = "रिक्वेस्ट करें",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductCardItem(
  product: Product,
  cart: Map<String, CartItem>,
  isFestivalSaleActive: Boolean = false,
  isWishlisted: Boolean = false,
  onToggleWishlist: () -> Unit = {},
  onClick: () -> Unit = {},
  onAdd: (ProductVariant?) -> Unit,
  onRemove: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val resolvedImageUrl = product.imageUrl?.takeIf { it.isNotBlank() }
    ?: DefaultProducts.fallbackImageForCategory(product.category)

  val defaultIndex = remember(product.id, product.variants) {
    val idx = product.variants.indexOfFirst { it.label.equals(product.unit, ignoreCase = true) }
    if (idx >= 0) idx else 0
  }
  var selectedVariantIndex by remember(product.id, product.variants) {
    mutableIntStateOf(defaultIndex)
  }
  val activeVariant = product.variants.getOrNull(selectedVariantIndex)
  val displayUnit = activeVariant?.label ?: product.unit
  val displayMrp = activeVariant?.mrp ?: product.mrp
  val displayPrice = activeVariant?.ourPrice ?: product.ourPrice
  val displayDiscount = activeVariant?.discountPercent ?: product.discountPercent
  val displaySavings = activeVariant?.savings ?: product.savings
  val cartKey = "${product.id}__${displayUnit}"
  val cartQuantity = cart[cartKey]?.quantity ?: 0

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("product_card_${product.id}")
  ) {
    Column(
      modifier = Modifier.fillMaxWidth()
    ) {
      // Full-bleed Blinkit/Myntra-style Product Photo Frame
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(132.dp)
          .background(Color(0xFFF5F5F6)),
        contentAlignment = Alignment.Center
      ) {
        val productModel = rememberCoilData(resolvedImageUrl)
        val imageRequest = remember(productModel) {
          ImageRequest.Builder(context)
            .data(productModel)
            .size(360, 360)
            .precision(Precision.INEXACT)
            .crossfade(120)
            .build()
        }
        AsyncImage(
          model = imageRequest,
          contentDescription = product.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
        )

        // Top-left tag pill
        if (product.tag != null) {
          Surface(
            color = Color.White.copy(alpha = 0.92f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(6.dp)
          ) {
            Text(
              text = product.tag,
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              color = MyntraDark,
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
          }
        } else if (isFestivalSaleActive) {
          Surface(
            color = Color(0xFFFEF3C7),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(6.dp)
          ) {
            Text(
              text = "महासेल ऑफर",
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF92400E),
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
          }
        }

        // Top-right Myntra/Flipkart Discount badge
        if (displayDiscount > 0) {
          Surface(
            color = MyntraPink,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(6.dp)
          ) {
            Text(
              text = "${displayDiscount}% OFF",
              fontSize = 9.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
          }
        }

        // Bottom-right Wishlist Heart Button with Spring Bounce Motion
        val heartScale by animateFloatAsState(
          targetValue = if (isWishlisted) 1.15f else 1.0f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
          label = "heartBounce"
        )
        Surface(
          color = Color.White.copy(alpha = 0.94f),
          shape = CircleShape,
          shadowElevation = 2.dp,
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(6.dp)
            .size(30.dp)
            .graphicsLayer { scaleX = heartScale; scaleY = heartScale }
            .clickable { onToggleWishlist() }
            .testTag("wishlist_btn_${product.id}")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Wishlist",
              tint = if (isWishlisted) Color(0xFFEF4444) else Color(0xFF64748B),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(10.dp)
      ) {

      val lang = LocalAppLanguage.current
      // Product Title
      Text(
        text = product.localizedTitle(lang),
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = Color(0xFF1E293B),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 16.sp,
        modifier = Modifier.height(34.dp)
      )

      if (lang == AppLanguage.HINDI && product.name.isNotBlank()) {
        Text(
          text = product.name,
          fontSize = 10.sp,
          color = Color(0xFF64748B),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      } else if (lang != AppLanguage.HINDI && product.hindiName.isNotBlank()) {
        Text(
          text = product.hindiName,
          fontSize = 11.sp,
          color = Color(0xFF64748B),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      if (product.variants.isNotEmpty()) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          product.variants.forEachIndexed { idx, variant ->
            val isSelected = idx == selectedVariantIndex
            Surface(
              color = if (isSelected) Color(0xFFE0F2FE) else Color(0xFFF8FAFC),
              shape = RoundedCornerShape(6.dp),
              border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 1.2.dp else 0.8.dp,
                color = if (isSelected) MyntraPink else Color(0xFFCBD5E1)
              ),
              modifier = Modifier
                .clickable { selectedVariantIndex = idx }
                .testTag("variant_chip_${product.id}_$idx")
            ) {
              Text(
                text = "${variant.label} ₹${variant.ourPrice.toInt()}",
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) MyntraPink else Color(0xFF334155),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
          }
        }
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = displayUnit,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF475569),
            modifier = Modifier.padding(vertical = 2.dp)
          )
          if (product.isHeavy) {
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
              color = Color(0xFFFEF3C7),
              shape = RoundedCornerShape(3.dp)
            ) {
              Text(
                text = "Heavy",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB45309),
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
              )
            }
          }
        }
      }

      // Price Row: Today MRP vs Our Price
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "₹${displayPrice.toInt()}",
          fontSize = 15.sp,
          fontWeight = FontWeight.ExtraBold,
          color = JioMartBlueDark
        )
        Spacer(modifier = Modifier.width(6.dp))
        if (displayMrp > displayPrice) {
          Text(
            text = "₹${displayMrp.toInt()}",
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            textDecoration = TextDecoration.LineThrough
          )
        }
      }

      if (displaySavings > 0) {
        Text(
          text = "${s(lang, "बचत", "Save", "সাশ্রয়")}: ₹${displaySavings.toInt()}",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = PrimaryGreen
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Action: Add Button or Stepper
      if (!product.inStock) {
        Surface(
          color = Color(0xFFF1F5F9),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = s(lang, "स्टॉक समाप्त", "Out of Stock", "স্টক শেষ"),
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 6.dp),
            textAlign = TextAlign.Center
          )
        }
      } else if (cartQuantity == 0) {
        OutlinedButton(
          onClick = { onAdd(activeVariant) },
          shape = RoundedCornerShape(8.dp),
          colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            contentColor = MyntraPink
          ),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, MyntraPink),
          modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .testTag("add_product_${product.id}")
        ) {
          Text(
            text = s(lang, "+ जोड़ें", "ADD TO BAG +", "+ যোগ করুন"),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }
      } else {
        // Stepper
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(MyntraPink, RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(26.dp)
              .clip(CircleShape)
              .clickable { onRemove(cartKey) }
              .testTag("decrease_product_${product.id}"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Remove,
              contentDescription = "Decrease",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }

          Text(
            text = "$cartQuantity",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.White
          )

          Box(
            modifier = Modifier
              .size(26.dp)
              .clip(CircleShape)
              .clickable { onAdd(activeVariant) }
              .testTag("increase_product_${product.id}"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Increase",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
    }
  }
}

@Composable
fun CategoryStoryStrip(
  selectedCategory: com.example.model.Category,
  onSelectCategory: (com.example.model.Category) -> Unit,
  categoryImages: Map<String, String> = emptyMap(),
  customCategories: List<com.example.model.CustomCategory> = emptyList(),
  selectedCustomCategoryId: String? = null,
  onSelectCustomCategory: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lang = LocalAppLanguage.current
  androidx.compose.foundation.lazy.LazyRow(
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    // 1. Custom Categories created by Admin
    items(customCategories.size, key = { "cc_${customCategories[it].id}" }) { idx ->
      val cc = customCategories[idx]
      val isSelected = selectedCustomCategoryId == cc.id
      val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ccScale"
      )
      val ccTitle = if (lang == AppLanguage.HINDI) cc.hindiName else cc.name
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .width(72.dp)
          .graphicsLayer { scaleX = scale; scaleY = scale }
          .clickable { onSelectCustomCategory(cc.id) }
      ) {
        Box(
          modifier = Modifier
            .size(60.dp)
            .border(
              width = if (isSelected) 2.5.dp else 1.2.dp,
              color = if (isSelected) Color(0xFF0284C7) else Color(0xFFBAE6FD),
              shape = CircleShape
            )
            .padding(3.dp)
            .clip(CircleShape)
            .background(Color(0xFFE0F2FE)),
          contentAlignment = Alignment.Center
        ) {
          val imgData = if (cc.imageUrl.isNotBlank()) rememberCoilData(cc.imageUrl) else DefaultProducts.fallbackImageForCategory(com.example.model.Category.ALL)
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(imgData)
              .size(160, 160)
              .precision(Precision.INEXACT)
              .crossfade(120)
              .build(),
            contentDescription = ccTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = ccTitle,
          fontSize = 10.sp,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          color = if (isSelected) Color(0xFF0284C7) else MyntraDark,
          maxLines = 2,
          textAlign = TextAlign.Center,
          lineHeight = 12.sp,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    // 2. Built-in Categories (with Admin image override support)
    items(com.example.model.Category.values().size) { index ->
      val cat = com.example.model.Category.values()[index]
      val isSelected = selectedCustomCategoryId == null && selectedCategory == cat
      val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "catScale"
      )
      val catName = cat.localizedName(lang)
      val overrideUrl = categoryImages[cat.name]
      val imgData: Any = if (!overrideUrl.isNullOrBlank()) {
        rememberCoilData(overrideUrl)
      } else {
        DefaultProducts.fallbackImageForCategory(cat)
      }
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .width(72.dp)
          .graphicsLayer { scaleX = scale; scaleY = scale }
          .clickable { onSelectCategory(cat) }
          .testTag("story_cat_${cat.name}")
      ) {
        Box(
          modifier = Modifier
            .size(60.dp)
            .border(
              width = if (isSelected) 2.5.dp else 1.dp,
              color = if (isSelected) MyntraPink else Color(0xFFE2E8F0),
              shape = CircleShape
            )
            .padding(3.dp)
            .clip(CircleShape)
            .background(Color(0xFFF5F5F6)),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(imgData)
              .size(160, 160)
              .precision(Precision.INEXACT)
              .crossfade(120)
              .build(),
            contentDescription = catName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = catName,
          fontSize = 10.sp,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          color = if (isSelected) MyntraPink else MyntraDark,
          maxLines = 2,
          textAlign = TextAlign.Center,
          lineHeight = 12.sp,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun StickyCartBottomBar(
  cartCount: Int,
  totalPrice: Double,
  deliveryFee: Double,
  totalSavings: Double,
  onOpenCart: () -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = cartCount > 0,
    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
    modifier = modifier
  ) {
    Surface(
      color = JioMartBlueDark,
      contentColor = Color.White,
      shadowElevation = 8.dp,
      shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onOpenCart() }
        .testTag("sticky_cart_bar")
    ) {
      val lang = LocalAppLanguage.current
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "$cartCount ${s(lang, "सामान", "items", "টি পণ্য")}",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              text = " • ₹${(totalPrice + deliveryFee).toInt()}",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 16.sp,
              color = SecondaryGold
            )
          }
          Text(
            text = "${s(lang, "बचत", "Saved", "সাশ্রয়")}: ₹${totalSavings.toInt()}",
            fontSize = 11.sp,
            color = Color(0xFFBAE6FD)
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Text(
            text = s(lang, "कार्ट देखें ➔", "View Cart ➔", "কার্ট দেখুন ➔"),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = JioMartBlueDark
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaPickerBottomSheet(
  currentArea: CorridorArea,
  onSelectArea: (CorridorArea) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var customLocation by remember { mutableStateOf("") }
  var isFetchingGps by remember { mutableStateOf(false) }

  fun resolveGps() {
    scope.launch {
      isFetchingGps = true
      val resolved = fetchCurrentLocationAddress(context)
      isFetchingGps = false
      if (resolved != null) {
        onSelectArea(
          CorridorArea(
            id = "gps_current",
            name = resolved.areaName,
            hindiName = resolved.fullAddress,
            landmarkHint = "📍 GPS वर्तमान लोकेशन",
            latitude = resolved.latitude,
            longitude = resolved.longitude
          )
        )
        onDismiss()
      } else {
        Toast.makeText(
          context,
          "GPS लोकेशन नहीं मिली। कृपया फोन की Location (GPS) ऑन करें या नीचे पता लिखें।",
          Toast.LENGTH_LONG
        ).show()
      }
    }
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { grants ->
    val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
      grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) {
      resolveGps()
    } else {
      Toast.makeText(context, "लोकेशन अनुमति अस्वीकार की गई", Toast.LENGTH_SHORT).show()
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 10.dp)
        .padding(bottom = 30.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.LocationOn,
          contentDescription = null,
          tint = JioMartBlue,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "डिलीवरी लोकेशन चुनें या लिखें",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Text(
            text = "अपनी वर्तमान लोकेशन चुनें या कहीं का भी नया पता लिखें",
            fontSize = 12.sp,
            color = Color.Gray
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Current GPS Location Button
      Button(
        onClick = {
          if (hasLocationPermission(context)) {
            resolveGps()
          } else {
            permissionLauncher.launch(
              arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
              )
            )
          }
        },
        enabled = !isFetchingGps,
        colors = ButtonDefaults.buttonColors(containerColor = MyntraPink),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp)
          .testTag("use_current_location_button")
      ) {
        if (isFetchingGps) {
          CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 2.dp,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("लोकेशन खोज रहे हैं...", color = Color.White, fontWeight = FontWeight.Bold)
        } else {
          Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = "Current Location",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("वर्तमान लोकेशन इस्तेमाल करें (GPS)", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Custom City / Address Input (Replace Raigarh-Punjipathra with any location)
      OutlinedTextField(
        value = customLocation,
        onValueChange = { customLocation = it },
        label = { Text("कोई और शहर, गाँव या नया पता लिखें") },
        placeholder = { Text("उदा: बिलासपुर, कोरबा, घरघोड़ा, रायगढ़...") },
        singleLine = true,
        trailingIcon = {
          if (customLocation.isNotEmpty()) {
            IconButton(onClick = { customLocation = "" }) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("custom_location_input")
      )

      if (customLocation.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
          onClick = {
            val clean = customLocation.trim()
            onSelectArea(
              CorridorArea(
                id = "custom_${clean.hashCode()}",
                name = clean,
                hindiName = clean,
                landmarkHint = "कस्टम डिलीवरी लोकेशन"
              )
            )
            onDismiss()
          },
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().testTag("apply_custom_location_button")
        ) {
          Text("\"$customLocation\" को डिलीवरी लोकेशन बनाएं ➔", fontWeight = FontWeight.Bold, color = MyntraPink)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = "प्रमुख डिलीवरी क्षेत्र:",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray
      )
      Spacer(modifier = Modifier.height(4.dp))

      DefaultProducts.defaultAreas.forEach { area ->
        val isSelected = area.id == currentArea.id
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE0F2FE) else Color(0xFFF8FAFC)
          ),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
              if (isSelected) listOf(JioMartBlue, Color(0xFF38BDF8)) else listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))
            )
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
              onSelectArea(area)
              onDismiss()
            }
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${area.name} (${area.hindiName})",
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (isSelected) JioMartBlueDark else Color(0xFF1E293B)
              )
              Text(
                text = area.landmarkHint,
                fontSize = 11.sp,
                color = Color.Gray
              )
            }
            Surface(
              color = if (isSelected) JioMartBlue else Color.White,
              shape = RoundedCornerShape(6.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) JioMartBlue else Color.LightGray)
            ) {
              Text(
                text = "8:00 AM",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else JioMartBlueDark,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductDetailSheet(
  product: Product,
  cart: Map<String, CartItem>,
  isWishlisted: Boolean,
  onToggleWishlist: () -> Unit,
  onAdd: (ProductVariant?) -> Unit,
  onRemove: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val resolvedImageUrl = product.imageUrl?.takeIf { it.isNotBlank() }
    ?: DefaultProducts.fallbackImageForCategory(product.category)
  val defaultIndex = remember(product.id, product.variants) {
    val idx = product.variants.indexOfFirst { it.label.equals(product.unit, ignoreCase = true) }
    if (idx >= 0) idx else 0
  }
  var selectedVariantIndex by remember(product.id, product.variants) {
    mutableIntStateOf(defaultIndex)
  }
  val activeVariant = product.variants.getOrNull(selectedVariantIndex)
  val displayUnit = activeVariant?.label ?: product.unit
  val displayMrp = activeVariant?.mrp ?: product.mrp
  val displayPrice = activeVariant?.ourPrice ?: product.ourPrice
  val displayDiscount = activeVariant?.discountPercent ?: product.discountPercent
  val displaySavings = activeVariant?.savings ?: product.savings
  val cartKey = "${product.id}__${displayUnit}"
  val cartQuantity = cart[cartKey]?.quantity ?: 0

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(210.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xFFF1F5F9))
      ) {
        val productModel = rememberCoilData(resolvedImageUrl)
        AsyncImage(
          model = ImageRequest.Builder(context)
            .data(productModel)
            .crossfade(true)
            .build(),
          contentDescription = product.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
        if (displayDiscount > 0) {
          Surface(
            color = PrimaryGreen,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(10.dp)
          ) {
            Text(
              text = "$displayDiscount% OFF",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
        IconButton(
          onClick = onToggleWishlist,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .background(Color.White.copy(alpha = 0.9f), CircleShape)
        ) {
          Icon(
            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "पसंदीदा",
            tint = if (isWishlisted) Color(0xFFE11D48) else Color(0xFF475569)
          )
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = product.hindiName.ifBlank { product.name },
          fontSize = 19.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF0F172A)
        )
        if (product.name.isNotBlank() && product.name != product.hindiName) {
          Text(
            text = product.name,
            fontSize = 13.sp,
            color = Color(0xFF64748B)
          )
        }
        if (product.description.isNotBlank()) {
          Text(
            text = product.description,
            fontSize = 12.sp,
            color = Color(0xFF475569),
            lineHeight = 17.sp,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }

      if (product.variants.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("वज़न / पैक चुनें:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            product.variants.forEachIndexed { idx, variant ->
              val isSel = idx == selectedVariantIndex
              Surface(
                color = if (isSel) JioMartBlue else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable { selectedVariantIndex = idx }
              ) {
                Text(
                  text = "${variant.label} · ₹${variant.ourPrice.toInt()}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSel) Color.White else Color(0xFF1E293B),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "₹${displayPrice.toInt()}",
              fontSize = 22.sp,
              fontWeight = FontWeight.Black,
              color = Color(0xFF0F172A)
            )
            if (displayMrp > displayPrice) {
              Text(
                text = "₹${displayMrp.toInt()}",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                textDecoration = TextDecoration.LineThrough
              )
            }
          }
          if (displaySavings > 0) {
            Text(
              text = "सीधी बचत ₹${displaySavings.toInt()} ($displayUnit)",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = PrimaryGreen
            )
          }
        }

        if (!product.inStock) {
          Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(10.dp)) {
            Text(
              text = "स्टॉक खत्म",
              color = TertiaryRed,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
          }
        } else if (cartQuantity == 0) {
          Button(
            onClick = { onAdd(activeVariant) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.height(48.dp)
          ) {
            Text("+ कार्ट में जोड़ें", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
        } else {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(PrimaryGreen)
          ) {
            IconButton(onClick = { onRemove(cartKey) }) {
              Text("-", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            Text(
              text = "$cartQuantity",
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 16.sp,
              modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = { onAdd(activeVariant) }) {
              Text("+", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
          }
        }
      }
    }
  }
}

