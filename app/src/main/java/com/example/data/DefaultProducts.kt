package com.example.data

import com.example.model.Category
import com.example.model.CorridorArea
import com.example.model.Product
import com.example.model.ProductVariant
import kotlin.math.roundToInt

object DefaultProducts {
  val defaultAreas = listOf(
    CorridorArea("p1", "Punjipathra Chowk", "पूँजीपथra चौक", "Near Main Market & Bus Stand"),
    CorridorArea("p2", "OP Jindal University (OPJU)", "ओ.पी. जिंदल यूनिवर्सिटी", "Main Gate & Staff Quarters"),
    CorridorArea("p3", "Taraimal Industrial Zone", "तराईमाल औद्योगिक क्षेत्र", "Near Plant Gates & Colony"),
    CorridorArea("p4", "Raigarh City (Kotra Road)", "रायगढ़ शहर (कोटरा रोड)", "Near Railway Station / Market"),
    CorridorArea("p5", "Tamnar Junction Road", "तमनार मोड़", "Colony & Commercial Complex"),
    CorridorArea("p6", "Gharghoda Road Crossing", "घरघोड़ा रोड क्रॉसिंग", "Residential Area"),
    CorridorArea("p7", "Chhal Mod & Mines Colony", "छाल मोड़ कॉलोनी", "Main Colony Gate")
  )

  fun getInitialCatalog(): List<Product> = buildRawCatalog().map { withDefaultVariants(it) }

  private fun withDefaultVariants(product: Product): Product {
    if (product.variants.isNotEmpty()) return product
    val u = product.unit.lowercase()
    return when {
      u.startsWith("1 kg") -> product.copy(
        variants = listOf(
          ProductVariant("500 gm", (product.mrp * 0.55).roundToInt().toDouble(), (product.ourPrice * 0.55).roundToInt().toDouble()),
          ProductVariant("700 gm", (product.mrp * 0.75).roundToInt().toDouble(), (product.ourPrice * 0.75).roundToInt().toDouble()),
          ProductVariant("1 kg", product.mrp, product.ourPrice)
        )
      )
      u.startsWith("500 gm") -> product.copy(
        variants = listOf(
          ProductVariant("250 gm", (product.mrp * 0.55).roundToInt().toDouble(), (product.ourPrice * 0.55).roundToInt().toDouble()),
          ProductVariant("500 gm", product.mrp, product.ourPrice),
          ProductVariant("1 kg", (product.mrp * 1.9).roundToInt().toDouble(), (product.ourPrice * 1.9).roundToInt().toDouble())
        )
      )
      u.startsWith("1 litre") -> product.copy(
        variants = listOf(
          ProductVariant("500 ml", (product.mrp * 0.55).roundToInt().toDouble(), (product.ourPrice * 0.55).roundToInt().toDouble()),
          ProductVariant("700 ml", (product.mrp * 0.75).roundToInt().toDouble(), (product.ourPrice * 0.75).roundToInt().toDouble()),
          ProductVariant("1 L", product.mrp, product.ourPrice)
        )
      )
      else -> product
    }
  }

  private fun buildRawCatalog(): List<Product> = listOf(
    // 1. VEGETABLES (Mandi Fresh at 5 AM)
    Product(
      id = "veg_1",
      name = "Fresh Red Potatoes (Aloo)",
      hindiName = "ताज़ा देसी लाल आलू",
      category = Category.VEGETABLES,
      unit = "1 kg",
      mrp = 35.0,
      ourPrice = 26.0,
      tag = "Mandi Fresh",
      emoji = "🥔",
      description = "Handpicked farm fresh red potatoes, direct from local Mandi."
    ),
    Product(
      id = "veg_2",
      name = "Nasik Onions (Pyaaz)",
      hindiName = "नासिक प्याज (ताज़ा)",
      category = Category.VEGETABLES,
      unit = "1 kg",
      mrp = 45.0,
      ourPrice = 34.0,
      tag = "Super Saver",
      emoji = "🧅",
      description = "Crisp, dry, premium grade onions with great shelf life."
    ),
    Product(
      id = "veg_3",
      name = "Hybrid Juicy Tomatoes (Tamatar)",
      hindiName = "देसी लाल पके टमाटर",
      category = Category.VEGETABLES,
      unit = "1 kg",
      mrp = 40.0,
      ourPrice = 28.0,
      tag = "Daily Deal",
      emoji = "🍅",
      description = "Firm, ripe red tomatoes ideal for curries, salads and purees."
    ),
    Product(
      id = "veg_4",
      name = "Fresh Green Chillies (Hari Mirch)",
      hindiName = "तीखी हरी मिर्च",
      category = Category.VEGETABLES,
      unit = "250 gm",
      mrp = 25.0,
      ourPrice = 16.0,
      tag = "Fresh Today",
      emoji = "🌶️",
      description = "Freshly harvested pungent green chillies."
    ),
    Product(
      id = "veg_5",
      name = "New Crop Ginger (Adrak)",
      hindiName = "ताज़ा अदरक",
      category = Category.VEGETABLES,
      unit = "250 gm",
      mrp = 35.0,
      ourPrice = 24.0,
      tag = "Direct Mandi",
      emoji = "🫚",
      description = "Clean, unwashed local spicy ginger."
    ),
    Product(
      id = "veg_6",
      name = "Desi Garlic (Lahsun)",
      hindiName = "देसी कली लहसुन",
      category = Category.VEGETABLES,
      unit = "250 gm",
      mrp = 70.0,
      ourPrice = 52.0,
      tag = "Best Seller",
      emoji = "🧄",
      description = "Flavourful small bulb desi garlic cloves."
    ),
    Product(
      id = "veg_7",
      name = "Tender Green Bhindi (Lady Finger)",
      hindiName = "ताज़ी हरी भिंडी",
      category = Category.VEGETABLES,
      unit = "500 gm",
      mrp = 35.0,
      ourPrice = 25.0,
      tag = "Morning Pluck",
      emoji = "🥬",
      description = "Small tender lady fingers, snapping fresh."
    ),
    Product(
      id = "veg_8",
      name = "Fresh Cauliflower (Phool Gobhi)",
      hindiName = "सफेद फूल गोभी",
      category = Category.VEGETABLES,
      unit = "1 Piece (~600g)",
      mrp = 40.0,
      ourPrice = 29.0,
      tag = "Farm Fresh",
      emoji = "🥦",
      description = "Compact white head cauliflower without blemish."
    ),
    Product(
      id = "veg_9",
      name = "Fresh Green Coriander (Dhaniya Patti)",
      hindiName = "खुशबूदार हरा धनिया",
      category = Category.VEGETABLES,
      unit = "200 gm",
      mrp = 20.0,
      ourPrice = 12.0,
      tag = "Fresh Morning",
      emoji = "🌿",
      description = "Aromatic fresh green coriander leaves."
    ),
    Product(
      id = "veg_10",
      name = "Sweet Green Peas (Matar)",
      hindiName = "मीठी हरी मटर",
      category = Category.VEGETABLES,
      unit = "500 gm",
      mrp = 50.0,
      ourPrice = 36.0,
      tag = "Popular",
      emoji = "🫛",
      description = "Plump tender sweet green pods."
    ),

    // 2. FRESH FRUITS
    Product(
      id = "frt_1",
      name = "Robusta Ripe Bananas (Kela)",
      hindiName = "पके ताज़े केले (दर्जन)",
      category = Category.FRUITS,
      unit = "1 Dozen (12 pcs)",
      mrp = 60.0,
      ourPrice = 44.0,
      tag = "Energy Pack",
      emoji = "🍌",
      description = "Naturally ripened sweet bananas delivered fresh at 8 AM."
    ),
    Product(
      id = "frt_2",
      name = "Shimla Royal Apples (Seb)",
      hindiName = "शिमला रॉयल लाल सेब",
      category = Category.FRUITS,
      unit = "1 kg (~4 pcs)",
      mrp = 180.0,
      ourPrice = 138.0,
      tag = "Premium Fruit",
      emoji = "🍎",
      description = "Crisp, sweet and juicy red apples."
    ),
    Product(
      id = "frt_3",
      name = "Nagpur Sweet Oranges (Santra)",
      hindiName = "नागपुर रसीला संतरा",
      category = Category.FRUITS,
      unit = "1 kg",
      mrp = 90.0,
      ourPrice = 68.0,
      tag = "Vitamin C",
      emoji = "🍊",
      description = "Thin skinned juicy oranges full of citrus sweetness."
    ),
    Product(
      id = "frt_4",
      name = "Kandhari Pomegranate (Anaar)",
      hindiName = "कंधारी मीठा अनार",
      category = Category.FRUITS,
      unit = "1 kg",
      mrp = 190.0,
      ourPrice = 145.0,
      tag = "Rich Iron",
      emoji = "🫐",
      description = "Ruby red pearls with soft sweet seeds."
    ),
    Product(
      id = "frt_5",
      name = "Sweet Ripe Papaya (Papita)",
      hindiName = "मीठा पका पपीता",
      category = Category.FRUITS,
      unit = "1 Piece (~1.2 kg)",
      mrp = 65.0,
      ourPrice = 48.0,
      tag = "Healthy Breakfast",
      emoji = "🍈",
      description = "Yellow sweet papaya, perfect morning digestion booster."
    ),

    // 3. DAIRY, BREAD & EGGS
    Product(
      id = "dry_1",
      name = "Amul Taaza Homogenised Toned Milk",
      hindiName = "अमूल ताज़ा टोंड दूध",
      category = Category.DAIRY_BREAD,
      unit = "1 Litre Pouch",
      mrp = 56.0,
      ourPrice = 54.0,
      tag = "Daily Must",
      emoji = "🥛",
      description = "Cold chilled Amul Taaza delivered daily morning at 8 AM sharp."
    ),
    Product(
      id = "dry_2",
      name = "Amul Gold Full Cream Milk",
      hindiName = "अमूल गोल्ड फुल क्रीम दूध",
      category = Category.DAIRY_BREAD,
      unit = "1 Litre Pouch",
      mrp = 68.0,
      ourPrice = 66.0,
      tag = "Thick & Rich",
      emoji = "🥛",
      description = "6% Fat 9% SNF rich milk for chai, malai and sweets."
    ),
    Product(
      id = "dry_3",
      name = "Amul Fresh Malai Paneer",
      hindiName = "अमूल फ्रेश मलाई पनीर",
      category = Category.DAIRY_BREAD,
      unit = "200 gm Block",
      mrp = 95.0,
      ourPrice = 88.0,
      tag = "Soft & Fresh",
      emoji = "🧀",
      description = "Soft, hygienic vacuum packed fresh cottage cheese."
    ),
    Product(
      id = "dry_4",
      name = "Amul Masti Dahi Pouch",
      hindiName = "अमूल मस्ती ताज़ा दही",
      category = Category.DAIRY_BREAD,
      unit = "400 gm Pouch",
      mrp = 35.0,
      ourPrice = 33.0,
      tag = "Probiotic",
      emoji = "🥣",
      description = "Thick, creamy curd set from pure pasteurized milk."
    ),
    Product(
      id = "dry_5",
      name = "Amul Salted Butter",
      hindiName = "अमूल बटर (मक्खन)",
      category = Category.DAIRY_BREAD,
      unit = "100 gm Pack",
      mrp = 60.0,
      ourPrice = 57.0,
      tag = "Utterly Butterly",
      emoji = "🧈",
      description = "Pure pasteurised butter for crisp toast and parathas."
    ),
    Product(
      id = "dry_6",
      name = "Fresh White Farm Eggs (Tray of 6)",
      hindiName = "ताज़े फार्म अंडे (6 पीस)",
      category = Category.DAIRY_BREAD,
      unit = "6 Pieces Crate",
      mrp = 48.0,
      ourPrice = 38.0,
      tag = "Daily Protein",
      emoji = "🥚",
      description = "Farm fresh graded white eggs, packed safely."
    ),
    Product(
      id = "dry_7",
      name = "Harvest Gold / Britannia Sandwich Bread",
      hindiName = "ताज़ी सैंडविच ब्रेड",
      category = Category.DAIRY_BREAD,
      unit = "400 gm Loaf",
      mrp = 45.0,
      ourPrice = 40.0,
      tag = "Baked Fresh",
      emoji = "🍞",
      description = "Soft, fluffy sandwich white bread delivered fresh."
    ),

    // 4. ATTA, RICE & DALS
    Product(
      id = "staple_1",
      name = "Aashirvaad Shudh Chakki Atta (100% MP Wheat)",
      hindiName = "आशीर्वाद शुद्ध चक्की आटा",
      category = Category.ATTA_DALS,
      unit = "5 kg Bag",
      mrp = 245.0,
      ourPrice = 208.0,
      tag = "Top Saver",
      emoji = "🌾",
      description = "Whole wheat rotis stay soft for longer. 0% Maida.",
      isHeavy = true
    ),
    Product(
      id = "staple_2",
      name = "Fortune Rozana Basmati Rice",
      hindiName = "फॉर्च्यून रोज़ाना बासमती चावल",
      category = Category.ATTA_DALS,
      unit = "5 kg Bag",
      mrp = 420.0,
      ourPrice = 335.0,
      tag = "Aromatic",
      emoji = "🍚",
      description = "Long fragrant basmati rice perfect for daily lunch and pulao.",
      isHeavy = true
    ),
    Product(
      id = "staple_3",
      name = "Tata Sampann Unpolished Toor / Arhar Dal",
      hindiName = "टाटा सम्पन्न अरहर दाल",
      category = Category.ATTA_DALS,
      unit = "1 kg Pack",
      mrp = 195.0,
      ourPrice = 162.0,
      tag = "High Protein",
      emoji = "🥣",
      description = "Unpolished toor dal rich in natural protein without artificial gloss."
    ),
    Product(
      id = "staple_4",
      name = "Desi Chana Dal (Unpolished)",
      hindiName = "देसी चना दाल",
      category = Category.ATTA_DALS,
      unit = "1 kg Pack",
      mrp = 110.0,
      ourPrice = 89.0,
      tag = "Mandi Value",
      emoji = "🫘",
      description = "Clean, stone-free desi chana dal for tadka and halwa."
    ),
    Product(
      id = "staple_5",
      name = "Green Moong Dal (Split / Chilka)",
      hindiName = "मूंग दाल छिलका",
      category = Category.ATTA_DALS,
      unit = "1 kg Pack",
      mrp = 145.0,
      ourPrice = 118.0,
      tag = "Healthy Choice",
      emoji = "🫘",
      description = "Nutritious light moong dal, easy to digest."
    ),
    Product(
      id = "staple_6",
      name = "Fortune Fine Sooji (Rawa)",
      hindiName = "फॉर्च्यून बारीक सूजी",
      category = Category.ATTA_DALS,
      unit = "500 gm",
      mrp = 40.0,
      ourPrice = 32.0,
      tag = "Fresh Pack",
      emoji = "🥣",
      description = "Golden granulated semolina for upma and halwa."
    ),
    Product(
      id = "staple_7",
      name = "Fortune Besan (Gram Flour)",
      hindiName = "फॉर्च्यून 100% चना बेसन",
      category = Category.ATTA_DALS,
      unit = "500 gm",
      mrp = 65.0,
      ourPrice = 52.0,
      tag = "Pure Chana",
      emoji = "🌾",
      description = "100% pure chana dal besan for pakodas and sweets."
    ),

    // 5. OILS & GHEE
    Product(
      id = "oil_1",
      name = "Fortune Kachi Ghani Mustard Oil (Sarson Tel)",
      hindiName = "फॉर्च्यून कच्ची घानी सरसों तेल",
      category = Category.OILS_GHEE,
      unit = "1 Litre Pouch",
      mrp = 165.0,
      ourPrice = 138.0,
      tag = "Pure Kachi Ghani",
      emoji = "🫒",
      description = "Pungent traditional mustard oil for authentic taste."
    ),
    Product(
      id = "oil_2",
      name = "Fortune Sunlite Refined Sunflower Oil",
      hindiName = "फॉर्च्यून सनलाइट रिफाइंड तेल",
      category = Category.OILS_GHEE,
      unit = "1 Litre Pouch",
      mrp = 150.0,
      ourPrice = 124.0,
      tag = "Heart Healthy",
      emoji = "🌻",
      description = "Enriched with vitamins A & D, light non-sticky oil."
    ),
    Product(
      id = "oil_3",
      name = "Amul Pure Cow Desi Ghee",
      hindiName = "अमूल शुद्ध गाय का देसी घी",
      category = Category.OILS_GHEE,
      unit = "1 Litre Carton",
      mrp = 660.0,
      ourPrice = 595.0,
      tag = "100% Pure Aroma",
      emoji = "🪔",
      description = "Granular texture and divine aroma for rotis and dal tadka."
    ),
    Product(
      id = "oil_4",
      name = "Saffola Gold Pro Healthy Oil",
      hindiName = "सफोला गोल्ड कुकिंग ऑयल",
      category = Category.OILS_GHEE,
      unit = "1 Litre Pouch",
      mrp = 185.0,
      ourPrice = 152.0,
      tag = "Cholesterol Care",
      emoji = "🫒",
      description = "Blended edible vegetable oil with natural antioxidants."
    ),

    // 6. MASALAS & SPICES
    Product(
      id = "spc_1",
      name = "Tata Salt (Desh Ka Namak) Vacuum Evaporated",
      hindiName = "टाटा नमक आयोडाइज्ड",
      category = Category.MASALAS,
      unit = "1 kg Pack",
      mrp = 28.0,
      ourPrice = 26.0,
      tag = "Everyday Essential",
      emoji = "🧂",
      description = "India's most trusted vacuum evaporated iodised salt."
    ),
    Product(
      id = "spc_2",
      name = "Everest Turmeric Powder (Haldi)",
      hindiName = "एवरेस्ट शुद्ध हल्दी पाउडर",
      category = Category.MASALAS,
      unit = "200 gm Pack",
      mrp = 65.0,
      ourPrice = 52.0,
      tag = "High Curcumin",
      emoji = "🟡",
      description = "Rich color and aroma, free from added chemicals."
    ),
    Product(
      id = "spc_3",
      name = "MDH Deggi Mirch (Natural Red Color & Heat)",
      hindiName = "एमडीएच देगी मिर्च",
      category = Category.MASALAS,
      unit = "100 gm Pack",
      mrp = 90.0,
      ourPrice = 75.0,
      tag = "Vibrant Color",
      emoji = "🌶️",
      description = "Mild heat with glorious natural red gravies."
    ),
    Product(
      id = "spc_4",
      name = "Catch Whole Cumin Seeds (Jeera)",
      hindiName = "कैच साबुत जीरा",
      category = Category.MASALAS,
      unit = "100 gm",
      mrp = 75.0,
      ourPrice = 58.0,
      tag = "High Aroma",
      emoji = "🌱",
      description = "Crisp, cleaned cumin seeds for fragrant tadka."
    ),
    Product(
      id = "spc_5",
      name = "Everest Garam Masala Powder",
      hindiName = "एवरेस्ट गरम मसाला",
      category = Category.MASALAS,
      unit = "100 gm Pack",
      mrp = 98.0,
      ourPrice = 82.0,
      tag = "Secret Spice Blend",
      emoji = "🫙",
      description = "Royal blend of 13 exotic spices for rich gravy."
    ),
    Product(
      id = "spc_6",
      name = "MDH Super Hing (Asafoetida)",
      hindiName = "एमडीएच सुपर हींग",
      category = Category.MASALAS,
      unit = "50 gm Jar",
      mrp = 110.0,
      ourPrice = 96.0,
      tag = "Strong Aroma",
      emoji = "🫙",
      description = "Pinch is enough for unforgettable tadka flavour."
    ),

    // 7. SNACKS & MUNCHIES
    Product(
      id = "snk_1",
      name = "Maggi 2-Minute Masala Noodles (Pack of 4)",
      hindiName = "मैगी 2-मिनट मसाला नूडल्स (4 पैक)",
      category = Category.SNACKS_MUNCHIES,
      unit = "280 gm Pack",
      mrp = 60.0,
      ourPrice = 52.0,
      tag = "Favorite",
      emoji = "🍜",
      description = "Original taste with goodness of iron and favorite tastemaker."
    ),
    Product(
      id = "snk_2",
      name = "Haldiram's Nagpur Aloo Bhujia",
      hindiName = "हल्दीराम नागपुर आलू भुजिया",
      category = Category.SNACKS_MUNCHIES,
      unit = "400 gm Pack",
      mrp = 120.0,
      ourPrice = 98.0,
      tag = "Crunchy Best",
      emoji = "🥨",
      description = "Spicy mint potato noodles, unmatched tea-time crunch."
    ),
    Product(
      id = "snk_3",
      name = "Parle-G Gold Glucose Biscuits",
      hindiName = "पारले-जी गोल्ड बिस्कुट",
      category = Category.SNACKS_MUNCHIES,
      unit = "1 kg Mega Pack",
      mrp = 120.0,
      ourPrice = 99.0,
      tag = "All Time Best",
      emoji = "🍪",
      description = "Crispy golden wheat and milk biscuits."
    ),
    Product(
      id = "snk_4",
      name = "Britannia Good Day Cashew Cookies",
      hindiName = "गुड डे काजू कुकीज़",
      category = Category.SNACKS_MUNCHIES,
      unit = "600 gm Multi-pack",
      mrp = 140.0,
      ourPrice = 112.0,
      tag = "Rich Butter",
      emoji = "🍪",
      description = "Abundant butter richness and crunchy cashew pieces."
    ),
    Product(
      id = "snk_5",
      name = "Lay's India's Magic Masala Chips",
      hindiName = "लेज़ मैजिक मसाला चिप्स",
      category = Category.SNACKS_MUNCHIES,
      unit = "73 gm Pack",
      mrp = 30.0,
      ourPrice = 26.0,
      tag = "Crispy",
      emoji = "🥔",
      description = "Crisp ridged potato chips coated in hot Indian spices."
    ),

    // 8. BEVERAGES & TEA
    Product(
      id = "bev_1",
      name = "Tata Tea Gold (Patti & Long Leaves)",
      hindiName = "टाटा टी गोल्ड प्रीमियम चाय",
      category = Category.BEVERAGES,
      unit = "500 gm Pack",
      mrp = 310.0,
      ourPrice = 255.0,
      tag = "Morning Bliss",
      emoji = "☕",
      description = "Exquisite combination of Assam CTC and gently rolled long leaves."
    ),
    Product(
      id = "bev_2",
      name = "Wagh Bakri Premium CTC Leaf Tea",
      hindiName = "वाघ बकरी प्रीमियम चाय",
      category = Category.BEVERAGES,
      unit = "500 gm Pack",
      mrp = 295.0,
      ourPrice = 240.0,
      tag = "Kadak Chai",
      emoji = "🫖",
      description = "Strong aroma and bold liquor for true tea lovers."
    ),
    Product(
      id = "bev_3",
      name = "Nescafe Classic 100% Pure Instant Coffee",
      hindiName = "नेस्कैफे क्लासिक कॉफ़ी",
      category = Category.BEVERAGES,
      unit = "100 gm Glass Jar",
      mrp = 350.0,
      ourPrice = 298.0,
      tag = "Rich Roast",
      emoji = "☕",
      description = "Signature dark roast instant coffee for quick energizing mornings."
    ),
    Product(
      id = "bev_4",
      name = "Cadbury Bournvita Nutrition Drink",
      hindiName = "बोर्नविटा हेल्थ ड्रिंक",
      category = Category.BEVERAGES,
      unit = "500 gm Refill",
      mrp = 255.0,
      ourPrice = 219.0,
      tag = "Immunity Boost",
      emoji = "🥛",
      description = "Inner strength formula with Vitamin D, iron and calcium."
    ),
    Product(
      id = "bev_5",
      name = "Real Fruit Power Mixed Fruit Juice",
      hindiName = "रियल मिक्स्ड फ्रूट जूस",
      category = Category.BEVERAGES,
      unit = "1 Litre Tetra",
      mrp = 130.0,
      ourPrice = 105.0,
      tag = "Refreshing",
      emoji = "🧃",
      description = "Blend of 9 delicious fruits without added preservatives."
    ),

    // 9. CLEANING & HOUSEHOLD
    Product(
      id = "cln_1",
      name = "Surf Excel Easy Wash Detergent Powder",
      hindiName = "सर्फ एक्सेल ईज़ी वॉश पाउडर",
      category = Category.CLEANING_HOME,
      unit = "1 kg Pack",
      mrp = 150.0,
      ourPrice = 126.0,
      tag = "Tough Stains",
      emoji = "🧺",
      description = "Removes tough stains easily like oil, curry and ink."
    ),
    Product(
      id = "cln_2",
      name = "Vim Lemon Dishwash Gel Bottle",
      hindiName = "विम लेमन डिशवॉश जेल",
      category = Category.CLEANING_HOME,
      unit = "750 ml Bottle",
      mrp = 195.0,
      ourPrice = 159.0,
      tag = "Grease Cutter",
      emoji = "🧼",
      description = "Power of 100 lemons. 1 spoon cleans an entire sink of utensils."
    ),
    Product(
      id = "cln_3",
      name = "Harpic Power Plus Toilet Cleaner Liquid",
      hindiName = "हार्पिक पावर प्लस टॉयलेट क्लीनर",
      category = Category.CLEANING_HOME,
      unit = "1 Litre Bottle",
      mrp = 225.0,
      ourPrice = 188.0,
      tag = "10x Cleaning",
      emoji = "🧴",
      description = "10x better stain and limescale removal."
    ),
    Product(
      id = "cln_4",
      name = "Lizol Citrus Disinfectant Floor Cleaner",
      hindiName = "लाइज़ोल फ्लोर क्लीनर",
      category = Category.CLEANING_HOME,
      unit = "1 Litre Bottle",
      mrp = 230.0,
      ourPrice = 192.0,
      tag = "Kills 99.9% Germs",
      emoji = "🧹",
      description = "Pleasant citrus fragrance with powerful germ protection."
    ),

    // 10. PERSONAL CARE
    Product(
      id = "per_1",
      name = "Dettol Original Germ Protection Bathing Soap",
      hindiName = "डेटॉल ओरिजिनल साबुन (4+1 फ्री)",
      category = Category.PERSONAL_CARE,
      unit = "Pack of 5 (125g each)",
      mrp = 240.0,
      ourPrice = 195.0,
      tag = "Value Pack",
      emoji = "🧼",
      imageUrl = "https://images.unsplash.com/photo-1600857062241-98e5dba7f214?w=400",
      description = "Trusted antiseptic germ protection for whole family."
    ),
    Product(
      id = "per_2",
      name = "Colgate Strong Teeth Dental Cream",
      hindiName = "कोलगेट स्ट्रॉन्ग टीथ टूथपेस्ट",
      category = Category.PERSONAL_CARE,
      unit = "500 gm Saver Pack",
      mrp = 295.0,
      ourPrice = 238.0,
      tag = "Amino Shakti",
      emoji = "🪥",
      imageUrl = "https://images.unsplash.com/photo-1559591937-e68214373452?w=400",
      description = "Strengthens enamel with natural calcium boost."
    ),
    Product(
      id = "per_3",
      name = "Head & Shoulders Anti-Dandruff Cool Menthol Shampoo",
      hindiName = "हेड एंड शोल्डर्स शैम्पू",
      category = Category.PERSONAL_CARE,
      unit = "340 ml Bottle",
      mrp = 390.0,
      ourPrice = 315.0,
      tag = "Cool Refresh",
      emoji = "🧴",
      imageUrl = "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400",
      description = "Menthol cooling sensation with 100% dandruff reduction."
    ),

    // GIRLS KAJAL & BEAUTY SETS
    Product(
      id = "kjl_1",
      name = "Lakme Eyeconic Kajal Classic Black (Twin Pack)",
      hindiName = "लैक्मे आइकॉनिक काजल (2 पीस सेट)",
      category = Category.PERSONAL_CARE,
      unit = "0.35g x 2 Pack",
      mrp = 370.0,
      ourPrice = 295.0,
      tag = "Girls Favourite",
      emoji = "👁️",
      imageUrl = "https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=400",
      description = "24Hr smudge-proof, waterproof matte black eye kajal twin set."
    ),
    Product(
      id = "kjl_2",
      name = "Maybelline New York Colossal Kajal 24H",
      hindiName = "मेबेलिन कोलोसल वॉटरप्रूफ काजल",
      category = Category.PERSONAL_CARE,
      unit = "0.35 gm Stick",
      mrp = 199.0,
      ourPrice = 165.0,
      tag = "Deep Black",
      emoji = "✨",
      imageUrl = "https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=400",
      description = "Aloe vera enriched super black waterproof kajal."
    ),
    Product(
      id = "kjl_3",
      name = "Plum NaturStudio All-Day-Wear Kohl Kajal + Sharpener",
      hindiName = "प्लम कोहल काजल व शार्पनर सेट",
      category = Category.PERSONAL_CARE,
      unit = "1.2 gm + Sharpener",
      mrp = 495.0,
      ourPrice = 399.0,
      tag = "Vegan & Safe",
      emoji = "💄",
      imageUrl = "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=400",
      description = "Vitamin E enriched gel-like kohl pencil with free sharpener."
    ),
    Product(
      id = "kjl_4",
      name = "Himalaya Herbals Natural Kajal (Almond Oil)",
      hindiName = "हिमालया हर्बल बादाम काजल",
      category = Category.PERSONAL_CARE,
      unit = "2.7 gm Stick",
      mrp = 160.0,
      ourPrice = 135.0,
      tag = "100% Herbal",
      emoji = "🌿",
      imageUrl = "https://images.unsplash.com/photo-1583241800698-e8ab01830a07?w=400",
      description = "Nourishes and cools eyes with Damask Rose & Almond Oil."
    ),
    Product(
      id = "kjl_5",
      name = "Faces Canada Magneteyes Kajal + Eyeliner Combo Set",
      hindiName = "फेसेस कनाडा काजल व आईलाइनर सेट",
      category = Category.PERSONAL_CARE,
      unit = "Combo Set (2 Pcs)",
      mrp = 449.0,
      ourPrice = 349.0,
      tag = "Bridal Combo",
      emoji = "💋",
      imageUrl = "https://images.unsplash.com/photo-1515688594390-b649af70d282?w=400",
      description = "Complete eye makeup set with intense black kajal & liquid liner."
    ),
    Product(
      id = "kjl_6",
      name = "Blue Heaven Intense Kajal & Mascara Gift Set",
      hindiName = "ब्लू हेवन काजल व मस्कारा सेट",
      category = Category.PERSONAL_CARE,
      unit = "Combo Pack of 2",
      mrp = 290.0,
      ourPrice = 225.0,
      tag = "Super Saver",
      emoji = "✨",
      imageUrl = "https://images.unsplash.com/photo-1571781926291-c477ebfd024b?w=400",
      description = "Daily wear waterproof kajal paired with curling mascara."
    ),
    Product(
      id = "kjl_7",
      name = "Sugar Kohl of Honour Intense Kajal",
      hindiName = "शुगर कोहल ऑफ ऑनर काजल",
      category = Category.PERSONAL_CARE,
      unit = "0.25 gm",
      mrp = 249.0,
      ourPrice = 210.0,
      tag = "12H Stay",
      emoji = "🖤",
      imageUrl = "https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=400",
      description = "Single-stroke ultra-matte black kajal."
    ),

    // FULL CONDOM & WELLNESS RANGE (Discreet 8 AM Delivery)
    Product(
      id = "cnd_1",
      name = "Durex Air Ultra Thin Condoms (10s)",
      hindiName = "ड्यूरेक्स एयर अल्ट्रा थिन (10 पीस)",
      category = Category.PERSONAL_CARE,
      unit = "Pack of 10",
      mrp = 240.0,
      ourPrice = 199.0,
      tag = "Discreet Pack",
      emoji = "🛡️",
      imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
      description = "Ultra-thin natural latex condoms for maximum sensitivity."
    ),
    Product(
      id = "cnd_2",
      name = "Durex Extra Time Climax Delay Condoms (10s)",
      hindiName = "ड्यूरेक्स एक्स्ट्रा टाइम (10 पीस)",
      category = Category.PERSONAL_CARE,
      unit = "Pack of 10",
      mrp = 250.0,
      ourPrice = 210.0,
      tag = "Long Lasting",
      emoji = "🛡️",
      imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
      description = "Specially formulated with benzocaine for longer pleasure."
    ),
    Product(
      id = "cnd_3",
      name = "Durex Mutual Climax Ribbed & Dotted (10s)",
      hindiName = "ड्यूरेक्स म्यूचुअल क्लाइमैक्स (10 पीस)",
      category = Category.PERSONAL_CARE,
      unit = "Pack of 10",
      mrp = 260.0,
      ourPrice = 219.0,
      tag = "Ribbed & Dotted",
      emoji = "🛡️",
      imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
      description = "Ribbed and dotted texture for dual stimulation."
    ),
    Product(
      id = "cnd_4",
      name = "Manforce Strawberry Flavoured Dotted Condoms (10s)",
      hindiName = "मैनफोर्स स्ट्रॉबेरी डॉटेड (10 पीस)",
      category = Category.PERSONAL_CARE,
      unit = "Pack of 10",
      mrp = 110.0,
      ourPrice = 89.0,
      tag = "Best Seller",
      emoji = "🍓",
      imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
      description = "1500+ raised dots with pleasant strawberry fragrance."
    ),
    Product(
      id = "cnd_5",
      name = "Manforce Chocolate Flavoured Condoms (10s)",
      hindiName = "मैनफोर्स चॉकलेट फ्लेवर (10 पीस)",
      category = Category.PERSONAL_CARE,
      unit = "Pack of 10",
      mrp = 110.0,
      ourPrice = 89.0,
      tag = "Chocolate",
      emoji = "🍫",
      imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
      description = "Rich chocolate aroma with extra lubricated dotted surface."
    ),
    Product(
      id = "cnd_6",
      name = "Manforce Stay Long Delay Condoms (10s)",
      hindiName = "मैनफोर्स स्टे लॉन्ग (10 पीस)",
      category = Category.PERSONAL_CARE,
      unit = "Pack of 10",
      mrp = 135.0,
      ourPrice = 110.0,
      tag = "Stay Long",
      emoji = "🛡️",
      imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
      description = "4.5% benzocaine lubricant for extended performance."
    ),
    Product(
      id = "cnd_7",
      name = "Skore Champion Dotted & Ribbed Condoms (10s)",
      hindiName = "स्कोर चैंपियन डॉटेड व रिब्ड (10 पीस)",
      category = Category.PERSONAL_CARE,
      unit = "Pack of 10",
      mrp = 120.0,
      ourPrice = 95.0,
      tag = "4-in-1 Texture",
      emoji = "🛡️",
      imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
      description = "Dots, ribs, contour and delay in one pack."
    ),
    Product(
      id = "cnd_8",
      name = "Kohinoor Xtra Dots Condoms (10s)",
      hindiName = "कोहिनूर एक्स्ट्रा डॉट्स (10 पीस)",
      category = Category.PERSONAL_CARE,
      unit = "Pack of 10",
      mrp = 100.0,
      ourPrice = 82.0,
      tag = "Classic",
      emoji = "🛡️",
      imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
      description = "Electronically tested dotted natural rubber latex."
    ),
    Product(
      id = "cnd_9",
      name = "Kamasutra Longlast Condoms (12s)",
      hindiName = "कामसूत्र लॉन्गलास्ट (12 पीस)",
      category = Category.PERSONAL_CARE,
      unit = "Pack of 12",
      mrp = 140.0,
      ourPrice = 115.0,
      tag = "Value Pack",
      emoji = "🛡️",
      imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
      description = "Super dotted contoured condoms for extra comfort."
    ),
    Product(
      id = "cnd_10",
      name = "Durex Invisible Extra Thin Super Sensitive (10s)",
      hindiName = "ड्यूरेक्स इनविज़िबल एक्स्ट्रा थिन (10 पीस)",
      category = Category.PERSONAL_CARE,
      unit = "Pack of 10",
      mrp = 299.0,
      ourPrice = 249.0,
      tag = "Ultra Thin",
      emoji = "🛡️",
      imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
      description = "Designed to maximize sensitivity while still providing high protection."
    ),
    Product(
      id = "cnd_11",
      name = "Manforce 1500 Dots Litchi Flavour Condoms (10s)",
      hindiName = "मैनफोर्स 1500 डॉट्स लीची फ्लेवर (10 पीस)",
      category = Category.PERSONAL_CARE,
      unit = "Pack of 10",
      mrp = 115.0,
      ourPrice = 92.0,
      tag = "Litchi Aroma",
      emoji = "🛡️",
      imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
      description = "1500+ raised dots with sweet litchi fragrance."
    ),
    Product(
      id = "cnd_12",
      name = "Moods All Night Long Climax Control (10s)",
      hindiName = "मूड्स ऑल नाइट लॉन्ग कंडोम (10 पीस)",
      category = Category.PERSONAL_CARE,
      unit = "Pack of 10",
      mrp = 130.0,
      ourPrice = 99.0,
      tag = "All Night",
      emoji = "🛡️",
      imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400",
      description = "Lubricated ribbed and dotted condoms for long lasting intimacy."
    ),

    // GIRLS NEEDS (गर्ल्स नीड्स - सैनिटरी पैड्स, लिप बाम, हेयर केयर, ब्यूटी)
    Product(
      id = "grl_1",
      name = "Whisper Ultra Clean XL+ Sanitary Pads (30 Pads)",
      hindiName = "व्हिस्पर अल्ट्रा क्लीन XL+ पैड्स (30 पीस)",
      category = Category.GIRLS_NEEDS,
      unit = "Jumbo Pack (30s)",
      mrp = 350.0,
      ourPrice = 289.0,
      tag = "Girls Essential",
      emoji = "🌸",
      imageUrl = "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400",
      description = "Up to 100% stain protection with odour lock gel technology."
    ),
    Product(
      id = "grl_2",
      name = "Stayfree Secure Cottony Regular Pads (40s)",
      hindiName = "स्टेफ्री सिक्योर कॉटनी पैड्स (40 पीस)",
      category = Category.GIRLS_NEEDS,
      unit = "Mega Pack (40s)",
      mrp = 240.0,
      ourPrice = 195.0,
      tag = "Soft Cotton",
      emoji = "🌸",
      imageUrl = "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400",
      description = "Cottony soft cover with extra large wings for all-day comfort."
    ),
    Product(
      id = "grl_3",
      name = "Lakme Lip Love Chapstick Strawberry Lip Balm",
      hindiName = "लैक्मे लिप लव स्ट्रॉबेरी लिप बाम",
      category = Category.GIRLS_NEEDS,
      unit = "4.5 gm Stick",
      mrp = 175.0,
      ourPrice = 142.0,
      tag = "SPF 15",
      emoji = "💄",
      imageUrl = "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=400",
      description = "Moisturizing tinted lip balm with 22-hour hydration."
    ),
    Product(
      id = "grl_4",
      name = "Garnier Skin Naturals Micellar Cleansing Water",
      hindiName = "गार्नियर माईसेलर मेकअप रिमूवर (125ml)",
      category = Category.GIRLS_NEEDS,
      unit = "125 ml Bottle",
      mrp = 225.0,
      ourPrice = 185.0,
      tag = "Gentle Clean",
      emoji = "✨",
      imageUrl = "https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=400",
      description = "Removes makeup, dirt and oil in one swipe without rinsing."
    ),
    Product(
      id = "grl_5",
      name = "Streax Professional Vitariche Gloss Hair Serum",
      hindiName = "स्ट्रीक्स हेयर सीरम (115ml)",
      category = Category.GIRLS_NEEDS,
      unit = "115 ml",
      mrp = 260.0,
      ourPrice = 215.0,
      tag = "Silky Shine",
      emoji = "💇‍♀️",
      imageUrl = "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=400",
      description = "Vitamin E & macadamia oil serum for frizz-free smooth hair."
    ),
    Product(
      id = "grl_6",
      name = "VWash Plus Intimate Hygiene Wash",
      hindiName = "वीवॉश प्लस इंटिमेट हाइजीन वॉश (100ml)",
      category = Category.GIRLS_NEEDS,
      unit = "100 ml Bottle",
      mrp = 180.0,
      ourPrice = 149.0,
      tag = "pH 3.5 Balance",
      emoji = "🧴",
      imageUrl = "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400",
      description = "Lactic acid formula for daily freshness and irritation protection."
    ),
    Product(
      id = "grl_7",
      name = "Trendy Scrunchies, Hair Clips & Bindi Combo",
      hindiName = "स्क्रंची रबर बैंड, हेयर क्लिप व बिंदी सेट",
      category = Category.GIRLS_NEEDS,
      unit = "Combo of 12 Pcs",
      mrp = 150.0,
      ourPrice = 99.0,
      tag = "Girls Combo",
      emoji = "🎀",
      imageUrl = "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=400",
      description = "Satin hair scrunchies, stylish clips and daily wear bindi pack."
    ),

    // BOYS NEEDS (बॉयज़ नीड्स - ग्रूमिंग, फेस वॉश, डिओ, शेविंग, हेयर जेल)
    Product(
      id = "boy_1",
      name = "Nivea Men Dark Spot Reduction Face Wash",
      hindiName = "नीविया मेन फेस वॉश (100g)",
      category = Category.BOYS_NEEDS,
      unit = "100 gm Tube",
      mrp = 225.0,
      ourPrice = 185.0,
      tag = "Boys Best",
      emoji = "🧼",
      imageUrl = "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=400",
      description = "Deep pore cleansing with 10x Vitamin C effect for men."
    ),
    Product(
      id = "boy_2",
      name = "Fogg Napoleon Long Lasting Body Spray",
      hindiName = "फॉग नेपोलियन बॉडी स्प्रे (150ml)",
      category = Category.BOYS_NEEDS,
      unit = "150 ml Spray",
      mrp = 275.0,
      ourPrice = 215.0,
      tag = "No Gas Deo",
      emoji = "💨",
      imageUrl = "https://images.unsplash.com/photo-1594035910387-fea47794261f?w=400",
      description = "Fresh woody fragrance that lasts up to 800 sprays."
    ),
    Product(
      id = "boy_3",
      name = "Gillette Mach3 Manual Shaving Razor + 2 Blades",
      hindiName = "जिलेट मैक-3 रेज़र व ब्लेड सेट",
      category = Category.BOYS_NEEDS,
      unit = "1 Razor + 2 Cartridges",
      mrp = 320.0,
      ourPrice = 265.0,
      tag = "Smooth Shave",
      emoji = "🪒",
      imageUrl = "https://images.unsplash.com/photo-1621607512214-68297480165e?w=400",
      description = "3-blade precision razor with lubrication strip for zero irritation."
    ),
    Product(
      id = "boy_4",
      name = "Set Wet Cool Hold Hair Styling Gel",
      hindiName = "सेट वेट कूल होल्ड हेयर जेल (100ml)",
      category = Category.BOYS_NEEDS,
      unit = "100 ml Tube",
      mrp = 110.0,
      ourPrice = 89.0,
      tag = "No Alcohol",
      emoji = "💇‍♂️",
      imageUrl = "https://images.unsplash.com/photo-1585751119414-ef2636f8aede?w=400",
      description = "Pro-Vitamin B5 hair styling gel for wet & spiky look."
    ),
    Product(
      id = "boy_5",
      name = "Beardo Beard & Mustache Growth Oil",
      hindiName = "बियर्डो दाढ़ी व मूंछ ग्रोथ ऑयल (30ml)",
      category = Category.BOYS_NEEDS,
      unit = "30 ml Dropper",
      mrp = 350.0,
      ourPrice = 279.0,
      tag = "Men's Grooming",
      emoji = "🧔",
      imageUrl = "https://images.unsplash.com/photo-1621607512214-68297480165e?w=400",
      description = "Sesame, Hibiscus & Rose oil blend for thicker beard nourishment."
    ),
    Product(
      id = "boy_6",
      name = "Wild Stone Edge Long Lasting Eau De Parfum",
      hindiName = "वाइल्ड स्टोन एज परफ्यूम (50ml)",
      category = Category.BOYS_NEEDS,
      unit = "50 ml Bottle",
      mrp = 399.0,
      ourPrice = 299.0,
      tag = "Party Scent",
      emoji = "✨",
      imageUrl = "https://images.unsplash.com/photo-1594035910387-fea47794261f?w=400",
      description = "Crisp citrus and cedarwood EDP perfume for men."
    ),
    Product(
      id = "boy_7",
      name = "Nivea Men Shaving Foam + After Shave Lotion Combo",
      hindiName = "नीविया शेविंग फोम व आफ्टर शेव कॉम्बो",
      category = Category.BOYS_NEEDS,
      unit = "Combo Pack (2 Pcs)",
      mrp = 440.0,
      ourPrice = 355.0,
      tag = "Value Combo",
      emoji = "🧴",
      imageUrl = "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=400",
      description = "Cooling shaving foam paired with soothing after-shave splash."
    ),

    // 11. BOOKS, NOTEBOOKS & PEN LIST (किताबें, कॉपी व पेन)
    Product(
      id = "bk_1",
      name = "Classmate Long Notebook Ruled (Pack of 6)",
      hindiName = "क्लासमेट लॉन्ग कॉपी (6 पीस सेट)",
      category = Category.BOOKS_STATIONERY,
      unit = "172 Pages x 6",
      mrp = 360.0,
      ourPrice = 295.0,
      tag = "School Essential",
      emoji = "📓",
      imageUrl = "https://images.unsplash.com/photo-1531346878377-a5be20888e57?w=400",
      description = "Smooth, bright eco-friendly paper long notebooks."
    ),
    Product(
      id = "bk_2",
      name = "Classmate Pulse A4 Spiral Notebook (300 Pages)",
      hindiName = "क्लासमेट पल्स स्पाइरल रजिस्टर A4",
      category = Category.BOOKS_STATIONERY,
      unit = "300 Pages",
      mrp = 180.0,
      ourPrice = 148.0,
      tag = "College Hit",
      emoji = "📒",
      imageUrl = "https://images.unsplash.com/photo-1517842645767-c639042777db?w=400",
      description = "Heavy-duty spiral binding with 6-subject divider."
    ),
    Product(
      id = "bk_3",
      name = "Navneet Youva Softbound Register (Pack of 4)",
      hindiName = "नवनीत युवा मोटा रजिस्टर (4 पीस)",
      category = Category.BOOKS_STATIONERY,
      unit = "240 Pages x 4",
      mrp = 320.0,
      ourPrice = 260.0,
      tag = "Best Value",
      emoji = "📘",
      imageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400",
      description = "Durable softbound registers for students & office."
    ),
    Product(
      id = "bk_4",
      name = "Cello Butterflow Ball Pen (Pack of 10 Blue)",
      hindiName = "सेलो बटरफ्लो बॉल पेन (10 पेन सेट)",
      category = Category.BOOKS_STATIONERY,
      unit = "Pack of 10",
      mrp = 120.0,
      ourPrice = 95.0,
      tag = "Smooth Writing",
      emoji = "🖊️",
      imageUrl = "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=400",
      description = "Lubricated ink system for effortless fast writing."
    ),
    Product(
      id = "bk_5",
      name = "Reynolds Trimax Liquid Gel Pen (Pack of 3 + Refill)",
      hindiName = "रेनॉल्ड्स ट्राइमैक्स जेल पेन सेट",
      category = Category.BOOKS_STATIONERY,
      unit = "3 Pens + 2 Refills",
      mrp = 210.0,
      ourPrice = 175.0,
      tag = "Waterproof Ink",
      emoji = "✒️",
      imageUrl = "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=400",
      description = "0.5mm precision tip gel pen with smudge-free ink."
    ),
    Product(
      id = "bk_6",
      name = "Hauser XO Ball Pen Jar (Pack of 20)",
      hindiName = "हॉसर XO बॉल पेन जार (20 पेन)",
      category = Category.BOOKS_STATIONERY,
      unit = "20 Pens (Blue/Black)",
      mrp = 200.0,
      ourPrice = 160.0,
      tag = "Exam Special",
      emoji = "🖊️",
      imageUrl = "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=400",
      description = "Acu-flow German ink technology for neat handwriting."
    ),
    Product(
      id = "bk_7",
      name = "Parker Vector Standard Roller Ball Gift Pen",
      hindiName = "पार्कर वेक्टर गिफ्ट पेन",
      category = Category.BOOKS_STATIONERY,
      unit = "1 Pen + Box",
      mrp = 450.0,
      ourPrice = 375.0,
      tag = "Premium Gift",
      emoji = "🖋️",
      imageUrl = "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=400",
      description = "Classic stainless steel clip Parker pen in gift box."
    ),
    Product(
      id = "bk_8",
      name = "Apsara Platinum Extra Dark Pencils & Geometry Kit",
      hindiName = "अप्सरा पेंसिल, रबड़, शार्पनर व स्केल किट",
      category = Category.BOOKS_STATIONERY,
      unit = "10 Pencils + Kit",
      mrp = 110.0,
      ourPrice = 88.0,
      tag = "Kids Combo",
      emoji = "✏️",
      imageUrl = "https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=400",
      description = "Complete school pencil pack with dust-free eraser & sharpener."
    ),
    Product(
      id = "bk_9",
      name = "Camlin Scholar Mathematical Geometry Box",
      hindiName = "कैमलिन स्कॉलर ज्योमेट्री बॉक्स",
      category = Category.BOOKS_STATIONERY,
      unit = "1 Complete Box",
      mrp = 175.0,
      ourPrice = 145.0,
      tag = "Board Exam",
      emoji = "📐",
      imageUrl = "https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=400",
      description = "Rust-free metal compass, divider, protractor & 15cm scale."
    ),
    Product(
      id = "bk_10",
      name = "Lucent's General Knowledge (सामान्य ज्ञान) Book",
      hindiName = "ल्यूसेंट सामान्य ज्ञान किताब (हिंदी)",
      category = Category.BOOKS_STATIONERY,
      unit = "Latest Edition",
      mrp = 260.0,
      ourPrice = 210.0,
      tag = "Competitive Exam",
      emoji = "📚",
      imageUrl = "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=400",
      description = "Essential Hindi GK book for SSC, CGPSC, Railway & Police exams."
    ),
    Product(
      id = "bk_11",
      name = "Hindi Vyakaran & English Grammar Combo Books",
      hindiName = "हिंदी व्याकरण व इंग्लिश ग्रामर किताब सेट",
      category = Category.BOOKS_STATIONERY,
      unit = "Set of 2 Books",
      mrp = 340.0,
      ourPrice = 265.0,
      tag = "Student Combo",
      emoji = "📖",
      imageUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400",
      description = "Easy-to-understand grammar reference books for classes 6 to 12."
    ),
    Product(
      id = "bk_12",
      name = "Camel Oil Pastels (25 Shades) + Drawing Book",
      hindiName = "कैमल ऑइल पेस्टल कलर व ड्राइंग कॉपी",
      category = Category.BOOKS_STATIONERY,
      unit = "25 Shades + Book",
      mrp = 165.0,
      ourPrice = 132.0,
      tag = "Art & Craft",
      emoji = "🎨",
      imageUrl = "https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=400",
      description = "Bright, non-toxic oil pastels with A4 drawing book."
    ),
    Product(
      id = "bk_13",
      name = "Neon Highlighter Markers (5 Pcs) + Sticky Notes",
      hindiName = "हाईलाइटर पेन (5 रंग) व स्टिकी नोट्स",
      category = Category.BOOKS_STATIONERY,
      unit = "Combo Pack",
      mrp = 150.0,
      ourPrice = 115.0,
      tag = "Study Aid",
      emoji = "🖍️",
      imageUrl = "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=400",
      description = "Chisel-tip fluorescent highlighters & 400-sheet sticky pad."
    ),

    // 12. POOJA SPECIAL
    Product(
      id = "pja_1",
      name = "Cycle Pure Agarbatti (Three in One)",
      hindiName = "साइकिल प्योर अगरबत्ती (3 इन 1)",
      category = Category.POOJA_SPECIAL,
      unit = "Pack of 2 (154 sticks)",
      mrp = 110.0,
      ourPrice = 90.0,
      tag = "Divine Fragrance",
      emoji = "🪔",
      imageUrl = "https://images.unsplash.com/photo-1602523961358-f9f03dd557db?w=400",
      description = "Traditional hand rolled devotional incense sticks."
    ),
    Product(
      id = "pja_2",
      name = "Mangalam Bhimseni Pure Camphor (Kapoor)",
      hindiName = "मंगलम भीमसेनी शुद्ध कपूर",
      category = Category.POOJA_SPECIAL,
      unit = "100 gm Jar",
      mrp = 180.0,
      ourPrice = 145.0,
      tag = "100% Pure",
      emoji = "✨",
      imageUrl = "https://images.unsplash.com/photo-1602523961358-f9f03dd557db?w=400",
      description = "Natural crystal camphor leaves zero ash or residue."
    ),
    Product(
      id = "pja_3",
      name = "Pure Cotton Round Diya Batti (Phool Batti)",
      hindiName = "शुद्ध रुई फूल बत्ती (गोल)",
      category = Category.POOJA_SPECIAL,
      unit = "Pack of 150 Batti",
      mrp = 60.0,
      ourPrice = 42.0,
      tag = "Long Burning",
      emoji = "🕯️",
      imageUrl = "https://images.unsplash.com/photo-1602523961358-f9f03dd557db?w=400",
      description = "Made from clean combed cotton, burns steadily."
    )
  )

  fun fallbackImageForCategory(category: Category): String = when (category) {
    Category.VEGETABLES -> "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400"
    Category.FRUITS -> "https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=400"
    Category.DAIRY_BREAD -> "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400"
    Category.ATTA_DALS -> "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400"
    Category.OILS_GHEE -> "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400"
    Category.MASALAS -> "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=400"
    Category.SNACKS_MUNCHIES -> "https://images.unsplash.com/photo-1599490659213-e2b9527bd087?w=400"
    Category.BEVERAGES -> "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=400"
    Category.CLEANING_HOME -> "https://images.unsplash.com/photo-1585421514738-01798e348b17?w=400"
    Category.PERSONAL_CARE -> "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400"
    Category.GIRLS_NEEDS -> "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=400"
    Category.BOYS_NEEDS -> "https://images.unsplash.com/photo-1594035910387-fea47794261f?w=400"
    Category.BOOKS_STATIONERY -> "https://images.unsplash.com/photo-1531346878377-a5be20888e57?w=400"
    Category.POOJA_SPECIAL -> "https://images.unsplash.com/photo-1602523961358-f9f03dd557db?w=400"
    Category.ALL -> "https://images.unsplash.com/photo-1542838132-92c53300491e?w=400"
  }
}
