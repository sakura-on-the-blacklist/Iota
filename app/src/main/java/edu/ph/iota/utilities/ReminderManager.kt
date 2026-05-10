package edu.ph.iota.utilities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import edu.ph.iota.models.Habit
import edu.ph.iota.receivers.ReminderReceiver
import java.util.Calendar

class ReminderManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(habit: Habit) {
        if (!habit.reminderEnabled || habit.startTime.isEmpty()) return

        val timeParts = habit.startTime.split(":")
        if (timeParts.size != 2) return

        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            // Adjust based on reminderTime (e.g., "5_MIN_BEFORE")
            when (habit.reminderTime) {
                "5_MIN_BEFORE" -> add(Calendar.MINUTE, -5)
                "15_MIN_BEFORE" -> add(Calendar.MINUTE, -15)
                "30_MIN_BEFORE" -> add(Calendar.MINUTE, -30)
                "1_HR_BEFORE" -> add(Calendar.HOUR_OF_DAY, -1)
            }

            // If time is in the past, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habitName", habit.habitName)
            putExtra("identity", habit.identity)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder(habitId: String) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
