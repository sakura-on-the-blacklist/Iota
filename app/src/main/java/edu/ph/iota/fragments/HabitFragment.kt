package edu.ph.iota.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import edu.ph.iota.R
import edu.ph.iota.activities.HabitSettingActivity
import edu.ph.iota.databinding.FragmentHabitBinding
import edu.ph.iota.viewmodels.HabitSettingViewModel

//parent: HabitSettingActivity

class HabitFragment : Fragment(){
    private lateinit var binding: FragmentHabitBinding
    private val viewModel: HabitSettingViewModel by activityViewModels ()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupIdentitySection()
        setupFrequencySection()
        setupGoalSection()
        setupTimeSection()
        setupReminderSection()
    }


    //identity section
    private fun setupIdentitySection() {

        val savedIdentity = viewModel.preferenceManager.getIdentity()

        if (savedIdentity != null) {
          //fix identify after the user has decided once
            binding.sectionIdentity.editTextIdentity.setText(savedIdentity)


            binding.sectionIdentity.editTextIdentity.isEnabled = false
            binding.sectionIdentity.editTextIdentity.alpha = 0.5f

            viewModel.setIdentity(savedIdentity)

        } else {

            binding.sectionIdentity.editTextIdentity.addTextChangedListener(
                onAfterChanged { viewModel.setIdentity(it) }
            )
        }

        // Habit name and location are always editable
        binding.sectionIdentity.editTextHabit.addTextChangedListener(
            onAfterChanged { viewModel.setName(it) }
        )

        binding.sectionIdentity.editTextLocation.addTextChangedListener(
            onAfterChanged { viewModel.setLocation(it) }
        )
    }

    //frequency
    private fun setupFrequencySection() {

        val chipDayMap = mapOf(
            binding.sectionFrequency.chipSun to "SUN",
            binding.sectionFrequency.chipMon to "MON",
            binding.sectionFrequency.chipTue to "TUE",
            binding.sectionFrequency.chipWed to "WED",
            binding.sectionFrequency.chipThu to "THU",
            binding.sectionFrequency.chipFri to "FRI",
            binding.sectionFrequency.chipSat to "SAT"
        )

        chipDayMap.forEach { (chip, day) ->
            chip.setOnCheckedChangeListener { _, _ ->
                val selectedDays = chipDayMap
                    .filter { (c, _) -> c.isChecked }
                    .values
                    .toList()

                viewModel.setFrequencyDays(selectedDays)

                // Derive frequency type from selection
                val frequency = if (selectedDays.size == 7) "daily" else "custom"
                viewModel.setFrequency(frequency)

                updateContinueButton()
            }
        }
    }

    //goal
    private fun setupGoalSection() {

        val units = listOf("times", "minutes", "hours", "km", "steps",
            "glasses", "pages", "kg", "calories", "reps")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            units
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.sectionGoal.spinner.adapter = adapter
        viewModel.setGoalUnit(units[0]) // default to "times"

        binding.sectionGoal.spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    viewModel.setGoalUnit(units[position])
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        binding.sectionGoal.editTextNumberDecimal.addTextChangedListener(
            onAfterChanged { text ->
                val value = text.toDoubleOrNull() ?: 0.0
                viewModel.setGoalValue(value)
                updateContinueButton()
            }
        )
    }

    //time
    private fun setupTimeSection() {

        val openPicker = {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(8)
                .setMinute(0)
                .setTitleText("Set habit time")
                .build()

            picker.addOnPositiveButtonClickListener {
                val formatted = String.format("%02d:%02d", picker.hour, picker.minute)
                binding.sectionTime.habitTimeInput.setText(formatted)
                viewModel.setStartTime(formatted)
                updateContinueButton()
            }

            picker.show(childFragmentManager, "habit_time_picker")
        }

        binding.sectionTime.habitTimeInput.setOnClickListener { openPicker() }
        binding.sectionTime.addTimeButton.setOnClickListener { openPicker() }
    }

    //reminder
    private fun setupReminderSection() {

        val reminderOptions = listOf(
            "At habit time",
            "5 minutes before",
            "15 minutes before",
            "30 minutes before",
            "1 hour before"
        )

        val reminderCodes = listOf(
            "AT_TIME",
            "5_MIN_BEFORE",
            "15_MIN_BEFORE",
            "30_MIN_BEFORE",
            "1_HR_BEFORE"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            reminderOptions
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.sectionReminder.spinner.adapter = adapter

        // Spinner starts hidden — only shown when reminder is switched on
        binding.sectionReminder.spinner.visibility = View.GONE

        binding.sectionReminder.spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    viewModel.setReminderTime(reminderCodes[position])
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        binding.sectionReminder.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setReminderEnabled(isChecked)
            binding.sectionReminder.spinner.visibility =
                if (isChecked) View.VISIBLE else View.GONE

            // Seed a default reminder code when first switched on
            if (isChecked) {
                viewModel.setReminderTime(reminderCodes[0]) // "AT_TIME"
            }
        }
    }


    private fun updateContinueButton() {
        viewModel.checkFormReady()
    }

    private fun onAfterChanged(action: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            action(s?.toString()?.trim() ?: "")
            updateContinueButton()
        }
    }



}
