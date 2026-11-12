package com.module.notelycompose.dailyexport

/**
 * Platform entry point for the daily export. Android schedules a background job,
 * other platforms bind a no-op implementation and report [isSupported] as false.
 */
interface DailyExportScheduler {
    val isSupported: Boolean

    fun schedule(hour: Int, minute: Int)

    fun cancel()

    fun runNow()

    /** Lets the user choose the export folder. Calls back with a persisted folder uri, or null if cancelled. */
    fun pickFolder(onPicked: (String?) -> Unit)
}
