package edu.ph.iota.adapters

import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import edu.ph.iota.databinding.ItemHabitPebbleBinding
import edu.ph.iota.models.Habit

/**
 * HabitAdapter drives the pebble list on the home screen.
 *
 * Each pebble is a rounded card matching the onboarding style.
 * Hold mechanic:
 *   ACTION_DOWN  → start 3-second CountDownTimer, show progress bar
 *   Each tick    → increment LinearProgressIndicator (0 → 100)
 *   ACTION_UP    → cancel timer, animate progress bar back to 0
 *   onFinish     → haptic feedback + call onHabitLogged(habit)
 */
class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val streakMap: Map<String, Int> = emptyMap(),
    private val onHabitLogged: (habit: Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.PebbleViewHolder>() {

    companion object {
        private const val HOLD_DURATION_MS = 3000L
        private const val TICK_INTERVAL_MS = 30L
        private const val PROGRESS_MAX     = 100
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    inner class PebbleViewHolder(
        private val binding: ItemHabitPebbleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var holdTimer: CountDownTimer? = null
        private var logged = false

        fun bind(habit: Habit) {

            binding.tvHabitName.text = habit.habitName
            binding.tvIdentity.text  = habit.identity

            val streak = streakMap[habit.habitId] ?: 0
            binding.tvStreak.text = if (streak > 0) "🔥 $streak" else "—"

            setupHoldToLog(habit)
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupHoldToLog(habit: Habit) {

            // The touch target is the card itself
            binding.cardView.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        logged = false
                        startHoldTimer(view, habit)
                        true
                    }
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        if (!logged) cancelHoldTimer()
                        true
                    }
                    else -> false
                }
            }
        }

        private fun startHoldTimer(view: View, habit: Habit) {

            binding.holdProgress.visibility = View.VISIBLE
            binding.holdProgress.progress   = 0

            // Subtle press-in scale so the card feels tactile
            binding.cardView.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(120)
                .start()

            holdTimer = object : CountDownTimer(HOLD_DURATION_MS, TICK_INTERVAL_MS) {

                override fun onTick(millisUntilFinished: Long) {
                    val elapsed  = HOLD_DURATION_MS - millisUntilFinished
                    val progress = ((elapsed.toFloat() / HOLD_DURATION_MS) * PROGRESS_MAX).toInt()
                    binding.holdProgress.progress = progress
                }

                override fun onFinish() {
                    logged = true
                    binding.holdProgress.progress = PROGRESS_MAX
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    resetCardVisuals(animate = false)
                    onHabitLogged(habit)
                }

            }.start()
        }

        private fun cancelHoldTimer() {
            holdTimer?.cancel()
            holdTimer = null
            resetCardVisuals(animate = true)
        }

        /**
         * Resets the card to its resting state.
         * @param animate true → drain the progress bar smoothly back to 0
         *                false → snap instantly (after a successful log)
         */
        private fun resetCardVisuals(animate: Boolean) {

            binding.cardView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(150)
                .start()

            if (animate && binding.holdProgress.progress > 0) {
                ObjectAnimator.ofInt(
                    binding.holdProgress,
                    "progress",
                    binding.holdProgress.progress,
                    0
                ).apply {
                    duration     = 250
                    interpolator = DecelerateInterpolator()
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            binding.holdProgress.visibility = View.INVISIBLE
                        }
                    })
                }.start()
            } else {
                binding.holdProgress.progress   = 0
                binding.holdProgress.visibility = View.INVISIBLE
            }
        }

        /** Called by RecyclerView when this ViewHolder is recycled — always cancel the timer. */
        fun onRecycled() {
            holdTimer?.cancel()
            holdTimer = null
            binding.holdProgress.progress   = 0
            binding.holdProgress.visibility = View.INVISIBLE
            binding.cardView.scaleX = 1f
            binding.cardView.scaleY = 1f
        }
    }

    // ── Adapter overrides ─────────────────────────────────────────────────────

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PebbleViewHolder {
        val binding = ItemHabitPebbleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PebbleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PebbleViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount() = habits.size

    override fun onViewRecycled(holder: PebbleViewHolder) {
        super.onViewRecycled(holder)
        holder.onRecycled()
    }

    // ── Public helpers ────────────────────────────────────────────────────────

    fun updateHabits(newHabits: List<Habit>) {
        habits.clear()
        habits.addAll(newHabits)
        notifyDataSetChanged()
    }
}