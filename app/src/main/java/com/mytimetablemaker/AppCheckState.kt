package com.mytimetablemaker

import android.content.Context
import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Whether App Check has cleared. Firestore holds one document tree per signed
// in user, so the settings account section stays hidden until this is true.
object AppCheckState {
    private const val TAG = "AppCheck"

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    // A failed attestation can still hand back a non-empty placeholder, which
    // the backend later rejects. Only a real three part JWT counts as ready
    private fun isValidJwt(token: String?): Boolean =
        token != null && token.split(".").let { it.size == 3 && it.none(String::isEmpty) }

    // Staged, not the same call repeated: a stale cache is fixed by forcing a
    // refresh, and a provider that never installed is fixed by installing it
    // again. Repeating one call just repeats one failure
    fun refresh(context: Context) {
        if (_isReady.value) return
        token(forceRefresh = false) { cached ->
            if (isValidJwt(cached)) return@token settle(cached)
            token(forceRefresh = true) { forced ->
                if (isValidJwt(forced)) return@token settle(forced)
                install(context)
                token(forceRefresh = true) { retried -> settle(retried) }
            }
        }
    }

    // Installed at launch and again as the last stage above
    fun install(context: Context) {
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            if (BuildConfig.DEBUG) {
                // The secret comes from AppCheckDebugSecretRegistrar, which the
                // SDK asks through its component graph
                if (BuildConfig.APP_CHECK_DEBUG_TOKEN.isEmpty()) {
                    Log.w(TAG, "No APP_CHECK_DEBUG_TOKEN in local.properties; the SDK will generate one")
                }
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )
    }

    private fun settle(token: String?) {
        _isReady.value = isValidJwt(token)
        Log.d(TAG, "App Check: ${if (_isReady.value) "ready (token length ${token?.length})" else "NOT ready"}")
    }

    private fun token(forceRefresh: Boolean, next: (String?) -> Unit) {
        FirebaseAppCheck.getInstance().getAppCheckToken(forceRefresh)
            .addOnSuccessListener { next(it.token) }
            .addOnFailureListener {
                Log.w(TAG, "App Check getToken failed (forceRefresh=$forceRefresh): ${it.message}")
                next(null)
            }
    }
}
