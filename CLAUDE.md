# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> Las reglas de la sección **Restricciones de seguridad** son obligatorias y no negociables. MedRemind procesa información de salud (PHI).

---

## 1. Resumen del proyecto

**MedRemind** es una aplicación Android nativa de recordatorio y seguimiento de medicamentos con dos perfiles de usuario con experiencias completamente distintas:

- **Médico / Admin**: gestiona pacientes, asigna tratamientos, monitorea adherencia y recibe alertas.
- **Paciente**: ve sus medicamentos del día, marca tomas, consulta su calendario e historial, y recibe recordatorios.

El login es único, con selección de rol, y redirige al flujo correspondiente. El rol y los permisos se validan **siempre en el backend**; el cliente solo refleja lo que el servidor autoriza.

---

## 2. Comandos de desarrollo

```bash
# Build
./gradlew assembleDebug          # APK de debug
./gradlew assembleRelease        # APK de release
./gradlew build                  # Compilación completa

# Instalar en dispositivo/emulador conectado
./gradlew installDebug

# Tests unitarios (JVM local)
./gradlew test
./gradlew test --tests "com.medicamentos.app.medremind.ExampleUnitTest"

# Tests instrumentados (requiere dispositivo/emulador)
./gradlew connectedAndroidTest

# Lint
./gradlew lint
./gradlew lintDebug              # Solo variante debug

# Limpiar
./gradlew clean
```

---

## 3. Stack técnico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material Design 3 (Material You) |
| Arquitectura | MVVM + Clean Architecture (presentation / domain / data) |
| Navegación | Navigation Compose (NavHost) |
| Inyección de dependencias | Hilt |
| Base de datos local | Room |
| Backend / sincronización | Firebase Auth + Firestore |
| Red | Retrofit + OkHttp (si se usa API REST propia) |
| Imágenes | Coil |
| Gráficas | MPAndroidChart o Vico (Compose nativo) |
| Alarmas / recordatorios | WorkManager + AlarmManager |
| Notificaciones | NotificationChannel API |
| Almacenamiento seguro | EncryptedSharedPreferences + Android Keystore |
| Biometría | BiometricPrompt (androidx.biometric) |

**minSdk:** 26 (Android 8.0). **targetSdk:** 36.

---

## 4. Arquitectura y estructura de carpetas

```
app/src/main/java/com/medicamentos/app/medremind/
├── di/                  # Módulos de Hilt
├── data/
│   ├── local/           # Room: entidades, DAOs, database
│   ├── remote/          # Firestore / Retrofit, DTOs
│   ├── repository/      # Implementaciones de repositorios
│   └── mappers/         # DTO <-> Domain <-> Entity
├── domain/
│   ├── model/           # Modelos de dominio (puros, sin Android)
│   ├── repository/      # Interfaces de repositorio
│   └── usecase/         # Casos de uso (una sola responsabilidad)
├── presentation/
│   ├── auth/            # Login, selección de rol
│   ├── admin/           # Pantallas del médico/admin
│   ├── patient/         # Pantallas del paciente
│   ├── common/          # Componentes Compose reutilizables
│   └── theme/           # Color, tipografía, tema claro/oscuro
├── notification/        # WorkManager, AlarmManager, receivers
└── security/            # Cifrado, gestión de sesión, validaciones
```

**Flujo de datos:** UI → ViewModel (StateFlow/UiState) → UseCase → Repository interface → implementación en `data/` con Room como fuente de verdad local y sincronización hacia Firestore.

Los modelos de `domain/model/` son Kotlin puro — sin dependencias de Room ni Firestore. Los mappers en `data/mappers/` hacen la conversión entre capas.

---

## 5. Pantallas y navegación

### 5.1 Autenticación
- **LoginScreen**: email, contraseña, selector de rol (Médico / Paciente).
- **BiometricLogin**: reingreso rápido vía `BiometricPrompt`.
- Redirección automática al grafo de navegación según rol validado por el backend.

### 5.2 Vista Médico / Admin (Bottom Navigation, 5 tabs)
1. **Dashboard**: adherencia global del día, alertas pendientes, gráfica semanal.
2. **Pacientes**: lista con buscador y filtros, FAB para añadir paciente.
3. **Detalle de paciente**: datos, medicamentos, heatmap de adherencia, historial.
4. **Medicamentos**: catálogo, crear/editar tratamiento (dosis, frecuencia, horarios, duración).
5. **Alertas / Reportes**: dosis omitidas por criticidad, exportar PDF/CSV.

### 5.3 Vista Paciente (Bottom Navigation, 5 tabs)
1. **Inicio / Hoy**: medicamentos del día por hora, botón "Tomado", progreso diario.
2. **Calendario**: heatmap mensual de adherencia, detalle del día en bottom sheet.
3. **Historial**: tomas cronológicas con estado, filtros por medicamento y fecha.
4. **Alertas**: recordatorios próximos, stock bajo, mensajes del médico.
5. **Perfil**: datos personales, configuración de notificaciones, Mi Stock, cerrar sesión.

---

## 6. Modelo de datos (Room + Firestore)

- `Usuario` — id (UUID), email, rol (ADMIN | MEDICO | PACIENTE).
- `Paciente` — id, nombre, edad, diagnóstico, fotoUrl, medicoAsignadoId.
- `Medicamento` — id, nombre, dosis, vía, instrucciones, fotoUrl.
- `Tratamiento` — id, pacienteId, medicamentoId, frecuencia, horarios, fechaInicio, fechaFin, stockRestante.
- `TomaProgramada` — id, tratamientoId, fechaHoraProgramada, estado.
- `RegistroToma` — id, tomaProgramadaId, fechaHoraReal, estado (TOMADO | OMITIDO | TARDE).

Usa IDs UUID, nunca secuenciales predecibles. Room es la fuente de verdad local; Firestore sincroniza de forma bidireccional. El dominio nunca depende de Room ni Firestore directamente.

---

## 7. Sistema de notificaciones

- Alarmas exactas con `AlarmManager.setExactAndAllowWhileIdle()` (permiso `SCHEDULE_EXACT_ALARM` en Android 12+).
- `WorkManager` para reprogramación tras reinicio (`BOOT_COMPLETED`) y tareas de sincronización.
- Acción "Tomado" directa desde la notificación vía `NotificationActionReceiver`, sin abrir la app.
- Las alarmas son críticas: deben sobrevivir a reinicios, modo Doze y ahorro de batería.

---

## 8. Diseño

- Material Design 3, colores dinámicos donde el dispositivo lo soporte.
- Paleta base: primario azul médico, acento verde.
- Soporte obligatorio modo claro y oscuro.
- Accesibilidad: texto escalable, contraste AA mínimo, `contentDescription` en iconos e imágenes, navegación TalkBack.

---

## 9. Restricciones de seguridad (OBLIGATORIAS)

> El incumplimiento puede tener consecuencias legales (HIPAA, GDPR, Ley N.º 29733 de Protección de Datos Personales en Perú).

### 9.1 Autenticación y autorización
- El rol y los permisos se validan **siempre en el backend** (Firestore Security Rules o API). El cliente nunca decide autorización.
- Autorización por recurso: un médico accede solo a *sus* pacientes; un paciente accede solo a *sus propios* datos.
- Tokens de sesión con expiración, refresh tokens rotativos, auto-logout por inactividad (5–15 min configurable).

### 9.2 Almacenamiento de credenciales y secretos
- **Prohibido** hardcodear API keys, claves de Firebase, contraseñas o tokens en el código o en `strings.xml`.
- Claves de configuración en `local.properties` / variables de entorno / Secrets Gradle Plugin, fuera de VCS.
- Datos sensibles en dispositivo: `EncryptedSharedPreferences` + clave maestra en **Android Keystore**.

### 9.3 Datos en tránsito y en reposo
- Todo el tráfico sobre **HTTPS/TLS**. `cleartextTrafficPermitted="false"` en Network Security Config.
- Considera **certificate pinning** para la API propia.
- Cifra la BD local con **SQLCipher** si almacena PHI sin sincronizar.
- PHI nunca en URLs, query params ni en analytics de terceros.

### 9.4 Logs y telemetría
- **Nunca** registres: contraseñas, tokens, nombres de pacientes, diagnósticos ni datos de salud, ni en debug.
- Logs verbosos solo con `if (BuildConfig.DEBUG)`.
- Si usas Crashlytics/Analytics, anonimiza; jamás envíes PHI.
- `FLAG_SECURE` en pantallas con PHI sensible para deshabilitar capturas de pantalla.

### 9.5 Validación de entrada
- Valida y sanitiza toda entrada del usuario; el servidor es la autoridad final.
- Consultas Room parametrizadas; nunca concatenes SQL crudo con datos del usuario.
- Valida MIME real de archivos subidos (fotos de perfil/medicamento), no solo extensión.

### 9.6 Permisos de Android
- Solicita el mínimo de permisos, solo en el momento de uso.
- Justifica cada permiso peligroso. `SCHEDULE_EXACT_ALARM`, `POST_NOTIFICATIONS` (Android 13+) y cámara/galería requieren consentimiento explícito.

### 9.7 Integridad del build
- Habilita **R8 / ProGuard** con `minifyEnabled` y `shrinkResources` en release.
- Firma con keystore protegida y fuera del repositorio. No publiques builds de debug.

### 9.8 Privacidad y cumplimiento
- Pantalla de consentimiento informado y política de privacidad accesible.
- Permite exportar y eliminar datos del usuario (derecho de acceso y supresión).
- Minimización de datos: recolecta solo lo estrictamente necesario.

### 9.9 Seguridad clínica
- **No** generes dosis, diagnósticos ni consejos médicos automáticamente. La app refleja solo lo que el médico configuró.
- Muestra avisos claros: la app es una herramienta de recordatorio y no sustituye criterio médico.

---

## 10. Convenciones de código

- Kotlin coding conventions oficiales.
- Componentes Compose en `PascalCase`, funciones `@Composable` puras con state hoisting.
- ViewModels exponen `StateFlow`/`UiState` inmutables; sin lógica de negocio en Composables.
- Casos de uso de una sola responsabilidad (`MarcarTomaUseCase`, `ObtenerPacientesUseCase`).
- Strings de UI en `strings.xml`, nunca hardcodeados en el código.
- Errores con `Result` o sealed class; nunca swallow exceptions.
- Tests unitarios para casos de uso y ViewModels; instrumentados para DAOs y flujos críticos (login, marcar toma, programar alarma).
