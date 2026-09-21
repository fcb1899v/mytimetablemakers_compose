package com.mytimetablemaker

import android.app.Application
import com.google.firebase.FirebaseApp

// App Check has to be installed before anything reaches Firestore; without this
// subclass Firebase comes up through its own content provider before any app code.
class MyTimetableMakerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppCheckState.install(this)
        AppCheckState.refresh(this)
    }
}
