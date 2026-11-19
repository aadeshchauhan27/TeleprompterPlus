package com.teleprompter.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.teleprompter.app.core.theme.TelePrompterTheme
import com.teleprompter.app.ui.screens.onboarding.ONBOARDING_LAST_BUTTON
import com.teleprompter.app.ui.screens.onboarding.OnboardingScreen
import com.teleprompter.app.ui.screens.onboarding.OnboardingTags
import com.teleprompter.app.ui.screens.onboarding.onboardingPages
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var finishedCount = 0

    private fun show() {
        finishedCount = 0
        composeRule.setContent {
            TelePrompterTheme {
                OnboardingScreen(onFinished = { finishedCount++ })
            }
        }
    }

    @Test
    fun startsOnFirstPage_withSkipAndNext() {
        show()
        composeRule.onNodeWithText(onboardingPages.first().title).assertIsDisplayed()
        composeRule.onNodeWithTag(OnboardingTags.SKIP).assertIsDisplayed()
        composeRule.onNodeWithText("Next").assertIsDisplayed()
    }

    @Test
    fun next_advancesThroughPages_thenGetStartedFinishes() {
        show()

        composeRule.onNodeWithTag(OnboardingTags.NEXT).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(onboardingPages[1].title).assertIsDisplayed()

        composeRule.onNodeWithTag(OnboardingTags.NEXT).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(onboardingPages[2].title).assertIsDisplayed()
        composeRule.onNodeWithText(ONBOARDING_LAST_BUTTON).assertIsDisplayed()

        assertEquals(0, finishedCount)
        composeRule.onNodeWithTag(OnboardingTags.NEXT).performClick()
        assertEquals(1, finishedCount)
    }

    @Test
    fun skip_finishesImmediately() {
        show()
        composeRule.onNodeWithTag(OnboardingTags.SKIP).performClick()
        assertEquals(1, finishedCount)
    }
}
