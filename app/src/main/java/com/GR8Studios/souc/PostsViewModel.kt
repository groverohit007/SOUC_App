package com.GR8Studios.souc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.GR8Studios.souc.data.PostStatus
import com.GR8Studios.souc.data.PostsRepository
import com.GR8Studios.souc.data.ScheduledPost
import com.GR8Studios.souc.data.local.SoucDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PostsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PostsRepository(
        context = application,
        dao = SoucDatabase.getInstance(application).scheduledPostDao()
    )

    val posts: StateFlow<List<ScheduledPost>> = repository.posts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun createPost(post: ScheduledPost) {
        viewModelScope.launch { repository.createPost(post) }
    }

    fun updatePost(post: ScheduledPost) {
        viewModelScope.launch { repository.updatePost(post) }
    }

    fun deletePost(id: String) {
        viewModelScope.launch { repository.deletePost(id) }
    }

    fun retryPost(id: String) {
        viewModelScope.launch { repository.retryPost(id) }
    }

    fun duplicatePost(id: String) {
        viewModelScope.launch { repository.duplicatePost(id) }
    }

    fun markUploading(post: ScheduledPost) {
        updatePost(post.copy(status = PostStatus.UPLOADING))
    }
}
