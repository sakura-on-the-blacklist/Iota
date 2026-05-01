package edu.ph.iota.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.ph.iota.R;
import edu.ph.iota.adapters.HabitProgressMonthAdapter;
import edu.ph.iota.viewmodels.HabitProgressMonthViewModel;

public class HabitProgressCardView extends AppCompatActivity {

    RecyclerView ViewRecycler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_habit_progress_card_view);

        ViewRecycler = findViewById(R.id.habit_progress_months);

        // SOME TEMPORARY DATA
        List<HabitProgressMonthViewModel> ListMonthModel = new ArrayList<>() {{
            add(new HabitProgressMonthViewModel(2, 2026, new int[]{1, 2, 3, 4, 27, 28, 29}));
            add(new HabitProgressMonthViewModel(3, 2026, new int[]{1, 5, 6, 7, 10, 15, 18}));
            add(new HabitProgressMonthViewModel(4, 2026, new int[]{2, 3, 8, 9, 12, 16, 20}));
            add(new HabitProgressMonthViewModel(5, 2026, new int[]{1, 4, 5, 6, 14, 22, 30}));
            add(new HabitProgressMonthViewModel(6, 2026, new int[]{3, 10, 11, 18, 19, 24, 28}));
            add(new HabitProgressMonthViewModel(7, 2026, new int[]{1, 8, 9, 15, 16, 21, 29}));
        }};

        ViewRecycler.setLayoutManager(new LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        ));
        HabitProgressMonthAdapter adapter = new HabitProgressMonthAdapter(ListMonthModel);
        ViewRecycler.setAdapter(adapter);
    }
}