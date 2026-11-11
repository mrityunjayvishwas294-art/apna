package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.DefaultProducts
import com.example.model.Category
import com.example.model.StoreConfig

@Composable
fun CategoryManagerTab(
  storeConfig: StoreConfig,
  onUpdateCategoryImage: (String, String) -> Unit,
  onAddCustomCategory: (String, String, String) -> Unit,
  onDeleteCustomCategory: (String) -> Unit
) {
  val context = LocalContext.current
  var targetCategoryKeyForCrop by remember { mutableStateOf<String?>(null) }
  var pendingCropUri by remember { mutableStateOf<Uri?>(null) }
  var showAddCustomDialog by remember { mutableStateOf(false) }

  val categoryImagePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) pendingCropUri = uri
  }

  pendingCropUri?.let { uri ->
    val key = targetCategoryKeyForCrop
    if (key != null) {
      ImageCropDialog(
        sourceUri = uri,
        defaultWideAspect = false,
        onDismiss = {
          pendingCropUri = null
          targetCategoryKeyForCrop = null
        },
        onCropped = { encoded ->
          onUpdateCategoryImage(key, encoded)
          pendingCropUri = null
          targetCategoryKeyForCrop = null
          Toast.makeText(context, "कैटेगरी फ़ोटो अपडेट हो गई!", Toast.LENGTH_SHORT).show()
        }
      )
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Button(
        onClick = { showAddCustomDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(6.dp))
        Text("नई कैटेगरी जोड़ें (Add New Category)", fontWeight = FontWeight.Bold, color = Color.White)
      }
    }

    if (storeConfig.customCategories.isNotEmpty()) {
      item {
        Text(
          text = "✨ आपकी बनाई हुई कैटेगरी (${storeConfig.customCategories.size})",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = Color(0xFF0369A1)
        )
      }
      items(storeConfig.customCategories, key = { it.id }) { cc ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val imgData: Any = if (cc.imageUrl.isNotBlank()) {
              rememberCoilData(cc.imageUrl)
            } else {
              DefaultProducts.fallbackImageForCategory(Category.ALL)
            }
            AsyncImage(
              model = ImageRequest.Builder(context).data(imgData).crossfade(true).build(),
              contentDescription = cc.hindiName,
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0F2FE))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(cc.hindiName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(cc.name, fontSize = 11.sp, color = Color.Gray)
            }
            IconButton(onClick = { onDeleteCustomCategory(cc.id) }) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626))
            }
          }
        }
      }
    }

    item {
      Text(
        text = "📂 सभी कैटेगरी की फ़ोटो बदलें (Crop व Save)",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color(0xFF1E293B),
        modifier = Modifier.padding(top = 6.dp)
      )
    }

    items(Category.entries, key = { it.name }) { cat ->
      val overrideUrl = storeConfig.categoryImages[cat.name]
      val imgData: Any = if (!overrideUrl.isNullOrBlank()) {
        rememberCoilData(overrideUrl)
      } else {
        DefaultProducts.fallbackImageForCategory(cat)
      }
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          AsyncImage(
            model = ImageRequest.Builder(context).data(imgData).crossfade(true).build(),
            contentDescription = cat.hindiName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(Color(0xFFF1F5F9))
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text("${cat.emoji} ${cat.hindiName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(cat.displayName, fontSize = 11.sp, color = Color.Gray)
          }
          OutlinedButton(
            onClick = {
              targetCategoryKeyForCrop = cat.name
              categoryImagePicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("फ़ोटो बदलें", fontSize = 11.sp)
          }
          if (!overrideUrl.isNullOrBlank()) {
            IconButton(onClick = { onUpdateCategoryImage(cat.name, "") }) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = "Reset", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }
  }

  if (showAddCustomDialog) {
    AddCustomCategoryDialog(
      onDismiss = { showAddCustomDialog = false },
      onSave = { name, hindiName, imageUrl ->
        onAddCustomCategory(name, hindiName, imageUrl)
        showAddCustomDialog = false
      }
    )
  }
}

@Composable
private fun AddCustomCategoryDialog(
  onDismiss: () -> Unit,
  onSave: (String, String, String) -> Unit
) {
  val context = LocalContext.current
  var name by remember { mutableStateOf("") }
  var hindiName by remember { mutableStateOf("") }
  var imageUrl by remember { mutableStateOf("") }
  var pendingUri by remember { mutableStateOf<Uri?>(null) }

  val picker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) pendingUri = uri
  }

  pendingUri?.let { uri ->
    ImageCropDialog(
      sourceUri = uri,
      defaultWideAspect = false,
      onDismiss = { pendingUri = null },
      onCropped = { encoded ->
        imageUrl = encoded
        pendingUri = null
      }
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("नई कैटेगरी जोड़ें", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("अंग्रेजी नाम (उदा: Bakery & Cakes)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = hindiName,
          onValueChange = { hindiName = it },
          label = { Text("हिंदी नाम (उदा: केक व बेकरी)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        if (imageUrl.isNotBlank()) {
          val prev = rememberCoilData(imageUrl)
          AsyncImage(
            model = ImageRequest.Builder(context).data(prev).crossfade(true).build(),
            contentDescription = "Preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
          )
        }
        Button(
          onClick = {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("गैलरी से कैटेगरी फ़ोटो चुनें (Crop)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() || hindiName.isNotBlank()) {
            onSave(name.ifBlank { hindiName }, hindiName.ifBlank { name }, imageUrl)
          }
        },
        enabled = name.isNotBlank() || hindiName.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
      ) {
        Text("कैटेगरी सेव करें", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("रद्द करें") }
    }
  )
}
