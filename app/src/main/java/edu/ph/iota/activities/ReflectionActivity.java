package edu.ph.iota.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.functions.FirebaseFunctions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import android.view.View;

import edu.ph.iota.R;
import edu.ph.iota.models.Reflection;
import edu.ph.iota.repositories.ReflectionRepository;

public class ReflectionActivity extends AppCompatActivity {
    public static final String EXTRA_HABIT_ID       = "habitId";
    public static final String EXTRA_HABIT_NAME     = "habitName";

    private FirebaseFunctions mFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reflection);

        mFunctions = FirebaseFunctions.getInstance();

        TextView tvQuestion = findViewById(R.id.tvQuestion);
        String habitName = getIntent().getStringExtra(EXTRA_HABIT_NAME);
        tvQuestion.setText(
            "How do you feel about your progress towards forming “"
            + habitName
            + "” habit?");

        EditText etReflection = findViewById(R.id.textReflectionContent);
        Button btnSave = findViewById(R.id.btnSave);
        TextView tvAIResponse = findViewById(R.id.textAIResponse);

        View btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        btnSave.setOnClickListener(v -> {
            String userInput = etReflection.getText().toString().trim();
            if (!userInput.isEmpty()) {
                backend(userInput);
                tvAIResponse.setText("AIota is thinking...");
                callCloudFunction(userInput, tvAIResponse);
            }
        });
    }

    private void callCloudFunction(String userInput, TextView responseView) {
        Map<String, Object> data = new HashMap<>();
        data.put("reflection", userInput);

        mFunctions.getHttpsCallable("analyzeReflection")
            .call(data)
            .addOnSuccessListener(taskResult -> {
                Map<String, Object> result = (Map<String, Object>) taskResult.getData();

                if (result != null && result.containsKey("comment")) {
                    String aiComment = (String) result.get("comment");
                    responseView.setText(aiComment);

                    Toast.makeText(this, "Reflection Saved!", Toast.LENGTH_SHORT).show();

                    finish();
                } else {
                    responseView.setText("AIota left the notebook blank.");
                }
            })
            .addOnFailureListener(e -> {
                responseView.setText("AI analysis failed.");
                e.printStackTrace();
            });
    }

    private void backend(String content){
        ReflectionRepository repository = new ReflectionRepository();


        String habitId = getIntent().getStringExtra(EXTRA_HABIT_ID);
        assert habitId != null;
        Reflection reflection = new Reflection(
            "", // reflectionId
                habitId,
            "yea we dont actually need this", // dummy userId
                content,
                LocalDate.now().toString(),
            "May 27, 2026",
            System.currentTimeMillis()
        );

        repository.createReflection(reflection)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Reflection logged successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to log reflection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}