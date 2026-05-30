package com.medicamentos.app.medremind.presentation.admin

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private data class AdminTab(val label: String, val icon: ImageVector)

private val adminTabs = listOf(
    AdminTab("Dashboard", Icons.Default.Dashboard),
    AdminTab("Pacientes", Icons.Default.Groups),
    AdminTab("Detalle", Icons.Default.Person),
    AdminTab("Medicamentos", Icons.Default.Medication),
    AdminTab("Alertas", Icons.Default.BarChart)
)

@Composable
fun AdminMainScreen(onLogout: () -> Unit) {
    val viewModel: AdminViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTreatment by remember { mutableStateOf(false) }

    // Auto-switch to Detalle tab when a patient is selected from the list
    LaunchedEffect(state.selectedPaciente) {
        if (state.selectedPaciente != null && selectedTab == 1) {
            selectedTab = 2
        }
    }

    if (showAddTreatment && state.selectedPaciente != null) {
        AddTreatmentScreen(
            pacienteId = state.selectedPaciente!!.id,
            pacienteNombre = state.selectedPaciente!!.nombre,
            onSaved = { showAddTreatment = false },
            onBack = { showAddTreatment = false }
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
                0 -> DashboardScreen(viewModel = viewModel)
                1 -> PatientsScreen(
                    viewModel = viewModel,
                    onSelectPaciente = { selectedTab = 2 }
                )
                2 -> PatientDetailScreen(
                    viewModel = viewModel,
                    onAddTreatment = { showAddTreatment = true }
                )
                3 -> MedicationCatalogScreen(viewModel = viewModel)
                4 -> ReportsScreen(viewModel = viewModel)
            }
        }
    }
}
