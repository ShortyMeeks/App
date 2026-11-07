package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inclusive.model.UserRole
import com.example.inclusive.ui.components.DiscreetDisguiseScreen
import com.example.inclusive.ui.components.PinLockScreen
import com.example.inclusive.ui.components.PrivacyNoticeDialog
import com.example.inclusive.ui.screens.AuthScreen
import com.example.inclusive.ui.screens.ProfileEditorScreen
import com.example.inclusive.ui.screens.StudentHomeScreen
import com.example.inclusive.ui.screens.TeacherClassroomScreen
import com.example.inclusive.viewmodel.InclusiveViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: InclusiveViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentRole by viewModel.currentRole.collectAsState()
                val isAuthenticated by viewModel.isAuthenticated.collectAsState()
                val isDiscreetMode by viewModel.isDiscreetMode.collectAsState()
                val isLocked by viewModel.isLocked.collectAsState()
                val studentProfile by viewModel.studentProfile.collectAsState()
                val courses by viewModel.courses.collectAsState()
                val consents by viewModel.consents.collectAsState()
                val selectedCourseId by viewModel.selectedCourseId.collectAsState()
                val userMessage by viewModel.userMessage.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }
                var showPrivacyNoticeDialog by remember { mutableStateOf(false) }
                var isProfileEditorVisible by remember { mutableStateOf(false) }

                LaunchedEffect(userMessage) {
                    userMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearUserMessage()
                    }
                }

                // Bloqueo por PIN de seguridad
                if (isLocked) {
                    PinLockScreen(
                        onUnlocked = { viewModel.toggleLock() }
                    )
                    return@MyApplicationTheme
                }

                // Modo Discreto (Camuflaje de privacidad para aulas y bibliotecas)
                if (isDiscreetMode) {
                    DiscreetDisguiseScreen(
                        onExitDiscreetMode = { viewModel.toggleDiscreetMode() }
                    )
                    return@MyApplicationTheme
                }

                // Pantalla de Autenticación si no hay sesión activa
                if (!isAuthenticated) {
                    AuthScreen(
                        viewModel = viewModel,
                        onAuthSuccess = { role ->
                            viewModel.setAuthenticated(true, role)
                        },
                        onShowPrivacyNotice = {
                            showPrivacyNoticeDialog = true
                        }
                    )
                    if (showPrivacyNoticeDialog) {
                        PrivacyNoticeDialog(
                            onDismiss = { showPrivacyNoticeDialog = false }
                        )
                    }
                    return@MyApplicationTheme
                }

                // Formulario de Creación / Edición del Perfil de Apoyos
                if (isProfileEditorVisible) {
                    ProfileEditorScreen(
                        viewModel = viewModel,
                        initialProfile = studentProfile,
                        onNavigateBack = { isProfileEditorVisible = false }
                    )
                    return@MyApplicationTheme
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Perfil de Apoyos Estudiantiles",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = currentRole.label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Inclusión en Aula y Consentimiento Granular",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            actions = {
                                // Botón de Modo Discreto (Escudo de Privacidad)
                                IconButton(
                                    onClick = { viewModel.toggleDiscreetMode() },
                                    modifier = Modifier.testTag("discreet_mode_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = "Activar Modo Discreto",
                                        tint = Color(0xFF0F766E)
                                    )
                                }

                                // Botón de Bloqueo por PIN
                                IconButton(
                                    onClick = { viewModel.toggleLock() },
                                    modifier = Modifier.testTag("lock_app_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Bloquear Aplicación",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Aviso Legal de Privacidad LFPDPPP
                                IconButton(
                                    onClick = { showPrivacyNoticeDialog = true },
                                    modifier = Modifier.testTag("privacy_notice_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Aviso de Privacidad LFPDPPP",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Cerrar Sesión
                                IconButton(
                                    onClick = { viewModel.signOut() },
                                    modifier = Modifier.testTag("logout_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Cerrar Sesión",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            NavigationBarItem(
                                selected = currentRole == UserRole.STUDENT,
                                onClick = { viewModel.switchRole(UserRole.STUDENT) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = "Panel Estudiante"
                                    )
                                },
                                label = { Text("Estudiante") },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_student_role")
                            )

                            NavigationBarItem(
                                selected = currentRole == UserRole.TEACHER,
                                onClick = { viewModel.switchRole(UserRole.TEACHER) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Panel Docente"
                                    )
                                },
                                label = { Text("Docente") },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_teacher_role")
                            )

                            // TODO v2: Agregar NavigationBarItem para Orientación y Psicología (Counseling)
                            // TODO v2: Agregar NavigationBarItem para Administración y Auditoría Institucional
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        when (currentRole) {
                            UserRole.STUDENT -> {
                                StudentHomeScreen(
                                    viewModel = viewModel,
                                    profile = studentProfile,
                                    courses = courses,
                                    consents = consents,
                                    onOpenProfileEditor = { isProfileEditorVisible = true },
                                    onShowPrivacyNotice = { showPrivacyNoticeDialog = true },
                                    onLogout = { viewModel.signOut() }
                                )
                            }
                            UserRole.TEACHER -> {
                                TeacherClassroomScreen(
                                    viewModel = viewModel,
                                    profile = studentProfile,
                                    courses = courses,
                                    consents = consents,
                                    selectedCourseId = selectedCourseId
                                )
                            }
                            // TODO v2:
                            // UserRole.COUNSELING -> CounselingScreen(viewModel, studentProfile)
                            // UserRole.ADMIN -> AdminAuditScreen(viewModel, auditLogs, consents, courses, onLogout = { viewModel.signOut() })
                        }
                    }
                }

                if (showPrivacyNoticeDialog) {
                    PrivacyNoticeDialog(
                        onDismiss = { showPrivacyNoticeDialog = false }
                    )
                }
            }
        }
    }
}
