package com.module.notelycompose.onboarding.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.platform.getPlatform
import com.module.notelycompose.notes.ui.theme.PoppingsFontFamily
import com.module.notelycompose.platform.presentation.PlatformUiState
import com.module.notelycompose.platform.presentation.PlatformViewModel
import kotlinx.coroutines.launch
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.onboarding_ios_one
import com.module.notelycompose.resources.onboarding_ios_three
import com.module.notelycompose.resources.onboarding_ios_two
import com.module.notelycompose.resources.onboarding_ios_four
import com.module.notelycompose.resources.onboarding_android_one
import com.module.notelycompose.resources.onboarding_android_tablet_one
import com.module.notelycompose.resources.onboarding_android_tablet_two
import com.module.notelycompose.resources.onboarding_android_tablet_three
import com.module.notelycompose.resources.onboarding_android_tablet_four
import com.module.notelycompose.resources.onboarding_ios_tablet_one
import com.module.notelycompose.resources.onboarding_ios_tablet_two
import com.module.notelycompose.resources.onboarding_ios_tablet_three
import com.module.notelycompose.resources.onboarding_ios_tablet_four
import com.module.notelycompose.resources.onboarding_android_three
import com.module.notelycompose.resources.onboarding_android_four
import com.module.notelycompose.resources.onboarding_android_two
import com.module.notelycompose.resources.onboarding_get_started
import com.module.notelycompose.resources.onboarding_next
import com.module.notelycompose.resources.onboarding_skip
import com.module.notelycompose.resources.onboarding_screen_one_title
import com.module.notelycompose.resources.onboarding_screen_one_desc
import com.module.notelycompose.resources.onboarding_screen_two_title
import com.module.notelycompose.resources.onboarding_screen_two_desc
import com.module.notelycompose.resources.onboarding_screen_three_title
import com.module.notelycompose.resources.onboarding_screen_three_desc
import com.module.notelycompose.resources.onboarding_screen_four_title
import com.module.notelycompose.resources.onboarding_screen_four_desc
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

data class OnboardingPage(
    val title: String,
    val description: String,
    val backgroundColor: Color,
    val textColor: Color,
    val androidResources: Painter,
    val iOSResources: Painter
)

@Composable
fun OnboardingWalkthrough(
    onFinish: () -> Unit = {},
    platformState: PlatformUiState
) {
    val pages = mutableListOf(
        OnboardingPage(
            title = stringResource(Res.string.onboarding_screen_one_title),
            description = stringResource(Res.string.onboarding_screen_one_desc),
            backgroundColor = Color(0xFFFFFAD0),
            textColor = Color(0xFFCA7F58),
            androidResources = when {
                platformState.isTablet -> painterResource(Res.drawable.onboarding_android_tablet_one)
                else -> painterResource(Res.drawable.onboarding_android_one)
            },
            iOSResources = when {
                platformState.isTablet -> painterResource(Res.drawable.onboarding_ios_tablet_one)
                else -> painterResource(Res.drawable.onboarding_ios_one)
            }
        ),
        OnboardingPage(
            title = stringResource(Res.string.onboarding_screen_two_title),
            description = stringResource(Res.string.onboarding_screen_two_desc),
            backgroundColor = Color(0xFFFFFAD0),
            textColor = Color(0xFFCA7F58),
            androidResources = when {
                platformState.isTablet -> painterResource(Res.drawable.onboarding_android_tablet_two)
                else -> painterResource(Res.drawable.onboarding_android_two)
            },
            iOSResources = when {
                platformState.isTablet -> painterResource(Res.drawable.onboarding_ios_tablet_two)
                else -> painterResource(Res.drawable.onboarding_ios_two)
            }
        ),
        OnboardingPage(
            title = stringResource(Res.string.onboarding_screen_three_title),
            description = stringResource(Res.string.onboarding_screen_three_desc),
            backgroundColor = Color(0xFFFFFAD0),
            textColor = Color(0xFFCA7F58),
            androidResources = when {
                platformState.isTablet -> painterResource(Res.drawable.onboarding_android_tablet_three)
                else -> painterResource(Res.drawable.onboarding_android_three)
            },
            iOSResources = when {
                platformState.isTablet -> painterResource(Res.drawable.onboarding_ios_tablet_three)
                else -> painterResource(Res.drawable.onboarding_ios_three)
            }
        ),
        OnboardingPage(
            title = stringResource(Res.string.onboarding_screen_four_title),
            description = stringResource(Res.string.onboarding_screen_four_desc),
            backgroundColor = Color(0xFFFFFAD0),
            textColor = Color(0xFFCA7F58),
            androidResources = when {
                platformState.isTablet -> painterResource(Res.drawable.onboarding_android_tablet_four)
                else -> painterResource(Res.drawable.onboarding_android_four)
            },
            iOSResources = when {
                platformState.isTablet -> painterResource(Res.drawable.onboarding_ios_tablet_four)
                else -> painterResource(Res.drawable.onboarding_ios_four)
            }
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pages[pagerState.currentPage].backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Bar Spacer
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onFinish,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.onboarding_skip),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(0.dp)
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    isTablet = platformState.isTablet,
                    isAndroid = platformState.isAndroid
                )
            }

            // Bottom Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageIndicators(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                    activeColor = pages[pagerState.currentPage].textColor,
                    inactiveColor = pages[pagerState.currentPage].textColor.copy(alpha = 0.3f)
                )

                // Next/Get Started Button
                Button(
                    onClick = {
                        if (pagerState.currentPage == pages.size - 1) {
                            onFinish()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages[pagerState.currentPage].textColor
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) stringResource(Res.string.onboarding_get_started) else stringResource(Res.string.onboarding_next),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    isTablet: Boolean,
    isAndroid: Boolean
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VoiceNotePageContent(page, isTablet, isAndroid)
    }
}

@Composable
fun VoiceNotePageContent(
    page: OnboardingPage,
    isTablet: Boolean,
    isAndroid: Boolean
) {

    val resource = if(isAndroid) {
        page.androidResources
    } else {
        page.iOSResources
    }

    val descriptionFontSize = if(isTablet) 20.sp else 18.sp
    val imageIllustrationWidth = if(isTablet) 800.dp else 360.dp

    Text(
        text = page.title,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PoppingsFontFamily(),
        color = page.textColor,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        lineHeight = 32.sp
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = resource,
                contentDescription = "Image illustration",
                modifier = Modifier
                    .width(imageIllustrationWidth),
                contentScale = ContentScale.FillWidth
            )
        }

        Text(
            text = page.description,
            fontSize = descriptionFontSize,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier
                .padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun PageIndicators(
    pageCount: Int,
    currentPage: Int,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage

            val animatedWidth by animateDpAsState(
                targetValue = if (isActive) 32.dp else 8.dp,
                animationSpec = tween(300, easing = EaseInOutQuad),
                label = "indicator_width"
            )

            val animatedColor by animateColorAsState(
                targetValue = if (isActive) activeColor else inactiveColor,
                animationSpec = tween(300, easing = EaseInOutQuad),
                label = "indicator_color"
            )

            Box(
                modifier = Modifier
                    .width(animatedWidth)
                    .height(8.dp)
                    .background(
                        animatedColor,
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}
