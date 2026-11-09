package com.example.base

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
abstract class FirestoreEmulatorTestBase {

  protected lateinit var context: Application
  protected lateinit var auth: FirebaseAuth
  protected lateinit var db: FirebaseFirestore

  companion object {
    private const val EMU_HOST = "127.0.0.1"
    private const val FIRESTORE_PORT = 8085
    private const val AUTH_PORT = 9099
    private var emulatorConfigured = false
  }

  @Before
  open fun setUpEmulator() {
    context = ApplicationProvider.getApplicationContext()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    val databaseId = context.getString(R.string.firestore_database_id)
    auth = Firebase.auth
    db = FirebaseFirestore.getInstance(databaseId)

    if (!emulatorConfigured) {
      auth.useEmulator(EMU_HOST, AUTH_PORT)
      db.useEmulator(EMU_HOST, FIRESTORE_PORT)
      db.firestoreSettings = FirebaseFirestoreSettings.Builder()
        .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
        .build()
      emulatorConfigured = true
    }

    val projectId = FirebaseApp.getInstance().options.projectId ?: "gen-lang-client-0398695106"
    clearAuthEmulator(projectId)
    clearFirestoreEmulator(projectId, databaseId)
    auth.signOut()
  }

  protected suspend fun createAndSignInTestUser(
    email: String,
    password: String = "TestPass123!"
  ): FirebaseUser {
    auth.signOut()
    val res = auth.createUserWithEmailAndPassword(email, password).await()
    return res.user ?: error("Failed to create test user $email")
  }

  private fun clearAuthEmulator(projectId: String) {
    val url = URL("http://$EMU_HOST:$AUTH_PORT/emulator/v1/projects/$projectId/accounts")
    (url.openConnection() as HttpURLConnection).run {
      requestMethod = "DELETE"
      connectTimeout = 3000
      readTimeout = 3000
      responseCode
      disconnect()
    }
  }

  protected fun runEmulatorTest(timeoutMs: Long = 15_000L, block: suspend () -> Unit) {
    val looper = org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper())
    var failure: Throwable? = null
    val done = java.util.concurrent.atomic.AtomicBoolean(false)
    val thread = Thread {
      try {
        kotlinx.coroutines.runBlocking {
          block()
        }
      } catch (t: Throwable) {
        failure = t
      } finally {
        done.set(true)
      }
    }
    thread.start()
    val deadline = System.currentTimeMillis() + timeoutMs
    while (!done.get() && System.currentTimeMillis() < deadline) {
      looper.idle()
      Thread.sleep(15)
    }
    if (!done.get()) {
      thread.interrupt()
      throw AssertionError("Test timed out after ${timeoutMs}ms")
    }
    failure?.let { throw it }
  }

  private fun clearFirestoreEmulator(projectId: String, databaseId: String) {
    val url = URL(
      "http://$EMU_HOST:$FIRESTORE_PORT/emulator/v1/projects/$projectId/databases/$databaseId/documents"
    )
    (url.openConnection() as HttpURLConnection).run {
      requestMethod = "DELETE"
      connectTimeout = 3000
      readTimeout = 3000
      responseCode
      disconnect()
    }
  }
}
