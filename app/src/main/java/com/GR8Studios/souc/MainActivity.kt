package com.GR8Studios.souc

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.GR8Studios.souc.auth.AuthSession
import com.GR8Studios.souc.auth.GoogleAuthManager
import com.GR8Studios.souc.auth.SecureTokenStorage
import com.GR8Studios.souc.data.AppDefaults
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- CONSTANTS & COLORS ---
val BgTop = Color(0xFF0B0F1A)
val BgBottom = Color(0xFF121A2A)
val GradientBrand = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF2196F3))

val NavBackground = Color(0xEE121A2A)
val NavUnselected = Color(0xFF9AA4B2)
val NavSelected = Color(0xFFFFFFFF)

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
    val context = LocalContext.current
    val bottomNavController = rememberNavController()
    val postsViewModel: PostsViewModel = viewModel()
    val posts by postsViewModel.posts.collectAsState()

    val isAdmin = AuthSession.currentUser?.email == AppDefaults.MASTER_EMAIL

    var isBarVisible by remember { mutableStateOf(false) }

    var youtubeConnected by rememberSaveable { mutableStateOf(false) }
    var instagramConnected by rememberSaveable { mutableStateOf(false) }
    var facebookConnected by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isBarVisible = true
    }

    // Build nav items dynamically based on admin status
    val navItems = remember(isAdmin) {
        buildList {
            add(BottomNavItem("home", "Home", Icons.Default.Home))
            add(BottomNavItem("create", "Create", Icons.Default.AddCircle))
            add(BottomNavItem("calendar", "Calendar", Icons.Default.DateRange))
            add(BottomNavItem("accounts", "Accounts", Icons.Default.People))
            if (isAdmin) {
                add(BottomNavItem("admin", "Admin", Icons.Default.Shield))
            }
            add(BottomNavItem("settings", "Settings", Icons.Default.Settings))
        }
    }

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
                FloatingBottomNavBar(navController = bottomNavController, items = navItems)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = bottomNavController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen(
                        bottomPadding = paddingValues.calculateBottomPadding(),
                        posts = posts,
                        onNavigateCreate = {
                            bottomNavController.navigate("create") {
                                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateCalendar = {
                            bottomNavController.navigate("calendar") {
                                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable("create") {
                    CreateScreen(
                        bottomPadding = paddingValues.calculateBottomPadding(),
                        youtubeConnected = youtubeConnected,
                        instagramConnected = instagramConnected,
                        facebookConnected = facebookConnected,
                        onOpenConnectPopup = {
                            bottomNavController.navigate("accounts") {
                                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
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
                composable("accounts") {
                    ConnectAccountsScreen(
                        bottomPadding = paddingValues.calculateBottomPadding(),
                        onContinue = {
                            bottomNavController.navigate("create") {
                                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                if (isAdmin) {
                    composable("admin") {
                        AdminScreen(
                            bottomPadding = paddingValues.calculateBottomPadding(),
                            posts = posts,
                            userCount = 1
                        )
                    }
                }
                composable("settings") {
                    SettingsScreen(
                        bottomPadding = paddingValues.calculateBottomPadding(),
                        onLogout = {
                            rootNavController.navigate("login") {
                                popUpTo("shell") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. FLOATING BOTTOM NAVIGATION BAR
// ==========================================
data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun FloatingBottomNavBar(navController: NavHostController, items: List<BottomNavItem>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black, spotColor = Color.Black)
            .clip(RoundedCornerShape(24.dp))
            .background(NavBackground)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val tabWidth = maxWidth / items.size

            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = INDICATOR_SPRING_DAMPING,
                    stiffness = INDICATOR_SPRING_STIFFNESS
                ),
                label = "indicator_slide"
            )

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route

                    BottomNavItemUI(
                        item = item,
                        isSelected = isSelected,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(item.route) {
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

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press_scale"
    )

    val selectScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "select_scale"
    )

    var glowAlpha by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            glowAlpha = 0.8f
            animate(
                initialValue = 0.8f,
                targetValue = 0.25f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) { value, _ -> glowAlpha = value }
        } else {
            glowAlpha = 0f
        }
    }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp)
            .scale(pressScale * selectScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
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

            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = if (isSelected) NavSelected else NavUnselected,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.title,
            color = if (isSelected) NavSelected else NavUnselected,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

// ==========================================
// 4. LOGIN SCREEN
// ==========================================
@Composable
fun LoginScreenStub(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val googleAuthManager = remember { GoogleAuthManager(context) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        googleAuthManager.restoreSession()
        if (AuthSession.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text("SOUC", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Schedule Once, Upload to All",
                color = Color(0xFF8B97AB),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isLoading = true
                    error = null
                    coroutineScope.launch {
                        val result = googleAuthManager.signIn()
                        isLoading = false
                        result.onSuccess {
                            onLoginSuccess()
                        }.onFailure {
                            error = it.message ?: "Google sign-in failed"
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Continue with Google", color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            }

            error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = Color(0xFFFF8A8A), fontSize = 12.sp)
            }
        }
    }
}
