package com.medicamentos.app.medremind.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.medicamentos.app.medremind.data.local.dao.ContactoEmergenciaDao
import com.medicamentos.app.medremind.data.local.dao.GlucosaDao
import com.medicamentos.app.medremind.data.local.dao.MedicamentoDao
import com.medicamentos.app.medremind.data.local.dao.PacienteDao
import com.medicamentos.app.medremind.data.local.dao.RegistroTomaDao
import com.medicamentos.app.medremind.data.local.dao.TomaProgramadaDao
import com.medicamentos.app.medremind.data.local.dao.TratamientoDao
import com.medicamentos.app.medremind.data.local.dao.UsuarioDao
import com.medicamentos.app.medremind.data.local.entity.ContactoEmergenciaEntity
import com.medicamentos.app.medremind.data.local.entity.GlucosaEntity
import com.medicamentos.app.medremind.data.local.entity.MedicamentoEntity
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
        ContactoEmergenciaEntity::class,
        GlucosaEntity::class,
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun pacienteDao(): PacienteDao
    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun tratamientoDao(): TratamientoDao
    abstract fun tomaProgramadaDao(): TomaProgramadaDao
    abstract fun registroTomaDao(): RegistroTomaDao
    abstract fun contactoEmergenciaDao(): ContactoEmergenciaDao
    abstract fun glucosaDao(): GlucosaDao
}
