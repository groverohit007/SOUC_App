package com.GR8Studios.souc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

val BgTop = Color(0xFF090A14)
val BgBottom = Color(0xFF111632)
val GradientBrand = listOf(Color(0xFFFF2E97), Color(0xFF972DFF), Color(0xFF158DFF))

private val NavBackground = Color(0xEE10162B)
private val NavUnselected = Color(0xFF9AA4B2)
private val NavSelected = Color.White

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
                onPurchaseSuccess = { rootNavController.popBackStack() }
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
                    val tier = snapshot?.getString("tier") ?: "FREE"
                    val isAdmin = snapshot?.getBoolean("isAdmin") ?: false
                    val postsUsed = (snapshot?.getLong("postsUsed") ?: 0L).toInt()
                    userState = userState.copy(
                        uid = firebaseUser.uid,
                        tier = tier,
                        isAdmin = isAdmin,
                        postsUsed = postsUsed
                    )
                }
            val configListener = db.collection("app_settings").document("config")
                .addSnapshotListener { snapshot, _ ->
                    val limit = (snapshot?.getLong("free_post_limit") ?: AppDefaults.FREE_POST_LIMIT.toLong()).toInt()
                    userState = userState.copy(freePostLimit = limit)
                }
            onDispose {
                userListener.remove()
                configListener.remove()
            }
        }
    }

    var barVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { barVisible = true }

    val navItems = remember(userState.isAdmin) {
        buildList {
            add(BottomNavItem("home", "Home", Icons.Default.Home))
            add(BottomNavItem("create", "Create", Icons.Default.AddCircle))
            add(BottomNavItem("calendar", "Calendar", Icons.Default.DateRange))
            add(BottomNavItem("accounts", "Accounts", Icons.Default.People))
            if (userState.isAdmin) add(BottomNavItem("admin", "Admin", Icons.Default.Shield))
            add(BottomNavItem("settings", "Settings", Icons.Default.Settings))
        }
    }

    val canSchedule = userState.isAdmin || userState.tier.equals("PREMIUM", true) || userState.postsUsed < userState.freePostLimit

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            AnimatedVisibility(
                visible = barVisible,
                enter = fadeIn(tween(350)) + slideInVertically(initialOffsetY = { it }, animationSpec = tween(350))
            ) {
                FloatingBottomNavBar(navController = bottomNavController, items = navItems)
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
                    onOpenConnectPopup = { bottomNavController.navigate("accounts") },
                    onNavigateCalendar = {
                        if (canSchedule) {
                            bottomNavController.navigate("calendar") { launchSingleTop = true }
                        } else {
                            rootNavController.navigate("paywall")
                        }
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
                AdminDashboardScreen(
                    bottomPadding = paddingValues.calculateBottomPadding(),
                    posts = posts
                )
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

data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)

@Composable
fun FloatingBottomNavBar(navController: NavHostController, items: List<BottomNavItem>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 16.dp)
            .shadow(14.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(NavBackground)
    ) {
        BoxWithConstraints {
            val tabWidth = maxWidth / items.size
            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = 320f),
                label = "indicator"
            )

            Box(
                modifier = Modifier.offset(x = indicatorOffset).width(tabWidth),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(width = 24.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(GradientBrand))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.forEach { item ->
                    BottomNavItemUI(
                        item = item,
                        isSelected = currentRoute == item.route,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItemUI(
    item: BottomNavItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (isSelected) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Column(
        modifier = modifier
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 12.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = if (isSelected) NavSelected else NavUnselected,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            item.title,
            color = if (isSelected) NavSelected else NavUnselected,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 11.sp
        )
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
