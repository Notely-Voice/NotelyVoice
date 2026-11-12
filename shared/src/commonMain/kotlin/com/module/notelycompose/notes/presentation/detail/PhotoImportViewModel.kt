package com.module.notelycompose.notes.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.FileManager
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ImportingPhotosState {
    data object Idle : ImportingPhotosState
    data object Importing : ImportingPhotosState
    data class Success(val paths: List<String>) : ImportingPhotosState
    data class Failure(val message: String) : ImportingPhotosState
}

class PhotoImportViewModel(
    private val fileManager: FileManager
) : ViewModel() {

    private val _importingPhotosState =
        MutableStateFlow<ImportingPhotosState>(ImportingPhotosState.Idle)
    val importingPhotosState: StateFlow<ImportingPhotosState> = _importingPhotosState

    fun importPhotos() = fileManager.launchPhotosPicker {
        viewModelScope.launch {
            _importingPhotosState.update { ImportingPhotosState.Importing }

            val paths = fileManager.processPickedPhotos()

            if (paths.isEmpty()) {
                _importingPhotosState.update {
                    ImportingPhotosState.Failure("Failed to import photos")
                }
                return@launch
            }

            Napier.d { "✅ Imported photos: $paths" }
            _importingPhotosState.update { ImportingPhotosState.Success(paths) }
        }
    }

    fun releaseState() {
        _importingPhotosState.update { ImportingPhotosState.Idle }
    }
}
