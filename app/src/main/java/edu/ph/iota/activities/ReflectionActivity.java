package edu.ph.iota.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import edu.ph.iota.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReflectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_reflection);

        EditText etReflection = findViewById(R.id.textReflectionContent);
        Button btnSave = findViewById(R.id.btnSave);
        TextView tvAIResponse = findViewById(R.id.textAIResponse);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String userInput = etReflection.getText().toString().trim();
            if (!userInput.isEmpty()) {
                tvAIResponse.setText("AIota is thinking...");
                callGroqAPI(userInput, tvAIResponse);
            }
        });
    }

    private void callGroqAPI(String userInput, TextView responseView) {
        String apiKey = "";
        OkHttpClient client = new OkHttpClient();

        String jsonPayload = "";
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "llama3-8b-8192");

            JSONArray messagesArray = new JSONArray();
            JSONObject messageObject = new JSONObject();
            messageObject.put("role", "user");
            messageObject.put("content", userInput);
            messagesArray.put(messageObject);

            jsonBody.put("messages", messagesArray);
            jsonPayload = jsonBody.toString();
        } catch (Exception e) {
            responseView.setText("Error constructing request.");
            return;
        }

        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> responseView.setText("Error: Could not reach AIota."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response resp = response) {
                    if (resp.isSuccessful() && resp.body() != null) {
                        String rawJson = resp.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(rawJson);
                            String cleanMessage = jsonObject.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            runOnUiThread(() -> responseView.setText(cleanMessage.trim()));
                        } catch (Exception e) {
                            runOnUiThread(() -> responseView.setText("AIota is having trouble reading that."));
                        }
                    } else {
                        String errorCode = String.valueOf(resp.code());
                        runOnUiThread(() -> responseView.setText("Server Error: " + errorCode));
                    }
                }
            }
        });
    }
}