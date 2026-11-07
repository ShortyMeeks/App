package com.example.inclusive.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.inclusive.model.Accommodation
import com.example.inclusive.model.AccommodationCategory
import com.example.inclusive.model.AuditActionType
import com.example.inclusive.model.ClinicalDetails
import com.example.inclusive.model.CommunicationPreferences
import com.example.inclusive.model.EmergencyCrisisProtocol
import com.example.inclusive.model.UserRole
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "student_profiles")
data class StudentProfileEntity(
    @PrimaryKey val id: String,
    val studentNumber: String,
    val fullName: String,
    val program: String,
    val semester: String,
    val contactEmail: String,
    val contactPhone: String,
    val clinicalConditionName: String?,
    val isVerifiedByCounseling: Boolean,
    val verifiedBy: String?,
    val verificationTimestamp: Long,
    val accommodationsJson: String,
    val communicationPrefsJson: String,
    val emergencyProtocolJson: String
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val teacherId: String,
    val teacherName: String,
    val schedule: String,
    val semester: String
)

@Entity(tableName = "consent_grants")
data class ConsentGrantEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentName: String,
    val teacherId: String,
    val teacherName: String,
    val courseId: String,
    val courseName: String,
    val academicTerm: String,
    val shareAccommodations: Boolean,
    val shareCommunicationPrefs: Boolean,
    val shareEmergencyProtocol: Boolean,
    val shareClinicalDiagnosis: Boolean,
    val grantedAtMillis: Long,
    val expiresAtMillis: Long,
    val isRevoked: Boolean
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val timestampMillis: Long,
    val actorName: String,
    val actorRole: String,
    val courseName: String,
    val actionType: String,
    val details: String
)

object JsonConverters {

    fun accommodationsToJson(list: List<Accommodation>): String {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("category", item.category.name)
            obj.put("instructionForTeacher", item.instructionForTeacher)
            obj.put("isMandatory", item.isMandatory)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToAccommodations(json: String?): List<Accommodation> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<Accommodation>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val cat = try {
                    AccommodationCategory.valueOf(obj.optString("category", AccommodationCategory.CLASSROOM.name))
                } catch (e: Exception) {
                    AccommodationCategory.CLASSROOM
                }
                list.add(
                    Accommodation(
                        id = obj.optString("id", "acc_$i"),
                        title = obj.optString("title"),
                        category = cat,
                        instructionForTeacher = obj.optString("instructionForTeacher"),
                        isMandatory = obj.optBoolean("isMandatory", true)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun communicationPrefsToJson(prefs: CommunicationPreferences): String {
        val obj = JSONObject()
        obj.put("writtenInstructionsOnly", prefs.writtenInstructionsOnly)
        obj.put("discreetNotifications", prefs.discreetNotifications)
        obj.put("avoidPublicSinglingOut", prefs.avoidPublicSinglingOut)
        obj.put("advanceNoticeOfChanges", prefs.advanceNoticeOfChanges)
        return obj.toString()
    }

    fun jsonToCommunicationPrefs(json: String?): CommunicationPreferences {
        if (json.isNullOrBlank()) return CommunicationPreferences()
        return try {
            val obj = JSONObject(json)
            CommunicationPreferences(
                writtenInstructionsOnly = obj.optBoolean("writtenInstructionsOnly", true),
                discreetNotifications = obj.optBoolean("discreetNotifications", true),
                avoidPublicSinglingOut = obj.optBoolean("avoidPublicSinglingOut", true),
                advanceNoticeOfChanges = obj.optBoolean("advanceNoticeOfChanges", true)
            )
        } catch (e: Exception) {
            CommunicationPreferences()
        }
    }

    fun emergencyProtocolToJson(protocol: EmergencyCrisisProtocol): String {
        val obj = JSONObject()
        obj.put("triggers", JSONArray(protocol.triggerWarnings))
        obj.put("actionsToTake", JSONArray(protocol.actionsToTake))
        obj.put("actionsToAvoid", JSONArray(protocol.actionsToAvoid))
        obj.put("emergencyContactName", protocol.emergencyContactName)
        obj.put("emergencyContactPhone", protocol.emergencyContactPhone)
        obj.put("counselorContact", protocol.counselorContact)
        return obj.toString()
    }

    fun jsonToEmergencyProtocol(json: String?): EmergencyCrisisProtocol {
        if (json.isNullOrBlank()) return EmergencyCrisisProtocol()
        return try {
            val obj = JSONObject(json)
            val triggers = mutableListOf<String>()
            val triggersArray = obj.optJSONArray("triggers")
            if (triggersArray != null) {
                for (i in 0 until triggersArray.length()) triggers.add(triggersArray.getString(i))
            }
            val actionsTake = mutableListOf<String>()
            val takeArray = obj.optJSONArray("actionsToTake")
            if (takeArray != null) {
                for (i in 0 until takeArray.length()) actionsTake.add(takeArray.getString(i))
            }
            val actionsAvoid = mutableListOf<String>()
            val avoidArray = obj.optJSONArray("actionsToAvoid")
            if (avoidArray != null) {
                for (i in 0 until avoidArray.length()) actionsAvoid.add(avoidArray.getString(i))
            }
            EmergencyCrisisProtocol(
                triggerWarnings = triggers,
                actionsToTake = actionsTake,
                actionsToAvoid = actionsAvoid,
                emergencyContactName = obj.optString("emergencyContactName"),
                emergencyContactPhone = obj.optString("emergencyContactPhone"),
                counselorContact = obj.optString("counselorContact")
            )
        } catch (e: Exception) {
            EmergencyCrisisProtocol()
        }
    }
}
