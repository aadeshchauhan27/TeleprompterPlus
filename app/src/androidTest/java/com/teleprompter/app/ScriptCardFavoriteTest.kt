package com.teleprompter.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.teleprompter.app.core.theme.TelePrompterTheme
import com.teleprompter.app.data.local.entity.ScriptEntity
import com.teleprompter.app.ui.components.ScriptCard
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/** A favourite card must stay readable: it used to be painted solid accent-blue from edge to edge. */
@RunWith(AndroidJUnit4::class)
class ScriptCardFavoriteTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var primary = Color.Unspecified
    private var surface = Color.Unspecified
    private var tertiary = Color.Unspecified
    private var density = 1f

    private fun show(dark: Boolean, favorite: Boolean) {
        composeRule.setContent {
            TelePrompterTheme(darkTheme = dark) {
                primary = MaterialTheme.colorScheme.primary
                surface = MaterialTheme.colorScheme.surface
                tertiary = MaterialTheme.colorScheme.tertiary
                density = LocalDensity.current.density
                CardUnderTest(favorite)
            }
        }
    }

    @Composable
    private fun CardUnderTest(favorite: Boolean) {
        Box(modifier = Modifier.padding(16.dp).testTag("card")) {
            ScriptCard(
                script = ScriptEntity(
                    id = "x", title = "Favourite script", content = "some words here",
                    isFavorite = favorite, wordCount = 3, estimatedReadingTime = 60
                ),
                onClick = {}, onFavoriteToggle = {}, onDuplicate = {}, onDelete = {}
            )
        }
    }

    private fun dist(a: Color, b: Color) =
        abs(a.red - b.red) + abs(a.green - b.green) + abs(a.blue - b.blue)

    private fun check(dark: Boolean) {
        show(dark, favorite = true)
        composeRule.waitForIdle()
        val img = composeRule.onNodeWithTag("card").captureToImage().toPixelMap()
        // interior of the card, away from text and the left bar: right-middle area
        val interior = img[(img.width * 0.62f).toInt(), (img.height * 0.82f).toInt()]
        assertTrue(
            "favourite card interior $interior must not be the solid accent $primary",
            dist(interior, primary) > 0.5f
        )
        assertTrue(
            "favourite card interior $interior should stay close to the surface $surface",
            dist(interior, surface) < 0.25f
        )
        // the slim bar on the left edge is the amber highlight
        val barX = (8 * density).toInt() // bar spans 6..10dp from the card edge
        val bar = img[barX.coerceAtMost(img.width - 1), img.height / 2]
        assertTrue("left bar $bar should be the amber highlight $tertiary", dist(bar, tertiary) < 0.2f)
    }

    @Test
    fun favoriteCard_inLightTheme_isReadableWithAmberBar() = check(dark = false)

    @Test
    fun favoriteCard_inDarkTheme_isReadableWithAmberBar() = check(dark = true)
}
