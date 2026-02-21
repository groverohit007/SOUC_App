package com.GR8Studios.souc

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.GR8Studios.souc.data.PostStatus
import com.GR8Studios.souc.data.ScheduledPost
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

private data class CreatePlatform(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

private val createPlatforms = listOf(
    CreatePlatform("youtube", "YouTube", Icons.Default.SmartDisplay, Color(0xFFFF3D3D)),
    CreatePlatform("instagram", "Instagram", Icons.Default.Image, Color(0xFFE1306C)),
    CreatePlatform("facebook", "Facebook", Icons.Default.People, Color(0xFF1877F2))
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CreateScreen(
    bottomPadding: Dp,
    youtubeConnected: Boolean,
    instagramConnected: Boolean,
    facebookConnected: Boolean,
    onOpenConnectPopup: () -> Unit,
    onNavigateCalendar: () -> Unit
) {
    val context = LocalContext.current
    val postsViewModel: PostsViewModel = viewModel()

    var mediaSelected by rememberSaveable { mutableStateOf(false) }
    var mediaName by rememberSaveable { mutableStateOf("reel_2026.mp4") }
    var mediaSize by rememberSaveable { mutableStateOf("18.4 MB") }
    var mediaDuration by rememberSaveable { mutableStateOf("00:27") }
    var mediaUploading by rememberSaveable { mutableStateOf(false) }
    var uploadProgress by rememberSaveable { mutableStateOf(0f) }

    var selectedYoutube by rememberSaveable { mutableStateOf(false) }
    var selectedInstagram by rememberSaveable { mutableStateOf(false) }
    var selectedFacebook by rememberSaveable { mutableStateOf(false) }

    var useSameCaption by rememberSaveable { mutableStateOf(true) }
    var captionAll by rememberSaveable { mutableStateOf("") }
    var captionYoutube by rememberSaveable { mutableStateOf("") }
    var captionInstagram by rememberSaveable { mutableStateOf("") }
    var captionFacebook by rememberSaveable { mutableStateOf("") }
    var activeCaptionTab by rememberSaveable { mutableStateOf("youtube") }

    var postNow by rememberSaveable { mutableStateOf(true) }
    var scheduledDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var scheduledHour by rememberSaveable { mutableStateOf<Int?>(null) }
    var scheduledMinute by rememberSaveable { mutableStateOf<Int?>(null) }

    var successPost by remember { mutableStateOf<ScheduledPost?>(null) }

    val sectionsVisible = remember { mutableStateListOf(false, false, false, false) }
    LaunchedEffect(Unit) {
        sectionsVisible.indices.forEachIndexed { index, _ ->
            kotlinx.coroutines.delay(85L * index)
            sectionsVisible[index] = true
        }
    }

    LaunchedEffect(mediaUploading) {
        if (mediaUploading) {
            uploadProgress = 0f
            while (uploadProgress < 1f) {
                kotlinx.coroutines.delay(110)
                uploadProgress = (uploadProgress + 0.12f).coerceAtMost(1f)
            }
            mediaUploading = false
        }
    }

    val connectedMap = mapOf(
        "youtube" to youtubeConnected,
        "instagram" to instagramConnected,
        "facebook" to facebookConnected
    )

    val selectedPlatforms = buildList {
        if (selectedYoutube && youtubeConnected) add("youtube")
        if (selectedInstagram && instagramConnected) add("instagram")
        if (selectedFacebook && facebookConnected) add("facebook")
    }

    val scheduleReady = postNow || (scheduledDateMillis != null && scheduledHour != null && scheduledMinute != null)
    val canSubmit = mediaSelected && selectedPlatforms.isNotEmpty() && scheduleReady

    val buttonLabel = if (postNow) "Post now" else "Schedule"

    val ctaPulse = remember { Animatable(0f) }
    LaunchedEffect(canSubmit) {
        if (canSubmit) {
            ctaPulse.snapTo(0.8f)
            ctaPulse.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Create", color = Color.White, fontWeight = FontWeight.SemiBold) },
                actions = {
                    TextButton(onClick = {
                        captionAll = ""
                        captionYoutube = ""
                        captionInstagram = ""
                        captionFacebook = ""
                    }) {
                        Text("Clear", color = Color(0xFFAEC7FF))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Column {
                    Box {
                        if (ctaPulse.value > 0f || canSubmit) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                GradientBrand[0].copy(alpha = 0.2f + ctaPulse.value * 0.2f),
                                                GradientBrand[2].copy(alpha = 0.2f + ctaPulse.value * 0.2f)
                                            )
                                        ),
                                        RoundedCornerShape(999.dp)
                                    )
                            )
                        }
                        Button(
                            onClick = {
                                val chosenEpoch = if (postNow) System.currentTimeMillis() else {
                                    val c = Calendar.getInstance()
                                    c.timeInMillis = scheduledDateMillis ?: System.currentTimeMillis()
                                    c.set(Calendar.HOUR_OF_DAY, scheduledHour ?: 0)
                                    c.set(Calendar.MINUTE, scheduledMinute ?: 0)
                                    c.timeInMillis
                                }
                                val captions = if (useSameCaption) {
                                    selectedPlatforms.associateWith { captionAll }
                                } else {
                                    mapOf(
                                        "youtube" to captionYoutube,
                                        "instagram" to captionInstagram,
                                        "facebook" to captionFacebook
                                    ).filterKeys { it in selectedPlatforms }
                                }
                                val post = ScheduledPost(
                                    id = "post_${Random.nextInt(1000, 9999)}",
                                    mediaUri = "content://mock/$mediaName",
                                    mediaName = mediaName,
                                    mediaType = if (mediaName.endsWith("mp4")) "video" else "image",
                                    platforms = selectedPlatforms,
                                    captionMap = captions,
                                    scheduledEpochMillis = chosenEpoch,
                                    status = PostStatus.SCHEDULED,
                                    lastError = null,
                                    createdAt = System.currentTimeMillis()
                                )
                                postsViewModel.createPost(post)
                                successPost = post
                            },
                            enabled = canSubmit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canSubmit) GradientBrand[0] else Color(0xFF39455D),
                                disabledContainerColor = Color(0xFF39455D)
                            )
                        ) {
                            Text(buttonLabel, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text(
                        "You can edit in Calendar.",
                        color = Color(0xFF8B97AB),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    )
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = bottomPadding + 120.dp)
            ) {
                item {
                    AnimatedVisibility(
                        visible = sectionsVisible[0],
                        enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(300))
                    ) {
                        MediaPickerCard(
                            mediaSelected = mediaSelected,
                            mediaName = mediaName,
                            mediaSize = mediaSize,
                            mediaDuration = mediaDuration,
                            mediaUploading = mediaUploading,
                            uploadProgress = uploadProgress,
                            onChoose = {
                                mediaSelected = true
                                mediaUploading = true
                                mediaName = "reel_2026.mp4"
                                mediaSize = "18.4 MB"
                                mediaDuration = "00:27"
                            }
                        )
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = sectionsVisible[1],
                        enter = fadeIn(tween(300, delayMillis = 80)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(300, delayMillis = 80))
                    ) {
                        PlatformSelectorSection(
                            youtubeConnected = youtubeConnected,
                            instagramConnected = instagramConnected,
                            facebookConnected = facebookConnected,
                            selectedYoutube = selectedYoutube,
                            selectedInstagram = selectedInstagram,
                            selectedFacebook = selectedFacebook,
                            onSelectedChange = { key, selected ->
                                when (key) {
                                    "youtube" -> selectedYoutube = selected
                                    "instagram" -> selectedInstagram = selected
                                    "facebook" -> selectedFacebook = selected
                                }
                            },
                            onConnectTap = onOpenConnectPopup
                        )
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = sectionsVisible[2],
                        enter = fadeIn(tween(300, delayMillis = 150)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(300, delayMillis = 150))
                    ) {
                        CaptionSection(
                            connectedMap = connectedMap,
                            useSameCaption = useSameCaption,
                            onUseSameChange = { useSameCaption = it },
                            activeTab = activeCaptionTab,
                            onTabChange = { activeCaptionTab = it },
                            captionAll = captionAll,
                            onCaptionAll = { captionAll = it },
                            captionYoutube = captionYoutube,
                            onCaptionYoutube = { captionYoutube = it },
                            captionInstagram = captionInstagram,
                            onCaptionInstagram = { captionInstagram = it },
                            captionFacebook = captionFacebook,
                            onCaptionFacebook = { captionFacebook = it }
                        )
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = sectionsVisible[3],
                        enter = fadeIn(tween(300, delayMillis = 220)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(300, delayMillis = 220))
                    ) {
                        ScheduleSection(
                            postNow = postNow,
                            onPostNowChange = { postNow = it },
                            scheduledDateMillis = scheduledDateMillis,
                            scheduledHour = scheduledHour,
                            scheduledMinute = scheduledMinute,
                            onPickDate = {
                                val cal = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val picked = Calendar.getInstance()
                                        picked.set(y, m, d, 0, 0, 0)
                                        scheduledDateMillis = picked.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            onPickTime = {
                                val now = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        scheduledHour = h
                                        scheduledMinute = m
                                    },
                                    now.get(Calendar.HOUR_OF_DAY),
                                    now.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }

    successPost?.let { post ->
        ScheduledSuccessSheet(
            post = post,
            onViewCalendar = {
                successPost = null
                onNavigateCalendar()
            },
            onCreateAnother = {
                successPost = null
                mediaSelected = false
                selectedYoutube = false
                selectedInstagram = false
                selectedFacebook = false
                captionAll = ""
                captionYoutube = ""
                captionInstagram = ""
                captionFacebook = ""
                scheduledDateMillis = null
                scheduledHour = null
                scheduledMinute = null
                postNow = true
            }
        )
    }
}

@Composable
private fun MediaPickerCard(
    mediaSelected: Boolean,
    mediaName: String,
    mediaSize: String,
    mediaDuration: String,
    mediaUploading: Boolean,
    uploadProgress: Float,
    onChoose: () -> Unit
) {
    Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF18233A), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            if (!mediaSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF202B43)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFFAEC1E9), modifier = Modifier.size(34.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Upload video or photo", color = Color(0xFFD9E3F7))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onChoose, shape = RoundedCornerShape(12.dp)) { Text("Choose file") }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.verticalGradient(listOf(GradientBrand[0], GradientBrand[2]))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SmartDisplay, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                        if (mediaUploading) {
                            CircularProgressIndicator(progress = uploadProgress, color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(60.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(mediaName, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$mediaDuration • $mediaSize", color = Color(0xFF9FAECC), fontSize = 12.sp)
                        if (mediaUploading) {
                            Text("Uploading ${(uploadProgress * 100).toInt()}%", color = Color(0xFFC8D4EE), fontSize = 12.sp)
                        }
                    }
                    TextButton(onClick = onChoose) { Text("Replace") }
                }
            }
        }
    }
}

@Composable
private fun PlatformSelectorSection(
    youtubeConnected: Boolean,
    instagramConnected: Boolean,
    facebookConnected: Boolean,
    selectedYoutube: Boolean,
    selectedInstagram: Boolean,
    selectedFacebook: Boolean,
    onSelectedChange: (String, Boolean) -> Unit,
    onConnectTap: () -> Unit
) {
    Text("Post to", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(10.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        createPlatforms.forEach { platform ->
            val connected = when (platform.key) {
                "youtube" -> youtubeConnected
                "instagram" -> instagramConnected
                else -> facebookConnected
            }
            val selected = when (platform.key) {
                "youtube" -> selectedYoutube
                "instagram" -> selectedInstagram
                else -> selectedFacebook
            }
            CreatePlatformCard(
                platform = platform,
                connected = connected,
                selected = selected,
                modifier = Modifier.weight(1f),
                onToggle = { onSelectedChange(platform.key, !selected) },
                onConnect = onConnectTap
            )
        }
    }
}

@Composable
private fun CreatePlatformCard(
    platform: CreatePlatform,
    connected: Boolean,
    selected: Boolean,
    modifier: Modifier,
    onToggle: () -> Unit,
    onConnect: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val iconTint by animateColorAsState(
        targetValue = when {
            !connected -> Color(0xFFB0B7C3)
            selected -> platform.color
            else -> Color(0xFF7E8BA7)
        },
        animationSpec = tween(260),
        label = "icon_tint"
    )
    val elevation by animateFloatAsState(targetValue = if (selected) 14f else 8f, animationSpec = tween(220), label = "elevation")
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation.dp, RoundedCornerShape(20.dp))
                .scale(scale)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = {
                        if (connected) onToggle() else onConnect()
                    }
                ),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = if (connected) 1f else 0.78f)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(platform.icon, contentDescription = platform.label, tint = iconTint, modifier = Modifier.size(24.dp))
                    AnimatedVisibility(
                        visible = selected,
                        enter = scaleIn(tween(180)) + fadeIn(tween(180))
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFF22C55E), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                        }
                    }
                    if (!connected) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFF4B5568), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(platform.label, color = if (connected) Color(0xFF1E293B) else Color(0xFF7B8799), fontSize = 12.sp)
            }
        }
        if (!connected) {
            TextButton(onClick = onConnect) {
                Text("Connect", fontSize = 12.sp, color = Color(0xFFAEC3F9))
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CaptionSection(
    connectedMap: Map<String, Boolean>,
    useSameCaption: Boolean,
    onUseSameChange: (Boolean) -> Unit,
    activeTab: String,
    onTabChange: (String) -> Unit,
    captionAll: String,
    onCaptionAll: (String) -> Unit,
    captionYoutube: String,
    onCaptionYoutube: (String) -> Unit,
    captionInstagram: String,
    onCaptionInstagram: (String) -> Unit,
    captionFacebook: String,
    onCaptionFacebook: (String) -> Unit
) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF162136)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Caption", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Use same caption for all", color = Color(0xFFD2DCF1), fontSize = 13.sp)
                Switch(checked = useSameCaption, onCheckedChange = onUseSameChange)
            }

            AnimatedContent(
                targetState = useSameCaption,
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInVertically { it / 4 }) togetherWith
                            (fadeOut(tween(180)) + slideOutVertically { -it / 4 })
                },
                label = "caption_mode"
            ) { same ->
                if (same) {
                    Column {
                        OutlinedTextField(
                            value = captionAll,
                            onValueChange = onCaptionAll,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Write your caption") }
                        )
                        Text(
                            "${captionAll.length}/5000",
                            modifier = Modifier.align(Alignment.End),
                            color = Color(0xFF9DAAC4),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    val tabs = listOf("youtube", "instagram", "facebook").filter { connectedMap[it] == true }
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            tabs.forEach { key ->
                                FilterChip(
                                    selected = activeTab == key,
                                    onClick = { onTabChange(key) },
                                    label = { Text(key.replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        AnimatedContent(
                            targetState = activeTab,
                            transitionSpec = {
                                (fadeIn(tween(220)) + slideInVertically { it / 5 }) togetherWith
                                        (fadeOut(tween(180)) + slideOutVertically { -it / 5 })
                            },
                            label = "caption_tab"
                        ) { tab ->
                            when (tab) {
                                "youtube" -> CaptionField(captionYoutube, onCaptionYoutube, 5000)
                                "instagram" -> CaptionField(captionInstagram, onCaptionInstagram, 2200)
                                else -> CaptionField(captionFacebook, onCaptionFacebook, 5000)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptionField(value: String, onChange: (String) -> Unit, limit: Int) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= limit) onChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Write your caption") }
        )
        Text(
            "${value.length}/$limit",
            modifier = Modifier.align(Alignment.End),
            color = Color(0xFF9DAAC4),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ScheduleSection(
    postNow: Boolean,
    onPostNowChange: (Boolean) -> Unit,
    scheduledDateMillis: Long?,
    scheduledHour: Int?,
    scheduledMinute: Int?,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit
) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF162136)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Schedule", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = postNow, onClick = { onPostNowChange(true) }, label = { Text("Post now") })
                FilterChip(selected = !postNow, onClick = { onPostNowChange(false) }, label = { Text("Schedule") })
            }

            AnimatedVisibility(visible = !postNow, enter = fadeIn() + slideInVertically { it / 4 }) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    ScheduleRow(
                        icon = Icons.Default.CalendarMonth,
                        label = if (scheduledDateMillis != null) {
                            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(scheduledDateMillis)
                        } else "Select date",
                        onClick = onPickDate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ScheduleRow(
                        icon = Icons.Default.Timer,
                        label = if (scheduledHour != null && scheduledMinute != null) {
                            String.format(Locale.getDefault(), "%02d:%02d", scheduledHour, scheduledMinute)
                        } else "Select time",
                        onClick = onPickTime
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Timezone: Europe/London", color = Color(0xFF9DAAC4), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ScheduleRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF22314B),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFC7D4ED))
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF99A9C8))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduledSuccessSheet(
    post: ScheduledPost,
    onViewCalendar: () -> Unit,
    onCreateAnother: () -> Unit
) {
    val scale = remember { Animatable(0.7f) }
    LaunchedEffect(post.id) {
        scale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 380f))
    }

    ModalBottomSheet(
        onDismissRequest = onCreateAnother,
        containerColor = Color(0xFF152139),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF58E69C),
                modifier = Modifier
                    .size(58.dp)
                    .scale(scale.value)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("Scheduled!", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${post.mediaName} • ${post.platforms.joinToString().replaceFirstChar { it.uppercase() }}",
                color = Color(0xFFC8D4EA),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(post.scheduledEpochMillis),
                color = Color(0xFF9DB2D8),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(onClick = onViewCalendar, modifier = Modifier.fillMaxWidth()) { Text("View in Calendar") }
            TextButton(onClick = onCreateAnother) { Text("Create another", color = Color(0xFFC9D8F4)) }
        }
    }
}
