package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Clean, lightweight, zero-error Sound Manager for CryptoCycles.
 * Uses native Android SoundPool for low-latency, zero-overhead audio.
 */
object AppSoundManager {

    private const val PREFS_NAME = "cryptocycles_sound_prefs"
    private const val KEY_SOUND_ENABLED = "sound_effects_enabled"

    // Disabled by default to ensure zero system resource queries on emulators
    private val _isSoundEnabled = MutableStateFlow(false)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private var sharedPreferences: SharedPreferences? = null
    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<SoundEffect, Int>()
    private val loadedSoundIds = mutableSetOf<Int>()
    private var isInitialized = false
    private var appContextRef: Context? = null

    enum class SoundEffect {
        CRYPTO_LAUNCH,
        TECH_CLICK,
        SWITCH_TOGGLE,
        SUCCESS_CHIME,
        SELECTION_POP
    }

    fun initialize(context: Context) {
        val appContext = context.applicationContext
        appContextRef = appContext

        try {
            sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedEnabled = sharedPreferences?.getBoolean(KEY_SOUND_ENABLED, false) ?: false
            _isSoundEnabled.value = savedEnabled
        } catch (_: Throwable) {
            // Silently ignore
        }
    }

    private fun ensureSoundPool() {
        if (isInitialized || !_isSoundEnabled.value) return
        val appContext = appContextRef ?: return

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build()

            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    loadedSoundIds.add(sampleId)
                }
            }

            soundPool = pool

            soundIds[SoundEffect.CRYPTO_LAUNCH] = pool.load(appContext, R.raw.sfx_crypto_launch, 1)
            soundIds[SoundEffect.TECH_CLICK] = pool.load(appContext, R.raw.sfx_tech_click, 1)
            soundIds[SoundEffect.SWITCH_TOGGLE] = pool.load(appContext, R.raw.sfx_switch_toggle, 1)
            soundIds[SoundEffect.SUCCESS_CHIME] = pool.load(appContext, R.raw.sfx_success_chime, 1)
            soundIds[SoundEffect.SELECTION_POP] = pool.load(appContext, R.raw.sfx_coin_pop, 1)

            isInitialized = true
        } catch (_: Throwable) {
            // Silently fallback without crashing
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
        sharedPreferences?.edit()?.putBoolean(KEY_SOUND_ENABLED, enabled)?.apply()
    }

    fun play(effect: SoundEffect, volume: Float = 0.85f) {
        if (!_isSoundEnabled.value) return
        try {
            ensureSoundPool()
            val soundId = soundIds[effect] ?: return
            val pool = soundPool ?: return
            pool.play(soundId, volume, volume, 1, 0, 1.0f)
        } catch (_: Throwable) {
            // Ignored safely
        }
    }

    /**
     * Synchronized playback for the launch screen
     */
    fun playLaunchAmbient(context: Context? = null) {
        if (!_isSoundEnabled.value) return
        play(SoundEffect.CRYPTO_LAUNCH, 0.90f)
    }

    fun playTechClick() = play(SoundEffect.TECH_CLICK, 0.65f)
    fun playSwitchToggle(on: Boolean = true) = play(SoundEffect.SWITCH_TOGGLE, 0.70f)
    fun playSuccessChime() = play(SoundEffect.SUCCESS_CHIME, 0.85f)
    fun playSelectionPop() = play(SoundEffect.SELECTION_POP, 0.70f)
}
