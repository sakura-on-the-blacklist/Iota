package edu.ph.iota.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.ph.iota.R
import edu.ph.iota.adapters.HabitProgressCardAdapter
import edu.ph.iota.viewmodels.HabitProgressCardViewModel
import edu.ph.iota.viewmodels.HabitProgressMonthViewModel

class HabitProgressFragment : Fragment() {

    private lateinit var habitProgressCard: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_habit_progress, container, false)
        habitProgressCard = view.findViewById(R.id.habit_progress_card)
        setupCards()
        return view
    }

    private fun setupCards() {
        val listMonthModel: MutableList<List<HabitProgressMonthViewModel>> = mutableListOf()

        listMonthModel.add(
            listOf(
                HabitProgressMonthViewModel(8, 2026, intArrayOf(1, 2, 3, 4, 5, 6, 7)),
                HabitProgressMonthViewModel(9, 2026, intArrayOf(2, 5, 9, 12, 18, 21, 27)),
                HabitProgressMonthViewModel(10, 2026, intArrayOf(1, 3, 4, 5, 6, 20, 21, 22)),
                HabitProgressMonthViewModel(11, 2026, intArrayOf(7, 14, 21, 28)),
                HabitProgressMonthViewModel(12, 2026, intArrayOf(1, 2, 10, 11, 12, 24, 25, 31)),
                HabitProgressMonthViewModel(1, 2027, intArrayOf(5, 6, 7, 8, 9, 15, 16, 30)),
                HabitProgressMonthViewModel(2, 2027, intArrayOf(1, 14, 15, 16, 28)),
                HabitProgressMonthViewModel(3, 2027, intArrayOf(3, 4, 5, 6, 7, 8, 9, 10)),
                HabitProgressMonthViewModel(4, 2027, intArrayOf(1, 10, 20, 30)),
                HabitProgressMonthViewModel(5, 2027, intArrayOf(2, 3, 4, 10, 11, 12, 18, 25)),
                HabitProgressMonthViewModel(6, 2027, intArrayOf(1, 5, 9, 13, 17, 21, 25, 29)),
                HabitProgressMonthViewModel(7, 2027, intArrayOf(1, 2, 3, 15, 16, 17, 28, 29, 30))
            )
        )

        listMonthModel.add(
            listOf(
                HabitProgressMonthViewModel(2, 2026, intArrayOf(1, 2, 3, 4, 27, 28, 29)),
                HabitProgressMonthViewModel(3, 2026, intArrayOf(1, 5, 6, 7, 10, 15, 18)),
                HabitProgressMonthViewModel(4, 2026, intArrayOf(2, 3, 8, 9, 12, 16, 20)),
                HabitProgressMonthViewModel(5, 2026, intArrayOf(1, 4, 5, 6, 14, 22, 30)),
                HabitProgressMonthViewModel(6, 2026, intArrayOf(3, 10, 11, 18, 19, 24, 28)),
                HabitProgressMonthViewModel(7, 2026, intArrayOf(1, 8, 9, 15, 16, 21, 29))
            )
        )

        listMonthModel.add(
            listOf(
                HabitProgressMonthViewModel(8, 2026, intArrayOf(1, 2, 3, 4, 5, 6, 7)),
                HabitProgressMonthViewModel(9, 2026, intArrayOf(2, 5, 9, 12, 18, 21, 27)),
                HabitProgressMonthViewModel(10, 2026, intArrayOf(1, 3, 4, 5, 6, 20, 21, 22)),
                HabitProgressMonthViewModel(11, 2026, intArrayOf(7, 14, 21, 28)),
                HabitProgressMonthViewModel(12, 2026, intArrayOf(1, 2, 10, 11, 12, 24, 25, 31))
            )
        )

        val listCardModel = listMonthModel.mapIndexed { i, months ->
            HabitProgressCardViewModel(
                "habitTrackingStartDate",
                "habitTrackingGoal $i",
                i.toString(),
                months
            )
        }

        habitProgressCard.layoutManager = object : LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        ) {
            override fun canScrollHorizontally() = false
        }

        habitProgressCard.adapter = HabitProgressCardAdapter(listCardModel)
    }
}