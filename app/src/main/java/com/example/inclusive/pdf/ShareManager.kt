package com.example.inclusive.pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object ShareManager {

    /**
     * Comparte el documento PDF de ajustes de manera segura mediante la hoja de compartir de Android.
     * PRINCIPIO DE SEGURIDAD CRÍTICO:
     * Nunca se adjuntan diagnósticos médicos ni datos sensibles en Intent.EXTRA_TEXT.
     * Solo se utiliza un texto neutral e institucional.
     */
    fun sharePdfViaShareSheet(context: Context, pdfFile: File, courseName: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Ficha de Apoyos en Aula ($courseName)")
                putExtra(Intent.EXTRA_TEXT, "Ficha oficial de apoyos y ajustes razonables para $courseName. Adjunta en formato PDF confidencial.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Compartir Ficha de Apoyos mediante...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al compartir: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Abre el documento PDF en el visor predeterminado del dispositivo
     */
    fun viewPdfDocument(context: Context, pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Abrir Ficha de Apoyos (PDF)"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No se encontró visor de PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
