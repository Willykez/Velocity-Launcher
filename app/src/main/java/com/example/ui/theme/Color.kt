package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val AuraPrimary = Color(0xFF6C5CE7)
val AuraPrimaryVariant = Color(0xFFA29BFE)
val AuraSecondary = Color(0xFF00CEC9)
val AuraAccent = Color(0xFFFF7675)

val AmoledBg = Color(0xFF0B0C10)
val AmoledSurface = Color(0xFF14161F)
val AmoledCard = Color(0xFF1E212D)

val NeonCyan = Color(0xFF00F5D4)
val NeonPurple = Color(0xFF7B2CBF)
val NeonPink = Color(0xFFFF007F)

object AuraGradients {
    val cosmic = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0D0B18),
            Color(0xFF191238),
            Color(0xFF0D1B2A)
        )
    )

    val aura = Brush.radialGradient(
        colors = listOf(
            Color(0xFF2E1C4E),
            Color(0xFF16152B),
            Color(0xFF090A0F)
        ),
        radius = 1600f
    )

    val sunset = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF38152E),
            Color(0xFF1E1035),
            Color(0xFF090A0F)
        )
    )

    val minimalDark = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF121316),
            Color(0xFF0A0A0C)
        )
    )
}
