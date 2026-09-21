package com.mytimetablemaker

import androidx.annotation.Keep
import com.google.firebase.appcheck.debug.InternalDebugSecretProvider
import com.google.firebase.components.Component
import com.google.firebase.components.ComponentRegistrar

// DebugAppCheckProviderFactory takes no token on Android: the SDK asks its component graph for one instead.
// Registered in AndroidManifest.
@Keep
class AppCheckDebugSecretRegistrar : ComponentRegistrar, InternalDebugSecretProvider {

    override fun getComponents(): List<Component<*>> = listOf(
        Component.builder(InternalDebugSecretProvider::class.java)
            .name("mytimetablemaker-app-check-debug-secret")
            .factory { this }
            .build()
    )

    // Null lets the SDK generate and log one, which is the behaviour without APP_CHECK_DEBUG_TOKEN in local.properties.
    override fun getDebugSecret(): String? =
        BuildConfig.APP_CHECK_DEBUG_TOKEN.ifEmpty { null }
}
