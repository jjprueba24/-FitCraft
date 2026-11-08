package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccountAuthStatus
import com.example.data.UserProfile
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PrimaryOrange

@Composable
fun AccountAndGuestSessionCard(
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
    modifier: Modifier = Modifier
) {
    var emailInput by remember(userProfile.email) { mutableStateOf(userProfile.email.orEmpty()) }
    var pinInput by remember { mutableStateOf("") }
    var verificationCodeInput by remember(authStatus.generatedVerificationCode) {
        mutableStateOf(authStatus.generatedVerificationCode.orEmpty())
    }

    val isLoggedInVerified = !userProfile.isGuest && !userProfile.email.isNullOrBlank()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(
            1.5.dp,
            if (isLoggedInVerified) NeonLime.copy(alpha = 0.65f) else PrimaryOrange.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isLoggedInVerified) Icons.Filled.VerifiedUser else Icons.Filled.TableChart,
                        contentDescription = null,
                        tint = if (isLoggedInVerified) NeonLime else PrimaryOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLoggedInVerified) "Cuenta Verificada & Sheet Activo" else "Conectar Correo y Generar Sheet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isLoggedInVerified) NeonLime.copy(alpha = 0.2f) else PrimaryOrange.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isLoggedInVerified) "Protegido con PIN" else "Sin Verificar",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLoggedInVerified) NeonLime else PrimaryOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isLoggedInVerified) {
                Text(
                    text = "Conecta tu correo con un PIN de seguridad. Al verificarlo se genera automáticamente tu Sheet donde se guardan tu progreso, comidas y fotos (y al cerrar y volver a entrar con tu correo y PIN recuperas todo intacto).",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 8) pinInput = it },
                    label = { Text("PIN o Contraseña de Seguridad (mín. 4 dígitos)") },
                    placeholder = { Text("Ej: 2580") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_account_pin")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // If verification code was generated, show the verification confirmation step!
                if (!authStatus.pendingVerificationEmail.isNullOrBlank() && !authStatus.generatedVerificationCode.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = NeonLime.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, NeonLime),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Comprobación de Seguridad para ${authStatus.pendingVerificationEmail}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonLime
                            )
                            Text(
                                text = "Tu código de comprobación generado es: ${authStatus.generatedVerificationCode}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            OutlinedTextField(
                                value = verificationCodeInput,
                                onValueChange = { verificationCodeInput = it },
                                label = { Text("Código de comprobación (6 dígitos)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_verification_code")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    onConfirmVerificationCode(
                                        authStatus.pendingVerificationEmail,
                                        verificationCodeInput
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonLime,
                                    contentColor = Color(0xFF0A1202)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("btn_confirm_verification_code")
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Confirmar Código y Crear Mi Sheet", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onRequestEmailVerification(emailInput, pinInput)
                        },
                        enabled = emailInput.contains("@") && emailInput.contains(".") && pinInput.trim().length >= 4,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("btn_save_permanent_account"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verificar / Entrar con PIN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onOpenUserSheet,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(48.dp)
                            .testTag("btn_open_sheet_guest"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                    ) {
                        Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver Sheet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Logged in & Verified with active Sheet
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = NeonLime.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, NeonLime.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CloudDone,
                                contentDescription = null,
                                tint = NeonLime,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Correo verificado: ${userProfile.email}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonLime
                                )
                                Text(
                                    text = "Archivo Sheet activo: ${authStatus.sheetFileName ?: "sheet_progreso_${userProfile.email}.csv"}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cada cambio de peso, comida o foto se guarda automáticamente en tu Sheet. Si cierras sesión, tus datos quedan protegidos bajo tu correo y PIN.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenUserSheet,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(46.dp)
                            .testTag("btn_open_user_sheet"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonLime,
                            contentColor = Color(0xFF0A1202)
                        )
                    ) {
                        Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Abrir Mi Sheet", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    OutlinedButton(
                        onClick = onExportUserSheet,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_export_sheet_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exportar CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onLogoutPreservingData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_logout_account"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryOrange)
                ) {
                    Text(
                        "Cerrar Sesión (Tus Datos y Fotos Quedan Guardados en tu Sheet)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!authStatus.authMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = authStatus.authMessage,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonLime
                )
            }

            if (!authStatus.authError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = authStatus.authError,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCoral
                )
            }
        }
    }
}
