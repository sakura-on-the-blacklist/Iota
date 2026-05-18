package edu.ph.iota.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

import edu.ph.iota.R;

public class SelectionActivity extends AppCompatActivity {

    private final String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private final int[] containerIds = {
            R.id.select_mon, R.id.select_tue, R.id.select_wed,
            R.id.select_thu, R.id.select_fri, R.id.select_sat, R.id.select_sun
    };
    private final boolean[] selectedStates = new boolean[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        for (int i = 0; i < containerIds.length; i++) {
            final int index = i;
            View container = findViewById(containerIds[i]);
            TextView label = container.findViewById(R.id.tv_day_label);
            View circle = container.findViewById(R.id.day_circle);

            label.setText(dayNames[i]);
            container.setOnClickListener(v -> {
                selectedStates[index] = !selectedStates[index];
                circle.setBackgroundResource(selectedStates[index] ? R.drawable.circle_solid_white : R.drawable.circle_outline);
            });
        }

        findViewById(R.id.btn_continue).setOnClickListener(v -> {
            ArrayList<String> selectedDays = new ArrayList<>();
            for (int i = 0; i < dayNames.length; i++) {
                if (selectedStates[i]) selectedDays.add(dayNames[i]);
            }
            Intent intent = new Intent(this, AchievementActivity.class);
            intent.putStringArrayListExtra("SELECTED_DAYS", selectedDays);
            startActivity(intent);
        });
    }
}