package com.teleprompter.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleprompter.app.core.theme.DeepNavyDark
import com.teleprompter.app.ui.components.TeleprompterBrandTitle
import com.teleprompter.app.ui.components.TeleprompterLogoBadge
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        delay(1200)
        onNavigateToHome()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepNavyDark,
                        Color(0xFF091222),
                        Color(0xFF0D1B34)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onNavigateToHome()
            }
    ) {
        // Subtle decorative waves at bottom (Screen 1 in mockup)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .align(Alignment.BottomCenter)
        ) {
            val w = size.width
            val h = size.height

            // Wave 1
            val path1 = Path().apply {
                moveTo(0f, h * 0.45f)
                cubicTo(w * 0.35f, h * 0.15f, w * 0.65f, h * 0.75f, w, h * 0.35f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = path1,
                color = Color(0xFF10284F).copy(alpha = 0.45f)
            )

            // Wave 2
            val path2 = Path().apply {
                moveTo(0f, h * 0.65f)
                cubicTo(w * 0.28f, h * 0.45f, w * 0.72f, h * 0.85f, w, h * 0.55f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = path2,
                color = Color(0xFF14366A).copy(alpha = 0.5f)
            )
        }

        // Center Branding
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .scale(scale.value)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TeleprompterLogoBadge(size = 96.dp)

            Spacer(modifier = Modifier.height(28.dp))

            TeleprompterBrandTitle(fontSize = 32.sp, isDarkBackground = true)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your Script. Your Flow. On Camera.",
                color = Color(0xFF8EA3BF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
