package com.hidemyspace.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hidemyspace.R
import com.hidemyspace.appContext
import com.hidemyspace.databinding.ActivityMainBinding
import com.hidemyspace.ui.fragment.AboutFragment
import com.hidemyspace.ui.fragment.AppsFragment
import com.hidemyspace.ui.fragment.SettingsFragment

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupBottomNavigation()
        
        // Hiển thị fragment mặc định
        if (savedInstanceState == null) {
            loadFragment(AppsFragment())
        }
        
        // Kiểm tra trạng thái module
        checkModuleStatus()
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_apps -> {
                    loadFragment(AppsFragment())
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                R.id.navigation_about -> {
                    loadFragment(AboutFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    private fun checkModuleStatus() {
        if (appContext.isHooked) {
            // Module đã được kích hoạt
            binding.moduleStatus.setText(R.string.module_activated)
            binding.moduleStatus.setBackgroundResource(R.color.module_activated)
        } else {
            // Module chưa được kích hoạt
            binding.moduleStatus.setText(R.string.module_not_activated)
            binding.moduleStatus.setBackgroundResource(R.color.module_not_activated)
        }
    }
}