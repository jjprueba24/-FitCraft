package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccountAuthStatus
import com.example.data.ActivityLevel
import com.example.data.FitnessGoal
import com.example.data.Gender
import com.example.data.UserProfile
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange
import java.util.Locale

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    authStatus: AccountAuthStatus = AccountAuthStatus(),
    onRequestEmailVerification: (email: String, pin: String) -> Unit = { _, _ -> },
    onConfirmVerificationCode: (email: String, code: String) -> Unit = { _, _ -> },
    onOpenUserSheet: () -> Unit = {},
    onExportUserSheet: () -> Unit = {},
    onLogoutPreservingData: () -> Unit = {},
    onLoginWithEmail: (String) -> Unit = {},
    onContinueAsGuest: () -> Unit = {},
    onWipeAndReset: () -> Unit = {},
    onSaveProfile: (
        heightCm: Float,
        weightKg: Float,
        targetWeightKg: Float,
        goal: FitnessGoal,
        activity: ActivityLevel,
        gender: Gender,
        age: Int,
        daysPerWeek: Int,
        equipment: String
    ) -> Unit,
    onNavigateToWorkout: () -> Unit = {},
    onOpenTermsAndPrivacy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var heightStr by remember(userProfile.heightCm) { mutableStateOf(userProfile.heightCm.toInt().toString()) }
    var weightStr by remember(userProfile.currentWeightKg) { mutableStateOf(userProfile.currentWeightKg.toString()) }
    var targetWeightStr by remember(userProfile.targetWeightKg) { mutableStateOf(userProfile.targetWeightKg.toString()) }
    var ageStr by remember(userProfile.age) { mutableStateOf(userProfile.age.toString()) }
    var selectedGoal by remember(userProfile.goal) { mutableStateOf(userProfile.goal) }
    var selectedActivity by remember(userProfile.activityLevel) { mutableStateOf(userProfile.activityLevel) }
    var selectedGender by remember(userProfile.gender) { mutableStateOf(userProfile.gender) }
    var selectedDaysPerWeek by remember(userProfile.trainingDaysPerWeek) { mutableIntStateOf(userProfile.trainingDaysPerWeek) }
    var selectedEquipment by remember(userProfile.equipment) { mutableStateOf(userProfile.equipment) }
    var showSavedMessage by remember { mutableStateOf(false) }

    // Live calculation preview so numbers update immediately as the user edits any input
    val parsedHeight = heightStr.replace(",", ".").trim().toFloatOrNull()?.coerceIn(120f, 230f) ?: userProfile.heightCm
    val parsedWeight = weightStr.replace(",", ".").trim().toFloatOrNull()?.coerceIn(35f, 250f) ?: userProfile.currentWeightKg
    val parsedTargetWeight = targetWeightStr.replace(",", ".").trim().toFloatOrNull()?.coerceIn(35f, 250f) ?: userProfile.targetWeightKg
    val parsedAge = ageStr.trim().toIntOrNull()?.coerceIn(14, 95) ?: userProfile.age

    val livePreviewProfile = userProfile.copy(
        heightCm = parsedHeight,
        currentWeightKg = parsedWeight,
        targetWeightKg = parsedTargetWeight,
        age = parsedAge,
        gender = selectedGender,
        activityLevel = selectedActivity,
        goal = selectedGoal,
        trainingDaysPerWeek = selectedDaysPerWeek,
        equipment = selectedEquipment
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Account Verification & Sheet Sync Card
        item {
            com.example.ui.components.AccountAndGuestSessionCard(
                userProfile = userProfile,
                authStatus = authStatus,
                onRequestEmailVerification = onRequestEmailVerification,
                onConfirmVerificationCode = onConfirmVerificationCode,
                onOpenUserSheet = onOpenUserSheet,
                onExportUserSheet = onExportUserSheet,
                onLogoutPreservingData = onLogoutPreservingData,
                onLoginWithEmail = onLoginWithEmail,
                onContinueAsGuest = onContinueAsGuest,
                onWipeAndReset = onWipeAndReset
            )
        }

        // 2. Live Recalculated Parameters & Macros Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, NeonLime.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Parámetros y Cálculos en Vivo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonLime.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "IMC: ${String.format(Locale.US, "%.1f", livePreviewProfile.bmi)}",
                                color = NeonLime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatBox(
                            label = "TMB (Basal)",
                            value = "${livePreviewProfile.bmr} kcal",
                            subtext = "Gasto en reposo",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatBox(
                            label = "TDEE (Mantenimiento)",
                            value = "${livePreviewProfile.tdee} kcal",
                            subtext = "Con actividad física",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatBox(
                            label = "Meta Calórica",
                            value = "${livePreviewProfile.targetCalories} kcal",
                            subtext = when (selectedGoal) {
                                FitnessGoal.LOSE_WEIGHT -> "Déficit -450"
                                FitnessGoal.GAIN_MUSCLE -> "Superávit +350"
                                FitnessGoal.MAINTAIN -> "Equilibrio"
                            },
                            valueColor = NeonLime,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Macros Row
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Proteína: ${livePreviewProfile.targetProteinGrams}g",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                            Text(
                                text = "Carbos: ${livePreviewProfile.targetCarbsGrams}g",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonLime
                            )
                            Text(
                                text = "Grasas: ${livePreviewProfile.targetFatGrams}g",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryOrange
                            )
                        }
                    }
                }
            }
        }

        // 3. Objetivos Principales (Adelgazar vs Ganar Músculo)
        item {
            Text(
                text = "¿Cuál es tu objetivo principal?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FitnessGoal.values().forEach { goal ->
                    val isSelected = goal == selectedGoal
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                selectedGoal = goal
                                showSavedMessage = false
                            }
                            .testTag("goal_option_${goal.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = goal.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = goal.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Sexo, Estatura, Peso y Edad Inputs
        item {
            Text(
                text = "Medidas y Datos Biométricos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Gender selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Gender.values().forEach { g ->
                    val isSel = selectedGender == g
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedGender = g
                                showSavedMessage = false
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                            Text(
                                text = g.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = heightStr,
                    onValueChange = {
                        heightStr = it
                        showSavedMessage = false
                    },
                    label = { Text("Estatura (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_profile_height"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = weightStr,
                    onValueChange = {
                        weightStr = it
                        showSavedMessage = false
                    },
                    label = { Text("Peso actual (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_profile_weight"),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = targetWeightStr,
                    onValueChange = {
                        targetWeightStr = it
                        showSavedMessage = false
                    },
                    label = { Text("Peso meta (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = ageStr,
                    onValueChange = {
                        ageStr = it
                        showSavedMessage = false
                    },
                    label = { Text("Edad (años)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        // 5. Equipo Disponible y Días por Semana (Igual a Pro Gimnasio Entrenamiento)
        item {
            Text(
                text = "Equipo Disponible y Días por Semana",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            val equipments = listOf("Gimnasio Completo", "En Casa / Mancuernas", "Bandas y Calistenia")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                equipments.forEach { eq ->
                    val isSel = selectedEquipment == eq
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) AccentCyan.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSel) AccentCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedEquipment = eq
                                showSavedMessage = false
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)) {
                            Text(
                                text = eq,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(3, 4, 5, 6).forEach { days ->
                    val isSel = selectedDaysPerWeek == days
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) NeonLime else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedDaysPerWeek = days
                                showSavedMessage = false
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = "$days días/sem",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color(0xFF0A1202) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 6. Nivel de Actividad
        item {
            Text(
                text = "Nivel de Actividad Semanal",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ActivityLevel.values().forEach { level ->
                    val isSelected = level == selectedActivity
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedActivity = level
                                showSavedMessage = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = level.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // 7. Botón Guardar y Recalcular + Confirmación Interactiva
        item {
            Button(
                onClick = {
                    onSaveProfile(
                        parsedHeight,
                        parsedWeight,
                        parsedTargetWeight,
                        selectedGoal,
                        selectedActivity,
                        selectedGender,
                        parsedAge,
                        selectedDaysPerWeek,
                        selectedEquipment
                    )
                    showSavedMessage = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_save_profile"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar y Recalcular Rutina, Dieta y Sheet", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }

            if (showSavedMessage) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NeonLime.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, NeonLime),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "✓ ¡Guardado y Recalculado en tu Base de Datos y Sheet!",
                            color = NeonLime,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Nueva Meta: ${livePreviewProfile.targetCalories} kcal/día • P: ${livePreviewProfile.targetProteinGrams}g | C: ${livePreviewProfile.targetCarbsGrams}g | G: ${livePreviewProfile.targetFatGrams}g • Rutina: ${selectedGoal.label} ($selectedDaysPerWeek días - $selectedEquipment)",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToWorkout,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonLime,
                                    contentColor = Color(0xFF0A1202)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.FitnessCenter, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ver Nueva Rutina", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            OutlinedButton(
                                onClick = onOpenUserSheet,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ver en Mi Sheet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 8. TÉRMINOS Y CONDICIONES Y PRIVACIDAD BUTTON
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Policy,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Información Legal y Privacidad",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Consulta el descargo de responsabilidad médica para entrenamientos de fuerza y nuestra estricta política de protección de datos y fotos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    OutlinedButton(
                        onClick = onOpenTermsAndPrivacy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_terms_and_privacy"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Ver Términos y Condiciones y Privacidad", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatBox(
    label: String,
    value: String,
    subtext: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
            Text(text = subtext, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        }
    }
}
