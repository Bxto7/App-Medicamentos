package com.medicamentos.app.medremind.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.medicamentos.app.medremind.security.rememberBiometricLauncher

/**
 * Pantalla de bloqueo biométrico que aparece al abrir la app cuando ya hay una
 * sesión guardada. El usuario debe autenticarse con huella/rostro para entrar,
 * o cerrar sesión. No aparece tras un login manual (ese ya autenticó con contraseña).
 */
@Composable
fun BiometricGateScreen(
    nombreUsuario: String,
    onUnlock: () -> Unit,
    onLogout: () -> Unit
) {
    var error by remember { mutableStateOf<String?>(null) }

    val launcher = rememberBiometricLauncher(
        onSuccess = onUnlock,
        onError = { error = it }
    )

    // Auto-lanzar el prompt al entrar
    LaunchedEffect(Unit) { launcher() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MonitorHeart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer16()
            Text("Adherencia360", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            Spacer8()
            Text(
                if (nombreUsuario.isNotBlank()) "Hola de nuevo, ${nombreUsuario.split(" ").first()}" else "Bienvenido de nuevo",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Box(Modifier.height(40.dp))

            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = "Desbloquear con biometría",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
            }
            Spacer16()
            Text(
                "Verifica tu identidad para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (error != null) {
                Spacer8()
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }

            Box(Modifier.height(32.dp))

            Button(
                onClick = { error = null; launcher() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer8H()
                Text("Desbloquear")
            }
            Spacer8()
            TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable private fun Spacer8() = Box(Modifier.height(8.dp))
@Composable private fun Spacer16() = Box(Modifier.height(16.dp))
@Composable private fun Spacer8H() = Box(Modifier.size(8.dp))
