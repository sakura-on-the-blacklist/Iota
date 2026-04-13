package edu.ph.iota.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import edu.ph.iota.R
import edu.ph.iota.databinding.FragmentPhoneNumberBinding
import edu.ph.iota.viewmodels.MainViewModel


class PhoneNumberFragment() : Fragment() {

    private lateinit var binding: FragmentPhoneNumberBinding

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPhoneNumberBinding.inflate(inflater, container, false)

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
                binding.continueButton.isEnabled = s?.length == 10
            }

        })

        binding.continueButton.setOnClickListener {

            viewModel.setPhoneNumber("+63" + binding.editText.text.toString().trim())

            onNavigateOneTimePassword()

        }

    }

    private fun setObservers() {}

    private fun onNavigateOneTimePassword() {
        findNavController().navigate(R.id.from_phone_number_to_one_time_password)
    }

}