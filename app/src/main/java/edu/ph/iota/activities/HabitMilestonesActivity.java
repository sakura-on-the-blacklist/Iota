package edu.ph.iota.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import edu.ph.iota.HexagonBadgeView;
import edu.ph.iota.R;

public class HabitMilestonesActivity extends AppCompatActivity {

    public static final String EXTRA_LONGEST_STREAK = "longestStreak";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_milestones);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        int longestStreak = getIntent().getIntExtra(EXTRA_LONGEST_STREAK, 0);

        setupMilestones(longestStreak);
    }

    private void setupMilestones(int longestStreak){
        int[] badge_items = {
                R.id.item_1,
                R.id.item_3,
                R.id.item_5,
                R.id.item_7,
                R.id.item_10,
                R.id.item_14,
                R.id.item_21,
                R.id.item_25,
                R.id.item_50,
                R.id.item_60,
                R.id.item_66
        };
        int[] badge_days = {
                1,
                3,
                5,
                7,
                10,
                14,
                21,
                25,
                50,
                60,
                66
        };

        int i = 0;
        for (int day:badge_days){
            if(longestStreak >= day){
                setupBadge(badge_items[i], String.valueOf(day), false);
            }else{
                setupBadge(badge_items[i], String.valueOf(day), true);
            }
            i++;
        }
    }

    private void setupBadge(int resId, String count, boolean locked) {
        View container = findViewById(resId);
        HexagonBadgeView badge = container.findViewById(R.id.badge_view);
        TextView label = container.findViewById(R.id.badge_text);

        badge.setData(count, locked);
        label.setText(count + (count.equals("66") ? " Repetitions!" : " Repetitions"));
    }
}