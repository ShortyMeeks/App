package com.example.inclusive.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InclusiveDao {

    @Query("SELECT * FROM student_profiles WHERE id = :id LIMIT 1")
    fun getStudentProfileFlow(id: String): Flow<StudentProfileEntity?>

    @Query("SELECT * FROM student_profiles WHERE id = :id LIMIT 1")
    suspend fun getStudentProfile(id: String): StudentProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: StudentProfileEntity)

    // Courses
    @Query("SELECT * FROM courses ORDER BY code ASC")
    fun getAllCoursesFlow(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses ORDER BY code ASC")
    suspend fun getAllCourses(): List<CourseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    // Consent Grants
    @Query("SELECT * FROM consent_grants WHERE studentId = :studentId ORDER BY courseName ASC")
    fun getConsentsForStudentFlow(studentId: String): Flow<List<ConsentGrantEntity>>

    @Query("SELECT * FROM consent_grants WHERE teacherId = :teacherId ORDER BY courseName ASC")
    fun getConsentsForTeacherFlow(teacherId: String): Flow<List<ConsentGrantEntity>>

    @Query("SELECT * FROM consent_grants ORDER BY courseName ASC")
    fun getAllConsentsFlow(): Flow<List<ConsentGrantEntity>>

    @Query("SELECT * FROM consent_grants WHERE studentId = :studentId AND courseId = :courseId LIMIT 1")
    suspend fun getConsent(studentId: String, courseId: String): ConsentGrantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConsent(grant: ConsentGrantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsents(grants: List<ConsentGrantEntity>)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestampMillis DESC")
    fun getAllAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(logs: List<AuditLogEntity>)
}
