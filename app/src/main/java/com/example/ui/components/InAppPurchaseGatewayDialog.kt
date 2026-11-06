package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PaymentMethod(val title: String, val icon: String, val subtitle: String) {
    GOOGLE_PLAY("Google Play Billing", "▶️", "1-Tap fast checkout with Google Play"),
    UPI("UPI (GPay / PhonePe / Paytm)", "⚡", "Instant transfer via UPI Apps"),
    CARDS("Debit / Credit Card", "💳", "Visa, MasterCard, RuPay"),
    NET_BANKING("Net Banking", "🏦", "All major Indian banks supported")
}

data class InAppPurchaseProduct(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val priceFormatted: String,
    val coinsReward: Long = 0L,
    val gemsReward: Int = 0,
    val unlocksNoAds: Boolean = false,
    val voiceDaysReward: Int = 0
)

/**
 * InAppPurchaseGatewayDialog:
 * Comprehensive real-money purchase gateway simulation matching modern mobile game standards:
 * - Order summary with ₹ price, GST included, and encrypted transaction security
 * - Payment method selector (Google Play, UPI, Cards, Netbanking)
 * - 1-Tap "Authorize & Pay" processing animation
 * - Instant in-game credit of Coins, Cash/Gems, No Ads, and Voice Chat Passes
 */
@Composable
fun InAppPurchaseGatewayDialog(
    product: InAppPurchaseProduct,
    onPurchaseCompleted: (InAppPurchaseProduct) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.GOOGLE_PLAY) }
    var isProcessing by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val orderId = remember { "ORD-${System.currentTimeMillis().toString().takeLast(8)}" }

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("in_app_purchase_gateway_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B35)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Brush.linearGradient(listOf(RoyalGold, Color(0xFF1E88E5))))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header: Security Badge & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF00E676), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Secure",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "256-Bit Encrypted Checkout",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Order #$orderId",
                                color = Color(0xFF90CAF9),
                                fontSize = 9.sp
                            )
                        }
                    }

                    if (!isProcessing && !isSuccess) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isSuccess) {
                    // Success View
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00C853)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(36.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Purchase Successful! 🎉",
                            color = RoyalGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Credited ${product.title} to your royal account.",
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onPurchaseCompleted(product)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done", color = Color(0xFF3E2723), fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                    }
                } else if (isProcessing) {
                    // Processing Loading View
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 28.dp)
                    ) {
                        CircularProgressIndicator(
                            color = RoyalGold,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Authorizing with Banking Gateway...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Please do not close or press back",
                            color = Color(0xFF90CAF9),
                            fontSize = 11.sp
                        )
                    }
                } else {
                    // Product Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF162547)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF284070))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(product.iconEmoji, fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.title,
                                    color = Color.White,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = product.description,
                                    color = RoyalGoldLight,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = product.priceFormatted,
                                color = Color(0xFF00E676),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "SELECT PAYMENT METHOD",
                        color = Color(0xFF90CAF9),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Payment Method Options
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PaymentMethod.values().forEach { method ->
                            val isSelected = selectedMethod == method
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF1E3A70) else Color(0xFF131F3A))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) RoyalGold else Color(0xFF22355A),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedMethod = method }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(method.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = method.title,
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = method.subtitle,
                                        color = Color(0xFF88A0C7),
                                        fontSize = 9.5.sp
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedMethod = method },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = RoyalGold,
                                        unselectedColor = Color(0xFF5C729A)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1-Tap Authorize & Pay Button
                    Button(
                        onClick = {
                            isProcessing = true
                            coroutineScope.launch {
                                delay(1600) // Realistic gateway handshake
                                isProcessing = false
                                isSuccess = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(8.dp, RoundedCornerShape(14.dp))
                            .testTag("in_app_pay_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E676)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Color(0xFF003300), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Authorize & Pay ${product.priceFormatted}",
                                color = Color(0xFF003300),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Includes 18% GST • Real-Money In-App Purchase Simulation",
                        color = Color(0xFF88A0C7),
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
