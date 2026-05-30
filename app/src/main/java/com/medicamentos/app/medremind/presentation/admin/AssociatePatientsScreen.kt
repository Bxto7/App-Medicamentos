package com.medicamentos.app.medremind.presentation.admin

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast

/**
 * Requerimiento 1: el médico ve todos los usuarios con rol PACIENTE y marca
 * cuáles asocia a su cuenta. Las casillas arrancan reflejando las asociaciones
 * actuales; al guardar se concilian altas y bajas en una sola operación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociatePatientsScreen(viewModel: AdminViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // IDs marcados localmente. Se inicializa con los ya asociados y se mantiene
    // estable aunque el flujo de la BD vuelva a emitir.
    var seleccionados by remember { mutableStateOf<Set<String>?>(null) }
    LaunchedEffect(state.pacientesAsociables) {
        if (seleccionados == null && state.pacientesAsociables.isNotEmpty()) {
            seleccionados = state.pacientesAsociables.filter { it.yaAsociado }.map { it.usuarioId }.toSet()
        }
    }
    val marcados = seleccionados ?: state.pacientesAsociables.filter { it.yaAsociado }.map { it.usuarioId }.toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asociar pacientes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") } }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val previos = state.pacientesAsociables.filter { it.yaAsociado }.map { it.usuarioId }.toSet()
                    val nuevos = (marcados - previos).toList()
                    val removidos = (previos - marcados).toList()
                    removidos.forEach { viewModel.desasociarPaciente(it) }
                    if (nuevos.isNotEmpty()) {
                        viewModel.asociarPacientes(nuevos) { ok ->
                            Toast.makeText(
                                context,
                                if (ok) "Pacientes asociados" else "No se pudo asociar",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (ok) onBack()
                        }
                    } else {
                        if (removidos.isNotEmpty()) {
                            Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
                        }
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp)
            ) {
                Text("Guardar asociaciones")
            }
        }
    ) { padding ->
        if (state.pacientesAsociables.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "No hay usuarios con rol Paciente registrados todavía.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Selecciona los pacientes que quieres gestionar. Solo podrás asignar tratamientos a los pacientes asociados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
            items(state.pacientesAsociables, key = { it.usuarioId }) { paciente ->
                val checked = paciente.usuarioId in marcados
                Row(
                    Modifier.fillMaxWidth().clickable {
                        seleccionados = if (checked) marcados - paciente.usuarioId else marcados + paciente.usuarioId
                    }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            paciente.nombre.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(paciente.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                        Text(paciente.email, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { isChecked ->
                            seleccionados = if (isChecked) marcados + paciente.usuarioId else marcados - paciente.usuarioId
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
