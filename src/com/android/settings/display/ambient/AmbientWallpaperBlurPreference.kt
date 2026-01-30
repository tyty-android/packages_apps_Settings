/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.display.ambient

import android.content.Context
import android.provider.Settings
import com.android.settings.R
import com.android.settingslib.datastore.HandlerExecutor
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.datastore.KeyedObserver
import com.android.settingslib.datastore.SettingsSecureStore
import com.android.settingslib.metadata.PreferenceLifecycleContext
import com.android.settingslib.metadata.PreferenceLifecycleProvider
import com.android.settingslib.metadata.SwitchPreference

class AmbientWallpaperBlurPreference(context: Context) :
    SwitchPreference(
        KEY,
        R.string.doze_wallpaper_blur_title,
        R.string.doze_wallpaper_blur_summary,
    ),
    PreferenceLifecycleProvider {

    private val secureStore = SettingsSecureStore.get(context).apply {
        setDefaultValue(KEY, true)
    }
    private val dozeAlwaysOnDataStore = AmbientDisplayStorage(context)
    private var settingsKeyedObserver: KeyedObserver<String>? = null

    override val key: String
        get() = KEY

    override fun storage(context: Context): KeyValueStore = secureStore

    override fun isEnabled(context: Context): Boolean {
        // Only enable the blur toggle if the wallpaper toggle is ON.
        return dozeAlwaysOnDataStore.getBoolean(Settings.Secure.DOZE_ALWAYS_ON)!! && ((secureStore.getInt(AmbientWallpaperPreference.KEY) ?: 0) == 1)
    }

    override fun onStart(context: PreferenceLifecycleContext) {
        if (settingsKeyedObserver == null) {
            settingsKeyedObserver = KeyedObserver { _, _ ->
                // Notify change to update the isEnabled state
                context.notifyPreferenceChange(key)
            }
        }

        settingsKeyedObserver?.let { observer ->
            secureStore.addObserver(
                AmbientWallpaperPreference.KEY,
                observer,
                HandlerExecutor.main
            )
            secureStore.addObserver(
                Settings.Secure.DOZE_ALWAYS_ON,
                observer,
                HandlerExecutor.main
            )
        }
    }

    override fun onStop(context: PreferenceLifecycleContext) {
        settingsKeyedObserver?.let { observer ->
            secureStore.removeObserver(AmbientWallpaperPreference.KEY, observer)
            secureStore.removeObserver(Settings.Secure.DOZE_ALWAYS_ON, observer)
        }
        settingsKeyedObserver = null
    }

    companion object {
        const val KEY = Settings.Secure.DOZE_ALWAYS_ON_WALLPAPER_BLUR
    }
}
