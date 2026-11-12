package com.module.notelycompose.notes.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.close
import com.module.notelycompose.resources.settings_light_theme
import com.module.notelycompose.resources.settings_dark_theme
import com.module.notelycompose.resources.settings_system_default
import com.module.notelycompose.resources.settings_change_language
import com.module.notelycompose.resources.settings_change_default
import com.module.notelycompose.resources.settings_transcription_language
import com.module.notelycompose.resources.settings_themes
import com.module.notelycompose.resources.settings_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsBottomSheet(
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.settings_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.close)
                    )
                }
            }
        }

        Divider()

        // Theme settings section with title and toggle switches

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.settings_themes),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        ThemeSettingsSection()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.settings_transcription_language),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Divider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.settings_change_default),
                fontSize = 16.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {}
            ) {
                Text(
                    text = stringResource(Res.string.settings_change_language),
                    fontSize = 16.sp
                )
            }
        }

        Spacer(Modifier.padding(600.dp))
    }
}

@Composable
fun ThemeSettingsSection() {
    var selectedTheme by remember { mutableStateOf(ThemeOption.SYSTEM) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        ThemeToggleOption(
            title = stringResource(Res.string.settings_system_default),
            isSelected = selectedTheme == ThemeOption.SYSTEM,
            onClick = { selectedTheme = ThemeOption.SYSTEM }
        )

        Divider()

        ThemeToggleOption(
            title = stringResource(Res.string.settings_light_theme),
            isSelected = selectedTheme == ThemeOption.LIGHT,
            onClick = { selectedTheme = ThemeOption.LIGHT }
        )

        Divider()

        ThemeToggleOption(
            title = stringResource(Res.string.settings_dark_theme),
            isSelected = selectedTheme == ThemeOption.DARK,
            onClick = { selectedTheme = ThemeOption.DARK }
        )
    }
}

@Composable
fun ThemeToggleOption(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp
        )
        Switch(
            checked = isSelected,
            onCheckedChange = { if (!isSelected) onClick() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = LocalCustomColors.current.bodyContentColor
            )
        )
    }
}

enum class ThemeOption {
    SYSTEM, LIGHT, DARK
}
