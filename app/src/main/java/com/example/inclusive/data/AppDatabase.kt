package com.example.inclusive.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.inclusive.model.Accommodation
import com.example.inclusive.model.AccommodationCategory
import com.example.inclusive.model.AuditActionType
import com.example.inclusive.model.CommunicationPreferences
import com.example.inclusive.model.EmergencyCrisisProtocol
import com.example.inclusive.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentProfileEntity::class,
        CourseEntity::class,
        ConsentGrantEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): InclusiveDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inclusive_profile_database.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch(Dispatchers.IO) {
                            populateInitialData(getDatabase(context, scope).dao())
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(dao: InclusiveDao) {
            // 1. Initial Student Profile (Sofia Hernández)
            val accommodations = listOf(
                Accommodation(
                    id = "acc_exam_time",
                    title = "Additional Examination Time (+50%)",
                    category = AccommodationCategory.EXAMINATION,
                    instructionForTeacher = "Grant 1.5x standard exam duration in a quiet room with minimal distractions.",
                    isMandatory = true
                ),
                Accommodation(
                    id = "acc_break_leave",
                    title = "Permission for 5-Minute Sensory Breaks",
                    category = AccommodationCategory.SENSORY_BREAKS,
                    instructionForTeacher = "Allow student to step outside the classroom for up to 5 minutes to decompress without requiring public justification.",
                    isMandatory = true
                ),
                Accommodation(
                    id = "acc_written_guide",
                    title = "Written Instructions & Project Rubrics",
                    category = AccommodationCategory.COMMUNICATION,
                    instructionForTeacher = "Provide oral instructions in written format (syllabus/email/portal) to prevent working memory overload.",
                    isMandatory = true
                ),
                Accommodation(
                    id = "acc_preferential_seat",
                    title = "Preferential Classroom Seating",
                    category = AccommodationCategory.CLASSROOM,
                    instructionForTeacher = "Reserve seating near the aisle or room exit to facilitate low-stress sensory break exits.",
                    isMandatory = false
                ),
                Accommodation(
                    id = "acc_oral_avoid",
                    title = "Adapted Oral Presentation Format",
                    category = AccommodationCategory.EXAMINATION,
                    instructionForTeacher = "Avoid surprise unannounced cold-calling. Allow pre-recorded video or 1-on-1 presentations instead of mandatory amphitheater public speaking.",
                    isMandatory = true
                )
            )

            val commPrefs = CommunicationPreferences(
                writtenInstructionsOnly = true,
                discreetNotifications = true,
                avoidPublicSinglingOut = true,
                advanceNoticeOfChanges = true
            )

            val emergency = EmergencyCrisisProtocol(
                triggerWarnings = listOf(
                    "Prolonged chaotic acoustic noise",
                    "Unexpected physical touch without verbal warning",
                    "Public confrontation or shouting in the classroom"
                ),
                actionsToTake = listOf(
                    "Offer student a quiet space or allow them to sit outside",
                    "Speak in a calm, neutral, low-volume tone",
                    "Contact Student Wellbeing Counselor if disorientation lasts >10 minutes"
                ),
                actionsToAvoid = listOf(
                    "Do NOT grab, touch, or restrict the student physically",
                    "Do NOT crowd the student or allow peers to form a circle",
                    "Do NOT demand verbal explanations during an active crisis"
                ),
                emergencyContactName = "Mateo Hernández (Brother)",
                emergencyContactPhone = "+52 (55) 4123-9876",
                counselorContact = "Dr. Elena Morales - Health Services (Ext. 402)"
            )

            val profileEntity = StudentProfileEntity(
                id = "student_sofia_2024",
                studentNumber = "20248812",
                fullName = "Sofía Hernández Cruz",
                program = "B.S. Computer Systems Engineering",
                semester = "5th Semester (Fall 2026)",
                contactEmail = "sofia.hernandez@university.edu.mx",
                contactPhone = "+52 (55) 9876-5432",
                clinicalConditionName = "ADHD (Inattentive) & Sensory Overload Sensitivity",
                isVerifiedByCounseling = true,
                verifiedBy = "Dr. Elena Morales (University Counseling Center)",
                verificationTimestamp = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000),
                accommodationsJson = JsonConverters.accommodationsToJson(accommodations),
                communicationPrefsJson = JsonConverters.communicationPrefsToJson(commPrefs),
                emergencyProtocolJson = JsonConverters.emergencyProtocolToJson(emergency)
            )
            dao.insertOrUpdateProfile(profileEntity)

            // 2. Initial Courses
            val courses = listOf(
                CourseEntity(
                    id = "course_cs301",
                    code = "CS-301",
                    name = "Design & Analysis of Algorithms",
                    teacherId = "teacher_ruiz",
                    teacherName = "Prof. Dr. Roberto Ruiz",
                    schedule = "Mon / Wed 10:00 - 12:00 (Room 304)",
                    semester = "Fall 2026"
                ),
                CourseEntity(
                    id = "course_cs304",
                    code = "CS-304",
                    name = "Software Architecture & Security",
                    teacherId = "teacher_delgado",
                    teacherName = "Mtra. Carmen Delgado",
                    schedule = "Tue / Thu 08:00 - 10:00 (Lab 2B)",
                    semester = "Fall 2026"
                ),
                CourseEntity(
                    id = "course_math202",
                    code = "MATH-202",
                    name = "Applied Linear Algebra",
                    teacherId = "teacher_vega",
                    teacherName = "Ing. Carlos Vega",
                    schedule = "Friday 09:00 - 13:00 (Room 102)",
                    semester = "Fall 2026"
                )
            )
            dao.insertCourses(courses)

            // 3. Initial Consent Grants
            val consents = listOf(
                ConsentGrantEntity(
                    id = "consent_cs301",
                    studentId = "student_sofia_2024",
                    studentName = "Sofía Hernández Cruz",
                    teacherId = "teacher_ruiz",
                    teacherName = "Prof. Dr. Roberto Ruiz",
                    courseId = "course_cs301",
                    courseName = "CS-301 Design & Analysis of Algorithms",
                    academicTerm = "Fall 2026",
                    shareAccommodations = true,
                    shareCommunicationPrefs = true,
                    shareEmergencyProtocol = false,
                    shareClinicalDiagnosis = false, // Strictly withheld by student!
                    grantedAtMillis = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000),
                    expiresAtMillis = System.currentTimeMillis() + (115L * 24 * 60 * 60 * 1000),
                    isRevoked = false
                ),
                ConsentGrantEntity(
                    id = "consent_cs304",
                    studentId = "student_sofia_2024",
                    studentName = "Sofía Hernández Cruz",
                    teacherId = "teacher_delgado",
                    teacherName = "Mtra. Carmen Delgado",
                    courseId = "course_cs304",
                    courseName = "CS-304 Software Architecture & Security",
                    academicTerm = "Fall 2026",
                    shareAccommodations = true,
                    shareCommunicationPrefs = true,
                    shareEmergencyProtocol = true, // Emergency protocol shared
                    shareClinicalDiagnosis = false, // Strictly withheld
                    grantedAtMillis = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
                    expiresAtMillis = System.currentTimeMillis() + (113L * 24 * 60 * 60 * 1000),
                    isRevoked = false
                )
            )
            dao.insertConsents(consents)

            // 4. Initial Audit Logs
            val initialLogs = listOf(
                AuditLogEntity(
                    id = "audit_01",
                    timestampMillis = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000),
                    actorName = "Dr. Elena Morales",
                    actorRole = UserRole.TEACHER.name, // TODO v2: UserRole.COUNSELING.name
                    courseName = "Registro de Bienestar Universitario",
                    actionType = AuditActionType.PROFILE_VERIFIED.name,
                    details = "Validación de apoyos académicos para matrícula 20248812 con evaluación acreditada."
                ),
                AuditLogEntity(
                    id = "audit_02",
                    timestampMillis = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
                    actorName = "Sofía Hernández (Student)",
                    actorRole = UserRole.STUDENT.name,
                    courseName = "CS-304 Software Architecture",
                    actionType = AuditActionType.GRANT_PERMISSIONS.name,
                    details = "Granted time-limited access (Accommodations + Comm + Emergency Protocol). Diagnosis withheld."
                ),
                AuditLogEntity(
                    id = "audit_03",
                    timestampMillis = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000),
                    actorName = "Sofía Hernández (Student)",
                    actorRole = UserRole.STUDENT.name,
                    courseName = "CS-301 Algorithms",
                    actionType = AuditActionType.GRANT_PERMISSIONS.name,
                    details = "Granted time-limited access (Accommodations + Comm only). Diagnosis withheld."
                ),
                AuditLogEntity(
                    id = "audit_04",
                    timestampMillis = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000),
                    actorName = "Prof. Dr. Roberto Ruiz",
                    actorRole = UserRole.TEACHER.name,
                    courseName = "CS-301 Algorithms",
                    actionType = AuditActionType.VIEW_CLASSROOM_PROFILE.name,
                    details = "Teacher accessed classroom accommodation sheet. No clinical data revealed."
                ),
                AuditLogEntity(
                    id = "audit_05",
                    timestampMillis = System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000),
                    actorName = "Sofía Hernández (Student)",
                    actorRole = UserRole.STUDENT.name,
                    courseName = "Academic Support Services",
                    actionType = AuditActionType.PDF_EXPORT_DOCUMENT.name,
                    details = "Generated watermarked PDF accommodation sheet for authorized course registration."
                )
            )
            dao.insertAuditLogs(initialLogs)
        }
    }
}
