package edu.ph.iota.fragments

import edu.ph.iota.viewmodels.MainViewModel

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.QuerySnapshot
import edu.ph.iota.activities.HabitSettingActivity
import edu.ph.iota.databinding.FragmentUsernameBinding
import java.lang.Exception
import kotlin.getValue

class UsernameFragment() : Fragment() {

    private lateinit var binding: FragmentUsernameBinding

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentUsernameBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        setViews()
        setObservers()

    }

    private fun setViews() {

        binding.editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {}

            override fun afterTextChanged(
                s: Editable?
            ) {
                binding.continueButton.isEnabled = s?.isNotBlank() == true
            }

        })

        binding.continueButton.setOnClickListener {

            binding.continueButton.isEnabled = false

            viewModel.setUsername(binding.editText.text.toString().lowercase().trim())

            onGetUsername()

        }

    }

    private fun setObservers() {}

    private fun onFailure(
        exception: Exception
    ) {

        binding.continueButton.isEnabled = true

        Snackbar.make(
            binding.root,
            "Error: ${exception.message}",
            Snackbar.LENGTH_SHORT
        ).show()

    }

    private fun onGetUsername() {
        viewModel.firestore
            .collection("users")
            .whereEqualTo("username", viewModel.getUsername())
            .get()
            .addOnSuccessListener { onGetUsernameSuccess(it) }
            .addOnFailureListener { onFailure(it) }
    }

    private fun onGetUsernameSuccess(
        snapshots: QuerySnapshot
    ) {
        if (snapshots.isEmpty) {
            onAddUser()
        } else {
            onFailure(Exception("Username already taken."))
        }
    }

    private fun onAddUser() {
        viewModel.firestore
            .collection("users")
            .document( viewModel.getPhoneNumber()!!)
            .set(viewModel.getUser()!!)
            .addOnSuccessListener { onAddUserSuccess(it) }
            .addOnFailureListener { onFailure(it) }
    }

    private fun onAddUserSuccess(
        void: Void?
    ) {
        onNavigateHabitSetting()
    }

    private fun onNavigateHabitSetting() {

        viewModel.preferenceManager.setPhoneNumber(viewModel.getPhoneNumber()!!)

        startActivity(
            Intent(
                requireContext(),
                HabitSettingActivity::class.java
            )
        )

        requireActivity().finish()

    }

}
