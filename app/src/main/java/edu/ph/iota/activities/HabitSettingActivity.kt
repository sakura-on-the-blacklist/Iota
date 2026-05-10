package edu.ph.iota.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import edu.ph.iota.R
import edu.ph.iota.databinding.ActivityHabitSettingBinding
import edu.ph.iota.fragments.HabitFragment
import edu.ph.iota.viewmodels.HabitSettingViewModel

class HabitSettingActivity : AppCompatActivity() {
    //this activity handles
    //loading habitfragment to framelayout
    //setting up continue buttons
    //observing viewmodel UI state for loading, error, success
    //handling the close button
    private lateinit var binding: ActivityHabitSettingBinding
    private val viewModel: HabitSettingViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                showError("Note: Notifications are disabled. Reminders won't show.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNotificationPermission()
        loadFragment()
        setViews()
        setObservers()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun loadFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HabitFragment())
            .commit()

    }
    private fun setViews(){
        binding.close.setOnClickListener {
            finish()
        }

        binding.continueButton.setOnClickListener{
            binding.continueButton.isEnabled = false
            viewModel.saveHabit(
                onSuccess = { onSaveSuccess()},
                onFailure = { message ->
                    binding.continueButton.isEnabled = true
                    showError(message)
                }
            )
        }
    }
    private fun setObservers() {
        viewModel.uiState.observe(this){state ->
            when (state){
                is HabitSettingViewModel.UiState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.continueButton.isEnabled = viewModel.isFormReady.value == true
                    binding.continueButton.text = "Continue"
                }
                is HabitSettingViewModel.UiState.Loading ->{
                    binding.progressBar.visibility = View.VISIBLE
                    binding.continueButton.text = ""
                    binding.continueButton.isEnabled = false

                }
                is HabitSettingViewModel.UiState.Error->{
                    binding.progressBar.visibility = View.GONE
                    binding.continueButton.isEnabled = true
                    showError(state.message)
                    viewModel.resetUiState()
                }
                is HabitSettingViewModel.UiState.Success->{
                    binding.progressBar.visibility = View.GONE
                    viewModel.resetUiState()
                }

            }
        }
        viewModel.isFormReady.observe(this) { ready ->
            binding.continueButton.isEnabled = ready
        }


    }

    private fun onSaveSuccess(){
        setResult(RESULT_OK)

        // Navigate to HomeActivity
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showError(message: String){
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
