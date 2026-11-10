package com.example.data

import androidx.compose.runtime.compositionLocalOf
import com.example.model.Category
import com.example.model.Product

enum class AppLanguage(val code: String, val label: String, val shortLabel: String) {
  HINDI("hi", "हिंदी", "हिं"),
  ENGLISH("en", "English", "EN"),
  BANGLA("bn", "বাংলা", "বাং");

  companion object {
    fun fromCode(code: String?): AppLanguage =
      entries.firstOrNull { it.code == code } ?: HINDI
  }
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.HINDI }

fun s(lang: AppLanguage, hi: String, en: String, bn: String): String = when (lang) {
  AppLanguage.HINDI -> hi
  AppLanguage.ENGLISH -> en
  AppLanguage.BANGLA -> bn
}

fun Category.localizedName(lang: AppLanguage): String = when (lang) {
  AppLanguage.HINDI -> hindiName
  AppLanguage.ENGLISH -> displayName
  AppLanguage.BANGLA -> when (this) {
    Category.ALL -> "সব সামগ্রী"
    Category.VEGETABLES -> "তাজা সবজি"
    Category.FRUITS -> "তাজা ফল"
    Category.DAIRY_BREAD -> "দুধ, দই ও রুটি"
    Category.ATTA_DALS -> "আটা, ডাল ও চাল"
    Category.OILS_GHEE -> "তেল ও খাঁটি ঘি"
    Category.MASALAS -> "মসলা ও নুন"
    Category.SNACKS_MUNCHIES -> "নমকিন ও বিস্কুট"
    Category.BEVERAGES -> "চা, কফি ও পানীয়"
    Category.CLEANING_HOME -> "পরিষ্কার ও ঘরোয়া"
    Category.PERSONAL_CARE -> "সাবান ও পার্সোনাল কেয়ার"
    Category.GIRLS_NEEDS -> "মেয়েদের সামগ্রী"
    Category.BOYS_NEEDS -> "ছেলেদের সামগ্রী"
    Category.BOOKS_STATIONERY -> "বই, খাতা ও কলম"
    Category.POOJA_SPECIAL -> "পূজা ও হবন সামগ্রী"
  }
}

fun Product.localizedTitle(lang: AppLanguage): String = when (lang) {
  AppLanguage.HINDI -> hindiName.ifBlank { name }
  AppLanguage.ENGLISH -> name.ifBlank { hindiName }
  AppLanguage.BANGLA -> name.ifBlank { hindiName }
}
