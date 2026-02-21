package com.GR8Studios.souc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun LoginScreenContent(
    loading: Boolean,
    error: String?,
    onGoogleTap: () -> Unit
) {
    val pulse = rememberInfiniteTransition(label = "logoPulse").animateFloat(
        initialValue = 0.88f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "alpha"
    )
    val drift = rememberInfiniteTransition(label = "orbs")
    val orbOffsetA by drift.animateFloat(-20f, 22f, infiniteRepeatable(tween(4200), RepeatMode.Reverse), label = "orbA")
    val orbOffsetB by drift.animateFloat(18f, -24f, infiniteRepeatable(tween(5000), RepeatMode.Reverse), label = "orbB")

    val enterStates = remember { mutableStateListOf(false, false, false, false) }
    LaunchedEffect(Unit) {
        enterStates.indices.forEach {
            delay(90)
            enterStates[it] = true
        }
    }

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val btnScale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "btnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom))),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .offset(x = (-90).dp, y = (orbOffsetA).dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0x66E91E63), Color.Transparent)))
        )
        Box(
            Modifier
                .offset(x = 100.dp, y = (orbOffsetB).dp)
                .size(260.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0x559C27B0), Color.Transparent)))
        )

        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(enterStates[0], enter = fadeIn(tween(420)) + slideInVertically(initialOffsetY = { it / 3 })) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = "SOUC logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .alpha(pulse.value)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(enterStates[1], enter = fadeIn(tween(420)) + slideInVertically(initialOffsetY = { it / 3 })) {
                Text("SOUC", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            }
            AnimatedVisibility(enterStates[2], enter = fadeIn(tween(420)) + slideInVertically(initialOffsetY = { it / 3 })) {
                Text("Schedule once. Post everywhere.", color = Color(0xFFAAB3D1), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(28.dp))

            AnimatedVisibility(enterStates[3], enter = fadeIn(tween(420)) + slideInVertically(initialOffsetY = { it / 3 })) {
                Button(
                    onClick = onGoogleTap,
                    enabled = !loading,
                    interactionSource = interaction,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .scale(btnScale)
                        .clip(RoundedCornerShape(999.dp))
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Continue with Google", color = Color.Black, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            if (!error.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(error, color = Color(0xFFFF8E8E), fontSize = 12.sp)
            }
        }
    }
}
