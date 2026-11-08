package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.AccountAuthStatus
import com.example.data.BodyMeasurement
import com.example.data.MealItem
import com.example.data.ProgressPhoto
import com.example.data.UserProfile
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSheetBottomSheet(
    userProfile: UserProfile,
    authStatus: AccountAuthStatus,
    measurements: List<BodyMeasurement>,
    meals: List<MealItem>,
    photos: List<ProgressPhoto>,
    onExportSheetCsv: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "1. Progreso (${measurements.size})",
        "2. Fotos (${photos.size})",
        "3. Comidas (${meals.size})",
        "4. Perfil & Metas"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.TableChart,
                        contentDescription = null,
                        tint = NeonLime,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Hoja de Datos (Sheet) de Usuario",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Cuenta: ${userProfile.email ?: "Invitado local"} • ${authStatus.sheetFileName ?: "sheet_progreso.csv"}",
                            fontSize = 11.sp,
                            color = NeonLime
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status & Export Bar
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, NeonLime.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Filled.CloudDone,
                            contentDescription = null,
                            tint = NeonLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Progreso, Dieta y Fotos Guardados en tu Sheet",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Última sincronización: ${authStatus.lastSheetSyncTime ?: "Ahora mismo"}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onExportSheetCsv,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonLime,
                            contentColor = Color(0xFF0A1202)
                        ),
                        modifier = Modifier.testTag("btn_export_google_sheet")
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exportar CSV", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sheet Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tabs.forEachIndexed { idx, title ->
                    val isSelected = selectedTabIndex == idx
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedTabIndex = idx }
                            .testTag("sheet_tab_$idx")
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Spreadsheet Grid Container
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0B1017),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> SheetMeasurementsTable(measurements = measurements)
                    1 -> SheetPhotosTable(photos = photos)
                    2 -> SheetMealsTable(meals = meals)
                    else -> SheetProfileTable(userProfile = userProfile)
                }
            }
        }
    }
}

@Composable
private fun SheetMeasurementsTable(measurements: List<BodyMeasurement>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Table Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF162230))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            SheetHeaderCell("FILA", Modifier.width(38.dp))
            SheetHeaderCell("FECHA", Modifier.width(72.dp))
            SheetHeaderCell("PESO (KG)", Modifier.width(74.dp))
            SheetHeaderCell("CINTURA", Modifier.width(68.dp))
            SheetHeaderCell("NOTA / ESTADO", Modifier.weight(1f))
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(measurements.reversed()) { idx, m ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (idx % 2 == 0) Color.Transparent else Color.White.copy(alpha = 0.03f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SheetDataCell("#${idx + 1}", Modifier.width(38.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SheetDataCell(m.dateFormatted, Modifier.width(72.dp))
                    SheetDataCell("${m.weightKg} kg", Modifier.width(74.dp), color = NeonLime, bold = true)
                    SheetDataCell("${m.waistCm ?: "-"} cm", Modifier.width(68.dp), color = AccentCyan)
                    SheetDataCell(m.notes.ifBlank { "Registrado" }, Modifier.weight(1f))
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            }
        }
    }
}

@Composable
private fun SheetPhotosTable(photos: List<ProgressPhoto>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF162230))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            SheetHeaderCell("FOTO", Modifier.width(52.dp))
            SheetHeaderCell("FECHA", Modifier.width(80.dp))
            SheetHeaderCell("ETIQUETA", Modifier.width(90.dp))
            SheetHeaderCell("PESO", Modifier.width(64.dp))
            SheetHeaderCell("ESTADO", Modifier.weight(1f))
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay fotos en tu Sheet. Sube tu foto en la pestaña Progreso y se guardará aquí automáticamente.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(photos.reversed()) { idx, p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (idx % 2 == 0) Color.Transparent else Color.White.copy(alpha = 0.03f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .padding(end = 8.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(Uri.parse(p.photoUriString))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = p.label,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        }
                        SheetDataCell(p.dateFormatted, Modifier.width(80.dp))
                        SheetDataCell(p.label, Modifier.width(90.dp), color = PrimaryOrange, bold = true)
                        SheetDataCell("${p.weightAtPhotoKg} kg", Modifier.width(64.dp), color = NeonLime, bold = true)
                        SheetDataCell("✓ Guardada", Modifier.weight(1f), color = AccentCyan)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                }
            }
        }
    }
}

@Composable
private fun SheetMealsTable(meals: List<MealItem>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF162230))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            SheetHeaderCell("COMIDA", Modifier.width(76.dp))
            SheetHeaderCell("PLATO / ALIMENTO", Modifier.weight(1f))
            SheetHeaderCell("KCAL", Modifier.width(56.dp))
            SheetHeaderCell("MACROS", Modifier.width(92.dp))
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(meals) { idx, meal ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (idx % 2 == 0) Color.Transparent else Color.White.copy(alpha = 0.03f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SheetDataCell(meal.mealType.label.split(" ").first(), Modifier.width(76.dp), color = PrimaryOrange)
                    SheetDataCell(meal.name, Modifier.weight(1f), bold = true)
                    SheetDataCell("${meal.calories}", Modifier.width(56.dp), color = NeonLime, bold = true)
                    SheetDataCell(
                        "P:${meal.proteinGrams.toInt()} C:${meal.carbsGrams.toInt()} G:${meal.fatGrams.toInt()}",
                        Modifier.width(92.dp),
                        color = AccentCyan
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            }
        }
    }
}

@Composable
private fun SheetProfileTable(userProfile: UserProfile) {
    val rows = listOf(
        "Correo Vinculado" to (userProfile.email ?: "Modo Local (Sin Correo)"),
        "Estatura Actual" to "${userProfile.heightCm.toInt()} cm",
        "Peso Actual" to "${userProfile.currentWeightKg} kg",
        "Peso Meta" to "${userProfile.targetWeightKg} kg",
        "Edad / Sexo" to "${userProfile.age} años • ${userProfile.gender.label}",
        "Objetivo Principal" to userProfile.goal.label,
        "Nivel de Actividad" to userProfile.activityLevel.label,
        "Entrenamiento" to "${userProfile.trainingDaysPerWeek} días/sem • ${userProfile.equipment}",
        "TMB (Gasto Basal)" to "${userProfile.bmr} kcal/día",
        "TDEE (Mantenimiento)" to "${userProfile.tdee} kcal/día",
        "Meta Calórica Diaria" to "${userProfile.targetCalories} kcal/día",
        "Macros Diarios Meta" to "Proteína: ${userProfile.targetProteinGrams}g | Carbos: ${userProfile.targetCarbsGrams}g | Grasas: ${userProfile.targetFatGrams}g"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF162230))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            SheetHeaderCell("CAMPO EN SHEET", Modifier.width(145.dp))
            SheetHeaderCell("VALOR GUARDADO", Modifier.weight(1f))
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(rows) { idx, (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (idx % 2 == 0) Color.Transparent else Color.White.copy(alpha = 0.03f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SheetDataCell(key, Modifier.width(145.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SheetDataCell(value, Modifier.weight(1f), color = NeonLime, bold = true)
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            }
        }
    }
}

@Composable
private fun SheetHeaderCell(text: String, modifier: Modifier) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        color = AccentCyan,
        modifier = modifier
    )
}

@Composable
private fun SheetDataCell(
    text: String,
    modifier: Modifier,
    color: Color = Color.White,
    bold: Boolean = false
) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        color = color,
        maxLines = 1,
        modifier = modifier
    )
}
