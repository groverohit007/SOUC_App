package com.GR8Studios.souc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.GR8Studios.souc.auth.AuthSession
import com.GR8Studios.souc.data.AppDefaults
import com.GR8Studios.souc.data.ScheduledPost
import com.GR8Studios.souc.data.PostStatus
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    bottomPadding: Dp,
    posts: List<ScheduledPost>,
    onNavigateCreate: () -> Unit,
    onNavigateCalendar: () -> Unit
) {
    val user = AuthSession.currentUser
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val isAdmin = user?.email == AppDefaults.MASTER_EMAIL

    var tier by remember { mutableStateOf("FREE") }
    var postsUsed by remember { mutableIntStateOf(0) }
    var monthlyLimit by remember { mutableIntStateOf(AppDefaults.FREE_POST_LIMIT) }

    DisposableEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onDispose { }
        } else {
            val db = FirebaseFirestore.getInstance()
            val userListener = db.collection("users").document(uid).addSnapshotListener { snap, _ ->
                tier = snap?.getString("tier") ?: "FREE"
                postsUsed = (snap?.getLong("postsUsed") ?: 0L).toInt()
            }
            val configListener = db.collection("app_settings").document("config").addSnapshotListener { snap, _ ->
                monthlyLimit = (snap?.getLong("free_post_limit") ?: AppDefaults.FREE_POST_LIMIT.toLong()).toInt()
            }
            onDispose {
                userListener.remove()
                configListener.remove()
            }
        }
    }

    val totalPosts = posts.size
    val scheduledCount = posts.count { it.status == PostStatus.SCHEDULED }
    val postedCount = posts.count { it.status == PostStatus.POSTED }
    val failedCount = posts.count { it.status == PostStatus.FAILED }
    val uploadingCount = posts.count { it.status == PostStatus.UPLOADING }

    val recentPosts = posts.sortedByDescending { it.createdAt }.take(5)

    val sectionsVisible = remember { mutableStateListOf(false, false, false, false) }
    LaunchedEffect(Unit) {
        sectionsVisible.indices.forEach { index ->
            delay(100L * index)
            sectionsVisible[index] = true
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = bottomPadding + 100.dp)
            ) {
                // Greeting
                item {
                    AnimatedVisibility(
                        visible = sectionsVisible[0],
                        enter = fadeIn(tween(400)) + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(400)
                        )
                    ) {
                        Column {
                            Text(
                                text = "$greeting,",
                                color = Color(0xFFB0BDD4),
                                fontSize = 16.sp
                            )
                            Text(
                                text = user?.displayName ?: "Creator",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (tier.equals("PREMIUM", true) || isAdmin) Color(0xFF9C27B0).copy(alpha = 0.24f) else Color.Transparent,
                                tonalElevation = if (tier.equals("PREMIUM", true) || isAdmin) 2.dp else 0.dp,
                                border = if (tier.equals("PREMIUM", true) || isAdmin) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6C7691))
                            ) {
                                Text(
                                    text = if (tier.equals("PREMIUM", true) || isAdmin) "Pro" else "Free ($postsUsed/$monthlyLimit Used)",
                                    color = if (tier.equals("PREMIUM", true) || isAdmin) Color(0xFFE0C4FF) else Color(0xFFB0BDD4),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            if (isAdmin) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF9C27B0).copy(alpha = 0.25f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = Color(0xFFCE93D8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text("Admin", color = Color(0xFFCE93D8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Stats cards
                item {
                    AnimatedVisibility(
                        visible = sectionsVisible[1],
                        enter = fadeIn(tween(400, delayMillis = 80)) + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(400, delayMillis = 80)
                        )
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Article,
                                    label = "Total Posts",
                                    value = totalPosts,
                                    color = Color(0xFF60A5FA)
                                )
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Schedule,
                                    label = "Scheduled",
                                    value = scheduledCount,
                                    color = Color(0xFFFBBF24)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.CheckCircle,
                                    label = "Posted",
                                    value = postedCount,
                                    color = Color(0xFF34D399)
                                )
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Error,
                                    label = "Failed",
                                    value = failedCount,
                                    color = Color(0xFFFB7185)
                                )
                            }
                            if (uploadingCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF18233A)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color(0xFF818CF8),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            "$uploadingCount uploading now...",
                                            color = Color(0xFF818CF8),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick actions
                item {
                    AnimatedVisibility(
                        visible = sectionsVisible[2],
                        enter = fadeIn(tween(400, delayMillis = 160)) + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(400, delayMillis = 160)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickActionButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.AddCircle,
                                label = "New Post",
                                gradient = listOf(Color(0xFFE91E63), Color(0xFF9C27B0)),
                                onClick = onNavigateCreate
                            )
                            QuickActionButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.DateRange,
                                label = "Calendar",
                                gradient = listOf(Color(0xFF2196F3), Color(0xFF00BCD4)),
                                onClick = onNavigateCalendar
                            )
                        }
                    }
                }

                // Recent posts
                item {
                    AnimatedVisibility(
                        visible = sectionsVisible[3],
                        enter = fadeIn(tween(400, delayMillis = 240)) + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(400, delayMillis = 240)
                        )
                    ) {
                        Column {
                            Text(
                                "Recent Activity",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            if (recentPosts.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF18233A)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.RocketLaunch,
                                            contentDescription = null,
                                            tint = Color(0xFF6B7A94),
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            "No posts yet",
                                            color = Color(0xFFB0BDD4),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "Create your first post to get started!",
                                            color = Color(0xFF7B8CA3),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(recentPosts, key = { it.id }) { post ->
                    RecentPostItem(post = post)
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: Int,
    color: Color
) {
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "stat_$label"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF18233A)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
                Text(label, color = Color(0xFFB0BDD4), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = animatedValue.toString(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "action_scale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradient), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(label, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RecentPostItem(post: ScheduledPost) {
    val statusColor = when (post.status) {
        PostStatus.POSTED -> Color(0xFF34D399)
        PostStatus.FAILED -> Color(0xFFFB7185)
        PostStatus.UPLOADING -> Color(0xFF818CF8)
        else -> Color(0xFF60A5FA)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF18233A)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                GradientBrand[0].copy(alpha = 0.4f),
                                GradientBrand[2].copy(alpha = 0.4f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (post.mediaType == "video") Icons.Default.SmartDisplay else Icons.Default.Image,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    post.mediaName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(post.scheduledEpochMillis)),
                    color = Color(0xFF8B97AB),
                    fontSize = 12.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    post.status,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
