package edu.ph.iota.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import edu.ph.iota.databinding.ItemCalendarDayBinding
import edu.ph.iota.utilities.CalendarUtils.toDayName
import edu.ph.iota.utilities.CalendarUtils.toDayNumber
import edu.ph.iota.utilities.CalendarUtils.isToday
import java.time.LocalDate

class CalendarAdapter(
    private val days: List<LocalDate>,
    private val completedDates: Set<String>,
    private var selectedDate: LocalDate,
    private val onDateClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    // ── ViewHolder ────────────────────────────────────────────────────────────

    inner class DayViewHolder(
        private val binding: ItemCalendarDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(date: LocalDate) {

            binding.tvDayName.text   = date.toDayName()
            binding.tvDayNumber.text = date.toDayNumber()

            val isSelected = date == selectedDate
            binding.tvDayNumber.isSelected = isSelected

            binding.tvDayName.alpha   = if (date.isAfter(LocalDate.now())) 0.4f else 1f
            binding.tvDayNumber.alpha = if (date.isAfter(LocalDate.now())) 0.4f else 1f

            val dateKey = date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            binding.dotIndicator.visibility =
                if (completedDates.contains(dateKey))
                    android.view.View.VISIBLE
                else
                    android.view.View.INVISIBLE

            binding.root.setOnClickListener {
                if (!date.isAfter(LocalDate.now())) {
                    val previous = selectedDate
                    selectedDate = date

                    notifyItemChanged(days.indexOf(previous))
                    notifyItemChanged(days.indexOf(date))
                    onDateClick(date)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount() = days.size


    fun updateCompletedDates(newCompletedDates: Set<String>) {

        (completedDates as? MutableSet)?.let {
            it.clear()
            it.addAll(newCompletedDates)
            notifyDataSetChanged()
        }
    }
}