package com.mytimetablemaker

import android.content.Context
import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
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

    // Staged, not the same call repeated: a stale cache needs a forced refresh,
    // a provider that never installed needs installing again
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

    // Installed at launch and again as the last stage above. appCheckProviderFactory()
    // has one copy per source set: firebase-appcheck-debug is debugImplementation only.
    fun install(context: Context) {
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(appCheckProviderFactory())
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
