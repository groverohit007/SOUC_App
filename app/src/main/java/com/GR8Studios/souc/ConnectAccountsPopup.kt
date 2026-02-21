package com.GR8Studios.souc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class SocialPlatform(val title: String, val icon: ImageVector, val activeColor: Color) {
    YouTube("YouTube", Icons.Default.PlayCircle, Color(0xFFFF3D3D)),
    Instagram("Instagram", Icons.Default.CameraAlt, Color(0xFFE1306C)),
    Facebook("Facebook", Icons.Default.Facebook, Color(0xFF1877F2))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectAccountsPopup(
    youtubeConnected: Boolean,
    instagramConnected: Boolean,
    facebookConnected: Boolean,
    youtubeLoading: Boolean,
    instagramLoading: Boolean,
    facebookLoading: Boolean,
    continueEnabled: Boolean,
    onConnectPlatform: (SocialPlatform) -> Unit,
    onDisconnectPlatform: (SocialPlatform) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cardVisibility = remember { mutableStateListOf(false, false, false) }
    var animatePopupIn by remember { mutableStateOf(false) }
    var managePlatform by remember { mutableStateOf<SocialPlatform?>(null) }

    LaunchedEffect(Unit) {
        animatePopupIn = true
        cardVisibility.indices.forEach { index ->
            delay(80L * index)
            cardVisibility[index] = true
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        scrimColor = Color.Black.copy(alpha = 0.56f),
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GradientBrand[1].copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(2.dp)
        ) {
            AnimatedVisibility(
                visible = animatePopupIn,
                enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.White, Color(0xFFF3F5F9))
                                )
                            )
                            .padding(22.dp)
                    ) {
                        Text(
                            text = "Connect your socials",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Connect accounts to schedule posts and reels.",
                            color = Color(0xFF52607A),
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SocialPlatform.entries.forEachIndexed { index, platform ->
                                val connected = when (platform) {
                                    SocialPlatform.YouTube -> youtubeConnected
                                    SocialPlatform.Instagram -> instagramConnected
                                    SocialPlatform.Facebook -> facebookConnected
                                }
                                val loading = when (platform) {
                                    SocialPlatform.YouTube -> youtubeLoading
                                    SocialPlatform.Instagram -> instagramLoading
                                    SocialPlatform.Facebook -> facebookLoading
                                }

                                AnimatedVisibility(
                                    visible = cardVisibility[index],
                                    enter = fadeIn(tween(280, delayMillis = index * 70)) +
                                            slideInVertically(
                                                initialOffsetY = { it / 2 },
                                                animationSpec = tween(280, delayMillis = index * 70)
                                            )
                                ) {
                                    Platform3DCard(
                                        platform = platform,
                                        connected = connected,
                                        loading = loading,
                                        modifier = Modifier.weight(1f),
                                        onTap = {
                                            if (connected) {
                                                managePlatform = platform
                                            } else if (!loading) {
                                                onConnectPlatform(platform)
                                            }
                                        }
                                    )
                                }

                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        val continueColor by animateColorAsState(
                            targetValue = if (continueEnabled) GradientBrand[0] else Color(0xFFD9E0EB),
                            animationSpec = tween(260),
                            label = "continue_color"
                        )
                        val continueAlpha by animateFloatAsState(
                            targetValue = if (continueEnabled) 1f else 0.7f,
                            animationSpec = tween(260),
                            label = "continue_alpha"
                        )

                        Box {
                            if (continueEnabled) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    GradientBrand[0].copy(alpha = 0.22f),
                                                    GradientBrand[2].copy(alpha = 0.22f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(999.dp)
                                        )
                                )
                            }
                            Button(
                                onClick = onContinue,
                                enabled = continueEnabled,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(continueAlpha)
                                    .height(52.dp),
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = continueColor,
                                    disabledContainerColor = continueColor,
                                    contentColor = Color.White,
                                    disabledContentColor = Color(0xFF75829A)
                                )
                            ) {
                                Text("Continue", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = onSkip,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Skip for now", color = Color(0xFF607089))
                        }
                    }
                }
            }
        }
    }

    managePlatform?.let { platform ->
        ModalBottomSheet(
            onDismissRequest = { managePlatform = null },
            containerColor = Color(0xFF151F33),
            contentColor = Color.White,
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("${platform.title} options", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                    onDisconnectPlatform(platform)
                    managePlatform = null
                }) {
                    Text("Disconnect", color = Color(0xFFFFA0A0))
                }
                TextButton(onClick = { managePlatform = null }) {
                    Text("Close", color = Color(0xFFCFD9E8))
                }
            }
        }
    }
}

@Composable
private fun Platform3DCard(
    platform: SocialPlatform,
    connected: Boolean,
    loading: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val tintColor by animateColorAsState(
        targetValue = if (connected) platform.activeColor else Color(0xFFB0B7C3),
        animationSpec = tween(300),
        label = "platform_tint"
    )

    val popScale = remember { Animatable(1f) }
    LaunchedEffect(connected) {
        if (connected) {
            popScale.snapTo(0.96f)
            popScale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 500f))
        }
    }

    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press_scale"
    )

    val cardElevation by animateFloatAsState(
        targetValue = if (connected) 16f else 10f,
        animationSpec = tween(220),
        label = "card_elevation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(cardElevation.dp, RoundedCornerShape(20.dp))
                .scale(pressScale * popScale.value)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onTap
                ),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PaddingValues(horizontal = 10.dp, vertical = 12.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = platform.activeColor
                    )
                } else {
                    Icon(
                        imageVector = platform.icon,
                        contentDescription = platform.title,
                        tint = tintColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = platform.title,
                    color = if (connected) Color(0xFF1B2436) else Color(0xFF8A95A8),
                    fontSize = 12.sp,
                    fontWeight = if (connected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }

        AnimatedVisibility(
            visible = connected,
            enter = scaleIn(tween(180, easing = FastOutSlowInEasing)) + fadeIn(tween(180))
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, end = 8.dp)
                    .size(18.dp)
                    .background(Color(0xFF22C55E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "connected",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
