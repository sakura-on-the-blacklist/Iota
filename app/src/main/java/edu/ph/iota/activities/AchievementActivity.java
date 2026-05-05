package edu.ph.iota.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import edu.ph.iota.R;

public class AchievementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        setupDayLabels();

        Button btnHome = findViewById(R.id.back_to_home_button);
        btnHome.setOnClickListener(v -> finish());
    }

    private void setupDayLabels() {
        String[] days = {"Fri", "Sat", "Sun", "Mon", "Tue", "Wed"};
        int[] ids = {R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6};

        for (int i = 0; i < days.length; i++) {
            View includeView = findViewById(ids[i]);
            TextView tv = includeView.findViewById(R.id.tvDayName);
            tv.setText(days[i]);
        }
    }
}