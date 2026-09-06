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

My Transit Makers is a Jetpack Compose-based Android application that helps users create and manage personal timetables for daily commutes and schedules. It provides a comprehensive solution with Firebase integration, user authentication, real-time railway data, and modern Material Design 3 interface.

### 🎯 Key Features

- **Personal Timetable Creation**: Create custom transit guides for daily commutes and schedules
- **Countdown Display**: Real-time countdown to departure time
- **Route Comparison**: Display and compare two routes simultaneously
- **Home/Office Routes**: Register separate routes for commuting and return trips with easy switching
- **Automatic Timetable Generation**: Auto-generate timetables for supported railway lines and bus routes
- **Modern Material Design 3**: Declarative UI with Jetpack Compose
- **Firebase Integration**: Authentication, Firestore database
- **User Authentication**: Sign up, login, password reset, account deletion with re-authentication
- **Railway Data Integration**: Real-time data from ODPT API and GTFS format
- **Timetable Management**: Create, edit, and manage personal timetables
- **Multi-language Support**: Japanese and English localization
- **Google Mobile Ads**: Banner ads integration
- **Data Synchronization**: Cloud-based data storage and sync (requires password for save/get/delete)
- **Customizable Settings**: Various configuration options
- **Offline Support**: Local data caching for offline functionality

## 🚀 Technology Stack

### Frameworks & Libraries
- **Jetpack Compose**: Modern declarative UI framework
- **Material 3**: Material Design components
- **Firebase**: Authentication, Firestore (via Firebase BOM)
- **Google Mobile Ads**: Advertisement display
- **Gradle Kotlin DSL**: Build configuration
- **ODPT API**: Real-time railway data from Open Data Platform for Transportation
- **GTFS**: General Transit Feed Specification for bus data

### Core Features
- **Authentication**: Firebase Auth for user management
- **Database**: Cloud Firestore for data storage
- **Railway Data**: ODPT API and GTFS integration
- **Ads**: Google Mobile Ads SDK
- **Localization**: Multi-language support (English, Japanese)
- **Data Management**: SharedPreferences for local storage
- **Navigation**: Jetpack Navigation Compose
- **Caching**: Intelligent data caching for offline access

## 📋 Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 21. Not 26: its `jlink` cannot transform the Android SDK's
  `core-for-system-modules.jar`, so the build stops before it compiles anything.
  Point Gradle at 21 with `org.gradle.java.home` in your user Gradle config
- Android SDK 24+ (compileSdk and targetSdk are 37)
- Gradle 9.5.0, Android Gradle Plugin 9.3.0, Kotlin 2.4.10
- Firebase project setup
- Google Mobile Ads account
- ODPT API access token (optional, for real-time railway data)
- ODPT API challenge token (optional, for ODPT API authentication)

## 🛠️ Setup

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/MyTransitMakers_JetpackCompose.git
cd MyTransitMakers_JetpackCompose
```

### 2. Configuration Files Setup

#### local.properties Configuration
Copy `local.properties.example` to `local.properties` in the project root and
fill it in. The template carries the same notes as this section, next to the
keys themselves. The file is not in Git:

```properties
sdk.dir=/path/to/your/Android/sdk
ODPT_ACCESS_TOKEN=your_odpt_api_token
ODPT_CHALLENGE_TOKEN=your_odpt_challenge_token
ADMOB_BANNER_UNIT_ID=ca-app-pub-xxxxxxxx/xxxxxxxx
APP_CHECK_DEBUG_TOKEN=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

`APP_CHECK_DEBUG_TOKEN` is the App Check debug secret, registered under Firebase
Console -> App Check -> this Android app -> Manage debug tokens. Android and iOS
are separate App Check apps, so the iOS token in the SwiftUI repo is a different
value. Leave it out and the SDK generates one per install and logs it, which
means registering a new token on every device. Debug builds only; release uses
Play Integrity.

The AdMob **app** id is not here: it is written out in
`app/src/main/AndroidManifest.xml`. It ships inside every copy of the app, so
keeping it in an untracked file protected nothing and left the value existing on
one machine only. Only the **unit** id stays here.

`verifyAdMobConfig` is wired into every release task
(`app/build.gradle.kts:129`) and throws when `ADMOB_BANNER_UNIT_ID` is missing,
so a release cannot fall back to the test unit unnoticed. Debug builds use
Google's test unit and need nothing.

Confirmed on 2026-09-03 that the task joins the graph:

```
$ ./gradlew :app:bundleRelease --dry-run
:app:buildReleasePreBundle SKIPPED
:app:verifyAdMobConfig SKIPPED
BUILD SUCCESSFUL
```

Exercised on 2026-09-06, after the file was accidentally overwritten with a
copy from another project and every key but `sdk.dir` was lost:

```
$ ./gradlew :app:verifyAdMobConfig
> Missing from local.properties: ADMOB_BANNER_UNIT_ID.
  A release build must not fall back to AdMob test ids.
BUILD FAILED
```

The guard holds. **Keep a copy of this file outside the project.** It is
machine-local and untracked, so losing it costs the ODPT tokens and the AdMob
unit id too, and nothing in the repository can restore them.

### 3. Firebase Configuration
1. Create a Firebase project
2. Place `google-services.json` in the `app/` directory
3. This file is tracked here: the build needs it, and it holds only
   identifiers that ship inside the app. Real secrets stay out

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
│   ├── CacheService.kt      # Data caching management
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
│   │   └── SettingsTransferSheetScreen.kt
│   ├── theme/               # Material theme
│   │   ├── Color.kt
│   │   └── Theme.kt
│   └── timetable/           # Timetable views
│       ├── SettingsTimetableSheetScreen.kt
│       └── TimetableContentScreen.kt
├── MainActivity.kt
└── MyTransitMakersApplication.kt
```

## 🚂 Railway Data Integration

### ODPT API Integration
The app integrates with the Open Data Platform for Transportation (ODPT) API:
- **Real-time Data**: Station information, line details, and operator data
- **Automatic Caching**: Intelligent caching for offline access
- **Data Validation**: ETag and Last-Modified header support

### GTFS Data Processing
Bus operators providing GTFS format:
- **Toei Bus**, **Yokohama Municipal Bus**, **Keio Bus**, **Nishitokyo Bus**
- **Kawasaki Bus**, **Kanto Bus**, **Izuhakone Bus**, **Keisei Transit Bus**
- ZIP download, CSV parsing, timetable generation

### Supported Operators
- **Railway**: JR East, Tokyo Metro, Toei Subway, Private railways, Monorails
- **Bus**: Multiple operators via ODPT API and GTFS format

## 🎨 Customization

### Timetable Features
- Week management (Monday to Sunday)
- Time entry add/edit/delete
- Station and line configuration
- Transport options (walking, bicycle, car)
- Color customization for line identification

### User Interface
- Material Design 3 components
- Responsive layouts
- Dark/Light mode support
- Bilingual (Japanese, English)

## 📱 Supported Platforms

- **Android**: API 24+ (Android 7.0+)
- **Target**: API 36 (Android 14+)

## 🔒 Security

- User authentication with email/password
- Password re-authentication for Firestore save/get and account deletion
- Secure data transmission with HTTPS
- API keys in local.properties (excluded from Git)
- Firebase configuration excluded from version control

## 📄 License

This project is licensed under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

If you have any problems or questions, please create an issue on GitHub.

---

<div align="center">
  <strong>My Transit Makers</strong> - Organize your commute, organize your day!
</div>
