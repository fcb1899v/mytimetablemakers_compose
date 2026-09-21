package com.mytimetablemaker

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

// See the debug copy of this file: the debug provider is not on the release
// classpath, so the two builds pick their factory by source set, not by branch.
internal fun appCheckProviderFactory(): AppCheckProviderFactory =
    PlayIntegrityAppCheckProviderFactory.getInstance()
