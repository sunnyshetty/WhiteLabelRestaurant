package com.skyhightechnology.restaurantengine.presentation

import app.cash.turbine.ReceiveTurbine

/** Skips states until one matches. StateFlow conflates, so transient states (e.g. loading) may never be observed. */
suspend fun <T> ReceiveTurbine<T>.awaitItemMatching(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
