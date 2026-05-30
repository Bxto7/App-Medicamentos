package com.medicamentos.app.medremind

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.medicamentos.app.medremind.domain.model.Rol
import com.medicamentos.app.medremind.notification.NotificationHelper
import com.medicamentos.app.medremind.notification.NotificationScheduler
import com.medicamentos.app.medremind.presentation.admin.AdminMainScreen
import com.medicamentos.app.medremind.presentation.auth.AuthViewModel
import com.medicamentos.app.medremind.presentation.auth.LoginScreen
import com.medicamentos.app.medremind.presentation.auth.RegistroScreen
import com.medicamentos.app.medremind.presentation.navigation.Screen
import com.medicamentos.app.medremind.presentation.patient.PatientMainScreen
import com.medicamentos.app.medremind.presentation.theme.MedRemindTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModel()
    private val notificationHelper: NotificationHelper by inject()
    private val notificationScheduler: NotificationScheduler by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        notificationHelper.createChannel()
        CoroutineScope(Dispatchers.IO).launch {
            notificationScheduler.scheduleAllPendientes()
        }

        setContent {
            MedRemindTheme {
                val session by authViewModel.sessionState.collectAsStateWithLifecycle()

                if (!session.isReady) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    return@MedRemindTheme
                }

                val startDestination = when {
                    !session.isLoggedIn -> Screen.Login.route
                    session.rol == Rol.MEDICO -> Screen.AdminMain.route
                    else -> Screen.PatientMain.route
                }

                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = startDestination) {

                    composable(Screen.Login.route) {
                        LoginScreen(
                            viewModel = authViewModel,
                            onCrearCuenta = { navController.navigate(Screen.Registro.route) }
                        )
                        val s by authViewModel.sessionState.collectAsStateWithLifecycle()
                        LaunchedEffect(s.isLoggedIn, s.rol) {
                            if (s.isLoggedIn && s.rol != null) {
                                val dest = if (s.rol == Rol.MEDICO) Screen.AdminMain.route else Screen.PatientMain.route
                                navController.navigate(dest) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                    }

                    composable(Screen.Registro.route) {
                        RegistroScreen(
                            viewModel = authViewModel,
                            onBack = { navController.popBackStack() },
                            onRegistroExitoso = {
                                navController.navigate(Screen.PatientMain.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.PatientMain.route) {
                        PatientMainScreen(
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.PatientMain.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.AdminMain.route) {
                        AdminMainScreen(onLogout = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.AdminMain.route) { inclusive = true }
                            }
                        })
                    }
                }
            }
        }
    }
}
