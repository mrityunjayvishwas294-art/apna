package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.ui.theme.MyntraDark
import com.example.ui.theme.MyntraPink
import com.example.ui.theme.SecondaryGold
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private tailrec fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}

@Composable
fun AuthScreen(
  onSignInSuccess: (FirebaseUser) -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  suspend fun signInWithIdToken(idToken: String) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    val result = Firebase.auth.signInWithCredential(credential).await()
    result.user?.let(onSignInSuccess)
  }

  LaunchedEffect(Unit) {
    val activity = context.findActivity() ?: return@LaunchedEffect
    try {
      val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(context.getString(R.string.default_web_client_id))
        .setAutoSelectEnabled(true)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val response = credentialManager.getCredential(activity, request)
      val cred = response.credential
      if (cred is CustomCredential &&
        cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
      ) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(cred.data)
        signInWithIdToken(googleIdTokenCredential.idToken)
      }
    } catch (_: NoCredentialException) {
      // No saved account yet — user will tap Sign-In button
    } catch (_: GetCredentialCancellationException) {
      // User dismissed silent prompt
    } catch (_: Exception) {
      // Ignore silent sign-in failures; interactive button remains available
    }
  }

  fun launchGoogleSignIn() {
    val activity = context.findActivity()
    if (activity == null) {
      errorMessage = "साइन-इन के लिए Activity उपलब्ध नहीं है।"
      return
    }
    scope.launch {
      isLoading = true
      errorMessage = null
      try {
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
          context.getString(R.string.default_web_client_id)
        ).build()

        val request = GetCredentialRequest.Builder()
          .addCredentialOption(signInWithGoogleOption)
          .build()

        val result = credentialManager.getCredential(activity, request)
        val cred = result.credential
        if (cred is CustomCredential &&
          cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
          val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(cred.data)
          signInWithIdToken(googleIdTokenCredential.idToken)
        } else {
          errorMessage = "अमान्य Google क्रेडेंशियल प्राप्त हुआ।"
        }
      } catch (_: GetCredentialCancellationException) {
        errorMessage = "साइन-इन रद्द किया गया। कृपया फिर से प्रयास करें।"
      } catch (_: NoCredentialException) {
        errorMessage = "डिवाइस पर कोई Google खाता नहीं मिला। कृपया पहले Google खाता जोड़ें।"
      } catch (e: GoogleIdTokenParsingException) {
        errorMessage = "Google टोकन पार्स करने में त्रुटि: ${e.localizedMessage ?: "अज्ञात"}"
      } catch (e: GetCredentialException) {
        errorMessage = "साइन-इन विफल: ${e.localizedMessage ?: "कृपया इंटरनेट जांचें"}"
      } catch (e: Exception) {
        errorMessage = "लॉगिन त्रुटि: ${e.localizedMessage ?: "कृपया पुनः प्रयास करें"}"
      } finally {
        isLoading = false
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(MyntraDark, Color(0xFF1A1F33))
        )
      )
      .statusBarsPadding()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 420.dp)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .background(
              Brush.radialGradient(listOf(SecondaryGold, MyntraPink)),
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "MV",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
          )
        }

        Text(
          text = "अपना मार्केट",
          fontSize = 24.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MyntraDark,
          textAlign = TextAlign.Center
        )

        Text(
          text = "रायगढ़ ➔ पुंजीपथरा · सुबह 8:00 AM डिलीवरी",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = MyntraPink,
          textAlign = TextAlign.Center
        )

        Surface(
          color = Color(0xFFF8FAFC),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "लाइव ऑर्डर ट्रैकिंग और सुरक्षित शॉपिंग के लिए Google से साइन-इन करें।",
              fontSize = 13.sp,
              color = Color(0xFF334155),
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )
            Text(
              text = "• आपके ऑर्डर केवल आपको ही दिखेंगे\n• नया सामान और रेट लाइव अपडेट होंगे",
              fontSize = 12.sp,
              color = Color(0xFF64748B)
            )
          }
        }

        if (errorMessage != null) {
          Surface(
            color = Color(0xFFFEF2F2),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = errorMessage!!,
              color = Color(0xFFB91C1C),
              fontSize = 12.sp,
              modifier = Modifier.padding(10.dp),
              textAlign = TextAlign.Center
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
          onClick = { launchGoogleSignIn() },
          enabled = !isLoading,
          colors = ButtonDefaults.buttonColors(containerColor = MyntraPink),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("google_sign_in_button")
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              color = Color.White,
              strokeWidth = 2.5.dp,
              modifier = Modifier.size(22.dp)
            )
          } else {
            Icon(
              imageVector = Icons.Default.VerifiedUser,
              contentDescription = "Google Sign In",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
              text = "Google से साइन-इन करें",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }
    }
  }
}

suspend fun signOutGoogle(context: Context) {
  Firebase.auth.signOut()
  try {
    CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
  } catch (_: Exception) {
  }
}
