package edu.ph.iota.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;

import edu.ph.iota.R;

import edu.ph.iota.fragments.PrevReflectionsFragment;

public class ReflectionActivity extends AppCompatActivity {

    private FirebaseFunctions mFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_reflection);

        mFunctions = FirebaseFunctions.getInstance();

        EditText etReflection = findViewById(R.id.textReflectionContent);
        Button btnSave = findViewById(R.id.btnSave);
        TextView tvAIResponse = findViewById(R.id.textAIResponse);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String userInput = etReflection.getText().toString().trim();
            if (!userInput.isEmpty()) {
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

                        getSupportFragmentManager().beginTransaction()
                                .replace(android.R.id.content, new PrevReflectionsFragment())
                                .addToBackStack(null)
                                .commit();

                    } else {
                        responseView.setText("AIota left the notebook blank.");
                    }
                })
                .addOnFailureListener(e -> {
                    responseView.setText("Error: Could not reach the server.");
                    e.printStackTrace();
                });
    }
}