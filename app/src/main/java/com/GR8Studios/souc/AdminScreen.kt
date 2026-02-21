package com.GR8Studios.souc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.GR8Studios.souc.data.AppConfig
import com.GR8Studios.souc.data.AppDefaults
import com.GR8Studios.souc.data.ScheduledPost
import com.GR8Studios.souc.data.PostStatus
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    bottomPadding: Dp,
    posts: List<ScheduledPost>,
    userCount: Int
) {
    val totalPosts = posts.size
    val postedCount = posts.count { it.status == PostStatus.POSTED }
    val scheduledCount = posts.count { it.status == PostStatus.SCHEDULED }
    val failedCount = posts.count { it.status == PostStatus.FAILED }
    val uploadingCount = posts.count { it.status == PostStatus.UPLOADING }

    var postLimitText by rememberSaveable { mutableStateOf(AppConfig.freePostLimit.toString()) }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    val sectionsVisible = remember { mutableStateListOf(false, false, false, false) }
    LaunchedEffect(Unit) {
        sectionsVisible.indices.forEach { index ->
            delay(100L * index)
            sectionsVisible[index] = true
        }
    }

    LaunchedEffect(showSavedSnackbar) {
        if (showSavedSnackbar) {
            delay(2000)
            showSavedSnackbar = false
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFCE93D8))
                        Text("Admin Dashboard", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                },
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = bottomPadding + 100.dp)
            ) {
                // Platform overview
                item {
                    AnimatedVisibility(
                        visible = sectionsVisible[0],
                        enter = fadeIn(tween(400)) + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(400)
                        )
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFF18233A)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    "Platform Overview",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    AdminStatBubble(
                                        icon = Icons.Default.Group,
                                        value = userCount.toString(),
                                        label = "Users",
                                        color = Color(0xFF818CF8)
                                    )
                                    AdminStatBubble(
                                        icon = Icons.AutoMirrored.Filled.Article,
                                        value = totalPosts.toString(),
                                        label = "Total Posts",
                                        color = Color(0xFF60A5FA)
                                    )
                                    AdminStatBubble(
                                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                                        value = if (totalPosts > 0) "${(postedCount * 100 / totalPosts)}%" else "0%",
                                        label = "Success",
                                        color = Color(0xFF34D399)
                                    )
                                }
                            }
                        }
                    }
                }

                // Post breakdown
                item {
                    AnimatedVisibility(
                        visible = sectionsVisible[1],
                        enter = fadeIn(tween(400, delayMillis = 80)) + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(400, delayMillis = 80)
                        )
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFF18233A)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    "Post Breakdown",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                AdminProgressRow("Posted", postedCount, totalPosts, Color(0xFF34D399))
                                Spacer(modifier = Modifier.height(10.dp))
                                AdminProgressRow("Scheduled", scheduledCount, totalPosts, Color(0xFF60A5FA))
                                Spacer(modifier = Modifier.height(10.dp))
                                AdminProgressRow("Uploading", uploadingCount, totalPosts, Color(0xFF818CF8))
                                Spacer(modifier = Modifier.height(10.dp))
                                AdminProgressRow("Failed", failedCount, totalPosts, Color(0xFFFB7185))
                            }
                        }
                    }
                }

                // App configuration
                item {
                    AnimatedVisibility(
                        visible = sectionsVisible[2],
                        enter = fadeIn(tween(400, delayMillis = 160)) + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(400, delayMillis = 160)
                        )
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFF18233A)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    "App Configuration",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                Text("Free tier monthly post limit", color = Color(0xFFB0BDD4), fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = postLimitText,
                                        onValueChange = { postLimitText = it.filter { c -> c.isDigit() } },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        placeholder = { Text("5") }
                                    )
                                    Button(
                                        onClick = {
                                            val newLimit = postLimitText.toIntOrNull() ?: AppDefaults.FREE_POST_LIMIT
                                            AppConfig.freePostLimit = newLimit
                                            showSavedSnackbar = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF9C27B0)
                                        )
                                    ) {
                                        Text("Apply")
                                    }
                                }

                                AnimatedVisibility(visible = showSavedSnackbar) {
                                    Surface(
                                        modifier = Modifier.padding(top = 10.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF34D399).copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Check, null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                                            Text("Post limit updated to ${AppConfig.freePostLimit}", color = Color(0xFF34D399), fontSize = 13.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))
                                HorizontalDivider(color = Color(0xFF2A3753))
                                Spacer(modifier = Modifier.height(14.dp))

                                Text("Premium pricing", color = Color(0xFFB0BDD4), fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF22314B)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Monthly subscription", color = Color(0xFFD2DCF1))
                                        Text(
                                            "$${AppConfig.premiumPriceMonthly}/mo",
                                            color = Color(0xFFFBBF24),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // System info
                item {
                    AnimatedVisibility(
                        visible = sectionsVisible[3],
                        enter = fadeIn(tween(400, delayMillis = 240)) + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(400, delayMillis = 240)
                        )
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFF18233A)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    "System Info",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                InfoRow("App version", "1.0.0")
                                InfoRow("Database", "Room v2")
                                InfoRow("Master email", AppDefaults.MASTER_EMAIL)
                                InfoRow("Backend", "Cloud Run (europe-west2)")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatBubble(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFF8B97AB), fontSize = 12.sp)
    }
}

@Composable
private fun AdminProgressRow(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val progress = if (total > 0) count.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "progress_$label"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color(0xFFB0BDD4), fontSize = 13.sp)
            Text("$count", color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF2A3753)
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF8B97AB), fontSize = 13.sp)
        Text(value, color = Color(0xFFD2DCF1), fontSize = 13.sp)
    }
}
