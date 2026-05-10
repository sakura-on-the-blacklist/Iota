package edu.ph.iota.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import edu.ph.iota.HexagonBadgeView;
import edu.ph.iota.R;


public class HabitDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_details);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        setupBadge(R.id.item_1, "1", false);
        setupBadge(R.id.item_3, "3", true);
        setupBadge(R.id.item_5, "5", true);
        setupBadge(R.id.item_7, "7", true);
        setupBadge(R.id.item_10, "10", true);
        setupBadge(R.id.item_14, "14", true);
        setupBadge(R.id.item_21, "21", true);
        setupBadge(R.id.item_25, "25", true);
        setupBadge(R.id.item_50, "50", true);
        setupBadge(R.id.item_60, "60", true);
        setupBadge(R.id.item_66, "66", true);
    }

    private void setupBadge(int resId, String count, boolean locked) {
        View container = findViewById(resId);
        HexagonBadgeView badge = container.findViewById(R.id.badge_view);
        TextView label = container.findViewById(R.id.badge_text);

        badge.setData(count, locked);
        label.setText(count + (count.equals("66") ? " Repetitions!" : " Repetitions"));
    }
}