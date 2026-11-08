package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndPrivacyBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableStateOf(0) } // 0: Términos, 1: Privacidad

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Policy,
                        contentDescription = null,
                        tint = NeonLime,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Legal y Seguridad",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Términos de Uso", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Política de Privacidad", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .height(380.dp)
                    .verticalScroll(scrollState)
            ) {
                if (selectedTab == 0) {
                    // TÉRMINOS Y CONDICIONES
                    SectionItem(
                        icon = Icons.Filled.HealthAndSafety,
                        title = "1. Descargo de Responsabilidad Médica y de Salud",
                        content = "FitCraft Gym & Dieta es una aplicación diseñada únicamente con fines informativos, de acondicionamiento físico general y educativos. Los planes de entrenamiento, sugerencias de ejercicios, recomendaciones nutricionales y cálculos calóricos (BMR, TDEE) no constituyen asesoramiento médico, diagnóstico ni tratamiento profesional.\n\nAntes de iniciar cualquier régimen de ejercicio de alta intensidad o cambio dietético drástico, consulte siempre con su médico o un profesional de la salud certificado, especialmente si tiene antecedentes de enfermedades cardiovasculares, lesiones articulares o afecciones metabólicas."
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SectionItem(
                        icon = Icons.Filled.VerifiedUser,
                        title = "2. Uso Apropiado de la Aplicación",
                        content = "Usted es el único responsable de ejecutar los ejercicios con la técnica correcta y dentro de sus límites físicos personales para evitar lesiones. No nos responsabilizamos por lesiones, molestias físicas o daños derivados de la sobrecarga o mala ejecución técnica."
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SectionItem(
                        icon = Icons.Filled.Lock,
                        title = "3. Modificaciones del Servicio",
                        content = "FitCraft se reserva el derecho de actualizar, optimizar o ajustar algoritmos de cálculo calórico y rutinas de ejercicio en función de las mejores prácticas de la ciencia del deporte."
                    )
                } else {
                    // POLÍTICA DE PRIVACIDAD
                    SectionItem(
                        icon = Icons.Filled.Lock,
                        title = "1. Almacenamiento Local y Privacidad Total",
                        content = "Su privacidad es nuestra prioridad absoluta. Toda la información personal introducida —incluyendo su estatura, peso, edad, registros semanales de medidas corporales y bitácora de comidas— se almacena localmente en su dispositivo mediante almacenamiento cifrado DataStore."
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SectionItem(
                        icon = Icons.Filled.Policy,
                        title = "2. Fotos de Progreso (Antes y Después)",
                        content = "Las fotos que usted sube para comparar su evolución física utilizan el selector de fotos seguro del sistema Android (Zero-Permission Photo Picker). Las imágenes permanecen en su galería y almacenamiento local del dispositivo; jamás son transmitidas, subidas a servidores externos, ni compartidas con terceros."
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SectionItem(
                        icon = Icons.Filled.VerifiedUser,
                        title = "3. Cero Venta de Datos y Cumplimiento",
                        content = "FitCraft no vende, alquila ni monetiza sus datos personales de salud o fitness. Usted tiene el control total para editar o eliminar sus registros y fotos en cualquier momento directamente desde la aplicación."
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_close_terms"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("He leído y acepto los términos", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}
