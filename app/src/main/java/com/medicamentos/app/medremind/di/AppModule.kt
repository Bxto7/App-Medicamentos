package com.medicamentos.app.medremind.di

import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.medicamentos.app.medremind.data.local.AppDatabase
import com.medicamentos.app.medremind.data.local.SessionManager
import com.medicamentos.app.medremind.data.local.SeedDataCallback
import com.medicamentos.app.medremind.data.repository.AsociacionRepositoryImpl
import com.medicamentos.app.medremind.data.repository.AuthRepositoryImpl
import com.medicamentos.app.medremind.data.repository.MedicamentoRepositoryImpl
import com.medicamentos.app.medremind.data.repository.PacienteRepositoryImpl
import com.medicamentos.app.medremind.data.repository.TomaRepositoryImpl
import com.medicamentos.app.medremind.data.repository.TratamientoRepositoryImpl
import com.medicamentos.app.medremind.domain.repository.AsociacionRepository
import com.medicamentos.app.medremind.domain.repository.AuthRepository
import com.medicamentos.app.medremind.domain.repository.MedicamentoRepository
import com.medicamentos.app.medremind.domain.repository.PacienteRepository
import com.medicamentos.app.medremind.domain.repository.TomaRepository
import com.medicamentos.app.medremind.domain.repository.TratamientoRepository
import com.medicamentos.app.medremind.domain.usecase.AgregarTratamientoUseCase
import com.medicamentos.app.medremind.domain.usecase.AsociarPacientesUseCase
import com.medicamentos.app.medremind.domain.usecase.LoginUseCase
import com.medicamentos.app.medremind.domain.usecase.LogoutUseCase
import com.medicamentos.app.medremind.domain.usecase.MarcarTomaUseCase
import com.medicamentos.app.medremind.domain.usecase.ObtenerHistorialUseCase
import com.medicamentos.app.medremind.domain.usecase.ObtenerMisTratamientosUseCase
import com.medicamentos.app.medremind.domain.usecase.ObtenerPacientesAsociablesUseCase
import com.medicamentos.app.medremind.domain.usecase.ObtenerPacientesUseCase
import com.medicamentos.app.medremind.domain.usecase.ObtenerTomasDelDiaUseCase
import com.medicamentos.app.medremind.domain.usecase.RegisterUseCase
import com.medicamentos.app.medremind.notification.CheckMissedDosesWorker
import com.medicamentos.app.medremind.notification.NotificationHelper
import com.medicamentos.app.medremind.notification.NotificationScheduler
import com.medicamentos.app.medremind.presentation.admin.AddTreatmentViewModel
import com.medicamentos.app.medremind.presentation.admin.AdminViewModel
import com.medicamentos.app.medremind.presentation.auth.AuthViewModel
import com.medicamentos.app.medremind.presentation.patient.PatientHomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.medicamentos.app.medremind.security.DatabaseKeyManager
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
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
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
            .addMigrations(AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().usuarioDao() }
    single { get<AppDatabase>().pacienteDao() }
    single { get<AppDatabase>().medicamentoDao() }
    single { get<AppDatabase>().tratamientoDao() }
    single { get<AppDatabase>().tomaProgramadaDao() }
    single { get<AppDatabase>().registroTomaDao() }
    single { get<AppDatabase>().medicoPacienteDao() }
    single { NotificationScheduler(androidContext(), get()) }
}

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), get()) }
    single<PacienteRepository> { PacienteRepositoryImpl(get(), get()) }
    single<AsociacionRepository> { AsociacionRepositoryImpl(get(), get()) }
    single<MedicamentoRepository> { MedicamentoRepositoryImpl(get(), get()) }
    single<TratamientoRepository> { TratamientoRepositoryImpl(get(), get(), get()) }
    single<TomaRepository> { TomaRepositoryImpl(get(), get(), get()) }
}

val useCaseModule = module {
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { ObtenerPacientesUseCase(get()) }
    factory { ObtenerPacientesAsociablesUseCase(get()) }
    factory { AsociarPacientesUseCase(get()) }
    factory { ObtenerTomasDelDiaUseCase(get()) }
    factory { MarcarTomaUseCase(get()) }
    factory { ObtenerHistorialUseCase(get()) }
    factory { ObtenerMisTratamientosUseCase(get()) }
    factory { AgregarTratamientoUseCase(get()) }
}

val viewModelModule = module {
    viewModel { AuthViewModel(get(), get(), get(), get()) }
    viewModel { PatientHomeViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AdminViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AddTreatmentViewModel(get(), get(), get(), get(), get()) }
}

val allModules = listOf(infraModule, databaseModule, repositoryModule, useCaseModule, viewModelModule)

fun scheduleCheckMissedDosesWorker(context: android.content.Context) {
    val request = PeriodicWorkRequestBuilder<CheckMissedDosesWorker>(1, TimeUnit.HOURS)
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        CheckMissedDosesWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}
