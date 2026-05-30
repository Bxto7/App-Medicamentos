package com.medicamentos.app.medremind.presentation.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

private data class AdminTab(val label: String, val icon: ImageVector)

private val adminTabs = listOf(
    AdminTab("Dashboard", Icons.Default.Dashboard),
    AdminTab("Pacientes", Icons.Default.Groups),
    AdminTab("Medicamentos", Icons.Default.Medication),
    AdminTab("Alertas", Icons.Default.BarChart),
    AdminTab("Perfil", Icons.Default.Person)
)

@Composable
fun AdminMainScreen(onLogout: () -> Unit) {
    val viewModel: AdminViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTreatment by remember { mutableStateOf(false) }
    var showPatientDetail by remember { mutableStateOf(false) }

    // Pantalla de agregar tratamiento (overlay completo)
    if (showAddTreatment) {
        val paciente = state.selectedPaciente
        AddTreatmentScreen(
            pacienteId = paciente?.id ?: "",
            pacienteNombre = paciente?.nombre ?: "",
            onSaved = { showAddTreatment = false },
            onBack = { showAddTreatment = false }
        )
        return
    }

    // Detalle de paciente (overlay completo)
    if (showPatientDetail && state.selectedPaciente != null) {
        PatientDetailScreen(
            viewModel = viewModel,
            onAddTreatment = { showAddTreatment = true },
            onBack = { showPatientDetail = false }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                adminTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onSelectPaciente = { showPatientDetail = true }
                )
                1 -> PatientsScreen(
                    viewModel = viewModel,
                    onSelectPaciente = {
                        showPatientDetail = true
                        selectedTab = 1
                    }
                )
                2 -> MedicationCatalogScreen(viewModel = viewModel)
                3 -> ReportsScreen(viewModel = viewModel)
                4 -> AdminProfileScreen(
                    viewModel = viewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}
