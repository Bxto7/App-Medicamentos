package com.medicamentos.app.medremind.di

import com.medicamentos.app.medremind.data.repository.AuthRepositoryImpl
import com.medicamentos.app.medremind.data.repository.MedicamentoRepositoryImpl
import com.medicamentos.app.medremind.data.repository.PacienteRepositoryImpl
import com.medicamentos.app.medremind.data.repository.TomaRepositoryImpl
import com.medicamentos.app.medremind.data.repository.TratamientoRepositoryImpl
import com.medicamentos.app.medremind.domain.repository.AuthRepository
import com.medicamentos.app.medremind.domain.repository.MedicamentoRepository
import com.medicamentos.app.medremind.domain.repository.PacienteRepository
import com.medicamentos.app.medremind.domain.repository.TomaRepository
import com.medicamentos.app.medremind.domain.repository.TratamientoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindPacienteRepository(impl: PacienteRepositoryImpl): PacienteRepository
    @Binds @Singleton abstract fun bindMedicamentoRepository(impl: MedicamentoRepositoryImpl): MedicamentoRepository
    @Binds @Singleton abstract fun bindTratamientoRepository(impl: TratamientoRepositoryImpl): TratamientoRepository
    @Binds @Singleton abstract fun bindTomaRepository(impl: TomaRepositoryImpl): TomaRepository
}
