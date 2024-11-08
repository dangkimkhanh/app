package com.example.h2h

import android.app.Application
import com.facebook.FacebookSdk // Thêm dòng này
import com.facebook.appevents.AppEventsLogger // Thêm dòng này

@Suppress("DEPRECATION")
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
    }
}