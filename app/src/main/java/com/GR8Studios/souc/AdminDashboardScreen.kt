package com.GR8Studios.souc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

@Composable
fun AdminDashboardScreen(bottomPadding: Dp, posts: List<ScheduledPost>) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var freeLimitText by remember { mutableStateOf(AppDefaults.FREE_POST_LIMIT.toString()) }
    var totalUsers by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val userListener = firestore.collection("users").addSnapshotListener { snap, _ ->
            totalUsers = snap?.size() ?: 0
        }
        val configListener = firestore.collection("app_settings").document("config")
            .addSnapshotListener { snap, _ ->
                freeLimitText = ((snap?.getLong("free_post_limit") ?: AppDefaults.FREE_POST_LIMIT.toLong()).toInt()).toString()
            }
        onDispose {
            userListener.remove()
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
            Surface(color = Color(0xFF18233A), shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Master Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Total users: $totalUsers", color = Color(0xFF9BB3E8))
                    Text("Total posts queued: ${posts.count { it.status == PostStatus.SCHEDULED }}", color = Color(0xFF9BB3E8))
                }
            }
        }
        item {
            Surface(color = Color(0xFF18233A), shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("App Settings", color = Color.White, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = freeLimitText,
                        onValueChange = { freeLimitText = it.filter(Char::isDigit) },
                        label = { Text("free_post_limit") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val limit = freeLimitText.toIntOrNull() ?: AppDefaults.FREE_POST_LIMIT
                            firestore.collection("app_settings").document("config")
                                .set(mapOf("free_post_limit" to limit), com.google.firebase.firestore.SetOptions.merge())
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Save limit")
                    }
                }
            }
        }
    }
}
