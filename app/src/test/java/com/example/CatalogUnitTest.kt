package com.example

import com.example.data.DefaultProducts
import com.example.model.BannerAd
import com.example.model.Category
import com.example.model.Order
import com.example.model.Product
import com.example.model.StoreConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogUnitTest {
  @Test
  fun testInitialCatalogContainsAllCategoriesAndNewNeeds() {
    val catalog = DefaultProducts.getInitialCatalog()
    assertTrue("Catalog should contain expanded items", catalog.size >= 60)

    val categories = catalog.map { it.category }.distinct()
    assertTrue(categories.contains(Category.VEGETABLES))
    assertTrue(categories.contains(Category.DAIRY_BREAD))
    assertTrue(categories.contains(Category.ATTA_DALS))
    assertTrue(categories.contains(Category.OILS_GHEE))
    assertTrue(categories.contains(Category.GIRLS_NEEDS))
    assertTrue(categories.contains(Category.BOYS_NEEDS))
    assertTrue(categories.contains(Category.BOOKS_STATIONERY))

    val girlsAndBoys = catalog.filter {
      it.category == Category.GIRLS_NEEDS || it.category == Category.BOYS_NEEDS
    }
    assertTrue("Girls & Boys needs should have items", girlsAndBoys.size >= 14)
    assertTrue("Every Girls & Boys item should have an imageUrl", girlsAndBoys.all { !it.imageUrl.isNullOrBlank() })
  }

  @Test
  fun testDiscountAndSavingsCalculation() {
    val product = Product(
      id = "test_1",
      name = "Test Item",
      hindiName = "टेस्ट आइटम",
      category = Category.VEGETABLES,
      unit = "1 kg",
      mrp = 100.0,
      ourPrice = 80.0,
      imageUrl = "https://example.com/image.jpg"
    )

    assertEquals(20, product.discountPercent)
    assertEquals(20.0, product.savings, 0.01)
    assertEquals("https://example.com/image.jpg", product.imageUrl)
  }

  @Test
  fun testCorridorAreasIncludePunjipathraAndRaigarh() {
    val areas = DefaultProducts.defaultAreas
    val names = areas.map { it.name }.joinToString()
    assertTrue(names.contains("Punjipathra"))
    assertTrue(names.contains("Raigarh"))
    assertTrue(names.contains("OP Jindal"))
  }

  @Test
  fun testStoreConfigMatchesApnaMarketSpecification() {
    val config = StoreConfig()
    assertEquals("अपना मार्केट", config.shopName)
    assertEquals("मृत्युंजय विश्वास", config.founderName)
    assertEquals("MV", config.founderInitials)
    assertEquals("8982903843", config.whatsapp)
    assertEquals("mukkubishwas81-2@okaxis", config.upiId)
    assertEquals(60.0, config.deliveryFee, 0.01)
    assertEquals(20.0, config.heavyDeliveryFee, 0.01)
    assertEquals("5520", config.adminPin)
    assertTrue("Festival sale should be enabled by default", config.isFestivalSaleActive)
    assertTrue("Welcome message should not be blank", config.welcomeMessage.isNotBlank())
  }

  @Test
  fun testBannerAdDataModelWithGifMedia() {
    val banner = BannerAd(
      id = "b1",
      title = "त्योहार महासेल",
      subtitle = "पूजा सामग्री व घी",
      badge = "सेल",
      discountTag = "25% OFF",
      isFestivalSpecial = true,
      isActive = true,
      mediaUrl = "https://example.com/offer.gif"
    )
    assertEquals("त्योहार महासेल", banner.title)
    assertTrue(banner.isFestivalSpecial)
    assertTrue(banner.isActive)
    assertEquals("https://example.com/offer.gif", banner.mediaUrl)
  }

  @Test
  fun testOrderRatingAndDeliveryTipDefaultsAndCopy() {
    val order = Order(
      id = "AM-9999",
      customerName = "अमित वर्मा",
      customerPhone = "9876543210",
      area = "पुंजीपथरा",
      addressDetail = "मेन मार्केट",
      items = emptyList(),
      subtotal = 300.0,
      totalSavings = 50.0,
      grandTotal = 360.0,
      rating = 5,
      ratingReview = "बहुत बढ़िया सेवा",
      deliveryTip = 20.0
    )
    assertEquals(5, order.rating)
    assertEquals("बहुत बढ़िया सेवा", order.ratingReview)
    assertEquals(20.0, order.deliveryTip, 0.01)
  }

  @Test
  fun testProductWeightAndVolumeVariants() {
    val catalog = DefaultProducts.getInitialCatalog()
    val potato = catalog.first { it.id == "veg_1" }
    val labels = potato.variants.map { it.label }
    assertTrue("1 kg item should have 500 gm, 700 gm, 1 kg", labels.containsAll(listOf("500 gm", "700 gm", "1 kg")))
    assertTrue("Each variant should have distinct price", potato.variants.map { it.ourPrice }.distinct().size == 3)

    val milk = catalog.first { it.id == "dry_1" }
    val milkLabels = milk.variants.map { it.label }
    assertTrue("1 Litre item should have 500 ml, 700 ml, 1 L", milkLabels.containsAll(listOf("500 ml", "700 ml", "1 L")))
  }
}
