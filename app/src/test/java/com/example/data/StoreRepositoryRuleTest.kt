package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.CartItem
import com.example.model.Category
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.ProductVariant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

  private fun newRepo(): StoreRepository = StoreRepository(
    context = context,
    firestore = db,
    auth = auth
  )

  @Test
  fun customer_can_create_read_and_rate_own_order_but_cannot_read_other_users_order() = runEmulatorTest {
    val userA = createAndSignInTestUser("custA_test@example.com")
    val repo = newRepo()

    val sampleProduct = Product(
      id = "veg_1",
      name = "Potato",
      hindiName = "आलू",
      category = Category.VEGETABLES,
      unit = "1 kg",
      mrp = 40.0,
      ourPrice = 30.0,
      variants = listOf(ProductVariant("500 gm", 20.0, 15.0))
    )

    val order = Order(
      id = "AM-5001",
      userId = userA.uid,
      customerName = "Ramesh",
      customerPhone = "9876543210",
      area = "पुंजीपथरा",
      addressDetail = "Main Chowk",
      items = listOf(CartItem(sampleProduct, 2)),
      subtotal = 60.0,
      totalSavings = 20.0,
      deliveryCharge = 60.0,
      grandTotal = 120.0,
      status = OrderStatus.RECEIVED
    )

    repo.createOrderRemote(order)

    val fetched = repo.getOrderRemote("AM-5001")
    assertNotNull(fetched)
    assertEquals("Ramesh", fetched?.customerName)

    repo.rateOrderRemote("AM-5001", rating = 5, review = "Super fresh", tip = 20.0)
    val rated = repo.getOrderRemote("AM-5001")
    assertEquals(5, rated?.rating)
    assertEquals(20.0, rated?.deliveryTip ?: 0.0, 0.01)

    // Switch to User B: reading User A's order must fail with SecurityException
    createAndSignInTestUser("custB_test@example.com")
    var denied = false
    try {
      repo.getOrderRemote("AM-5001")
    } catch (_: SecurityException) {
      denied = true
    }
    assertTrue("Expected SecurityException when User B reads User A's order", denied)
  }

  @Test
  fun customer_cannot_write_products_without_admin_privileges() = runEmulatorTest {
    createAndSignInTestUser("regular_cust@example.com")
    val repo = newRepo()

    val unauthorizedProduct = Product(
      id = "prod_hack",
      name = "Unauthorized Item",
      hindiName = "अनाधिकृत",
      category = Category.VEGETABLES,
      unit = "1 kg",
      mrp = 100.0,
      ourPrice = 10.0
    )

    var denied = false
    try {
      repo.upsertProductRemote(unauthorizedProduct)
    } catch (_: SecurityException) {
      denied = true
    }
    assertTrue("Expected SecurityException for non-admin product write", denied)
  }

  @Test
  fun unauthenticated_read_is_denied_by_security_rules() = runEmulatorTest {
    auth.signOut()
    val repo = newRepo()

    var denied = false
    try {
      withTimeout(5000) {
        repo.observeProductsRemote().first()
      }
    } catch (_: Exception) {
      denied = true
    }
    assertTrue("Expected unauthenticated product read to fail", denied)
  }
}
