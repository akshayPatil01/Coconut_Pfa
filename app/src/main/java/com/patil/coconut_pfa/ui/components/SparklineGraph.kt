package com.patil.coconut_pfa.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme

/**
 * Custom Sparkline graph composable drawn with Canvas and Path showing trend data.
 */
@Composable
fun SparklineGraph(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
) {
    if (dataPoints.isEmpty()) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val min = dataPoints.minOrNull() ?: 0f
        val max = dataPoints.maxOrNull() ?: 1f
        val range = if (max == min) 1f else max - min

        val points = dataPoints.mapIndexed { index, value ->
            val x = (index.toFloat() / (dataPoints.size - 1)) * width
            // Leave a small 10% vertical margin top and bottom
            val y = height - (((value - min) / range) * (height * 0.8f) + (height * 0.1f))
            Pair(x, y)
        }

        val strokePath = Path().apply {
            moveTo(points.first().first, points.first().second)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val current = points[i]
                val controlX1 = (prev.first + current.first) / 2
                val controlY1 = prev.second
                val controlX2 = (prev.first + current.first) / 2
                val controlY2 = current.second
                cubicTo(controlX1, controlY1, controlX2, controlY2, current.first, current.second)
            }
        }

        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(points.last().first, height)
            lineTo(points.first().first, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor, Color.Transparent)
            )
        )

        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SparklineGraphPreview() {
    Coconut_PfaTheme {
        SparklineGraph(
            dataPoints = listOf(120f, 125f, 122f, 130f, 128f, 140f, 145f),
            modifier = Modifier
                .height(60.dp)
                .padding(8.dp)
        )
    }
}
