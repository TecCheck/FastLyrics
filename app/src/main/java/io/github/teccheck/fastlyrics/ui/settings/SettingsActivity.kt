package io.github.teccheck.fastlyrics.ui.settings

import android.os.Bundle
import io.github.teccheck.fastlyrics.BaseActivity
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.databinding.ActivitySettingsBinding

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.toolbarLayout.toolbar, R.string.menu_settings)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_fragment, SettingsFragment())
                .commit()
        }
    }
}