package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

                if (isLocked) {
                    PinLockScreen(viewModel = viewModel, onUnlocked = { viewModel.toggleLock() })
                    return@MyApplicationTheme
                }

                if (isDiscreetMode) {
                    DiscreetDisguiseScreen(onExitDiscreetMode = { viewModel.toggleDiscreetMode() })
                    return@MyApplicationTheme
                }

                if (!isAuthenticated) {
                    AuthScreen(
                        viewModel = viewModel,
                        onAuthSuccess = { role -> viewModel.setAuthenticated(true, role) },
                        onShowPrivacyNotice = { showPrivacyNoticeDialog = true }
                    )
                    if (showPrivacyNoticeDialog) {
                        PrivacyNoticeDialog(onDismiss = { showPrivacyNoticeDialog = false })
                    }
                    return@MyApplicationTheme
                }

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
                            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars), // <-- Ajuste para la cámara
                            title = {
                                Column {
                                    Text(
                                        text = "Perfil de Apoyos",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = currentRole.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            actions = {
                                // Botones con espacio reducido y tamaños controlados
                                Row(
                                    modifier = Modifier.padding(end = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    IconButton(onClick = { viewModel.toggleDiscreetMode() }) {
                                        Icon(
                                            imageVector = Icons.Default.VisibilityOff,
                                            contentDescription = "Modo Discreto",
                                            tint = Color(0xFF0F766E),
                                            modifier = Modifier.size(22.dp) // Tamaño controlado
                                        )
                                    }
                                    IconButton(onClick = { viewModel.toggleLock() }) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Bloquear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    IconButton(onClick = { showPrivacyNoticeDialog = true }) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "Privacidad",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.signOut() }) {
                                        Icon(
                                            imageVector = Icons.Default.Logout,
                                            contentDescription = "Cerrar Sesión",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                ) { paddingValues ->
                    // Aquí aplicamos el padding para que la lista no se meta debajo de la cámara ni de los botones
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
                                    onShowPrivacyNotice = { showPrivacyNoticeDialog = true }
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
                        }
                    }
                }

                if (showPrivacyNoticeDialog) {
                    PrivacyNoticeDialog(onDismiss = { showPrivacyNoticeDialog = false })
                }
            }
        }
    }
}