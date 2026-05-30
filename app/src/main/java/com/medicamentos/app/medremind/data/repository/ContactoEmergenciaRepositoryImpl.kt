package com.medicamentos.app.medremind.data.repository

import com.medicamentos.app.medremind.data.local.dao.ContactoEmergenciaDao
import com.medicamentos.app.medremind.data.mappers.toDomain
import com.medicamentos.app.medremind.data.mappers.toEntity
import com.medicamentos.app.medremind.domain.model.ContactoEmergencia
import com.medicamentos.app.medremind.domain.repository.ContactoEmergenciaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContactoEmergenciaRepositoryImpl @Inject constructor(
    private val dao: ContactoEmergenciaDao
) : ContactoEmergenciaRepository {

    override fun getByUsuario(usuarioId: String): Flow<List<ContactoEmergencia>> =
        dao.getByUsuario(usuarioId).map { list -> list.map { it.toDomain() } }

    override suspend fun getByUsuarioSync(usuarioId: String): List<ContactoEmergencia> =
        dao.getByUsuarioSync(usuarioId).map { it.toDomain() }

    override suspend fun saveContactos(usuarioId: String, contactos: List<ContactoEmergencia>) {
        dao.deleteByUsuario(usuarioId)
        dao.insertAll(contactos.map { it.toEntity() })
    }
}
