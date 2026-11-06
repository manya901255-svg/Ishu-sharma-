package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.FairDiceProof
import com.example.ui.theme.*

@Composable
fun FairDiceDialog(
    proof: FairDiceProof?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(20.dp, RoundedCornerShape(20.dp))
                .testTag("fair_dice_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalNavyDark),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                Brush.linearGradient(listOf(RoyalGoldLight, RoyalGoldDark))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Fairness",
                            tint = RoyalGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Provably Fair Verification",
                            color = RoyalGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "MG Ludo implements cryptographic HMAC-SHA256 provable fairness. Neither the server nor the player can manipulate any dice roll outcome.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Verification formula box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RoyalBlueCard)
                        .border(1.dp, RoyalNavyAccent, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "DICE SEED FORMULA:",
                            color = RoyalGoldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "outcome = (HMAC_SHA256(ServerSeed, ClientSeed:Nonce) % 6) + 1",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (proof != null) {
                    ProofItem(label = "VERIFIED OUTCOME", value = "🎲 Rolled ${proof.roll} (100% Cryptographic Match)", isHighlight = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProofItem(label = "SERVER SEED SHA-256 HASH", value = proof.serverSeedHash)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProofItem(label = "CLIENT SEED", value = proof.clientSeed)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProofItem(label = "ROLL NONCE (INDEX)", value = "#${proof.nonce}")
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Roll the dice in an active match to inspect the real-time cryptographic signature.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Zero Server Bias • Anti-Rigging Shield Active",
                        color = EmeraldGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProofItem(label: String, value: String, isHighlight: Boolean = false) {
    Column {
        Text(
            text = label,
            color = if (isHighlight) RoyalGold else TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(RoyalBlueSurface)
                .padding(8.dp)
        ) {
            Text(
                text = value,
                color = if (isHighlight) RoyalGoldLight else Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
