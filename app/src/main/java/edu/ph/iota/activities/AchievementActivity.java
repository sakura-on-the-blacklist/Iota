package edu.ph.iota.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import edu.ph.iota.R;

/**
 * AchievementActivity — shown right after the user logs a habit.
 *
 * What it shows (no habit name — pure streak):
 *
 *   milestone_number  → the raw streak count (e.g. "15")
 *   headline_text     → dynamic message based on streak milestone
 *   congrats_text     → dynamic sub-message (which day of which week)
 *   day_tracker       → 7 circles labelled 1–7, showing progress
 *                       through the CURRENT week of the streak cycle
 *
 * Day tracker logic:
 *   streak = 5  → circles 1–5 lit,  6–7 dim   (day 5 of week 1)
 *   streak = 7  → circles 1–7 lit              (week 1 complete)
 *   streak = 8  → circle  1   lit,  2–7 dim   (day 1 of week 2)
 *   streak = 15 → circle  1   lit,  2–7 dim   (day 1 of week 3)
 *   streak = 14 → circles 1–7 lit              (week 2 complete)
 */
public class AchievementActivity extends AppCompatActivity {

    // Intent extra keys — used by HomeFragment when launching this activity
    public static final String EXTRA_HABIT_ID       = "habitId";
    public static final String EXTRA_HABIT_NAME     = "habitName";
    public static final String EXTRA_STREAK         = "streak";
    public static final String EXTRA_LONGEST_STREAK = "longestStreak";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        String habitId       = getIntent().getStringExtra(EXTRA_HABIT_ID);
        int    streak        = getIntent().getIntExtra(EXTRA_STREAK, 0);
        int    longestStreak = getIntent().getIntExtra(EXTRA_LONGEST_STREAK, 0);

        setupStreakDisplay(streak, longestStreak);
        setupDayTracker(streak);
        setupButtons(habitId, streak, longestStreak);
    }

    // ── Streak text ───────────────────────────────────────────────────────────

    /**
     * Fills milestone_number, headline_text, and congrats_text
     * dynamically based on where the streak sits in the week cycle.
     */
    private void setupStreakDisplay(int streak, int longestStreak) {

        TextView milestoneNumber = findViewById(R.id.milestone_number);
        TextView headlineText    = findViewById(R.id.headline_text);
        TextView congratsText    = findViewById(R.id.congrats_text);

        // Big number = raw streak
        milestoneNumber.setText(String.valueOf(streak));

        int completedWeeks    = streak / 7;       // full weeks done
        int daysInCurrentWeek = streak % 7;       // 0 means a full week was just finished

        // ── Headline ──────────────────────────────────────────────────────────
        if (streak == 1) {
            headlineText.setText("1st Step to Greatness!");
        } else if (daysInCurrentWeek == 0) {
            // Just completed a full week
            headlineText.setText("Week " + completedWeeks + " Complete! 🎉");
        } else if (streak == longestStreak) {
            headlineText.setText("New Personal Best! 🏆");
        } else {
            headlineText.setText("Day " + streak + " Streak! 🔥");
        }

        // ── Sub-message ───────────────────────────────────────────────────────
        if (streak == 1) {
            congratsText.setText("You've taken the first step. Keep going!");
        } else if (daysInCurrentWeek == 0) {
            congratsText.setText(
                    "Congrats! You've completed " + completedWeeks +
                            " full week" + (completedWeeks > 1 ? "s" : "") + "!"
            );
        } else {
            int currentWeek = completedWeeks + 1;
            congratsText.setText(
                    "Day " + daysInCurrentWeek + " of Week " + currentWeek +
                            " — don't break the chain!"
            );
        }
    }

    // ── Day tracker ───────────────────────────────────────────────────────────

    /**
     * The 7 circles represent days 1–7 within the CURRENT week of the streak.
     *
     * Labels change from "Mon/Tue..." to "1/2/3/4/5/6/7".
     * A circle is lit (full opacity) if its day number has been completed
     * in the current week cycle.
     *
     * Examples:
     *   streak=5  → lit: 1,2,3,4,5   dim: 6,7
     *   streak=7  → lit: 1,2,3,4,5,6,7   (all — week complete)
     *   streak=8  → lit: 1            dim: 2,3,4,5,6,7
     *   streak=14 → lit: 1,2,3,4,5,6,7   (all — week 2 complete)
     */
    private void setupDayTracker(int streak) {
        int[] ids = {
                R.id.day1, R.id.day2, R.id.day3,
                R.id.day4, R.id.day5, R.id.day6, R.id.day7
        };

        int daysInCurrentWeek = streak % 7;
        // If streak is a multiple of 7 (and > 0), the whole week is done — light all 7
        int litCount = (daysInCurrentWeek == 0 && streak > 0) ? 7 : daysInCurrentWeek;

        for (int i = 0; i < ids.length; i++) {
            View     includeView = findViewById(ids[i]);
            TextView tvLabel     = includeView.findViewById(R.id.tvDayName);
            View     circle      = includeView.findViewById(R.id.day_circle_view);

            // Label is now the day number within the week (1–7), not Mon/Tue/...
            tvLabel.setText(String.valueOf(i + 1));

            // Lit = completed this week cycle, dim = not yet
            boolean isLit = (i < litCount);
            float alpha   = isLit ? 1.0f : 0.3f;
            circle.setAlpha(alpha);
            tvLabel.setAlpha(alpha);
        }
    }

    // ── Buttons ───────────────────────────────────────────────────────────────

    private void setupButtons(String habitId, int streak, int longestStreak) {

        // Back to home — closes this activity, returns to HomeFragment
        Button btnHome = findViewById(R.id.back_to_home_button);
        btnHome.setOnClickListener(v -> finish());

        // View habit details — goes to the progress summary page
        Button btnViewDetails = findViewById(R.id.view_habit_button);
        btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(AchievementActivity.this, HabitDetails.class);
            intent.putExtra("habitId",       habitId);
            intent.putExtra("streak",        streak);
            intent.putExtra("longestStreak", longestStreak);
            startActivity(intent);
        });
    }
}