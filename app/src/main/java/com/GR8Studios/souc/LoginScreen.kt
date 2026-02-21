package com.GR8Studios.souc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Facebook
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val LoginGradientTop = Color(0xFF0B0F1A)
private val LoginGradientBottom = Color(0xFF121A2A)
private val BrandGradient = listOf(Color(0xFFFF2E97), Color(0xFF972DFF), Color(0xFF158DFF))

@Composable
fun LoginScreen(onNavigateHome: () -> Unit) {
    var loading by rememberSaveable { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(LoginGradientTop, LoginGradientBottom)))
    ) {
        DriftingGlowBlobs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing))
            ) {
                BrandHeader()
            }

            AnimatedContentBlock(visible = contentVisible, delayMillis = 80) {
                Text(
                    text = "Schedule once. Post everywhere.",
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 22.dp)
                )
            }

            AnimatedContentBlock(visible = contentVisible, delayMillis = 150) {
                Text(
                    text = "Upload posts & reels to YouTube, Instagram, and Facebook — at the same time.",
                    color = Color(0xFFB4C0D6),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            AnimatedContentBlock(visible = contentVisible, delayMillis = 220) {
                PlatformIconRow(modifier = Modifier.padding(top = 20.dp))
            }

            AnimatedContentBlock(visible = contentVisible, delayMillis = 300) {
                GoogleSignInButton(
                    loading = loading,
                    modifier = Modifier.padding(top = 28.dp),
                    onClick = {
                        if (loading) return@GoogleSignInButton
                        loading = true
                    }
                )
            }

            LaunchedEffect(loading) {
                if (loading) {
                    delay(1200)
                    onNavigateHome()
                    loading = false
                }
            }

            AnimatedContentBlock(visible = contentVisible, delayMillis = 370) {
                FooterLinks(modifier = Modifier.padding(top = 24.dp))
            }

            AnimatedContentBlock(visible = contentVisible, delayMillis = 430) {
                Text(
                    text = "We never post without your confirmation.",
                    color = Color(0xFF7E8AA0),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    val iconScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "iconScale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(iconScale)
                .background(
                    brush = Brush.linearGradient(BrandGradient),
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
            )
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(24.dp)
            )
        }

        Text(
            text = "SOUC",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

@Composable
private fun AnimatedContentBlock(
    visible: Boolean,
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(320, delayMillis = delayMillis)) +
            slideInVertically(
                initialOffsetY = { it / 5 },
                animationSpec = tween(320, delayMillis = delayMillis, easing = FastOutSlowInEasing)
            )
    ) {
        content()
    }
}

@Composable
private fun PlatformIconRow(modifier: Modifier = Modifier) {
    val iconTint = Color(0xFFA6B1C4)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PlatformIcon(Icons.Outlined.SmartDisplay, "YouTube", iconTint)
        PlatformIcon(Icons.Outlined.VideoLibrary, "Instagram", iconTint)
        PlatformIcon(Icons.Outlined.Facebook, "Facebook", iconTint)
    }
}

@Composable
private fun PlatformIcon(icon: ImageVector, label: String, tint: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
        Text(label, color = tint, fontSize = 12.sp)
    }
}

@Composable
private fun GoogleSignInButton(
    loading: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        enabled = !loading,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        shape = RoundedCornerShape(50),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scale)
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Color.Black,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Signing you in…",
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 10.dp)
            )
        } else {
            GoogleGlyph()
            Text(
                text = "Continue with Google",
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}

@Composable
private fun GoogleGlyph() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(color = Color(0xFFF1F3F4), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("G", color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FooterLinks(modifier: Modifier = Modifier) {
    val source = remember { MutableInteractionSource() }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Privacy",
            color = Color(0xFFB4C0D6),
            fontSize = 13.sp,
            modifier = Modifier.clickable(interactionSource = source, indication = null) {}
        )
        Text(
            text = "Terms",
            color = Color(0xFFB4C0D6),
            fontSize = 13.sp,
            modifier = Modifier.clickable(interactionSource = source, indication = null) {}
        )
    }
}

@Composable
private fun DriftingGlowBlobs() {
    val transition = rememberInfiniteTransition(label = "blobTransition")

    val blobOneX by transition.animateValue(
        initialValue = -160f,
        targetValue = -110f,
        typeConverter = androidx.compose.animation.core.Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobOneX"
    )

    val blobTwoY by transition.animateValue(
        initialValue = 170f,
        targetValue = 120f,
        typeConverter = androidx.compose.animation.core.Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobTwoY"
    )

    val blobThreeX by transition.animateValue(
        initialValue = 80f,
        targetValue = 140f,
        typeConverter = androidx.compose.animation.core.Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 13000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobThreeX"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x66FF2E97), Color.Transparent),
                center = Offset(blobOneX.dp.toPx(), size.height * 0.18f),
                radius = 260.dp.toPx()
            ),
            radius = 260.dp.toPx(),
            center = Offset(blobOneX.dp.toPx(), size.height * 0.18f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x55972DFF), Color.Transparent),
                center = Offset(size.width * 0.85f, blobTwoY.dp.toPx()),
                radius = 300.dp.toPx()
            ),
            radius = 300.dp.toPx(),
            center = Offset(size.width * 0.85f, blobTwoY.dp.toPx())
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x55158DFF), Color.Transparent),
                center = Offset(blobThreeX.dp.toPx(), size.height * 0.8f),
                radius = 260.dp.toPx()
            ),
            radius = 260.dp.toPx(),
            center = Offset(blobThreeX.dp.toPx(), size.height * 0.8f)
        )
    }
}
