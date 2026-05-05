package edu.ph.iota.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.ph.iota.R;
import edu.ph.iota.adapters.HabitProgressMonthAdapter;
import edu.ph.iota.viewmodels.HabitProgressMonthViewModel;

public class test_HabitProgressCardView extends AppCompatActivity {

    RecyclerView habit_progress_months;
    TextView habitTrackingGoal, habitTrackingStartDate, habitTrackingEndNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_habit_progress_card);

        setupMonths();
        setupDates();
    }

    public void setupMonths(){
        // SOME TEMPORARY DATA
        List<HabitProgressMonthViewModel> ListMonthModel = new ArrayList<>() {{
            add(new HabitProgressMonthViewModel(2, 2026, new int[]{1, 2, 3, 4, 27, 28, 29}));
            add(new HabitProgressMonthViewModel(3, 2026, new int[]{1, 5, 6, 7, 10, 15, 18}));
            add(new HabitProgressMonthViewModel(4, 2026, new int[]{2, 3, 8, 9, 12, 16, 20}));
            add(new HabitProgressMonthViewModel(5, 2026, new int[]{1, 4, 5, 6, 14, 22, 30}));
            add(new HabitProgressMonthViewModel(6, 2026, new int[]{3, 10, 11, 18, 19, 24, 28}));
            add(new HabitProgressMonthViewModel(7, 2026, new int[]{1, 8, 9, 15, 16, 21, 29}));
        }};

        habit_progress_months = findViewById(R.id.habit_progress_months);
        habit_progress_months.setLayoutManager(new LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
        ));
        HabitProgressMonthAdapter adapter = new HabitProgressMonthAdapter(ListMonthModel);
        habit_progress_months.setAdapter(adapter);
    }
    public void setupDates(){
        habitTrackingGoal = findViewById(R.id.habitTrackingGoal);
        habitTrackingStartDate = findViewById(R.id.habitTrackingStartDate);
        habitTrackingEndNumber = findViewById(R.id.habitTrackingEndNumber);

        habitTrackingGoal.setText(R.string.habit_details_example_goal_short);
        habitTrackingStartDate.setText("December 5, 2025");
        habitTrackingEndNumber.setText("35");
    }
}