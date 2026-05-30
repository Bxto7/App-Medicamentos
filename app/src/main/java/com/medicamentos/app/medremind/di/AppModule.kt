package com.medicamentos.app.medremind.di

import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.medicamentos.app.medremind.data.local.AppDatabase
import com.medicamentos.app.medremind.data.local.SessionManager
import com.medicamentos.app.medremind.data.local.SeedDataCallback
import com.medicamentos.app.medremind.data.repository.AuthRepositoryImpl
import com.medicamentos.app.medremind.data.repository.ContactoEmergenciaRepositoryImpl
import com.medicamentos.app.medremind.data.repository.GlucosaRepositoryImpl
import com.medicamentos.app.medremind.data.repository.MedicamentoRepositoryImpl
import com.medicamentos.app.medremind.data.repository.PacienteRepositoryImpl
import com.medicamentos.app.medremind.data.repository.TomaRepositoryImpl
import com.medicamentos.app.medremind.data.repository.TratamientoRepositoryImpl
import com.medicamentos.app.medremind.domain.repository.AuthRepository
import com.medicamentos.app.medremind.domain.repository.ContactoEmergenciaRepository
import com.medicamentos.app.medremind.domain.repository.GlucosaRepository
import com.medicamentos.app.medremind.domain.repository.MedicamentoRepository
import com.medicamentos.app.medremind.domain.repository.PacienteRepository
import com.medicamentos.app.medremind.domain.repository.TomaRepository
import com.medicamentos.app.medremind.domain.repository.TratamientoRepository
import com.medicamentos.app.medremind.domain.usecase.AgregarTratamientoUseCase
import com.medicamentos.app.medremind.domain.usecase.LoginUseCase
import com.medicamentos.app.medremind.domain.usecase.LogoutUseCase
import com.medicamentos.app.medremind.domain.usecase.MarcarTomaUseCase
import com.medicamentos.app.medremind.domain.usecase.ObtenerGlucosaUseCase
import com.medicamentos.app.medremind.domain.usecase.ObtenerHistorialUseCase
import com.medicamentos.app.medremind.domain.usecase.ObtenerPacientesUseCase
import com.medicamentos.app.medremind.domain.usecase.ObtenerTomasDelDiaUseCase
import com.medicamentos.app.medremind.domain.usecase.RegisterUseCase
import com.medicamentos.app.medremind.domain.usecase.RegistrarGlucosaUseCase
import com.medicamentos.app.medremind.notification.CheckMissedDosesWorker
import com.medicamentos.app.medremind.notification.NotificationHelper
import com.medicamentos.app.medremind.notification.NotificationScheduler
import com.medicamentos.app.medremind.presentation.admin.AddTreatmentViewModel
import com.medicamentos.app.medremind.presentation.admin.AdminViewModel
import com.medicamentos.app.medremind.presentation.auth.AuthViewModel
import com.medicamentos.app.medremind.presentation.patient.PatientHomeViewModel
import com.medicamentos.app.medremind.presentation.premium.GlucosaViewModel
import com.medicamentos.app.medremind.security.DatabaseKeyManager
import com.medicamentos.app.medremind.security.PremiumManager
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val infraModule = module {
    single { SessionManager(androidContext()) }
    single { NotificationHelper(androidContext()) }
    single { DatabaseKeyManager(androidContext()) }
    single { PremiumManager(androidContext()) }
}

val databaseModule = module {
    single<AppDatabase> {
        val keyManager: DatabaseKeyManager = get()
        val passphrase = SQLiteDatabase.getBytes(
            String(keyManager.getOrCreateKey()).toCharArray()
        )
        val factory = SupportFactory(passphrase)
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "medremind.db")
            .openHelperFactory(factory)
            .addCallback(SeedDataCallback())
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().usuarioDao() }
    single { get<AppDatabase>().pacienteDao() }
    single { get<AppDatabase>().medicamentoDao() }
    single { get<AppDatabase>().tratamientoDao() }
    single { get<AppDatabase>().tomaProgramadaDao() }
    single { get<AppDatabase>().registroTomaDao() }
    single { get<AppDatabase>().contactoEmergenciaDao() }
    single { get<AppDatabase>().glucosaDao() }
    single { NotificationScheduler(androidContext(), get()) }
}

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<PacienteRepository> { PacienteRepositoryImpl(get()) }
    single<MedicamentoRepository> { MedicamentoRepositoryImpl(get()) }
    single<TratamientoRepository> { TratamientoRepositoryImpl(get(), get()) }
    single<TomaRepository> { TomaRepositoryImpl(get(), get(), get()) }
    single<ContactoEmergenciaRepository> { ContactoEmergenciaRepositoryImpl(get()) }
    single<GlucosaRepository> { GlucosaRepositoryImpl(get()) }
}

val useCaseModule = module {
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { ObtenerPacientesUseCase(get()) }
    factory { ObtenerTomasDelDiaUseCase(get()) }
    factory { MarcarTomaUseCase(get()) }
    factory { ObtenerHistorialUseCase(get()) }
    factory { AgregarTratamientoUseCase(get()) }
    factory { RegistrarGlucosaUseCase(get()) }
    factory { ObtenerGlucosaUseCase(get()) }
}

val viewModelModule = module {
    viewModel { AuthViewModel(get(), get(), get(), get()) }
    viewModel { PatientHomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { AdminViewModel(get(), get(), get(), get(), get()) }
    viewModel { AddTreatmentViewModel(get(), get(), get()) }
    viewModel { GlucosaViewModel(get(), get(), get()) }
}

val allModules = listOf(infraModule, databaseModule, repositoryModule, useCaseModule, viewModelModule)

/**
 * Programa el CheckMissedDosesWorker para ejecutarse cada hora.
 */
fun scheduleCheckMissedDosesWorker(context: android.content.Context) {
    val request = PeriodicWorkRequestBuilder<CheckMissedDosesWorker>(1, TimeUnit.HOURS)
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        CheckMissedDosesWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}
