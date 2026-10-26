package com.skyhightechnology.restaurantengine

/**
 * Desktop has no packaged app identifier, so the tenant is chosen with
 * `-Dtenant.applicationId=<id>` (e.g. `com.burgerjoint.app`), falling back to the engine's own ID.
 */
actual fun getApplicationId(): String =
    System.getProperty("tenant.applicationId") ?: "com.skyhightechnology.restaurantengine"
