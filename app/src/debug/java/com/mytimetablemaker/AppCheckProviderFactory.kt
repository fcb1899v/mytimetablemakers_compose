package com.mytimetablemaker

import android.util.Log
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

// The debug provider is a debugImplementation dependency, so the reference lives in this
// source set and release/ has its own copy: a BuildConfig.DEBUG branch fails to compile.
internal fun appCheckProviderFactory(): AppCheckProviderFactory {
    // The secret itself comes from AppCheckDebugSecretRegistrar, which the SDK
    // asks for through its component graph
    if (BuildConfig.APP_CHECK_DEBUG_TOKEN.isEmpty()) {
        Log.w("AppCheck", "No debug token configured; the SDK will generate one")
    }
    return DebugAppCheckProviderFactory.getInstance()
}
