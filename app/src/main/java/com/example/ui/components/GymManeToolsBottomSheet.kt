package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Exercise
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange
import java.util.UUID
import kotlin.math.pow
import kotlin.math.roundToInt

data class PlateSpec(
    val weightKg: Float,
    val label: String,
    val color: Color,
    val heightFraction: Float,
    val widthDp: Float
)

private val standardOlympicPlates = listOf(
    PlateSpec(25f, "25 kg", Color(0xFFEF4444), 0.92f, 18f), // IWF Red
    PlateSpec(20f, "20 kg", Color(0xFF3B82F6), 0.88f, 16f), // IWF Blue
    PlateSpec(15f, "15 kg", Color(0xFFEAB308), 0.80f, 14f), // IWF Yellow
    PlateSpec(10f, "10 kg", Color(0xFF22C55E), 0.72f, 13f), // IWF Green
    PlateSpec(5f, "5 kg", Color(0xFFF8FAFC), 0.58f, 11f),   // IWF White
    PlateSpec(2.5f, "2.5 kg", Color(0xFF64748B), 0.48f, 9f),
    PlateSpec(1.25f, "1.25 kg", Color(0xFF94A3B8), 0.40f, 8f)
)

fun calculatePlatesPerSide(totalWeightKg: Float, barWeightKg: Float): List<PlateSpec> {
    var remainingPerSide = ((totalWeightKg - barWeightKg) / 2f).coerceAtLeast(0f)
    val result = mutableListOf<PlateSpec>()
    for (plate in standardOlympicPlates) {
        while (remainingPerSide >= plate.weightKg - 0.01f && result.size < 10) {
            result.add(plate)
            remainingPerSide -= plate.weightKg
        }
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymManeToolsBottomSheet(
    completedExercisesCount: Int,
    measurementsCount: Int,
    mealsCount: Int,
    onAddCustomExercise: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Discos Barra", "Calc. 1RM", "+ Ejercicio", "Pase & Logros")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("gymmane_tools_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NeonLime.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Calculate,
                            contentDescription = null,
                            tint = NeonLime
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Suite Pro GymMane",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Calculadora de Discos, 1RM, Ejercicios Propios y Pase QR",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                contentColor = NeonLime,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == idx) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    when (selectedTab) {
                        0 -> PlateCalculatorTab()
                        1 -> OneRepMaxCalculatorTab()
                        2 -> CustomExerciseCreatorTab(onAddCustomExercise = onAddCustomExercise)
                        else -> GymPassAndMedalsTab(
                            completedExercisesCount = completedExercisesCount,
                            measurementsCount = measurementsCount,
                            mealsCount = mealsCount
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlateCalculatorTab() {
    var totalWeightKg by remember { mutableFloatStateOf(100f) }
    var barWeightKg by remember { mutableFloatStateOf(20f) }

    val platesPerSide = remember(totalWeightKg, barWeightKg) {
        calculatePlatesPerSide(totalWeightKg, barWeightKg)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Calculadora Visual de Discos por Lado",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = NeonLime
        )

        // Bar weight selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(20f to "Barra Olímpica 20kg", 15f to "Olímpica 15kg", 10f to "Barra Z 10kg").forEach { (w, label) ->
                val isSelected = barWeightKg == w
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) AccentCyan else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { barWeightKg = w }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0F141C) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                    )
                }
            }
        }

        // Target weight display and quick step buttons
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PESO TOTAL EN BARRA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${totalWeightKg} kg",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonLime
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(-10f, -5f, -2.5f, +2.5f, +5f, +10f).forEach { delta ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, NeonLime.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    totalWeightKg = (totalWeightKg + delta).coerceIn(barWeightKg, 340f)
                                }
                        ) {
                            Text(
                                text = if (delta > 0) "+$delta" else "$delta",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (delta > 0) NeonLime else AccentCoral,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Olympic Barbell Graphic Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0B0F17))
                .padding(12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cy = size.height / 2f
                val w = size.width
                val h = size.height

                // Main steel barbell shaft
                drawRoundRect(
                    color = Color(0xFF94A3B8),
                    topLeft = Offset(w * 0.06f, cy - 6f),
                    size = Size(w * 0.88f, 12f),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Left & Right sleeves Collar stops
                val leftCollarX = w * 0.28f
                val rightCollarX = w * 0.72f
                drawRoundRect(
                    color = Color(0xFFCBD5E1),
                    topLeft = Offset(leftCollarX - 8f, cy - 16f),
                    size = Size(8f, 32f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                drawRoundRect(
                    color = Color(0xFFCBD5E1),
                    topLeft = Offset(rightCollarX, cy - 16f),
                    size = Size(8f, 32f),
                    cornerRadius = CornerRadius(3f, 3f)
                )

                // Draw plates on RIGHT sleeve (moving outward) and LEFT sleeve (moving outward)
                var rightCursor = rightCollarX + 10f
                var leftCursor = leftCollarX - 10f

                platesPerSide.forEach { plate ->
                    val plateH = h * plate.heightFraction
                    val plateW = plate.widthDp * 1.4f

                    // Right side plate
                    drawRoundRect(
                        color = plate.color,
                        topLeft = Offset(rightCursor, cy - plateH / 2f),
                        size = Size(plateW, plateH),
                        cornerRadius = CornerRadius(5f, 5f)
                    )
                    rightCursor += plateW + 3f

                    // Left side plate
                    drawRoundRect(
                        color = plate.color,
                        topLeft = Offset(leftCursor - plateW, cy - plateH / 2f),
                        size = Size(plateW, plateH),
                        cornerRadius = CornerRadius(5f, 5f)
                    )
                    leftCursor -= (plateW + 3f)
                }
            }
        }

        // Breakdown of plates per side
        Text(
            text = "Discos a colocar en CADA LADO de la barra (${(totalWeightKg - barWeightKg) / 2f} kg/lado):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (platesPerSide.isEmpty()) {
            Text(
                text = "Solo la barra vacía ($barWeightKg kg).",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val grouped = platesPerSide.groupBy { it.label }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grouped.forEach { (label, list) ->
                    val spec = list.first()
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = spec.color.copy(alpha = 0.22f),
                        border = BorderStroke(1.5.dp, spec.color)
                    ) {
                        Text(
                            text = "${list.size} × $label",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OneRepMaxCalculatorTab() {
    var weightText by remember { mutableStateOf("80") }
    var repsText by remember { mutableStateOf("8") }

    val weight = weightText.replace(',', '.').toFloatOrNull()?.coerceIn(1f, 500f) ?: 80f
    val reps = repsText.toIntOrNull()?.coerceIn(1, 30) ?: 8

    // Formulas from GymMane strength_calculators_test.dart
    val epley1Rm = if (reps == 1) weight else weight * (1f + reps / 30f)
    val brzycki1Rm = if (reps == 1) weight else weight * (36f / (37f - reps.coerceAtMost(35)))
    val lombardi1Rm = weight * reps.toDouble().pow(0.10).toFloat()
    val avg1Rm = ((epley1Rm + brzycki1Rm + lombardi1Rm) / 3f).roundToInt()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Calculadora Científica de 1RM (Fuerza Máxima)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = AccentCyan
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Peso levantado (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = repsText,
                onValueChange = { repsText = it },
                label = { Text("Repeticiones (1-20)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NeonLime.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, NeonLime.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TU 1RM ESTIMADO (1 REPETICIÓN MÁXIMA)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime
                    )
                    Text(
                        text = "Epley: ${epley1Rm.roundToInt()} kg • Brzycki: ${brzycki1Rm.roundToInt()} kg • Lombardi: ${lombardi1Rm.roundToInt()} kg",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$avg1Rm kg",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonLime
                )
            }
        }

        Text(
            text = "Tabla de Cargas por Objetivo (% de tu 1RM):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        val zones = listOf(
            Triple("95% (Fuerza Pura / Récord)", 0.95f, "1 - 2 reps"),
            Triple("90% (Fuerza Pesada)", 0.90f, "3 - 4 reps"),
            Triple("85% (Fuerza / Hipertrofia)", 0.85f, "5 - 6 reps"),
            Triple("80% (Hipertrofia Óptima)", 0.80f, "7 - 8 reps"),
            Triple("75% (Hipertrofia / Volumen)", 0.75f, "10 - 12 reps"),
            Triple("65% (Resistencia Muscular)", 0.65f, "15+ reps")
        )

        zones.forEach { (zoneName, pct, repGuide) ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = zoneName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Rango sugerido: $repGuide",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${(avg1Rm * pct).roundToInt()} kg",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomExerciseCreatorTab(
    onAddCustomExercise: (Exercise) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf("Pecho") }
    var setsText by remember { mutableStateOf("4") }
    var repsText by remember { mutableStateOf("10-12") }
    var restSecondsText by remember { mutableStateOf("75") }
    var tips by remember { mutableStateOf("Controla la fase excéntrica en 3 segundos y mantén el core firme.") }
    var savedBanner by remember { mutableStateOf<String?>(null) }

    val muscles = listOf("Pecho", "Espalda", "Hombros", "Cuádriceps", "Isquios/Glúteos", "Bíceps/Tríceps", "Core")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Crear Ejercicio Personalizado (Biblioteca GymMane)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryOrange
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; savedBanner = null },
            label = { Text("Nombre del ejercicio (ej. Remo Pendlay con Barra)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Músculo Principal:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            muscles.take(4).forEach { m ->
                val selected = muscleGroup == m
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) PrimaryOrange else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { muscleGroup = m }
                ) {
                    Text(
                        text = m,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color(0xFF0F141C) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = setsText,
                onValueChange = { setsText = it },
                label = { Text("Series") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = repsText,
                onValueChange = { repsText = it },
                label = { Text("Reps") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = restSecondsText,
                onValueChange = { restSecondsText = it },
                label = { Text("Descanso (s)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = tips,
            onValueChange = { tips = it },
            label = { Text("Guía técnica / Notas del ejercicio") },
            modifier = Modifier.fillMaxWidth()
        )

        if (savedBanner != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NeonLime.copy(alpha = 0.16f)
            ) {
                Text(
                    text = savedBanner!!,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonLime,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Button(
            onClick = {
                val cleanName = name.trim().ifEmpty { "Ejercicio Personalizado ($muscleGroup)" }
                val newEx = Exercise(
                    id = "custom_${UUID.randomUUID()}",
                    name = cleanName,
                    muscleGroup = muscleGroup,
                    sets = setsText.toIntOrNull()?.coerceIn(1, 10) ?: 4,
                    reps = repsText.ifBlank { "10-12" },
                    restSeconds = restSecondsText.toIntOrNull()?.coerceIn(15, 300) ?: 75,
                    tip = tips.ifBlank { "Mantén técnica estricta y rango completo." },
                    commonMistakes = "Evita usar impulso excesivo o acortar el recorrido articular."
                )
                onAddCustomExercise(newEx)
                savedBanner = "✓ '$cleanName' agregado a tu rutina de hoy y con figura animada lista."
                name = ""
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonLime,
                contentColor = Color(0xFF0F141C)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Agregar Ejercicio a Mi Rutina", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun GymPassAndMedalsTab(
    completedExercisesCount: Int,
    measurementsCount: Int,
    mealsCount: Int
) {
    var gymName by remember { mutableStateOf("FitCraft Smart Gym Perú") }
    var memberCode by remember { mutableStateOf("FC-2026-8841") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Digital Gym Pass Card (Inspired by GymMane gym_pass_card.dart)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF101826),
            border = BorderStroke(1.5.dp, NeonLime.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.QrCode2,
                            contentDescription = null,
                            tint = NeonLime,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = gymName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "PASE DIGITAL DE ACCESO RÁPIDO • ID: $memberCode",
                                fontSize = 10.sp,
                                color = AccentCyan
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonLime.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "ACTIVO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonLime,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Simulated high-contrast barcode / QR pattern for turnstile scanner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barCount = 42
                        val step = size.width / barCount
                        for (i in 0 until barCount) {
                            val hash = (memberCode.hashCode() + i * 31)
                            val isThick = (hash % 3 == 0)
                            val barW = if (isThick) step * 0.65f else step * 0.30f
                            drawRect(
                                color = Color(0xFF0F172A),
                                topLeft = Offset(i * step, 0f),
                                size = Size(barW, size.height)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gymName,
                        onValueChange = { gymName = it },
                        label = { Text("Gimnasio") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = memberCode,
                        onValueChange = { memberCode = it },
                        label = { Text("Código Socio / DNI") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Athlete Medals Shelf (Inspired by GymMane medal_shelf.dart)
        Text(
            text = "Estante de Medallas y Trofeos Desbloqueados",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryOrange
        )

        val medals = listOf(
            Triple("Hierro Forjado", "Completa ejercicios en tu rutina (${completedExercisesCount} listos)", completedExercisesCount >= 1),
            Triple("Control Antropométrico", "Registra al menos ${measurementsCount} pesajes en tu Sheet", measurementsCount >= 1),
            Triple("Nutrición Peruana Pro", "Cumple tu registro de comidas del día ($mealsCount platos)", mealsCount >= 2),
            Triple("Atleta Constante Élite", "Completa 4 o más ejercicios de la sesión", completedExercisesCount >= 4)
        )

        medals.forEach { (title, subtitle, unlocked) ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (unlocked) NeonLime.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, if (unlocked) NeonLime.copy(alpha = 0.45f) else Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = if (unlocked) NeonLime else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (unlocked) NeonLime else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (unlocked) "DESBLOQUEADA" else "EN PROGRESO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (unlocked) NeonLime else PrimaryOrange
                    )
                }
            }
        }
    }
}
