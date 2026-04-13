package edu.ph.iota.fragments


import android.app.DatePickerDialog
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
import edu.ph.iota.databinding.FragmentBirthdayBinding
import edu.ph.iota.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.getValue

class BirthdayFragment() : Fragment() {

    private lateinit var binding: FragmentBirthdayBinding

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentBirthdayBinding.inflate(inflater, container, false)

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
            R.string.text_birthday,
            viewModel.getName()?.replaceFirstChar { it.uppercase() }
        )

        binding.editText.setOnClickListener {

            val calendar = Calendar.getInstance()

            if (binding.editText.text.isNotEmpty()) {
                calendar.time = SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()).parse(binding.editText.text.toString().trim()
                )!!
            }

            DatePickerDialog(
                requireContext(),
                { _, y, m, d -> binding.editText.setText("%02d/%02d/%04d".format(d, m + 1, y)) },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {

                datePicker.maxDate = System.currentTimeMillis()

                show()

            }

        }

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

            viewModel.setBirthday(binding.editText.text.toString().trim())

            onNavigateUsername()

        }

    }

    private fun setObservers() {}

    private fun onNavigateUsername() {
        findNavController().navigate(R.id.from_birthday_to_username)
    }

}