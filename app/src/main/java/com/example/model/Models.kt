package com.example.model

enum class Category(val displayName: String, val hindiName: String, val emoji: String) {
  ALL("All Items", "सभी सामान", "🛒"),
  VEGETABLES("Vegetables", "ताज़ी सब्ज़ियाँ", "🥦"),
  FRUITS("Fresh Fruits", "ताज़े फल", "🍎"),
  DAIRY_BREAD("Dairy & Bread", "दूध, दही व ब्रेड", "🥛"),
  ATTA_DALS("Atta, Rice & Dals", "आटा, दाल व चावल", "🌾"),
  OILS_GHEE("Oils & Ghee", "तेल व शुद्ध घी", "🫒"),
  MASALAS("Masalas & Spices", "मसाले व नमक", "🌶️"),
  SNACKS_MUNCHIES("Snacks & Biscuits", "नमकीन, बिस्कुट", "🍪"),
  BEVERAGES("Tea, Coffee & Drinks", "चाय, कॉफ़ी व जूस", "☕"),
  CLEANING_HOME("Cleaning & Home", "सफाई व बर्तन धुलाई", "🧼"),
  PERSONAL_CARE("Personal Care", "साबुन, कॉस्मेटिक व केयर", "🧴"),
  GIRLS_NEEDS("Girls Needs", "गर्ल्स नीड्स", "💄"),
  BOYS_NEEDS("Boys Needs", "बॉयज़ नीड्स", "🕶️"),
  BOOKS_STATIONERY("Books & Stationery", "किताबें, कॉपी व पेन", "📚"),
  POOJA_SPECIAL("Pooja & Hawan", "पूजा सामग्री व कपूर", "🪔")
}

data class CustomCategory(
  val id: String,
  val name: String,
  val hindiName: String,
  val imageUrl: String = ""
)

data class ProductVariant(
  val label: String,
  val mrp: Double,
  val ourPrice: Double
) {
  val discountPercent: Int
    get() = if (mrp > ourPrice && mrp > 0) (((mrp - ourPrice) / mrp) * 100).toInt() else 0

  val savings: Double
    get() = if (mrp > ourPrice) mrp - ourPrice else 0.0
}

data class Product(
  val id: String,
  val name: String,
  val hindiName: String,
  val category: Category,
  val unit: String,
  val mrp: Double,
  val ourPrice: Double,
  val inStock: Boolean = true,
  val tag: String? = null,
  val emoji: String = "🛒",
  val imageUrl: String? = null,
  val description: String = "",
  val isHeavy: Boolean = false,
  val variants: List<ProductVariant> = emptyList(),
  val customCategoryId: String = ""
) {
  val discountPercent: Int
    get() = if (mrp > ourPrice && mrp > 0) (((mrp - ourPrice) / mrp) * 100).toInt() else 0

  val savings: Double
    get() = if (mrp > ourPrice) mrp - ourPrice else 0.0
}

data class CartItem(
  val product: Product,
  val quantity: Int
)

enum class OrderStatus(val label: String, val hindiLabel: String, val badgeColorHex: Long) {
  RECEIVED("Order Received", "ऑर्डर मिला", 0xFF0078AD),
  MANDI_PROCUREMENT("Procuring at 5 AM", "मंडी से ताज़ा खरीद जारी", 0xFF7C3AED),
  PACKED("Packed with Guarantee", "पैक हो चुका है", 0xFFD97706),
  OUT_FOR_8AM("Out for 8 AM Delivery", "सुबह 8 बजे रास्ते में", 0xFF059669),
  DELIVERED("Delivered at Door", "घर तक पहुँचाया गया", 0xFF10B981),
  CANCELLED("Cancelled", "रद्द किया गया", 0xFFDC2626)
}

data class CorridorArea(
  val id: String,
  val name: String,
  val hindiName: String,
  val landmarkHint: String,
  val estimatedArrival: String = "8:00 AM",
  val latitude: Double = 0.0,
  val longitude: Double = 0.0
)

data class Order(
  val id: String,
  val userId: String = "",
  val customerName: String,
  val customerPhone: String,
  val customerEmail: String = "",
  val area: String,
  val addressDetail: String,
  val items: List<CartItem>,
  val subtotal: Double,
  val totalSavings: Double,
  val deliveryCharge: Double = 60.0,
  val heavyDeliveryCharge: Double = 0.0,
  val grandTotal: Double,
  val advancePaid: Double = 0.0,
  val balanceDueAtDelivery: Double = 0.0,
  val paymentMethod: String = "UPI (Advance ₹60 + Balance at 8 AM)",
  val upiTransactionRef: String = "",
  val deliveryDate: String = "Tomorrow Morning 8:00 AM Sharp",
  val createdAt: Long = System.currentTimeMillis(),
  val status: OrderStatus = OrderStatus.RECEIVED,
  val mrityunjayTrustGuarantee: Boolean = true,
  val specialNote: String = "",
  val isMyOrder: Boolean = true,
  val rating: Int = 0,
  val ratingReview: String = "",
  val deliveryTip: Double = 0.0,
  val assignedRiderId: String = "",
  val assignedRiderName: String = "",
  val assignedRiderPhone: String = "",
  val collectedPaymentMode: String = "",
  val isRiderCashSettled: Boolean = false,
  val customerLat: Double = 0.0,
  val customerLng: Double = 0.0
)

data class Rider(
  val id: String,
  val name: String,
  val phone: String,
  val email: String = "",
  val area: String = "रायगढ़ ➔ पुंजीपथरा",
  val vehicleType: String = "Bike",
  val isActive: Boolean = true,
  val createdAt: Long = System.currentTimeMillis()
)

data class CustomItemRequest(
  val id: String,
  val userId: String = "",
  val customerName: String,
  val customerPhone: String,
  val customerEmail: String = "",
  val itemName: String,
  val quantity: String,
  val notes: String = "",
  val targetArea: String = "पुंजीपथरा",
  val timestamp: Long = System.currentTimeMillis(),
  val status: String = "मृत्युंजय द्वारा समीक्षा जारी (कल सुबह 8 AM रायगढ़ से लाएंगे)"
)

data class BannerAd(
  val id: String,
  val title: String,
  val subtitle: String,
  val badge: String = "महाबचत",
  val discountTag: String = "FLAT 20% OFF",
  val isFestivalSpecial: Boolean = false,
  val isActive: Boolean = true,
  val mediaUrl: String? = null
)

data class StoreConfig(
  val shopName: String = "अपना मार्केट",
  val tagline: String = "रायगढ़ ➔ पुंजीपथरा · सुपरफास्ट होम डिलीवरी",
  val founderName: String = "मृत्युंजय विश्वास",
  val founderInitials: String = "MV",
  val founderHead: String = "100% सुरक्षित एवं विश्वसनीय सेवा",
  val founderMsg: String = "यह सेवा आपके अपने मृत्युंजय विश्वास द्वारा रायगढ़ और पुंजीपथरा क्षेत्र के लोगों की सुविधा के लिए समर्पित है। यह 100% भरोसेमंद लोकल डिलीवरी है, कोई फर्जीवाड़ा नहीं! आपके सहयोग से हम इसे और बेहतर बनाएंगे। बस हमें सेवा का अवसर दें। धन्यवाद!",
  val whatsapp: String = "8982903843",
  val upiId: String = "mukkubishwas81-2@okaxis",
  val upiName: String = "मृत्युंजय विश्वास",
  val deliveryFee: Double = 60.0,
  val heavyDeliveryFee: Double = 20.0,
  val advanceThreshold: Double = 500.0,
  val adminPin: String = "5520",
  val storeOwnerEmail: String = "mrityunjayvishwas294@gmail.com",
  val routeCorridor: String = "रायगढ़ ➔ पुंजीपथरा कॉरिडोर",
  val deliveryTiming: String = "सुबह ठीक 8:00 AM",
  val cutoffHourText: String = "रात 8:00 बजे",
  val deliveryScheduleDays: String = "प्रतिदिन (सोमवार से रविवार)",
  val launchDaysRemaining: Int = 15,
  val launchDateText: String = "15 दिन बाद रात 8:00 बजे से बुकिंग",
  val announcementText: String = "⏳ अपना मार्केट: 15 दिन बाद रात 8:00 बजे से ऑर्डर बुकिंग शुरू! रोज़ाना रात 8:00 बजे ऑर्डर ➔ अगली सुबह ठीक 8:00 AM डिलीवरी!",
  val isAcceptingOrders: Boolean = true,
  val isFestivalSaleActive: Boolean = true,
  val festivalSaleHeading: String = "🪔 त्योहार महासेल & ऑफर्स",
  val festivalSaleSubtitle: String = "मंडी रेट से भी कम दाम · सीधे रायगढ़ से पुंजीपथरा आपके द्वार",
  val festivalDiscountBadge: String = "FLAT 15% - 40% OFF",
  val welcomeMessage: String = "🙏 नमस्ते! आपका अपना मार्केट में दिल से स्वागत है। रायगढ़ से पुंजीपथरा तक हर सुबह 8:00 बजे ताज़ा, शुद्ध और भरोसेमंद सामान सीधे आपके द्वार पहुँचाना ही हमारा संकल्प है। — आपका मृत्युंजय विश्वास (MV)",
  val heroBannerUrl: String = "",
  val showHeroBanner: Boolean = true,
  val categoryImages: Map<String, String> = emptyMap(),
  val customCategories: List<CustomCategory> = emptyList()
)
