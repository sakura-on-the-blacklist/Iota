package edu.ph.iota.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import edu.ph.iota.R
import edu.ph.iota.databinding.ActivityHomeBinding
import edu.ph.iota.fragments.FocusFragment
import edu.ph.iota.fragments.HabitProgressFragment
import edu.ph.iota.fragments.HomeFragment
import edu.ph.iota.viewmodels.HomeViewModel

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private val progressFragment = HabitProgressFragment()
    private val homeFragment = HomeFragment()
    private val focusFragment = FocusFragment()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        showFragment(homeFragment)


        setupBottomNav()
    }
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeButton -> {
                    showFragment(homeFragment)
                    true
                }
                R.id.progresButton -> {
                    showFragment(progressFragment)
                    true
                }
                R.id.focusButton -> {
                    showFragment(focusFragment)
                    true
                }
                else -> false
            }
        }

        // Mark home as selected on launch
        binding.bottomNavigation.selectedItemId = R.id.homeButton
    }




}
