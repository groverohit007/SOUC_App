package com.GR8Studios.souc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.GR8Studios.souc.data.AppDefaults
import com.GR8Studios.souc.data.PostStatus
import com.GR8Studios.souc.data.ScheduledPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class DashboardUser(val uid: String, val email: String, val tier: String)

@Composable
fun AdminDashboardScreen(bottomPadding: Dp, posts: List<ScheduledPost>) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var freeLimitText by remember { mutableStateOf(AppDefaults.FREE_POST_LIMIT.toString()) }
    var freeLimitSlider by remember { mutableFloatStateOf(AppDefaults.FREE_POST_LIMIT.toFloat()) }
    var totalUsers by remember { mutableIntStateOf(0) }
    var totalProUsers by remember { mutableIntStateOf(0) }
    val users = remember { mutableStateListOf<DashboardUser>() }

    DisposableEffect(Unit) {
        val usersListener = firestore.collection("users").addSnapshotListener { snap, _ ->
            totalUsers = snap?.size() ?: 0
            totalProUsers = snap?.documents?.count { it.getString("tier").equals("PREMIUM", true) } ?: 0
            users.clear()
            snap?.documents?.forEach {
                users.add(
                    DashboardUser(
                        uid = it.id,
                        email = it.getString("email") ?: "unknown",
                        tier = it.getString("tier") ?: "FREE"
                    )
                )
            }
        }
        val configListener = firestore.collection("app_settings").document("config")
            .addSnapshotListener { snap, _ ->
                val limit = ((snap?.getLong("free_post_limit") ?: AppDefaults.FREE_POST_LIMIT.toLong()).toInt())
                freeLimitText = limit.toString()
                freeLimitSlider = limit.toFloat()
            }
        onDispose {
            usersListener.remove()
            configListener.remove()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = bottomPadding + 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Total Users", totalUsers.toString(), Color(0xFF4FC3F7), Modifier.weight(1f))
                MetricCard("Pro Users", totalProUsers.toString(), Color(0xFFCE93D8), Modifier.weight(1f))
                MetricCard(
                    "Queued",
                    posts.count { it.status == PostStatus.SCHEDULED }.toString(),
                    Color(0xFF66BB6A),
                    Modifier.weight(1f)
                )
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF18233A)), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Control Center", color = Color.White, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = freeLimitText,
                        onValueChange = {
                            freeLimitText = it.filter(Char::isDigit)
                            freeLimitSlider = freeLimitText.toFloatOrNull() ?: 0f
                        },
                        label = { Text("free_post_limit") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Slider(
                        value = freeLimitSlider,
                        onValueChange = {
                            freeLimitSlider = it
                            freeLimitText = it.toInt().toString()
                        },
                        valueRange = 1f..60f
                    )
                    Button(
                        onClick = {
                            val limit = freeLimitText.toIntOrNull() ?: AppDefaults.FREE_POST_LIMIT
                            firestore.collection("app_settings").document("config")
                                .set(mapOf("free_post_limit" to limit), SetOptions.merge())
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Save global free limit")
                    }
                }
            }
        }

        item {
            Text("User Manager", color = Color.White, fontWeight = FontWeight.Bold)
        }
        items(users, key = { it.uid }) { user ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2744)), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(user.email, color = Color.White)
                        Text(user.tier, color = Color(0xFF9BB3E8))
                    }
                    Button(
                        onClick = {
                            firestore.collection("users").document(user.uid)
                                .set(mapOf("tier" to "PREMIUM"), SetOptions.merge())
                        }
                    ) {
                        Text("Grant Pro")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, glow: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF15203A)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(title, color = glow)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
