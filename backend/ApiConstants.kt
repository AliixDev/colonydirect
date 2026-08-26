package com.colonydirect.app.network

import com.colonydirect.app.BuildConfig

/**
 * Central networking constants.
 *
 * BASE_URL is injected from BuildConfig (set in app/build.gradle.kts).
 * For a real device on the same Wi-Fi, change BASE_URL to your machine's LAN IP.
 * For Android Emulator hitting localhost: use 10.0.2.2 (the default).
 */
object ApiConstants {
    val BASE_URL: String get() = BuildConfig.BASE_URL
    const val API_VERSION = "/api/v1"
}
