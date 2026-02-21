package com.GR8Studios.souc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
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
import com.GR8Studios.souc.data.AppDefaults
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

val BgTop = Color(0xFF0B0F1A)
val BgBottom = Color(0xFF121A2A)
val GradientBrand = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF2196F3))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme()) {
                RootNavigation()
            }
        }
    }
}

@Composable
fun RootNavigation() {
    val rootNavController = rememberNavController()
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

    NavHost(
        navController = rootNavController,
        startDestination = if (isLoggedIn) "shell" else "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    rootNavController.navigate("shell") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("shell") { HomeShell(rootNavController) }
        composable("paywall") {
            SubscriptionScreen(
                onDismiss = { rootNavController.popBackStack() },
                onPurchaseSuccess = {
                    rootNavController.popBackStack("create", false)
                }
            )
        }
    }
}

private data class UserState(
    val uid: String = "",
    val isAdmin: Boolean = false,
    val tier: String = "FREE",
    val postsUsed: Int = 0,
    val freePostLimit: Int = AppDefaults.FREE_POST_LIMIT
)

@Composable
fun HomeShell(rootNavController: NavController) {
    val bottomNavController = rememberNavController()
    val postsViewModel: PostsViewModel = viewModel()
    val posts by postsViewModel.posts.collectAsState()

    val firebaseUser = FirebaseAuth.getInstance().currentUser
    var userState by remember { mutableStateOf(UserState()) }

    DisposableEffect(firebaseUser?.uid) {
        if (firebaseUser == null) {
            onDispose { }
        } else {
            val db = FirebaseFirestore.getInstance()
            val userListener = db.collection("users").document(firebaseUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    userState = userState.copy(
                        uid = firebaseUser.uid,
                        tier = snapshot?.getString("tier") ?: "FREE",
                        isAdmin = snapshot?.getBoolean("isAdmin") ?: false,
                        postsUsed = (snapshot?.getLong("postsUsed") ?: 0L).toInt()
                    )
                }
            val configListener = db.collection("app_settings").document("config")
                .addSnapshotListener { snapshot, _ ->
                    userState = userState.copy(
                        freePostLimit = (snapshot?.getLong("free_post_limit")
                            ?: AppDefaults.FREE_POST_LIMIT.toLong()).toInt()
                    )
                }
            onDispose {
                userListener.remove()
                configListener.remove()
            }
        }
    }

    var navVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { navVisible = true }

    val canSchedule = userState.isAdmin || userState.tier.equals("PREMIUM", true) || userState.postsUsed < userState.freePostLimit

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            AnimatedVisibility(
                visible = navVisible,
                enter = fadeIn(tween(330)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(330))
            ) {
                Floating3DNavBar(navController = bottomNavController, isAdmin = userState.isAdmin)
            }
        }
    ) { paddingValues ->
        NavHost(navController = bottomNavController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    bottomPadding = paddingValues.calculateBottomPadding(),
                    posts = posts,
                    onNavigateCreate = { bottomNavController.navigate("create") { launchSingleTop = true } },
                    onNavigateCalendar = { bottomNavController.navigate("calendar") { launchSingleTop = true } }
                )
            }
            composable("create") {
                CreateScreen(
                    bottomPadding = paddingValues.calculateBottomPadding(),
                    youtubeConnected = false,
                    instagramConnected = false,
                    facebookConnected = false,
                    onOpenConnectPopup = { bottomNavController.navigate("accounts") { launchSingleTop = true } },
                    onNavigateCalendar = {
                        if (canSchedule) bottomNavController.navigate("calendar") { launchSingleTop = true }
                        else rootNavController.navigate("paywall")
                    }
                )
            }
            composable("calendar") {
                CalendarScreen(
                    bottomPadding = paddingValues.calculateBottomPadding(),
                    onCreatePost = { bottomNavController.navigate("create") { launchSingleTop = true } }
                )
            }
            composable("accounts") {
                ConnectAccountsScreen(
                    bottomPadding = paddingValues.calculateBottomPadding(),
                    onContinue = { bottomNavController.navigate("create") { launchSingleTop = true } }
                )
            }
            composable("admin") {
                AdminDashboardScreen(bottomPadding = paddingValues.calculateBottomPadding(), posts = posts)
            }
            composable("settings") {
                SettingsScreen(
                    bottomPadding = paddingValues.calculateBottomPadding(),
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        AuthSession.setUser(null)
                        rootNavController.navigate("login") {
                            popUpTo("shell") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// 3. 3D FLOATING NAVIGATION BAR
// ==========================================
data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)

val StandardNavItems = listOf(
    BottomNavItem("home", "Home", Icons.Default.Home),
    BottomNavItem("create", "Create", Icons.Default.Add),
    BottomNavItem("calendar", "Calendar", Icons.Default.DateRange),
    BottomNavItem("accounts", "Accounts", Icons.Default.Person),
    BottomNavItem("settings", "Settings", Icons.Default.Settings)
)

val AdminNavItem = BottomNavItem("admin", "Admin", Icons.Default.AdminPanelSettings)

@Composable
fun Floating3DNavBar(navController: NavHostController, isAdmin: Boolean) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val activeNavItems = remember(isAdmin) { if (isAdmin) StandardNavItems + AdminNavItem else StandardNavItems }
    val selectedIndex = activeNavItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .graphicsLayer {
                shadowElevation = 24.dp.toPx()
                shape = RoundedCornerShape(32.dp)
                clip = true
                ambientShadowColor = Color(0xFFE91E63)
                spotShadowColor = Color(0xFF9C27B0)
            }
            .background(Color(0xD9121A2A))
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f), Color.Transparent)))
            .padding(vertical = 8.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val tabWidth = maxWidth / activeNavItems.size
            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 250f),
                label = "indicator_slide"
            )

            Box(
                modifier = Modifier.offset(x = indicatorOffset).width(tabWidth),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            shadowElevation = 20f
                            shape = CircleShape
                            ambientShadowColor = Color(0xFF9C27B0)
                            spotShadowColor = Color(0xFF2196F3)
                        }
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFE91E63).copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                activeNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavItem3D(
                        item = item,
                        isSelected = isSelected,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
fun NavItem3D(
    item: BottomNavItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val liftY by animateFloatAsState(
        targetValue = if (isSelected) -12f else if (isPressed) 4f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "lift_y"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )

    Column(
        modifier = modifier
            .clickable(interactionSource = interactionSource, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .graphicsLayer {
                translationY = liftY
                scaleX = scale
                scaleY = scale
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = if (isSelected) Color.White else Color(0xFF9AA4B2),
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        AnimatedVisibility(visible = isSelected) {
            Text(text = item.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val authManager = remember { GoogleAuthManager(androidx.compose.ui.platform.LocalContext.current) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        authManager.restoreSession()
        if (FirebaseAuth.getInstance().currentUser != null) onLoginSuccess()
    }

    LoginScreenContent(
        loading = loading,
        error = error,
        onGoogleTap = {
            loading = true
            error = null
            authManager.signInAndSyncProfile(
                onSuccess = {
                    loading = false
                    onLoginSuccess()
                },
                onError = {
                    loading = false
                    error = it
                }
            )
        }
    )
}
