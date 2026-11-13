package com.example.util

import android.content.Context
import kotlinx.coroutines.CoroutineScope

/**
 * SoundAndHaptics: Unified interface coordinating haptic feedback and sound effects
 * powered by the dedicated SoundEffectManager.
 */
class SoundAndHaptics(context: Context) {

    val soundEffectManager = SoundEffectManager(context)
    val soundManager = RoyalSoundManager(context)

    fun vibrateDiceRoll() {
        soundEffectManager.playDiceRoll()
    }

    fun playDiceRoll() {
        soundEffectManager.playDiceRoll()
    }

    fun playDiceRoll(soundEffect: com.example.model.DiceSoundEffect) {
        soundEffectManager.playDiceRollForTheme(soundEffect)
    }

    fun vibratePawnCapture() {
        soundEffectManager.playPawnCapture()
    }

    fun playPawnCapture() {
        soundEffectManager.playPawnCapture()
    }

    fun vibrateVictory() {
        soundEffectManager.playVictory()
    }

    fun playVictory() {
        soundEffectManager.playVictory()
    }

    fun vibrateClick() {
        soundEffectManager.playButtonClick()
    }

    fun playButtonClick() {
        soundEffectManager.playButtonClick()
    }

    fun playPawnMove(stepIndex: Int = 1, totalSteps: Int = 1, isHomeStretch: Boolean = false) {
        soundEffectManager.playPawnMove(stepIndex, totalSteps, isHomeStretch)
    }

    fun playPawnStepsSequence(steps: Int, scope: CoroutineScope, isHomeStretch: Boolean = false, onStep: ((Int) -> Unit)? = null) {
        soundEffectManager.playPawnStepsSequence(steps, scope, isHomeStretch, onStep)
    }

    fun playSafeTile() {
        soundEffectManager.playSafeTile()
    }

    fun playStarSafe() {
        soundEffectManager.playSafeTile()
    }

    fun playSixRolled() {
        soundEffectManager.playSixRolled()
    }

    fun playTurnNotification() {
        soundEffectManager.playTurnNotification()
    }

    fun playPawnHome() {
        soundEffectManager.playPawnHome()
    }

    fun playCoinReward() {
        soundEffectManager.playCoinReward()
    }

    fun playInvalidMove() {
        soundEffectManager.playInvalidMove()
    }

    fun setSfxPreset(preset: String) {
        soundEffectManager.setSfxPreset(preset)
        soundManager.setSfxPreset(preset)
    }

    fun setGameActive(active: Boolean) {
        soundEffectManager.isGameActive = active
        soundManager.setGameActive(active)
    }

    fun playTrack(track: com.example.model.BgmTrack, playlist: List<com.example.model.BgmTrack> = emptyList()) {
        soundManager.playTrack(track, playlist)
    }

    fun seekTo(positionMs: Int) {
        soundManager.seekTo(positionMs)
    }

    fun getCurrentPosition(): Int = soundManager.getCurrentPosition()
    fun getDuration(): Int = soundManager.getDuration()
    fun isBuffering(): Boolean = soundManager.isBuffering

    fun getSfxPreset(): String = soundEffectManager.preset

    fun release() {
        soundEffectManager.release()
        soundManager.release()
    }
}
