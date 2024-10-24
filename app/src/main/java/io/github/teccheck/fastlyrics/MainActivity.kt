package io.github.teccheck.fastlyrics

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import io.github.teccheck.fastlyrics.databinding.ActivityMainBinding
import io.github.teccheck.fastlyrics.service.DummyNotificationListenerService
import io.github.teccheck.fastlyrics.ui.about.AboutActivity
import io.github.teccheck.fastlyrics.ui.permission.PermissionActivity
import io.github.teccheck.fastlyrics.ui.saved.SavedActivity
import io.github.teccheck.fastlyrics.ui.settings.SettingsActivity

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    private var searchMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_fast_lyrics), binding.drawerLayout)
        binding.navView.setNavigationItemSelectedListener(this)

        setSupportActionBar(binding.appBarMain.toolbarLayout.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        if (!DummyNotificationListenerService.canAccessNotifications(this)) {
            startActivity(Intent(this, PermissionActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu?.findItem(R.id.app_bar_search)?.let { searchMenuItem = it }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.app_bar_search -> {
                if (navController.currentDestination?.id != R.id.nav_search) {
                    navController.navigate(R.id.nav_search)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_saved -> startActivity(Intent(this, SavedActivity::class.java))
            R.id.nav_permission -> startActivity(Intent(this, PermissionActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_about -> startActivity(Intent(this, AboutActivity::class.java))
        }

        return false
    }

    fun getSearchMenuItem(): MenuItem? {
        return searchMenuItem
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}