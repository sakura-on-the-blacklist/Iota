package edu.ph.iota.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import edu.ph.iota.R
import edu.ph.iota.adapters.HabitProgressCardAdapter
import edu.ph.iota.repositories.HabitLogRepository
import edu.ph.iota.utilities.HabitToMonthFormatting
import edu.ph.iota.viewmodels.HabitProgressCardViewModel
import edu.ph.iota.viewmodels.HabitProgressMonthViewModel
import edu.ph.iota.viewmodels.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class HabitProgressFragment : Fragment() {
    private lateinit var habitProgressCard: RecyclerView
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.activity_habit_progress, container, false)
        habitProgressCard = view.findViewById(R.id.habit_progress_card)
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = viewModel.preferenceManager.getUserId()

        habitProgressCard = view.findViewById(R.id.habit_progress_card)
        getDataHabit()
    }
    // ==================================================
    // CARD SETUP
    // ==================================================
    private var userId: String? = null
    private val ListCardModel: MutableList<HabitProgressCardViewModel> = mutableListOf()

    private val ListHabitID: MutableList<String> = mutableListOf()
    private fun getDataHabit(){
        // first get the habits associated with the user
        val empty_month_list:MutableList<HabitProgressMonthViewModel?> = mutableListOf()
        val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
        firestore.collection("habits")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
                for (doc in snapshot.documents) {
                    val createdAtDate = doc.getDate("createdAt")
                    ListCardModel.add(
                        HabitProgressCardViewModel(
                            createdAtDate?.let { dateFormat.format(it) } ?: "",
                            doc.getString("habitName") ?: "",
                            "",
                            empty_month_list
                        )
                    )
                    doc.getString("habitId")?.let { habitId ->
                        ListHabitID.add(habitId)
                    }
                }
                getDataHabitLogs()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "ERROR\n${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getDataHabitLogs() {
        var i = 0
        for (habit:HabitProgressCardViewModel in ListCardModel) {
            val repository = HabitLogRepository(FirebaseFirestore.getInstance())
            repository.getLogs(
                ListHabitID[i],
                "0",
                "999",
                { logs ->
                    val datesFormatter = HabitToMonthFormatting()
                    datesFormatter.HabitLogs = logs
                    datesFormatter.ListHabitLogToListYMD()

                    val monthModels = datesFormatter.YMDs.map { ymd ->
                        HabitProgressMonthViewModel(
                            ymd.month,
                            ymd.year,
                            ymd.dates.toIntArray()
                        )
                    }
                    habit.listMonthModel = monthModels
                    habit.habitTrackingEndNumber = (66 - logs.size).toString();
                    activity?.runOnUiThread {
                        setupCards()
                    }
                },
                { e ->
                    Toast.makeText(requireContext(), "ERROR\n$e", Toast.LENGTH_SHORT).show()
                }
            )
            i += 1
        }
    }

    private fun setupCards() {
        habitProgressCard.layoutManager = object : LinearLayoutManager(
            requireContext(),
            VERTICAL,
            false
        ) {
            override fun canScrollHorizontally() = false
        }
        habitProgressCard.adapter = HabitProgressCardAdapter(ListCardModel)
    }
}