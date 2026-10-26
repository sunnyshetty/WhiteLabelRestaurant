package com.skyhightechnology.restaurantengine

import android.app.Application
import com.skyhightechnology.restaurantengine.di.initKoin
import org.koin.android.ext.koin.androidContext

class WhiteLabelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidPlatform.init(this)
        initKoin {
            androidContext(this@WhiteLabelApplication)
        }
    }
}
