package com.moonbench.bifrost.external

import com.moonbench.bifrost.animations.LedAnimationType

/**
 * Which capture an effect needs, and whether Bifrost holds it right now. Pure
 * (no Android types) so the rules are unit-tested; the live inputs come from
 * LEDService (projection token) and BifrostAccessibilityService (running).
 *
 * Mirrors what LEDService.createAnimation actually does:
 *  - AMBIENT samples the screen through MediaProjection when the user prefers
 *    it AND a token is held, otherwise through the accessibility screenshot
 *    service. Either one is enough.
 *  - AUDIO_REACTIVE and AMBIAURORA need MediaProjection (playback capture);
 *    without a token createAnimation returns null and the LEDs stay dark.
 *  - Everything else needs nothing.
 *
 * An external caller can never obtain the capture consent itself (only
 * Bifrost's own UI can show that prompt), so a missing capture is reported to
 * the caller instead of starting a dark override.
 */
object ExternalCapture {

    enum class Need { NONE, SCREEN, PROJECTION }

    fun isReactive(effect: LedAnimationType): Boolean = needOf(effect) != Need.NONE

    fun needOf(effect: LedAnimationType): Need = when (effect) {
        LedAnimationType.AMBIENT -> Need.SCREEN
        LedAnimationType.AUDIO_REACTIVE, LedAnimationType.AMBIAURORA -> Need.PROJECTION
        else -> Need.NONE
    }

    /**
     * @param hasProjectionToken LEDService holds a MediaProjection consent token.
     * @param accessibilityRunning the accessibility capture service is connected.
     * @param ambientPrefersProjection the user's "use MediaProjection for ambient" setting.
     */
    fun isAvailable(
        effect: LedAnimationType,
        hasProjectionToken: Boolean,
        accessibilityRunning: Boolean,
        ambientPrefersProjection: Boolean,
    ): Boolean = when (needOf(effect)) {
        Need.NONE -> true
        Need.PROJECTION -> hasProjectionToken
        Need.SCREEN -> (ambientPrefersProjection && hasProjectionToken) || accessibilityRunning
    }
}
