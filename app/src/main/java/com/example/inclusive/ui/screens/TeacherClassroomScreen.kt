package com.example.inclusive.ui.screens

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inclusive.model.ConsentGrant
import com.example.inclusive.model.Course
import com.example.inclusive.model.StudentProfile
import com.example.inclusive.viewmodel.InclusiveViewModel

@Composable
fun TeacherClassroomScreen(
    viewModel: InclusiveViewModel,
    profile: StudentProfile?,
    courses: List<Course>,
    consents: List<ConsentGrant>,
    selectedCourseId: String?
) {
    val context = LocalContext.current
    val currentCourse = courses.find { it.id == selectedCourseId } ?: courses.firstOrNull()
    val consent = consents.find { it.courseId == currentCourse?.id }

    val isRevoked = consent?.isRevoked == true
    val isGranted = consent != null && !isRevoked

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Encabezado del Portal Docente
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Portal Docente de Apoyos en Aula",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "DOCENTE",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = "Consulta de apoyos pedagógicos autorizados para estudiantes matriculados. El diagnóstico médico permanece reservado conforme a los principios de privacidad y minimización de datos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.signOut() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End).height(32.dp)
                    ) {
                        Text("Cerrar Sesión", fontSize = 11.sp)
                    }
                }
            }
        }

        // Selector de Asignaturas
        item {
            Text(
                text = "Seleccione la Asignatura Asignada:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                courses.forEach { course ->
                    val isSelected = course.id == currentCourse?.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCourse(course.id) },
                        label = { Text(course.code) }
                    )
                }
            }
        }

        if (currentCourse == null || profile == null) {
            item {
                Text("No hay información de asignatura disponible.")
            }
            return@LazyColumn
        }

        // Estado del Acceso
        if (isRevoked) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Acceso Revocado por el Estudiante",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "El estudiante ha ejercido su derecho de revocación bajo la LFPDPPP para la materia ${currentCourse.code}. No se muestra información de apoyos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }
        } else if (!isGranted) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Pendiente de Autorización del Estudiante",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = "El estudiante aún no ha otorgado acceso al perfil de apoyos para la asignatura ${currentCourse.code}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }
        } else {
            // Ficha de Apoyos Activa para el Docente
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Estudiante: ${profile.fullName}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Matrícula: ${profile.studentNumber} | Periodo: ${currentCourse.semester}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Docente: ${currentCourse.teacherName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "ACCESO VIGENTE",
                                    color = Color(0xFF166534),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Banner de Minimización de Datos
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (consent?.shareClinicalDiagnosis == true)
                                        "Declaración voluntaria autorizada: ${profile.clinicalDetails?.conditionName}"
                                    else
                                        "Diagnóstico clínico reservado por decisión del estudiante (Principio de Minimización). Aplique los apoyos pedagógicos siguientes:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF334155),
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        HorizontalDivider()

                        // Lista de apoyos en aula
                        Text(
                            text = "Apoyos y Ajustes Requeridos en Aula:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        if (consent?.shareAccommodations == true) {
                            profile.accommodations.forEach { acc ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF0F766E),
                                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = acc.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = "Ajuste: ${acc.instructionForTeacher}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        } else {
                            Text(
                                text = "El estudiante no autorizó la visualización de apoyos para esta asignatura.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        HorizontalDivider()

                        // Pautas de comunicación
                        Text(
                            text = "Pautas de Comunicación Recomendadas en Aula:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        if (consent?.shareCommunicationPrefs == true) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("• Brindar instrucciones de evaluaciones y tareas en formato escrito o digital", style = MaterialTheme.typography.bodySmall)
                                Text("• Comunicar retroalimentación de manera discreta y privada; evitar observaciones públicas", style = MaterialTheme.typography.bodySmall)
                                Text("• Evitar forzar exposiciones orales imprevistas sin acuerdo previo", style = MaterialTheme.typography.bodySmall)
                                Text("• Permitir salir brevemente del aula (hasta 5 minutos) sin exigir explicaciones públicas", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        // Protocolo de emergencia si fue autorizado
                        if (consent?.shareEmergencyProtocol == true) {
                            HorizontalDivider()
                            Text(
                                text = "Protocolo de Emergencia o Crisis (Autorizado):",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Acciones a Evitar: " + profile.emergencyProtocol.actionsToAvoid.joinToString("; "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = "Acciones Recomendadas: " + profile.emergencyProtocol.actionsToTake.joinToString("; "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF166534)
                                    )
                                    Text(
                                        text = "Contacto: ${profile.emergencyProtocol.emergencyContactName} (${profile.emergencyProtocol.emergencyContactPhone})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF1F2937)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Botón de Descarga de Ficha en PDF
                        Button(
                            onClick = {
                                viewModel.exportAndSharePdf(
                                    context = context,
                                    courseName = "${currentCourse.code} ${currentCourse.name}",
                                    teacherName = currentCourse.teacherName,
                                    shareViaWhatsApp = false
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Descargar Ficha de Apoyos en Aula (PDF)")
                        }
                    }
                }
            }

            // Nota de Transparencia y Auditoría
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Auditoría Activa: La consulta de esta ficha ha sido registrada de forma inmutable en la bitácora institucional de auditoría.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
