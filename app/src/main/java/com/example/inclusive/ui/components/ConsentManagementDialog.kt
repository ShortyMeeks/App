package com.example.inclusive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inclusive.model.ConsentGrant
import com.example.inclusive.model.Course

@Composable
fun ConsentManagementDialog(
    course: Course,
    existingGrant: ConsentGrant?,
    onSaveConsent: (shareAcc: Boolean, shareComm: Boolean, shareEmerg: Boolean, shareDiag: Boolean) -> Unit,
    onRevoke: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    var shareAccommodations by remember { mutableStateOf(existingGrant?.shareAccommodations ?: true) }
    var shareCommunication by remember { mutableStateOf(existingGrant?.shareCommunicationPrefs ?: true) }
    var shareEmergency by remember { mutableStateOf(existingGrant?.shareEmergencyProtocol ?: false) }
    var shareDiagnosis by remember { mutableStateOf(existingGrant?.shareClinicalDiagnosis ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Autorización de Acceso",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "${course.code} • ${course.teacherName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Usted mantiene el control exclusivo sobre sus datos personales sensibles. Seleccione únicamente la información que el/la Prof. ${course.teacherName} requiere conocer durante el periodo ${course.semester}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                ConsentToggleRow(
                    title = "Apoyos en Aula",
                    subtitle = "Tiempo extra en exámenes (+50%), pausas de 5 minutos, ubicación preferencial.",
                    checked = shareAccommodations,
                    onCheckedChange = { shareAccommodations = it }
                )

                ConsentToggleRow(
                    title = "Pautas de Comunicación",
                    subtitle = "Instrucciones de proyectos por escrito, retroalimentación discreta, evitar señalamientos públicos.",
                    checked = shareCommunication,
                    onCheckedChange = { shareCommunication = it }
                )

                ConsentToggleRow(
                    title = "Protocolo de Emergencia",
                    subtitle = "Acciones a seguir, qué evitar y datos del contacto de emergencia en caso de crisis.",
                    checked = shareEmergency,
                    onCheckedChange = { shareEmergency = it }
                )

                // Tarjeta de advertencia de alta sensibilidad para el diagnóstico clínico
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (shareDiagnosis) Color(0xFFFEF2F2) else Color(0xFFF8FAFC)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = if (shareDiagnosis) Color(0xFFDC2626) else Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Divulgación de Diagnóstico Clínico",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (shareDiagnosis) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Revela su condición específica. Bajo el Principio de Minimización de Datos, esto es totalmente OPCIONAL y NO es requisito para recibir los apoyos pedagógicos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (shareDiagnosis) "Divulgar Diagnóstico al Docente" else "Diagnóstico Reservado (Protegido)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (shareDiagnosis) Color(0xFFDC2626) else Color(0xFF16A34A)
                            )
                            Switch(
                                checked = shareDiagnosis,
                                onCheckedChange = { shareDiagnosis = it }
                            )
                        }
                    }
                }

                if (existingGrant != null && !existingGrant.isRevoked && onRevoke != null) {
                    Button(
                        onClick = onRevoke,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Revocar Acceso Inmediatamente (Derecho ARCO)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveConsent(shareAccommodations, shareCommunication, shareEmergency, shareDiagnosis)
                }
            ) {
                Text("Guardar Autorización")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ConsentToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
