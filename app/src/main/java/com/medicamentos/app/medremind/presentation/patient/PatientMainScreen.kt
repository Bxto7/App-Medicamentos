package com.medicamentos.app.medremind.presentation.patient

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medicamentos.app.medremind.presentation.premium.GlucosaScreen
import com.medicamentos.app.medremind.presentation.premium.GlucosaViewModel
import com.medicamentos.app.medremind.presentation.premium.PremiumScreen
import org.koin.androidx.compose.koinViewModel

private val PremiumGold = Color(0xFFFFB300)

@Composable
fun PatientMainScreen(
    onLogout: () -> Unit,
    isPremium: Boolean,
    onActivarPremium: (String) -> Boolean
) {
    val viewModel: PatientHomeViewModel = koinViewModel()
    val glucosaViewModel: GlucosaViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Handle result if needed */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Tabs base siempre disponibles
    data class Tab(val label: String, val icon: ImageVector, val premium: Boolean = false)

    val baseTabs = listOf(
        Tab("Inicio", Icons.Default.Home),
        Tab("Calendario", Icons.Default.CalendarMonth),
        Tab("Historial", Icons.Default.History),
        Tab("Alertas", Icons.Default.Notifications),
        Tab("Perfil", Icons.Default.Person)
    )
    val premiumTab = Tab("Glucosa", Icons.Default.MonitorHeart, premium = true)
    val tabs = if (isPremium) baseTabs + premiumTab else baseTabs

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            if (tab.premium) {
                                BadgedBox(badge = {
                                    Badge(containerColor = PremiumGold) {
                                        Icon(Icons.Default.Star, null, modifier = Modifier.size(8.dp), tint = Color.Black)
                                    }
                                }) {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.label)
                            }
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                0 -> PatientHomeScreen(viewModel = viewModel)
                1 -> CalendarScreen(viewModel = viewModel)
                2 -> HistoryScreen(viewModel = viewModel)
                3 -> PatientAlertsScreen()
                4 -> PatientProfileScreen(
                    nombrePaciente = state.nombrePaciente,
                    avatarId = state.avatarId,
                    isPremium = isPremium,
                    onLogout = onLogout,
                    onPremiumClick = { selectedTab = if (isPremium) 5 else tabs.size - 1 }
                )
                5 -> if (isPremium) GlucosaScreen(viewModel = glucosaViewModel)
                    else PremiumScreen(
                        isPremium = false,
                        onActivar = onActivarPremium,
                        onActivadoExitoso = {}
                    )
            }
        }
    }
}
