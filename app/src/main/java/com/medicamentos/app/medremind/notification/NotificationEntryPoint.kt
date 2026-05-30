package com.medicamentos.app.medremind.notification

import com.medicamentos.app.medremind.data.local.dao.RegistroTomaDao
import com.medicamentos.app.medremind.data.local.dao.TomaProgramadaDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationEntryPoint {
    fun tomaProgramadaDao(): TomaProgramadaDao
    fun registroTomaDao(): RegistroTomaDao
    fun notificationScheduler(): NotificationScheduler
}
