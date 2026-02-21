package com.GR8Studios.souc

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.GR8Studios.souc.data.AppDefaults
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult

@Composable
fun SubscriptionScreen(onDismiss: () -> Unit, onPurchaseSuccess: () -> Unit) {
    var showContent by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (showContent) 1f else 0.92f, tween(420), label = "paywallScale")
    val billingHelper = rememberBillingClientHelper()

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
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF171D36))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(painterResource(R.drawable.app_logo), "SOUC", modifier = Modifier.size(88.dp))
                    Text("Unlock SOUC Premium", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text(
                        "Free: ${AppDefaults.FREE_POST_LIMIT} posts/month · Premium: unlimited scheduling",
                        color = Color(0xFFAAB3D1),
                        fontSize = 13.sp
                    )
                    BenefitRow("Unlimited posts across YouTube, Instagram, Facebook")
                    BenefitRow("Priority publishing queue and advanced analytics")
                    BenefitRow("Admin-grade automation controls")

                    Button(
                        onClick = {
                            billingHelper.launchBillingFlow()
                            onPurchaseSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF32A6)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Upgrade now", fontWeight = FontWeight.SemiBold) }

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
private fun BenefitRow(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1FD0C0), modifier = Modifier.size(18.dp))
        Text(text, color = Color(0xFFD5D9F0), fontSize = 13.sp)
    }
}

private class BillingClientHelper(private val billingClient: BillingClient) {
    fun launchBillingFlow() {
        // Wrapper scaffold: query products + launch billing flow can be implemented next.
    }

    fun close() {
        billingClient.endConnection()
    }
}

@Composable
private fun rememberBillingClientHelper(): BillingClientHelper {
    val context = LocalContext.current
    val activity = context as? Activity
    val helper = remember {
        val client = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener { _: BillingResult, _ -> }
            .build()
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) = Unit
            override fun onBillingServiceDisconnected() = Unit
        })
        BillingClientHelper(client)
    }
    androidx.compose.runtime.DisposableEffect(activity) {
        onDispose { helper.close() }
    }
    return helper
}
