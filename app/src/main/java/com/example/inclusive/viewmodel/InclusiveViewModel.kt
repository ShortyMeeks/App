package com.example.inclusive.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inclusive.data.AppDatabase
import com.example.inclusive.data.AuditLogEntity
import com.example.inclusive.data.JsonConverters
import com.example.inclusive.data.StudentProfileEntity
import com.example.inclusive.data.firebase.FirebaseService
import com.example.inclusive.model.Accommodation
import com.example.inclusive.model.AccommodationCategory
import com.example.inclusive.model.AuditActionType
import com.example.inclusive.model.AuditLogEntry
import com.example.inclusive.model.ClinicalDetails
import com.example.inclusive.model.ConsentGrant
import com.example.inclusive.model.Course
import com.example.inclusive.model.StudentProfile
import com.example.inclusive.model.UserRole
import com.example.inclusive.pdf.PdfExportManager
import com.example.inclusive.pdf.ShareManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class InclusiveViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val dao = db.dao()
    val firebaseService = FirebaseService(application)
    private val pdfExportManager = PdfExportManager(application)

    // SharedPreferences para el PIN
    private val prefs: SharedPreferences = application.getSharedPreferences("AppSecurityPrefs", Context.MODE_PRIVATE)

    // Current Active Role
    private val _currentRole = MutableStateFlow(UserRole.STUDENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Auth state
    private val _isAuthenticated = MutableStateFlow(firebaseService.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Discreet Mode & Lock
    private val _isDiscreetMode = MutableStateFlow(false)
    val isDiscreetMode: StateFlow<Boolean> = _isDiscreetMode.asStateFlow()

    // Inicia bloqueado si hay sesión activa
    private val _isLocked = MutableStateFlow(firebaseService.currentUser != null)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    // Selected Course for Teacher View
    private val _selectedCourseId = MutableStateFlow<String?>(null)
    val selectedCourseId: StateFlow<String?> = _selectedCourseId.asStateFlow()

    // File & Messages
    private val _lastExportedFile = MutableStateFlow<File?>(null)
    val lastExportedFile: StateFlow<File?> = _lastExportedFile.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _allUsers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val allUsers: StateFlow<List<Map<String, Any>>> = _allUsers.asStateFlow()

    // PERFIL DEL ESTUDIANTE (Inicia vacío, se llena con Firebase)
    private val _studentProfile = MutableStateFlow<StudentProfile?>(null)
    val studentProfile: StateFlow<StudentProfile?> = _studentProfile.asStateFlow()

    // Courses (Inicia vacío, se llena desde la UI)
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    // Consents
    private val _consents = MutableStateFlow<List<ConsentGrant>>(emptyList())
    val consents: StateFlow<List<ConsentGrant>> = _consents.asStateFlow()

    // Audit Logs
    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    init {
        syncFromCloud()
    }

    fun syncFromCloud() {
        viewModelScope.launch {
            val cloudProfile = firebaseService.getStudentProfile()
            if (cloudProfile != null) {
                _studentProfile.value = cloudProfile
            }
            loadAllUsers()
        }
    }

    fun clearUserMessage() { _userMessage.value = null }
    fun setSnackbarMessage(message: String) { _userMessage.value = message }

    fun setAuthenticated(isAuth: Boolean, role: UserRole = UserRole.STUDENT) {
        _isAuthenticated.value = isAuth
        _currentRole.value = role
        // Al iniciar sesión, si no tiene PIN, lo dejamos pasar para que lo cree.
        // Si ya tiene PIN, lo bloqueamos.
        _isLocked.value = isAuth && hasPinConfigured()
        if (isAuth) syncFromCloud()
    }

    fun signOut() {
        firebaseService.signOut()
        _isAuthenticated.value = false
        _currentRole.value = UserRole.STUDENT
        _studentProfile.value = null
        _isLocked.value = false
        _userMessage.value = "Sesión cerrada."
    }

    // --- LÓGICA DEL PIN ---
    fun hasPinConfigured(): Boolean = prefs.contains("user_pin")

    fun saveNewPin(newPin: String) {
        prefs.edit().putString("user_pin", newPin).apply()
        _isLocked.value = false
        _userMessage.value = "PIN de seguridad configurado correctamente."
    }

    fun toggleLock() {
        // Solo permitimos bloquear si ya tiene un PIN configurado
        if (hasPinConfigured()) {
            _isLocked.value = !_isLocked.value
        } else {
            _userMessage.value = "Configure un PIN primero para usar el bloqueo."
        }
    }

    fun unlockWithPin(pin: String): Boolean {
        val savedPin = prefs.getString("user_pin", null)
        return if (pin == savedPin) {
            _isLocked.value = false
            true
        } else false
    }

    fun toggleDiscreetMode() {
        _isDiscreetMode.value = !_isDiscreetMode.value
    }

    fun selectCourse(courseId: String) {
        _selectedCourseId.value = courseId
    }

    fun addCustomCourse(code: String, name: String, teacherName: String, teacherEmail: String) {
        val newCourse = Course(
            id = "course_${System.currentTimeMillis()}",
            code = code,
            name = name,
            teacherId = teacherEmail,
            teacherName = teacherName,
            semester = "2024-2",
            schedule = "Por definir"
        )
        val currentList = _courses.value.toMutableList()
        currentList.add(newCourse)
        _courses.value = currentList
        _userMessage.value = "Materia agregada."
    }

    fun saveFullStudentProfile(profile: StudentProfile) {
        viewModelScope.launch {
            _studentProfile.value = profile
            val result = firebaseService.saveStudentProfile(profile)
            result.onSuccess {
                _userMessage.value = "¡Perfil guardado exitosamente!"
            }.onFailure { err ->
                _userMessage.value = "Error al guardar: ${err.localizedMessage}"
            }
            logAudit(AuditActionType.UPDATE_PERMISSIONS, "Perfil", "Estudiante guardó su perfil.")
        }
    }

    fun grantOrUpdateConsent(courseId: String, shareAccommodations: Boolean, shareComm: Boolean, shareEmergency: Boolean, shareDiagnosis: Boolean) {
        viewModelScope.launch {
            val course = _courses.value.find { it.id == courseId } ?: return@launch
            val profile = _studentProfile.value ?: return@launch

            val existing = _consents.value.find { it.courseId == course.id }
            val consentId = existing?.id ?: "consent_${course.id}_${System.currentTimeMillis()}"

            val grant = ConsentGrant(
                id = consentId,
                studentId = profile.id,
                studentName = profile.fullName,
                teacherId = course.teacherId,
                teacherName = course.teacherName,
                courseId = course.id,
                courseName = "${course.code} ${course.name}",
                academicTerm = course.semester,
                shareAccommodations = shareAccommodations,
                shareCommunicationPrefs = shareComm,
                shareEmergencyProtocol = shareEmergency,
                shareClinicalDiagnosis = shareDiagnosis,
                grantedAtMillis = System.currentTimeMillis(),
                expiresAtMillis = System.currentTimeMillis() + (120L * 24 * 60 * 60 * 1000),
                isRevoked = false
            )

            firebaseService.saveConsentGrant(grant)

            val currentList = _consents.value.toMutableList()
            currentList.removeAll { it.id == consentId }
            currentList.add(grant)
            _consents.value = currentList

            logAudit(AuditActionType.GRANT_PERMISSIONS, course.name, "Permisos dados a ${course.teacherName}")
            _userMessage.value = "Permisos actualizados."
        }
    }

    fun revokeConsent(consentId: String) {
        viewModelScope.launch {
            val current = _consents.value.find { it.id == consentId } ?: return@launch
            val updated = current.copy(isRevoked = true)

            firebaseService.saveConsentGrant(updated)

            val currentList = _consents.value.toMutableList()
            currentList.removeAll { it.id == consentId }
            currentList.add(updated)
            _consents.value = currentList

            logAudit(AuditActionType.REVOKE_PERMISSIONS, current.courseId, "Acceso revocado.")
            _userMessage.value = "Acceso revocado."
        }
    }

    fun exportAndSharePdf(context: Context, courseName: String, teacherName: String, shareViaWhatsApp: Boolean) {
        viewModelScope.launch {
            val profile = _studentProfile.value ?: return@launch
            val file = pdfExportManager.generateClassroomSupportSheet(
                studentName = profile.fullName,
                studentNumber = profile.studentNumber,
                courseName = courseName,
                teacherName = teacherName,
                academicTerm = profile.semester,
                accommodations = profile.accommodations,
                communicationGuidelines = emptyList(),
                emergencyProtocol = profile.emergencyProtocol,
                disclosedDiagnosis = profile.clinicalDetails?.conditionName,
                expiresAtDate = "2025-12-31"
            )
            _lastExportedFile.value = file
            if (shareViaWhatsApp) ShareManager.sharePdfViaShareSheet(context, file, courseName)
            else ShareManager.viewPdfDocument(context, file)
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _allUsers.value = firebaseService.getAllUsers()
        }
    }

    fun approveInstitutionalRole(targetUid: String, newRole: String) {
        viewModelScope.launch {
            firebaseService.assignUserRole(targetUid, newRole)
            loadAllUsers()
        }
    }

    private fun logAudit(action: AuditActionType, course: String, details: String) {
        viewModelScope.launch {
            val user = firebaseService.currentUser
            val actorName = user?.email ?: "Usuario Desconocido"

            val logEntity = AuditLogEntity(
                id = "audit_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
                timestampMillis = System.currentTimeMillis(),
                actorName = actorName,
                actorRole = _currentRole.value.name,
                courseName = course,
                actionType = action.name,
                details = details
            )
            firebaseService.logAuditEvent(action, _currentRole.value, course, details, actorName)
        }
    }

    fun addCustomAccommodation(title: String, category: AccommodationCategory, instruction: String) {
        viewModelScope.launch {
            val current = _studentProfile.value ?: return@launch
            val existingList = current.accommodations.toMutableList()
            existingList.add(
                Accommodation(
                    id = "acc_${System.currentTimeMillis()}",
                    title = title,
                    category = category,
                    instructionForTeacher = instruction,
                    isMandatory = true
                )
            )
            val updated = current.copy(accommodations = existingList)
            saveFullStudentProfile(updated)
            _userMessage.value = "Apoyo agregado: $title"
        }
    }

    fun removeAccommodation(accId: String) {
        viewModelScope.launch {
            val current = _studentProfile.value ?: return@launch
            val updatedList = current.accommodations.filter { it.id != accId }
            val updated = current.copy(accommodations = updatedList)
            saveFullStudentProfile(updated)
            _userMessage.value = "Apoyo eliminado."
        }
    }
}