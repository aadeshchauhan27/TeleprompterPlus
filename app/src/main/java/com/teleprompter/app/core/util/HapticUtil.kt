package com.teleprompter.app.core.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

enum class HapticType {
    LIGHT_TICK,
    CLICK,
    SELECTION,
    CONFIRM,
    HEAVY_CLICK,
    COUNTDOWN_TICK,
    WARNING
}

class AppHaptics(
    private val context: Context,
    private val view: View? = null
) {
    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    fun lightTick() = perform(HapticType.LIGHT_TICK)
    fun click() = perform(HapticType.CLICK)
    fun selection() = perform(HapticType.SELECTION)
    fun confirm() = perform(HapticType.CONFIRM)
    fun heavyClick() = perform(HapticType.HEAVY_CLICK)
    fun countdownTick() = perform(HapticType.COUNTDOWN_TICK)
    fun warning() = perform(HapticType.WARNING)

    fun perform(type: HapticType) {
        try {
            // First try View-based hardware haptics (ultra low latency, system-synchronized)
            var handledByView = false
            if (view != null) {
                val constant = when (type) {
                    HapticType.LIGHT_TICK -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            HapticFeedbackConstants.SEGMENT_TICK
                        } else {
                            HapticFeedbackConstants.CLOCK_TICK
                        }
                    }
                    HapticType.CLICK -> HapticFeedbackConstants.KEYBOARD_TAP
                    HapticType.SELECTION -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            HapticFeedbackConstants.GESTURE_START
                        } else {
                            HapticFeedbackConstants.KEYBOARD_TAP
                        }
                    }
                    HapticType.CONFIRM -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            HapticFeedbackConstants.CONFIRM
                        } else {
                            HapticFeedbackConstants.LONG_PRESS
                        }
                    }
                    HapticType.HEAVY_CLICK -> HapticFeedbackConstants.LONG_PRESS
                    HapticType.COUNTDOWN_TICK -> HapticFeedbackConstants.CLOCK_TICK
                    HapticType.WARNING -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            HapticFeedbackConstants.REJECT
                        } else {
                            HapticFeedbackConstants.LONG_PRESS
                        }
                    }
                }
                handledByView = view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
            }

            // Fallback or complement with Vibrator VibrationEffect if view haptics was not handled or vibrator is active
            val vib = vibrator
            if (vib != null && vib.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val effect = when (type) {
                        HapticType.LIGHT_TICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        HapticType.CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                        HapticType.SELECTION -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                        HapticType.CONFIRM -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                        HapticType.HEAVY_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                        HapticType.COUNTDOWN_TICK -> VibrationEffect.createOneShot(18, 120)
                        HapticType.WARNING -> VibrationEffect.createOneShot(45, 200)
                    }
                    vib.vibrate(effect)
                } else if (!handledByView && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val duration = when (type) {
                        HapticType.LIGHT_TICK -> 8L
                        HapticType.CLICK -> 18L
                        HapticType.SELECTION -> 14L
                        HapticType.CONFIRM -> 35L
                        HapticType.HEAVY_CLICK -> 40L
                        HapticType.COUNTDOWN_TICK -> 15L
                        HapticType.WARNING -> 50L
                    }
                    vib.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        } catch (_: Exception) {
            // Never allow haptics to crash app
        }
    }
}

@Composable
fun rememberAppHaptics(): AppHaptics {
    val context = LocalContext.current
    val view = LocalView.current
    return remember(context, view) {
        AppHaptics(context.applicationContext, view)
    }
}
