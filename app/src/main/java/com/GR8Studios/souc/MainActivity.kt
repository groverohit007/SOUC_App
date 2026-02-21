package com.GR8Studios.souc

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- CONSTANTS & COLORS ---
val BgTop = Color(0xFF0B0F1A)
val BgBottom = Color(0xFF121A2A)
val GradientBrand = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF2196F3))

val NavBackground = Color(0xEE121A2A) // Slight transparency for floating effect
val NavUnselected = Color(0xFF9AA4B2)
val NavSelected = Color(0xFFFFFFFF)

// Animation Durations
const val BAR_ENTER_DURATION = 320
const val INDICATOR_SPRING_STIFFNESS = 300f
const val INDICATOR_SPRING_DAMPING = 0.7f

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                RootNavigation()
            }
        }
    }
}

// ==========================================
// 1. ROOT NAVIGATION
// ==========================================
@Composable
fun RootNavigation() {
    val rootNavController = rememberNavController()

    NavHost(navController = rootNavController, startDestination = "login") {
        composable("login") {
            LoginScreenStub(
                onLoginSuccess = {
                    rootNavController.navigate("shell") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("shell") {
            HomeShell(rootNavController)
        }
    }
}

// ==========================================
// 2. HOME SHELL & NESTED NAV
// ==========================================
@Composable
fun HomeShell(rootNavController: NavController) {
    val bottomNavController = rememberNavController()
    var isBarVisible by remember { mutableStateOf(false) }
    var popupVisible by rememberSaveable { mutableStateOf(true) }
    var showSkipBanner by rememberSaveable { mutableStateOf(false) }

    var youtubeConnected by rememberSaveable { mutableStateOf(false) }
    var instagramConnected by rememberSaveable { mutableStateOf(false) }
    var facebookConnected by rememberSaveable { mutableStateOf(false) }

    var youtubeLoading by rememberSaveable { mutableStateOf(false) }
    var instagramLoading by rememberSaveable { mutableStateOf(false) }
    var facebookLoading by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isBarVisible = true
        popupVisible = true
    }

    val hasConnectedPlatform = youtubeConnected || instagramConnected || facebookConnected

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                AnimatedVisibility(
                    visible = isBarVisible,
                    enter = fadeIn(tween(BAR_ENTER_DURATION, easing = EaseOut)) +
                            slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(BAR_ENTER_DURATION, easing = EaseOutBack)
                            )
                ) {
                    FloatingBottomNavBar(navController = bottomNavController)
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = bottomNavController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        PlaceholderTabScreen(
                            title = "Home",
                            bottomPadding = paddingValues.calculateBottomPadding(),
                            bannerText = if (showSkipBanner) "Connect your socials to start scheduling posts faster." else null
                        )
                    }
                    composable("create") {
                        CreateScreen(
                            bottomPadding = paddingValues.calculateBottomPadding(),
                            youtubeConnected = youtubeConnected,
                            instagramConnected = instagramConnected,
                            facebookConnected = facebookConnected,
                            onOpenConnectPopup = { popupVisible = true },
                            onNavigateCalendar = {
                                bottomNavController.navigate("calendar") {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    composable("calendar") {
                        CalendarScreen(
                            bottomPadding = paddingValues.calculateBottomPadding(),
                            onCreatePost = {
                                bottomNavController.navigate("create") {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    composable("accounts") { PlaceholderTabScreen("Accounts", paddingValues.calculateBottomPadding()) }
                    composable("settings") { PlaceholderTabScreen("Settings", paddingValues.calculateBottomPadding()) }
                }
            }
        }

        if (popupVisible) {
            ConnectAccountsPopup(
                youtubeConnected = youtubeConnected,
                instagramConnected = instagramConnected,
                facebookConnected = facebookConnected,
                youtubeLoading = youtubeLoading,
                instagramLoading = instagramLoading,
                facebookLoading = facebookLoading,
                onConnectPlatform = { platform ->
                    when (platform) {
                        SocialPlatform.YouTube -> if (!youtubeLoading) {
                            youtubeLoading = true
                        }

                        SocialPlatform.Instagram -> if (!instagramLoading) {
                            instagramLoading = true
                        }

                        SocialPlatform.Facebook -> if (!facebookLoading) {
                            facebookLoading = true
                        }
                    }
                },
                onDisconnectPlatform = { platform ->
                    when (platform) {
                        SocialPlatform.YouTube -> youtubeConnected = false
                        SocialPlatform.Instagram -> instagramConnected = false
                        SocialPlatform.Facebook -> facebookConnected = false
                    }
                },
                onCompleteConnect = { platform ->
                    when (platform) {
                        SocialPlatform.YouTube -> {
                            youtubeLoading = false
                            youtubeConnected = true
                        }

                        SocialPlatform.Instagram -> {
                            instagramLoading = false
                            instagramConnected = true
                        }

                        SocialPlatform.Facebook -> {
                            facebookLoading = false
                            facebookConnected = true
                        }
                    }
                },
                onSkip = {
                    popupVisible = false
                    showSkipBanner = true
                },
                onContinue = {
                    if (hasConnectedPlatform) {
                        popupVisible = false
                        showSkipBanner = false
                    }
                },
                onDismissRequest = { /* Block swipe-out to force explicit action */ },
                continueEnabled = hasConnectedPlatform
            )
        }
    }
}

// ==========================================
// 3. FLOATING BOTTOM NAVIGATION BAR
// ==========================================
data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)

val BottomNavItems = listOf(
    BottomNavItem("home", "Home", Icons.Default.Home),
    BottomNavItem("create", "Create", Icons.Default.AddCircle),
    BottomNavItem("calendar", "Calendar", Icons.Default.DateRange),
    BottomNavItem("accounts", "Accounts", Icons.Default.People),
    BottomNavItem("settings", "Settings", Icons.Default.Settings)
)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun FloatingBottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    // Find the currently selected index for the sliding indicator
    val selectedIndex = BottomNavItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Safe area handling
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black, spotColor = Color.Black)
            .clip(RoundedCornerShape(24.dp))
            .background(NavBackground)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val tabWidth = maxWidth / BottomNavItems.size

            // The Sliding Gradient Indicator
            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = INDICATOR_SPRING_DAMPING,
                    stiffness = INDICATOR_SPRING_STIFFNESS
                ),
                label = "indicator_slide"
            )

            // Draw the indicator dot/pill at the top of the selected tab
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .padding(top = 6.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 20.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(GradientBrand))
                )
            }

            // The Navigation Items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route

                    BottomNavItemUI(
                        item = item,
                        isSelected = isSelected,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(item.route) {
                                    // Preserve state per tab!
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavItemUI(
    item: BottomNavItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 1. Press Microinteraction (0.97f)
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f, // Exaggerated slightly for satisfying feel
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press_scale"
    )

    // 2. Selection Pop (0.92f -> 1.0f)
    val selectScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = 0.6f, // Bouncy spring
            stiffness = 400f
        ),
        label = "select_scale"
    )

    // 3. Single Pulse Glow Effect
    var glowAlpha by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            glowAlpha = 0.8f // Initial bright pulse
            animate(
                initialValue = 0.8f,
                targetValue = 0.25f, // Settles into a soft resting glow
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) { value, _ -> glowAlpha = value }
        } else {
            glowAlpha = 0f // Turns off when unselected
        }
    }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Remove default ripple for custom scaling
                onClick = onClick
            )
            .padding(vertical = 12.dp)
            .scale(pressScale * selectScale), // Combine both scales
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            // The Gradient Glow Behind the Icon
            if (glowAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(GradientBrand[1].copy(alpha = glowAlpha), Color.Transparent),
                                radius = 60f
                            )
                        )
                )
            }

            // The Icon
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = if (isSelected) NavSelected else NavUnselected,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // The Label
        Text(
            text = item.title,
            color = if (isSelected) NavSelected else NavUnselected,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

// ==========================================
// 4. PLACEHOLDER TAB SCREENS
// ==========================================
@Composable
fun PlaceholderTabScreen(title: String, bottomPadding: Dp, bannerText: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = bottomPadding + 80.dp)
        ) {
            bannerText?.let {
                Surface(
                    color = Color(0xFF1E2A43),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Text(
                        text = it,
                        color = Color(0xFFD6E4FF),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dashboard Content Here",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

// ==========================================
// 5. LOGIN STUB (Kept for runnable completeness)
// ==========================================
@Composable
fun LoginScreenStub(onLoginSuccess: () -> Unit) {
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom))),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                isLoading = true
                coroutineScope.launch {
                    delay(1200)
                    isLoading = false
                    onLoginSuccess()
                }
            }
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Simulate Login Success")
            }
        }
    }
}