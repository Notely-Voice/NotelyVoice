package com.module.notelycompose.transcription

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.presentation.detail.TextEditorViewModel
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.platform.HandlePlatformBackNavigation
import com.module.notelycompose.platform.getPlatform
import com.module.notelycompose.resources.vectors.IcChevronLeft
import com.module.notelycompose.resources.vectors.Images
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.top_bar_back
import com.module.notelycompose.resources.transcription_dialog_append
import com.module.notelycompose.resources.transcription_dialog_original
import com.module.notelycompose.resources.transcription_dialog_summarize
import com.module.notelycompose.resources.transcription_dialog_error_got_it
import com.module.notelycompose.resources.transcription_dialog_error_audio_file_title
import com.module.notelycompose.resources.transcription_dialog_error_audio_file_desc
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TranscriptionScreen(
    navigateBack: () -> Unit,
    viewModel: TranscriptionViewModel = koinViewModel(),
    editorViewModel: TextEditorViewModel
) {

    val scrollState = rememberScrollState()
    val transcriptionUiState by viewModel.uiState.collectAsState()
    val editorState by editorViewModel.editorPresentationState.collectAsState()


    LaunchedEffect(transcriptionUiState.originalText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    DisposableEffect(Unit) {
        viewModel.requestAudioPermission()
        viewModel.initRecognizer()
        // Transcribe the recording selected in the editor, or the primary one by default
        viewModel.startRecognizer(editorViewModel.getRecordingPathForTranscription())
        onDispose {
            viewModel.stopRecognizer()
            viewModel.finishRecognizer()
        }
    }
        Card(
            backgroundColor = LocalCustomColors.current.bodyBackgroundColor,
            elevation = 0.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 48.dp)
            ) {
                Box(modifier = Modifier.align(Alignment.Start)
                    .padding(start = 4.dp, bottom = 12.dp, top = 4.dp)) {
                    BackButton(onNavigateBack = {
                        viewModel.stopRecognizer()
                        viewModel.finishRecognizer()
                        editorViewModel.onSelectRecordingForTranscription(null)
                        navigateBack()
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .border(
                            2.dp,
                            LocalCustomColors.current.bodyContentColor,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = if(transcriptionUiState.viewOriginalText) transcriptionUiState.originalText else transcriptionUiState.summarizedText,
                        color = LocalCustomColors.current.bodyContentColor,
                        style = TextStyle(fontSize = editorState.bodyTextSize.sp)
                    )
                }
                if(transcriptionUiState.progress == 0){
                    LinearProgressIndicator(
                        modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                        strokeCap = StrokeCap.Round
                    )
                } else if(transcriptionUiState.progress in 1..99){
                   SmoothLinearProgressBar((transcriptionUiState.progress / 100f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = !transcriptionUiState.inTranscription,
                        border = BorderStroke(
                            width = 2.dp,
                            color = if(!transcriptionUiState.inTranscription) {
                                LocalCustomColors.current.bodyContentColor
                            } else {
                                LocalCustomColors.current.bodyContentColor.copy(alpha = 0.38f)
                            }
                        ),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = LocalCustomColors.current.bodyContentColor,
                            disabledContentColor = LocalCustomColors.current.bodyContentColor.copy(alpha = 0.38f)
                        ),
                        content = {
                            Text(
                                stringResource(Res.string.transcription_dialog_append)
                            )
                        },
                        onClick = {
                            val result = if (transcriptionUiState.viewOriginalText) transcriptionUiState.originalText else transcriptionUiState.summarizedText
                            val selectedRecordingId = editorViewModel.getSelectedRecordingIdForTranscription()
                            if (selectedRecordingId != null) {
                                // Bind the transcription to the recording it was made from
                                editorViewModel.onUpdateRecordingTranscription(selectedRecordingId, result)
                            } else {
                                editorViewModel.onUpdateContent(TextFieldValue("${editorState.content.text}\n$result"))
                            }
                            editorViewModel.onSelectRecordingForTranscription(null)
                            navigateBack()
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = !transcriptionUiState.inTranscription,
                        border = BorderStroke(
                            width = 2.dp,
                            color = if(!transcriptionUiState.inTranscription) {
                                LocalCustomColors.current.bodyContentColor
                            } else {
                                LocalCustomColors.current.bodyContentColor.copy(alpha = 0.38f)
                            }
                        ),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = LocalCustomColors.current.bodyContentColor,
                            disabledContentColor = LocalCustomColors.current.bodyContentColor.copy(alpha = 0.38f)
                        ),
                        content = {
                            Text(
                                if(transcriptionUiState.viewOriginalText) stringResource(Res.string.transcription_dialog_summarize) else
                                    stringResource(Res.string.transcription_dialog_original),
                                fontSize = 12.sp
                            )
                        }, onClick = {
                            viewModel.summarize()
                        })
                }

            }
        }

    HandlePlatformBackNavigation(enabled = true) {
        editorViewModel.onSelectRecordingForTranscription(null)
        navigateBack()
    }

    if(transcriptionUiState.hasError) {
        AlertDialog(
            onDismissRequest = navigateBack,
            confirmButton = {
                TextButton(onClick = navigateBack) {
                    Text(stringResource(Res.string.transcription_dialog_error_got_it))
                }
            },
            title = { Text(stringResource(Res.string.transcription_dialog_error_audio_file_title)) },
            text = { Text(stringResource(Res.string.transcription_dialog_error_audio_file_desc)) }
        )
    }

}
@Composable
fun BackButton(
    onNavigateBack: () -> Unit
) {
    if (getPlatform().isAndroid) {
        IconButton(
            onClick = onNavigateBack,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(Res.string.top_bar_back),
                tint = LocalCustomColors.current.bodyContentColor
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onNavigateBack() }
        ) {
            androidx.compose.material.Icon(
                imageVector = Images.Icons.IcChevronLeft,
                contentDescription = stringResource(Res.string.top_bar_back),
                modifier = Modifier.size(28.dp),
                tint = LocalCustomColors.current.bodyContentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.material.Text(
                text = stringResource(Res.string.top_bar_back),
                style = androidx.compose.material.MaterialTheme.typography.body1,
                color = LocalCustomColors.current.bodyContentColor
            )
        }
    }
}


@Composable
fun SmoothLinearProgressBar(progress: Float) {
    // Animate the progress value for smooth transitions
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500) // Adjust duration as needed
    )

    LinearProgressIndicator(
        progress,
        modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
        strokeCap = StrokeCap.Round
    )
}
