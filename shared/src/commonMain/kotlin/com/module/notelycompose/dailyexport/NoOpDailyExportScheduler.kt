package com.module.notelycompose.dailyexport

class NoOpDailyExportScheduler : DailyExportScheduler {
    override val isSupported: Boolean = false
    override fun schedule(hour: Int, minute: Int) = Unit
    override fun cancel() = Unit
    override fun runNow() = Unit
    override fun pickFolder(onPicked: (String?) -> Unit) = onPicked(null)
}
