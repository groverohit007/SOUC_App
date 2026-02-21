package com.GR8Studios.souc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun SubscriptionScreen(onDismiss: () -> Unit, onPurchaseSuccess: () -> Unit) {
    var showContent by remember { mutableStateOf(false) }
    var priceText by remember { mutableStateOf("$4.99/month") }

    val spin by rememberInfiniteTransition(label = "spin").animateFloat(
        0f,
        360f,
        infiniteRepeatable(animation = tween(6000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Restart),
        label = "heroSpin"
    )
    val scale by animateFloatAsState(if (showContent) 1f else 0.92f, tween(420), label = "paywallScale")
    val billingHelper = rememberBillingClientHelper()

    DisposableEffect(Unit) {
        val reg = FirebaseFirestore.getInstance().collection("app_settings").document("config")
            .addSnapshotListener { snap, _ ->
                priceText = snap?.getString("premium_price_display") ?: "$4.99/month"
            }
        onDispose { reg.remove() }
    }

    LaunchedEffect(Unit) { showContent = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
            .padding(20.dp)
    ) {
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(350)) + slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(350)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().scale(scale),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF171D36))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .rotate(spin)
                            .background(
                                brush = Brush.sweepGradient(GradientBrand + GradientBrand.first()),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AllInclusive, contentDescription = null, tint = Color.White, modifier = Modifier.size(46.dp))
                    }

                    Text("Unlock SOUC Pro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PlanColumn("Free Limit Reached", Color(0xFF8A93AC), "Limited posts/month")
                        PlanColumn("SOUC Pro", Color(0xFFEF32A6), "Unlimited scheduling")
                    }

                    BenefitRow("Unlimited posts across YouTube, Instagram, Facebook")
                    BenefitRow("Priority queue + pro automation")
                    BenefitRow("Advanced analytics and growth controls")

                    Button(
                        onClick = {
                            billingHelper.launchBillingFlow()
                            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                                FirebaseFirestore.getInstance().collection("users").document(uid)
                                    .set(mapOf("tier" to "PREMIUM"), SetOptions.merge())
                            }
                            onPurchaseSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(GradientBrand), RoundedCornerShape(14.dp))
                    ) { Text("Upgrade now • $priceText", fontWeight = FontWeight.SemiBold) }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283150)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Maybe later") }
                }
            }
        }
    }
}

@Composable
private fun PlanColumn(title: String, accent: Color, desc: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF232D4A)), modifier = Modifier.weight(1f)) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(desc, color = Color(0xFFC8D2EE), fontSize = 12.sp)
        }
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1FD0C0), modifier = Modifier.size(18.dp))
        Text(text, color = Color(0xFFD5D9F0), fontSize = 13.sp)
    }
}

private class BillingClientHelper(private val billingClient: BillingClient) {
    fun launchBillingFlow() {
        // Billing wrapper scaffold for product query + launch flow.
    }

    fun close() {
        billingClient.endConnection()
    }
}

@Composable
private fun rememberBillingClientHelper(): BillingClientHelper {
    val helper = remember {
        val client = BillingClient.newBuilder(androidx.compose.ui.platform.LocalContext.current)
            .enablePendingPurchases()
            .setListener { _: BillingResult, _ -> }
            .build()
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) = Unit
            override fun onBillingServiceDisconnected() = Unit
        })
        BillingClientHelper(client)
    }
    DisposableEffect(Unit) {
        onDispose { helper.close() }
    }
    return helper
}
