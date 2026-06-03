package com.medicamentos.app.medremind.presentation.admin

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medicamentos.app.medremind.domain.model.EstadoToma
import com.medicamentos.app.medremind.domain.model.Paciente
import com.medicamentos.app.medremind.domain.model.Tratamiento
import com.medicamentos.app.medremind.presentation.theme.MedicalGreen
import com.medicamentos.app.medremind.presentation.theme.OmitidoRed
import com.medicamentos.app.medremind.presentation.theme.TomadoGreen
import com.medicamentos.app.medremind.presentation.theme.WarningAmber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: AdminViewModel, onSelectPaciente: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Bienvenido, ${state.medicoNombre.ifBlank { "Médico" }}", style = MaterialTheme.typography.headlineMedium)
            Text("Panel de control", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), Icons.Default.Groups, "${state.pacientes.size}", "Pacientes activos", MaterialTheme.colorScheme.primaryContainer)
                StatCard(Modifier.weight(1f), Icons.Default.Medication, "${state.medicamentos.size}", "Medicamentos", MedicalGreen.copy(alpha = 0.15f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), Icons.Default.Check, "75%", "Adherencia hoy", TomadoGreen.copy(alpha = 0.15f))
                StatCard(Modifier.weight(1f), Icons.Default.Warning, "${state.pacientes.size}", "Alertas activas", WarningAmber.copy(alpha = 0.15f))
            }
        }
        item {
            Text("Pacientes recientes", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
        }
        items(state.pacientes.take(3)) { paciente ->
            PacienteRowSimple(paciente = paciente, onClick = {
                viewModel.selectPaciente(paciente)
                onSelectPaciente()
            })
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, icon: ImageVector, valor: String, label: String, bgColor: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PatientsScreen(
    viewModel: AdminViewModel,
    onSelectPaciente: (Paciente) -> Unit,
    onAsociarPacientes: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onSearchChange,
                placeholder = { Text("Buscar paciente...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            )
            if (state.pacientes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Aún no tienes pacientes asociados.\nPulsa \"Asociar pacientes\" para agregarlos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.pacientes) { paciente ->
                        PacienteCard(paciente = paciente, onClick = {
                            viewModel.selectPaciente(paciente)
                            onSelectPaciente(paciente)
                        })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onAsociarPacientes,
            icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
            text = { Text("Asociar pacientes") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}

@Composable
fun PacienteCard(paciente: Paciente, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Text((paciente.nombre.firstOrNull() ?: '?').uppercase(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(paciente.nombre, style = MaterialTheme.typography.titleMedium)
                Text(paciente.diagnostico, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${paciente.edad} años", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PacienteRowSimple(paciente: Paciente, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(paciente.nombre, style = MaterialTheme.typography.bodyMedium)
            Text(paciente.diagnostico, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(viewModel: AdminViewModel, onAddTreatment: () -> Unit, onBack: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val paciente = state.selectedPaciente
    if (paciente == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Person, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Text("Selecciona un paciente de la lista", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onBack) { Text("Ir a Pacientes") }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(paciente.nombre) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTreatment,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Agregar tratamiento") }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Text((paciente.nombre.firstOrNull() ?: '?').uppercase(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(paciente.nombre, style = MaterialTheme.typography.titleLarge)
                            Text("${paciente.edad} años", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(paciente.diagnostico, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            item {
                Text("Tratamientos (${state.tratamientosDelPaciente.size})", style = MaterialTheme.typography.titleMedium)
            }
            if (state.tratamientosDelPaciente.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Sin tratamientos asignados. Pulsa + para agregar.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(state.tratamientosDelPaciente) { trat -> TratamientoCard(trat) }
            }
            item {
                Divider()
                Spacer(Modifier.height(4.dp))
                Text("Historial reciente (${state.historialDelPaciente.size} registros)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
            }
            val fmt = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
            items(state.historialDelPaciente.take(15)) { registro ->
                val color = when (registro.estado) {
                    EstadoToma.TOMADO -> TomadoGreen
                    EstadoToma.OMITIDO -> OmitidoRed
                    else -> WarningAmber
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(12.dp))
                    Text(registro.medicamentoNombre, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(fmt.format(Date(registro.fechaHoraReal)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TratamientoCard(trat: Tratamiento) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(trat.medicamentoNombre, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(trat.dosis, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(4.dp))
            Text("Cada ${trat.frecuenciaHoras}h · ${trat.horarios.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (trat.instrucciones.isNotBlank()) {
                Text(trat.instrucciones, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (trat.stockRestante < 5) OmitidoRed else TomadoGreen)
                Spacer(Modifier.width(4.dp))
                Text("Stock: ${trat.stockRestante} unidades", style = MaterialTheme.typography.labelSmall, color = if (trat.stockRestante < 5) OmitidoRed else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun MedicationCatalogScreen(viewModel: AdminViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AddMedicamentoDialog(
            onDismiss = { showDialog = false },
            onConfirm = { nombre, dosis, via, instrucciones ->
                viewModel.addMedicamento(nombre, dosis, via, instrucciones) { showDialog = false }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar medicamento")
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Catálogo de medicamentos", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
            }
            if (state.medicamentos.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay medicamentos. Pulsa + para agregar.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(state.medicamentos) { med ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(med.nombre, style = MaterialTheme.typography.titleMedium)
                                Text("${med.dosis} · ${med.via}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (med.instrucciones.isNotBlank()) Text(med.instrucciones, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun AddMedicamentoDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var via by remember { mutableStateOf("") }
    var instrucciones by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo medicamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { if (it.length <= 100) nombre = it }, label = { Text("Nombre *") }, singleLine = true, isError = nombre.isBlank(), supportingText = { if (nombre.isBlank()) Text("Requerido") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dosis, onValueChange = { if (it.length <= 50) dosis = it }, label = { Text("Dosis * (ej: 500mg)") }, singleLine = true, isError = dosis.isBlank(), supportingText = { if (dosis.isBlank()) Text("Requerido") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = via, onValueChange = { if (it.length <= 50) via = it }, label = { Text("Vía * (ej: oral, IV)") }, singleLine = true, isError = via.isBlank(), supportingText = { if (via.isBlank()) Text("Requerido") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = instrucciones, onValueChange = { if (it.length <= 300) instrucciones = it }, label = { Text("Instrucciones (opcional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nombre.isNotBlank() && dosis.isNotBlank() && via.isNotBlank()) onConfirm(nombre, dosis, via, instrucciones) },
                enabled = nombre.isNotBlank() && dosis.isNotBlank() && via.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun ReportsScreen(viewModel: AdminViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Alertas y reportes", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    onClick = { exportarPdf(context, state.pacientes, state.medicoNombre) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Exportar PDF")
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f))) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber)
                        Spacer(Modifier.width(12.dp))
                        Text("Dosis omitidas hoy", style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(Modifier.height(8.dp))
                    if (state.pacientes.isEmpty()) {
                        Text("Sin alertas pendientes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        state.pacientes.take(3).forEach { p ->
                            Text("• ${p.nombre} — ${p.diagnostico}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Adherencia global (últimos 7 días)", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("75%", style = MaterialTheme.typography.headlineMedium, color = TomadoGreen, fontWeight = FontWeight.Bold)
                    Text("Promedio de todos los pacientes activos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        items(state.pacientes) { p ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Text((p.nombre.firstOrNull() ?: '?').uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.nombre, style = MaterialTheme.typography.titleSmall)
                        Text(p.diagnostico, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("80%", style = MaterialTheme.typography.titleMedium, color = TomadoGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminProfileScreen(viewModel: AdminViewModel, onLogout: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditNombreDialog(
            nombreActual = state.medicoNombre,
            onDismiss = { showEditDialog = false },
            onConfirm = { nuevoNombre ->
                viewModel.updateNombre(nuevoNombre) { showEditDialog = false }
            }
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(state.medicoNombre, style = MaterialTheme.typography.titleMedium)
                    Text("Médico responsable", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar nombre", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Divider()
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
fun EditNombreDialog(nombreActual: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var nombre by remember { mutableStateOf(nombreActual) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar nombre") },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { if (it.length <= 100) nombre = it },
                label = { Text("Nombre completo") },
                singleLine = true,
                isError = nombre.trim().isEmpty(),
                supportingText = { if (nombre.trim().isEmpty()) Text("El nombre no puede estar vacío") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (nombre.trim().isNotBlank()) onConfirm(nombre.trim()) }, enabled = nombre.trim().isNotBlank()) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun exportarPdf(context: android.content.Context, pacientes: List<Paciente>, medico: String) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
    val bodyPaint = Paint().apply { textSize = 12f }
    val subtitlePaint = Paint().apply { textSize = 14f; isFakeBoldText = true }
    val dateFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    var y = 60f
    canvas.drawText("Reporte de Adherencia — Adherencia360", 40f, y, titlePaint); y += 30f
    canvas.drawText("Médico: $medico", 40f, y, bodyPaint); y += 20f
    canvas.drawText("Generado: ${dateFmt.format(Date())}", 40f, y, bodyPaint); y += 30f
    canvas.drawLine(40f, y, 555f, y, bodyPaint); y += 25f
    canvas.drawText("Pacientes activos: ${pacientes.size}", 40f, y, subtitlePaint); y += 25f
    pacientes.forEach { p ->
        canvas.drawText("• ${p.nombre} (${p.edad} años) — ${p.diagnostico}", 50f, y, bodyPaint); y += 20f
        if (y > 800f) return@forEach
    }
    pdfDocument.finishPage(page)
    try {
        val dir = File(context.getExternalFilesDir(null), "reports").also { it.mkdirs() }
        val file = File(dir, "reporte_adherencia_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { pdfDocument.writeTo(it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartir reporte"))
        android.widget.Toast.makeText(context, "PDF generado correctamente", android.widget.Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Error al generar PDF: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    } finally {
        pdfDocument.close()
    }
}
