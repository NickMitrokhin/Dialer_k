package com.nickmitrokhin.dialer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.ui.*
import com.nickmitrokhin.dialer.databinding.ActivityMainBinding

val Context.dataStore by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val navController get() = findNavController(R.id.nav_host_fragment_content_main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
        setupAppBarWithNavController()
        initAppVersion()
    }

    private fun initAppVersion() {
        var version = ""
        try {
            val packageInfo = packageManager?.getPackageInfo(packageName, 0)
            version = "${packageInfo?.versionName}.${packageInfo?.versionCode}"
        } catch(err: Exception) {
        }

        val titleView: TextView = binding.navView.getHeaderView(0).findViewById(R.id.titleView)
        titleView.text = "${titleView.text} v$version"
    }

    private fun setupAppBarWithNavController() {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_contacts, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.nav_exit) {
            finish()
        } else {
            navController.navigate(item.itemId, null, null)
            binding.drawerLayout.closeDrawers()
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }
}