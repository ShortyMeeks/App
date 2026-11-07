package com.example.inclusive.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inclusive.model.Accommodation
import com.example.inclusive.model.AccommodationCategory
import com.example.inclusive.model.ClinicalDetails
import com.example.inclusive.model.CommunicationPreferences
import com.example.inclusive.model.EmergencyCrisisProtocol
import com.example.inclusive.model.StudentProfile
import com.example.inclusive.viewmodel.InclusiveViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(
    viewModel: InclusiveViewModel,
    initialProfile: StudentProfile?,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    // Sección A: Información Personal y Académica
    var fullName by remember { mutableStateOf(initialProfile?.fullName ?: "") }
    var studentNumber by remember { mutableStateOf(initialProfile?.studentNumber ?: "") }
    var program by remember { mutableStateOf(initialProfile?.program ?: "Ingeniería en Sistemas Computacionales") }
    var semester by remember { mutableStateOf(initialProfile?.semester ?: "5to Semestre") }
    var contactEmail by remember { mutableStateOf(initialProfile?.contactEmail ?: "") }
    var contactPhone by remember { mutableStateOf(initialProfile?.contactPhone ?: "") }

    // Sección B: Apoyos y Ajustes Académicos
    var extraExamTime by remember { mutableStateOf(initialProfile?.accommodations?.any { it.id.contains("exam") } ?: true) }
    var writtenInstructions by remember { mutableStateOf(initialProfile?.accommodations?.any { it.id.contains("written") } ?: true) }
    var printedMaterials by remember { mutableStateOf(false) }
    var preferredSeating by remember { mutableStateOf(initialProfile?.accommodations?.any { it.id.contains("seat") } ?: true) }
    var breaksAllowed by remember { mutableStateOf(initialProfile?.accommodations?.any { it.id.contains("break") } ?: true) }
    var adaptedAssessments by remember { mutableStateOf(initialProfile?.accommodations?.any { it.id.contains("oral") } ?: true) }
    var flexiblePresentations by remember { mutableStateOf(true) }
    var additionalAccommodationNotes by remember { mutableStateOf("") }

    // Sección C: Pautas de Comunicación
    var writtenInstructionsOnly by remember { mutableStateOf(initialProfile?.communicationPreferences?.writtenInstructionsOnly ?: true) }
    var discreetNotifications by remember { mutableStateOf(initialProfile?.communicationPreferences?.discreetNotifications ?: true) }
    var avoidPublicAttention by remember { mutableStateOf(initialProfile?.communicationPreferences?.avoidPublicSinglingOut ?: true) }
    var advanceNoticeOfChanges by remember { mutableStateOf(initialProfile?.communicationPreferences?.advanceNoticeOfChanges ?: true) }

    // Sección D: Estrategias de Apoyo en Aula
    var strategyShortBreaks by remember { mutableStateOf(true) }
    var strategyAvoidPhysicalContact by remember { mutableStateOf(true) }
    var strategyLeaveClassroom by remember { mutableStateOf(true) }
    var strategyAvoidRaisingVoice by remember { mutableStateOf(true) }
    var strategyClearStructuredInstructions by remember { mutableStateOf(true) }

    // Sección E: Protocolo de Emergencia o Crisis (Opcional)
    var emergencyContactName by remember { mutableStateOf(initialProfile?.emergencyProtocol?.emergencyContactName ?: "Mateo Hernández (Hermano)") }
    var emergencyContactPhone by remember { mutableStateOf(initialProfile?.emergencyProtocol?.emergencyContactPhone ?: "+52 (55) 4123-9876") }
    var emergencyActionsToTake by remember { mutableStateOf(initialProfile?.emergencyProtocol?.actionsToTake?.joinToString("\n") ?: "Ofrecer al estudiante un espacio tranquilo y ventilado\nHablar con voz neutra y calmada") }
    var emergencyActionsToAvoid by remember { mutableStateOf(initialProfile?.emergencyProtocol?.actionsToAvoid?.joinToString("\n") ?: "NO sujetar ni tocar físicamente\nNO amontonarse alrededor del estudiante") }
    var counselingExtension by remember { mutableStateOf(initialProfile?.emergencyProtocol?.counselorContact ?: "Dra. Elena Morales - Servicios de Salud Estudiantil (Ext. 402)") }

    // Sección F: Condición de Salud o Neurodesarrollo (Sensible y Totalmente Opcional)
    var conditionAutism by remember { mutableStateOf(false) }
    var conditionADHD by remember { mutableStateOf(initialProfile?.clinicalDetails?.conditionName?.contains("TDAH", ignoreCase = true) == true || initialProfile?.clinicalDetails?.conditionName?.contains("ADHD", ignoreCase = true) == true) }
    var conditionAnxiety by remember { mutableStateOf(false) }
    var conditionVisual by remember { mutableStateOf(false) }
    var conditionEpilepsy by remember { mutableStateOf(false) }
    var conditionAllergies by remember { mutableStateOf(false) }
    var conditionCustom by remember { mutableStateOf(if (initialProfile?.clinicalDetails != null && !conditionADHD) initialProfile.clinicalDetails.conditionName else "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (initialProfile == null) "Crear Perfil de Apoyos" else "Editar Perfil de Apoyos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Formulario Confidencial de Ajustes Razonables",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Banner informativo de privacidad
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Su Privacidad es Primero: Usted decide qué apoyos pedagógicos necesita. Su diagnóstico clínico nunca es obligatorio y permanece reservado ante los docentes de forma predeterminada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Sección A: Información Personal y Académica
            item {
                SectionCard(title = "Sección A: Información Personal y Académica", icon = Icons.Default.Person) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nombre Completo *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = studentNumber,
                        onValueChange = { studentNumber = it },
                        label = { Text("Matrícula o ID Estudiantil *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = program,
                        onValueChange = { program = it },
                        label = { Text("Programa Académico o Carrera *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = semester,
                        onValueChange = { semester = it },
                        label = { Text("Semestre o Periodo Actual *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        label = { Text("Correo Electrónico Institucional *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Teléfono de Contacto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Sección B: Apoyos y Ajustes Académicos
            item {
                SectionCard(title = "Sección B: Apoyos y Ajustes Académicos", icon = Icons.Default.School) {
                    Text(
                        text = "Seleccione los ajustes necesarios para exámenes y actividades en clase:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FormCheckboxItem(
                        label = "Tiempo adicional en evaluaciones y exámenes (+50%)",
                        subtitle = "Tiempo extendido en un espacio con baja estimulación sensorial",
                        checked = extraExamTime,
                        onCheckedChange = { extraExamTime = it }
                    )
                    FormCheckboxItem(
                        label = "Instrucciones y rúbricas de tareas por escrito",
                        subtitle = "Entrega de pautas orales en formato escrito o digital",
                        checked = writtenInstructions,
                        onCheckedChange = { writtenInstructions = it }
                    )
                    FormCheckboxItem(
                        label = "Materiales impresos o en formato de macrotipo",
                        subtitle = "Alto contraste y tamaño de letra ampliado",
                        checked = printedMaterials,
                        onCheckedChange = { printedMaterials = it }
                    )
                    FormCheckboxItem(
                        label = "Ubicación preferencial en el aula",
                        subtitle = "Asiento cercano a la salida o zona tranquila para disminuir sobrecarga",
                        checked = preferredSeating,
                        onCheckedChange = { preferredSeating = it }
                    )
                    FormCheckboxItem(
                        label = "Permiso para pausas breves de descompresión",
                        subtitle = "Hasta 5 minutos fuera del aula sin interrogatorios públicos",
                        checked = breaksAllowed,
                        onCheckedChange = { breaksAllowed = it }
                    )
                    FormCheckboxItem(
                        label = "Evaluaciones adaptadas y formatos alternativos de exposición",
                        subtitle = "Evitar preguntas imprevistas en público; permitir videos o exposiciones individuales",
                        checked = adaptedAssessments,
                        onCheckedChange = { adaptedAssessments = it }
                    )
                    OutlinedTextField(
                        value = additionalAccommodationNotes,
                        onValueChange = { additionalAccommodationNotes = it },
                        label = { Text("Apoyos o Notas Adicionales") },
                        placeholder = { Text("Ej. Permitir uso de computadora portátil para tomar notas") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }

            // Sección C: Pautas de Comunicación
            item {
                SectionCard(title = "Sección C: Pautas de Comunicación Preferidas", icon = Icons.Default.RecordVoiceOver) {
                    FormCheckboxItem(
                        label = "Instrucciones escritas preferidas sobre órdenes orales",
                        subtitle = "Disminuye la saturación de la memoria de trabajo",
                        checked = writtenInstructionsOnly,
                        onCheckedChange = { writtenInstructionsOnly = it }
                    )
                    FormCheckboxItem(
                        label = "Notificaciones y retroalimentación discretas",
                        subtitle = "Comunicación individual o por el portal institucional",
                        checked = discreetNotifications,
                        onCheckedChange = { discreetNotifications = it }
                    )
                    FormCheckboxItem(
                        label = "Evitar señalamientos o llamadas de atención públicas",
                        subtitle = "No exponer al estudiante ante el grupo o anfiteatro",
                        checked = avoidPublicAttention,
                        onCheckedChange = { avoidPublicAttention = it }
                    )
                    FormCheckboxItem(
                        label = "Aviso anticipado ante cambios importantes de temario o fechas",
                        subtitle = "Reduce la ansiedad ante modificaciones abruptas de itinerario",
                        checked = advanceNoticeOfChanges,
                        onCheckedChange = { advanceNoticeOfChanges = it }
                    )
                }
            }

            // Sección D: Estrategias de Apoyo en Aula
            item {
                SectionCard(title = "Sección D: Estrategias Pedagógicas en Aula", icon = Icons.Default.Assignment) {
                    FormCheckboxItem(
                        label = "Permitir pausas de regulación sensorial de 5 minutos",
                        subtitle = "Permite autorregularse fuera del aula cuando sea necesario",
                        checked = strategyShortBreaks,
                        onCheckedChange = { strategyShortBreaks = it }
                    )
                    FormCheckboxItem(
                        label = "Evitar contacto físico sin previo acuerdo",
                        subtitle = "Respeto a los límites de espacio personal",
                        checked = strategyAvoidPhysicalContact,
                        onCheckedChange = { strategyAvoidPhysicalContact = it }
                    )
                    FormCheckboxItem(
                        label = "Permitir retirarse momentáneamente si experimenta sobrecarga",
                        subtitle = "Salida ágil sin exigir explicaciones públicas verbales",
                        checked = strategyLeaveClassroom,
                        onCheckedChange = { strategyLeaveClassroom = it }
                    )
                    FormCheckboxItem(
                        label = "Evitar levantar la voz o dar indicaciones con gritos",
                        subtitle = "El tono neutro y respetuoso fomenta la calma y seguridad",
                        checked = strategyAvoidRaisingVoice,
                        onCheckedChange = { strategyAvoidRaisingVoice = it }
                    )
                }
            }

            // Sección E: Protocolo de Emergencia o Crisis (Opcional)
            item {
                SectionCard(title = "Sección E: Protocolo de Emergencia o Crisis (Opcional)", icon = Icons.Default.Emergency) {
                    Text(
                        text = "El protocolo de emergencia se comparte ÚNICAMENTE si usted lo autoriza de forma explícita para cada asignatura.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = emergencyContactName,
                        onValueChange = { emergencyContactName = it },
                        label = { Text("Nombre del Contacto de Emergencia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emergencyContactPhone,
                        onValueChange = { emergencyContactPhone = it },
                        label = { Text("Teléfono del Contacto de Emergencia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = counselingExtension,
                        onValueChange = { counselingExtension = it },
                        label = { Text("Extensión de Salud o Consejería Universitaria") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emergencyActionsToTake,
                        onValueChange = { emergencyActionsToTake = it },
                        label = { Text("Acciones Recomendadas en Caso de Crisis") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = emergencyActionsToAvoid,
                        onValueChange = { emergencyActionsToAvoid = it },
                        label = { Text("Acciones a EVITAR Estrictamente en una Crisis") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }

            // Sección F: Condición de Salud o Neurodesarrollo (Sensible y Totalmente Opcional)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sección F: Condición de Salud (Dato Personal Sensible)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF991B1B)
                            )
                        }
                        Text(
                            text = "Declarar su diagnóstico es completamente voluntario y NO es necesario para recibir ajustes razonables. Esta información se RESERVA de los docentes salvo que marque la casilla de divulgación expresa por materia.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7F1D1D)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormCheckboxItem(label = "TDAH", subtitle = "Déficit de atención y memoria de trabajo", checked = conditionADHD, onCheckedChange = { conditionADHD = it })
                                FormCheckboxItem(label = "Espectro Autista", subtitle = "Diversidad en procesamiento sensorial", checked = conditionAutism, onCheckedChange = { conditionAutism = it })
                                FormCheckboxItem(label = "Ansiedad", subtitle = "Tensión en exámenes o público", checked = conditionAnxiety, onCheckedChange = { conditionAnxiety = it })
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormCheckboxItem(label = "Discapacidad Visual", subtitle = "Requerimientos de contraste y agudeza", checked = conditionVisual, onCheckedChange = { conditionVisual = it })
                                FormCheckboxItem(label = "Epilepsia", subtitle = "Protocolo ante crisis convulsiva", checked = conditionEpilepsy, onCheckedChange = { conditionEpilepsy = it })
                                FormCheckboxItem(label = "Alergias Severas", subtitle = "Protocolo ante anafilaxia", checked = conditionAllergies, onCheckedChange = { conditionAllergies = it })
                            }
                        }
                        OutlinedTextField(
                            value = conditionCustom,
                            onValueChange = { conditionCustom = it },
                            label = { Text("Otra condición o especificación (opcional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Botón para Guardar Perfil
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (fullName.isNotBlank() && studentNumber.isNotBlank()) {
                            isSaving = true
                            coroutineScope.launch {
                                val accList = mutableListOf<Accommodation>()
                                if (extraExamTime) {
                                    accList.add(
                                        Accommodation(
                                            id = "acc_exam_time",
                                            title = "Tiempo adicional en evaluaciones (+50%)",
                                            category = AccommodationCategory.EXAMINATION,
                                            instructionForTeacher = "Conceder 1.5 veces el tiempo estándar en un aula tranquila.",
                                            isMandatory = true
                                        )
                                    )
                                }
                                if (breaksAllowed) {
                                    accList.add(
                                        Accommodation(
                                            id = "acc_break_leave",
                                            title = "Permiso para pausas breves de descompresión",
                                            category = AccommodationCategory.SENSORY_BREAKS,
                                            instructionForTeacher = "Permitir salir del aula hasta 5 minutos sin exigir explicaciones públicas.",
                                            isMandatory = true
                                        )
                                    )
                                }
                                if (writtenInstructions) {
                                    accList.add(
                                        Accommodation(
                                            id = "acc_written_guide",
                                            title = "Instrucciones de proyectos y rúbricas por escrito",
                                            category = AccommodationCategory.COMMUNICATION,
                                            instructionForTeacher = "Entregar las pautas orales en formato escrito o digital para apoyar la memoria de trabajo.",
                                            isMandatory = true
                                        )
                                    )
                                }
                                if (preferredSeating) {
                                    accList.add(
                                        Accommodation(
                                            id = "acc_preferential_seat",
                                            title = "Ubicación preferencial en el aula",
                                            category = AccommodationCategory.CLASSROOM,
                                            instructionForTeacher = "Reservar asiento cercano a la puerta o zona tranquila para facilitar pausas sensoriales.",
                                            isMandatory = false
                                        )
                                    )
                                }
                                if (adaptedAssessments) {
                                    accList.add(
                                        Accommodation(
                                            id = "acc_oral_avoid",
                                            title = "Formatos adaptados de evaluación y presentaciones",
                                            category = AccommodationCategory.EXAMINATION,
                                            instructionForTeacher = "Evitar preguntas imprevistas en público. Permitir grabaciones en video o presentaciones individuales.",
                                            isMandatory = true
                                        )
                                    )
                                }
                                if (additionalAccommodationNotes.isNotBlank()) {
                                    accList.add(
                                        Accommodation(
                                            id = "acc_custom_${System.currentTimeMillis()}",
                                            title = "Ajuste Razonable Individualizado",
                                            category = AccommodationCategory.MATERIALS,
                                            instructionForTeacher = additionalAccommodationNotes.trim(),
                                            isMandatory = true
                                        )
                                    )
                                }

                                val conditionsSelected = mutableListOf<String>()
                                if (conditionADHD) conditionsSelected.add("TDAH")
                                if (conditionAutism) conditionsSelected.add("Espectro Autista")
                                if (conditionAnxiety) conditionsSelected.add("Ansiedad")
                                if (conditionVisual) conditionsSelected.add("Discapacidad Visual")
                                if (conditionEpilepsy) conditionsSelected.add("Epilepsia")
                                if (conditionAllergies) conditionsSelected.add("Alergias Severas")
                                if (conditionCustom.isNotBlank()) conditionsSelected.add(conditionCustom.trim())

                                val conditionLabel = if (conditionsSelected.isNotEmpty()) conditionsSelected.joinToString(" y ") else null

                                val updatedProfile = StudentProfile(
                                    id = initialProfile?.id ?: "student_${UUID.randomUUID().toString().take(8)}",
                                    studentNumber = studentNumber.trim(),
                                    fullName = fullName.trim(),
                                    program = program.trim(),
                                    semester = semester.trim(),
                                    contactEmail = contactEmail.trim(),
                                    contactPhone = contactPhone.trim(),
                                    clinicalDetails = if (conditionLabel != null) {
                                        ClinicalDetails(
                                            conditionName = conditionLabel,
                                            isVerifiedByCounseling = true,
                                            verifiedBy = "Servicios de Salud y Bienestar Estudiantil",
                                            verificationTimestamp = System.currentTimeMillis()
                                        )
                                    } else null,
                                    accommodations = accList,
                                    communicationPreferences = CommunicationPreferences(
                                        writtenInstructionsOnly = writtenInstructionsOnly,
                                        discreetNotifications = discreetNotifications,
                                        avoidPublicSinglingOut = avoidPublicAttention,
                                        advanceNoticeOfChanges = advanceNoticeOfChanges
                                    ),
                                    emergencyProtocol = EmergencyCrisisProtocol(
                                        triggerWarnings = listOf("Ruido ambiental prolongado", "Contacto físico imprevisto"),
                                        actionsToTake = emergencyActionsToTake.lines().filter { it.isNotBlank() },
                                        actionsToAvoid = emergencyActionsToAvoid.lines().filter { it.isNotBlank() },
                                        emergencyContactName = emergencyContactName.trim(),
                                        emergencyContactPhone = emergencyContactPhone.trim(),
                                        counselorContact = counselingExtension.trim()
                                    )
                                )

                                viewModel.saveFullStudentProfile(updatedProfile)
                                isSaving = false
                                onNavigateBack()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving && fullName.isNotBlank() && studentNumber.isNotBlank()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GUARDAR PERFIL DE APOYOS", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(28.dp)) }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            HorizontalDivider()
            content()
        }
    }
}

@Composable
fun FormCheckboxItem(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
