package com.teleprompter.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleprompter.app.core.theme.ElectricBlue
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun TeleprompterLogoBadge(
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.26f))
            .background(Color(0xFF0D1B2A))
            .border(
                width = 1.5.dp,
                color = Color(0xFF1E3A8A).copy(alpha = 0.6f),
                shape = RoundedCornerShape(size * 0.26f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.65f)) {
            val w = this.size.width
            val h = this.size.height

            // Speech bubble outline
            val bubblePath = Path().apply {
                val r = w * 0.16f
                moveTo(r, 0f)
                lineTo(w - r, 0f)
                quadraticTo(w, 0f, w, r)
                lineTo(w, h * 0.78f - r)
                quadraticTo(w, h * 0.78f, w - r, h * 0.78f)
                lineTo(w * 0.52f, h * 0.78f)
                lineTo(w * 0.42f, h * 0.98f)
                lineTo(w * 0.32f, h * 0.78f)
                lineTo(r, h * 0.78f)
                quadraticTo(0f, h * 0.78f, 0f, h * 0.78f - r)
                lineTo(0f, r)
                quadraticTo(0f, 0f, r, 0f)
                close()
            }
            drawPath(
                path = bubblePath,
                color = Color(0xFF152238)
            )

            // 3 Horizontal script lines
            val strokeW = w * 0.065f
            val lineLeft = w * 0.18f
            val lineRight = w * 0.48f

            drawLine(
                color = Color.White,
                start = Offset(lineLeft, h * 0.24f),
                end = Offset(lineRight, h * 0.24f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White,
                start = Offset(lineLeft, h * 0.40f),
                end = Offset(lineRight, h * 0.40f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White,
                start = Offset(lineLeft, h * 0.56f),
                end = Offset(lineRight * 0.85f, h * 0.56f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Play triangle on right
            val playPath = Path().apply {
                moveTo(w * 0.58f, h * 0.22f)
                lineTo(w * 0.84f, h * 0.40f)
                lineTo(w * 0.58f, h * 0.58f)
                close()
            }
            drawPath(
                path = playPath,
                color = ElectricBlue
            )
        }
    }
}

@Composable
fun TeleprompterBrandTitle(
    fontSize: TextUnit = 28.sp,
    isDarkBackground: Boolean = true,
    modifier: Modifier = Modifier
) {
    val primaryText = if (isDarkBackground) Color.White else Color(0xFF0F172A)
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = primaryText,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("Tele")
            }
            withStyle(
                SpanStyle(
                    color = ElectricBlue,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("Prompter")
            }
        },
        fontSize = fontSize,
        modifier = modifier
    )
}
