# My Transit Makers

<div align="center">
  <img src="app/src/main/res/drawable/icon.png" alt="My Transit Makers Icon" width="120" height="120">
  <br>
  <strong>Create and manage your personal timetable with ease</strong>
  <br>
  <strong>Smart timetable management for Android with Firebase integration</strong>
</div>

![Android](https://img.shields.io/badge/Platform-Android-green)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange)
![Target SDK](https://img.shields.io/badge/Target%20SDK-37-orange)

## 📱 Application Overview

My Transit Makers is a Jetpack Compose Android application for building personal timetables for daily commutes.
It combines Firebase authentication and Firestore storage with railway and bus data from the ODPT API and GTFS feeds, on a Material Design 3 interface.

### 🎯 Key Features

- **Personal Timetable Creation**: Create custom transit guides for daily commutes and schedules
- **Countdown Display**: Real-time countdown to departure time
- **Route Comparison**: Display and compare two routes simultaneously
- **Home/Office Routes**: Register separate routes for commuting and return trips with easy switching
- **Automatic Timetable Generation**: Auto-generate timetables for supported railway lines and bus routes
- **Material Design 3**: Declarative UI with Jetpack Compose
- **Firebase Integration**: Authentication, Firestore, Analytics, App Check
- **User Authentication**: Sign up, login, password reset, account deletion with re-authentication
- **Railway Data Integration**: ODPT API and GTFS feeds
- **Multi-language Support**: Japanese and English localization
- **Google Mobile Ads**: Banner ads
- **Data Synchronization**: Firestore save and get, each guarded by the account password (`FirestoreViewModel.setFirestore` / `getFirestore`)
- **Account Deletion**: Re-authenticated account deletion through `LoginViewModel.delete`
- **Offline Support**: Caching of ODPT railway and bus data; GTFS feeds are fetched on demand and are not part of the cache bootstrap

## 🚀 Technology Stack

### Frameworks & Libraries

- **Jetpack Compose**: UI, through the Compose BOM `2026.09.00`
- **Material 3**: `androidx.compose.material3`, with the extended icon set
- **Navigation Compose**: `androidx.navigation:navigation-compose`
- **Firebase**: Auth, Firestore, Analytics and App Check, pinned by the Firebase BOM `34.18.0`
- **Google Mobile Ads**: `play-services-ads` `25.5.0`
- **OkHttp `5.5.0` and Gson `2.14.0`**: HTTP and JSON for the ODPT and GTFS clients
- **Gradle Kotlin DSL**: Build configuration, with the version catalog in `gradle/libs.versions.toml`

### Data Sources

- **ODPT API**: Station, line and operator data from the Open Data Platform for Transportation
- **GTFS**: General Transit Feed Specification archives for bus timetables

## 📋 Prerequisites

- An Android Studio release that supports Android Gradle Plugin 9.4
- A JDK that Android Gradle Plugin 9.4 supports, pointed at Gradle with `org.gradle.java.home` in your user Gradle config if it is not the one Android Studio runs
- The build targets Java 17 and Kotlin JVM 17 (`app/build.gradle.kts`), and deliberately sets no `jvmToolchain`, so the running JDK cross compiles instead of having to be that exact version
- Android SDK platform 37, because `compileSdk` and `targetSdk` are both 37
- Gradle 9.6.0 through the wrapper, Android Gradle Plugin 9.4.1, Kotlin 2.4.20
- A Firebase project with Authentication, Firestore and App Check enabled
- An AdMob account, for the release banner unit id
- An ODPT API access token and challenge token, for railway and bus data

## 🛠️ Setup

### 1. Clone the Repository

```bash
git clone https://github.com/fcb1899v/mytimetablemakers_compose.git
cd mytimetablemakers_compose
```

### 2. Configuration Files Setup

Copy `local.properties.example` to `local.properties` in the project root and fill it in.
The template carries the same notes as this section, next to the keys themselves.
The file is not in Git.

```properties
sdk.dir=/path/to/your/Android/sdk
ODPT_ACCESS_TOKEN=your_odpt_api_token
ODPT_CHALLENGE_TOKEN=your_odpt_challenge_token
ADMOB_BANNER_UNIT_ID=ca-app-pub-xxxxxxxx/xxxxxxxx
APP_CHECK_DEBUG_TOKEN=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

`APP_CHECK_DEBUG_TOKEN` is the App Check debug secret, registered under Firebase Console -> App Check -> this Android app -> Manage debug tokens.
Android and iOS are separate App Check apps, so the iOS token in the SwiftUI repository is a different value.
Leave it out and the SDK generates one per install and logs it, which means registering a new token on every device.
It is read in debug builds only, because release builds use Play Integrity.

The AdMob **app** id is not kept here: it is written out in `app/src/main/AndroidManifest.xml`.
It ships inside every copy of the app, so keeping it in an untracked file protects nothing.
Only the **unit** id stays in the properties file.

Keep a copy of the filled-in file outside the project.
It is machine-local and untracked, so nothing in the repository can restore the ODPT tokens or the AdMob unit id.

### 3. Firebase Configuration

1. Create a Firebase project and enable Email/Password authentication.
2. Download `google-services.json` from the Firebase console (Project settings > Your apps) and place it in the `app/` directory.
   It is not in the repository, so download it before the first build.
   The `com.google.gms.google-services` plugin fails the build without it, so a fresh clone has to download it first.
3. Deploy the Firestore rules in `firestore.rules`.
   ```bash
   firebase deploy --only firestore:rules --project <PROJECT_ID>
   ```
4. Register the App Check providers: the debug token above for emulators, Play Integrity for release.

### 4. Build and Run

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and run on a device or emulator.

## 🎮 Application Structure

```
app/src/main/java/com/mytimetablemaker/
├── extensions/              # Kotlin extensions
│   ├── AccountExtensions.kt
│   ├── ColorExtensions.kt
│   ├── LineExtensions.kt
│   ├── SizeExtensions.kt
│   └── TimeExtensions.kt
├── models/                  # Data models
│   ├── Enums.kt
│   └── TransportationModels.kt
├── services/                # Service layer
│   ├── CacheService.kt      # Caching of fetched data
│   ├── GTFSDataService.kt   # GTFS data processing
│   └── ODPTDataService.kt   # ODPT API integration
├── ui/
│   ├── common/              # Reusable components
│   │   ├── AdMobBannerView.kt
│   │   └── CommonComponents.kt
│   ├── login/               # Authentication
│   │   ├── LoginContentScreen.kt
│   │   ├── LoginViewModel.kt
│   │   └── SignUpContentScreen.kt
│   ├── main/                # Main content
│   │   ├── MainContentScreen.kt
│   │   ├── MainViewModel.kt
│   │   └── SplashContentScreen.kt
│   ├── settings/            # Settings and Firestore
│   │   ├── FirestoreViewModel.kt
│   │   ├── SettingsContentScreen.kt
│   │   ├── SettingsLineSheetScreen.kt
│   │   ├── SettingsLineViewModel.kt
│   │   ├── SettingsTimetableViewModel.kt
│   │   ├── SettingsTransferSheetScreen.kt
│   │   └── SettingsTransferSheetViewModel.kt
│   ├── theme/               # Material theme
│   │   ├── Color.kt
│   │   └── Theme.kt
│   └── timetable/           # Timetable views
│       ├── SettingsTimetableSheetScreen.kt
│       └── TimetableContentScreen.kt
├── AppCheckState.kt         # App Check initialization state
├── MainActivity.kt
└── MyTimetableMakerApplication.kt

app/src/debug/AndroidManifest.xml   # Registers the App Check debug secret provider
```

## 🚂 Railway Data Integration

### ODPT API Integration

The app reads the Open Data Platform for Transportation (ODPT) API for station information, line details and operator data.
Responses are cached, and revalidated with ETag and Last-Modified headers.

### GTFS Data Processing

Only the seven operators whose `apiType()` is `GTFS` in `models/Enums.kt` are read this way: Keio Bus, Nishitokyo Bus, Kawasaki City Bus, Kawasaki Tsurumi Rinko Bus, Kanto Bus, Izuhakone Bus and Keisei Transit Bus.
For those, the app downloads the ZIP, parses the CSV, and builds timetables from it.
Every other bus operator uses the ODPT bus endpoints instead: Toei Bus is `PUBLIC_API`, and Yokohama Municipal Bus, Tokyu Bus, Seibu Bus and Sotetsu Bus are `STANDARD`, while Kanachu, Kokusai Kogyo and Tobu Bus are `CHALLENGE`.
GTFS data is fetched when the user selects such an operator; `services/CacheService.kt` excludes GTFS operators from its startup cache bootstrap.

### Supported Operators

- **Railway**: JR East, Tokyo Metro, Toei Subway, private railways and monorails published on ODPT
- **Bus**: Toei Bus, Yokohama Municipal Bus, Tokyu Bus, Seibu Bus, Sotetsu Bus, Kanachu, Kokusai Kogyo and Tobu Bus through ODPT, plus the seven GTFS operators listed above

## 📱 Supported Platforms

- **Android**: API 24+ (`minSdk`)
- **Target**: API 37 (`targetSdk`, the same as `compileSdk`)

## 🔧 Development

### Release Configuration Check

The AdMob unit id is verified before every release output.

```bash
./gradlew :app:verifyAdMobConfig   # expected: BUILD SUCCESSFUL once the properties file is filled in
```

The task is wired into `assembleRelease` and `bundleRelease` in `app/build.gradle.kts`, and fails when `ADMOB_BANNER_UNIT_ID` is missing.
Test ads look like real ones and earn nothing, so a release built without the real id would otherwise have no symptom.
Debug builds use Google's test unit and need nothing.

### Tests and Lint

The repository carries the Android Studio test templates, so these run but check nothing of the app yet.

```bash
./gradlew test                   # unit tests
./gradlew connectedAndroidTest   # instrumented tests, needs a running device
./gradlew lint                   # Android Lint, report under app/build/reports/
```

### Build

```bash
./gradlew assembleDebug      # debug APK
./gradlew assembleRelease    # release APK
./gradlew bundleRelease      # Play App Bundle
```

Release builds run R8 with resource shrinking, configured by `app/proguard-rules.pro`.

## 📄 License

This project is not open source.
The source is published so that it can be read, and all rights are reserved.
See [LICENSE](LICENSE) for what that permits.
Third-party components keep their own licenses, listed below.

## 🤝 Contributing

Issue reports are welcome.
Pull requests are not accepted, because the code is not licensed for redistribution.

## 📞 Support

If you have any problems or questions, please create an issue on GitHub.

## Licenses & Credits

This app uses the following third-party components:

- **Kotlin** (Apache License 2.0)
- **Gradle Wrapper** (Apache License 2.0): the `gradle/wrapper/gradle-wrapper.jar` committed here
- **Jetpack Compose** (Apache License 2.0): UI, UI Graphics, Foundation, Material 3, Material Icons Extended, UI Tooling
- **AndroidX** (Apache License 2.0): Core KTX, Lifecycle Runtime KTX, Lifecycle ViewModel Compose, Activity Compose, Navigation Compose
- **Firebase Android SDK, open-source parts** (Apache License 2.0): Firebase BOM, Firestore, App Check with the Play Integrity and debug providers
- **Firebase Android SDK, closed-source parts** (Android Software Development Kit License): `firebase-auth`, `firebase-analytics`
- **Google Mobile Ads** (Android Software Development Kit License): `play-services-ads`
- **OkHttp** (Apache License 2.0)
- **Gson** (Apache License 2.0)
- **JUnit** (Eclipse Public License 1.0), used by tests only
- **AndroidX Test** (Apache License 2.0), used by tests only: Ext JUnit, Espresso Core, Compose UI Test

The Android Software Development Kit License is at [developer.android.com/studio/terms](https://developer.android.com/studio/terms.html).

### Data Sources

- **ODPT (Open Data Platform for Transportation)**: railway and bus data, at [odpt.org](https://www.odpt.org/)
- **GTFS**: bus timetable archives published by the seven operators listed above

ODPT data is not covered by the software licenses above.
Its use is governed by the ODPT terms, and each GTFS archive by the terms of the operator that publishes it.

