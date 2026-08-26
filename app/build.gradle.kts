plugins {
    id("com.android.application")
    kotlin("plugin.compose")
}

android {
    namespace = "com.colonydirect.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.colonydirect.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "0.4.0"

        // Override this to your actual backend address for device/production builds.
        // Example: COLONYDIRECT_BASE_URL=https://api.colonydirect.pk
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")
    }

    signingConfigs {
        // Release signing config: provide these via local.properties or CI env vars.
        // local.properties example:
        //   keystore.file=../keystore.jks
        //   keystore.password=mypass
        //   keystore.alias=colonydirect
        //   keystore.keyPassword=mypass
        create("release") {
            val keystoreFile = rootProject.file(
                findProperty("keystore.file")?.toString() ?: "keystore.jks"
            )
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = findProperty("keystore.password")?.toString() ?: System.getenv("KEYSTORE_PASSWORD")
                keyAlias = findProperty("keystore.alias")?.toString() ?: System.getenv("KEY_ALIAS") ?: "colonydirect"
                keyPassword = findProperty("keystore.keyPassword")?.toString() ?: System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            // Only apply release signing if the keystore file is present.
            if (releaseSigning.storeFile?.exists() == true) {
                signingConfig = releaseSigning
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Generate an AAB for Play Store submission; APK for direct installs.
    // Run: ./gradlew bundleRelease  → produces .aab
    // Run: ./gradlew assembleRelease → produces .apk
}

dependencies {
    // Compose BOM — manages all Compose version alignment
    implementation(platform("androidx.compose:compose-bom:2025.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Core AndroidX
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("androidx.activity:activity-compose:1.10.1")

    // ViewModel + Compose integration
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.0")

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Gson
    implementation("com.google.code.gson:gson:2.11.0")

    // DataStore (token + prefs persistence)
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Splash Screen API (Step 4 polish)
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
