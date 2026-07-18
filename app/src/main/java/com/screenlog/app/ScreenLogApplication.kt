package com.screenlog.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ScreenLogApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase, local analytics, or configurations here
    }
}
