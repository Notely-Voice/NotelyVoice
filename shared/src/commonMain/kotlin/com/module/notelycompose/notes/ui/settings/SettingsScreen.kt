package com.module.notelycompose.notes.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.modelDownloader.NO_MODEL_SELECTION
import com.module.notelycompose.modelDownloader.OPTIMIZED_MODEL_SELECTION
import com.module.notelycompose.notes.extension.TEXT_SIZE_BODY
import com.module.notelycompose.notes.extension.intBodyFontSizes
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.dailyexport.DailyExportRepository
import com.module.notelycompose.dailyexport.DailyExportScheduler
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.platform.Theme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.settings
import com.module.notelycompose.resources.customize_your_experience
import com.module.notelycompose.resources.language_and_region
import com.module.notelycompose.resources.transcription_language
import com.module.notelycompose.resources.language_used_for_voice_transcription
import com.module.notelycompose.resources.select_language
import com.module.notelycompose.resources.appearance
import com.module.notelycompose.resources.body_text_pt
import com.module.notelycompose.resources.theme
import com.module.notelycompose.resources.choose_how_the_app_looks
import com.module.notelycompose.resources.close
import com.module.notelycompose.resources.accessibility
import com.module.notelycompose.resources.batch_export_settings_how_to
import com.module.notelycompose.resources.batch_export_settings_how_to_1
import com.module.notelycompose.resources.batch_export_settings_how_to_2
import com.module.notelycompose.resources.batch_export_settings_how_to_3
import com.module.notelycompose.resources.batch_export_settings_how_to_description_1
import com.module.notelycompose.resources.batch_export_settings_how_to_description_2
import com.module.notelycompose.resources.batch_export_settings_how_to_description_3
import com.module.notelycompose.resources.batch_export_settings_how_to_description_4
import com.module.notelycompose.resources.batch_export_settings_title
import com.module.notelycompose.resources.body_text_default
import com.module.notelycompose.resources.body_text_preferred_text
import com.module.notelycompose.resources.body_text_size
import com.module.notelycompose.resources.export
import com.module.notelycompose.resources.ic_export_selections
import com.module.notelycompose.resources.navigate
import org.jetbrains.compose.resources.painterResource
import com.module.notelycompose.resources.transcription_model_selection
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.module.notelycompose.platform.Transcriber
import com.module.notelycompose.modelDownloader.ModelSelection
import androidx.compose.runtime.LaunchedEffect

@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    navigateToLanguages: () -> Unit,
    navigateToSettingsText: () -> Unit,
    navigateToModelSelection: () -> Unit,
    navigateToMoveModel: () -> Unit,
    navigateToDailyExport: () -> Unit = {},
    preferencesRepository: PreferencesRepository = koinInject(),
    dailyExportScheduler: DailyExportScheduler = koinInject(),
    dailyExportRepository: DailyExportRepository = koinInject()
) {
    val dailyExportSettings by dailyExportRepository.getSettings()
        .collectAsState(com.module.notelycompose.dailyexport.DailyExportSettings())
    val language by preferencesRepository.getDefaultTranscriptionLanguage()
        .collectAsState(languageCodeMap.entries.first().key)
    val uiMode by preferencesRepository.getTheme().collectAsState(Theme.SYSTEM.name)
    val coroutineScope = rememberCoroutineScope()
    val bodyTextSize = preferencesRepository.getBodyTextSize().collectAsState(TEXT_SIZE_BODY).value
    val modelSavedSelection = preferencesRepository.getModelSelection().collectAsState(
        NO_MODEL_SELECTION
    ).value
    val modelFilePath by preferencesRepository.getModelFilePath().collectAsState(null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.bodyBackgroundColor)
    ) {
        // Header
        SettingsHeader(
            onDismiss = navigateBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                LanguageRegionSection(
                    navigateToLanguages = navigateToLanguages,
                    selectedLanguage = language
                )
            }

            item {
                AppearanceSection(
                    selectedTheme = Theme.valueOf(uiMode),
                    onThemeSelected = {
                        coroutineScope.launch {
                            preferencesRepository.setTheme(it.name)
                        }
                    }
                )
            }

            item {
                LanguageModelSelectionSection(
                    navigateToModelSelection = navigateToModelSelection,
                    modelSavedSelection = modelSavedSelection
                )
            }

            item {
                ModelStorageSection(
                    modelFilePath = modelFilePath,
                    modelSelection = modelSavedSelection,
                    preferencesRepository = preferencesRepository,
                    navigateToMoveModel = navigateToMoveModel
                )
            }

            item {
                ExportSettingSection()
            }

            if (dailyExportScheduler.isSupported) {
                item {
                    TextSizeSettingItem(
                        title = "Daily export",
                        subtitle = "Save your notes to a folder automatically every day",
                        currentValue = if (dailyExportSettings.enabled) "On" else "Off",
                        onClick = navigateToDailyExport
                    )
                }
            }

            item {
                AccessibilitySection(
                    navigateToSettingsText = navigateToSettingsText,
                    bodyTextSize = bodyTextSize
                )
            }
        }
    }
}

@Composable
private fun SettingsHeader(
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(Res.string.settings),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = LocalCustomColors.current.bodyContentColor
            )
            Text(
                text = stringResource(Res.string.customize_your_experience),
                fontSize = 16.sp,
                color = LocalCustomColors.current.bodyContentColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        IconButton(
            onClick = { onDismiss() },
            modifier = Modifier
                .size(48.dp)
                .background(
                    LocalCustomColors.current.settingCancelBackgroundColor,
                    RoundedCornerShape(24.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.close),
                tint = LocalCustomColors.current.settingCancelTextColor
            )
        }
    }
}

@Composable
private fun LanguageRegionSection(
    navigateToLanguages: () -> Unit,
    selectedLanguage: String
) {
    Column {
        Text(
            text = stringResource(Res.string.language_and_region),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = LocalCustomColors.current.bodyContentColor,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        TranscriptionLanguageItem(
            navigateToLanguages = navigateToLanguages,
            selectedLanguage = selectedLanguage
        )
    }
}

@Composable
fun TranscriptionLanguageItem(
    navigateToLanguages: () -> Unit,
    selectedLanguage: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(Res.string.transcription_language),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = LocalCustomColors.current.bodyContentColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = stringResource(Res.string.language_used_for_voice_transcription),
            fontSize = 14.sp,
            color = LocalCustomColors.current.bodyContentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navigateToLanguages() }
                .border(
                    2.dp,
                    LocalCustomColors.current.bodyContentColor,
                    RoundedCornerShape(8.dp)
            ),
            colors = CardDefaults.cardColors(
                containerColor = LocalCustomColors.current.settingLanguageBackgroundColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Color(0xFF6366F1),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedLanguage.uppercase().take(2),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = languageCodeMap[selectedLanguage] ?:"en",
                        fontSize = 16.sp,
                        color = LocalCustomColors.current.bodyContentColor,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = stringResource(Res.string.select_language),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AppearanceSection(
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.appearance),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = LocalCustomColors.current.bodyContentColor,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        ThemeSection(
            selectedTheme = selectedTheme,
            onThemeSelected = onThemeSelected
        )
    }
}

@Composable
private fun ThemeSection(
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.theme),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = LocalCustomColors.current.bodyContentColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = stringResource(Res.string.choose_how_the_app_looks),
            fontSize = 14.sp,
            color = LocalCustomColors.current.bodyContentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemeOption(
                theme = Theme.LIGHT,
                isSelected = selectedTheme == Theme.LIGHT,
                onSelected = { onThemeSelected(Theme.LIGHT) },
                modifier = Modifier.weight(1f)
            )
            ThemeOption(
                theme = Theme.DARK,
                isSelected = selectedTheme == Theme.DARK,
                onSelected = { onThemeSelected(Theme.DARK) },
                modifier = Modifier.weight(1f)
            )
            ThemeOption(
                theme = Theme.SYSTEM,
                isSelected = selectedTheme == Theme.SYSTEM,
                onSelected = { onThemeSelected(Theme.SYSTEM) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeOption(
    theme: Theme,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onSelected() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        LocalCustomColors.current.bodyContentColor,
                        RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = LocalCustomColors.current.bodyBackgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ThemePreview(theme = theme)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = theme.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = LocalCustomColors.current.bodyContentColor
            )
        }
    }
}

@Composable
private fun ThemePreview(theme: Theme) {
    when (theme) {
        Theme.LIGHT -> {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFFF1F1F1),
                        RoundedCornerShape(8.dp)
                    )
            )
        }
        Theme.DARK -> {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFF2D3748),
                        RoundedCornerShape(8.dp)
                    )
            )
        }
        Theme.SYSTEM -> {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                // Left half - light
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .background(Color(0xFFF1F1F1))
                        .align(Alignment.CenterStart)
                )
                // Right half - dark
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .background(Color(0xFF2D3748))
                        .align(Alignment.CenterEnd)
                )
            }
        }
        else -> {
            // Fallback for any other themes
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFFF1F1F1),
                        RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

@Composable
fun AccessibilitySection(
    navigateToSettingsText: () -> Unit,
    bodyTextSize: Float
) {
    Text(
        text = stringResource(Res.string.accessibility),
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        color = LocalCustomColors.current.bodyContentColor,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    TextSizeSettingItem(
        title = stringResource(Res.string.body_text_size),
        subtitle = stringResource(Res.string.body_text_preferred_text),
        currentValue = if(bodyTextSize.intBodyFontSizes() == TEXT_SIZE_BODY.toInt()) {
            stringResource(Res.string.body_text_default)
        } else {
            stringResource(Res.string.body_text_pt, bodyTextSize.intBodyFontSizes())
        },
        onClick = {
            navigateToSettingsText()
        }
    )
}

@Composable
fun TextSizeSettingItem(
    title: String,
    subtitle: String,
    currentValue: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                LocalCustomColors.current.settingsBodyBorderColor,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .background(LocalCustomColors.current.bodyBackgroundColor)
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = LocalCustomColors.current.bodyContentColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = LocalCustomColors.current.settingsBodyTextColor,
                    lineHeight = 20.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentValue,
                    fontSize = 16.sp,
                    color = LocalCustomColors.current.settingsBodyTextColor,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = stringResource(Res.string.navigate),
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


@Composable
fun ExportSettingSection() {
    Column {
        Text(
            text = stringResource(Res.string.batch_export_settings_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = LocalCustomColors.current.bodyContentColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    LocalCustomColors.current.settingsBodyBorderColor,
                    RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = LocalCustomColors.current.bodyBackgroundColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFF6366F1).copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👆",
                            fontSize = 20.sp
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(Res.string.batch_export_settings_how_to_description_1),
                            fontSize = 14.sp,
                            color = LocalCustomColors.current.settingsBodyTextColor,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            LocalCustomColors.current.settingsBodyBorderColor.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.batch_export_settings_how_to_1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalCustomColors.current.bodyContentColor
                            )

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        Color(0xFFCD9777),
                                        RoundedCornerShape(4.dp)
                                    )
                            )

                            Text(
                                text = stringResource(Res.string.batch_export_settings_how_to_description_2),
                                fontSize = 12.sp,
                                color = LocalCustomColors.current.settingsBodyTextColor
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.batch_export_settings_how_to_2),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalCustomColors.current.bodyContentColor
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            Color(0xFFCD9777),
                                            RoundedCornerShape(2.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            Color(0xFFCD9777),
                                            RoundedCornerShape(2.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }

                                Text(
                                    text = stringResource(Res.string.batch_export_settings_how_to_description_3),
                                    fontSize = 12.sp,
                                    color = LocalCustomColors.current.settingsBodyTextColor
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.batch_export_settings_how_to_3),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalCustomColors.current.bodyContentColor
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            Color(0xFFCD9777),
                                            RoundedCornerShape(2.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material.Icon(
                                        painter = painterResource(Res.drawable.ic_export_selections),
                                        contentDescription = stringResource(Res.string.export),
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }

                            Text(
                                text = stringResource(Res.string.batch_export_settings_how_to_description_4),
                                fontSize = 12.sp,
                                color = LocalCustomColors.current.settingsBodyTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageModelSelectionSection(
    navigateToModelSelection: () -> Unit,
    modelSavedSelection: Int
) {
    Column(
        modifier = Modifier.clickable {
            navigateToModelSelection()
        }
    ) {
        Text(
            text = stringResource(Res.string.transcription_model_selection),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = LocalCustomColors.current.bodyContentColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        SettingsModelOptionCard(
            model = when (modelSavedSelection) {
                OPTIMIZED_MODEL_SELECTION -> {
                    ModelOption(
                        title = "Optimized model (multilingual)",
                        description = "Highest accuracy available\n" +
                                "Supports all languages except Hindi & Gujarati\n" +
                                "Larger file size, slower performance",
                        size = "468 MB"
                    )
                }
                else -> {
                    ModelOption(
                        title = "Standard model (multilingual)",
                        description = "Faster performance and smaller file size\n" +
                                "Supports all languages",
                        size = "142 MB"
                    )
                }
            },
            onClick = {
                navigateToModelSelection()
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun SettingsModelOptionCard(
    model: ModelOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            LocalCustomColors.current.modelSelectionBgColor
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, LocalCustomColors.current.bodyContentColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = model.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LocalCustomColors.current.bodyContentColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = model.description,
                    fontSize = 14.sp,
                    color = LocalCustomColors.current.modelSelectionDescColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = model.size,
                    fontSize = 14.sp,
                    color = LocalCustomColors.current.modelSelectionDescColor
                )
            }
        }
    }
}

@Composable
fun ModelStorageSection(
    modelFilePath: String?,
    modelSelection: Int,
    preferencesRepository: PreferencesRepository,
    navigateToMoveModel: () -> Unit,
    transcriber: Transcriber = koinInject(),
    modelSelectionHelper: ModelSelection = koinInject()
) {
    var modelFileName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(modelSelection) {
        val selectedModel = modelSelectionHelper.getSelectedModel()
        modelFileName = selectedModel.name
    }

    val isInExternalStorage = modelFileName != null && transcriber.doesModelExistInExternalStorage(modelFileName!!)
    val isInInternalStorage = modelFileName != null && transcriber.doesModelExistInInternalStorage(modelFileName!!)

    if (modelFileName != null && (isInExternalStorage || isInInternalStorage)) {
        Column {
            Text(
                text = "Storage",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = LocalCustomColors.current.bodyContentColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isInExternalStorage) { navigateToMoveModel() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LocalCustomColors.current.bodyBackgroundColor
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    LocalCustomColors.current.settingsBodyBorderColor
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (isInInternalStorage) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "✓",
                                    fontSize = 20.sp,
                                    color = Color(0xFF10B981)
                                )
                                Text(
                                    text = "Model in protected storage",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LocalCustomColors.current.bodyContentColor
                                )
                            }
                        } else {
                            Text(
                                text = "Move model to protected storage",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = LocalCustomColors.current.bodyContentColor
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Recommended for GrapheneOS and MIUI users",
                                fontSize = 14.sp,
                                color = LocalCustomColors.current.settingsBodyTextColor,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    if (isInExternalStorage) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Move model",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
