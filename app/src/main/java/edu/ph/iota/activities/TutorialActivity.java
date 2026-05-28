package edu.ph.iota.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import edu.ph.iota.R;

public class TutorialActivity extends AppCompatActivity {

    private static final String ARG_IMAGE_RES = "image_res";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";
    private static final String ARG_PROG = "prog";

    private int currentStep = 1;

    public static Intent newIntent(android.content.Context context, int imageRes, String title, String desc, String prog) {
        Intent intent = new Intent(context, TutorialActivity.class);
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_RES, imageRes);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESC, desc);
        args.putString(ARG_PROG, prog);
        intent.putExtras(args);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_tutorial_page);

        ImageView ivStep = findViewById(R.id.ivStepImage);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvDesc = findViewById(R.id.tvDescription);
        TextView tvProg = findViewById(R.id.tvProgress);

        TextView tvPart1 = findViewById(R.id.tvPart1);
        TextView tvHighlightedPart = findViewById(R.id.tvHighlightedPart);
        TextView tvPart2 = findViewById(R.id.tvPart2);

        Button btnNext = findViewById(R.id.btnNext);
        Button btnSkip = findViewById(R.id.btnSkip);

        tvTitle.setText("Tutorial");
        tvProg.setText("1/3");
        tvPart1.setText("I will ");
        tvHighlightedPart.setText("meditate for 5 mins,");
        tvPart2.setText("when I wake up so that I can become a mindful person.");

        btnNext.setOnClickListener(v -> {
            if (currentStep == 1) {
                tvPart1.setText("I will meditate for 5 mins, ");
                tvHighlightedPart.setText("when I wake up");
                tvPart2.setText(" so that I can become a mindful person.");
                tvProg.setText("2/3");

                btnSkip.setVisibility(View.VISIBLE);
                btnNext.setText("Next");
                currentStep = 2;

            } else if (currentStep == 2) {
                tvPart1.setText("I will meditate for 5 mins, when I wake up so that I can become ");
                tvHighlightedPart.setText("a mindful person.");
                tvPart2.setText("");
                tvProg.setText("3/3");

                btnSkip.setVisibility(View.GONE);
                btnNext.setText("Get started");
                currentStep = 3;

            } else if (currentStep == 3) {
                Intent intent = new Intent(this, HabitSettingActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSkip.setOnClickListener(v -> {
            Intent intent = new Intent(this, HabitSettingActivity.class);
            startActivity(intent);
            finish();
        });
    }
}