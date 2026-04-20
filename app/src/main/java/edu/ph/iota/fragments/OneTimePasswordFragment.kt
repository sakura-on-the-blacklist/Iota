package edu.ph.iota.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.QuerySnapshot
import edu.ph.iota.R
import edu.ph.iota.activities.HomeActivity
import edu.ph.iota.databinding.FragmentOneTimePasswordBinding
import edu.ph.iota.viewmodels.MainViewModel

import java.lang.Exception
import kotlin.getValue

class OneTimePasswordFragment() : Fragment() {

    private lateinit var binding: FragmentOneTimePasswordBinding

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentOneTimePasswordBinding.inflate(inflater, container, false)

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

        binding.textView.text = getString(
            R.string.text_one_time_password,
            viewModel.getPhoneNumber()
        )

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
                binding.continueButton.isEnabled = s?.length == 6
            }

        })

        binding.continueButton.setOnClickListener {

            binding.continueButton.isEnabled = false

            onGetPhoneNumber()

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

    private fun onGetPhoneNumber() {
        viewModel.firestore
            .collection("users")
            .whereEqualTo("phoneNumber", viewModel.getPhoneNumber())
            .get()
            .addOnSuccessListener { onGetPhoneNumberSuccess(it) }
            .addOnFailureListener { onFailure(it) }
    }

    private fun onGetPhoneNumberSuccess(
        snapshots: QuerySnapshot
    ) {
        if (snapshots.isEmpty) {
            onNavigateName()
        } else {
            onNavigateHome()
        }
    }

    private fun onNavigateName() {
        findNavController().navigate(R.id.from_one_time_password_to_name)
    }

    private fun onNavigateHome() {

        viewModel.preferenceManager.setPhoneNumber(viewModel.getPhoneNumber()!!)

        startActivity(
            Intent(
                requireContext(),
                HomeActivity::class.java
            )
        )

        requireActivity().finish()

    }

}