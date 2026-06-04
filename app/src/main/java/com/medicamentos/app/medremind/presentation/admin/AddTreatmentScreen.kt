package com.medicamentos.app.medremind.presentation.admin

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTreatmentScreen(
    pacienteId: String = "",
    pacienteNombre: String = "",
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddTreatmentViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    LaunchedEffect(pacienteId) {
        if (pacienteId.isNotBlank()) viewModel.preseleccionarPaciente(pacienteId, pacienteNombre)
    }
    LaunchedEffect(state.success) {
        if (state.success) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Agregar tratamiento", fontWeight = FontWeight.Bold)
                        if (state.selectedPacienteNombre.isNotBlank())
                            Text(state.selectedPacienteNombre, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── 1. Paciente y medicamento ──────────────────────────
            SectionCard(Icons.Default.Person, "Paciente y medicamento") {
                if (pacienteId.isBlank()) {
                    var pacExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = pacExpanded, onExpandedChange = { pacExpanded = it }) {
                        OutlinedTextField(
                            value = state.selectedPacienteNombre.ifBlank { "Seleccionar paciente" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Paciente *") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pacExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = pacExpanded, onDismissRequest = { pacExpanded = false }) {
                            state.pacientes.forEach { paciente ->
                                DropdownMenuItem(
                                    text = { Column { Text(paciente.nombre); Text(paciente.diagnostico, style = MaterialTheme.typography.labelSmall) } },
                                    onClick = { viewModel.selectPaciente(paciente); pacExpanded = false }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                var medExpanded by remember { mutableStateOf(false) }
                val selectedMedName = state.medicamentos.getOrNull(state.selectedMedIndex)?.nombre ?: "Seleccionar medicamento"
                ExposedDropdownMenuBox(expanded = medExpanded, onExpandedChange = { medExpanded = it }) {
                    OutlinedTextField(
                        value = selectedMedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Medicamento *") },
                        leadingIcon = { Icon(Icons.Default.Medication, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = medExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = medExpanded, onDismissRequest = { medExpanded = false }) {
                        state.medicamentos.forEachIndexed { idx, med ->
                            DropdownMenuItem(
                                text = { Column { Text(med.nombre); Text("${med.dosis} · ${med.via}", style = MaterialTheme.typography.labelSmall) } },
                                onClick = { viewModel.selectMedicamento(idx); medExpanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.dosis,
                    onValueChange = viewModel::onDosisChange,
                    label = { Text("Dosis") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── 2. Frecuencia y horarios ───────────────────────────
            SectionCard(Icons.Default.Schedule, "Frecuencia y horarios") {
                Text("¿Cada cuánto se toma?", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                val frecuencias = listOf(6 to "Cada 6h", 8 to "Cada 8h", 12 to "Cada 12h", 24 to "Cada 24h")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    frecuencias.chunked(2).forEach { fila ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            fila.forEach { (h, label) ->
                                FilterChip(
                                    selected = state.frecuenciaHoras == h,
                                    onClick = { viewModel.setFrecuencia(h) },
                                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                val primeraHora = state.horarios.firstOrNull() ?: "08:00"
                val horaValida = primeraHora.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d$"))
                OutlinedTextField(
                    value = primeraHora,
                    onValueChange = viewModel::setPrimeraHora,
                    label = { Text("Hora de la primera toma") },
                    placeholder = { Text("08:00") },
                    leadingIcon = { Icon(Icons.Default.Schedule, null) },
                    singleLine = true,
                    isError = primeraHora.isNotEmpty() && !horaValida,
                    supportingText = {
                        if (primeraHora.isNotEmpty() && !horaValida) Text("Formato HH:mm (ej: 08:00)")
                        else Text("Las siguientes tomas se calculan automáticamente")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Horarios calculados (solo lectura)
                Spacer(Modifier.height(10.dp))
                Text("Tomas del día (${state.horarios.size})", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.horarios.chunked(3).forEach { fila ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            fila.forEach { hora ->
                                Box(
                                    Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.size(4.dp))
                                        Text(hora, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 3. Duración ────────────────────────────────────────
            SectionCard(Icons.Default.CalendarMonth, "Duración del tratamiento") {
                var showDateInicio by remember { mutableStateOf(false) }
                val dateInicioState = rememberDatePickerState(initialSelectedDateMillis = state.fechaInicio)
                FechaRow("Inicio", dateFmt.format(Date(state.fechaInicio))) { showDateInicio = true }
                if (showDateInicio) {
                    DatePickerDialog(
                        onDismissRequest = { showDateInicio = false },
                        confirmButton = { TextButton(onClick = { dateInicioState.selectedDateMillis?.let { viewModel.setFechaInicio(it) }; showDateInicio = false }) { Text("Aceptar") } },
                        dismissButton = { TextButton(onClick = { showDateInicio = false }) { Text("Cancelar") } }
                    ) { DatePicker(state = dateInicioState) }
                }

                Spacer(Modifier.height(8.dp))
                var showDateFin by remember { mutableStateOf(false) }
                val dateFinState = rememberDatePickerState()
                FechaRow("Fin (opcional)", state.fechaFin?.let { dateFmt.format(Date(it)) } ?: "Sin fecha de fin") { showDateFin = true }
                if (state.fechaFin != null) {
                    TextButton(onClick = { viewModel.setFechaFin(null) }) { Text("Quitar fecha de fin", color = MaterialTheme.colorScheme.error) }
                }
                if (showDateFin) {
                    DatePickerDialog(
                        onDismissRequest = { showDateFin = false },
                        confirmButton = { TextButton(onClick = { dateFinState.selectedDateMillis?.let { viewModel.setFechaFin(it) }; showDateFin = false }) { Text("Aceptar") } },
                        dismissButton = { TextButton(onClick = { showDateFin = false }) { Text("Cancelar") } }
                    ) { DatePicker(state = dateFinState) }
                }
            }

            // ── 4. Stock e instrucciones ───────────────────────────
            SectionCard(Icons.Default.Inventory2, "Inventario e indicaciones") {
                val stockNum = state.stockInicial.toIntOrNull()
                OutlinedTextField(
                    value = state.stockInicial,
                    onValueChange = { input -> if (input.isEmpty() || input.toIntOrNull() != null) viewModel.onStockChange(input) },
                    label = { Text("Stock inicial (unidades)") },
                    leadingIcon = { Icon(Icons.Default.Inventory2, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = stockNum != null && stockNum < 0,
                    supportingText = { if (stockNum != null && stockNum < 0) Text("El stock no puede ser negativo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.instrucciones,
                    onValueChange = viewModel::onInstruccionesChange,
                    label = { Text("Instrucciones (opcional)") },
                    leadingIcon = { Icon(Icons.Default.Notes, null) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.error != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                }
            }

            Button(
                onClick = { viewModel.save() },
                enabled = !state.isLoading && state.selectedMedIndex >= 0,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Guardar tratamiento", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionCard(icon: ImageVector, title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.size(10.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun FechaRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)).clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
    }
}
