package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.GoogleAuthHelper
import com.example.util.GoogleSignInOutcome
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    isLoadingExternal: Boolean = false,
    onSignedIn: (FirebaseUser) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RoyalNavyDark, LudoClubPurpleDark, RoyalNavy)
                )
            )
            .systemBarsPadding()
            .padding(24.dp)
            .testTag("google_sign_in_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(RoyalBlueCard.copy(alpha = 0.94f), RoyalNavy.copy(alpha = 0.96f))
                    )
                )
                .border(1.5.dp, RoyalGold.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(RoyalGold, RoyalGoldDark))
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("👑", fontSize = 40.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MG LUDO",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = RoyalGold,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Sign in to sync your verified tasks, Voice Clubs, friends & rewards.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextMuted
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(RoyalNavyDark.copy(alpha = 0.55f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Verified Progress Security",
                    tint = LudoClubPlusGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Server-verified rewards, cloud rooms & club voice access.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.9f))
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFF8A80)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RubyRedDark.copy(alpha = 0.45f))
                        .padding(10.dp)
                        .testTag("sign_in_error_text")
                )
            }

            val busy = isSigningIn || isLoadingExternal
            Button(
                onClick = {
                    if (activity == null) {
                        errorMessage = "Activity context unavailable for Google Sign-In."
                        return@Button
                    }
                    scope.launch {
                        isSigningIn = true
                        errorMessage = null
                        when (val outcome = GoogleAuthHelper.signInInteractive(activity)) {
                            is GoogleSignInOutcome.Success -> {
                                onSignedIn(outcome.user)
                            }
                            is GoogleSignInOutcome.Error -> {
                                errorMessage = outcome.message
                            }
                        }
                        isSigningIn = false
                    }
                },
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("google_sign_in_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalGold,
                    contentColor = RoyalNavyDark
                )
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = RoyalNavyDark
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Signing In...", fontWeight = FontWeight.Bold)
                } else {
                    Text("Continue with Google", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
            }
        }
    }
}
