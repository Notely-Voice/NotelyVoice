package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults.elevation
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.module.notelycompose.export.presentation.ExportSelectionViewModel
import com.module.notelycompose.export.ui.ExportSelectedItemConfirmationDialog
import com.module.notelycompose.export.ui.NoSelectionErrorDialog
import com.module.notelycompose.notes.presentation.list.NoteListIntent
import com.module.notelycompose.notes.presentation.list.NoteListViewModel
import com.module.notelycompose.notes.ui.share.ShareDialog
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.platform.presentation.PlatformUiState
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.cancel
import com.module.notelycompose.resources.export
import com.module.notelycompose.resources.ic_cancel_all
import com.module.notelycompose.resources.note_list_add_note
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.module.notelycompose.resources.ic_export_selections
import org.jetbrains.compose.resources.painterResource

@Composable
fun NoteListScreen(
    navigateToSettings: () -> Unit,
    navigateToMenu: () -> Unit,
    navigateToNoteDetails: (String) -> Unit,
    navigateToExportNotes: () -> Unit,
    viewModel: NoteListViewModel = koinViewModel(),
    exportViewModel: ExportSelectionViewModel = koinViewModel(),
    platformUiState: PlatformUiState
) {
    val notesListState by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    var isSelectAllAction by remember { mutableStateOf(false) }
    var showExportNotesConfirmDialog by remember { mutableStateOf(false) }
    var showNoSelectionDialog by remember { mutableStateOf(false) }
    var isEmptySelection by remember { mutableStateOf(false) }

    Scaffold(
            topBar = {
                TopBar(
                    onMenuClicked = {
                       navigateToMenu()
                    },
                    onSettingsClicked = {
                      navigateToSettings()
                    }
                )
            },
            isFloatingActionButtonDocked = true,
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if(isSelectAllAction) {
                            // call function depending what was chosen
                            if(isEmptySelection) {
                                showNoSelectionDialog = true
                            } else {
                                showExportNotesConfirmDialog = true
                            }
                        } else {
                            navigateToNoteDetails("0")
                        }

                    },
                    backgroundColor = LocalCustomColors.current.backgroundViewColor,
                    elevation = elevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        if(isSelectAllAction) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_export_selections),
                                contentDescription = stringResource(Res.string.export),
                                tint = LocalCustomColors.current.floatActionButtonIconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(Res.string.note_list_add_note),
                                tint = LocalCustomColors.current.floatActionButtonIconColor
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(LocalCustomColors.current.bodyBackgroundColor)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    }
            ) {
                SearchBar(
                    onSearchByKeyword = { keyword ->
                        viewModel.onProcessIntent(NoteListIntent.OnSearchNote(keyword))
                    }
                )
                FilterTabBar(
                    selectedTabIndex = notesListState.selectedTabIndex,
                    onFilterTabItemClicked = { titleIndex ->
                        viewModel.onProcessIntent(NoteListIntent.OnFilterNote(titleIndex))
                    },
                    allSizeStr = notesListState.allNotesSizeStr
                )
                NoteList(
                    noteList = viewModel.onGetUiState(notesListState),
                    onNoteClicked = { id ->
                        navigateToNoteDetails("$id")
                    },
                    onNoteDeleteClicked = {
                        viewModel.onProcessIntent(NoteListIntent.OnNoteDeleted(it))
                    },
                    isSelectAllAction = isSelectAllAction,
                    onCancelSelectionAction = {
                        isSelectAllAction = !isSelectAllAction
                    },
                    onUpdateSelection = { selectionIds ->
                        isEmptySelection = selectionIds.isEmpty()
                        exportViewModel.onUpdateNoteIds(selectionIds)
                    }
                )
                if(notesListState.showEmptyContent) EmptyNoteUi(platformUiState.isTablet)
            }
        }

    ExportSelectedItemConfirmationDialog(
        showExportNotesConfirmDialog = showExportNotesConfirmDialog,
        onExport = { exportAudio, exportTxt ->

            exportViewModel.onUpdateExportOptions(
                exportAudio,
                exportTxt
            )
            navigateToExportNotes()
        },
        onDismiss = {
            showExportNotesConfirmDialog = false
        }
    )

    NoSelectionErrorDialog(
        showDialog = showNoSelectionDialog,
        onDismiss = { showNoSelectionDialog = false }
    )

}
