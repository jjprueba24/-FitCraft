package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Exercise
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class MuscleZone(
    val id: String,
    val label: String,
    val isFront: Boolean,
    val keywords: List<String>
) {
    ALL("ALL", "Todos los Músculos", true, emptyList()),
    CHEST("CHEST", "Pecho", true, listOf("pecho", "banca", "aperturas", "fondos", "cruces")),
    SHOULDERS("SHOULDERS", "Hombros", true, listOf("hombro", "deltoide", "militar", "laterales", "face pull")),
    BICEPS("BICEPS", "Bíceps", true, listOf("bíceps", "biceps", "curl", "martillo", "brazo")),
    CORE("CORE", "Abdomen / Core", true, listOf("core", "abdom", "plancha", "crunch", "hiit", "cardio")),
    QUADS("QUADS", "Cuádriceps", true, listOf("cuádriceps", "cuadriceps", "sentadilla", "prensa", "zancada", "pierna")),
    BACK("BACK", "Espalda / Dorsales", false, listOf("espalda", "dorsal", "dominada", "remo", "jalón", "pull")),
    TRICEPS("TRICEPS", "Tríceps", false, listOf("tríceps", "triceps", "francés", "extensión", "polea")),
    GLUTES("GLUTES", "Glúteos", false, listOf("glúteo", "gluteo", "hip thrust", "búlgara", "puente")),
    HAMSTRINGS("HAMSTRINGS", "Isquios / Femoral", false, listOf("isquio", "femoral", "peso muerto", "cadena posterior")),
    CALVES("CALVES", "Gemelos", false, listOf("gemelo", "pantorrilla", "talón"))
}

fun Exercise.matchesMuscleZone(zone: MuscleZone): Boolean {
    if (zone == MuscleZone.ALL) return true
    val combined = "${this.name} ${this.muscleGroup}".lowercase()
    return zone.keywords.any { combined.contains(it) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveBodyMapCard(
    exercisesForDay: List<Exercise>,
    completedExerciseIds: Set<String>,
    selectedZone: MuscleZone,
    onSelectZone: (MuscleZone) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var showBackBody by remember { mutableStateOf(false) }

    // Determine which muscle zones are active today
    val activeZonesToday = remember(exercisesForDay) {
        MuscleZone.values().filter { zone ->
            zone != MuscleZone.ALL && exercisesForDay.any { it.matchesMuscleZone(zone) }
        }.toSet()
    }

    val completedZonesToday = remember(exercisesForDay, completedExerciseIds) {
        MuscleZone.values().filter { zone ->
            zone != MuscleZone.ALL && exercisesForDay.any {
                it.matchesMuscleZone(zone) && completedExerciseIds.contains(it.id)
            }
        }.toSet()
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, NeonLime.copy(alpha = 0.28f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_body_map_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NeonLime.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccessibilityNew,
                            contentDescription = null,
                            tint = NeonLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mapa Muscular Interactivo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentCyan.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "GYMMANE PRO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (selectedZone == MuscleZone.ALL) {
                                "Toca un músculo en el cuerpo para filtrar ejercicios (${activeZonesToday.size} zonas hoy)"
                            } else {
                                "Filtrando: ${selectedZone.label} • Toca de nuevo para ver todos"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "Expandir mapa corporal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Front / Back view toggle + Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (!showBackBody) NeonLime else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showBackBody = false }
                                    .testTag("btn_body_front")
                            ) {
                                Text(
                                    text = "Frontal",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!showBackBody) Color(0xFF0F141C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (showBackBody) NeonLime else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showBackBody = true }
                                    .testTag("btn_body_back")
                            ) {
                                Text(
                                    text = "Posterior (Espalda)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showBackBody) Color(0xFF0F141C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        if (selectedZone != MuscleZone.ALL) {
                            Text(
                                text = "Limpiar filtro",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryOrange,
                                modifier = Modifier
                                    .clickable { onSelectZone(MuscleZone.ALL) }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Interactive Dual Body Map Canvas (Front & Back side-by-side)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0B0F17))
                            .padding(8.dp)
                    ) {
                        AnatomicalBodyCanvas(
                            highlightBack = showBackBody,
                            selectedZone = selectedZone,
                            activeZonesToday = activeZonesToday,
                            completedZonesToday = completedZonesToday,
                            onZoneTapped = { tapped ->
                                if (selectedZone == tapped) {
                                    onSelectZone(MuscleZone.ALL)
                                } else {
                                    onSelectZone(tapped)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Interactive Muscle Filter Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MuscleZone.values().forEach { zone ->
                            val isSelected = selectedZone == zone
                            val isWorkedToday = activeZonesToday.contains(zone)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected && zone != MuscleZone.ALL) {
                                        onSelectZone(MuscleZone.ALL)
                                    } else {
                                        onSelectZone(zone)
                                        if (zone != MuscleZone.ALL) {
                                            showBackBody = !zone.isFront
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        text = if (isWorkedToday && zone != MuscleZone.ALL) "● ${zone.label}" else zone.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected || isWorkedToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonLime,
                                    selectedLabelColor = Color(0xFF0F141C)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnatomicalBodyCanvas(
    highlightBack: Boolean,
    selectedZone: MuscleZone,
    activeZonesToday: Set<MuscleZone>,
    completedZonesToday: Set<MuscleZone>,
    onZoneTapped: (MuscleZone) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(selectedZone, highlightBack) {
                detectTapGestures { offset ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val isLeftFigure = offset.x < w * 0.5f
                    val normY = offset.y / h

                    val tappedZone = if (isLeftFigure) {
                        // Left figure is FRONT anatomy
                        when {
                            normY in 0.18f..0.27f -> MuscleZone.SHOULDERS
                            normY in 0.27f..0.38f -> {
                                val localX = offset.x / (w * 0.5f)
                                if (localX < 0.33f || localX > 0.67f) MuscleZone.BICEPS else MuscleZone.CHEST
                            }
                            normY in 0.38f..0.54f -> MuscleZone.CORE
                            normY in 0.54f..0.85f -> MuscleZone.QUADS
                            else -> MuscleZone.CHEST
                        }
                    } else {
                        // Right figure is BACK anatomy
                        when {
                            normY in 0.18f..0.38f -> {
                                val localX = (offset.x - w * 0.5f) / (w * 0.5f)
                                if (localX < 0.32f || localX > 0.68f) MuscleZone.TRICEPS else MuscleZone.BACK
                            }
                            normY in 0.38f..0.56f -> MuscleZone.GLUTES
                            normY in 0.56f..0.76f -> MuscleZone.HAMSTRINGS
                            normY in 0.76f..0.95f -> MuscleZone.CALVES
                            else -> MuscleZone.BACK
                        }
                    }
                    onZoneTapped(tappedZone)
                }
            }
    ) {
        val w = size.width
        val h = size.height

        fun colorForZone(zone: MuscleZone): Color {
            return when {
                selectedZone == zone -> NeonLime
                completedZonesToday.contains(zone) -> AccentCyan
                activeZonesToday.contains(zone) -> PrimaryOrange.copy(alpha = 0.85f)
                else -> Color(0xFF283244)
            }
        }

        // Draw subtle grid lines
        for (i in 1..3) {
            val y = h * (i / 4f)
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        // Center divider between FRONT (left) and BACK (right)
        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = Offset(w * 0.5f, 8f),
            end = Offset(w * 0.5f, h - 8f),
            strokeWidth = 1.5f
        )

        // ================= FRONT SILHOUETTE (Left half: center at w * 0.25f) =================
        val fx = w * 0.25f
        // Head
        drawCircle(
            color = if (!highlightBack) Color(0xFF475569) else Color(0xFF334155),
            radius = h * 0.055f,
            center = Offset(fx, h * 0.11f)
        )
        // Deltoids (Shoulders)
        val shoulderColor = colorForZone(MuscleZone.SHOULDERS)
        drawCircle(color = shoulderColor, radius = h * 0.042f, center = Offset(fx - w * 0.09f, h * 0.23f))
        drawCircle(color = shoulderColor, radius = h * 0.042f, center = Offset(fx + w * 0.09f, h * 0.23f))

        // Chest (Pectorals)
        val chestColor = colorForZone(MuscleZone.CHEST)
        drawRoundRect(
            color = chestColor,
            topLeft = Offset(fx - w * 0.068f, h * 0.20f),
            size = Size(w * 0.064f, h * 0.11f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = chestColor,
            topLeft = Offset(fx + w * 0.004f, h * 0.20f),
            size = Size(w * 0.064f, h * 0.11f),
            cornerRadius = CornerRadius(10f, 10f)
        )

        // Biceps / Arms
        val bicepsColor = colorForZone(MuscleZone.BICEPS)
        drawRoundRect(
            color = bicepsColor,
            topLeft = Offset(fx - w * 0.115f, h * 0.28f),
            size = Size(w * 0.036f, h * 0.18f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = bicepsColor,
            topLeft = Offset(fx + w * 0.079f, h * 0.28f),
            size = Size(w * 0.036f, h * 0.18f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Core / Abs (6-pack blocks)
        val coreColor = colorForZone(MuscleZone.CORE)
        for (row in 0..2) {
            val topY = h * (0.325f + row * 0.055f)
            drawRoundRect(
                color = coreColor,
                topLeft = Offset(fx - w * 0.048f, topY),
                size = Size(w * 0.043f, h * 0.046f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = coreColor,
                topLeft = Offset(fx + w * 0.005f, topY),
                size = Size(w * 0.043f, h * 0.046f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }

        // Quads (Left & Right thighs)
        val quadsColor = colorForZone(MuscleZone.QUADS)
        drawRoundRect(
            color = quadsColor,
            topLeft = Offset(fx - w * 0.062f, h * 0.51f),
            size = Size(w * 0.052f, h * 0.23f),
            cornerRadius = CornerRadius(14f, 14f)
        )
        drawRoundRect(
            color = quadsColor,
            topLeft = Offset(fx + w * 0.010f, h * 0.51f),
            size = Size(w * 0.052f, h * 0.23f),
            cornerRadius = CornerRadius(14f, 14f)
        )

        // Front Shins / Calves
        val calvesColor = colorForZone(MuscleZone.CALVES)
        drawRoundRect(
            color = calvesColor.copy(alpha = 0.75f),
            topLeft = Offset(fx - w * 0.054f, h * 0.76f),
            size = Size(w * 0.040f, h * 0.17f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = calvesColor.copy(alpha = 0.75f),
            topLeft = Offset(fx + w * 0.014f, h * 0.76f),
            size = Size(w * 0.040f, h * 0.17f),
            cornerRadius = CornerRadius(10f, 10f)
        )

        // ================= BACK SILHOUETTE (Right half: center at w * 0.75f) =================
        val bx = w * 0.75f
        // Head
        drawCircle(
            color = if (highlightBack) Color(0xFF475569) else Color(0xFF334155),
            radius = h * 0.055f,
            center = Offset(bx, h * 0.11f)
        )
        // Rear Delts
        drawCircle(color = shoulderColor, radius = h * 0.040f, center = Offset(bx - w * 0.09f, h * 0.23f))
        drawCircle(color = shoulderColor, radius = h * 0.040f, center = Offset(bx + w * 0.09f, h * 0.23f))

        // Back / Lats V-Taper
        val backColor = colorForZone(MuscleZone.BACK)
        val latsPath = Path().apply {
            moveTo(bx - w * 0.072f, h * 0.19f)
            lineTo(bx + w * 0.072f, h * 0.19f)
            lineTo(bx + w * 0.048f, h * 0.44f)
            lineTo(bx - w * 0.048f, h * 0.44f)
            close()
        }
        drawPath(path = latsPath, color = backColor)

        // Triceps
        val tricepsColor = colorForZone(MuscleZone.TRICEPS)
        drawRoundRect(
            color = tricepsColor,
            topLeft = Offset(bx - w * 0.115f, h * 0.28f),
            size = Size(w * 0.036f, h * 0.18f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = tricepsColor,
            topLeft = Offset(bx + w * 0.079f, h * 0.28f),
            size = Size(w * 0.036f, h * 0.18f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Glutes
        val glutesColor = colorForZone(MuscleZone.GLUTES)
        drawRoundRect(
            color = glutesColor,
            topLeft = Offset(bx - w * 0.062f, h * 0.45f),
            size = Size(w * 0.058f, h * 0.10f),
            cornerRadius = CornerRadius(16f, 16f)
        )
        drawRoundRect(
            color = glutesColor,
            topLeft = Offset(bx + w * 0.004f, h * 0.45f),
            size = Size(w * 0.058f, h * 0.10f),
            cornerRadius = CornerRadius(16f, 16f)
        )

        // Hamstrings
        val hamstringsColor = colorForZone(MuscleZone.HAMSTRINGS)
        drawRoundRect(
            color = hamstringsColor,
            topLeft = Offset(bx - w * 0.060f, h * 0.56f),
            size = Size(w * 0.050f, h * 0.18f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = hamstringsColor,
            topLeft = Offset(bx + w * 0.010f, h * 0.56f),
            size = Size(w * 0.050f, h * 0.18f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Calves (Gastrocnemius)
        drawRoundRect(
            color = calvesColor,
            topLeft = Offset(bx - w * 0.054f, h * 0.76f),
            size = Size(w * 0.042f, h * 0.17f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = calvesColor,
            topLeft = Offset(bx + w * 0.012f, h * 0.76f),
            size = Size(w * 0.042f, h * 0.17f),
            cornerRadius = CornerRadius(12f, 12f)
        )
    }
}

/**
 * 6-Axis Muscle Balance Radar Chart inspired by GymMane's `muscle_radar.dart`
 */
@Composable
fun MuscleBalanceRadarCard(
    completedExercisesCount: Int,
    measurementsCount: Int,
    modifier: Modifier = Modifier
) {
    val axes = listOf("Pecho", "Espalda", "Piernas", "Hombros", "Brazos", "Core")
    val baseBoost = (completedExercisesCount * 0.06f).coerceAtMost(0.32f)
    val values = listOf(
        (0.68f + baseBoost).coerceAtMost(0.98f),
        (0.72f + baseBoost).coerceAtMost(0.98f),
        (0.65f + baseBoost).coerceAtMost(0.96f),
        (0.70f + baseBoost).coerceAtMost(0.95f),
        (0.74f + baseBoost).coerceAtMost(0.98f),
        (0.62f + (measurementsCount * 0.04f)).coerceAtMost(0.95f)
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.28f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("muscle_radar_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Radar,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Radar de Simetría Muscular (GymMane)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Equilibrio de volumen entre grupos musculares",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonLime.copy(alpha = 0.16f)
                ) {
                    val score = (values.average() * 100).toInt()
                    Text(
                        text = "Score $score%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonLime,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val maxR = size.minDimension * 0.44f
                        val count = axes.size

                        // Concentric hexagonal webs
                        for (ring in 1..3) {
                            val r = maxR * (ring / 3f)
                            val webPath = Path()
                            for (i in 0 until count) {
                                val angle = (2 * PI * i / count) - (PI / 2)
                                val px = cx + (r * cos(angle)).toFloat()
                                val py = cy + (r * sin(angle)).toFloat()
                                if (i == 0) webPath.moveTo(px, py) else webPath.lineTo(px, py)
                            }
                            webPath.close()
                            drawPath(
                                path = webPath,
                                color = Color.White.copy(alpha = 0.12f),
                                style = Stroke(width = 1.2f)
                            )
                        }

                        // Axis spokes
                        for (i in 0 until count) {
                            val angle = (2 * PI * i / count) - (PI / 2)
                            val px = cx + (maxR * cos(angle)).toFloat()
                            val py = cy + (maxR * sin(angle)).toFloat()
                            drawLine(
                                color = Color.White.copy(alpha = 0.14f),
                                start = Offset(cx, cy),
                                end = Offset(px, py),
                                strokeWidth = 1f
                            )
                        }

                        // Athlete Muscle Polygon
                        val dataPath = Path()
                        for (i in 0 until count) {
                            val angle = (2 * PI * i / count) - (PI / 2)
                            val r = maxR * values[i]
                            val px = cx + (r * cos(angle)).toFloat()
                            val py = cy + (r * sin(angle)).toFloat()
                            if (i == 0) dataPath.moveTo(px, py) else dataPath.lineTo(px, py)
                        }
                        dataPath.close()

                        drawPath(
                            path = dataPath,
                            color = NeonLime.copy(alpha = 0.28f)
                        )
                        drawPath(
                            path = dataPath,
                            color = NeonLime,
                            style = Stroke(width = 3f, cap = StrokeCap.Round)
                        )

                        // Vertex dots
                        for (i in 0 until count) {
                            val angle = (2 * PI * i / count) - (PI / 2)
                            val r = maxR * values[i]
                            val px = cx + (r * cos(angle)).toFloat()
                            val py = cy + (r * sin(angle)).toFloat()
                            drawCircle(color = AccentCyan, radius = 5f, center = Offset(px, py))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    axes.forEachIndexed { idx, label ->
                        val pct = (values[idx] * 100).toInt()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$pct%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (pct >= 75) NeonLime else AccentCyan
                            )
                        }
                    }
                }
            }
        }
    }
}
