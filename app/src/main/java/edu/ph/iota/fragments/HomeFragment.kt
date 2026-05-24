package edu.ph.iota.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import edu.ph.iota.activities.AchievementActivity
import edu.ph.iota.activities.HabitSettingActivity
import edu.ph.iota.activities.LogoutActivity
import edu.ph.iota.adapters.CalendarAdapter
import edu.ph.iota.adapters.HabitAdapter
import edu.ph.iota.databinding.FragmentHomeBinding
import edu.ph.iota.models.Habit
import edu.ph.iota.repositories.HabitLogRepository
import edu.ph.iota.utilities.CalendarUtils
import edu.ph.iota.utilities.CalendarUtils.toMonthYear
import edu.ph.iota.viewmodels.HomeViewModel
import java.time.LocalDate

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var habitAdapter: HabitAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendar()
        setupHabits()
        setViews()
        setObservers()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        val userId = viewModel.preferenceManager.getUserId() ?: return
        viewModel.loadHabits(userId)
    }

    private fun setViews() {
        binding.addHabitButton.setOnClickListener {
            startActivity(Intent(requireContext(), HabitSettingActivity::class.java))
        }

        binding.profileBtn.setOnClickListener {
            startActivity(Intent(requireContext(), LogoutActivity::class.java))
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupCalendar() {
        val weekDays = CalendarUtils.getWeekDays()

        binding.monthYearTV.text = weekDays.first().toMonthYear()

        val completedDates: Set<String> = emptySet()

        calendarAdapter = CalendarAdapter(
            days           = weekDays,
            completedDates = completedDates,
            selectedDate   = selectedDate,
            onDateClick    = { date ->
                selectedDate = date
                viewModel.filterHabitsForDate(date)
            }
        )

        binding.calendarRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = calendarAdapter
            isNestedScrollingEnabled = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupHabits() {

        habitAdapter = HabitAdapter(
            habits = mutableListOf(),

            onHabitLogged = { habit ->
                onHabitLogged(habit)
            }
        )

        binding.habitsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter       = habitAdapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setObservers() {
        viewModel.habitsForDay.observe(viewLifecycleOwner) { habits ->
            habitAdapter.updateHabits(habits)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onHabitLogged(habit: Habit) {
        val repo = HabitLogRepository()
        repo.logHabit(
            habit     = habit,
            date      = selectedDate,
            value     = habit.goalValue,   // counts as 1 full completion
            onSuccess = { updatedHabit, newStreak ->
                val intent = Intent(requireContext(), AchievementActivity::class.java).apply {
                    putExtra("habitId",    updatedHabit.habitId)
                    putExtra("habitName",  updatedHabit.habitName)
                    putExtra("streak",     newStreak)
                    putExtra("longestStreak", updatedHabit.longestStreak)
                }
                startActivity(intent)
            },
            onFailure = { message ->
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        )
    }
}