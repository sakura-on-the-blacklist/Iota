package ph.edu.mobdev_pomodoro;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PomodoroMainActivity extends AppCompatActivity {

    private LinearLayout projectListContainer;
    private SharedPreferences mainPrefs;
    private List<String> projects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pomodoro_main_activity);

        projectListContainer = findViewById(R.id.pomodoro_main_project_list);
        mainPrefs = getSharedPreferences("PomodoroMainPrefs", Context.MODE_PRIVATE);

        findViewById(R.id.pomodoro_main_add_btn).setOnClickListener(v -> showAddProjectDialog());

        loadProjects();
    }

    private void loadProjects() {
        projectListContainer.removeAllViews();
        Set<String> projectSet = mainPrefs.getStringSet("project_list", new HashSet<>());
        projects = new ArrayList<>(projectSet);
        for (String project : projects) {
            createProjectView(project);
        }
    }

    private void createProjectView(String projectName) {
        View itemView = getLayoutInflater().inflate(R.layout.pomodoro_project_item, null);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) (5 * getResources().getDisplayMetrics().density);
        params.setMargins(0, 0, 0, margin);
        itemView.setLayoutParams(params);

        TextView nameTv = itemView.findViewById(R.id.project_name_tv);
        nameTv.setText(projectName);

        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(this, PomodoroActivity.class);
            intent.putExtra("PROJECT_NAME", projectName);
            startActivity(intent);
        });

        View editBtn = itemView.findViewById(R.id.project_edit_btn);
        editBtn.setOnClickListener(v -> showEditMenu(projectName));

        projectListContainer.addView(itemView);
    }

    private void showEditMenu(String oldName) {
        String[] options = {"Rename", "Delete"};
        new AlertDialog.Builder(this)
                .setTitle("Edit Project")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showRenameDialog(oldName);
                    else deleteProject(oldName);
                })
                .show();
    }

    private void showRenameDialog(String oldName) {
        EditText input = new EditText(this);
        input.setText(oldName);
        new AlertDialog.Builder(this)
                .setTitle("Rename Project")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty() && !newName.equals(oldName)) {
                        projects.remove(oldName);
                        projects.add(newName);
                        saveAndRefresh();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProject(String name) {
        projects.remove(name);
        saveAndRefresh();
    }

    private void showAddProjectDialog() {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("New Project")
                .setView(input)
                .setPositiveButton("Create", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        projects.add(name);
                        saveAndRefresh();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveAndRefresh() {
        mainPrefs.edit().putStringSet("project_list", new HashSet<>(projects)).apply();
        loadProjects();
    }
}