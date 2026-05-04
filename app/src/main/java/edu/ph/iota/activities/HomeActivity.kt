package edu.ph.iota.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import edu.ph.iota.R
import edu.ph.iota.databinding.ActivityHomeBinding
import edu.ph.iota.fragments.HomeFragment
import edu.ph.iota.viewmodels.HomeViewModel

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment()
        setupBottomNav()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    private fun setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeButton -> {
                    true
                }
                R.id.progresButton -> {
                    // TODO: startActivity(Intent(this, ProgressActivity::class.java)) JULES PROGRESS
                    true
                }
                R.id.focusButton -> {
                    // TODO: startActivity(Intent(this, FocusActivity::class.java)) REV POMODORO
                    true
                }
                else -> false
            }
        }

        // Mark home as selected on launch
        binding.bottomNavigation.selectedItemId = R.id.homeButton
    }




}
