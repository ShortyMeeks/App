package com.example.inclusive.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inclusive.data.AppDatabase
import com.example.inclusive.data.AuditLogEntity
import com.example.inclusive.data.ConsentGrantEntity
import com.example.inclusive.data.CourseEntity
import com.example.inclusive.data.JsonConverters
import com.example.inclusive.data.StudentProfileEntity
import com.example.inclusive.data.firebase.FirebaseService
import com.example.inclusive.model.Accommodation
import com.example.inclusive.model.AccommodationCategory
import com.example.inclusive.model.AuditActionType
import com.example.inclusive.model.AuditLogEntry
import com.example.inclusive.model.ClinicalDetails
import com.example.inclusive.model.CommunicationPreferences
import com.example.inclusive.model.ConsentGrant
import com.example.inclusive.model.Course
import com.example.inclusive.model.EmergencyCrisisProtocol
import com.example.inclusive.model.StudentProfile
import com.example.inclusive.model.UserRole
import com.example.inclusive.pdf.PdfExportManager
import com.example.inclusive.pdf.ShareManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class InclusiveViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val dao = db.dao()
    val firebaseService = FirebaseService(application)
    private val pdfExportManager = PdfExportManager(application)

    // Current Active Role
    private val _currentRole = MutableStateFlow(UserRole.STUDENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Auth state
    private val _isAuthenticated = MutableStateFlow(firebaseService.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Discreet Mode ("Privacy Shield" disguise)
    private val _isDiscreetMode = MutableStateFlow(false)
    val isDiscreetMode: StateFlow<Boolean> = _isDiscreetMode.asStateFlow()

    // Security PIN Lock
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()
    val securityPin = "1234"

    // Selected Course for Teacher View
    private val _selectedCourseId = MutableStateFlow<String?>("course_cs301")
    val selectedCourseId: StateFlow<String?> = _selectedCourseId.asStateFlow()

    // Last exported file
    private val _lastExportedFile = MutableStateFlow<File?>(null)
    val lastExportedFile: StateFlow<File?> = _lastExportedFile.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Users list for Administrator dashboard
    private val _allUsers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val allUsers: StateFlow<List<Map<String, Any>>> = _allUsers.asStateFlow()

    // Student profile flow (persisted in Room with Firestore sync)
    val studentProfile: StateFlow<StudentProfile?> = dao.getStudentProfileFlow("student_sofia_2024")
        .combine(_currentRole) { entity, _ ->
            entity?.let { toDomainProfile(it) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Courses flow
    val courses: StateFlow<List<Course>> = dao.getAllCoursesFlow()
        .combine(_currentRole) { list, _ ->
            list.map {
                Course(
                    id = it.id,
                    code = it.code,
                    name = it.name,
                    teacherId = it.teacherId,
                    teacherName = it.teacherName,
                    schedule = it.schedule,
                    semester = it.semester
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Consents flow
    val consents: StateFlow<List<ConsentGrant>> = dao.getAllConsentsFlow()
        .combine(_currentRole) { list, _ ->
            list.map {
                ConsentGrant(
                    id = it.id,
                    studentId = it.studentId,
                    studentName = it.studentName,
                    teacherId = it.teacherId,
                    teacherName = it.teacherName,
                    courseId = it.courseId,
                    courseName = it.courseName,
                    academicTerm = it.academicTerm,
                    shareAccommodations = it.shareAccommodations,
                    shareCommunicationPrefs = it.shareCommunicationPrefs,
                    shareEmergencyProtocol = it.shareEmergencyProtocol,
                    shareClinicalDiagnosis = it.shareClinicalDiagnosis,
                    grantedAtMillis = it.grantedAtMillis,
                    expiresAtMillis = it.expiresAtMillis,
                    isRevoked = it.isRevoked
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audit logs flow
    val auditLogs: StateFlow<List<AuditLogEntry>> = dao.getAllAuditLogsFlow()
        .combine(_currentRole) { list, _ ->
            list.map {
                val action = try {
                    AuditActionType.valueOf(it.actionType)
                } catch (e: Exception) {
                    AuditActionType.VIEW_CLASSROOM_PROFILE
                }
                val role = try {
                    UserRole.valueOf(it.actorRole)
                } catch (e: Exception) {
                    UserRole.STUDENT
                }
                AuditLogEntry(
                    id = it.id,
                    timestampMillis = it.timestampMillis,
                    actorName = it.actorName,
                    actorRole = role,
                    courseName = it.courseName,
                    actionType = action,
                    details = it.details
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        syncFromCloud()
    }

    fun syncFromCloud() {
        viewModelScope.launch {
            // Check if user has saved profile in Firestore
            val cloudProfile = firebaseService.getStudentProfile()
            if (cloudProfile != null) {
                dao.insertOrUpdateProfile(toProfileEntity(cloudProfile))
            }
            // Fetch users if admin
            loadAllUsers()
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun setSnackbarMessage(message: String) {
        _userMessage.value = message
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        if (role == UserRole.TEACHER) {
            if (_selectedCourseId.value == null) {
                _selectedCourseId.value = "course_cs301"
            }
            logTeacherAccessForSelectedCourse()
        }
        // TODO v2: Support for COUNSELING and ADMIN roles in second release
        // if (role == UserRole.ADMIN) { loadAllUsers() }
    }

    fun setAuthenticated(isAuth: Boolean, role: UserRole = UserRole.STUDENT) {
        _isAuthenticated.value = isAuth
        _currentRole.value = role
        if (isAuth) {
            syncFromCloud()
        }
    }

    fun signOut() {
        firebaseService.signOut()
        _isAuthenticated.value = false
        _currentRole.value = UserRole.STUDENT
        _userMessage.value = "Successfully logged out."
    }

    fun selectCourse(courseId: String) {
        _selectedCourseId.value = courseId
        if (_currentRole.value == UserRole.TEACHER) {
            logTeacherAccessForSelectedCourse()
        }
    }

    fun toggleDiscreetMode() {
        val newState = !_isDiscreetMode.value
        _isDiscreetMode.value = newState
        logAudit(
            action = AuditActionType.DISCREET_MODE,
            course = "System Privacy Shield",
            details = if (newState) "Discreet disguise screen activated by student." else "Discreet disguise deactivated."
        )
    }

    fun toggleLock() {
        _isLocked.value = !_isLocked.value
    }

    fun unlockWithPin(pin: String): Boolean {
        return if (pin == securityPin) {
            _isLocked.value = false
            true
        } else {
            false
        }
    }

    private fun logTeacherAccessForSelectedCourse() {
        val cId = _selectedCourseId.value ?: return
        viewModelScope.launch {
            val course = dao.getAllCourses().find { it.id == cId } ?: return@launch
            val logEntity = AuditLogEntity(
                id = "audit_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
                timestampMillis = System.currentTimeMillis(),
                actorName = course.teacherName,
                actorRole = UserRole.TEACHER.name,
                courseName = "${course.code} ${course.name}",
                actionType = AuditActionType.VIEW_CLASSROOM_PROFILE.name,
                details = "Instructor accessed classroom accommodations view. Clinical data withheld by student consent rules."
            )
            dao.insertAuditLog(logEntity)
            firebaseService.logAuditEvent(
                action = AuditActionType.VIEW_CLASSROOM_PROFILE,
                actorRole = UserRole.TEACHER,
                courseName = "${course.code} ${course.name}",
                details = "Instructor accessed classroom accommodations view.",
                actorNameOverride = course.teacherName
            )
        }
    }

    // -------------------------------------------------------------
    // Full Profile Creation & Firestore Persistence (Feature 6 & 7)
    // -------------------------------------------------------------

    fun saveFullStudentProfile(profile: StudentProfile) {
        viewModelScope.launch {
            // 1. Save to local Room database for offline cache
            val entity = toProfileEntity(profile)
            dao.insertOrUpdateProfile(entity)

            // 2. Persist to Cloud Firestore
            val result = firebaseService.saveStudentProfile(profile)
            result.onSuccess {
                _userMessage.value = "Support Profile successfully saved to Cloud Firestore!"
            }.onFailure { err ->
                _userMessage.value = "Saved locally. Cloud sync pending: ${err.localizedMessage}"
            }

            logAudit(
                action = AuditActionType.UPDATE_PERMISSIONS,
                course = "Profile Management",
                details = "Student saved support profile with ${profile.accommodations.size} accommodations."
            )
        }
    }

    fun grantOrUpdateConsent(
        courseId: String,
        shareAccommodations: Boolean,
        shareComm: Boolean,
        shareEmergency: Boolean,
        shareDiagnosis: Boolean
    ) {
        viewModelScope.launch {
            val course = dao.getAllCourses().find { it.id == courseId } ?: return@launch
            val profile = dao.getStudentProfile("student_sofia_2024") ?: return@launch

            val existing = dao.getConsent(profile.id, course.id)
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
                shareClinicalDiagnosis = shareDiagnosis, // Controlled explicitly by student
                grantedAtMillis = System.currentTimeMillis(),
                expiresAtMillis = System.currentTimeMillis() + (120L * 24 * 60 * 60 * 1000),
                isRevoked = false
            )

            // 1. Save locally in Room
            val entity = ConsentGrantEntity(
                id = grant.id,
                studentId = grant.studentId,
                studentName = grant.studentName,
                teacherId = grant.teacherId,
                teacherName = grant.teacherName,
                courseId = grant.courseId,
                courseName = grant.courseName,
                academicTerm = grant.academicTerm,
                shareAccommodations = grant.shareAccommodations,
                shareCommunicationPrefs = grant.shareCommunicationPrefs,
                shareEmergencyProtocol = grant.shareEmergencyProtocol,
                shareClinicalDiagnosis = grant.shareClinicalDiagnosis,
                grantedAtMillis = grant.grantedAtMillis,
                expiresAtMillis = grant.expiresAtMillis,
                isRevoked = grant.isRevoked
            )
            dao.insertOrUpdateConsent(entity)

            // 2. Persist to Cloud Firestore
            firebaseService.saveConsentGrant(grant)

            val details = buildString {
                append("Permissions configured: Accommodations=$shareAccommodations, ")
                append("Communication=$shareComm, Emergency=$shareEmergency, ")
                append("Diagnosis=${if (shareDiagnosis) "Disclosed" else "WITHHELD (Protected)"}.")
            }

            logAudit(
                action = if (existing == null) AuditActionType.GRANT_PERMISSIONS else AuditActionType.UPDATE_PERMISSIONS,
                course = "${course.code} ${course.name}",
                details = details
            )
            _userMessage.value = "Permissions successfully updated for ${course.code}."
        }
    }

    fun revokeConsent(consentId: String) {
        viewModelScope.launch {
            val current = consents.value.find { it.id == consentId } ?: return@launch
            val updated = ConsentGrant(
                id = current.id,
                studentId = current.studentId,
                studentName = current.studentName,
                teacherId = current.teacherId,
                teacherName = current.teacherName,
                courseId = current.courseId,
                courseName = current.courseName,
                academicTerm = current.academicTerm,
                shareAccommodations = false,
                shareCommunicationPrefs = false,
                shareEmergencyProtocol = false,
                shareClinicalDiagnosis = false,
                grantedAtMillis = current.grantedAtMillis,
                expiresAtMillis = current.expiresAtMillis,
                isRevoked = true
            )

            // Update in Room
            dao.insertOrUpdateConsent(
                ConsentGrantEntity(
                    id = updated.id,
                    studentId = updated.studentId,
                    studentName = updated.studentName,
                    teacherId = updated.teacherId,
                    teacherName = updated.teacherName,
                    courseId = updated.courseId,
                    courseName = updated.courseName,
                    academicTerm = updated.academicTerm,
                    shareAccommodations = false,
                    shareCommunicationPrefs = false,
                    shareEmergencyProtocol = false,
                    shareClinicalDiagnosis = false,
                    grantedAtMillis = updated.grantedAtMillis,
                    expiresAtMillis = updated.expiresAtMillis,
                    isRevoked = true
                )
            )

            // Update in Cloud Firestore
            firebaseService.saveConsentGrant(updated)

            logAudit(
                action = AuditActionType.REVOKE_PERMISSIONS,
                course = current.courseName,
                details = "Student exercised ARCO revocation right. Access immediately severed for ${current.teacherName}."
            )
            _userMessage.value = "Access revoked for ${current.courseName}. Teacher can no longer view support sheet."
        }
    }

    fun exportAndSharePdf(context: Context, courseName: String, teacherName: String, shareViaWhatsApp: Boolean) {
        viewModelScope.launch {
            val profile = studentProfile.value ?: return@launch
            val activeConsents = consents.value
            val courseConsent = activeConsents.find { it.courseName.contains(courseName, ignoreCase = true) }

            val shareAcc = courseConsent?.shareAccommodations ?: true
            val shareComm = courseConsent?.shareCommunicationPrefs ?: true
            val shareEmerg = courseConsent?.shareEmergencyProtocol ?: false
            val shareDiag = courseConsent?.shareClinicalDiagnosis ?: false

            val filteredAccommodations = if (shareAcc) profile.accommodations else emptyList()
            val filteredCommGuidelines = if (shareComm) {
                listOfNotNull(
                    if (profile.communicationPreferences.writtenInstructionsOnly) "Provide oral instructions in written format" else null,
                    if (profile.communicationPreferences.discreetNotifications) "Communicate sensitive feedback discreetly (private email/note)" else null,
                    if (profile.communicationPreferences.avoidPublicSinglingOut) "Avoid calling out or singling out student publicly in amphitheater" else null,
                    if (profile.communicationPreferences.advanceNoticeOfChanges) "Provide advance notice before sudden changes to syllabus or deadlines" else null
                )
            } else emptyList()

            val filteredEmergency = if (shareEmerg) profile.emergencyProtocol else null
            val filteredDiagnosis = if (shareDiag) profile.clinicalDetails?.conditionName else null

            val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiry = dateFmt.format(Date(courseConsent?.expiresAtMillis ?: (System.currentTimeMillis() + 100000000L)))

            val file = pdfExportManager.generateClassroomSupportSheet(
                studentName = profile.fullName,
                studentNumber = profile.studentNumber,
                courseName = courseName,
                teacherName = teacherName,
                academicTerm = profile.semester,
                accommodations = filteredAccommodations,
                communicationGuidelines = filteredCommGuidelines,
                emergencyProtocol = filteredEmergency,
                disclosedDiagnosis = filteredDiagnosis,
                expiresAtDate = expiry
            )

            _lastExportedFile.value = file

            val actionType = if (shareViaWhatsApp) AuditActionType.PDF_SHARE_WHATSAPP else AuditActionType.PDF_EXPORT_DOCUMENT
            logAudit(
                action = actionType,
                course = courseName,
                details = "Generated signed PDF document with watermark. Diagnosis status: ${if (filteredDiagnosis != null) "Disclosed" else "WITHHELD"}."
            )

            if (shareViaWhatsApp) {
                ShareManager.sharePdfViaShareSheet(context, file, courseName)
            } else {
                ShareManager.viewPdfDocument(context, file)
            }
        }
    }

    fun addCustomAccommodation(title: String, category: AccommodationCategory, instruction: String) {
        viewModelScope.launch {
            val current = dao.getStudentProfile("student_sofia_2024") ?: return@launch
            val existingList = JsonConverters.jsonToAccommodations(current.accommodationsJson).toMutableList()
            existingList.add(
                Accommodation(
                    id = "acc_${System.currentTimeMillis()}",
                    title = title,
                    category = category,
                    instructionForTeacher = instruction,
                    isMandatory = true
                )
            )
            val updated = current.copy(accommodationsJson = JsonConverters.accommodationsToJson(existingList))
            dao.insertOrUpdateProfile(updated)
            _userMessage.value = "Added accommodation: $title"
        }
    }

    fun removeAccommodation(accId: String) {
        viewModelScope.launch {
            val current = dao.getStudentProfile("student_sofia_2024") ?: return@launch
            val existingList = JsonConverters.jsonToAccommodations(current.accommodationsJson).filter { it.id != accId }
            val updated = current.copy(accommodationsJson = JsonConverters.accommodationsToJson(existingList))
            dao.insertOrUpdateProfile(updated)
            _userMessage.value = "Accommodation removed."
        }
    }

    fun verifyProfileCounseling(counselorName: String) {
        viewModelScope.launch {
            val current = dao.getStudentProfile("student_sofia_2024") ?: return@launch
            val updated = current.copy(
                isVerifiedByCounseling = true,
                verifiedBy = counselorName,
                verificationTimestamp = System.currentTimeMillis()
            )
            dao.insertOrUpdateProfile(updated)
            logAudit(
                action = AuditActionType.PROFILE_VERIFIED,
                course = "Institutional Health Registry",
                details = "Institutional validation confirmed by $counselorName with university accreditation stamp."
            )
            _userMessage.value = "Profile officially verified by $counselorName."
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _allUsers.value = firebaseService.getAllUsers()
        }
    }

    fun approveInstitutionalRole(targetUid: String, newRole: String) {
        viewModelScope.launch {
            val res = firebaseService.assignUserRole(targetUid, newRole)
            res.onSuccess {
                _userMessage.value = "Approved institutional role: $newRole for UID: $targetUid"
                loadAllUsers()
            }.onFailure { err ->
                _userMessage.value = "Role update failed: ${err.localizedMessage}"
            }
        }
    }

    private fun logAudit(action: AuditActionType, course: String, details: String) {
        viewModelScope.launch {
            val role = _currentRole.value
            val actorName = when (role) {
                UserRole.STUDENT -> "Sofía Hernández Cruz (Estudiante)"
                UserRole.TEACHER -> "Docente Titular (Prof. Ruiz / Delgado)"
                // TODO v2: UserRole.COUNSELING -> "Dr. Elena Morales (Orientación)"
                // TODO v2: UserRole.ADMIN -> "Oficina de Auditoría y Cumplimiento"
            }
            dao.insertAuditLog(
                AuditLogEntity(
                    id = "audit_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
                    timestampMillis = System.currentTimeMillis(),
                    actorName = actorName,
                    actorRole = role.name,
                    courseName = course,
                    actionType = action.name,
                    details = details
                )
            )
            firebaseService.logAuditEvent(
                action = action,
                actorRole = role,
                courseName = course,
                details = details,
                actorNameOverride = actorName
            )
        }
    }

    private fun toProfileEntity(profile: StudentProfile): StudentProfileEntity {
        return StudentProfileEntity(
            id = profile.id,
            studentNumber = profile.studentNumber,
            fullName = profile.fullName,
            program = profile.program,
            semester = profile.semester,
            contactEmail = profile.contactEmail,
            contactPhone = profile.contactPhone,
            clinicalConditionName = profile.clinicalDetails?.conditionName,
            isVerifiedByCounseling = profile.clinicalDetails?.isVerifiedByCounseling ?: false,
            verifiedBy = profile.clinicalDetails?.verifiedBy,
            verificationTimestamp = profile.clinicalDetails?.verificationTimestamp ?: 0L,
            accommodationsJson = JsonConverters.accommodationsToJson(profile.accommodations),
            communicationPrefsJson = JsonConverters.communicationPrefsToJson(profile.communicationPreferences),
            emergencyProtocolJson = JsonConverters.emergencyProtocolToJson(profile.emergencyProtocol)
        )
    }

    private fun toDomainProfile(entity: StudentProfileEntity): StudentProfile {
        return StudentProfile(
            id = entity.id,
            studentNumber = entity.studentNumber,
            fullName = entity.fullName,
            program = entity.program,
            semester = entity.semester,
            contactEmail = entity.contactEmail,
            contactPhone = entity.contactPhone,
            clinicalDetails = if (entity.clinicalConditionName != null) {
                ClinicalDetails(
                    conditionName = entity.clinicalConditionName,
                    isVerifiedByCounseling = entity.isVerifiedByCounseling,
                    verifiedBy = entity.verifiedBy ?: "Student Wellbeing Center",
                    verificationTimestamp = entity.verificationTimestamp
                )
            } else null,
            accommodations = JsonConverters.jsonToAccommodations(entity.accommodationsJson),
            communicationPreferences = JsonConverters.jsonToCommunicationPrefs(entity.communicationPrefsJson),
            emergencyProtocol = JsonConverters.jsonToEmergencyProtocol(entity.emergencyProtocolJson)
        )
    }
}
