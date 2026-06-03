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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.medicamentos.app.medremind.R
import com.medicamentos.app.medremind.domain.model.EstadoToma
import com.medicamentos.app.medremind.domain.model.TomaProgramada
import com.medicamentos.app.medremind.domain.model.Tratamiento
import com.medicamentos.app.medremind.presentation.auth.AVATARES
import com.medicamentos.app.medremind.presentation.theme.OmitidoRed
import com.medicamentos.app.medremind.presentation.theme.PendienteGrey
import com.medicamentos.app.medremind.presentation.theme.TardeAmber
import com.medicamentos.app.medremind.presentation.theme.TomadoGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────
// INICIO / HOY
// ─────────────────────────────────────────────
@Composable
fun PatientHomeScreen(viewModel: PatientHomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val progreso = if (state.total > 0) state.tomadas.toFloat() / state.total else 0f
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Logo de marca
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = "Adherencia360",
                    modifier = Modifier.size(44.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Adherencia360",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Saludo + avatar
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val (icon, _) = AVATARES.getOrNull(state.avatarId) ?: (Icons.Default.Person to "")
                    Icon(icon, null, Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("¡Hola, ${state.nombrePaciente.split(" ").first()}!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Aquí están tus medicamentos de hoy", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Tarjeta de progreso del día
        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Progreso de hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Text("${state.tomadas} / ${state.total}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)), color = if (progreso >= 0.8f) TomadoGreen else if (progreso >= 0.5f) TardeAmber else OmitidoRed)
                    Spacer(Modifier.height(6.dp))
                    Text("${(progreso * 100).toInt()}% completado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Tomas de hoy
        if (state.tomasHoy.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AssignmentTurnedIn, null, Modifier.size(48.dp), tint = TomadoGreen)
                            Spacer(Modifier.height(8.dp))
                            Text("¡Todo al día!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("No tienes medicamentos pendientes para hoy", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        } else {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text("Medicamentos programados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            items(state.tomasHoy) { toma ->
                TomaCard(toma = toma, hora = fmt.format(Date(toma.fechaHoraProgramada)), onMarcar = { estado -> viewModel.marcar(toma, estado) })
            }
        }

        // Tratamientos recetados
        if (state.tratamientos.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalHospital, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text("Mis tratamientos activos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            items(state.tratamientos) { trat -> TratamientoRecetadoCard(trat) }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
fun TratamientoRecetadoCard(trat: Tratamiento) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Medication, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(trat.medicamentoNombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(trat.dosis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text("Cada ${trat.frecuenciaHoras}h · ${trat.horarios.joinToString(", ")}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (trat.medicoNombre.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text("Dr. ${trat.medicoNombre}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${trat.stockRestante}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (trat.stockRestante < 5) OmitidoRed else TomadoGreen)
                Text("unidades", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─────────────────────────────────────────────
// TOMA CARD
// ─────────────────────────────────────────────
@Composable
fun TomaCard(toma: TomaProgramada, hora: String, onMarcar: (EstadoToma) -> Unit) {
    val colorEstado = when (toma.estado) {
        EstadoToma.TOMADO -> TomadoGreen
        EstadoToma.OMITIDO -> OmitidoRed
        EstadoToma.TARDE -> TardeAmber
        EstadoToma.PENDIENTE -> PendienteGrey
    }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(colorEstado.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Medication, null, tint = colorEstado, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(toma.medicamentoNombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(toma.dosis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(hora, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (toma.estado == EstadoToma.PENDIENTE) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(onClick = { onMarcar(EstadoToma.TOMADO) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = TomadoGreen)) {
                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tomado", style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(onClick = { onMarcar(EstadoToma.OMITIDO) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Omitir", style = MaterialTheme.typography.labelMedium)
                    }
                }
            } else {
                Box(Modifier.size(32.dp).clip(CircleShape).background(colorEstado), contentAlignment = Alignment.Center) {
                    Icon(if (toma.estado == EstadoToma.TOMADO || toma.estado == EstadoToma.TARDE) Icons.Default.Check else Icons.Default.Close, toma.estado.name, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// CALENDARIO
// ─────────────────────────────────────────────
@Composable
fun CalendarScreen(viewModel: PatientHomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val cal = Calendar.getInstance()
    var currentYear by remember { mutableStateOf(cal.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(cal.get(Calendar.MONTH)) }
    val meses = listOf("Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre")
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
    val adherenciaMes = if (state.historial.isEmpty()) 0
        else state.historial.filter { r -> Calendar.getInstance().apply { timeInMillis = r.fechaHoraReal }.let { it.get(Calendar.MONTH) == currentMonth && it.get(Calendar.YEAR) == currentYear } }
            .let { lista -> if (lista.isEmpty()) 0 else lista.count { it.estado == EstadoToma.TOMADO || it.estado == EstadoToma.TARDE } * 100 / lista.size }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            // Cabecera mes
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton("<") { if (currentMonth == 0) { currentMonth = 11; currentYear-- } else currentMonth-- }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(meses[currentMonth], style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("$currentYear", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(">") { if (currentMonth == 11) { currentMonth = 0; currentYear++ } else currentMonth++ }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text("Adherencia del mes:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("$adherenciaMes%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (adherenciaMes >= 80) TomadoGreen else if (adherenciaMes >= 50) TardeAmber else OmitidoRed)
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        diasSemana.forEach { d -> Text(d, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    Spacer(Modifier.height(8.dp))
                    val celdas = primerDia + diasEnMes
                    val filas = (celdas + 6) / 7
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(filas) { fila ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(7) { col ->
                                    val idx = fila * 7 + col
                                    val dia = idx - primerDia + 1
                                    Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(6.dp)).background(
                                        when {
                                            dia < 1 || dia > diasEnMes -> Color.Transparent
                                            registrosPorDia[dia] == EstadoToma.TOMADO -> TomadoGreen.copy(alpha = 0.85f)
                                            registrosPorDia[dia] == EstadoToma.OMITIDO -> OmitidoRed.copy(alpha = 0.7f)
                                            registrosPorDia[dia] == EstadoToma.TARDE -> TardeAmber.copy(alpha = 0.7f)
                                            dia == cal.get(Calendar.DAY_OF_MONTH) && currentMonth == cal.get(Calendar.MONTH) && currentYear == cal.get(Calendar.YEAR) -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        }
                                    ), contentAlignment = Alignment.Center) {
                                        if (dia in 1..diasEnMes) {
                                            Text("$dia", style = MaterialTheme.typography.labelSmall, fontWeight = if (dia == cal.get(Calendar.DAY_OF_MONTH) && currentMonth == cal.get(Calendar.MONTH)) FontWeight.Bold else FontWeight.Normal, color = if (registrosPorDia[dia] != null) Color.White else MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                LeyendaItem(TomadoGreen, "Tomado")
                LeyendaItem(OmitidoRed, "Omitido")
                LeyendaItem(TardeAmber, "Parcial")
                LeyendaItem(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), "Sin datos")
            }
        }
    }
}

@Composable
private fun IconButton(label: String, onClick: () -> Unit) {
    Box(Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LeyendaItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

// ─────────────────────────────────────────────
// HISTORIAL
// ─────────────────────────────────────────────
@Composable
fun HistoryScreen(viewModel: PatientHomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    if (state.historial.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AssignmentTurnedIn, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(12.dp))
                Text("Sin historial de tomas", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tu historial aparecerá aquí", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    } else {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AssignmentTurnedIn, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Historial de tomas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Text("(${state.historial.size})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
            }
            items(state.historial) { registro ->
                val color = when (registro.estado) {
                    EstadoToma.TOMADO -> TomadoGreen
                    EstadoToma.OMITIDO -> OmitidoRed
                    EstadoToma.TARDE -> TardeAmber
                    else -> PendienteGrey
                }
                val estadoLabel = when (registro.estado) {
                    EstadoToma.TOMADO -> "Tomado"
                    EstadoToma.OMITIDO -> "Omitido"
                    EstadoToma.TARDE -> "Tardío"
                    else -> "Pendiente"
                }
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(if (registro.estado == EstadoToma.TOMADO || registro.estado == EstadoToma.TARDE) Icons.Default.Check else Icons.Default.Close, null, Modifier.size(20.dp), tint = color)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(registro.medicamentoNombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(4.dp))
                                Text(fmt.format(Date(registro.fechaHoraReal)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(estadoLabel, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// ALERTAS
// ─────────────────────────────────────────────
@Composable
fun PatientAlertsScreen(state: PatientHomeUiState = PatientHomeUiState()) {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val proximas = state.tomasHoy.filter { it.estado == EstadoToma.PENDIENTE }
    val omitidas = state.tomasHoy.filter { it.estado == EstadoToma.OMITIDO }
    val stockBajo = state.tratamientos.filter { it.stockRestante in 1..7 }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Alertas y recordatorios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        // Próximas tomas
        if (proximas.isNotEmpty()) {
            item {
                SectionHeader(Icons.Default.Schedule, "Próximas tomas", MaterialTheme.colorScheme.primary)
            }
            items(proximas) { toma ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Medication, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(toma.medicamentoNombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(toma.dosis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(fmt.format(Date(toma.fechaHoraProgramada)), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Tomas omitidas
        if (omitidas.isNotEmpty()) {
            item { SectionHeader(Icons.Default.Close, "Tomas omitidas hoy", OmitidoRed) }
            items(omitidas) { toma ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = OmitidoRed.copy(alpha = 0.08f))) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(OmitidoRed.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, null, Modifier.size(22.dp), tint = OmitidoRed)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(toma.medicamentoNombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("No registrada · ${fmt.format(Date(toma.fechaHoraProgramada))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Stock bajo
        if (stockBajo.isNotEmpty()) {
            item { SectionHeader(Icons.Default.Notifications, "Stock bajo", TardeAmber) }
            items(stockBajo) { trat ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = TardeAmber.copy(alpha = 0.1f))) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(TardeAmber.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Medication, null, Modifier.size(22.dp), tint = TardeAmber)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(trat.medicamentoNombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("Quedan ${trat.stockRestante} unidades", style = MaterialTheme.typography.bodySmall, color = TardeAmber)
                        }
                        Icon(Icons.Default.Notifications, null, Modifier.size(20.dp), tint = TardeAmber)
                    }
                }
            }
        }

        if (proximas.isEmpty() && omitidas.isEmpty() && stockBajo.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Star, null, Modifier.size(52.dp), tint = TomadoGreen)
                            Spacer(Modifier.height(8.dp))
                            Text("¡Sin alertas pendientes!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Todo está en orden", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, null, Modifier.size(18.dp), tint = color)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = color)
    }
}

// ─────────────────────────────────────────────
// PERFIL DEL PACIENTE
// ─────────────────────────────────────────────
@Composable
fun PatientProfileScreen(
    state: PatientHomeUiState,
    onLogout: () -> Unit
) {
    val (avatarIcon, avatarLabel) = AVATARES.getOrNull(state.avatarId) ?: (Icons.Default.Person to "Avatar")
    val adherenciaColor = when {
        state.adherenciaPorcentaje >= 80 -> TomadoGreen
        state.adherenciaPorcentaje >= 50 -> TardeAmber
        else -> OmitidoRed
    }
    val adherenciaLabel = when {
        state.adherenciaPorcentaje >= 80 -> "Excelente"
        state.adherenciaPorcentaje >= 60 -> "Buena"
        state.adherenciaPorcentaje >= 40 -> "Regular"
        else -> "Necesita mejorar"
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Hero: avatar + nombre ──────────────────────
        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Icon(avatarIcon, avatarLabel, Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(state.nombrePaciente, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Paciente", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    if (state.emailPaciente.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            Text(state.emailPaciente, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // ── Stats: adherencia + tratamientos + tomas ─
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatProfileCard(Modifier.weight(1f), Icons.Default.TrendingUp, "${state.adherenciaPorcentaje}%", "Adherencia", adherenciaColor)
                StatProfileCard(Modifier.weight(1f), Icons.Default.LocalHospital, "${state.tratamientosActivos}", "Tratamientos", MaterialTheme.colorScheme.primary)
                StatProfileCard(Modifier.weight(1f), Icons.Default.Check, "${state.tomadas}/${state.total}", "Tomas hoy", TomadoGreen)
            }
        }

        // ── Nivel de adherencia ──────────────────────
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonitorHeart, null, Modifier.size(20.dp), tint = adherenciaColor)
                        Spacer(Modifier.width(8.dp))
                        Text("Nivel de adherencia", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text(adherenciaLabel, style = MaterialTheme.typography.labelMedium, color = adherenciaColor, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { state.adherenciaPorcentaje / 100f },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = adherenciaColor
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Basado en ${state.historial.size} registros históricos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ── Diagnóstico actual ───────────────────────
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonitorHeart, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Diagnóstico actual", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    if (state.diagnostico.isBlank()) {
                        Text(
                            "Tu médico aún no registró un diagnóstico.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(state.diagnostico, style = MaterialTheme.typography.bodyMedium)
                        if (state.medicoPrincipal.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text("Según ${state.medicoPrincipal}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // ── Información personal ──────────────────────
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Información personal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Divider()
                    InfoRow(Icons.Default.Person, "Nombre", state.nombrePaciente)
                    if (state.emailPaciente.isNotBlank())
                        InfoRow(Icons.Default.Email, "Correo", state.emailPaciente)
                    if (state.telefono.isNotBlank())
                        InfoRow(Icons.Default.Phone, "Celular", state.telefono)
                    if (state.telefonoFamiliar.isNotBlank())
                        InfoRow(Icons.Default.ContactPhone, "Emergencia", state.telefonoFamiliar)
                    InfoRow(Icons.Default.Favorite, "Rol", "Paciente")
                    InfoRow(Icons.Default.LocalHospital, "Médico", state.medicoPrincipal)
                }
            }
        }

        // ── Tratamientos activos ──────────────────────
        if (state.tratamientos.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Medication, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Tratamientos activos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Box(Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text("${state.tratamientos.size}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        state.tratamientos.forEachIndexed { index, trat ->
                            if (index > 0) Divider(Modifier.padding(vertical = 8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Medication, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(trat.medicamentoNombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("${trat.dosis} · Cada ${trat.frecuenciaHoras}h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(trat.horarios.joinToString(", "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${trat.stockRestante}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (trat.stockRestante < 5) OmitidoRed else TomadoGreen)
                                    Text("uds.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Resumen historial ─────────────────────────
        if (state.historial.isNotEmpty()) {
            item {
                val tomadas = state.historial.count { it.estado == EstadoToma.TOMADO }
                val omitidas = state.historial.count { it.estado == EstadoToma.OMITIDO }
                val tardes = state.historial.count { it.estado == EstadoToma.TARDE }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AssignmentTurnedIn, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Resumen histórico", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            ResumenItem("$tomadas", "Tomadas", TomadoGreen)
                            ResumenItem("$omitidas", "Omitidas", OmitidoRed)
                            ResumenItem("$tardes", "Tardías", TardeAmber)
                        }
                    }
                }
            }
        }

        // ── Cerrar sesión ─────────────────────────────
        item {
            Button(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", style = MaterialTheme.typography.labelLarge)
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun StatProfileCard(modifier: Modifier, icon: ImageVector, valor: String, label: String, color: Color) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(22.dp), tint = color)
            Spacer(Modifier.height(6.dp))
            Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(10.dp))
        Text("$label:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(70.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ResumenItem(valor: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
