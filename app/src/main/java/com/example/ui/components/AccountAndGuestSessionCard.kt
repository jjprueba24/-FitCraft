package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange

@Composable
fun AccountAndGuestSessionCard(
    userProfile: UserProfile,
    onLoginWithEmail: (String) -> Unit,
    onContinueAsGuest: () -> Unit,
    onWipeAndReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var emailInput by remember { mutableStateOf("") }
    var isEditingEmail by remember { mutableStateOf(userProfile.email.isNullOrBlank() && !userProfile.isGuest) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (userProfile.isGuest) PrimaryOrange.copy(alpha = 0.5f) else NeonLime.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (userProfile.isGuest) Icons.Filled.AccessTime else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (userProfile.isGuest) PrimaryOrange else NeonLime,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (userProfile.isGuest) "Modo Invitado Temporal" else "Cuenta Guardada en Room",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (userProfile.isGuest) PrimaryOrange.copy(alpha = 0.2f) else NeonLime.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (userProfile.isGuest) "4 Horas Máx" else "Permanente",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (userProfile.isGuest) PrimaryOrange else NeonLime,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (userProfile.isGuest) {
                // Warning about 4-hour self-destruction
                val remainingMs = userProfile.remainingGuestTimeMillis
                val hours = remainingMs / (1000 * 60 * 60)
                val minutes = (remainingMs % (1000 * 60 * 60)) / (1000 * 60)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryOrange.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tiempo restante de sesión: ${hours}h ${minutes}min",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryOrange
                            )
                            Text(
                                text = "Al cumplirse las 4 horas, la base de datos Room borrará todos los registros y volverá a iniciar en cero.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Email login box to save permanently
                Text(
                    text = "Inicia con tu correo para guardar tu progreso para siempre:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Tu correo electrónico") },
                    placeholder = { Text("ejemplo@gmail.com") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_account_email")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (emailInput.contains("@") && emailInput.contains(".")) {
                                onLoginWithEmail(emailInput)
                            }
                        },
                        enabled = emailInput.contains("@") && emailInput.contains("."),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("btn_save_permanent_account"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Iniciar y Guardar Datos", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onWipeAndReset,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(48.dp)
                            .testTag("btn_reset_guest_data"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCoral)
                    ) {
                        Text("Borrar Todo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Permanently logged in
                Text(
                    text = "Conectado como: ${userProfile.email}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonLime
                )
                Text(
                    text = "Tus rutinas, pesajes y comidas están almacenados de forma permanente en la base de datos local Room de tu teléfono.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onWipeAndReset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_logout_account"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCoral)
                ) {
                    Text("Cerrar Sesión y Limpiar Datos Locales", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
