package com.module.notelycompose.dailyexport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DailyExportViewModel(
    private val repository: DailyExportRepository,
    private val scheduler: DailyExportScheduler
) : ViewModel() {

    val settings: StateFlow<DailyExportSettings> = repository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyExportSettings())

    fun onChooseFolder() {
        scheduler.pickFolder { uri ->
            if (uri != null) viewModelScope.launch { repository.setFolderUri(uri) }
        }
    }

    fun onEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.currentSettings()
            if (!enabled) {
                repository.setEnabled(false)
                scheduler.cancel()
                return@launch
            }
            if (current.folderUri == null) {
                // A folder is needed first. Enable once the user has picked one.
                scheduler.pickFolder { uri ->
                    if (uri != null) viewModelScope.launch {
                        repository.setFolderUri(uri)
                        repository.setEnabled(true)
                        scheduler.schedule(current.hour, current.minute)
                    }
                }
            } else {
                repository.setEnabled(true)
                scheduler.schedule(current.hour, current.minute)
            }
        }
    }

    fun onTimeChange(hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.setTime(hour.mod(24), minute.mod(60))
            if (repository.currentSettings().enabled) scheduler.schedule(hour.mod(24), minute.mod(60))
        }
    }

    fun onScopeChange(scope: DailyExportScope) {
        viewModelScope.launch { repository.setScope(scope) }
    }

    fun onKeepVersionsChange(keep: Boolean) {
        viewModelScope.launch { repository.setKeepVersions(keep) }
    }

    fun onExportNow() {
        scheduler.runNow()
    }
}
