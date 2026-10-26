package com.skyhightechnology.restaurantengine

import android.content.Context

/**
 * Holds the application [Context] so [getApplicationId] can read `packageName` without
 * every caller passing a Context through common code. Must be initialized from
 * `Application.onCreate()` before the engine is used.
 */
object AndroidPlatform {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    internal val context: Context
        get() = checkNotNull(appContext) {
            "AndroidPlatform.init(context) must be called from Application.onCreate()"
        }
}

actual fun getApplicationId(): String = AndroidPlatform.context.packageName
