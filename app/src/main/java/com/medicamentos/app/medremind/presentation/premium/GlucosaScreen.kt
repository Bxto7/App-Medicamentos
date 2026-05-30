package com.medicamentos.app.medremind.presentation.premium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medicamentos.app.medremind.domain.model.GlucosaMedicion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Rangos de glucosa (mg/dL)
private val VERDE = Color(0xFF43A047)   // < 140
private val AMARILLO = Color(0xFFFFB300) // 140-180
private val ROJO = Color(0xFFE53935)    // > 180

fun glucosaColor(valor: Int) = when {
    valor < 140 -> VERDE
    valor <= 180 -> AMARILLO
    else -> ROJO
}

fun glucosaLabel(valor: Int) = when {
    valor < 140 -> "Normal"
    valor <= 180 -> "Elevada"
    else -> "Alta"
}

@Composable
fun GlucosaScreen(viewModel: GlucosaViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbarHost.showSnackbar(it); viewModel.clearSuccess() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MonitorHeart, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Glucosa en sangre", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Últimos 14 días", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Último valor y semáforo
        item {
            val ultima = state.registros.firstOrNull()
            if (ultima != null) {
                val color = glucosaColor(ultima.valor)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(64.dp).clip(CircleShape).background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${ultima.valor}", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Último registro", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${ultima.valor} mg/dL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
                            Text(glucosaLabel(ultima.valor), style = MaterialTheme.typography.bodySmall, color = color)
                            Text(ultima.momento, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Gráfica
        if (state.registros.size >= 2) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Tendencia", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        GlucosaChart(registros = state.registros.reversed())
                    }
                }
            }
        }

        // Leyenda de semáforo
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SemaforoItem(VERDE, "Normal (<140)")
                SemaforoItem(AMARILLO, "Elevada (140-180)")
                SemaforoItem(ROJO, "Alta (>180)")
            }
        }

        // Formulario de registro
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Registrar glucosa", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = state.valorInput,
                        onValueChange = viewModel::onValorChange,
                        label = { Text("Glucosa (mg/dL)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = state.error != null,
                        supportingText = state.error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Text("mg/dL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
                        }
                    )
                    // Selector de momento
                    Text("Momento:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(MOMENTOS_GLUCOSA) { momento ->
                            FilterChip(
                                selected = state.momentoSeleccionado == momento,
                                onClick = { viewModel.onMomentoChange(momento) },
                                label = { Text(momento.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = state.notasInput,
                        onValueChange = viewModel::onNotasChange,
                        label = { Text("Notas (opcional)") },
                        singleLine = false,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = viewModel::registrar,
                        enabled = !state.isLoading && state.valorInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Registrar medición")
                        }
                    }
                }
            }
        }

        // Historial
        if (state.registros.isNotEmpty()) {
            item {
                Text("Historial", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            itemsIndexed(state.registros) { _, registro ->
                GlucosaHistorialItem(registro)
            }
        }
    }
}

@Composable
private fun GlucosaChart(registros: List<GlucosaMedicion>) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        if (registros.size < 2) return@Canvas
        val maxVal = registros.maxOf { it.valor }.toFloat().coerceAtLeast(200f)
        val minVal = registros.minOf { it.valor }.toFloat().coerceAtMost(60f)
        val range = maxVal - minVal
        val stepX = size.width / (registros.size - 1)
        val points = registros.mapIndexed { i, r ->
            val x = i * stepX
            val y = size.height - ((r.valor - minVal) / range * size.height)
            Offset(x, y.coerceIn(0f, size.height))
        }
        // Línea de conexión
        val path = Path()
        path.moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { path.lineTo(it.x, it.y) }
        drawPath(path, color = color, style = Stroke(width = 3.dp.toPx()))
        // Puntos
        points.forEachIndexed { i, p ->
            val dotColor = glucosaColor(registros[i].valor)
            drawCircle(color = dotColor, radius = 5.dp.toPx(), center = p)
        }
    }
}

@Composable
private fun GlucosaHistorialItem(registro: GlucosaMedicion) {
    val color = glucosaColor(registro.valor)
    val fmt = SimpleDateFormat("dd/MM HH:mm", Locale("es", "PE"))
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("${registro.valor}", style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${registro.valor} mg/dL", style = MaterialTheme.typography.titleSmall, color = color)
                Text(registro.momento, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!registro.notas.isNullOrBlank()) {
                    Text(registro.notas, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(glucosaLabel(registro.valor), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
                Text(fmt.format(Date(registro.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SemaforoItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
