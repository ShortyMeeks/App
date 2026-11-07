package com.example.inclusive.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.inclusive.model.Accommodation
import com.example.inclusive.model.EmergencyCrisisProtocol
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfExportManager(private val context: Context) {

    fun generateClassroomSupportSheet(
        studentName: String,
        studentNumber: String,
        courseName: String,
        teacherName: String,
        academicTerm: String,
        accommodations: List<Accommodation>,
        communicationGuidelines: List<String>,
        emergencyProtocol: EmergencyCrisisProtocol?,
        disclosedDiagnosis: String?,
        expiresAtDate: String
    ): File {
        val document = PdfDocument()
        // Página estándar A4: 595 x 842 puntos (72 dpi)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val pageWidth = 595f
        val pageHeight = 842f
        val leftMargin = 45f
        val rightMargin = 550f
        val contentWidth = rightMargin - leftMargin

        // 1. Marca de Agua Diagonal Sutil y Oficial
        val watermarkPaint = Paint().apply {
            color = Color.argb(22, 220, 38, 38)
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.save()
        canvas.rotate(-32f, pageWidth / 2, pageHeight / 2)
        canvas.drawText("CONFIDENCIAL – USO EXCLUSIVO EN AULA AUTORIZADA", pageWidth / 2, pageHeight / 2 - 100, watermarkPaint)
        canvas.drawText("PROTEGIDO POR CONSENTIMIENTO – PROHIBIDA SU DIFUSIÓN", pageWidth / 2, pageHeight / 2 + 80, watermarkPaint)
        canvas.restore()

        // 2. Barra de Encabezado Institucional
        val headerBarPaint = Paint().apply {
            color = Color.rgb(30, 58, 138) // Índigo Profundo #1E3A8A
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, pageWidth, 75f, headerBarPaint)

        val headerTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("PERFIL DE APOYOS Y AJUSTES RAZONABLES EN AULA", leftMargin, 38f, headerTitlePaint)

        val headerSubPaint = Paint().apply {
            color = Color.rgb(224, 231, 255)
            textSize = 9.5f
            isAntiAlias = true
        }
        canvas.drawText("FICHA OFICIAL DE INCLUSIÓN EDUCATIVA Y ACCESIBILIDAD UNIVERSITARIA", leftMargin, 55f, headerSubPaint)

        // 3. Cuadro de Metadatos del Documento
        var currentY = 95f
        val metaBoxPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        val metaStrokePaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val metaRect = RectF(leftMargin, currentY, rightMargin, currentY + 70f)
        canvas.drawRoundRect(metaRect, 6f, 6f, metaBoxPaint)
        canvas.drawRoundRect(metaRect, 6f, 6f, metaStrokePaint)

        val labelPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val col1 = leftMargin + 12f
        val col2 = leftMargin + 260f

        canvas.drawText("ESTUDIANTE:", col1, currentY + 20f, labelPaint)
        canvas.drawText("$studentName (Matrícula: $studentNumber)", col1 + 75f, currentY + 20f, valuePaint)

        canvas.drawText("ASIGNATURA:", col1, currentY + 40f, labelPaint)
        canvas.drawText(courseName, col1 + 75f, currentY + 40f, valuePaint)

        canvas.drawText("DOCENTE:", col1, currentY + 58f, labelPaint)
        canvas.drawText(teacherName, col1 + 75f, currentY + 58f, valuePaint)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        canvas.drawText("PERIODO ACADÉMICO:", col2, currentY + 20f, labelPaint)
        canvas.drawText(academicTerm, col2 + 115f, currentY + 20f, valuePaint)

        canvas.drawText("FECHA DE EMISIÓN:", col2, currentY + 40f, labelPaint)
        canvas.drawText(dateFormat.format(Date()), col2 + 115f, currentY + 40f, valuePaint)

        canvas.drawText("VIGENCIA HASTA:", col2, currentY + 58f, labelPaint)
        canvas.drawText(expiresAtDate, col2 + 115f, currentY + 58f, valuePaint)

        currentY += 88f

        // 4. Sección: Apoyos Académicos Autorizados
        val sectionHeaderPaint = Paint().apply {
            color = Color.rgb(30, 58, 138)
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val dividerPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 1f
        }

        canvas.drawText("1. AJUSTES RAZONABLES Y APOYOS AUTORIZADOS EN AULA", leftMargin, currentY, sectionHeaderPaint)
        canvas.drawLine(leftMargin, currentY + 4f, rightMargin, currentY + 4f, dividerPaint)
        currentY += 18f

        val bulletTitlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bulletDescPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        if (accommodations.isEmpty()) {
            canvas.drawText("• Se aplican apoyos estándar y principios de diseño universal de aprendizaje.", leftMargin + 8f, currentY, bulletDescPaint)
            currentY += 16f
        } else {
            for (acc in accommodations) {
                canvas.drawText("• ${acc.title}", leftMargin + 8f, currentY, bulletTitlePaint)
                currentY += 14f
                val instructionText = "  Instrucción: ${acc.instructionForTeacher}"
                canvas.drawText(instructionText, leftMargin + 14f, currentY, bulletDescPaint)
                currentY += 16f
            }
        }
        currentY += 6f

        // 5. Sección: Pautas de Comunicación
        canvas.drawText("2. PAUTAS DE COMUNICACIÓN E INCLUSIÓN", leftMargin, currentY, sectionHeaderPaint)
        canvas.drawLine(leftMargin, currentY + 4f, rightMargin, currentY + 4f, dividerPaint)
        currentY += 18f

        if (communicationGuidelines.isEmpty()) {
            canvas.drawText("• Comunicación respetuosa habitual de aula.", leftMargin + 8f, currentY, bulletDescPaint)
            currentY += 16f
        } else {
            for (pref in communicationGuidelines) {
                canvas.drawText("• $pref", leftMargin + 8f, currentY, bulletDescPaint)
                currentY += 15f
            }
        }
        currentY += 8f

        // 6. Sección: Protocolo de Emergencia (si fue autorizado)
        if (emergencyProtocol != null) {
            val alertSectionPaint = Paint().apply {
                color = Color.rgb(185, 28, 28)
                textSize = 11.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("3. PROTOCOLO ANTE CRISIS O EMERGENCIA (AUTORIZADO)", leftMargin, currentY, alertSectionPaint)
            canvas.drawLine(leftMargin, currentY + 4f, rightMargin, currentY + 4f, dividerPaint)
            currentY += 18f

            if (emergencyProtocol.actionsToAvoid.isNotEmpty()) {
                canvas.drawText("ACCIONES A EVITAR ESTRICTAMENTE:", leftMargin + 8f, currentY, labelPaint)
                currentY += 14f
                for (avoid in emergencyProtocol.actionsToAvoid) {
                    canvas.drawText("• $avoid", leftMargin + 14f, currentY, bulletDescPaint)
                    currentY += 14f
                }
            }

            if (emergencyProtocol.actionsToTake.isNotEmpty()) {
                currentY += 4f
                canvas.drawText("PASOS RECOMENDADOS:", leftMargin + 8f, currentY, labelPaint)
                currentY += 14f
                for (step in emergencyProtocol.actionsToTake) {
                    canvas.drawText("• $step", leftMargin + 14f, currentY, bulletDescPaint)
                    currentY += 14f
                }
            }

            if (emergencyProtocol.emergencyContactName.isNotBlank()) {
                currentY += 4f
                canvas.drawText("Contacto de Emergencia: ${emergencyProtocol.emergencyContactName} (${emergencyProtocol.emergencyContactPhone})", leftMargin + 8f, currentY, bulletTitlePaint)
                currentY += 14f
            }
            currentY += 8f
        }

        // 7. Cuadro de Estado de Divulgación de Diagnóstico (Principio de Minimización)
        val noticeBoxPaint = Paint().apply {
            color = if (disclosedDiagnosis != null) Color.rgb(254, 243, 199) else Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        val noticeRect = RectF(leftMargin, currentY, rightMargin, currentY + 44f)
        canvas.drawRoundRect(noticeRect, 4f, 4f, noticeBoxPaint)

        val noticeTitlePaint = Paint().apply {
            color = if (disclosedDiagnosis != null) Color.rgb(180, 83, 9) else Color.rgb(71, 85, 105)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val noticeBodyPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }

        if (disclosedDiagnosis != null) {
            canvas.drawText("DIVULGACIÓN CLÍNICA VOLUNTARIA POR EL ESTUDIANTE:", leftMargin + 10f, currentY + 16f, noticeTitlePaint)
            canvas.drawText("Condición: $disclosedDiagnosis (Compartido con consentimiento expreso del estudiante)", leftMargin + 10f, currentY + 32f, noticeBodyPaint)
        } else {
            canvas.drawText("PRINCIPIO DE MINIMIZACIÓN DE DATOS (LFPDPPP Y DISEÑO UNIVERSAL):", leftMargin + 10f, currentY + 16f, noticeTitlePaint)
            canvas.drawText("El diagnóstico clínico permanece reservado por decisión del estudiante. Los ajustes pedagógicos son vinculantes.", leftMargin + 10f, currentY + 32f, noticeBodyPaint)
        }
        currentY += 56f

        // 8. Pie de Página
        val footerPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawLine(leftMargin, pageHeight - 35f, rightMargin, pageHeight - 35f, dividerPaint)
        canvas.drawText(
            "Este documento contiene apoyos protegidos bajo Políticas de Inclusión Universitaria y la Ley Federal de Protección de Datos Personales (México).",
            pageWidth / 2,
            pageHeight - 22f,
            footerPaint
        )
        canvas.drawText(
            "Identificador Único: ${java.util.UUID.randomUUID()} | Emisión Digital Automatizada",
            pageWidth / 2,
            pageHeight - 12f,
            footerPaint
        )

        document.finishPage(page)

        // Guardar archivo en cacheDir/exports
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val sanitizedStudent = studentName.replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
        val pdfFile = File(exportDir, "PerfilApoyos_${sanitizedStudent}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }
}
