package com.medicamentos.app.medremind.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Registro : Screen("registro")
    object PatientMain : Screen("patient_main")
    object AdminMain : Screen("admin_main")
}
