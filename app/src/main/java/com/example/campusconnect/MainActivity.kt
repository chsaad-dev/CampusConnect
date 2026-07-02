package com.example.campusconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.campusconnect.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)

        // Setup Navigation Controller
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Top-level destinations (jin par back button ki jagah drawer icon dikhega)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_notes, R.id.nav_lost_found, 
                R.id.nav_rides, R.id.nav_profile, R.id.nav_blood_donation,
                R.id.nav_complaints, R.id.nav_events, R.id.nav_jobs, R.id.nav_chat
            ),
            binding.drawerLayout
        )

        // Sync Toolbar with NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Connect Bottom Navigation and Navigation Drawer
        binding.bottomNavigation.setupWithNavController(navController)
        binding.navView.setupWithNavController(navController)
        
        // Drawer item clicks handling
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
            if (handled) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            handled
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
