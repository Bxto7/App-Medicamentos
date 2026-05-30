package com.medicamentos.app.medremind.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.medicamentos.app.medremind.data.local.SessionManager
import com.medicamentos.app.medremind.data.local.dao.UsuarioDao
import com.medicamentos.app.medremind.data.local.entity.UsuarioEntity
import com.medicamentos.app.medremind.data.mappers.toDomain
import com.medicamentos.app.medremind.domain.model.Rol
import com.medicamentos.app.medremind.domain.model.Usuario
import com.medicamentos.app.medremind.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class AuthRepositoryImpl(
    private val usuarioDao: UsuarioDao,
    private val sessionManager: SessionManager,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    // ── Login ──────────────────────────────────────────────────
    override suspend fun login(email: String, password: String): Result<Usuario> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: throw Exception("Error de autenticación")
            val usuario = syncProfileFromFirestore(uid, email.trim().lowercase())
            sessionManager.saveSession(usuario.id, usuario.rol, usuario.nombre)
            Result.success(usuario)
        } catch (firebaseEx: Exception) {
            // Offline fallback: use local Room auth (seed data / sin red)
            loginLocal(email, password)
        }
    }

    private suspend fun loginLocal(email: String, password: String): Result<Usuario> {
        val emailNorm = email.trim().lowercase()
        // Si el usuario existe pero fue registrado con Firebase (sin hash local),
        // no podemos autenticarlo offline — la contraseña la maneja Firebase Auth.
        val byEmail = usuarioDao.findByEmail(emailNorm)
        if (byEmail != null && byEmail.passwordHash.isEmpty()) {
            return Result.failure(Exception("Sin conexión. Necesitas internet para iniciar sesión"))
        }
        val hash = hashPassword(password)
        val entity = usuarioDao.findByEmailAndPassword(emailNorm, hash)
            ?: return Result.failure(Exception("Correo o contraseña incorrectos"))
        val domain = entity.toDomain()
        sessionManager.saveSession(domain.id, domain.rol, domain.nombre)
        return Result.success(domain)
    }

    // ── Registro ───────────────────────────────────────────────
    override suspend fun register(
        nombre: String,
        email: String,
        password: String,
        rol: Rol,
        avatarId: Int
    ): Result<Usuario> {
        return try {
            val emailNorm = email.trim().lowercase()

            // 1. Crear usuario en Firebase Auth
            val authResult = firebaseAuth.createUserWithEmailAndPassword(emailNorm, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Error al crear usuario")

            // 2. Guardar perfil en Firestore (no bloqueante — si falla sin red, Room ya guardó)
            val userData = mapOf(
                "nombre" to nombre.trim(),
                "email" to emailNorm,
                "rol" to rol.name,
                "avatarId" to avatarId
            )
            try { firestore.collection("usuarios").document(uid).set(userData).await() } catch (_: Exception) {}

            // 3. Cachear en Room (sin password hash — la auth la maneja Firebase)
            val entity = UsuarioEntity(uid, emailNorm, nombre.trim(), "", rol.name, avatarId)
            usuarioDao.upsert(entity)

            sessionManager.saveSession(uid, rol, nombre.trim())
            Result.success(Usuario(uid, emailNorm, nombre.trim(), rol, avatarId))

        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e.message)))
        }
    }

    // ── Logout ─────────────────────────────────────────────────
    override suspend fun logout() {
        try { firebaseAuth.signOut() } catch (_: Exception) {}
        sessionManager.clearSession()
    }

    // ── Actualizar nombre ──────────────────────────────────────
    override suspend fun updateNombre(nombre: String) {
        val userId = sessionManager.getUserId().first() ?: return
        usuarioDao.updateNombre(userId, nombre.trim())
        sessionManager.updateNombre(nombre.trim())
        try {
            firestore.collection("usuarios").document(userId)
                .update("nombre", nombre.trim()).await()
        } catch (_: Exception) { /* Sin red: se actualizará en el siguiente sync */ }
    }

    // ── Flows de sesión ────────────────────────────────────────
    override fun getUserId(): Flow<String?> = sessionManager.getUserId()
    override fun getRol(): Flow<Rol?> = sessionManager.getRol()
    override fun getNombre(): Flow<String?> = sessionManager.getNombre()
    override fun isLoggedIn(): Flow<Boolean> = sessionManager.isLoggedIn()

    // ── Helpers ────────────────────────────────────────────────

    private suspend fun syncProfileFromFirestore(uid: String, emailFallback: String): Usuario {
        return try {
            val doc = firestore.collection("usuarios").document(uid).get().await()
            if (doc.exists()) {
                val nombre = doc.getString("nombre") ?: ""
                val rol = Rol.valueOf(doc.getString("rol") ?: "PACIENTE")
                val avatarId = doc.getLong("avatarId")?.toInt() ?: 0
                val email = doc.getString("email") ?: emailFallback
                // Actualizar caché local
                usuarioDao.upsert(UsuarioEntity(uid, email, nombre, "", rol.name, avatarId))
                Usuario(uid, email, nombre, rol, avatarId)
            } else {
                // El documento no existe aún — usar datos locales si los hay
                val local = usuarioDao.findById(uid) ?: usuarioDao.findByEmail(emailFallback)
                val entity = local ?: UsuarioEntity(uid, emailFallback, "", "", "PACIENTE", 0)
                entity.toDomain()
            }
        } catch (e: Exception) {
            // Sin red: usar caché local
            val local = usuarioDao.findByEmail(emailFallback)
                ?: UsuarioEntity(uid, emailFallback, "", "", "PACIENTE", 0)
            local.toDomain()
        }
    }

    private fun mapFirebaseError(message: String?): String = when {
        message?.contains("email-already-in-use") == true -> "El correo ya está registrado"
        message?.contains("wrong-password") == true || message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> "Correo o contraseña incorrectos"
        message?.contains("user-not-found") == true -> "Usuario no encontrado"
        message?.contains("weak-password") == true -> "La contraseña debe tener al menos 6 caracteres"
        message?.contains("network") == true || message?.contains("Network") == true -> "Sin conexión a Internet"
        message?.contains("too-many-requests") == true -> "Demasiados intentos. Intenta más tarde"
        else -> "Error: $message"
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
