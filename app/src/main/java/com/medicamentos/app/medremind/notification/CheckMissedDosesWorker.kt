package com.medicamentos.app.medremind.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class CheckMissedDosesWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = Result.success()

    companion object {
        const val WORK_NAME = "check_missed_doses"
    }
}
