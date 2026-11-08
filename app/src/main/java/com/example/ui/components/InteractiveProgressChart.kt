package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BodyMeasurement
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange

@Composable
fun InteractiveProgressChart(
    measurements: List<BodyMeasurement>,
    targetWeightKg: Float,
    selectedMetric: String = "Peso (kg)",
    onMetricSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (measurements.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Registra al menos 1 medida para ver el gráfico",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(measurements.size, selectedMetric) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    val values = measurements.map { m ->
        when (selectedMetric) {
            "Cintura (cm)" -> m.waistCm ?: 0f
            "Pecho (cm)" -> m.chestCm ?: 0f
            "Brazo (cm)" -> m.armsCm ?: 0f
            else -> m.weightKg
        }
    }

    val dates = measurements.map { it.dateFormatted }
    val minVal = (values.filter { it > 0 }.minOrNull() ?: 50f) - 1.5f
    val maxVal = (values.filter { it > 0 }.maxOrNull() ?: 80f) + 1.5f
    val rangeVal = if (maxVal - minVal <= 0f) 5f else (maxVal - minVal)

    val lineColor = when (selectedMetric) {
        "Cintura (cm)" -> AccentCyan
        "Pecho (cm)" -> PrimaryOrange
        else -> NeonLime
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("progress_chart_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with metric chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Evolución Corporal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Último registro: ${values.lastOrNull() ?: "-"} ${if (selectedMetric == "Peso (kg)") "kg" else "cm"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedMetric == "Peso (kg)") {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Meta: ${targetWeightKg}kg",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metric selector pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Peso (kg)", "Cintura (cm)", "Pecho (cm)", "Brazo (cm)").forEach { metric ->
                    val isSelected = metric == selectedMetric
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onMetricSelected(metric) }
                            .testTag("chart_metric_${metric.take(4)}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = metric.split(" ")[0],
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Canvas Chart with Smooth Bezier Splines and Gradient Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 35f
                    val topPadding = 25f
                    val chartHeight = height - bottomPadding - topPadding

                    if (values.size < 2) {
                        // Single point representation
                        val x = width / 2f
                        val y = height / 2f
                        drawCircle(color = lineColor, radius = 8.dp.toPx(), center = Offset(x, y))
                        return@Canvas
                    }

                    val points = mutableListOf<Offset>()
                    val stepX = width / (values.size - 1)

                    for (i in values.indices) {
                        val v = values[i]
                        val normalized = (v - minVal) / rangeVal
                        val x = i * stepX
                        // Higher value should be higher on Y axis (lower pixel value)
                        val y = topPadding + (chartHeight * (1f - normalized))
                        points.add(Offset(x, y))
                    }

                    // Draw subtle grid lines
                    val gridSteps = 3
                    for (g in 0..gridSteps) {
                        val gridY = topPadding + (chartHeight / gridSteps) * g
                        drawLine(
                            color = Color.White.copy(alpha = 0.07f),
                            start = Offset(0f, gridY),
                            end = Offset(width, gridY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Target weight guide line if viewing weight
                    if (selectedMetric == "Peso (kg)" && targetWeightKg in minVal..maxVal) {
                        val targetNorm = (targetWeightKg - minVal) / rangeVal
                        val targetY = topPadding + (chartHeight * (1f - targetNorm))
                        drawLine(
                            color = Color(0xFFCCFF00).copy(alpha = 0.4f),
                            start = Offset(0f, targetY),
                            end = Offset(width, targetY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        )
                    }

                    // Build smooth cubic bezier curve
                    val linePath = Path()
                    val fillPath = Path()

                    val animatedCount = (points.size * animationProgress.value).coerceAtLeast(1f)

                    linePath.moveTo(points.first().x, points.first().y)
                    fillPath.moveTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]

                        val cx = (p0.x + p1.x) / 2f
                        val cy0 = p0.y
                        val cy1 = p1.y

                        linePath.cubicTo(
                            cx, cy0,
                            cx, cy1,
                            p1.x, p1.y
                        )
                        fillPath.cubicTo(
                            cx, cy0,
                            cx, cy1,
                            p1.x, p1.y
                        )
                    }

                    // Close fill path down to chart bottom
                    fillPath.lineTo(points.last().x, height - bottomPadding)
                    fillPath.lineTo(points.first().x, height - bottomPadding)
                    fillPath.close()

                    // Draw gradient area under the line
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.35f * animationProgress.value),
                                lineColor.copy(alpha = 0.0f)
                            ),
                            startY = topPadding,
                            endY = height - bottomPadding
                        )
                    )

                    // Draw main path line
                    drawPath(
                        path = linePath,
                        color = lineColor,
                        style = Stroke(
                            width = 3.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // Draw points & labels
                    for (i in points.indices) {
                        val p = points[i]
                        // Outer halo
                        drawCircle(
                            color = lineColor.copy(alpha = 0.3f),
                            radius = 6.dp.toPx(),
                            center = p
                        )
                        // Inner circle
                        drawCircle(
                            color = Color(0xFF0F141C),
                            radius = 4.dp.toPx(),
                            center = p
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 2.5.dp.toPx(),
                            center = p
                        )
                    }
                }
            }

            // X-Axis Date Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dates.forEach { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
