package com.skyhightechnology.restaurantengine.presentation

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/** The English text of [resource], so UI tests find nodes by the same strings the app shows. */
fun text(resource: StringResource, vararg args: Any): String = runBlocking { getString(resource, *args) }
