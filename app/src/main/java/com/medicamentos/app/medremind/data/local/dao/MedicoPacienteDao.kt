package com.medicamentos.app.medremind.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medicamentos.app.medremind.data.local.entity.MedicoPacienteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Proyección de un paciente asociado a un médico. Combina la identidad real
 * (usuarios) con la ficha clínica opcional (pacientes), por eso edad/diagnóstico
 * pueden venir nulos cuando el médico aún no completó el perfil.
 */
data class PacienteConPerfil(
    val id: String,
    val nombre: String,
    val email: String,
    val edad: Int?,
    val diagnostico: String?,
    @ColumnInfo(name = "foto_url") val fotoUrl: String?
)

/**
 * Proyección para la pantalla "Asociar pacientes": todos los usuarios con rol
 * PACIENTE y si ya están asociados al médico actual.
 */
data class PacienteAsociableRow(
    val id: String,
    val nombre: String,
    val email: String,
    val asociado: Int,
    val diagnostico: String?
)

@Dao
interface MedicoPacienteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(asociacion: MedicoPacienteEntity)

    @Query("DELETE FROM medico_paciente WHERE medico_id = :medicoId AND paciente_id = :pacienteId")
    suspend fun delete(medicoId: String, pacienteId: String)

    @Query("UPDATE medico_paciente SET diagnostico = :diagnostico WHERE medico_id = :medicoId AND paciente_id = :pacienteId")
    suspend fun updateDiagnostico(medicoId: String, pacienteId: String, diagnostico: String)

    @Query(
        """
        SELECT u.id AS id, u.nombre AS nombre, u.email AS email,
               p.edad AS edad, mp.diagnostico AS diagnostico, p.foto_url AS foto_url
        FROM medico_paciente mp
        JOIN usuarios u ON u.id = mp.paciente_id
        LEFT JOIN pacientes p ON p.id = u.id
        WHERE mp.medico_id = :medicoId
        ORDER BY u.nombre ASC
        """
    )
    fun getPacientesByMedico(medicoId: String): Flow<List<PacienteConPerfil>>

    @Query(
        """
        SELECT u.id AS id, u.nombre AS nombre, u.email AS email,
               p.edad AS edad, mp.diagnostico AS diagnostico, p.foto_url AS foto_url
        FROM medico_paciente mp
        JOIN usuarios u ON u.id = mp.paciente_id
        LEFT JOIN pacientes p ON p.id = u.id
        WHERE mp.medico_id = :medicoId
          AND (u.nombre LIKE '%' || :query || '%' OR mp.diagnostico LIKE '%' || :query || '%')
        ORDER BY u.nombre ASC
        """
    )
    fun searchPacientesByMedico(medicoId: String, query: String): Flow<List<PacienteConPerfil>>

    @Query("SELECT COUNT(*) FROM medico_paciente WHERE medico_id = :medicoId")
    fun countByMedico(medicoId: String): Flow<Int>

    @Query(
        """
        SELECT u.id AS id, u.nombre AS nombre, u.email AS email,
               EXISTS(
                   SELECT 1 FROM medico_paciente mp
                   WHERE mp.medico_id = :medicoId AND mp.paciente_id = u.id
               ) AS asociado,
               (
                   SELECT mp.diagnostico FROM medico_paciente mp
                   WHERE mp.medico_id = :medicoId AND mp.paciente_id = u.id
               ) AS diagnostico
        FROM usuarios u
        WHERE u.rol = 'PACIENTE'
        ORDER BY u.nombre ASC
        """
    )
    fun getPacientesAsociables(medicoId: String): Flow<List<PacienteAsociableRow>>

    @Query("SELECT EXISTS(SELECT 1 FROM medico_paciente WHERE medico_id = :medicoId AND paciente_id = :pacienteId)")
    suspend fun existeAsociacion(medicoId: String, pacienteId: String): Boolean

    /** Diagnóstico vigente del paciente (el más reciente entre sus médicos), para su perfil. */
    @Query("SELECT diagnostico FROM medico_paciente WHERE paciente_id = :pacienteId AND diagnostico != '' ORDER BY fecha_asociacion DESC LIMIT 1")
    fun getDiagnosticoDePaciente(pacienteId: String): Flow<String?>

    /** Médico responsable del paciente (el más reciente que lo asoció), para su perfil. */
    @Query(
        """
        SELECT u.nombre FROM medico_paciente mp
        JOIN usuarios u ON u.id = mp.medico_id
        WHERE mp.paciente_id = :pacienteId
        ORDER BY mp.fecha_asociacion DESC
        LIMIT 1
        """
    )
    fun getMedicoDePaciente(pacienteId: String): Flow<String?>
}
