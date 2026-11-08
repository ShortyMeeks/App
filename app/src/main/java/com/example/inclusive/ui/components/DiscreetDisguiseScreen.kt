package com.example.inclusive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DiscreetDisguiseScreen(
    onExitDiscreetMode: () -> Unit
) {
    var noteTitle by remember { mutableStateOf("Apuntes de Clase") }
    var noteContent by remember { mutableStateOf("Semestre 2026-2 • Notas de estudio\n\nRepaso de conceptos clave:\nLectura de capítulos 3 y 4. Preparación para la sesión de dudas.\n\nEjercicios prácticos:\nResolver la guía de problemas asignada en la plataforma.\n\nLecturas recomendadas:\nRevisar el material bibliográfico en la carpeta compartida.") }

    var editingField by remember { mutableStateOf<String?>(null) }

    // Fondo ligeramente grisáceo
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF3F5F9)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // --- BARRA SUPERIOR (Solo botón de regreso y título) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón regresar (Círculo blanco)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .clickable { onExitDiscreetMode() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Regresar",
                        tint = Color(0xFF1F2937),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Título central
                Text(
                    text = "Apuntes",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF1F2937),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Espacio vacío del mismo tamaño que el botón para que el título quede centrado
                Spacer(modifier = Modifier.width(44.dp))
            }

            // --- CONTENIDO (Caja Blanca Grande) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                // Título de la nota
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editingField = "title" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = noteTitle,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1F2937),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar título",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Caja de texto principal
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F5F9), RoundedCornerShape(16.dp))
                        .clickable { editingField = "content" }
                        .padding(16.dp)
                ) {
                    Text(
                        text = noteContent,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF4B5563),
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }

    // Diálogo para editar el texto
    if (editingField != null) {
        var tempText by remember {
            mutableStateOf(
                if (editingField == "title") noteTitle else noteContent
            )
        }

        AlertDialog(
            onDismissRequest = { editingField = null },
            title = { Text(if (editingField == "title") "Editar Título" else "Editar Nota") },
            text = {
                OutlinedTextField(
                    value = tempText,
                    onValueChange = { tempText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = if (editingField == "title") 1 else 8,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editingField == "title") noteTitle = tempText else noteContent = tempText
                    editingField = null
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { editingField = null }) { Text("Cancelar") }
            }
        )
    }
}