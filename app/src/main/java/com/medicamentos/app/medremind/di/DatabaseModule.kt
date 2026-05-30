package com.medicamentos.app.medremind.di

import android.content.Context
import androidx.room.Room
import com.medicamentos.app.medremind.data.local.AppDatabase
import com.medicamentos.app.medremind.data.local.SeedDataCallback
import com.medicamentos.app.medremind.security.DatabaseKeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        seedDataCallback: SeedDataCallback,
        keyManager: DatabaseKeyManager
    ): AppDatabase {
        val passphrase = SQLiteDatabase.getBytes(
            String(keyManager.getOrCreateKey()).toCharArray()
        )
        return Room.databaseBuilder(context, AppDatabase::class.java, "medremind.db")
            .openHelperFactory(SupportFactory(passphrase))
            .addCallback(seedDataCallback)
            .build()
    }

    @Provides fun provideUsuarioDao(db: AppDatabase) = db.usuarioDao()
    @Provides fun providePacienteDao(db: AppDatabase) = db.pacienteDao()
    @Provides fun provideMedicamentoDao(db: AppDatabase) = db.medicamentoDao()
    @Provides fun provideTratamientoDao(db: AppDatabase) = db.tratamientoDao()
    @Provides fun provideTomaProgramadaDao(db: AppDatabase) = db.tomaProgramadaDao()
    @Provides fun provideRegistroTomaDao(db: AppDatabase) = db.registroTomaDao()
}
