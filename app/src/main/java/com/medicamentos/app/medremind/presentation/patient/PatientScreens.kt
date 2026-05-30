package com.medicamentos.app.medremind.presentation.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medicamentos.app.medremind.domain.model.EstadoToma
import com.medicamentos.app.medremind.domain.model.TomaProgramada
import com.medicamentos.app.medremind.presentation.auth.AVATARES
import com.medicamentos.app.medremind.presentation.theme.OmitidoRed
import com.medicamentos.app.medremind.presentation.theme.PendienteGrey
import com.medicamentos.app.medremind.presentation.theme.TardeAmber
import com.medicamentos.app.medremind.presentation.theme.TomadoGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PatientHomeScreen(viewModel: PatientHomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val progreso = if (state.total > 0) state.tomadas.toFloat() / state.total else 0f
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val (icon, _) = AVATARES.getOrNull(state.avatarId) ?: (Icons.Default.Person to "")
                    Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("¡Hola, ${state.nombrePaciente}!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Control de tu diabetes hoy", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progreso del día", style = MaterialTheme.typography.titleMedium)
                        Text("${state.tomadas}/${state.total}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = TomadoGreen)
                    Spacer(Modifier.height(4.dp))
                    Text("${(progreso * 100).toInt()}% de adherencia", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (state.tomasHoy.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No hay medicamentos programados para hoy", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(state.tomasHoy) { toma ->
                TomaCard(toma = toma, hora = fmt.format(Date(toma.fechaHoraProgramada)), onMarcar = { estado -> viewModel.marcar(toma, estado) })
            }
        }
    }
}

@Composable
fun TomaCard(toma: TomaProgramada, hora: String, onMarcar: (EstadoToma) -> Unit) {
    val colorEstado = when (toma.estado) {
        EstadoToma.TOMADO -> TomadoGreen
        EstadoToma.OMITIDO -> OmitidoRed
        EstadoToma.TARDE -> TardeAmber
        EstadoToma.PENDIENTE -> PendienteGrey
    }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(colorEstado.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Medication, contentDescription = null, tint = colorEstado, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(toma.medicamentoNombre, style = MaterialTheme.typography.titleMedium)
                Text(toma.dosis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(hora, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (toma.estado == EstadoToma.PENDIENTE) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(onClick = { onMarcar(EstadoToma.TOMADO) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = TomadoGreen)) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tomado", style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(onClick = { onMarcar(EstadoToma.OMITIDO) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Omitir", style = MaterialTheme.typography.labelMedium)
                    }
                }
            } else {
                Box(Modifier.size(32.dp).clip(CircleShape).background(colorEstado), contentAlignment = Alignment.Center) {
                    Icon(if (toma.estado == EstadoToma.TOMADO || toma.estado == EstadoToma.TARDE) Icons.Default.Check else Icons.Default.Close, contentDescription = toma.estado.name, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun CalendarScreen(viewModel: PatientHomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val cal = Calendar.getInstance()
    var currentYear by remember { mutableStateOf(cal.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(cal.get(Calendar.MONTH)) }
    val meses = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    val diasSemana = listOf("L", "M", "M", "J", "V", "S", "D")

    val registrosPorDia: Map<Int, EstadoToma> = remember(state.historial, currentMonth, currentYear) {
        val map = mutableMapOf<Int, MutableList<EstadoToma>>()
        state.historial.forEach { r ->
            val c = Calendar.getInstance().apply { timeInMillis = r.fechaHoraReal }
            if (c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear) {
                map.getOrPut(c.get(Calendar.DAY_OF_MONTH)) { mutableListOf() }.add(r.estado)
            }
        }
        map.mapValues { (_, estados) ->
            when {
                estados.all { it == EstadoToma.TOMADO } -> EstadoToma.TOMADO
                estados.all { it == EstadoToma.OMITIDO } -> EstadoToma.OMITIDO
                else -> EstadoToma.TARDE
            }
        }
    }

    val calMes = Calendar.getInstance().apply { set(currentYear, currentMonth, 1) }
    val primerDia = (calMes.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val diasEnMes = calMes.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("<", modifier = Modifier.clickable { if (currentMonth == 0) { currentMonth = 11; currentYear-- } else currentMonth-- }.padding(8.dp))
            Text("${meses[currentMonth]} $currentYear", style = MaterialTheme.typography.titleMedium)
            Text(">", modifier = Modifier.clickable { if (currentMonth == 11) { currentMonth = 0; currentYear++ } else currentMonth++ }.padding(8.dp))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            diasSemana.forEach { d -> Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Spacer(Modifier.height(4.dp))
        val celdas = primerDia + diasEnMes
        val filas = (celdas + 6) / 7
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(filas) { fila ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { col ->
                        val idx = fila * 7 + col
                        val dia = idx - primerDia + 1
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(CircleShape).background(
                            when {
                                dia < 1 || dia > diasEnMes -> Color.Transparent
                                registrosPorDia[dia] == EstadoToma.TOMADO -> TomadoGreen.copy(alpha = 0.8f)
                                registrosPorDia[dia] == EstadoToma.OMITIDO -> OmitidoRed.copy(alpha = 0.7f)
                                registrosPorDia[dia] == EstadoToma.TARDE -> TardeAmber.copy(alpha = 0.7f)
                                dia == cal.get(Calendar.DAY_OF_MONTH) && currentMonth == cal.get(Calendar.MONTH) && currentYear == cal.get(Calendar.YEAR) -> MaterialTheme.colorScheme.primaryContainer
                                else -> Color.Transparent
                            }
                        ), contentAlignment = Alignment.Center) {
                            if (dia in 1..diasEnMes) {
                                Text("$dia", style = MaterialTheme.typography.labelSmall, color = if (registrosPorDia[dia] != null) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            LeyendaItem(TomadoGreen, "Tomado")
            LeyendaItem(OmitidoRed, "Omitido")
            LeyendaItem(TardeAmber, "Parcial")
            LeyendaItem(PendienteGrey, "Sin datos")
        }
    }
}

@Composable
fun LeyendaItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun HistoryScreen(viewModel: PatientHomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    if (state.historial.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin historial de tomas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.historial) { registro ->
                val color = when (registro.estado) {
                    EstadoToma.TOMADO -> TomadoGreen
                    EstadoToma.OMITIDO -> OmitidoRed
                    EstadoToma.TARDE -> TardeAmber
                    else -> PendienteGrey
                }
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(registro.medicamentoNombre, style = MaterialTheme.typography.titleSmall)
                            Text(fmt.format(Date(registro.fechaHoraReal)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(registro.estado.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium, color = color)
                    }
                }
            }
        }
    }
}

@Composable
fun PatientAlertsScreen() {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Próximos recordatorios", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }
        items(listOf("Metformina - 20:00", "Glibenclamida - mañana 08:00")) { item ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(item, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun PatientProfileScreen(
    nombrePaciente: String,
    avatarId: Int,
    onLogout: () -> Unit
) {
    val (avatarIcon, avatarLabel) = AVATARES.getOrNull(avatarId) ?: (Icons.Default.Person to "Avatar")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(avatarIcon, contentDescription = avatarLabel, modifier = Modifier.size(52.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(nombrePaciente, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Paciente — Recordatorio de medicamentos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item { Divider() }

        item {
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
