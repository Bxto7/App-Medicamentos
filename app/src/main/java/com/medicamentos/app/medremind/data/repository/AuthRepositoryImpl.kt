package com.medicamentos.app.medremind.data.repository

import com.medicamentos.app.medremind.data.local.SessionManager
import com.medicamentos.app.medremind.data.local.dao.UsuarioDao
import com.medicamentos.app.medremind.data.local.entity.UsuarioEntity
import com.medicamentos.app.medremind.data.mappers.toDomain
import com.medicamentos.app.medremind.domain.model.Rol
import com.medicamentos.app.medremind.domain.model.Usuario
import com.medicamentos.app.medremind.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Usuario> {
        val hash = hashPassword(password)
        val entity = usuarioDao.findByEmailAndPassword(email.trim().lowercase(), hash)
            ?: return Result.failure(Exception("Correo o contraseña incorrectos"))
        val domain = entity.toDomain()
        sessionManager.saveSession(domain.id, domain.rol, domain.nombre)
        return Result.success(domain)
    }

    override suspend fun register(
        nombre: String,
        email: String,
        password: String,
        rol: Rol,
        avatarId: Int
    ): Result<Usuario> {
        val emailNorm = email.trim().lowercase()
        val existente = usuarioDao.findByEmail(emailNorm)
        if (existente != null) return Result.failure(Exception("El correo ya está registrado"))

        val id = UUID.randomUUID().toString()
        val entity = UsuarioEntity(
            id = id,
            email = emailNorm,
            nombre = nombre.trim(),
            passwordHash = hashPassword(password),
            rol = rol.name,
            avatarId = avatarId
        )
        usuarioDao.insert(entity)

        val domain = entity.toDomain()
        sessionManager.saveSession(domain.id, domain.rol, domain.nombre)
        return Result.success(domain)
    }

    override suspend fun logout() = sessionManager.clearSession()

    override fun getUserId(): Flow<String?> = sessionManager.getUserId()
    override fun getRol(): Flow<Rol?> = sessionManager.getRol()
    override fun getNombre(): Flow<String?> = sessionManager.getNombre()
    override fun isLoggedIn(): Flow<Boolean> = sessionManager.isLoggedIn()

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
