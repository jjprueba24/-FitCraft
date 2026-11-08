package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BodyMeasurement
import com.example.data.ProgressPhoto
import com.example.data.UserProfile
import com.example.ui.components.BeforeAfterComparisonView
import com.example.ui.components.InteractiveProgressChart
import com.example.ui.components.MeasurementHistoryCard
import com.example.ui.components.ProgressPhotoThumbnail

@Composable
fun ProgressScreen(
    userProfile: UserProfile,
    measurements: List<BodyMeasurement>,
    photos: List<ProgressPhoto>,
    selectedMetric: String,
    onSelectMetric: (String) -> Unit,
    onOpenAddMeasurement: () -> Unit,
    onOpenAddPhoto: () -> Unit,
    onDeleteMeasurement: (String) -> Unit,
    onDeletePhoto: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("progress_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Weekly Progress Chart
        item {
            InteractiveProgressChart(
                measurements = measurements,
                targetWeightKg = userProfile.targetWeightKg,
                selectedMetric = selectedMetric,
                onMetricSelected = onSelectMetric
            )
        }

        // Action buttons row (Registrar peso semanal / Subir foto)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenAddMeasurement,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_log_weight"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Filled.MonitorWeight, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pesarme / Medidas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenAddPhoto,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_add_progress_photo"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Subir Foto", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Before & After Visual Comparison Card
        item {
            BeforeAfterComparisonView(photos = photos)
        }

        // 3. Photo gallery list if photos exist
        if (photos.isNotEmpty()) {
            item {
                Text(
                    text = "Galería de Fotos (${photos.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(photos, key = { it.id }) { photo ->
                        ProgressPhotoThumbnail(
                            photo = photo,
                            onDelete = { onDeletePhoto(photo.id) }
                        )
                    }
                }
            }
        }

        // 4. Weekly Log History
        item {
            Text(
                text = "Historial de Pesajes Semanales",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (measurements.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aún no tienes mediciones registradas.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        } else {
            // Show recent measurements first
            val reversed = measurements.reversed()
            items(reversed, key = { it.id }) { measurement ->
                MeasurementHistoryCard(
                    measurement = measurement,
                    onDelete = { onDeleteMeasurement(measurement.id) }
                )
            }
        }
    }
}
