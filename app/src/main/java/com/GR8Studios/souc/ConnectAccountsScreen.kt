package com.GR8Studios.souc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class PlatformConfig(val key: String, val title: String, val icon: ImageVector)

private val connectPlatforms = listOf(
    PlatformConfig("youtube", "YouTube", Icons.Default.SmartDisplay),
    PlatformConfig("instagram", "Instagram", Icons.Default.CameraAlt),
    PlatformConfig("facebook", "Facebook", Icons.Default.Facebook)
)

private sealed interface AccountSheetState {
    data class OAuth(val platformKey: String) : AccountSheetState
    data class Manage(val platformKey: String) : AccountSheetState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectAccountsScreen(
    bottomPadding: Dp,
    onContinue: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    var youtubeConnected by rememberSaveable { mutableStateOf(false) }
    var instagramConnected by rememberSaveable { mutableStateOf(false) }
    var facebookConnected by rememberSaveable { mutableStateOf(false) }

    var youtubeEnabled by rememberSaveable { mutableStateOf(false) }
    var instagramEnabled by rememberSaveable { mutableStateOf(false) }
    var facebookEnabled by rememberSaveable { mutableStateOf(false) }

    var youtubePulse by rememberSaveable { mutableIntStateOf(0) }
    var instagramPulse by rememberSaveable { mutableIntStateOf(0) }
    var facebookPulse by rememberSaveable { mutableIntStateOf(0) }

    var activeSheet by remember { mutableStateOf<AccountSheetState?>(null) }

    val staggerVisibility = remember { mutableStateListOf(false, false, false) }
    LaunchedEffect(Unit) {
        staggerVisibility.indices.forEach { index ->
            delay(100L * index)
            staggerVisibility[index] = true
        }
    }

    val canContinue = (youtubeConnected && youtubeEnabled) ||
            (instagramConnected && instagramEnabled) ||
            (facebookConnected && facebookEnabled)

    val ctaColor by animateColorAsState(
        targetValue = if (canContinue) GradientBrand.first() else Color(0xFF2B3446),
        animationSpec = tween(320),
        label = "cta_color"
    )
    val ctaAlpha by animateFloatAsState(
        targetValue = if (canContinue) 1f else 0.65f,
        animationSpec = tween(280),
        label = "cta_alpha"
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text("Connect accounts", color = Color.White, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                Button(
                    onClick = onContinue,
                    enabled = canContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .alpha(ctaAlpha),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ctaColor,
                        disabledContainerColor = ctaColor,
                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Text("Continue", modifier = Modifier, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GradientBrand[1].copy(alpha = 0.22f), Color.Transparent),
                            radius = 900f
                        )
                    )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 12.dp,
                    bottom = 130.dp + bottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Connect the platforms you want to schedule posts to.",
                        color = Color(0xFFB8C2D6),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                items(connectPlatforms.size) { index ->
                    val platform = connectPlatforms[index]
                    val connected = when (platform.key) {
                        "youtube" -> youtubeConnected
                        "instagram" -> instagramConnected
                        else -> facebookConnected
                    }
                    val enabled = when (platform.key) {
                        "youtube" -> youtubeEnabled
                        "instagram" -> instagramEnabled
                        else -> facebookEnabled
                    }
                    val pulseTick = when (platform.key) {
                        "youtube" -> youtubePulse
                        "instagram" -> instagramPulse
                        else -> facebookPulse
                    }

                    AnimatedVisibility(
                        visible = staggerVisibility[index],
                        enter = fadeIn(tween(350, delayMillis = index * 80)) +
                                slideInVertically(
                                    initialOffsetY = { it / 4 },
                                    animationSpec = tween(350, delayMillis = index * 80, easing = LinearOutSlowInEasing)
                                )
                    ) {
                        PlatformCard(
                            config = platform,
                            connected = connected,
                            enabledForPosting = enabled,
                            pulseTick = pulseTick,
                            onActionClick = {
                                activeSheet = if (connected) {
                                    AccountSheetState.Manage(platform.key)
                                } else {
                                    AccountSheetState.OAuth(platform.key)
                                }
                            },
                            onEnabledToggle = { toggled ->
                                when (platform.key) {
                                    "youtube" -> {
                                        youtubeEnabled = toggled
                                        youtubePulse++
                                    }
                                    "instagram" -> {
                                        instagramEnabled = toggled
                                        instagramPulse++
                                    }
                                    else -> {
                                        facebookEnabled = toggled
                                        facebookPulse++
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    when (val state = activeSheet) {
        is AccountSheetState.OAuth -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = Color(0xFF151F33),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Simulating OAuth…", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(14.dp))
                    CircularProgressIndicator(color = GradientBrand[0])
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Connecting ${state.platformKey.replaceFirstChar { it.uppercase() }} account",
                        color = Color(0xFFB8C2D6)
                    )
                }

                LaunchedEffect(state.platformKey) {
                    delay(900)
                    when (state.platformKey) {
                        "youtube" -> {
                            youtubeConnected = true
                            youtubeEnabled = true
                            youtubePulse++
                        }
                        "instagram" -> {
                            instagramConnected = true
                            instagramEnabled = true
                            instagramPulse++
                        }
                        else -> {
                            facebookConnected = true
                            facebookEnabled = true
                            facebookPulse++
                        }
                    }
                    activeSheet = null
                }
            }
        }

        is AccountSheetState.Manage -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = Color(0xFF151F33),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Manage ${state.platformKey.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = {
                        when (state.platformKey) {
                            "youtube" -> {
                                youtubeConnected = false
                                youtubeEnabled = false
                            }
                            "instagram" -> {
                                instagramConnected = false
                                instagramEnabled = false
                            }
                            else -> {
                                facebookConnected = false
                                facebookEnabled = false
                            }
                        }
                        activeSheet = null
                    }) {
                        Text("Disconnect", color = Color(0xFFFF8EA2))
                    }
                    TextButton(onClick = { activeSheet = null }) {
                        Text("View account", color = Color(0xFF9FD3FF))
                    }
                    TextButton(onClick = { activeSheet = null }) {
                        Text("Close", color = Color(0xFFB8C2D6))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        null -> Unit
    }
}

@Composable
private fun PlatformCard(
    config: PlatformConfig,
    connected: Boolean,
    enabledForPosting: Boolean,
    pulseTick: Int,
    onActionClick: () -> Unit,
    onEnabledToggle: (Boolean) -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = if (connected) Color(0xFF1FC979) else Color(0xFF5A6478),
        animationSpec = tween(280),
        label = "status_color"
    )

    val glow = remember { Animatable(0f) }
    LaunchedEffect(pulseTick) {
        if (pulseTick > 0) {
            glow.snapTo(0f)
            glow.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
            glow.animateTo(0f, tween(420, easing = FastOutSlowInEasing))
        }
    }

    var showCheck by remember { mutableStateOf(false) }
    LaunchedEffect(connected) {
        if (connected) {
            showCheck = true
            delay(520)
            showCheck = false
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF172033)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (connected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Brush.horizontalGradient(GradientBrand))
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (glow.value > 0f) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                GradientBrand[0].copy(alpha = 0.35f * glow.value),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                        Icon(
                            imageVector = config.icon,
                            contentDescription = config.title,
                            tint = Color(0xFFEAF2FF),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(config.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        FilterChip(
                            selected = connected,
                            onClick = { },
                            enabled = false,
                            label = {
                                Text(
                                    if (connected) "Connected" else "Not connected",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            },
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                disabledContainerColor = statusColor.copy(alpha = 0.9f),
                                disabledLabelColor = Color.White
                            ),
                            border = null
                        )
                    }

                    Button(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (connected) Color(0xFF293652) else Color(0xFF31425F),
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (connected) "Manage" else "Connect", fontSize = 12.sp)
                    }
                }

                AnimatedVisibility(visible = connected) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Enabled for posting",
                            color = Color(0xFFD6DEF1),
                            fontSize = 13.sp
                        )
                        Switch(
                            checked = enabledForPosting,
                            onCheckedChange = onEnabledToggle
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showCheck) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF59E79E))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connected successfully", color = Color(0xFFB5F5D3), fontSize = 12.sp)
                }
            }
        }
    }
}
