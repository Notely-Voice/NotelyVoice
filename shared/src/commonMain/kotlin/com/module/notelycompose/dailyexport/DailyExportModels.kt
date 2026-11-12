package com.module.notelycompose.dailyexport

enum class DailyExportScope {
    ALL,
    CREATED_TODAY,
    CHANGED_SINCE_LAST_EXPORT
}

data class DailyExportSettings(
    val enabled: Boolean = false,
    val folderUri: String? = null,
    val hour: Int = 23,
    val minute: Int = 59,
    val scope: DailyExportScope = DailyExportScope.CHANGED_SINCE_LAST_EXPORT,
    val keepVersions: Boolean = false,
    val lastRunAtMillis: Long? = null,
    val lastRunMessage: String? = null
)
