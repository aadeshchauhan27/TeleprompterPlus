package com.teleprompter.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleprompter.app.core.theme.CyanGradient
import com.teleprompter.app.core.theme.ElectricBlue
import com.teleprompter.app.core.theme.PurpleGradient

@Composable
fun TeleprompterLogoBadge(
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0A0F1D)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = 0.8f),
                        CyanGradient.copy(alpha = 0.4f),
                        PurpleGradient.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(size * 0.28f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // Teleprompter Rounded Glass Viewport
            val glassPath = Path().apply {
                val r = w * 0.18f
                moveTo(r, 0f)
                lineTo(w - r, 0f)
                quadraticTo(w, 0f, w, r)
                lineTo(w, h * 0.82f - r)
                quadraticTo(w, h * 0.82f, w - r, h * 0.82f)
                lineTo(r, h * 0.82f)
                quadraticTo(0f, h * 0.82f, 0f, h * 0.82f - r)
                lineTo(0f, r)
                quadraticTo(0f, 0f, r, 0f)
                close()
            }
            drawPath(
                path = glassPath,
                color = Color(0xFF1E293B).copy(alpha = 0.85f)
            )

            // 3 Horizontal glowing script lines
            val strokeW = w * 0.075f
            val lineLeft = w * 0.16f
            val lineRight = w * 0.50f

            drawLine(
                color = Color.White,
                start = Offset(lineLeft, h * 0.24f),
                end = Offset(lineRight, h * 0.24f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF38BDF8),
                start = Offset(lineLeft, h * 0.42f),
                end = Offset(lineRight * 0.92f, h * 0.42f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(lineLeft, h * 0.60f),
                end = Offset(lineRight * 0.72f, h * 0.60f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Play indicator arrow
            val playPath = Path().apply {
                moveTo(w * 0.62f, h * 0.24f)
                lineTo(w * 0.86f, h * 0.42f)
                lineTo(w * 0.62f, h * 0.60f)
                close()
            }
            drawPath(
                path = playPath,
                color = ElectricBlue
            )
        }

        // Distinct "PLUS" badge pill in top right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(size * 0.34f)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(ElectricBlue, CyanGradient)
                    )
                )
                .border(1.5.dp, Color(0xFF0F172A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = Color.White,
                fontSize = (size.value * 0.22f).sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun TeleprompterBrandTitle(
    fontSize: TextUnit = 26.sp,
    isDarkBackground: Boolean = true,
    showPlusBadge: Boolean = true,
    modifier: Modifier = Modifier
) {
    val primaryText = if (isDarkBackground) Color.White else Color(0xFF0F172A)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = primaryText,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("Teleprompter")
                }
            },
            fontSize = fontSize
        )

        Spacer(modifier = Modifier.width(6.dp))

        if (showPlusBadge) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(ElectricBlue, Color(0xFF38BDF8))
                        )
                    )
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PLUS",
                    fontSize = (fontSize.value * 0.42f).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.8.sp
                )
            }
        } else {
            Text(
                text = "Plus",
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.linearGradient(listOf(ElectricBlue, CyanGradient)),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = fontSize
                )
            )
        }
    }
}
