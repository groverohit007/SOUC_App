package com.GR8Studios.souc

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.GR8Studios.souc.data.PostStatus
import com.GR8Studios.souc.data.ScheduledPost
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun platformColor(platform: String): Color = when (platform.lowercase()) {
    "youtube" -> Color(0xFFFF3D3D)
    "instagram" -> Color(0xFFE1306C)
    "facebook" -> Color(0xFF1877F2)
    else -> Color(0xFF94A3B8)
}

private fun platformIcon(platform: String) = when (platform.lowercase()) {
    "youtube" -> Icons.Default.SmartDisplay
    "instagram" -> Icons.Default.Image
    "facebook" -> Icons.Default.People
    else -> Icons.Default.CalendarMonth
}

private fun normalizeStatus(status: String): String = when (status.uppercase()) {
    "POSTING" -> PostStatus.SCHEDULED
    PostStatus.SCHEDULED, PostStatus.POSTED, PostStatus.FAILED -> status.uppercase()
    else -> PostStatus.SCHEDULED
}

fun groupPostsByDate(posts: List<ScheduledPost>): List<Pair<Long, List<ScheduledPost>>> {
    return posts
        .groupBy { startOfDay(it.scheduledEpochMillis) }
        .toSortedMap()
        .map { (day, list) -> day to list.sortedBy { it.scheduledEpochMillis } }
}

private fun startOfDay(epochMillis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = epochMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private enum class CalendarTab { Agenda, Month }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(bottomPadding: Dp, onCreatePost: () -> Unit) {
    val postsViewModel: PostsViewModel = viewModel()
    val uiPosts by postsViewModel.posts.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()

    var activeTab by rememberSaveable { mutableStateOf(CalendarTab.Agenda) }
    var activeFilter by rememberSaveable { mutableStateOf("all") }
    var monthAnchor by rememberSaveable { mutableStateOf(startOfDay(System.currentTimeMillis())) }
    var selectedMonthDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingPostId by rememberSaveable { mutableStateOf<String?>(null) }

    val entryVisible = remember { mutableStateListOf(false, false, false) }
    LaunchedEffect(Unit) {
        entryVisible.indices.forEachIndexed { index, _ ->
            delay(80L * index)
            entryVisible[index] = true
        }
    }

    val allPosts = uiPosts
    val filteredPosts = allPosts
        .filter {
            activeFilter == "all" || it.platforms.any { p -> p.equals(activeFilter, ignoreCase = true) }
        }
        .map { post -> if (post.status == normalizeStatus(post.status)) post else post.copy(status = normalizeStatus(post.status)) }
        .sortedBy { it.scheduledEpochMillis }

    val agendaGroups = groupPostsByDate(filteredPosts)

    val monthCalendar = remember(monthAnchor) {
        Calendar.getInstance().apply {
            timeInMillis = monthAnchor
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Calendar", color = Color.White, fontWeight = FontWeight.SemiBold) },
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
                contentPadding = PaddingValues(top = 10.dp, bottom = bottomPadding + 26.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AnimatedVisibility(
                        visible = entryVisible[0],
                        enter = fadeIn(tween(280)) + slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(280))
                    ) {
                        SegmentTabs(activeTab = activeTab, onTabChange = { activeTab = it })
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = entryVisible[1],
                        enter = fadeIn(tween(280, delayMillis = 60)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(280, delayMillis = 60))
                    ) {
                        FilterRow(activeFilter = activeFilter, onFilterChange = { activeFilter = it })
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = entryVisible[2],
                        enter = fadeIn(tween(280, delayMillis = 120)) +
                                slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(280, delayMillis = 120))
                    ) {
                        AnimatedContent(
                            targetState = activeTab,
                            transitionSpec = {
                                (fadeIn(tween(220)) + slideInVertically { it / 5 }) togetherWith
                                        (fadeOut(tween(180)) + slideOutVertically { -it / 5 })
                            },
                            label = "tab_switch"
                        ) { tab ->
                            if (filteredPosts.isEmpty()) {
                                EmptyCalendarState(onCreatePost = onCreatePost)
                            } else {
                                when (tab) {
                                    CalendarTab.Agenda -> AgendaList(
                                        agendaGroups = agendaGroups,
                                        onEdit = { editingPostId = it.id },
                                        onDuplicate = { postsViewModel.duplicatePost(it.id) },
                                        onDelete = { postsViewModel.deletePost(it.id) },
                                        onRetry = {
                                            postsViewModel.retryPost(it.id)
                                            snackbarScope.launch { snackbarHostState.showSnackbar("Retry queued") }
                                        }
                                    )

                                    CalendarTab.Month -> MonthTab(
                                        monthCalendar = monthCalendar,
                                        posts = filteredPosts,
                                        selectedDay = selectedMonthDay,
                                        onMonthShift = { delta ->
                                            val c = Calendar.getInstance().apply { timeInMillis = monthAnchor }
                                            c.add(Calendar.MONTH, delta)
                                            monthAnchor = startOfDay(c.timeInMillis)
                                        },
                                        onDaySelect = { selectedMonthDay = it },
                                        onEdit = { editingPostId = it.id },
                                        onDuplicate = { postsViewModel.duplicatePost(it.id) },
                                        onDelete = { postsViewModel.deletePost(it.id) },
                                        onRetry = {
                                            postsViewModel.retryPost(it.id)
                                            snackbarScope.launch { snackbarHostState.showSnackbar("Retry queued") }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val editingPost = allPosts.firstOrNull { it.id == editingPostId }
    if (editingPost != null) {
        EditPostBottomSheet(
            post = editingPost,
            onDismiss = { editingPostId = null },
            onSaveTime = { hour, minute ->
                val c = Calendar.getInstance().apply {
                    timeInMillis = editingPost.scheduledEpochMillis
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                postsViewModel.updatePost(editingPost.copy(scheduledEpochMillis = c.timeInMillis, status = PostStatus.SCHEDULED, lastError = null))
                editingPostId = null
            },
            onOpenTimePicker = { currentH, currentM, onPicked ->
                TimePickerDialog(context, { _, h, m -> onPicked(h, m) }, currentH, currentM, true).show()
            }
        )
    }
}

@Composable
private fun SegmentTabs(activeTab: CalendarTab, onTabChange: (CalendarTab) -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF172033), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(CalendarTab.Agenda, CalendarTab.Month).forEach { tab ->
                val selected = tab == activeTab
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) Color(0xFF2A3753) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabChange(tab) }
                ) {
                    Text(
                        text = if (tab == CalendarTab.Agenda) "Agenda" else "Month",
                        color = if (selected) Color.White else Color(0xFFA1AEC7),
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterRow(activeFilter: String, onFilterChange: (String) -> Unit) {
    val filters = listOf("all", "youtube", "instagram", "facebook")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        filters.forEach { filter ->
            val selected = activeFilter == filter
            val activeColor = if (filter == "all") Color(0xFF64748B) else platformColor(filter)
            FilterChip(
                selected = selected,
                onClick = { onFilterChange(filter) },
                label = {
                    Text(
                        filter.replaceFirstChar { it.uppercase() },
                        color = if (selected) Color.White else Color(0xFFB5C1D8)
                    )
                },
                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    selectedContainerColor = activeColor.copy(alpha = 0.75f),
                    containerColor = Color(0xFF1A2740)
                )
            )
        }
    }
}

@Composable
private fun EmptyCalendarState(onCreatePost: () -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFF162136), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF8FA5CC), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Nothing scheduled yet", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = onCreatePost) { Text("Create a post") }
        }
    }
}

@Composable
private fun AgendaList(
    agendaGroups: List<Pair<Long, List<ScheduledPost>>>,
    onEdit: (ScheduledPost) -> Unit,
    onDuplicate: (ScheduledPost) -> Unit,
    onDelete: (ScheduledPost) -> Unit,
    onRetry: (ScheduledPost) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        agendaGroups.forEach { (day, posts) ->
            Text(
                text = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date(day)),
                color = Color(0xFFB5C1D8),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp)
            )
            posts.forEach { post ->
                CalendarPostCard(
                    post = post,
                    onEdit = { onEdit(post) },
                    onDuplicate = { onDuplicate(post) },
                    onDelete = { onDelete(post) },
                    onRetry = { onRetry(post) }
                )
            }
        }
    }
}

@Composable
fun MonthGrid(
    monthCalendar: Calendar,
    posts: List<ScheduledPost>,
    selectedDay: Long?,
    onSelectDay: (Long) -> Unit
) {
    val firstWeekday = ((monthCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7)
    val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

    val postsByDay = posts.groupBy { startOfDay(it.scheduledEpochMillis) }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            dayNames.forEach { day ->
                Text(day, color = Color(0xFF8EA0C3), fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        val totalCells = ((firstWeekday + daysInMonth + 6) / 7) * 7
        var dayCounter = 1
        repeat(totalCells / 7) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(7) { col ->
                    val cellIndex = it * 7 + col
                    if (cellIndex < firstWeekday || dayCounter > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f).height(56.dp))
                    } else {
                        val dayCalendar = Calendar.getInstance().apply {
                            timeInMillis = monthCalendar.timeInMillis
                            set(Calendar.DAY_OF_MONTH, dayCounter)
                        }
                        val dayStart = startOfDay(dayCalendar.timeInMillis)
                        val dayPosts = postsByDay[dayStart].orEmpty()
                        val selected = selectedDay == dayStart

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable { onSelectDay(dayStart) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) Color(0xFF2A3A58) else Color(0xFF17233A)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(top = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(dayCounter.toString(), color = if (selected) Color.White else Color(0xFFB8C4DA), fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    dayPosts
                                        .flatMap { it.platforms }
                                        .distinct()
                                        .take(3)
                                        .forEach { platform ->
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .background(platformColor(platform), CircleShape)
                                            )
                                        }
                                }
                            }
                        }
                        dayCounter++
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun MonthTab(
    monthCalendar: Calendar,
    posts: List<ScheduledPost>,
    selectedDay: Long?,
    onMonthShift: (Int) -> Unit,
    onDaySelect: (Long) -> Unit,
    onEdit: (ScheduledPost) -> Unit,
    onDuplicate: (ScheduledPost) -> Unit,
    onDelete: (ScheduledPost) -> Unit,
    onRetry: (ScheduledPost) -> Unit
) {
    Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFF152139), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onMonthShift(-1) }) { Icon(Icons.Default.ChevronLeft, null, tint = Color.White) }
                AnimatedContent(
                    targetState = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(monthCalendar.timeInMillis)),
                    transitionSpec = {
                        fadeIn(tween(220)) togetherWith fadeOut(tween(150))
                    },
                    label = "month_title"
                ) { title ->
                    Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                }
                IconButton(onClick = { onMonthShift(1) }) { Icon(Icons.Default.ChevronRight, null, tint = Color.White) }
            }

            MonthGrid(monthCalendar = monthCalendar, posts = posts, selectedDay = selectedDay, onSelectDay = onDaySelect)

            selectedDay?.let { day ->
                val dayPosts = posts.filter { startOfDay(it.scheduledEpochMillis) == day }.sortedBy { it.scheduledEpochMillis }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Selected day", color = Color(0xFFAEBBD4), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (dayPosts.isEmpty()) {
                    Text("No posts on this day", color = Color(0xFF7F8CA5), fontSize = 12.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dayPosts.forEach { post ->
                            CalendarPostCard(post, onEdit = { onEdit(post) }, onDuplicate = { onDuplicate(post) }, onDelete = { onDelete(post) }, onRetry = { onRetry(post) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: ScheduledPost,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit
) {
    CalendarPostCard(post = post, onEdit = onEdit, onDuplicate = onDuplicate, onDelete = onDelete, onRetry = onRetry)
}

@Composable
private fun CalendarPostCard(
    post: ScheduledPost,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_press"
    )

    var menuExpanded by remember { mutableStateOf(false) }
    val status = normalizeStatus(post.status)
    val statusColor by animateColorAsState(
        targetValue = when (status) {
            PostStatus.POSTED -> Color(0xFF34D399)
            PostStatus.FAILED -> Color(0xFFFB7185)
            else -> Color(0xFF60A5FA)
        },
        animationSpec = tween(260),
        label = "status_color"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(22.dp))
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { },
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF121A2A)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Brush.verticalGradient(listOf(GradientBrand[0].copy(alpha = 0.45f), GradientBrand[2].copy(alpha = 0.45f))), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (post.mediaType.equals("video", true)) Icons.Default.SmartDisplay else Icons.Default.Image,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(post.mediaName, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(post.scheduledEpochMillis)), color = Color(0xFF9EACC8), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    post.platforms.forEach { p ->
                        Icon(platformIcon(p), contentDescription = p, tint = platformColor(p), modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Surface(shape = RoundedCornerShape(999.dp), color = statusColor.copy(alpha = 0.2f)) {
                        Text(status, color = statusColor, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color(0xFFB8C5DE))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { menuExpanded = false; onEdit() }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                    DropdownMenuItem(text = { Text("Duplicate") }, onClick = { menuExpanded = false; onDuplicate() }, leadingIcon = { Icon(Icons.Default.CheckCircle, null) })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { menuExpanded = false; onDelete() }, leadingIcon = { Icon(Icons.Default.ErrorOutline, null) })
                    if (status == PostStatus.FAILED) {
                        DropdownMenuItem(text = { Text("Retry") }, onClick = { menuExpanded = false; onRetry() }, leadingIcon = { Icon(Icons.Default.PlayArrow, null) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBottomSheet(
    post: ScheduledPost,
    onDismiss: () -> Unit,
    onSaveTime: (Int, Int) -> Unit,
    onOpenTimePicker: (Int, Int, (Int, Int) -> Unit) -> Unit
) {
    EditPostBottomSheet(post, onDismiss, onSaveTime, onOpenTimePicker)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPostBottomSheet(
    post: ScheduledPost,
    onDismiss: () -> Unit,
    onSaveTime: (Int, Int) -> Unit,
    onOpenTimePicker: (Int, Int, (Int, Int) -> Unit) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(Calendar.getInstance().apply { timeInMillis = post.scheduledEpochMillis }.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(Calendar.getInstance().apply { timeInMillis = post.scheduledEpochMillis }.get(Calendar.MINUTE)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF152139),
        contentColor = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text("Edit post", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(post.mediaName, color = Color(0xFFCCDAF2))
            Spacer(modifier = Modifier.height(6.dp))
            Text("Platforms: ${post.platforms.joinToString()}", color = Color(0xFFA7B6D1), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Caption: ${post.captionMap.values.firstOrNull()?.take(60).orEmpty().ifEmpty { "(empty)" }}",
                color = Color(0xFFA7B6D1),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF21314C),
                modifier = Modifier.fillMaxWidth().clickable {
                    onOpenTimePicker(selectedHour, selectedMinute) { h, m ->
                        selectedHour = h
                        selectedMinute = m
                    }
                }
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFBBD0F2))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(String.format(Locale.getDefault(), "Reschedule to %02d:%02d", selectedHour, selectedMinute))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Button(onClick = { onSaveTime(selectedHour, selectedMinute) }, modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Close", color = Color(0xFFB6C6E4))
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

