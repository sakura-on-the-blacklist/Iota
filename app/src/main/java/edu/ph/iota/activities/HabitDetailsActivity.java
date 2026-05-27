package edu.ph.iota.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.ph.iota.R;
import edu.ph.iota.adapters.HabitProgressCardAdapter;
import edu.ph.iota.repositories.HabitLogRepository;
import edu.ph.iota.repositories.ReflectionRepository;
import edu.ph.iota.utilities.HabitToMonthFormatting;
import edu.ph.iota.viewmodels.HabitProgressCardViewModel;
import edu.ph.iota.viewmodels.HabitProgressMonthViewModel;
import kotlin.Unit;

public class HabitDetailsActivity extends AppCompatActivity {

    // Intent extra keys — used by HomeFragment when launching this activity
    public static final String EXTRA_HABIT_ID       = "habitId";
    public static final String EXTRA_HABIT_NAME     = "habitName";
    public static final String EXTRA_STREAK         = "streak";
    public static final String EXTRA_LONGEST_STREAK = "longestStreak";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_habit_details);

        setupButtons();
        setupData();
        setupStreakMilestones(
            getIntent().getIntExtra(EXTRA_LONGEST_STREAK, 0)
        );
    }
    public void setupButtons(){
        Button btnX = findViewById(R.id.button_x);
        btnX.setOnClickListener(v -> finish());

        int longestStreak = getIntent().getIntExtra(EXTRA_LONGEST_STREAK, 0);
        // this one is for the MILESTONES page
        Button buttonViewAll = findViewById(R.id.buttonViewAll);
        buttonViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(HabitDetailsActivity.this, HabitMilestonesActivity.class);
            intent.putExtra("longestStreak", longestStreak);
            startActivity(intent);
        });

        Button buttonPreviousLogs = findViewById(R.id.buttonPreviousLogs);
        buttonPreviousLogs.setOnClickListener(v -> {
            Intent intent = new Intent(HabitDetailsActivity.this, PrevReflectionsActivity.class);
            startActivity(intent);
        });

        Button buttonLogReflection = findViewById(R.id.buttonReflection);
        if (buttonLogReflection != null) {
            buttonLogReflection.setOnClickListener(v -> {
                Intent intent = new Intent(HabitDetailsActivity.this, ReflectionActivity.class);
                startActivity(intent);
            });
        }

    }

    public void setupData(){
        String habitId = getIntent().getStringExtra(EXTRA_HABIT_ID);
        String habitName = getIntent().getStringExtra(EXTRA_HABIT_NAME);
        this.streakNumber = String.valueOf(getIntent().getIntExtra(EXTRA_STREAK, 0));
        this.habitTrackingGoal = habitName;

        // REQUEST FOR THE HABIT LOGS
        assert habitId != null;
        HabitLogRepository repository = new HabitLogRepository(FirebaseFirestore.getInstance());
        repository.getLogs(habitId, "0", "999",
            logs -> {
                // THIS PART GET REQUESTS UR DATA
                HabitToMonthFormatting datesFormatter = new HabitToMonthFormatting();
                datesFormatter.HabitLogs = logs;
                datesFormatter.ListHabitLogToListYMD();
                List<HabitToMonthFormatting.YMD> dates = datesFormatter.YMDs;

                // THIS PART FORMATS YOUR DATA FOR THE MONTH FRAGMENT THING
                List<HabitProgressMonthViewModel> ListMonthModel = new ArrayList<>();
                for(HabitToMonthFormatting.YMD date: dates){
                    ListMonthModel.add(new HabitProgressMonthViewModel(
                        date.month,
                        date.year,
                        date.dates.stream().mapToInt(Integer::intValue).toArray()
                    ));
                }

                this.habitTrackingEndNumber = String.valueOf(66 - logs.size());
                this.completionRate = String.valueOf((logs.size()*100)/66);
                this.ListMonthModel = ListMonthModel;
                runOnUiThread(this::setupCard);
                runOnUiThread(this::setupRecordsAndStreaks);
                return Unit.INSTANCE;
            },
            error -> {
                runOnUiThread(() -> Toast.makeText(
                    getApplicationContext(),
                    "Error: " + error, Toast.LENGTH_SHORT).show()
                );
                return Unit.INSTANCE;
            }
        );

        // REQUEST FOR THE HABIT DETAILS
        // we need a new request here for the start date an the estimated time to end
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference habitsCollection = firestore.collection("habits");
        habitsCollection
            .whereEqualTo("habitId", habitId)
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
                DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                this.habitTrackingStartDate = sdf.format(doc.getTimestamp("createdAt").toDate());

                this.habitTitle = doc.getString("habitName");
                this.habitIdentity = doc.getString("identity");
                this.frequencyDays = (List<String>) doc.get("frequencyDays");
                this.frequencyTime = doc.getString("startTime");
                runOnUiThread(this::setupReflection);
                runOnUiThread(this::setupHeaders);
                runOnUiThread(this::setupReflection);
            })
            .addOnFailureListener(e -> {
                runOnUiThread(() -> Toast.makeText(
                    getApplicationContext(),
                    "Error: " + e, Toast.LENGTH_SHORT).show()
                );
            });

        // REQUEST FOR THE LAST HABIT LOG
        CollectionReference reflectionsCollection = firestore.collection("reflections");
        reflectionsCollection
            .whereEqualTo("habitId", habitId)
            .get()
            .addOnSuccessListener(querySnapshot -> {

                for (DocumentSnapshot doc:querySnapshot.getDocuments()){
                    if(this.lastReflectionLogDate == null){
                        this.lastReflectionLogDate = doc.getString("date");
                        this.lastReflectionLog = doc.getString("content");
                    }else if(Objects.requireNonNull(doc.getString("date")).compareTo(this.lastReflectionLogDate) > 0){
                        this.lastReflectionLogDate = doc.getString("date");
                        this.lastReflectionLog = doc.getString("content");
                    }
                }
                runOnUiThread(this::setupReflection);
            })
            .addOnFailureListener(e -> {
                runOnUiThread(() -> Toast.makeText(
                        getApplicationContext(),
                        "Error: " + e, Toast.LENGTH_SHORT).show()
                );
            });

    }

    private RecyclerView habit_progress_card;
    private String habitTrackingStartDate = null;
    private String habitTrackingGoal = null;
    private String habitTrackingEndNumber = null;
    private List<HabitProgressMonthViewModel> ListMonthModel = null;
    private void setupCard(){
        List<HabitProgressCardViewModel> ListCardModel = new ArrayList<>() {{
            add(new HabitProgressCardViewModel(
                habitTrackingStartDate,
                habitTrackingGoal,
                habitTrackingEndNumber,
                ListMonthModel
            ));
        }};
        habit_progress_card = findViewById(R.id.habit_progress_card);
        habit_progress_card.setLayoutManager(new LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
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


    private String completionRate = "0";
    private String streakNumber = "0";
    private TextView TV_completion_rate;
    private TextView TV_streak_number;
    private void setupRecordsAndStreaks(){

        // RECORDS AND STREAKS
        // Streaks is just EXTRA_STREAK
        // completion rate is <logs.size()> / 66
        TV_completion_rate = findViewById(R.id.completion_rate);
        TV_streak_number = findViewById(R.id.streak_number);
        TV_completion_rate.setText(completionRate + "%");
        TV_streak_number.setText(streakNumber);
    }

    private String lastReflectionLogDate = null;
    private String lastReflectionLog = null;
    private void setupReflection(){
        // REFLECTION LOG
        // how do you feel your progress towards peforming <habit_title>
        // last reflectiion log date + last reflection log
        TextView TV_reflection_title;
        TV_reflection_title = findViewById(R.id.reflection_title);
        TV_reflection_title.setText("How do you feel about your progress towards forming \"" + habitTitle + "\" habit?");
        TextView TV_reflection_last_review;
        TV_reflection_last_review = findViewById(R.id.reflection_last_review);
        TV_reflection_last_review.setText(lastReflectionLogDate + ":\n" + lastReflectionLog);
    }

    private String habitTitle;
    private String habitIdentity;
    private List<String> frequencyDays = new ArrayList<>();
    private String frequencyTime = null;
    private TextView TV_habit_statement;
    private TextView TV_habit_time;
    private void setupHeaders(){

        // TOP STUFF
        // sets up the title thing I will <habit_title> so that I can <habit_identity>
        // <Daily, or like MWF> at <time>

        TV_habit_statement = findViewById(R.id.habit_statement);
        TV_habit_time = findViewById(R.id.habit_time);
        TV_habit_statement.setText("I will " + habitTitle + " so that I can " + habitIdentity);
        String frequencyDaysString = "";
        if (frequencyDays.size() == 7){
            frequencyDaysString = "Daily";
        }else if(frequencyDays.size() == 1){
            frequencyDaysString += frequencyDays.get(0);
        } else if(frequencyDays.size() < 1){
            frequencyDaysString = "NULL";
        }
        else{
            for(String day: frequencyDays){
                frequencyDaysString += day + ", ";
            }
            frequencyDaysString = frequencyDaysString.substring(0, frequencyDaysString.length() - 2);
        }
        frequencyDaysString += " at " + frequencyTime;
        TV_habit_time.setText(frequencyDaysString);
    }
    private ImageView IV_milesstone_badge_1;
    private ImageView IV_milesstone_badge_2;
    private ImageView IV_milesstone_badge_3;
    private void setupStreakMilestones(int longestStreakNumber){
        // MILESTONES
        IV_milesstone_badge_1 = findViewById(R.id.milesstone_badge_1);
        IV_milesstone_badge_2 = findViewById(R.id.milesstone_badge_2);
        IV_milesstone_badge_3 = findViewById(R.id.milesstone_badge_3);
        int[] badge_c = {
                R.drawable.day1_c,
                R.drawable.day3_c,
                R.drawable.day5_c,
                R.drawable.day7_c,
                R.drawable.day10_c,
                R.drawable.day14_c,
                R.drawable.day21_c,
                R.drawable.day25_c,
                R.drawable.day50_c,
                R.drawable.day60_c,
                R.drawable.day66_c
        };
        int[] badge_nc = {
                R.drawable.day1_nc,
                R.drawable.day3_nc,
                R.drawable.day5_nc,
                R.drawable.day7_nc,
                R.drawable.day10_nc,
                R.drawable.day14_nc,
                R.drawable.day21_nc,
                R.drawable.day25_nc,
                R.drawable.day50_nc,
                R.drawable.day60_nc,
                R.drawable.day66_nc
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

        if(longestStreakNumber == 0){
            IV_milesstone_badge_1.setImageResource(badge_nc[0]);
            IV_milesstone_badge_2.setImageResource(badge_nc[1]);
            IV_milesstone_badge_3.setImageResource(badge_nc[2]);
        }else if(longestStreakNumber == badge_days.length - 1){
            IV_milesstone_badge_1.setImageResource(badge_c[badge_days.length-3]);
            IV_milesstone_badge_2.setImageResource(badge_c[badge_days.length-2]);
            IV_milesstone_badge_3.setImageResource(badge_nc[badge_days.length-1]);
        }else if(longestStreakNumber == badge_days.length){
            IV_milesstone_badge_1.setImageResource(badge_c[badge_days.length-3]);
            IV_milesstone_badge_2.setImageResource(badge_c[badge_days.length-2]);
            IV_milesstone_badge_3.setImageResource(badge_c[badge_days.length-1]);
        }else{
            int i = 0;
            for (int day:badge_days){
                if (longestStreakNumber < day){
                    IV_milesstone_badge_1.setImageResource(badge_c[i-1]);
                    IV_milesstone_badge_2.setImageResource(badge_nc[i]);
                    IV_milesstone_badge_3.setImageResource(badge_nc[i+1]);
                    break;
                }
                i++;
            }
        }
    }
}