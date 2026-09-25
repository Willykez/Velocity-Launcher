package com.example.model

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class IconShape(val displayName: String) {
    SQUIRCLE("Squircle"),
    CIRCLE("Circle"),
    ROUNDED_SQUARE("Rounded Square"),
    TEARDROP("Teardrop"),
    HEXAGON("Hexagon"),
    OCTAGON("Octagon");

    fun toComposeShape(cornerPercent: Int = 30): Shape {
        return when (this) {
            CIRCLE -> CircleShape
            SQUIRCLE -> RoundedCornerShape(cornerPercent)
            ROUNDED_SQUARE -> RoundedCornerShape(16.dp)
            TEARDROP -> RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp,
                bottomStart = 28.dp,
                bottomEnd = 4.dp
            )
            HEXAGON -> GenericShape { size, _ ->
                val radius = min(size.width, size.height) / 2f
                val cx = size.width / 2f
                val cy = size.height / 2f
                for (i in 0 until 6) {
                    val angle = (i * 60f) * (PI.toFloat() / 180f)
                    val x = cx + radius * cos(angle)
                    val y = cy + radius * sin(angle)
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            OCTAGON -> GenericShape { size, _ ->
                val radius = min(size.width, size.height) / 2f
                val cx = size.width / 2f
                val cy = size.height / 2f
                for (i in 0 until 8) {
                    val angle = (i * 45f + 22.5f) * (PI.toFloat() / 180f)
                    val x = cx + radius * cos(angle)
                    val y = cy + radius * sin(angle)
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
        }
    }
}
