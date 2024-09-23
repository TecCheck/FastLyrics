package io.github.teccheck.fastlyrics.ui.permission

import android.content.Intent
import android.os.Bundle
import io.github.teccheck.fastlyrics.BaseActivity
import io.github.teccheck.fastlyrics.R
import io.github.teccheck.fastlyrics.api.MediaSession
import io.github.teccheck.fastlyrics.databinding.ActivityPermissionBinding

class PermissionActivity : BaseActivity() {

    private lateinit var binding: ActivityPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.toolbarLayout.toolbar, R.string.menu_permission)

        binding.gotoSettingsButton.setOnClickListener { startNotificationsSettings() }
    }

    override fun onResume() {
        super.onResume()
        MediaSession.init(this)
    }

    private fun startNotificationsSettings() {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        startActivity(intent)
    }
}
