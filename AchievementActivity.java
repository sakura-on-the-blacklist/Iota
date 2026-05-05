package ph.edu.mobdevfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class AchievementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        ArrayList<String> selectedDays = getIntent().getStringArrayListExtra("SELECTED_DAYS");
        if (selectedDays == null) selectedDays = new ArrayList<>();

        setupDayLabels(selectedDays);

        Button btnHome = findViewById(R.id.back_to_home_button);
        btnHome.setOnClickListener(v -> finish());

        Button btnViewDetails = findViewById(R.id.view_habit_button);
        btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(AchievementActivity.this, HabitDetailsActivity.class);
            startActivity(intent);
        });
    }

    private void setupDayLabels(ArrayList<String> selected) {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        int[] ids = {R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6, R.id.day7};

        for (int i = 0; i < days.length; i++) {
            View includeView = findViewById(ids[i]);
            TextView tv = includeView.findViewById(R.id.tvDayName);
            View circle = includeView.findViewById(R.id.day_circle_view);

            tv.setText(days[i]);

            if (selected.contains(days[i])) {
                circle.setBackgroundResource(R.drawable.circle_outline);
                circle.setAlpha(1.0f);
                tv.setAlpha(1.0f);
            } else {
                circle.setBackgroundResource(R.drawable.circle_outline);
                circle.setAlpha(0.3f);
                tv.setAlpha(0.3f);
            }
        }
    }
}