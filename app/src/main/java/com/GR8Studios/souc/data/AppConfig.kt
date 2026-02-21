package com.GR8Studios.souc.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppConfig {
    var freePostLimit by mutableIntStateOf(AppDefaults.FREE_POST_LIMIT)
    var premiumPriceMonthly by mutableStateOf(AppDefaults.PREMIUM_PRICE_MONTHLY)
}
