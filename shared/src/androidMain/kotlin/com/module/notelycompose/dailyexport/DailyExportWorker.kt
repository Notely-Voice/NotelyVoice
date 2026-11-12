package com.module.notelycompose.dailyexport

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DailyExportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val runner: DailyExportRunner by inject()

    override suspend fun doWork(): Result {
        return runner.run().fold(
            onSuccess = { Result.success() },
            // The failure message is stored for the settings screen. The next daily run tries again.
            onFailure = { Result.failure() }
        )
    }
}
