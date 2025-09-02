package com.example.hangout.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize


@Composable
fun CircleBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        val colors = listOf(
            Color(0xFF8B0000), Color(0xFFFF4500), Color(0xFFFF6347), Color(0xFFFF7F50),
            Color(0xFFAEC6CF), Color(0xFF87CEEB), Color(0xFF683475), Color(0xFFDB7093),
            Color(0xFFA65E2E), Color(0xFFFF6961)
        )

        val radii = (100..1000 step 100).reversed()

        radii.forEachIndexed { i, radius ->
            drawCircle(
                color = colors[i].copy(alpha = 0.15f),
                radius = radius.toFloat(),
                center = Offset(centerX, centerY),
                style = Stroke(width = 6f)
            )
        }
    }
}
