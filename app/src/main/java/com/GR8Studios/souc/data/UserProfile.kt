package com.GR8Studios.souc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val uid: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val subscriptionTier: String = SubscriptionTier.FREE,
    val monthlyPostLimit: Int = AppDefaults.FREE_POST_LIMIT,
    val postsUsedThisMonth: Int = 0,
    val subscriptionExpiryMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
) {
    val isPremium: Boolean get() = subscriptionTier == SubscriptionTier.PREMIUM
    val isAdmin: Boolean get() = email == AppDefaults.MASTER_EMAIL
    val canPost: Boolean get() = isPremium || isAdmin || postsUsedThisMonth < monthlyPostLimit
    val remainingPosts: Int get() = if (isPremium || isAdmin) Int.MAX_VALUE else (monthlyPostLimit - postsUsedThisMonth).coerceAtLeast(0)
}

object SubscriptionTier {
    const val FREE = "free"
    const val PREMIUM = "premium"
}

object AppDefaults {
    const val MASTER_EMAIL = "groverohit0@gmail.com"
    const val FREE_POST_LIMIT = 5
    const val PREMIUM_PRICE_MONTHLY = 4.99
}
