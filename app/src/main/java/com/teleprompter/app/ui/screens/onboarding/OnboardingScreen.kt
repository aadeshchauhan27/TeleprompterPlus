package com.teleprompter.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teleprompter.app.core.theme.AccentCobalt
import com.teleprompter.app.core.theme.PrompterDefaultBackground
import com.teleprompter.app.core.theme.ReadingGuideBorder
import com.teleprompter.app.core.theme.ReadingGuideHighlight
import com.teleprompter.app.core.theme.RecordRed
import kotlinx.coroutines.launch

enum class OnboardingArt { PROMPTER, CONTROLS, CAMERA }

data class OnboardingPage(
    val art: OnboardingArt,
    val title: String,
    val body: String
)

val onboardingPages = listOf(
    OnboardingPage(
        art = OnboardingArt.PROMPTER,
        title = "Read it off the screen, talk to the lens",
        body = "The words scroll at your pace, so you keep eye contact without memorising a thing."
    ),
    OnboardingPage(
        art = OnboardingArt.CONTROLS,
        title = "Tap to play, drag to jump",
        body = "Change the speed and size on the fly. Drop [pause 3] or [slow] on its own line when you need a breather."
    ),
    OnboardingPage(
        art = OnboardingArt.CAMERA,
        title = "Record with the script on top",
        body = "It floats over the camera preview but never shows up in your video. Drag it right under the lens."
    )
)

/** Label of the final button; it leads to the sample script on the home screen. */
const val ONBOARDING_LAST_BUTTON = "Try the sample script"

object OnboardingTags {
    const val NEXT = "onboarding_next"
    const val SKIP = "onboarding_skip"
    const val PAGER = "onboarding_pager"
}

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    pages: List<OnboardingPage> = onboardingPages
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Step marker on the left, Skip on the right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(pages.size) { i ->
                    Box(
                        modifier = Modifier
                            .width(if (i == pagerState.currentPage) 28.dp else 14.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (i <= pagerState.currentPage) MaterialTheme.colorScheme.onBackground
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
            if (!isLastPage) {
                TextButton(onClick = onFinished, modifier = Modifier.testTag(OnboardingTags.SKIP)) {
                    Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .testTag(OnboardingTags.PAGER)
        ) { index ->
            val page = pages[index]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                // Real product mock-up instead of a generic icon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PrompterDefaultBackground)
                ) {
                    when (page.art) {
                        OnboardingArt.PROMPTER -> PrompterMock(showCue = false, showSpeed = false)
                        OnboardingArt.CONTROLS -> PrompterMock(showCue = true, showSpeed = true)
                        OnboardingArt.CAMERA -> CameraMock()
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = page.title,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = page.body,
                    fontSize = 16.sp,
                    lineHeight = 23.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        Button(
            onClick = {
                if (isLastPage) {
                    onFinished()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(52.dp)
                .testTag(OnboardingTags.NEXT)
        ) {
            Text(
                text = if (isLastPage) ONBOARDING_LAST_BUTTON else "Next",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}

/** A miniature of the real prompter: actual script lines, the reading band, optionally a cue and speed readout. */
@Composable
private fun BoxScope.PrompterMock(showCue: Boolean, showSpeed: Boolean) {
    val dim = Color.White.copy(alpha = 0.32f)
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Hi, I'm recording a quick update", color = dim, fontSize = 17.sp)
        Text("on how the project is going.", color = dim.copy(alpha = 0.5f), fontSize = 17.sp)
        // Reading band, same colours as the real prompter
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ReadingGuideHighlight)
                .border(1.5.dp, ReadingGuideBorder, RoundedCornerShape(10.dp))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("First, the good news.", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Medium)
        }
        if (showCue) {
            Text(
                "⏸ pause 3s",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic
            )
        }
        Text("We shipped the first version", color = dim, fontSize = 17.sp)
        Text("two weeks ahead of plan.", color = dim.copy(alpha = 0.5f), fontSize = 17.sp)
    }
    if (showSpeed) {
        Text(
            text = "Speed 50  •  ~130 WPM",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
    }
}

/** Camera preview with the script card near the lens and the record button. */
@Composable
private fun BoxScope.CameraMock() {
    // Stand-in for the live preview
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A2233))
    )
    // Lens
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 14.dp)
            .size(8.dp)
            .clip(CircleShape)
            .background(Color.Black)
    )
    // Script card under the lens
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 40.dp, start = 36.dp, end = 36.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("First, the good news.", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text("We shipped the first version", color = Color.White.copy(alpha = 0.55f), fontSize = 14.sp)
        }
    }
    // Drag handle hint
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 33.dp)
            .width(36.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White.copy(alpha = 0.75f))
    )
    // Record button
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 20.dp)
            .size(56.dp)
            .border(3.dp, Color.White, CircleShape)
            .padding(6.dp)
            .clip(CircleShape)
            .background(RecordRed)
    )
}
