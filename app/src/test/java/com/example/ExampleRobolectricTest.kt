package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MG Ludo", appName)
  }

  @Test
  fun `save and retrieve profile preferences`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.util.ProfilePreferences(context)
    prefs.saveFullProfile("Maharaja Veer", "crown_kohinoor_diamond", "Shahenshah")

    assertEquals("Maharaja Veer", prefs.getNickname())
    assertEquals("crown_kohinoor_diamond", prefs.getCrownAvatar())
    assertEquals("Shahenshah", prefs.getRoyalTitle())
  }

  @Test
  fun `sendChatMessage adds message with player color and enforces 100 char limit`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.viewmodel.LudoViewModel(application)
    viewModel.startNewGame(com.example.model.GameMode.VS_AI)

    val longMessage = "A".repeat(150)
    viewModel.sendChatMessage(longMessage)

    val messages = viewModel.uiState.value.chatMessages
    org.junit.Assert.assertTrue("Messages should not be empty", messages.isNotEmpty())
    val lastMsg = messages.last()
    org.junit.Assert.assertTrue("Length should be at most 100", lastMsg.text.length <= 100)
    org.junit.Assert.assertNotNull("Sender color should be assigned", lastMsg.senderColor)
  }

  @Test
  fun `toggleChatDialog clears unread chat count`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.viewmodel.LudoViewModel(application)
    viewModel.startNewGame(com.example.model.GameMode.VS_AI)

    viewModel.toggleChatDialog(true)
    assertEquals(0, viewModel.uiState.value.unreadChatCount)
    assertEquals(true, viewModel.uiState.value.showChatDialog)

    viewModel.toggleChatDialog(false)
    assertEquals(false, viewModel.uiState.value.showChatDialog)
  }

  @Test
  fun `bgm playlist contains songs with unique ids and valid metadata`() {
    val playlist = com.example.model.BGM_PLAYLIST
    org.junit.Assert.assertTrue("Playlist must not be empty", playlist.isNotEmpty())

    val ids = playlist.map { it.id }.toSet()
    assertEquals("All track IDs must be unique", playlist.size, ids.size)

    playlist.forEach { track ->
      org.junit.Assert.assertTrue("Title must not be blank for ${track.id}", track.title.isNotBlank())
      org.junit.Assert.assertTrue("Singer must not be blank for ${track.id}", track.singer.isNotBlank())
    }
  }

  @Test
  fun `test RoyalSoundManager track playback`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val soundManager = com.example.util.RoyalSoundManager(context)
    soundManager.setBgmEnabled(true)
    soundManager.selectTrack(0)
    org.junit.Assert.assertEquals(0, soundManager.currentTrackIndex)
  }

  @Test
  fun `verify classic ludo quadrant and start positions`() {
    assertEquals(1, com.example.model.LudoColor.RED.startTrackPos)
    assertEquals(14, com.example.model.LudoColor.GREEN.startTrackPos)
    assertEquals(27, com.example.model.LudoColor.BLUE.startTrackPos)
    assertEquals(40, com.example.model.LudoColor.YELLOW.startTrackPos)

    val redBase0 = com.example.ui.components.StandardLudoBoardLayout.getBasePocketFloatPosition(com.example.model.LudoColor.RED, 0)
    assertEquals(1.75f, redBase0.first, 0.01f)
    assertEquals(1.75f, redBase0.second, 0.01f)

    val greenBase0 = com.example.ui.components.StandardLudoBoardLayout.getBasePocketFloatPosition(com.example.model.LudoColor.GREEN, 0)
    assertEquals(10.75f, greenBase0.first, 0.01f)
    assertEquals(1.75f, greenBase0.second, 0.01f)
  }
}
