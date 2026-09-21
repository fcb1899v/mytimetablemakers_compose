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
        versionCode = 58
        versionName = "2.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // The AdMob app id lives in AndroidManifest.xml because it ships in every copy of the app anyway.
        // Unit ids stay in local.properties.

        // Get ODPT Access Token from local.properties and set in BuildConfig
        val odptAccessToken = localProperties.getProperty("ODPT_ACCESS_TOKEN") ?: ""
        buildConfigField("String", "ODPT_ACCESS_TOKEN", "\"$odptAccessToken\"")
        
        // Get ODPT Challenge Token from local.properties and set in BuildConfig
        val odptChallengeToken = localProperties.getProperty("ODPT_CHALLENGE_TOKEN") ?: ""
        buildConfigField("String", "ODPT_CHALLENGE_TOKEN", "\"$odptChallengeToken\"")
    }

    buildTypes {
        debug {
            // Adaptive banners have their own demo unit.
            // The fixed size unit (6300978111) only serves 320x50, so every adaptive size would measure as 320x50.
            resValue("string", "admob_banner_unit_id", "ca-app-pub-3940256099942544/9214589741")
            // A fixed App Check debug secret lets one registered token cover every device.
            // Otherwise the SDK generates a new one per install.
            buildConfigField(
                "String",
                "APP_CHECK_DEBUG_TOKEN",
                "\"${localProperties.getProperty("APP_CHECK_DEBUG_TOKEN") ?: ""}\""
            )
        }
        
        release {
            // Not read in release because PlayIntegrity is used there.
            // BuildConfig still needs the field defined in every build type.
            buildConfigField("String", "APP_CHECK_DEBUG_TOKEN", "\"\"")
            isMinifyEnabled = true
            isShrinkResources = true
            // ndk.debugSymbolLevel is left unset on purpose because the only .so files come from dependencies.
            // They carry no symbols, so SYMBOL_TABLE would emit an empty directory.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Falls back to the test unit so configuring works without local.properties.
            // verifyAdMobConfig below stops a release built this way.
            val admobBannerUnitId = localProperties.getProperty("ADMOB_BANNER_UNIT_ID")
                ?: "ca-app-pub-3940256099942544/9214589741"

            // Set as resource value for release builds
            resValue("string", "admob_banner_unit_id", admobBannerUnitId)
        }
    }
    // Use 17, the same as the eight Flutter apps.
    // AGP 8 and later require it, and Java 11 is no longer installed on the build machine.
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

// No jvmToolchain, because it demands a JDK of exactly that version.
// compileOptions above and jvmTarget below set the target and let the running JDK cross compile instead.
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

// Test ads look like real ones and earn nothing, so a build without the real unit id has no symptom.
// Runs only for release outputs so debug builds work without it.
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
    // Analytics needs no call sites.
    // The dependency alone collects first_open, session_start and user_engagement.
    implementation(libs.firebase.analytics)
    // App Check, because Firestore holds one document tree per signed in user.
    // The security rules were its only protection until this was added.
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