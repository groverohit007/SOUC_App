package com.GR8Studios.souc

import android.app.Application
import androidx.work.Configuration

class SoucApp : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
}
