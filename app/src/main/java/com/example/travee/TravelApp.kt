package com.example.travee

import android.app.Application
import android.content.Context
import com.example.travee.data.ThemeManager

class TravelApp : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // Initialize ThemeManager
        ThemeManager.getInstance().initialize(this)
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}
