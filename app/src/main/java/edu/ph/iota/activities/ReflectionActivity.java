package edu.ph.iota.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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
            String userInput = etReflection.getText().toString();
            if (!userInput.isEmpty()) {
                tvAIResponse.setText("AIota is thinking...");
                callGroqAPI(userInput, tvAIResponse);
            }
        });
    }

    private void callGroqAPI(String userInput, TextView responseView) {
        String apiKey = "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"; // <----- key key key key
        OkHttpClient client = new OkHttpClient();

        String json = "{\"model\": \"llama3-8b-8192\", \"messages\": [{\"role\": \"user\", \"content\": \"" + userInput + "\"}]}";

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
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
                if (response.isSuccessful()) {
                    String rawJson = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(rawJson);
                        String cleanMessage = jsonObject.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        runOnUiThread(() -> responseView.setText(cleanMessage));
                    } catch (Exception e) {
                        runOnUiThread(() -> responseView.setText("AIota is having trouble reading that."));
                    }
                } else {
                    String errorCode = String.valueOf(response.code());
                    runOnUiThread(() -> responseView.setText("Server Error: " + errorCode));
                }
            }
        });
    }
}