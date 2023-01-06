package io.github.teccheck.fastlyrics.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.teccheck.fastlyrics.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}