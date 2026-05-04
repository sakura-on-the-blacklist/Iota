package edu.ph.iota.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment()
        setViews()
        setObservers()
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
                is HabitSettingViewModel.UiState.Idle    -> {  }
                is HabitSettingViewModel.UiState.Loading ->{
//
                    binding.continueButton.isEnabled = false
                }
                is HabitSettingViewModel.UiState.Error->{
                    binding.continueButton.isEnabled = true
                    showError(state.message)
                    viewModel.resetUiState()
                }
                is HabitSettingViewModel.UiState.Success->{
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
