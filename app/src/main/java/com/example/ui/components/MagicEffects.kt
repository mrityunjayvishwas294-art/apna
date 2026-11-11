package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
  val angleRad: Float,
  val speed: Float,
  val sizeDp: Float,
  val color: Color,
  val rotationSpeed: Float,
  val isCircle: Boolean
)

@Composable
fun MagicConfettiOverlay(
  visible: Boolean,
  modifier: Modifier = Modifier
) {
  if (!visible) return

  val progress = remember { Animatable(0f) }
  val particles = remember {
    val palette = listOf(
      Color(0xFF0284C7),
      Color(0xFFF59E0B),
      Color(0xFF10B981),
      Color(0xFFEC4899),
      Color(0xFF8B5CF6),
      Color(0xFFFBBF24)
    )
    val rng = Random(System.currentTimeMillis())
    List(48) {
      ConfettiParticle(
        angleRad = (rng.nextFloat() * Math.PI * 2.0).toFloat(),
        speed = 180f + rng.nextFloat() * 520f,
        sizeDp = 6f + rng.nextFloat() * 8f,
        color = palette[it % palette.size],
        rotationSpeed = (rng.nextFloat() - 0.5f) * 720f,
        isCircle = it % 3 == 0
      )
    }
  }

  LaunchedEffect(visible) {
    progress.snapTo(0f)
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
    )
  }

  val t = progress.value
  if (t >= 1f) return

  Canvas(modifier = modifier.fillMaxSize()) {
    val originX = size.width * 0.5f
    val originY = size.height * 0.35f
    val alpha = (1f - t).coerceIn(0f, 1f)
    val gravity = 420f * t * t

    particles.forEach { p ->
      val dist = p.speed * t
      val x = originX + cos(p.angleRad) * dist
      val y = originY + sin(p.angleRad) * dist + gravity
      val px = p.sizeDp * density
      val c = p.color.copy(alpha = alpha)

      if (p.isCircle) {
        drawCircle(color = c, radius = px * 0.5f, center = Offset(x, y))
      } else {
        rotate(degrees = p.rotationSpeed * t, pivot = Offset(x, y)) {
          drawRect(
            color = c,
            topLeft = Offset(x - px * 0.5f, y - px * 0.3f),
            size = Size(px, px * 0.6f)
          )
        }
      }
    }
  }
}
