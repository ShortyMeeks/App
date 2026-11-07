package com.example.inclusive.model

enum class UserRole(val label: String, val description: String) {
    STUDENT("Estudiante", "Crea su perfil, otorga y revoca permisos granulares"),
    TEACHER("Docente", "Consulta exclusivamente los apoyos en aula autorizados")
    // TODO v2: COUNSELING("Orientación y Salud", "Validación y acompañamiento institucional")
    // TODO v2: ADMIN("Administración", "Auditoría, expiraciones y métricas institucionales")
}

enum class AccommodationCategory(val label: String) {
    EXAMINATION("Evaluaciones y Exámenes"),
    CLASSROOM("Entorno de Aula"),
    COMMUNICATION("Comunicación e Interacción"),
    SENSORY_BREAKS("Pausas y Apoyo Sensorial"),
    MATERIALS("Materiales y Tecnología")
}

data class Accommodation(
    val id: String,
    val title: String,
    val category: AccommodationCategory,
    val instructionForTeacher: String,
    val isMandatory: Boolean = true
)

data class CommunicationPreferences(
    val writtenInstructionsOnly: Boolean = true,
    val discreetNotifications: Boolean = true,
    val avoidPublicSinglingOut: Boolean = true,
    val advanceNoticeOfChanges: Boolean = true
)

data class EmergencyCrisisProtocol(
    val triggerWarnings: List<String> = emptyList(),
    val actionsToTake: List<String> = emptyList(),
    val actionsToAvoid: List<String> = emptyList(),
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val counselorContact: String = ""
)

data class ClinicalDetails(
    val conditionName: String,
    val isVerifiedByCounseling: Boolean = true,
    val verifiedBy: String = "Centro de Apoyo y Bienestar Estudiantil",
    val verificationTimestamp: Long = System.currentTimeMillis()
)

data class StudentProfile(
    val id: String,
    val studentNumber: String,
    val fullName: String,
    val program: String,
    val semester: String,
    val contactEmail: String,
    val contactPhone: String,
    val clinicalDetails: ClinicalDetails?,
    val accommodations: List<Accommodation>,
    val communicationPreferences: CommunicationPreferences,
    val emergencyProtocol: EmergencyCrisisProtocol
)

data class Course(
    val id: String,
    val code: String,
    val name: String,
    val teacherId: String,
    val teacherName: String,
    val schedule: String,
    val semester: String
)

data class ConsentGrant(
    val id: String,
    val studentId: String,
    val studentName: String,
    val teacherId: String,
    val teacherName: String,
    val courseId: String,
    val courseName: String,
    val academicTerm: String,
    val shareAccommodations: Boolean = true,
    val shareCommunicationPrefs: Boolean = true,
    val shareEmergencyProtocol: Boolean = false,
    val shareClinicalDiagnosis: Boolean = false, // Conforme al principio de minimización de datos
    val grantedAtMillis: Long = System.currentTimeMillis(),
    val expiresAtMillis: Long = System.currentTimeMillis() + (120L * 24 * 60 * 60 * 1000), // Semestre
    val isRevoked: Boolean = false
)

enum class AuditActionType(val display: String) {
    VIEW_CLASSROOM_PROFILE("Consulta de Perfil de Apoyos"),
    PDF_EXPORT_DOCUMENT("Exportación de Documento PDF"),
    PDF_SHARE_WHATSAPP("Compartición de PDF por Hoja del Sistema"),
    GRANT_PERMISSIONS("Otorgamiento de Permisos"),
    REVOKE_PERMISSIONS("Revocación de Acceso (Derecho ARCO)"),
    UPDATE_PERMISSIONS("Actualización de Permisos"),
    PROFILE_VERIFIED("Validación Institucional de Apoyos"),
    DISCREET_MODE("Activación de Modo Discreto")
}

data class AuditLogEntry(
    val id: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val actorName: String,
    val actorRole: UserRole,
    val courseName: String,
    val actionType: AuditActionType,
    val details: String
)

data class TeacherClassroomView(
    val studentDisplayName: String,
    val studentNumber: String,
    val courseName: String,
    val academicTerm: String,
    val activeAccommodations: List<Accommodation>,
    val communicationGuidelines: List<String>,
    val emergencyProtocol: EmergencyCrisisProtocol?,
    val diagnosisDisclosed: String?,
    val consentStatus: String, // "ACTIVO", "REVOCADO", "EXPIRADO"
    val expiresAtDate: String
)
