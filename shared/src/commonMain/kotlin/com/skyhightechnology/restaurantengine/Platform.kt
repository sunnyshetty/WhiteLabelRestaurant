package com.skyhightechnology.restaurantengine

/**
 * The running app's platform identifier (Android `applicationId`, iOS bundle identifier).
 *
 * This is the white-label tenant key: each brand ships under its own identifier, and the
 * engine uses it at runtime to resolve which restaurant's data and branding to load.
 */
expect fun getApplicationId(): String
