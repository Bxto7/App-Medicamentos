package com.medicamentos.app.medremind.presentation.admin

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTreatmentScreen(
    pacienteId: String,
    pacienteNombre: String,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddTreatmentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    LaunchedEffect(state.success) {
        if (state.success) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar tratamiento") },
                subtitle = { Text(pacienteNombre, style = MaterialTheme.typography.bodySmall) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Medication selector
            var medExpanded by remember { mutableStateOf(false) }
            val selectedMedName = state.medicamentos.getOrNull(state.selectedMedIndex)?.nombre ?: "Seleccionar medicamento"
            Text("Medicamento *", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(expanded = medExpanded, onExpandedChange = { medExpanded = it }) {
                OutlinedTextField(
                    value = selectedMedName,
                    onValueChange = {},
                    readOnly = true,
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

            // Dosis
            OutlinedTextField(
                value = state.dosis,
                onValueChange = viewModel::onDosisChange,
                label = { Text("Dosis") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Frecuencia
            Text("Frecuencia", style = MaterialTheme.typography.labelLarge)
            val frecOptions = listOf(6 to "Cada 6h", 8 to "Cada 8h", 12 to "Cada 12h", 24 to "Cada 24h")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                frecOptions.forEach { (horas, label) ->
                    FilterChip(
                        selected = state.frecuenciaHoras == horas,
                        onClick = { viewModel.setFrecuencia(horas) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // Horarios
            Text("Horarios (HH:mm)", style = MaterialTheme.typography.labelLarge)
            state.horarios.forEachIndexed { idx, hora ->
                OutlinedTextField(
                    value = hora,
                    onValueChange = { viewModel.updateHorario(idx, it) },
                    label = { Text("Horario ${idx + 1}") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Fecha inicio
            var showDateInicio by remember { mutableStateOf(false) }
            val dateInicioState = rememberDatePickerState(initialSelectedDateMillis = state.fechaInicio)
            Text("Fecha de inicio", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = dateFmt.format(Date(state.fechaInicio)),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                modifier = Modifier.fillMaxWidth().then(
                    Modifier.then(androidx.compose.ui.Modifier)
                )
            )
            TextButton(onClick = { showDateInicio = true }) { Text("Cambiar fecha de inicio") }
            if (showDateInicio) {
                DatePickerDialog(
                    onDismissRequest = { showDateInicio = false },
                    confirmButton = {
                        TextButton(onClick = {
                            dateInicioState.selectedDateMillis?.let { viewModel.setFechaInicio(it) }
                            showDateInicio = false
                        }) { Text("Aceptar") }
                    },
                    dismissButton = { TextButton(onClick = { showDateInicio = false }) { Text("Cancelar") } }
                ) { DatePicker(state = dateInicioState) }
            }

            // Fecha fin (optional)
            var showDateFin by remember { mutableStateOf(false) }
            val dateFinState = rememberDatePickerState()
            TextButton(onClick = { showDateFin = true }) {
                Text(if (state.fechaFin != null) "Fin: ${dateFmt.format(Date(state.fechaFin!!))}" else "Agregar fecha de fin (opcional)")
            }
            if (state.fechaFin != null) {
                TextButton(onClick = { viewModel.setFechaFin(null) }) { Text("Quitar fecha de fin", color = MaterialTheme.colorScheme.error) }
            }
            if (showDateFin) {
                DatePickerDialog(
                    onDismissRequest = { showDateFin = false },
                    confirmButton = {
                        TextButton(onClick = {
                            dateFinState.selectedDateMillis?.let { viewModel.setFechaFin(it) }
                            showDateFin = false
                        }) { Text("Aceptar") }
                    },
                    dismissButton = { TextButton(onClick = { showDateFin = false }) { Text("Cancelar") } }
                ) { DatePicker(state = dateFinState) }
            }

            // Stock
            OutlinedTextField(
                value = state.stockInicial,
                onValueChange = viewModel::onStockChange,
                label = { Text("Stock inicial (unidades)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Instrucciones
            OutlinedTextField(
                value = state.instrucciones,
                onValueChange = viewModel::onInstruccionesChange,
                label = { Text("Instrucciones (opcional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = { viewModel.save(pacienteId) },
                enabled = !state.isLoading && state.selectedMedIndex >= 0,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Guardar tratamiento")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
