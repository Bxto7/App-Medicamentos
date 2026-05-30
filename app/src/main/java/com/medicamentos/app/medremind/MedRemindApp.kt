package com.medicamentos.app.medremind

import android.app.Application
import com.medicamentos.app.medremind.di.allModules
import com.medicamentos.app.medremind.di.scheduleCheckMissedDosesWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MedRemindApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MedRemindApp)
            modules(allModules)
        }
        // Iniciar worker de verificación de tomas omitidas
        scheduleCheckMissedDosesWorker(this)
    }
}
