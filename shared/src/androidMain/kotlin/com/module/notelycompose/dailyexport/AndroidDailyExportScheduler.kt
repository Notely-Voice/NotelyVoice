package com.module.notelycompose.dailyexport

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.module.notelycompose.FolderPickerHandler
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private const val PERIODIC_WORK_NAME = "daily_export_periodic"
private const val ONE_TIME_WORK_NAME = "daily_export_now"

class AndroidDailyExportScheduler(
    private val context: Context,
    private val folderPickerHandler: FolderPickerHandler
) : DailyExportScheduler {

    override val isSupported: Boolean = true

    private val workManager get() = WorkManager.getInstance(context)

    override fun schedule(hour: Int, minute: Int) {
        val request = PeriodicWorkRequestBuilder<DailyExportWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntilNext(hour, minute).toMillis(), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    override fun cancel() {
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
    }

    override fun runNow() {
        val request = OneTimeWorkRequestBuilder<DailyExportWorker>().build()
        workManager.enqueueUniqueWork(ONE_TIME_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    override fun pickFolder(onPicked: (String?) -> Unit) {
        folderPickerHandler.pickFolder { uri ->
            if (uri == null) {
                onPicked(null)
                return@pickFolder
            }
            // Keep access after the app restarts so the background job can still write.
            val persisted = runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }.isSuccess
            onPicked(if (persisted) uri.toString() else null)
        }
    }

    private fun delayUntilNext(hour: Int, minute: Int): Duration {
        val now = LocalDateTime.now()
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next)
    }
}
