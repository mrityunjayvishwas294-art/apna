package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

@Composable
fun ImageCropDialog(
  sourceUri: Uri,
  defaultWideAspect: Boolean = false,
  onDismiss: () -> Unit,
  onCropped: (String) -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var loadedBitmap by remember(sourceUri) { mutableStateOf<Bitmap?>(null) }
  var isGif by remember(sourceUri) { mutableStateOf(false) }
  var isProcessing by remember { mutableStateOf(true) }
  var isSaving by remember { mutableStateOf(false) }

  var wideAspect by remember { mutableStateOf(defaultWideAspect) }
  var zoom by remember { mutableFloatStateOf(1f) }
  var offsetX by remember { mutableFloatStateOf(0f) }
  var offsetY by remember { mutableFloatStateOf(0f) }
  var boxSize by remember { mutableStateOf(IntSize.Zero) }

  LaunchedEffect(sourceUri) {
    isProcessing = true
    val result = withContext(Dispatchers.IO) {
      val mimeType = context.contentResolver.getType(sourceUri)?.lowercase() ?: ""
      if (mimeType.contains("gif") || sourceUri.toString().lowercase().endsWith(".gif")) {
        val bytes = context.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() }
        if (bytes != null) {
          val encoded = if (bytes.size <= 320_000) {
            "data:image/gif;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
          } else {
            val dir = File(context.filesDir, "product_images").apply { mkdirs() }
            val gifFile = File(dir, "anim_${System.currentTimeMillis()}.gif")
            FileOutputStream(gifFile).use { it.write(bytes) }
            "file://${gifFile.absolutePath}"
          }
          return@withContext Pair(true, encoded)
        }
      }
      Pair(false, decodePreviewBitmap(context, sourceUri))
    }
    if (result.first) {
      isGif = true
      val gifStr = result.second as? String
      if (gifStr != null) {
        onCropped(gifStr)
        return@LaunchedEffect
      }
    } else {
      loadedBitmap = result.second as? Bitmap
    }
    isProcessing = false
  }

  AlertDialog(
    onDismissRequest = { if (!isSaving) onDismiss() },
    title = {
      Text("✂️ फ़ोटो क्रॉप व सेट करें", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (isProcessing) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(color = Color(0xFF0284C7))
          }
        } else if (loadedBitmap != null) {
          val bmp = loadedBitmap!!
          Text(
            text = "उंगली से फ़ोटो खिसकाएँ और स्लाइडर से ज़ूम करें:",
            fontSize = 11.sp,
            color = Color(0xFF475569)
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = !wideAspect,
              onClick = { wideAspect = false; offsetX = 0f; offsetY = 0f },
              label = { Text("1:1 प्रोडक्ट", fontSize = 11.sp) }
            )
            FilterChip(
              selected = wideAspect,
              onClick = { wideAspect = true; offsetX = 0f; offsetY = 0f },
              label = { Text("16:9 बैनर/पोस्टर", fontSize = 11.sp) }
            )
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(if (wideAspect) 16f / 9f else 1f)
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF0F172A))
              .border(2.dp, Color(0xFF0284C7), RoundedCornerShape(10.dp))
              .clipToBounds()
              .onSizeChanged { boxSize = it }
              .pointerInput(zoom) {
                detectDragGestures { change, dragAmount ->
                  change.consume()
                  val maxPanX = (boxSize.width * (zoom - 1f) * 0.5f).coerceAtLeast(boxSize.width * 0.25f)
                  val maxPanY = (boxSize.height * (zoom - 1f) * 0.5f).coerceAtLeast(boxSize.height * 0.25f)
                  offsetX = (offsetX + dragAmount.x).coerceIn(-maxPanX, maxPanX)
                  offsetY = (offsetY + dragAmount.y).coerceIn(-maxPanY, maxPanY)
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Image(
              bitmap = bmp.asImageBitmap(),
              contentDescription = "Crop Preview",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                  scaleX = zoom
                  scaleY = zoom
                  translationX = offsetX
                  translationY = offsetY
                }
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ज़ूम:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Slider(
              value = zoom,
              onValueChange = { zoom = it },
              valueRange = 1f..3f,
              modifier = Modifier.weight(1f)
            )
          }
        } else {
          Text("फ़ोटो लोड नहीं हो सकी।", color = Color.Red, fontSize = 13.sp)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val bmp = loadedBitmap ?: return@Button
          isSaving = true
          scope.launch {
            val saved = withContext(Dispatchers.IO) {
              cropAndEncodeBitmap(
                context = context,
                bitmap = bmp,
                wideAspect = wideAspect,
                zoom = zoom,
                offsetX = offsetX,
                offsetY = offsetY,
                boxSize = boxSize
              )
            }
            isSaving = false
            if (saved != null) onCropped(saved) else onDismiss()
          }
        },
        enabled = !isProcessing && !isSaving && loadedBitmap != null,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
      ) {
        if (isSaving) {
          CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
          Text("क्रॉप करके सेव करें", fontWeight = FontWeight.Bold)
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, enabled = !isSaving) {
        Text("रद्द करें")
      }
    }
  )
}

private fun decodePreviewBitmap(context: Context, uri: Uri): Bitmap? {
  return try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (bounds.outWidth / sample > 960 || bounds.outHeight / sample > 960) {
      sample *= 2
    }
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
  } catch (_: Exception) {
    null
  }
}

private fun cropAndEncodeBitmap(
  context: Context,
  bitmap: Bitmap,
  wideAspect: Boolean,
  zoom: Float,
  offsetX: Float,
  offsetY: Float,
  boxSize: IntSize
): String? {
  return try {
    val targetAspect = if (wideAspect) 16f / 9f else 1f
    val srcW = bitmap.width.toFloat()
    val srcH = bitmap.height.toFloat()
    val srcAspect = srcW / srcH

    // Base crop region matching ContentScale.Crop inside the box
    val baseCropW: Float
    val baseCropH: Float
    if (srcAspect > targetAspect) {
      baseCropH = srcH
      baseCropW = srcH * targetAspect
    } else {
      baseCropW = srcW
      baseCropH = srcW / targetAspect
    }

    val cropW = (baseCropW / zoom).coerceIn(32f, srcW)
    val cropH = (baseCropH / zoom).coerceIn(32f, srcH)

    val normShiftX = if (boxSize.width > 0) (-offsetX / boxSize.width.toFloat()) * (baseCropW / zoom) else 0f
    val normShiftY = if (boxSize.height > 0) (-offsetY / boxSize.height.toFloat()) * (baseCropH / zoom) else 0f

    val left = ((srcW - cropW) * 0.5f + normShiftX).roundToInt().coerceIn(0, (bitmap.width - cropW.roundToInt()).coerceAtLeast(0))
    val top = ((srcH - cropH) * 0.5f + normShiftY).roundToInt().coerceIn(0, (bitmap.height - cropH.roundToInt()).coerceAtLeast(0))
    val widthInt = cropW.roundToInt().coerceAtMost(bitmap.width - left).coerceAtLeast(1)
    val heightInt = cropH.roundToInt().coerceAtMost(bitmap.height - top).coerceAtLeast(1)

    val cropped = Bitmap.createBitmap(bitmap, left, top, widthInt, heightInt)
    val maxOut = if (wideAspect) 640 else 480
    val scale = minOf(maxOut.toFloat() / cropped.width, maxOut.toFloat() / cropped.height, 1f)
    val outW = (cropped.width * scale).roundToInt().coerceAtLeast(1)
    val outH = (cropped.height * scale).roundToInt().coerceAtLeast(1)
    val finalBmp = if (scale < 1f) Bitmap.createScaledBitmap(cropped, outW, outH, true) else cropped

    val baos = ByteArrayOutputStream()
    finalBmp.compress(Bitmap.CompressFormat.JPEG, 76, baos)
    val bytes = baos.toByteArray()

    if (finalBmp != cropped) cropped.recycle()
    finalBmp.recycle()

    if (bytes.size <= 320_000) {
      "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    } else {
      val dir = File(context.filesDir, "product_images").apply { mkdirs() }
      val outFile = File(dir, "crop_${System.currentTimeMillis()}.jpg")
      FileOutputStream(outFile).use { it.write(bytes) }
      "file://${outFile.absolutePath}"
    }
  } catch (_: Exception) {
    null
  }
}
