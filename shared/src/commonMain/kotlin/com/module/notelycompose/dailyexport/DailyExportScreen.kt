package com.module.notelycompose.dailyexport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

private const val MINUTE_STEP = 5

@Composable
fun DailyExportScreen(
    navigateBack: () -> Unit,
    viewModel: DailyExportViewModel = koinViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val textColor = LocalCustomColors.current.bodyContentColor

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.bodyBackgroundColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
            }
            Text("Daily export", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = textColor)
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Export notes every day", color = textColor, fontSize = 18.sp)
                    Text(
                        "Saves each note as a text file in the folder you choose.",
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
                Switch(checked = settings.enabled, onCheckedChange = viewModel::onEnabledChange)
            }

            Section("Folder") {
                Text(settings.folderUri ?: "No folder selected", color = textColor, fontSize = 14.sp)
                OutlinedButton(onClick = viewModel::onChooseFolder) { Text("Choose folder") }
            }

            Section("Time") {
                Text(
                    "Runs around this time each day. Android may delay it slightly to save battery.",
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.onTimeChange(settings.hour - 1, settings.minute) }) { Text("-") }
                    Text(settings.hour.toString().padStart(2, '0'), color = textColor, fontSize = 20.sp)
                    OutlinedButton(onClick = { viewModel.onTimeChange(settings.hour + 1, settings.minute) }) { Text("+") }
                    Text(":", color = textColor, fontSize = 20.sp)
                    OutlinedButton(onClick = { viewModel.onTimeChange(settings.hour, settings.minute - MINUTE_STEP) }) { Text("-") }
                    Text(settings.minute.toString().padStart(2, '0'), color = textColor, fontSize = 20.sp)
                    OutlinedButton(onClick = { viewModel.onTimeChange(settings.hour, settings.minute + MINUTE_STEP) }) { Text("+") }
                }
            }

            Section("Which notes") {
                ScopeOption("All notes", settings.scope == DailyExportScope.ALL) {
                    viewModel.onScopeChange(DailyExportScope.ALL)
                }
                ScopeOption("Notes created that day", settings.scope == DailyExportScope.CREATED_TODAY) {
                    viewModel.onScopeChange(DailyExportScope.CREATED_TODAY)
                }
                ScopeOption(
                    "Notes new or changed since the last export",
                    settings.scope == DailyExportScope.CHANGED_SINCE_LAST_EXPORT
                ) { viewModel.onScopeChange(DailyExportScope.CHANGED_SINCE_LAST_EXPORT) }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Keep older copies", color = textColor, fontSize = 18.sp)
                    Text(
                        "When off, an existing file for the same note is overwritten.",
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
                Switch(checked = settings.keepVersions, onCheckedChange = viewModel::onKeepVersionsChange)
            }

            Section("Status") {
                Text(lastRunText(settings), color = textColor, fontSize = 14.sp)
                Button(onClick = viewModel::onExportNow, enabled = settings.folderUri != null) { Text("Export now") }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = LocalCustomColors.current.bodyContentColor
        )
        content()
    }
}

@Composable
private fun ScopeOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label, color = LocalCustomColors.current.bodyContentColor, fontSize = 14.sp)
    }
}

private fun lastRunText(settings: DailyExportSettings): String {
    val at = settings.lastRunAtMillis ?: return "Not exported yet"
    val time = Instant.fromEpochMilliseconds(at).toLocalDateTime(TimeZone.currentSystemDefault())
    val stamp = "${time.date} ${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
    return "Last run $stamp. ${settings.lastRunMessage.orEmpty()}"
}
