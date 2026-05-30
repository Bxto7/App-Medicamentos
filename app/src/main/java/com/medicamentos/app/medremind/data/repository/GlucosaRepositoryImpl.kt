package com.medicamentos.app.medremind.data.repository

import com.medicamentos.app.medremind.data.local.dao.GlucosaDao
import com.medicamentos.app.medremind.data.mappers.toDomain
import com.medicamentos.app.medremind.data.mappers.toEntity
import com.medicamentos.app.medremind.domain.model.GlucosaMedicion
import com.medicamentos.app.medremind.domain.repository.GlucosaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GlucosaRepositoryImpl @Inject constructor(
    private val dao: GlucosaDao
) : GlucosaRepository {

    override fun getByPaciente(pacienteId: String): Flow<List<GlucosaMedicion>> =
        dao.getByPaciente(pacienteId).map { list -> list.map { it.toDomain() } }

    override fun getDesde(pacienteId: String, desde: Long): Flow<List<GlucosaMedicion>> =
        dao.getDesde(pacienteId, desde).map { list -> list.map { it.toDomain() } }

    override suspend fun registrar(medicion: GlucosaMedicion) {
        dao.insert(medicion.toEntity())
    }

    override suspend fun eliminar(id: String) {
        dao.delete(id)
    }

    override suspend fun getUltima(pacienteId: String): GlucosaMedicion? =
        dao.getUltima(pacienteId)?.toDomain()
}
