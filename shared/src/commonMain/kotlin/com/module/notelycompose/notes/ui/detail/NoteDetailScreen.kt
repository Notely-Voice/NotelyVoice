package com.module.notelycompose.notes.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults.elevation
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.module.notelycompose.audio.presentation.AudioPlayerViewModel
import com.module.notelycompose.audio.ui.player.PlatformAudioPlayerUi
import com.module.notelycompose.audio.ui.player.model.AudioPlayerUiState
import com.module.notelycompose.modelDownloader.DownloaderDialog
import com.module.notelycompose.modelDownloader.DownloaderEffect
import com.module.notelycompose.modelDownloader.ModelDownloaderViewModel
import com.module.notelycompose.audio.presentation.AudioImportViewModel
import com.module.notelycompose.audio.ui.importing.ImportingAudioStateHost
import com.module.notelycompose.modelDownloader.ModelSelection
import com.module.notelycompose.notes.presentation.detail.ImportingPhotosState
import com.module.notelycompose.notes.presentation.detail.PhotoImportViewModel
import com.module.notelycompose.notes.presentation.detail.TextEditorViewModel
import com.module.notelycompose.notes.ui.share.ShareDialog
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.platform.presentation.PlatformViewModel
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.confirmation_cancel
import com.module.notelycompose.resources.download_dialog_error
import com.module.notelycompose.resources.ic_transcription
import com.module.notelycompose.resources.note_detail_recorder
import com.module.notelycompose.resources.transcription_icon
import com.module.notelycompose.resources.vectors.IcRecorder
import com.module.notelycompose.resources.vectors.Images
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    navigateBack: () -> Unit,
    navigateToRecorder: (noteId: String) -> Unit,
    navigateToTranscription: () -> Unit,
    onNavigateToSettingsText: () -> Unit,
    audioPlayerViewModel: AudioPlayerViewModel = koinViewModel(),
    downloaderViewModel: ModelDownloaderViewModel = koinViewModel(),
    platformViewModel: PlatformViewModel = koinViewModel(),
    audioImportViewModel: AudioImportViewModel = koinViewModel(),
    photoImportViewModel: PhotoImportViewModel = koinViewModel(),
    editorViewModel: TextEditorViewModel,
    modelSelection: ModelSelection = koinInject()
) {
    val currentNoteId by editorViewModel.currentNoteId.collectAsStateWithLifecycle()
    val importingState by audioImportViewModel.importingAudioState.collectAsStateWithLifecycle()
    val importingPhotosState by photoImportViewModel.importingPhotosState.collectAsStateWithLifecycle()
    val downloaderUiState by downloaderViewModel.uiState.collectAsStateWithLifecycle()
    val editorState = editorViewModel.editorPresentationState.collectAsStateWithLifecycle().value
        .let { editorViewModel.onGetUiState(it) }

    val audioPlayerUiState = audioPlayerViewModel.uiState.collectAsStateWithLifecycle().value
        .let { audioPlayerViewModel.onGetUiState(it) }
    val platformState by platformViewModel.state.collectAsStateWithLifecycle()

    val hasAnyRecording = editorState.recordings.isNotEmpty() ||
            editorState.recording.isRecordingExist

    var showFormatBar by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var isTextFieldFocused by remember { mutableStateOf(false) }
    var showDownloadQuestionDialog by remember { mutableStateOf(false) }
    var showCopiedTooltip by remember { mutableStateOf(false) }
    var isFabVisible by remember { mutableStateOf(true) }

    LaunchedEffect(importingPhotosState) {
        when (val state = importingPhotosState) {
            is ImportingPhotosState.Success -> {
                editorViewModel.onAddPhotos(state.paths)
                photoImportViewModel.releaseState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        if (noteId.toLong() > 0L) {
            editorViewModel.onGetNoteById(noteId)
        }
        downloaderViewModel.effects.collect {
            when (it) {
                is DownloaderEffect.DownloadEffect -> {
                    showDownloadDialog = true
                    showDownloadQuestionDialog = false
                    showLoadingDialog = false
                }

                is DownloaderEffect.ErrorEffect -> {
                    showDownloadDialog = false
                    showErrorDialog = true
                    showLoadingDialog = false
                }

                is DownloaderEffect.ModelsAreReady -> {
                    showDownloadDialog = false
                    showLoadingDialog = false
                    navigateToTranscription()
                }

                is DownloaderEffect.AskForUserAcceptance -> {
                    showDownloadQuestionDialog = true
                    showLoadingDialog = false
                }

                is DownloaderEffect.CheckingEffect -> {
                    showLoadingDialog = true
                    showDownloadDialog = false
                }
            }
        }
    }
    Scaffold(
        topBar = {
            DetailNoteTopBar(
                onNavigateBack = navigateBack,
                onShare = {
                    showShareDialog = true
                },
                onCopy = {
                    platformViewModel.onCopy(editorState.content.text)
                },
                onExportAudio = {
                    platformViewModel.onExportAudio(editorState.recording.recordingPath)
                },
                onImportClick = {
                    audioPlayerViewModel.releasePlayer()
                    audioImportViewModel.importAudio()
                },
                onImportVideoClick = {
                    audioPlayerViewModel.releasePlayer()
                    audioImportViewModel.importVideo()
                },
                onExportTextAsTxt = {
                    platformViewModel.onExportTextAsTxt(editorState.content.text)
                },
                onExportTextAsPDF = {
                    platformViewModel.onExportTextAsPDF(editorState.content.text)
                }
            )
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (hasAnyRecording) {
                    AnimatedVisibility(
                        visible = isFabVisible,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                    ) {
                        FloatingActionButton(
                            modifier = Modifier.border(
                                width = 1.dp,
                                color = LocalCustomColors.current.floatActionButtonBorderColor,
                                shape = CircleShape
                            ),
                            backgroundColor = LocalCustomColors.current.bodyBackgroundColor,
                            onClick = {
                                // Transcribe the primary recording (legacy behaviour)
                                editorViewModel.onSelectRecordingForTranscription(null)
                                downloaderViewModel.checkTranscriptionAvailability()
                            },
                            elevation = elevation(defaultElevation = 2.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_transcription),
                                contentDescription = stringResource(Res.string.transcription_icon),
                                tint = LocalCustomColors.current.bodyContentColor
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isFabVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    FloatingActionButton(
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = LocalCustomColors.current.floatActionButtonBorderColor,
                            shape = CircleShape
                        ),
                        backgroundColor = LocalCustomColors.current.bodyBackgroundColor,
                        onClick = {
                            // A note can hold several photos: pick one or more images
                            photoImportViewModel.importPhotos()
                        },
                        elevation = elevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add photos",
                            tint = LocalCustomColors.current.bodyContentColor
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isFabVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    FloatingActionButton(
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = LocalCustomColors.current.floatActionButtonBorderColor,
                            shape = CircleShape
                        ),
                        backgroundColor = LocalCustomColors.current.bodyBackgroundColor,
                        onClick = {
                            // A note can hold several recordings: always add a new one
                            navigateToRecorder("$currentNoteId")
                        },
                        elevation = elevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Images.Icons.IcRecorder,
                            contentDescription = stringResource(Res.string.note_detail_recorder),
                            tint = LocalCustomColors.current.bodyContentColor
                        )
                    }
                }

            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            BottomNavigationBar(
                isTextFieldFocused = isTextFieldFocused,
                selectionSize = editorState.selectionSize,
                isStarred = editorState.isStarred,
                showFormatBar = showFormatBar,
                textFieldFocusRequester = focusRequester,
                onShowTextFormatBar = { showFormatBar = it },
                editorViewModel = editorViewModel,
                navigateBack = navigateBack,
                onNavigateToSettingsText = onNavigateToSettingsText
            )
        }
    ) { paddingValues ->

        NoteContent(
            paddingValues = paddingValues,
            newNoteDateString = editorState.createdAt,
            editorState = editorState,
            showFormatBar = showFormatBar,
            focusRequester = focusRequester,
            audioPlayerUiState = audioPlayerUiState,
            textEditorViewModel = editorViewModel,
            audioPlayerViewModel = audioPlayerViewModel,
            onTranscribeRecording = { recording ->
                editorViewModel.onSelectRecordingForTranscription(recording.id)
                downloaderViewModel.checkTranscriptionAvailability()
            },
            onFocusChange = {
                isTextFieldFocused = it
            },
            onFabVisibility = { isFabVisible = it }
        )
    }


    if (showDownloadDialog) {
        LocalSoftwareKeyboardController.current?.hide()
        DownloaderDialog(
            transcriptionModel = downloaderUiState.selectedModel,
            downloaderUiState,
            onDismiss = { showDownloadDialog = false }
        )
    }

    if (showErrorDialog) {
        LocalSoftwareKeyboardController.current?.hide()
        AlertDialog(
            modifier = Modifier.height(120.dp),
            title = { Text(stringResource(resource = Res.string.download_dialog_error)) },
            onDismissRequest = { showErrorDialog = false },
            buttons = {
                Button(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp),
                    onClick = {
                        showErrorDialog = false
                    },
                ) { Text(stringResource(resource = Res.string.confirmation_cancel)) }
            }
        )
    }
    if (showDownloadQuestionDialog) {
        LocalSoftwareKeyboardController.current?.hide()
        DownloadModelDialog(
            onDownload = {
                downloaderViewModel.startDownload()
                showDownloadQuestionDialog = false
            },
            onCancel = {
                showDownloadQuestionDialog = false
            },
            transcriptionModel = downloaderUiState.selectedModel
        )
    }


    if (showLoadingDialog) {
        PreparingLoadingDialog()
    }

    if (showShareDialog) {
        ShareDialog(
            onShareAudioRecording = {
                platformViewModel.shareRecording(editorState.recording.recordingPath)
            },
            onShareTexts = {
                platformViewModel.shareText(editorState.content.text)
            },
            onDismiss = { showShareDialog = false }
        )
    }

    ImportingAudioStateHost(
        state = importingState,
        onSuccess = editorViewModel::onUpdateRecordingPath,
        onRelease = audioImportViewModel::releaseState
    )

    LaunchedEffect(platformState.copySuccess, showCopiedTooltip) {
        if (platformState.copySuccess == true) {
            showCopiedTooltip = true
        }
    }

    CopiedNotification(
        visible = showCopiedTooltip,
        onDismiss = {
            showCopiedTooltip = false
            platformViewModel.onClearCopyState()
        },
        modifier = Modifier
            .padding(bottom = 52.dp)
    )
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NoteContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    newNoteDateString: String,
    editorState: EditorUiState,
    showFormatBar: Boolean,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
    audioPlayerUiState: AudioPlayerUiState,
    textEditorViewModel: TextEditorViewModel,
    audioPlayerViewModel: AudioPlayerViewModel,
    onTranscribeRecording: (RecordingUiModel) -> Unit,
    onFabVisibility: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var recordingToDelete by remember { mutableStateOf<RecordingUiModel?>(null) }
    var playingRecordingId by remember { mutableStateOf<Long?>(null) }
    var photoToDelete by remember { mutableStateOf<PhotoUiModel?>(null) }
    var fullscreenPhotoIndex by remember { mutableStateOf<Int?>(null) }
    var showDeleteLegacyRecordingDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    LaunchedEffect(editorState.content) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(scrollState)
            .background(LocalCustomColors.current.bodyBackgroundColor)
            .imePadding()
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            DateHeader(newNoteDateString)

            if (editorState.photos.isNotEmpty()) {
                PhotosRow(
                    photos = editorState.photos,
                    onPhotoClick = { fullscreenPhotoIndex = editorState.photos.indexOf(it).coerceAtLeast(0) },
                    onDeleteRequest = { photoToDelete = it }
                )
            }

            if (editorState.recordings.isNotEmpty()) {
                editorState.recordings.forEach { recording ->
                    key(recording.id) {
                        RecordingItem(
                            recording = recording,
                            playingRecordingId = playingRecordingId,
                            onPlaybackStarted = { playingRecordingId = it },
                            isDeletePending = recordingToDelete?.id == recording.id,
                            onTranscribeRequest = { onTranscribeRecording(recording) },
                            onDeleteRequest = { recordingToDelete = recording }
                        )
                    }
                }
            } else if (editorState.recording.isRecordingExist) {
                // Legacy note with a single recording that has not been migrated yet
                val dismissState = rememberDismissState()
                if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                    LaunchedEffect(Unit) {
                        showDeleteLegacyRecordingDialog = true
                    }
                }

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    background = {
                        Box(
                            modifier = Modifier
                                .width(800.dp)
                                .height(36.dp)
                                .padding(horizontal = 16.dp, vertical = 0.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Red),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    },
                    dismissContent = {
                        PlatformAudioPlayerUi(
                            filePath = editorState.recording.recordingPath,
                            uiState = audioPlayerUiState,
                            onLoadAudio = audioPlayerViewModel::onLoadAudio,
                            onClear = audioPlayerViewModel::onClear,
                            onSeekTo = audioPlayerViewModel::onSeekTo,
                            onTogglePlayPause = audioPlayerViewModel::onTogglePlayPause
                        )
                    }
                )

                if (showDeleteLegacyRecordingDialog) {
                    DeleteRecordingConfirmationDialog(
                        showDialog = true,
                        onDismiss = {
                            showDeleteLegacyRecordingDialog = false
                            coroutineScope.launch {
                                dismissState.reset()
                            }
                        },
                        onConfirm = {
                            textEditorViewModel.onDeleteRecord()
                        }
                    )
                }
            }

            NoteEditor(
                modifier = Modifier.fillMaxWidth().weight(1f),
                editorState = editorState,
                showFormatBar = showFormatBar,
                focusRequester = focusRequester,
                onFocusChange = onFocusChange,
                textEditorViewModel = textEditorViewModel,
                onFabVisibility = onFabVisibility
            )
        }
    }

    recordingToDelete?.let { recording ->
        DeleteRecordingConfirmationDialog(
            showDialog = true,
            onDismiss = {
                recordingToDelete = null
            },
            onConfirm = {
                textEditorViewModel.onDeleteRecording(recording.id)
                recordingToDelete = null
            }
        )
    }

    photoToDelete?.let { photo ->
        DeleteRecordingConfirmationDialog(
            showDialog = true,
            onDismiss = {
                photoToDelete = null
            },
            onConfirm = {
                textEditorViewModel.onDeletePhoto(photo.id)
                photoToDelete = null
            }
        )
    }

    fullscreenPhotoIndex?.let { index ->
        if (editorState.photos.isEmpty()) {
            fullscreenPhotoIndex = null
        } else {
            FullscreenPhotoViewer(
                photos = editorState.photos,
                initialIndex = index.coerceIn(0, editorState.photos.lastIndex),
                onDeleteRequest = { photoToDelete = it },
                onClose = { fullscreenPhotoIndex = null }
            )
        }
    }
}

@Composable
private fun FullscreenPhotoViewer(
    photos: List<PhotoUiModel>,
    initialIndex: Int,
    onDeleteRequest: (PhotoUiModel) -> Unit,
    onClose: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { photos.size }
    val scope = rememberCoroutineScope()
    val scrim = Color.Black.copy(alpha = 0.55f)

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = photos[page].filePath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${photos.size}",
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(scrim)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        photos.getOrNull(pagerState.currentPage)?.let(onDeleteRequest)
                    },
                    modifier = Modifier.clip(CircleShape).background(scrim)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete photo",
                        tint = Color.Red
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.clip(CircleShape).background(scrim)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            if (pagerState.currentPage > 0) {
                IconButton(
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(scrim)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous photo",
                        tint = Color.White
                    )
                }
            }
            if (pagerState.currentPage < photos.lastIndex) {
                IconButton(
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(scrim)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next photo",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotosRow(
    photos: List<PhotoUiModel>,
    onPhotoClick: (PhotoUiModel) -> Unit,
    onDeleteRequest: (PhotoUiModel) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photos, key = { it.id }) { photo ->
            AsyncImage(
                model = photo.filePath,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .combinedClickable(
                        onClick = { onPhotoClick(photo) },
                        // Long press is a shortcut to delete, the viewer has a delete button too
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDeleteRequest(photo)
                        }
                    ),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun RecordingItem(
    recording: RecordingUiModel,
    playingRecordingId: Long?,
    onPlaybackStarted: (Long) -> Unit,
    isDeletePending: Boolean,
    onTranscribeRequest: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Every recording has its own player, so durations and positions don't affect each other
        val playerViewModel: AudioPlayerViewModel = koinViewModel(key = "recording-player-${recording.id}")
        val audioPlayerUiState = playerViewModel.uiState.collectAsStateWithLifecycle().value
            .let { playerViewModel.onGetUiState(it) }
        LaunchedEffect(audioPlayerUiState.isPlaying) {
            if (audioPlayerUiState.isPlaying) onPlaybackStarted(recording.id)
        }
        // Another recording started playing: pause this one
        LaunchedEffect(playingRecordingId) {
            if (playingRecordingId != recording.id && audioPlayerUiState.isPlaying) {
                playerViewModel.onTogglePlayPause()
            }
        }

        val dismissState = rememberDismissState()
        if (dismissState.isDismissed(DismissDirection.EndToStart)) {
            LaunchedEffect(Unit) {
                onDeleteRequest()
            }
        }
        // Snap the row back when the confirmation dialog is cancelled
        LaunchedEffect(isDeletePending) {
            if (!isDeletePending) {
                dismissState.reset()
            }
        }

        SwipeToDismiss(
            state = dismissState,
            directions = setOf(DismissDirection.EndToStart),
            background = {
                Box(
                    modifier = Modifier
                        .width(800.dp)
                        .height(36.dp)
                        .padding(horizontal = 16.dp, vertical = 0.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Red),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                }
            },
            dismissContent = {
                PlatformAudioPlayerUi(
                    filePath = recording.filePath,
                    uiState = audioPlayerUiState,
                    onLoadAudio = playerViewModel::onLoadAudio,
                    onClear = playerViewModel::onClear,
                    onSeekTo = playerViewModel::onSeekTo,
                    onTogglePlayPause = playerViewModel::onTogglePlayPause
                )
            }
        )

        IconButton(
            onClick = onTranscribeRequest,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.End)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_transcription),
                contentDescription = stringResource(Res.string.transcription_icon),
                tint = LocalCustomColors.current.bodyContentColor
            )
        }

        if (recording.transcription.isNotBlank()) {
            Text(
                text = recording.transcription,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LocalCustomColors.current.bodyBackgroundColor)
                    .padding(12.dp),
                fontSize = 14.sp,
                color = LocalCustomColors.current.bodyContentColor
            )
        }
    }
}

@Composable
private fun DateHeader(dateString: String) {
    Text(
        text = dateString,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        fontSize = 12.sp,
        color = LocalCustomColors.current.bodyContentColor
    )
}

@Composable
private fun NoteEditor(
    modifier: Modifier = Modifier,
    editorState: EditorUiState,
    showFormatBar: Boolean,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
    textEditorViewModel: TextEditorViewModel,
    onFabVisibility: (Boolean) -> Unit
) {

    val scrollState = rememberScrollState()
    var previousScrollValue by remember { mutableStateOf(0) }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .collect { currentScroll ->
                val scrollingUp = currentScroll < previousScrollValue
                val result = scrollingUp || currentScroll == 0
                previousScrollValue = currentScroll
                onFabVisibility(result)
            }
    }

    val transformation = VisualTransformation { text ->
        TransformedText(
            buildAnnotatedString {
                append(text)
                editorState.formats.forEach { format ->
                    addStyle(
                        SpanStyle(
                            fontWeight = if (format.isBold) FontWeight.Bold else null,
                            fontStyle = if (format.isItalic) FontStyle.Italic else null,
                            textDecoration = if (format.isUnderline)
                                TextDecoration.Underline else null,
                            fontSize = format.textSize?.sp ?: TextUnit.Unspecified
                        ),
                        format.range.first.coerceIn(0, text.length),
                        format.range.last.coerceIn(0, text.length)
                    )
                }
            },
            OffsetMapping.Identity
        )
    }

    BasicTextField(
        value = editorState.content,
        onValueChange = textEditorViewModel::onUpdateContent,
        modifier =
            modifier
                .focusRequester(focusRequester)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
                .onFocusChanged {
                    onFocusChange(it.isFocused)
                },
        textStyle = TextStyle(
            color = LocalCustomColors.current.bodyContentColor,
            textAlign = editorState.textAlign,
            fontSize = editorState.bodyTextSize.sp
        ),
        cursorBrush = SolidColor(LocalCustomColors.current.bodyContentColor),
        readOnly = showFormatBar,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        ),
        visualTransformation = transformation
    )
}
