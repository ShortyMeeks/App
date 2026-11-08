package com.example.inclusive.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.inclusive.model.Accommodation
import com.example.inclusive.model.ConsentGrant
import com.example.inclusive.model.Course
import com.example.inclusive.model.StudentProfile
import com.example.inclusive.ui.components.AddAccommodationDialog
import com.example.inclusive.ui.components.ConsentManagementDialog
import com.example.inclusive.viewmodel.InclusiveViewModel

@Composable
fun StudentHomeScreen(
    viewModel: InclusiveViewModel,
    profile: StudentProfile?,
    courses: List<Course>,
    consents: List<ConsentGrant>,
    onOpenProfileEditor: () -> Unit,
    onShowPrivacyNotice: () -> Unit
) {
    val context = LocalContext.current
    var editingCourseConsent by remember { mutableStateOf<Course?>(null) }
    var showAddAccommodationDialog by remember { mutableStateOf(false) }

    // ESTADO NUEVO PARA EL DIALOGO DE MATERIA
    var showAddCourseDialog by remember { mutableStateOf(false) }

    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Aún no tienes un perfil de apoyos creado.", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onOpenProfileEditor) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CREAR MI PERFIL DE APOYOS")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Tarjeta de Identidad y Encabezado
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE0F2FE), // Azul cielo muy claro
                                        Color(0xFFD1FAE5)  // Verde menta suave
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Bienestar",
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Espacio Seguro",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF0F766E)
                                )
                                Text(
                                    text = "Configuración de Apoyos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF0F766E).copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = profile.fullName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${profile.program} • Matrícula: ${profile.studentNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (profile.clinicalDetails?.isVerifiedByCounseling == true) {
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = Color(0xFF16A34A),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Acreditado",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF166534)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))


                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Button(
                                onClick = onOpenProfileEditor,
                                modifier = Modifier.fillMaxWidth().height(48.dp), // Altura táctil recomendada (HCI)
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (profile.accommodations.isEmpty()) "CREAR MI PERFIL" else "EDITAR MI PERFIL",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tarjeta de Protección de Datos Sensibles (Minimización LFPDPPP)
        item {
            ElevatedCard(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF0FDF4)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    // Fila 1: Título y Etiqueta de Privado (uno debajo del otro)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Registro Confidencial",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF14532D),
                            modifier = Modifier.weight(1f) // Toma el espacio que sobra
                        )

                        // Etiqueta compacta
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "Reservado a Docentes",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Privado",
                                    fontSize = 10.sp,
                                    color = Color(0xFF92400E),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Fila 2: Condición
                    Text(
                        text = "Condición autodeclarada: ${profile.clinicalDetails?.conditionName ?: "Ninguna registrada"}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF1F2937)
                    )

                    // Fila 3: Texto legal
                    Text(
                        text = "En apego al Principio de Minimización de Datos y a la LFPDPPP (México), sus docentes únicamente reciben los apoyos pedagógicos necesarios para su clase. Su diagnóstico clínico nunca es visible salvo autorización expresa suya por asignatura.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF374151)
                    )

                    TextButton(
                        onClick = onShowPrivacyNotice,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver Derechos ARCO (LFPDPPP)")
                    }
                }
            }
        }

        // Sección: Apoyos Académicos Aprobados
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Apoyos y Ajustes Académicos (${profile.accommodations.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = { showAddAccommodationDialog = true },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    // Solo el ícono, sin texto, o con texto "Añadir"
                }
            }
        }

        items(profile.accommodations) { acc ->
            AccommodationItemCard(
                accommodation = acc,
                onDelete = { viewModel.removeAccommodation(acc.id) }
            )
        }

        // Sección: Gestión de Consentimiento Granular por Asignatura
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Permisos por Asignatura y Consentimiento",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Usted decide exactamente qué información puede consultar cada docente durante el semestre.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // BOTÓN PARA AGREGAR MATERIA
            Button(
                onClick = { showAddCourseDialog = true },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Materia y Docente")
            }
        }

        if (courses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No has agregado ninguna materia aún.\nPresiona el botón de arriba para compartir tu perfil con un docente.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(courses) { course ->
            val consent = consents.find { it.courseId == course.id }
            CourseConsentCard(
                course = course,
                consent = consent,
                onConfigure = { editingCourseConsent = course },
                onExportPdf = {
                    viewModel.exportAndSharePdf(
                        context = context,
                        courseName = "${course.code} ${course.name}",
                        teacherName = course.teacherName,
                        shareViaWhatsApp = false
                    )
                },
                onShareWhatsApp = {
                    viewModel.exportAndSharePdf(
                        context = context,
                        courseName = "${course.code} ${course.name}",
                        teacherName = course.teacherName,
                        shareViaWhatsApp = true
                    )
                },
                onRevoke = {
                    consent?.let { viewModel.revokeConsent(it.id) }
                }
            )
        }

        // Sección: Protocolo de Emergencia y Crisis
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Emergency, contentDescription = null, tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pautas y Protocolo en Caso de Crisis o Emergencia",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "Contacto de Emergencia: ${profile.emergencyProtocol.emergencyContactName} (${profile.emergencyProtocol.emergencyContactPhone})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Text(
                        text = "Apoyo Institucional: ${profile.emergencyProtocol.counselorContact}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "Acciones a Evitar: " + profile.emergencyProtocol.actionsToAvoid.joinToString("; "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB91C1C)
                    )

                    Text(
                        text = "Acciones Recomendadas: " + profile.emergencyProtocol.actionsToTake.joinToString("; "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF15803D)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Diálogo para agregar apoyo
    if (showAddAccommodationDialog) {
        AddAccommodationDialog(
            onAdd = { title, category, instruction ->
                viewModel.addCustomAccommodation(title, category, instruction)
                showAddAccommodationDialog = false
            },
            onDismiss = { showAddAccommodationDialog = false }
        )
    }

    // NUEVO DIÁLOGO PARA AGREGAR MATERIA
    if (showAddCourseDialog) {
        var courseCode by remember { mutableStateOf("") }
        var courseName by remember { mutableStateOf("") }
        var teacherName by remember { mutableStateOf("") }
        var teacherEmail by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCourseDialog = false },
            title = { Text("Agregar Materia") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Código (Ej. CS-301)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text("Nombre de la materia") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = { teacherName = it },
                        label = { Text("Nombre del Docente") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = teacherEmail,
                        onValueChange = { teacherEmail = it },
                        label = { Text("Correo del Docente (Firebase)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (courseCode.isNotBlank() && teacherEmail.isNotBlank()) {
                            viewModel.addCustomCourse(courseCode, courseName, teacherName, teacherEmail)
                            showAddCourseDialog = false
                        }
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourseDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para gestionar consentimiento de la asignatura
    if (editingCourseConsent != null) {
        val course = editingCourseConsent!!
        val grant = consents.find { it.courseId == course.id }
        ConsentManagementDialog(
            course = course,
            existingGrant = grant,
            onSaveConsent = { shareAcc, shareComm, shareEmerg, shareDiag ->
                viewModel.grantOrUpdateConsent(
                    courseId = course.id,
                    shareAccommodations = shareAcc,
                    shareComm = shareComm,
                    shareEmergency = shareEmerg,
                    shareDiagnosis = shareDiag
                )
                editingCourseConsent = null
            },
            onRevoke = if (grant != null && !grant.isRevoked) {
                {
                    viewModel.revokeConsent(grant.id)
                    editingCourseConsent = null
                }
            } else null,
            onDismiss = { editingCourseConsent = null }
        )
    }
}

@Composable
fun AccommodationItemCard(
    accommodation: Accommodation,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = accommodation.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = accommodation.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = accommodation.instructionForTeacher,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar apoyo",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CourseConsentCard(
    course: Course,
    consent: ConsentGrant?,
    onConfigure: () -> Unit,
    onExportPdf: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onRevoke: () -> Unit
) {
    val isRevoked = consent?.isRevoked == true
    val isConfigured = consent != null && !isRevoked

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isRevoked) Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = if (isRevoked) Modifier.border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(16.dp)) else Modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${course.code} • ${course.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${course.teacherName} | ${course.schedule}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = when {
                        isRevoked -> Color(0xFFFEE2E2)
                        isConfigured -> Color(0xFFDCFCE7)
                        else -> Color(0xFFFEF3C7)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when {
                            isRevoked -> "REVOCADO"
                            isConfigured -> "ACTIVO"
                            else -> "PENDIENTE"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when {
                            isRevoked -> Color(0xFF991B1B)
                            isConfigured -> Color(0xFF166534)
                            else -> Color(0xFF92400E)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Etiquetas de alcance de permisos autorizados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PermissionMiniChip(
                    label = "Apoyos",
                    isGranted = consent?.shareAccommodations == true && !isRevoked
                )
                PermissionMiniChip(
                    label = "Comunicación",
                    isGranted = consent?.shareCommunicationPrefs == true && !isRevoked
                )
                PermissionMiniChip(
                    label = "Emergencia",
                    isGranted = consent?.shareEmergencyProtocol == true && !isRevoked
                )
                PermissionMiniChip(
                    label = "Diagnóstico",
                    isGranted = consent?.shareClinicalDiagnosis == true && !isRevoked,
                    customWithheldLabel = "Diagnóstico: RESERVADO"
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onConfigure,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isConfigured) "Editar Permisos" else "Autorizar Acceso", fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Ver PDF",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onShareWhatsApp,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir con Docente",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionMiniChip(
    label: String,
    isGranted: Boolean,
    customWithheldLabel: String? = null
) {
    Surface(
        color = if (isGranted) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = if (isGranted) "$label: SÍ" else (customWithheldLabel ?: "$label: NO"),
            style = MaterialTheme.typography.labelSmall,
            color = if (isGranted) Color(0xFF166534) else Color(0xFF64748B),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp
        )
    }
}