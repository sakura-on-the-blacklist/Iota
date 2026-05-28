package edu.ph.iota.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ph.iota.R;
import edu.ph.iota.adapters.HabitProgressCardAdapter;
import edu.ph.iota.viewmodels.HabitProgressCardViewModel;
import edu.ph.iota.viewmodels.HabitProgressMonthViewModel;

public class TEST_HabitProgressActivity extends AppCompatActivity {

    RecyclerView habit_progress_card;
    //TextView habitTrackingGoal, habitTrackingStartDate, habitTrackingEndNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_habit_progress);
        setupCards();
        setupButtons();
    }

    public void setupCards(){
        List<List<HabitProgressMonthViewModel>> listMonthModel = new ArrayList<>();
        listMonthModel.add(new ArrayList<>(Arrays.asList(
                new HabitProgressMonthViewModel(8, 2026, new int[]{1, 2, 3, 4, 5, 6, 7}), // full streak week
                new HabitProgressMonthViewModel(9, 2026, new int[]{2, 5, 9, 12, 18, 21, 27}), // scattered
                new HabitProgressMonthViewModel(10, 2026, new int[]{1, 3, 4, 5, 6, 20, 21, 22}), // early streak + late streak
                new HabitProgressMonthViewModel(11, 2026, new int[]{7, 14, 21, 28}), // weekly pattern
                new HabitProgressMonthViewModel(12, 2026, new int[]{1, 2, 10, 11, 12, 24, 25, 31}), // mixed clusters
                new HabitProgressMonthViewModel(1, 2027, new int[]{5, 6, 7, 8, 9, 15, 16, 30}), // mid + early streak
                new HabitProgressMonthViewModel(2, 2027, new int[]{1, 14, 15, 16, 28}), // sparse + mini streak
                new HabitProgressMonthViewModel(3, 2027, new int[]{3, 4, 5, 6, 7, 8, 9, 10}), // long streak
                new HabitProgressMonthViewModel(4, 2027, new int[]{1, 10, 20, 30}), // very sparse
                new HabitProgressMonthViewModel(5, 2027, new int[]{2, 3, 4, 10, 11, 12, 18, 25}), // multiple clusters
                new HabitProgressMonthViewModel(6, 2027, new int[]{1, 5, 9, 13, 17, 21, 25, 29}), // consistent interval
                new HabitProgressMonthViewModel(7, 2027, new int[]{1, 2, 3, 15, 16, 17, 28, 29, 30}) // split streaks
        )));
        listMonthModel.add(new ArrayList<>(Arrays.asList(
                new HabitProgressMonthViewModel(2, 2026, new int[]{1, 2, 3, 4, 27, 28, 29}),
                new HabitProgressMonthViewModel(3, 2026, new int[]{1, 5, 6, 7, 10, 15, 18}),
                new HabitProgressMonthViewModel(4, 2026, new int[]{2, 3, 8, 9, 12, 16, 20}),
                new HabitProgressMonthViewModel(5, 2026, new int[]{1, 4, 5, 6, 14, 22, 30}),
                new HabitProgressMonthViewModel(6, 2026, new int[]{3, 10, 11, 18, 19, 24, 28}),
                new HabitProgressMonthViewModel(7, 2026, new int[]{1, 8, 9, 15, 16, 21, 29})
        )));
        listMonthModel.add(new ArrayList<>(Arrays.asList(
                new HabitProgressMonthViewModel(8, 2026, new int[]{1, 2, 3, 4, 5, 6, 7}), // full streak week
                new HabitProgressMonthViewModel(9, 2026, new int[]{2, 5, 9, 12, 18, 21, 27}), // scattered
                new HabitProgressMonthViewModel(10, 2026, new int[]{1, 3, 4, 5, 6, 20, 21, 22}), // early streak + late streak
                new HabitProgressMonthViewModel(11, 2026, new int[]{7, 14, 21, 28}), // weekly pattern
                new HabitProgressMonthViewModel(12, 2026, new int[]{1, 2, 10, 11, 12, 24, 25, 31}) // mixed clusters
        )));

        List<HabitProgressCardViewModel> ListCardModel = new ArrayList<>();
        for(int i =0; i<listMonthModel.toArray().length; i++){
            ListCardModel.add(new HabitProgressCardViewModel(
                    "habitTrackingStartDate",
                    "habitTrackingGoal " + String.valueOf(i),
                    String.valueOf(i),
                    listMonthModel.get(i)
            ));
        }

        habit_progress_card = findViewById(R.id.habit_progress_card);
        habit_progress_card.setLayoutManager(new LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        ){
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        });
        HabitProgressCardAdapter adapter = new HabitProgressCardAdapter(ListCardModel);
        habit_progress_card.setAdapter(adapter);
    }
    public void setupButtons(){
        Button btnX = findViewById(R.id.button_x);
        btnX.setOnClickListener(v -> finish());
    }
}