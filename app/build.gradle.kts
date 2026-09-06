import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

// Load local.properties for environment variables
val localPropertiesFile: File = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

// Load .env file for environment variables (if exists)
val envFile: File = rootProject.file(".env")
if (envFile.exists()) {
    envFile.readLines().forEach { line ->
        val trimmedLine = line.trim()
        if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
            val keyValue = trimmedLine.split("=", limit = 2)
            if (keyValue.size == 2) {
                val key = keyValue[0].trim()
                val value = keyValue[1].trim()
                // Only set if not already in localProperties
                if (!localProperties.containsKey(key)) {
                    localProperties.setProperty(key, value)
                }
            }
        }
    }
}

android {
    namespace = "com.mytimetablemaker"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.mytimetablemaker"
        minSdk = 24
        targetSdk = 37
        versionCode = 57
        versionName = "2.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // The AdMob app id is not injected here any more: it is written out in
        // AndroidManifest.xml. It ships inside every copy of the app, so hiding
        // it in local.properties protected nothing and left the value existing
        // on one machine only. Unit ids stay in local.properties.

        // Get ODPT Access Token from local.properties and set in BuildConfig
        val odptAccessToken = localProperties.getProperty("ODPT_ACCESS_TOKEN") ?: ""
        buildConfigField("String", "ODPT_ACCESS_TOKEN", "\"$odptAccessToken\"")
        
        // Get ODPT Challenge Token from local.properties and set in BuildConfig
        val odptChallengeToken = localProperties.getProperty("ODPT_CHALLENGE_TOKEN") ?: ""
        buildConfigField("String", "ODPT_CHALLENGE_TOKEN", "\"$odptChallengeToken\"")
    }

    buildTypes {
        debug {
            // Adaptive banners have their own demo unit. The fixed size one
            // (6300978111) only serves 320x50, which makes every adaptive size
            // measured against it look like 320x50
            resValue("string", "admob_banner_unit_id", "ca-app-pub-3940256099942544/9214589741")
            // App Check debug secret, so one registered token covers every
            // device instead of the SDK generating one per install
            buildConfigField(
                "String",
                "APP_CHECK_DEBUG_TOKEN",
                "\"${localProperties.getProperty("APP_CHECK_DEBUG_TOKEN") ?: ""}\""
            )
        }
        
        release {
            // Not read in release: PlayIntegrity is used there, but BuildConfig
            // needs the field defined in every build type
            buildConfigField("String", "APP_CHECK_DEBUG_TOKEN", "\"\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Falls back to the test unit so that configuring the project
            // still works without local.properties. The fallback is what let
            // this app ship test ads unnoticed, so verifyAdMobConfig below
            // stops the release before it can be packaged again
            val admobBannerUnitId = localProperties.getProperty("ADMOB_BANNER_UNIT_ID")
                ?: "ca-app-pub-3940256099942544/9214589741"

            // Set as resource value for release builds
            resValue("string", "admob_banner_unit_id", admobBannerUnitId)
        }
    }
    // 17, the same as the eight Flutter apps. AGP 8 and later require it, and
    // Java 11 is not installed on the build machine any more, so the toolchain
    // below could not resolve and the build stopped before compiling.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        resValues = true
        buildConfig = true
    }
}

// No jvmToolchain: it demands a JDK of exactly that version be installed, and
// this machine has 21 and 26. The eight Flutter apps set the target instead
// and let the running JDK cross compile, which is what compileOptions above
// and jvmTarget below do.
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

// Test ads look exactly like real ones and earn nothing, so an app built
// without the real unit id has no symptom at all: it simply never appears in
// AdMob reporting, which reads the same as an app nobody uses. This app
// shipped that way. The check runs only for release outputs, so debug builds
// and IDE syncs still work without local.properties
val verifyAdMobConfig = tasks.register("verifyAdMobConfig") {
    doLast {
        val missing = listOf("ADMOB_BANNER_UNIT_ID")
            .filter { localProperties.getProperty(it).isNullOrEmpty() }
        if (missing.isNotEmpty()) {
            throw GradleException(
                "Missing from local.properties: ${missing.joinToString(", ")}. " +
                "A release build must not fall back to AdMob test ids."
            )
        }
    }
}

tasks.matching { it.name == "assembleRelease" || it.name == "bundleRelease" }
    .configureEach { dependsOn(verifyAdMobConfig) }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Firebase - Use BOM to manage versions
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    // Measurement stopped on 2025-11-23 and the app has been shipping blind
    // since. Analytics needs no call sites: first_open, session_start and
    // user_engagement are collected once the dependency is on the classpath
    implementation(libs.firebase.analytics)
    // App Check. Firestore holds one document tree per signed in user and the
    // security rules were its only protection until this was added
    implementation(libs.firebase.appcheck.playintegrity)
    debugImplementation(libs.firebase.appcheck.debug)
    
    // Google Mobile Ads
    implementation(libs.play.services.ads)
    
    // Gson for JSON parsing
    implementation(libs.gson)
    
    // OkHttp for HTTP requests
    implementation(libs.okhttp)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}