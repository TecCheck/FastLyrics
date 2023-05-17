package io.github.teccheck.fastlyrics

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import io.github.teccheck.fastlyrics.databinding.ActivityMainBinding
import io.github.teccheck.fastlyrics.service.DummyNotificationListenerService

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var settings: Settings

    private var searchMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = Settings(this)
        setTheme(settings.getMaterialStyle())
        setNightMode(settings.getAppTheme())

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_fast_lyrics), binding.drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            onDestinationChanged(destination)
        }

        if (!DummyNotificationListenerService.canAccessNotifications(this)) navController.navigate(R.id.nav_permission)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(TAG, "onCreateOptionsMenu")
        menuInflater.inflate(R.menu.menu_main, menu)
        menu?.findItem(R.id.app_bar_search)?.let { searchMenuItem = it }
        updateSearchEnabled()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.app_bar_search -> {
                if (navController.currentDestination?.id != R.id.nav_search) navController.navigate(
                    R.id.nav_search
                )
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setNightMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun onDestinationChanged(destination: NavDestination) {
        val lockMode = if (destination.id in appBarConfiguration.topLevelDestinations) {
            DrawerLayout.LOCK_MODE_UNLOCKED
        } else {
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        }
        binding.drawerLayout.setDrawerLockMode(lockMode)

        updateSearchEnabled()
    }

    private fun updateSearchEnabled() {
        val dest = navController.currentDestination ?: return
        searchMenuItem?.isVisible = dest.id == R.id.nav_fast_lyrics || dest.id == R.id.nav_search
    }

    fun getSearchMenuItem(): MenuItem? {
        return searchMenuItem
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}