package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.*
import com.example.ui.components.WallpaperBackgroundView
import com.example.ui.theme.*
import com.example.viewmodel.LudoUiState
import com.example.viewmodel.LudoViewModel

@Composable
fun VoiceClubScreen(
    uiState: LudoUiState,
    viewModel: LudoViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Public Clubs, 1 = My Clubs
    var showCreateDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var joinPrivateTarget by remember { mutableStateOf<VoiceClubItem?>(null) }
    var joinCodeInput by remember { mutableStateOf("") }
    var newRoomTitle by remember { mutableStateOf("") }
    var showCreateRoomDialog by remember { mutableStateOf(false) }

    val activeClub = uiState.activeClub
    val currentUid = uiState.authUserId ?: uiState.inventoryUiState.playerId

    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.onRecordPermissionGranted()
            if (uiState.isMicMuted) {
                viewModel.toggleMic()
            }
        }
    }

    fun handleMicToggleRequest() {
        if (!uiState.isMicMuted) {
            viewModel.toggleMic()
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.toggleMic()
        } else {
            recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    BackHandler {
        if (activeClub != null) {
            viewModel.selectActiveClub(null)
        } else {
            onBack()
        }
    }

    WallpaperBackgroundView(
        wallpaperIdOrPath = uiState.activeWallpapers[WallpaperTarget.HOME.id],
        target = WallpaperTarget.HOME
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("voice_club_screen")
        ) {
            // Top Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                tonalElevation = 4.dp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (activeClub != null) viewModel.selectActiveClub(null) else onBack()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("voice_club_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeClub?.name ?: "MG Voice Clubs",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (activeClub != null) {
                                "${activeClub.memberCount} members • ${if (activeClub.isPrivate) "Private Code: ${activeClub.inviteCode}" else "Public Club"}"
                            } else {
                                "Create clubs, host live voice rooms & squad up"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (activeClub == null) {
                        Button(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("create_club_open_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Create Club", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("New Club", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(
                            onClick = { showInviteDialog = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("club_invite_friend_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Invite Friends",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Pending Club Invitations Banner
            AnimatedVisibility(visible = activeClub == null && uiState.clubInvitations.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.clubInvitations.forEach { invite ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f),
                            border = BorderStroke(1.dp, RoyalGoldBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Club Invitation: ${invite.clubName}",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Invited by ${invite.fromName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                TextButton(
                                    onClick = { viewModel.respondToClubInvite(invite, accept = false) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Text("Decline")
                                }
                                Button(
                                    onClick = { viewModel.respondToClubInvite(invite, accept = true) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Text("Accept")
                                }
                            }
                        }
                    }
                }
            }

            if (activeClub == null) {
                // Club Discovery List View
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Public Clubs (${uiState.publicClubs.size})") },
                        modifier = Modifier.testTag("tab_public_clubs")
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("My Clubs (${uiState.myClubs.size})") },
                        modifier = Modifier.testTag("tab_my_clubs")
                    )
                }

                val list = if (selectedTab == 0) uiState.publicClubs else uiState.myClubs
                if (list.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("🎙️", fontSize = 40.sp)
                                Text(
                                    text = if (selectedTab == 0) "No Public Voice Clubs Yet" else "You Haven't Joined a Club",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Create your own MG Ludo Voice Club with public or invite-only voice rooms, roles, and live team chat.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { showCreateDialog = true },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Text("Create Voice Club")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(list, key = { it.clubId }) { club ->
                            val isMember = currentUid in club.memberUids
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isMember || !club.isPrivate) {
                                            viewModel.selectActiveClub(club)
                                        } else {
                                            joinPrivateTarget = club
                                        }
                                    }
                                    .testTag("club_card_${club.clubId}"),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                                ),
                                border = BorderStroke(1.dp, RoyalGoldBorder.copy(alpha = 0.55f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(listOf(RoyalGold, RoyalBluePrimary))
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(club.avatarEmoji.ifBlank { "👑" }, fontSize = 26.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = club.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (club.isPrivate) RoyalRedAccent.copy(alpha = 0.2f) else EmeraldGreen.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = if (club.isPrivate) "PRIVATE" else "PUBLIC",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (club.isPrivate) RoyalRedAccent else EmeraldGreen,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = club.description.ifBlank { "Owner: ${club.ownerName}" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "👥 ${club.memberCount} Members • ⭐ ${club.clubXp} Club XP",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    if (isMember) {
                                        OutlinedButton(
                                            onClick = { viewModel.selectActiveClub(club) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Text("Open")
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                if (club.isPrivate) {
                                                    joinPrivateTarget = club
                                                } else {
                                                    viewModel.joinVoiceClub(club)
                                                }
                                            },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Text("Join")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Active Club Workspace
                ActiveVoiceClubWorkspace(
                    club = activeClub,
                    uiState = uiState,
                    currentUid = currentUid,
                    viewModel = viewModel,
                    onRequestMicToggle = ::handleMicToggleRequest,
                    onOpenCreateRoom = { showCreateRoomDialog = true }
                )
            }
        }
    }

    // Create Club Dialog
    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var emoji by remember { mutableStateOf("👑") }
        var isPrivate by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Voice Club", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Club Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("club_name_input")
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Club Description") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("club_desc_input")
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("👑", "🎙️", "🎲", "⚔️", "🦁", "💎", "🔥").forEach { option ->
                            FilterChip(
                                selected = emoji == option,
                                onClick = { emoji = option },
                                label = { Text(option, fontSize = 18.sp) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Private Club", fontWeight = FontWeight.SemiBold)
                            Text("Requires invite or room code to join", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = name.isNotBlank(),
                    onClick = {
                        viewModel.createVoiceClub(name, desc, emoji, isPrivate)
                        showCreateDialog = false
                    },
                    modifier = Modifier.testTag("confirm_create_club_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Join Private Club with Code
    if (joinPrivateTarget != null) {
        AlertDialog(
            onDismissRequest = { joinPrivateTarget = null },
            title = { Text("Join ${joinPrivateTarget?.name}") },
            text = {
                OutlinedTextField(
                    value = joinCodeInput,
                    onValueChange = { joinCodeInput = it.uppercase() },
                    label = { Text("Invite Code (e.g. MG1234)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val target = joinPrivateTarget
                    if (target != null) {
                        viewModel.joinVoiceClub(target, joinCodeInput.trim())
                    }
                    joinCodeInput = ""
                    joinPrivateTarget = null
                }) {
                    Text("Join Club")
                }
            },
            dismissButton = {
                TextButton(onClick = { joinPrivateTarget = null }) { Text("Cancel") }
            }
        )
    }

    // Create Voice Room Dialog
    if (showCreateRoomDialog && activeClub != null) {
        AlertDialog(
            onDismissRequest = { showCreateRoomDialog = false },
            title = { Text("Open Live Voice Room") },
            text = {
                OutlinedTextField(
                    value = newRoomTitle,
                    onValueChange = { newRoomTitle = it },
                    label = { Text("Room Title (e.g. Royal Chill Lounge)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.createClubVoiceRoom(activeClub, newRoomTitle.ifBlank { "Royal Voice Room" })
                    newRoomTitle = ""
                    showCreateRoomDialog = false
                }) {
                    Text("Start Room")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRoomDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Invite Friend to Club Dialog
    if (showInviteDialog && activeClub != null) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invite Friends to ${activeClub.name}") },
            text = {
                if (uiState.friends.isEmpty()) {
                    Text("Add friends from the Leaderboard or Friends Hub to invite them directly to your club.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.friends.forEach { friend ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(friend.friendName, fontWeight = FontWeight.SemiBold)
                                Button(
                                    onClick = {
                                        viewModel.inviteFriendToClub(activeClub, friend.friendUid)
                                        showInviteDialog = false
                                    }
                                ) {
                                    Text("Invite")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInviteDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun ActiveVoiceClubWorkspace(
    club: VoiceClubItem,
    uiState: LudoUiState,
    currentUid: String,
    viewModel: LudoViewModel,
    onRequestMicToggle: () -> Unit,
    onOpenCreateRoom: () -> Unit
) {
    var innerTab by remember { mutableStateOf(0) } // 0 = Voice & Chat, 1 = Members
    var chatInput by remember { mutableStateOf("") }
    var sendAsAnnouncement by remember { mutableStateOf(false) }

    val myMember = uiState.clubMembers.firstOrNull { it.uid == currentUid }
    val myRole = myMember?.role ?: if (club.ownerId == currentUid) VoiceClubRole.OWNER else VoiceClubRole.MEMBER
    val canModerate = myRole.canModerate()

    Column(modifier = Modifier.fillMaxSize()) {
        // Club Announcement Banner
        if (club.announcement.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, RoyalGoldBorder.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = "Announcement", tint = RoyalGold)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = club.announcement,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Live Voice Stage Controls
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, RoyalGoldBorder.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.activeClubVoiceRoomId != null) "🎙️ Live Voice Room Connected" else "Voice Rooms (${uiState.clubVoiceRooms.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val activeSpeakers = uiState.voiceParticipants.values.filter { it.isSpeaking }
                        Text(
                            text = if (activeSpeakers.isNotEmpty()) {
                                "Speaking: ${activeSpeakers.joinToString { it.displayName }}"
                            } else {
                                "Mic ${if (uiState.isMicMuted) "Muted" else "Live"} • Speaker ${if (uiState.isSpeakerMuted) "Off" else "On"}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalIconButton(
                            onClick = onRequestMicToggle,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("club_mic_toggle")
                        ) {
                            Icon(
                                imageVector = if (uiState.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = if (uiState.isMicMuted) "Unmute Microphone" else "Mute Microphone",
                                tint = if (uiState.isMicMuted) RoyalRedAccent else EmeraldGreen
                            )
                        }
                        FilledTonalIconButton(
                            onClick = { viewModel.toggleSpeaker() },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("club_speaker_toggle")
                        ) {
                            Icon(
                                imageVector = if (uiState.isSpeakerMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Toggle Speaker"
                            )
                        }
                        if (uiState.activeClubVoiceRoomId == null) {
                            Button(
                                onClick = onOpenCreateRoom,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text("Host Room")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.leaveClubVoiceRoom() },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text("Leave")
                            }
                        }
                    }
                }

                if (uiState.clubVoiceRooms.isNotEmpty() && uiState.activeClubVoiceRoomId == null) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.clubVoiceRooms.forEach { room ->
                            AssistChip(
                                onClick = { viewModel.joinClubVoiceRoom(room.roomId) },
                                label = { Text("🎙️ Join ${room.title}") }
                            )
                        }
                    }
                }
            }
        }

        // Workspace Tabs
        TabRow(selectedTabIndex = innerTab, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            Tab(selected = innerTab == 0, onClick = { innerTab = 0 }, text = { Text("Club Chat (${uiState.clubMessages.size})") })
            Tab(selected = innerTab == 1, onClick = { innerTab = 1 }, text = { Text("Members (${uiState.clubMembers.size})") })
        }

        if (innerTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.clubMessages, key = { it.messageId }) { msg ->
                    val isMe = msg.senderUid == currentUid
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = when {
                            msg.isAnnouncement -> RoyalGold.copy(alpha = 0.2f)
                            isMe -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (msg.isAnnouncement) RoyalGoldBorder else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (msg.isAnnouncement) "📣 ${msg.senderName} (Announcement)" else msg.senderName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (msg.isAnnouncement) RoyalGold else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Emoji Quick Bar + Message Composer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("👑", "🎲", "🔥", "🎉", "🤝", "😂", "👏", "🏆").forEach { emoji ->
                    Surface(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clickable { viewModel.sendClubChatMessage(club, emoji) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(emoji, fontSize = 18.sp)
                        }
                    }
                }
            }

            if (canModerate) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = sendAsAnnouncement, onCheckedChange = { sendAsAnnouncement = it })
                    Text("Pin as Club Announcement", style = MaterialTheme.typography.labelMedium)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Message ${club.name}...") },
                    modifier = Modifier.weight(1f).testTag("club_chat_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                FilledIconButton(
                    onClick = {
                        if (chatInput.isNotBlank()) {
                            viewModel.sendClubChatMessage(club, chatInput.trim(), isAnnouncement = sendAsAnnouncement)
                            chatInput = ""
                            sendAsAnnouncement = false
                        }
                    },
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("club_chat_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Club Message")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.clubMembers, key = { it.uid }) { member ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (member.isOnline) EmeraldGreen else Color.Gray)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.displayName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${member.role.badge} • ${if (member.isOnline) "Online" else "Offline"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (canModerate && member.uid != currentUid && member.role != VoiceClubRole.OWNER) {
                                IconButton(
                                    onClick = {
                                        val nextRole = if (member.role == VoiceClubRole.MEMBER) VoiceClubRole.MODERATOR else VoiceClubRole.MEMBER
                                        viewModel.updateClubMemberRole(club.clubId, member.uid, nextRole)
                                    },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Toggle Moderator Role")
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.kickAndBlockClubMember(club, member.uid, "Moderation removal")
                                    },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = "Kick and Block", tint = RoyalRedAccent)
                                }
                            }
                            if (member.uid != currentUid) {
                                IconButton(
                                    onClick = {
                                        viewModel.submitAbuseReport(member.uid, "VOICE_CLUB", club.clubId, "Abusive conduct in Voice Club")
                                    },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.Flag, contentDescription = "Report Member")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
