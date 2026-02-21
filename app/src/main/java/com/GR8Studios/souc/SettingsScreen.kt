package com.GR8Studios.souc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.GR8Studios.souc.auth.AuthSession
import com.GR8Studios.souc.data.AppDefaults
import com.GR8Studios.souc.data.SubscriptionTier
import kotlinx.coroutines.delay
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    bottomPadding: Dp,
    onLogout: () -> Unit
) {
    val user = AuthSession.currentUser
    val isAdmin = user?.email == AppDefaults.MASTER_EMAIL

    var showTimezoneDialog by rememberSaveable { mutableStateOf(false) }
    var selectedTimezone by rememberSaveable { mutableStateOf(TimeZone.getDefault().id) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

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
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.SemiBold) },
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
                // Profile card
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFE91E63), Color(0xFF9C27B0))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (user?.displayName?.firstOrNull() ?: 'U').uppercase(),
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        user?.displayName ?: "User",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        user?.email ?: "",
                                        color = Color(0xFF8B97AB),
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isAdmin) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF9C27B0).copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    "Admin",
                                                    color = Color(0xFFCE93D8),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF34D399).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                if (isAdmin) "Premium" else "Free",
                                                color = Color(0xFF34D399),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Subscription
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
                                    "Subscription",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                if (isAdmin) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF9C27B0).copy(alpha = 0.15f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(Icons.Default.Star, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                                            Column {
                                                Text("Master Admin Account", color = Color.White, fontWeight = FontWeight.SemiBold)
                                                Text("Unlimited posts, full access", color = Color(0xFFCE93D8), fontSize = 12.sp)
                                            }
                                        }
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF22314B),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("Free Plan", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                            Text("${AppDefaults.FREE_POST_LIMIT} posts/month", color = Color(0xFF8B97AB), fontSize = 13.sp)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = { /* TODO: Implement Stripe/Google Play billing */ },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFFBBF24)
                                                )
                                            ) {
                                                Text(
                                                    "Upgrade to Premium - $${AppDefaults.PREMIUM_PRICE_MONTHLY}/mo",
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Preferences
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
                                    "Preferences",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                SettingsRow(
                                    icon = Icons.Default.Schedule,
                                    label = "Timezone",
                                    value = selectedTimezone,
                                    onClick = { showTimezoneDialog = true }
                                )
                                Divider(color = Color(0xFF2A3753), modifier = Modifier.padding(vertical = 4.dp))
                                SettingsRow(
                                    icon = Icons.Default.Notifications,
                                    label = "Notifications",
                                    value = "Enabled"
                                )
                                Divider(color = Color(0xFF2A3753), modifier = Modifier.padding(vertical = 4.dp))
                                SettingsRow(
                                    icon = Icons.Default.Language,
                                    label = "Language",
                                    value = "English"
                                )
                            }
                        }
                    }
                }

                // About & logout
                item {
                    AnimatedVisibility(
                        visible = sectionsVisible[3],
                        enter = fadeIn(tween(400, delayMillis = 240)) + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(400, delayMillis = 240)
                        )
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = Color(0xFF18233A)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        "About",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    SettingsRow(icon = Icons.Default.Info, label = "App Version", value = "1.0.0")
                                    Divider(color = Color(0xFF2A3753), modifier = Modifier.padding(vertical = 4.dp))
                                    SettingsRow(icon = Icons.Default.Code, label = "Built by", value = "GR8 Studios")
                                }
                            }

                            Button(
                                onClick = { showLogoutConfirm = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFB7185).copy(alpha = 0.15f)
                                )
                            ) {
                                Icon(Icons.Default.Logout, null, tint = Color(0xFFFB7185))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Log out", color = Color(0xFFFB7185), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Timezone picker dialog
    if (showTimezoneDialog) {
        val commonTimezones = listOf(
            "America/New_York",
            "America/Chicago",
            "America/Denver",
            "America/Los_Angeles",
            "Europe/London",
            "Europe/Paris",
            "Europe/Berlin",
            "Asia/Tokyo",
            "Asia/Shanghai",
            "Asia/Kolkata",
            "Australia/Sydney",
            "Pacific/Auckland"
        )

        AlertDialog(
            onDismissRequest = { showTimezoneDialog = false },
            title = { Text("Select Timezone") },
            text = {
                LazyColumn {
                    items(commonTimezones.size) { index ->
                        val tz = commonTimezones[index]
                        val offset = TimeZone.getTimeZone(tz).let { zone ->
                            val hours = zone.rawOffset / 3600000
                            val mins = Math.abs(zone.rawOffset % 3600000) / 60000
                            "UTC${if (hours >= 0) "+" else ""}$hours${if (mins > 0) ":${mins.toString().padStart(2, '0')}" else ""}"
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTimezone = tz
                                    showTimezoneDialog = false
                                },
                            color = if (selectedTimezone == tz) Color(0xFF2A3753) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    tz.replace("_", " "),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(offset, color = Color(0xFF8B97AB), fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimezoneDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Logout confirmation
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out?") },
            text = { Text("Are you sure you want to log out of your account?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    AuthSession.setUser(null)
                    onLogout()
                }) {
                    Text("Log out", color = Color(0xFFFB7185))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF8B97AB), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = Color(0xFFD2DCF1), modifier = Modifier.weight(1f))
        Text(value, color = Color(0xFF8B97AB), fontSize = 13.sp)
        if (onClick != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF6B7A94), modifier = Modifier.size(18.dp))
        }
    }
}
