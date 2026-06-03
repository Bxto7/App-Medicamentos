package com.medicamentos.app.medremind.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.medicamentos.app.medremind.data.local.dao.MedicamentoDao
import com.medicamentos.app.medremind.data.local.dao.MedicoPacienteDao
import com.medicamentos.app.medremind.data.local.dao.PacienteDao
import com.medicamentos.app.medremind.data.local.dao.RegistroTomaDao
import com.medicamentos.app.medremind.data.local.dao.TomaProgramadaDao
import com.medicamentos.app.medremind.data.local.dao.TratamientoDao
import com.medicamentos.app.medremind.data.local.dao.UsuarioDao
import com.medicamentos.app.medremind.data.local.entity.MedicamentoEntity
import com.medicamentos.app.medremind.data.local.entity.MedicoPacienteEntity
import com.medicamentos.app.medremind.data.local.entity.PacienteEntity
import com.medicamentos.app.medremind.data.local.entity.RegistroTomaEntity
import com.medicamentos.app.medremind.data.local.entity.TomaProgramadaEntity
import com.medicamentos.app.medremind.data.local.entity.TratamientoEntity
import com.medicamentos.app.medremind.data.local.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        PacienteEntity::class,
        MedicamentoEntity::class,
        TratamientoEntity::class,
        TomaProgramadaEntity::class,
        RegistroTomaEntity::class,
        MedicoPacienteEntity::class,
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun pacienteDao(): PacienteDao
    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun tratamientoDao(): TratamientoDao
    abstract fun tomaProgramadaDao(): TomaProgramadaDao
    abstract fun registroTomaDao(): RegistroTomaDao
    abstract fun medicoPacienteDao(): MedicoPacienteDao

    companion object {
        /**
         * v3 -> v4: tabla de unión médico-paciente (N:N) y médico responsable
         * denormalizado en cada tratamiento.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS medico_paciente (
                        medico_id TEXT NOT NULL,
                        paciente_id TEXT NOT NULL,
                        fecha_asociacion INTEGER NOT NULL,
                        PRIMARY KEY(medico_id, paciente_id)
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE tratamientos ADD COLUMN medico_id TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE tratamientos ADD COLUMN medico_nombre TEXT NOT NULL DEFAULT ''")
            }
        }

        /**
         * v4 -> v5: teléfonos de contacto del paciente (propio y de emergencia)
         * y diagnóstico principal guardado en la relación médico-paciente.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE usuarios ADD COLUMN telefono TEXT")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN telefono_familiar TEXT")
                db.execSQL("ALTER TABLE medico_paciente ADD COLUMN diagnostico TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
