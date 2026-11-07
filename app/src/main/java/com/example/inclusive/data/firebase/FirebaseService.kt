package com.example.inclusive.data.firebase

import android.content.Context
import android.util.Log
import com.example.R
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseService(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // -------------------------------------------------------------
    // Authentication & Role Detection
    // -------------------------------------------------------------

    suspend fun getUserRole(uid: String? = currentUser?.uid): UserRole {
        val user = currentUser ?: return UserRole.STUDENT
        try {
            // 1. Authoritative check via Firebase Custom Claims on ID Token
            val tokenResult = user.getIdToken(false).await()
            val claimRole = tokenResult.claims["role"] as? String
            if (claimRole != null) {
                return when (claimRole.lowercase()) {
                    "teacher", "docente" -> UserRole.TEACHER
                    // TODO v2: "counseling" -> UserRole.COUNSELING
                    // TODO v2: "administrator", "admin" -> UserRole.ADMIN
                    else -> UserRole.STUDENT
                }
            }

            // 2. Authoritative fallback to Firestore user document
            val targetUid = uid ?: user.uid
            val userDoc = db.collection("users").document(targetUid).get().await()
            val docRole = userDoc.getString("role")
            if (docRole != null) {
                return when (docRole.lowercase()) {
                    "teacher", "docente" -> UserRole.TEACHER
                    // TODO v2: "counseling" -> UserRole.COUNSELING
                    // TODO v2: "administrator", "admin" -> UserRole.ADMIN
                    else -> UserRole.STUDENT
                }
            }
        } catch (e: Exception) {
            Log.w("FirebaseService", "Error resolving role from claims/document: ${e.message}")
        }
        return UserRole.STUDENT
    }

    suspend fun registerUser(
        fullName: String,
        email: String,
        password: String,
        studentId: String?
    ): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase registration returned null user.")

            // Create user document in Firestore with default STUDENT role
            val userRecord = hashMapOf(
                "id" to user.uid,
                "fullName" to fullName.trim(),
                "email" to email.trim(),
                "studentId" to (studentId?.trim() ?: ""),
                "role" to "student", // All newly registered users default to student
                "isApproved" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(user.uid).set(userRecord).await()

            logAuditEvent(
                action = AuditActionType.GRANT_PERMISSIONS,
                actorRole = UserRole.STUDENT,
                courseName = "Registration & Security Gateway",
                details = "New student account registered for $fullName ($email). Default student role provisioned."
            )

            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Registration failed", e)
            Result.failure(e)
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<UserRole> {
        return try {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            val role = getUserRole()
            logAuditEvent(
                action = AuditActionType.VIEW_CLASSROOM_PROFILE,
                actorRole = role,
                courseName = "Authentication Portal",
                details = "User signed in successfully with email credentials. Role: ${role.label}."
            )
            Result.success(role)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Login failed", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Password reset failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    // -------------------------------------------------------------
    // Student Support Profile (Firestore persistence)
    // -------------------------------------------------------------

    suspend fun saveStudentProfile(profile: StudentProfile): Result<Unit> {
        val uid = currentUser?.uid ?: profile.id
        return try {
            val docRef = db.collection("studentProfiles").document(uid)

            val accommodationsList = profile.accommodations.map { acc ->
                mapOf(
                    "id" to acc.id,
                    "title" to acc.title,
                    "category" to acc.category.name,
                    "instructionForTeacher" to acc.instructionForTeacher,
                    "isMandatory" to acc.isMandatory
                )
            }

            val commPrefsMap = mapOf(
                "writtenInstructionsOnly" to profile.communicationPreferences.writtenInstructionsOnly,
                "discreetNotifications" to profile.communicationPreferences.discreetNotifications,
                "avoidPublicSinglingOut" to profile.communicationPreferences.avoidPublicSinglingOut,
                "advanceNoticeOfChanges" to profile.communicationPreferences.advanceNoticeOfChanges
            )

            val emergencyMap = mapOf(
                "triggerWarnings" to profile.emergencyProtocol.triggerWarnings,
                "actionsToTake" to profile.emergencyProtocol.actionsToTake,
                "actionsToAvoid" to profile.emergencyProtocol.actionsToAvoid,
                "emergencyContactName" to profile.emergencyProtocol.emergencyContactName,
                "emergencyContactPhone" to profile.emergencyProtocol.emergencyContactPhone,
                "counselorContact" to profile.emergencyProtocol.counselorContact
            )

            val sensitiveMap = mapOf(
                "conditionName" to (profile.clinicalDetails?.conditionName ?: ""),
                "isVerifiedByCounseling" to (profile.clinicalDetails?.isVerifiedByCounseling ?: false),
                "verifiedBy" to (profile.clinicalDetails?.verifiedBy ?: ""),
                "verificationTimestamp" to (profile.clinicalDetails?.verificationTimestamp ?: 0L)
            )

            val payload = hashMapOf(
                "id" to uid,
                "studentUid" to uid,
                "fullName" to profile.fullName,
                "studentNumber" to profile.studentNumber,
                "program" to profile.program,
                "semester" to profile.semester,
                "contactEmail" to profile.contactEmail,
                "contactPhone" to profile.contactPhone,
                "accommodations" to accommodationsList,
                "communicationPreferences" to commPrefsMap,
                "classroomStrategies" to profile.emergencyProtocol.actionsToTake,
                "emergencyInformation" to emergencyMap,
                "sensitiveInformation" to sensitiveMap,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(payload, SetOptions.merge()).await()

            logAuditEvent(
                action = AuditActionType.UPDATE_PERMISSIONS,
                actorRole = UserRole.STUDENT,
                courseName = "Academic Support Profile",
                details = "Support profile successfully updated in Cloud Firestore with ${profile.accommodations.size} accommodations."
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Failed to save student profile", e)
            Result.failure(e)
        }
    }

    suspend fun getStudentProfile(studentUid: String? = currentUser?.uid): StudentProfile? {
        val uid = studentUid ?: currentUser?.uid ?: return null
        return try {
            val doc = db.collection("studentProfiles").document(uid).get().await()
            if (!doc.exists()) return null

            val accommodationsRaw = doc.get("accommodations") as? List<Map<String, Any>> ?: emptyList()
            val accommodations = accommodationsRaw.map { map ->
                val catName = map["category"] as? String ?: AccommodationCategory.CLASSROOM.name
                val cat = try { AccommodationCategory.valueOf(catName) } catch (e: Exception) { AccommodationCategory.CLASSROOM }
                Accommodation(
                    id = map["id"] as? String ?: UUID.randomUUID().toString(),
                    title = map["title"] as? String ?: "",
                    category = cat,
                    instructionForTeacher = map["instructionForTeacher"] as? String ?: "",
                    isMandatory = map["isMandatory"] as? Boolean ?: true
                )
            }

            val commRaw = doc.get("communicationPreferences") as? Map<String, Any> ?: emptyMap()
            val commPrefs = CommunicationPreferences(
                writtenInstructionsOnly = commRaw["writtenInstructionsOnly"] as? Boolean ?: true,
                discreetNotifications = commRaw["discreetNotifications"] as? Boolean ?: true,
                avoidPublicSinglingOut = commRaw["avoidPublicSinglingOut"] as? Boolean ?: true,
                advanceNoticeOfChanges = commRaw["advanceNoticeOfChanges"] as? Boolean ?: true
            )

            val emergRaw = doc.get("emergencyInformation") as? Map<String, Any> ?: emptyMap()
            val emerg = EmergencyCrisisProtocol(
                triggerWarnings = (emergRaw["triggerWarnings"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                actionsToTake = (emergRaw["actionsToTake"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                actionsToAvoid = (emergRaw["actionsToAvoid"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                emergencyContactName = emergRaw["emergencyContactName"] as? String ?: "",
                emergencyContactPhone = emergRaw["emergencyContactPhone"] as? String ?: "",
                counselorContact = emergRaw["counselorContact"] as? String ?: ""
            )

            val sensitiveRaw = doc.get("sensitiveInformation") as? Map<String, Any> ?: emptyMap()
            val conditionName = sensitiveRaw["conditionName"] as? String
            val clinicalDetails = if (!conditionName.isNullOrBlank()) {
                ClinicalDetails(
                    conditionName = conditionName,
                    isVerifiedByCounseling = sensitiveRaw["isVerifiedByCounseling"] as? Boolean ?: false,
                    verifiedBy = sensitiveRaw["verifiedBy"] as? String ?: "Student Wellbeing Center",
                    verificationTimestamp = (sensitiveRaw["verificationTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            } else null

            StudentProfile(
                id = uid,
                studentNumber = doc.getString("studentNumber") ?: "",
                fullName = doc.getString("fullName") ?: "",
                program = doc.getString("program") ?: "",
                semester = doc.getString("semester") ?: "",
                contactEmail = doc.getString("contactEmail") ?: "",
                contactPhone = doc.getString("contactPhone") ?: "",
                clinicalDetails = clinicalDetails,
                accommodations = accommodations,
                communicationPreferences = commPrefs,
                emergencyProtocol = emerg
            )
        } catch (e: Exception) {
            Log.e("FirebaseService", "Failed to fetch student profile", e)
            null
        }
    }

    // -------------------------------------------------------------
    // Granular Consent Grants
    // -------------------------------------------------------------

    suspend fun saveConsentGrant(grant: ConsentGrant): Result<Unit> {
        return try {
            val payload = hashMapOf(
                "id" to grant.id,
                "studentUid" to grant.studentId,
                "studentName" to grant.studentName,
                "teacherUid" to grant.teacherId,
                "teacherName" to grant.teacherName,
                "courseId" to grant.courseId,
                "courseName" to grant.courseName,
                "academicTerm" to grant.academicTerm,
                "shareAccommodations" to grant.shareAccommodations,
                "shareCommunicationPrefs" to grant.shareCommunicationPrefs,
                "shareEmergencyProtocol" to grant.shareEmergencyProtocol,
                "shareClinicalDiagnosis" to grant.shareClinicalDiagnosis, // Protected field
                "isRevoked" to grant.isRevoked,
                "grantedAtMillis" to grant.grantedAtMillis,
                "expiresAtMillis" to grant.expiresAtMillis,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("consents").document(grant.id).set(payload, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Failed to save consent", e)
            Result.failure(e)
        }
    }

    suspend fun getConsents(): List<ConsentGrant> {
        return try {
            val snapshot = db.collection("consents").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val sUid = doc.getString("studentUid") ?: return@mapNotNull null
                ConsentGrant(
                    id = id,
                    studentId = sUid,
                    studentName = doc.getString("studentName") ?: "",
                    teacherId = doc.getString("teacherUid") ?: "",
                    teacherName = doc.getString("teacherName") ?: "",
                    courseId = doc.getString("courseId") ?: "",
                    courseName = doc.getString("courseName") ?: "",
                    academicTerm = doc.getString("academicTerm") ?: "Fall 2026",
                    shareAccommodations = doc.getBoolean("shareAccommodations") ?: true,
                    shareCommunicationPrefs = doc.getBoolean("shareCommunicationPrefs") ?: true,
                    shareEmergencyProtocol = doc.getBoolean("shareEmergencyProtocol") ?: false,
                    shareClinicalDiagnosis = doc.getBoolean("shareClinicalDiagnosis") ?: false,
                    grantedAtMillis = doc.getLong("grantedAtMillis") ?: System.currentTimeMillis(),
                    expiresAtMillis = doc.getLong("expiresAtMillis") ?: (System.currentTimeMillis() + 100000000L),
                    isRevoked = doc.getBoolean("isRevoked") ?: false
                )
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Failed to fetch consents", e)
            emptyList()
        }
    }

    // -------------------------------------------------------------
    // Audit Logging
    // -------------------------------------------------------------

    suspend fun logAuditEvent(
        action: AuditActionType,
        actorRole: UserRole,
        courseName: String,
        details: String,
        actorNameOverride: String? = null
    ) {
        try {
            val actor = actorNameOverride ?: (currentUser?.displayName ?: currentUser?.email ?: "Authorized User")
            val logId = "audit_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
            val payload = hashMapOf(
                "id" to logId,
                "actorUid" to (currentUser?.uid ?: "system"),
                "actorName" to actor,
                "actorRole" to actorRole.name,
                "courseName" to courseName,
                "actionType" to action.name,
                "details" to details,
                "timestampMillis" to System.currentTimeMillis(),
                "timestamp" to FieldValue.serverTimestamp()
            )
            db.collection("auditLogs").document(logId).set(payload).await()
        } catch (e: Exception) {
            Log.w("FirebaseService", "Could not write audit log: ${e.message}")
        }
    }

    suspend fun getAuditLogs(): List<AuditLogEntry> {
        return try {
            val snapshot = db.collection("auditLogs").orderBy("timestampMillis", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(50).get().await()
            snapshot.documents.mapNotNull { doc ->
                val action = try {
                    AuditActionType.valueOf(doc.getString("actionType") ?: AuditActionType.VIEW_CLASSROOM_PROFILE.name)
                } catch (e: Exception) {
                    AuditActionType.VIEW_CLASSROOM_PROFILE
                }
                val role = try {
                    UserRole.valueOf(doc.getString("actorRole") ?: UserRole.STUDENT.name)
                } catch (e: Exception) {
                    UserRole.STUDENT
                }
                AuditLogEntry(
                    id = doc.id,
                    timestampMillis = doc.getLong("timestampMillis") ?: System.currentTimeMillis(),
                    actorName = doc.getString("actorName") ?: "User",
                    actorRole = role,
                    courseName = doc.getString("courseName") ?: "System",
                    actionType = action,
                    details = doc.getString("details") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Failed to read audit logs", e)
            emptyList()
        }
    }

    // -------------------------------------------------------------
    // Administrative User Management
    // -------------------------------------------------------------

    suspend fun getAllUsers(): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection("users").get().await()
            snapshot.documents.map { doc ->
                doc.data ?: emptyMap()
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Failed to get all users", e)
            emptyList()
        }
    }

    suspend fun assignUserRole(targetUid: String, newRole: String): Result<Unit> {
        return try {
            db.collection("users").document(targetUid).update(
                mapOf(
                    "role" to newRole.lowercase(),
                    "isApproved" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            logAuditEvent(
                action = AuditActionType.UPDATE_PERMISSIONS,
                actorRole = UserRole.TEACHER, // TODO v2: UserRole.ADMIN
                courseName = "Centro de Administración Institucional",
                details = "Asignación de rol institucional '$newRole' para UID $targetUid."
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Failed to assign role", e)
            Result.failure(e)
        }
    }
}
