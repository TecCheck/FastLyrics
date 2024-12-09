package io.github.teccheck.fastlyrics

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var settings: Settings
    private var homeAsUp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = Settings(this)
        setTheme(settings.getMaterialStyle())
        setNightMode(settings.getAppTheme())

        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (homeAsUp && item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    protected fun setupToolbar(toolbar: Toolbar, @StringRes title: Int? = null) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title?.let { supportActionBar?.setTitle(it) }
        homeAsUp = true
    }

    private fun setNightMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}