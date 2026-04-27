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

/**
 * CalendarAdapter drives the horizontal 7-day calendar bar.
 *
 * @param days           The 7 LocalDates for the current week (Mon–Sun).
 *                       Comes from CalendarUtils.getWeekDays().
 * @param completedDates Set of "YYYY-MM-DD" strings where the user logged
 *                       at least one habit. Used to show the dot indicator.
 * @param selectedDate   The currently highlighted date.
 * @param onDateClick    Called when the user taps a day — HomeFragment
 *                       uses this to reload the pebble list for that date.
 */
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

            // Day name ("Mon") and number ("23")
            binding.tvDayName.text   = date.toDayName()
            binding.tvDayNumber.text = date.toDayNumber()

            // Highlight: today gets a default highlight, selected date gets
            // the active state. Both are handled by calendar_day_selector.xml.
            val isSelected = date == selectedDate
            binding.tvDayNumber.isSelected = isSelected

            // Dim future dates slightly so users know they can't log ahead
            binding.tvDayName.alpha   = if (date.isAfter(LocalDate.now())) 0.4f else 1f
            binding.tvDayNumber.alpha = if (date.isAfter(LocalDate.now())) 0.4f else 1f

            // Dot indicator — show if any habit was logged on this date
            val dateKey = date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            binding.dotIndicator.visibility =
                if (completedDates.contains(dateKey))
                    android.view.View.VISIBLE
                else
                    android.view.View.INVISIBLE  // INVISIBLE keeps the space, GONE collapses it

            // Click — don't allow tapping future dates
            binding.root.setOnClickListener {
                if (!date.isAfter(LocalDate.now())) {
                    val previous = selectedDate
                    selectedDate = date
                    // Only rebind the two changed positions for efficiency
                    notifyItemChanged(days.indexOf(previous))
                    notifyItemChanged(days.indexOf(date))
                    onDateClick(date)
                }
            }
        }
    }

    // ── Adapter overrides ─────────────────────────────────────────────────────

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

    // ── Public helpers called by HomeFragment ─────────────────────────────────

    /**
     * Replaces the completed dates set and redraws all dots.
     * Call this after loading HabitLogs from Firestore.
     */
    fun updateCompletedDates(newCompletedDates: Set<String>) {
        // Note: adapter holds a var reference internally — safest to just
        // update and redraw since it's only 7 items
        (completedDates as? MutableSet)?.let {
            it.clear()
            it.addAll(newCompletedDates)
            notifyDataSetChanged()
        }
    }
}