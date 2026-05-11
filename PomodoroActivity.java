package ph.edu.mobdev_pomodoro;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PomodoroActivity extends AppCompatActivity {

    private TextView pomodoro_tv_timer_display;
    private Button pomodoro_btn_start_stop;
    private LinearLayout pomodoro_todo_list, pomodoro_done_list;
    private TextView pomodoro_btn_add_task;
    private ImageButton pomodoro_btn_settings;
    private ImageButton pomodoro_btn_back;
    private TextView pomodoro_activity_name;

    private CountDownTimer pomodoro_timer;
    private boolean isTimerRunning = false;
    private long pomodoro_timeLeftInMillis;
    private SharedPreferences prefs;
    private String currentProjectName = "Default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pomodoro_activity_layout);

        prefs = getSharedPreferences("PomodoroPrefs", Context.MODE_PRIVATE);

        pomodoro_tv_timer_display = findViewById(R.id.pomodoro_tv_timer_display);
        pomodoro_btn_start_stop = findViewById(R.id.pomodoro_btn_start_stop);
        pomodoro_todo_list = findViewById(R.id.pomodoro_todo_list);
        pomodoro_done_list = findViewById(R.id.pomodoro_done_list);
        pomodoro_btn_add_task = findViewById(R.id.pomodoro_btn_add_task);
        pomodoro_btn_settings = findViewById(R.id.pomodoro_btn_settings);
        pomodoro_btn_back = findViewById(R.id.pomodoro_btn_back);
        pomodoro_activity_name = findViewById(R.id.pomodoro_activity_name);

        String projectName = getIntent().getStringExtra("PROJECT_NAME");
        if (projectName != null) {
            currentProjectName = projectName;
            pomodoro_activity_name.setText(projectName);
        }

        pomodoro_load_timer_settings();
        pomodoro_load_tasks();
        pomodoro_update_display();

        pomodoro_btn_start_stop.setOnClickListener(v -> {
            if (isTimerRunning) {
                pomodoro_stop_timer();
            } else {
                pomodoro_start_timer();
            }
        });

        pomodoro_btn_add_task.setOnClickListener(v -> pomodoro_show_task_dialog());
        pomodoro_btn_settings.setOnClickListener(v -> pomodoro_show_settings());
        pomodoro_btn_back.setOnClickListener(v -> finish());
    }

    private void pomodoro_load_timer_settings() {
        int focusMinutes = prefs.getInt("row_focus_" + currentProjectName, 25);
        pomodoro_timeLeftInMillis = focusMinutes * 60000L;
    }

    private void pomodoro_start_timer() {
        pomodoro_timer = new CountDownTimer(pomodoro_timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                pomodoro_timeLeftInMillis = millisUntilFinished;
                pomodoro_update_display();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                pomodoro_btn_start_stop.setText("Start");
            }
        }.start();

        isTimerRunning = true;
        pomodoro_btn_start_stop.setText("Stop");
    }

    private void pomodoro_stop_timer() {
        if (pomodoro_timer != null) {
            pomodoro_timer.cancel();
        }
        isTimerRunning = false;
        pomodoro_btn_start_stop.setText("Start");
    }

    private void pomodoro_update_display() {
        int minutes = (int) (pomodoro_timeLeftInMillis / 1000) / 60;
        int seconds = (int) (pomodoro_timeLeftInMillis / 1000) % 60;
        pomodoro_tv_timer_display.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void pomodoro_show_task_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Pomodoro Task");

        final EditText input = new EditText(this);
        input.setHint("What are you working on?");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String task = input.getText().toString();
            if (!task.trim().isEmpty()) {
                pomodoro_create_task_item(task, false);
                pomodoro_save_tasks();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void pomodoro_create_task_item(String taskName, boolean isChecked) {
        CheckBox taskBox = new CheckBox(this);
        taskBox.setText(taskName);
        taskBox.setTextColor(0xFF4C463D);
        taskBox.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFF4C463D));
        taskBox.setChecked(isChecked);

        if (isChecked) {
            pomodoro_done_list.addView(taskBox);
        } else {
            pomodoro_todo_list.addView(taskBox);
        }

        taskBox.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) {
                pomodoro_todo_list.removeView(taskBox);
                if (taskBox.getParent() == null) {
                    pomodoro_done_list.addView(taskBox);
                }
            } else {
                pomodoro_done_list.removeView(taskBox);
                if (taskBox.getParent() == null) {
                    pomodoro_todo_list.addView(taskBox);
                }
            }
            pomodoro_save_tasks();
        });

        taskBox.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Remove this task?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        ViewGroup parent = (ViewGroup) taskBox.getParent();
                        if (parent != null) {
                            parent.removeView(taskBox);
                            pomodoro_save_tasks();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    private void pomodoro_save_tasks() {
        Set<String> todoSet = new HashSet<>();
        for (int i = 0; i < pomodoro_todo_list.getChildCount(); i++) {
            todoSet.add(((CheckBox) pomodoro_todo_list.getChildAt(i)).getText().toString());
        }

        Set<String> doneSet = new HashSet<>();
        for (int i = 0; i < pomodoro_done_list.getChildCount(); i++) {
            doneSet.add(((CheckBox) pomodoro_done_list.getChildAt(i)).getText().toString());
        }

        prefs.edit()
                .putStringSet("todo_" + currentProjectName, todoSet)
                .putStringSet("done_" + currentProjectName, doneSet)
                .apply();
    }

    private void pomodoro_load_tasks() {
        pomodoro_todo_list.removeAllViews();
        pomodoro_done_list.removeAllViews();

        Set<String> todoSet = prefs.getStringSet("todo_" + currentProjectName, new HashSet<>());
        Set<String> doneSet = prefs.getStringSet("done_" + currentProjectName, new HashSet<>());

        for (String task : todoSet) {
            pomodoro_create_task_item(task, false);
        }
        for (String task : doneSet) {
            pomodoro_create_task_item(task, true);
        }
    }

    private void pomodoro_show_settings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.pomodoro_settings_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        int[] rowIds = {R.id.row_focus, R.id.row_short, R.id.row_long, R.id.row_long_after};
        for (int id : rowIds) {
            PomodoroSettingRow row = dialogView.findViewById(id);
            if (row != null) {
                String baseKey = getResources().getResourceEntryName(id);
                int defaultValue;
                if (baseKey.equals("row_focus")) {
                    defaultValue = 25;
                } else if (baseKey.equals("row_short")) {
                    defaultValue = 5;
                } else if (baseKey.equals("row_long")) {
                    defaultValue = 15;
                } else {
                    defaultValue = 4;
                }
                int savedVal = prefs.getInt(baseKey + "_" + currentProjectName, defaultValue);
                row.setValue(savedVal);
                row.setOnClickListener(v -> pomodoro_show_detail(row, baseKey));
            }
        }

        TextView closeBtn = dialogView.findViewById(R.id.pomodoro_settings_close);
        closeBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void pomodoro_show_detail(PomodoroSettingRow targetRow, String baseKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.pomodoro_settings_detail, null);
        builder.setView(v);
        AlertDialog detailDialog = builder.create();
        if (detailDialog.getWindow() != null) {
            detailDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView title = v.findViewById(R.id.pomodoro_detail_title);
        TextView valueDisplay = v.findViewById(R.id.pomodoro_detail_value);

        title.setText(targetRow.getTitle());
        valueDisplay.setText(targetRow.getValue());

        v.findViewById(R.id.pomodoro_detail_plus).setOnClickListener(view -> {
            int val = Integer.parseInt(valueDisplay.getText().toString()) + 1;
            pomodoro_apply_detail_change(targetRow, valueDisplay, baseKey, val);
        });

        v.findViewById(R.id.pomodoro_detail_minus).setOnClickListener(view -> {
            int val = Integer.parseInt(valueDisplay.getText().toString());
            if (val > 1) {
                val--;
                pomodoro_apply_detail_change(targetRow, valueDisplay, baseKey, val);
            }
        });

        v.findViewById(R.id.pomodoro_detail_back).setOnClickListener(view -> detailDialog.dismiss());
        detailDialog.show();
    }

    private void pomodoro_apply_detail_change(PomodoroSettingRow row, TextView display, String baseKey, int val) {
        String formatted = String.format(Locale.getDefault(), "%02d", val);
        display.setText(formatted);
        row.setValue(val);

        prefs.edit().putInt(baseKey + "_" + currentProjectName, val).apply();

        if (baseKey.equals("row_focus") && !isTimerRunning) {
            pomodoro_timeLeftInMillis = val * 60000L;
            pomodoro_update_display();
        }
    }
}