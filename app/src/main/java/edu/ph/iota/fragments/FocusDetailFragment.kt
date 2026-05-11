package edu.ph.iota.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import edu.ph.iota.PomodoroSettingRow
import edu.ph.iota.R
import java.util.HashSet
import java.util.Locale
import java.util.Set

class FocusDetailFragment : Fragment() {

    private lateinit var timerDisplay: TextView
    private lateinit var startStopButton: Button
    private lateinit var todoList: LinearLayout
    private lateinit var doneList: LinearLayout
    private lateinit var addTaskButton: TextView
    private lateinit var settingsButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var activityName: TextView

    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 0L
    private lateinit var prefs: SharedPreferences
    private var currentProjectName: String = "Default"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.pomodoro_activity_layout, container, false)

        prefs = requireContext().getSharedPreferences("PomodoroPrefs", Context.MODE_PRIVATE)

        timerDisplay      = view.findViewById(R.id.pomodoro_tv_timer_display)
        startStopButton   = view.findViewById(R.id.pomodoro_btn_start_stop)
        todoList          = view.findViewById(R.id.pomodoro_todo_list)
        doneList          = view.findViewById(R.id.pomodoro_done_list)
        addTaskButton     = view.findViewById(R.id.pomodoro_btn_add_task)
        settingsButton    = view.findViewById(R.id.pomodoro_btn_settings)
        backButton        = view.findViewById(R.id.pomodoro_btn_back)
        activityName      = view.findViewById(R.id.pomodoro_activity_name)

        val projectName = arguments?.getString("PROJECT_NAME")
        if (projectName != null) {
            currentProjectName = projectName
            activityName.text = projectName
        }

        loadTimerSettings()
        loadTasks()
        updateDisplay()

        startStopButton.setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
            } else {
                startTimer()
            }
        }

        addTaskButton.setOnClickListener { showTaskDialog() }
        settingsButton.setOnClickListener { showSettings() }
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun loadTimerSettings() {
        val focusMinutes = prefs.getInt("row_focus_$currentProjectName", 25)
        timeLeftInMillis = focusMinutes * 60_000L
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateDisplay()
            }

            override fun onFinish() {
                isTimerRunning = false
                startStopButton.text = "Start"
            }
        }.start()

        isTimerRunning = true
        startStopButton.text = "Stop"
    }

    private fun stopTimer() {
        timer?.cancel()
        isTimerRunning = false
        startStopButton.text = "Start"
    }

    private fun updateDisplay() {
        val minutes = (timeLeftInMillis / 1000L / 60L).toInt()
        val seconds = (timeLeftInMillis / 1000L % 60L).toInt()
        timerDisplay.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun showTaskDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("New Pomodoro Task")

        val input = EditText(requireContext())
        input.hint = "What are you working on?"
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val task = input.text.toString()
            if (task.trim().isNotEmpty()) {
                createTaskItem(task, false)
                saveTasks()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun createTaskItem(taskName: String, isChecked: Boolean) {
        val taskBox = CheckBox(requireContext()).apply {
            text = taskName
            setTextColor(0xFF4C463D.toInt())
            buttonTintList = ColorStateList.valueOf(0xFF4C463D.toInt())
        }

        val parent = if (isChecked) doneList else todoList
        parent.addView(taskBox)

        taskBox.setOnCheckedChangeListener { _, checked ->

            val currentParent = taskBox.parent as? ViewGroup
            currentParent?.removeView(taskBox)

            if (checked) {
                if (taskBox.parent == null) doneList.addView(taskBox)
            } else {
                if (taskBox.parent == null) todoList.addView(taskBox)
            }

            saveTasks()
        }

        taskBox.setOnLongClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Remove this task?")
                .setPositiveButton("Delete") { _, _ ->
                    val currentParent = taskBox.parent as? ViewGroup
                    currentParent?.removeView(taskBox)
                    saveTasks()
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
    }

    private fun saveTasks() {
        val todoSet: MutableSet<String> = HashSet()
        for (i in 0 until todoList.childCount) {
            val cb = todoList.getChildAt(i) as CheckBox
            todoSet.add(cb.text.toString())
        }

        val doneSet: MutableSet<String> = HashSet()
        for (i in 0 until doneList.childCount) {
            val cb = doneList.getChildAt(i) as CheckBox
            doneSet.add(cb.text.toString())
        }

        prefs.edit()
            .putStringSet("todo_$currentProjectName", todoSet)
            .putStringSet("done_$currentProjectName", doneSet)
            .apply()
    }

    private fun loadTasks() {
        todoList.removeAllViews()
        doneList.removeAllViews()

        val todoSet = prefs.getStringSet("todo_$currentProjectName", emptySet()) ?: emptySet()
        val doneSet = prefs.getStringSet("done_$currentProjectName", emptySet()) ?: emptySet()

        for (task in todoSet) {
            createTaskItem(task, false)
        }
        for (task in doneSet) {
            createTaskItem(task, true)
        }
    }

    private fun showSettings() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.pomodoro_settings_dialog, null)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val rowIds = intArrayOf(
            R.id.row_focus,
            R.id.row_short,
            R.id.row_long,
            R.id.row_long_after
        )

        for (id in rowIds) {
            val row = dialogView.findViewById<PomodoroSettingRow>(id) ?: continue
            val baseKey = resources.getResourceEntryName(id)

            val defaultValue = when (baseKey) {
                "row_focus" -> 25
                "row_short" -> 5
                "row_long" -> 15
                else       -> 4
            }

            val savedVal = prefs.getInt("${baseKey}_$currentProjectName", defaultValue)
            row.setValue(savedVal)

            row.setOnClickListener {
                showDetailSetting(row, baseKey)
            }
        }

        val closeBtn = dialogView.findViewById<TextView>(R.id.pomodoro_settings_close)
        closeBtn.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showDetailSetting(targetRow: PomodoroSettingRow, baseKey: String) {
        val builder = AlertDialog.Builder(requireContext())
        val v = layoutInflater.inflate(R.layout.pomodoro_settings_detail, null)
        builder.setView(v)

        val detailDialog = builder.create()
        detailDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val title = v.findViewById<TextView>(R.id.pomodoro_detail_title)
        val valueDisplay = v.findViewById<TextView>(R.id.pomodoro_detail_value)

        title.text = targetRow.title
        valueDisplay.text = targetRow.value

        v.findViewById<View>(R.id.pomodoro_detail_plus).setOnClickListener {
            val valInt = valueDisplay.text.toString().toInt() + 1
            applyDetailChange(targetRow, valueDisplay, baseKey, valInt)
        }

        v.findViewById<View>(R.id.pomodoro_detail_minus).setOnClickListener {
            var valInt = valueDisplay.text.toString().toInt()
            if (valInt > 1) {
                valInt--
                applyDetailChange(targetRow, valueDisplay, baseKey, valInt)
            }
        }

        v.findViewById<View>(R.id.pomodoro_detail_back).setOnClickListener {
            detailDialog.dismiss()
        }

        detailDialog.show()
    }

    private fun applyDetailChange(
        row: PomodoroSettingRow,
        display: TextView,
        baseKey: String,
        value: Int
    ) {
        val formatted = String.format(Locale.getDefault(), "%02d", value)
        display.text = formatted
        row.setValue(value)

        prefs.edit()
            .putInt("${baseKey}_$currentProjectName", value)
            .apply()

        if (baseKey == "row_focus" && !isTimerRunning) {
            timeLeftInMillis = value * 60_000L
            updateDisplay()
        }
    }
}