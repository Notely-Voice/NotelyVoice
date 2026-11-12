package com.module.notelycompose.notes.ui.list

import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.note_detail_more_options
import org.jetbrains.compose.resources.stringResource

@Composable
fun FilterSelection(
    titles: List<String>,
    icons: List<ImageVector>,
    tabSelected: String,
    onTabSelected: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    ScrollableTabRow(
        edgePadding = 0.dp,
        backgroundColor = Color.Transparent,
        selectedTabIndex = titles.indexOf(tabSelected),
        contentColor = LocalCustomColors.current.dateContentColorViewColor,
        indicator = { tabPositions: List<TabPosition> ->

            val selectedIndex = titles.indexOf(tabSelected).let { index ->
                if (index == -1) 0 else index // Default to first tab if key not found
            }

            Box(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[selectedIndex])
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
                    .border(BorderStroke(2.dp, LocalCustomColors.current.dateContentIconColor), RoundedCornerShape(16.dp))
            )
        },
        divider = { }
    ) {
        titles.forEachIndexed { index, title ->
            val selected = index == titles.indexOf(tabSelected)
            val icon = icons[index]

            val textModifier = Modifier
                .padding(vertical = 4.dp, horizontal = 4.dp)

            Tab(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LocalCustomColors.current.backgroundViewColor),
                selected = selected,
                onClick = {
                    onTabSelected(titles[index])
                    focusManager.clearFocus()
                }
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(Res.string.note_detail_more_options),
                        tint = LocalCustomColors.current.dateContentIconColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        modifier = textModifier,
                        text = title,
                        color = LocalCustomColors.current.dateContentIconColor
                    )
                }
            }
        }
    }
}
