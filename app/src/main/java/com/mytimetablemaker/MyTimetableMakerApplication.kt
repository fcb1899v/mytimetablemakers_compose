package com.mytimetablemaker

import android.app.Application
import com.google.firebase.FirebaseApp

// App Check has to be installed before anything reaches Firestore, and nothing
// ran that early before: there was no Application subclass and Firebase came up
// through its own content provider.
class MyTimetableMakerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppCheckState.install(this)
        AppCheckState.refresh(this)
    }
}
