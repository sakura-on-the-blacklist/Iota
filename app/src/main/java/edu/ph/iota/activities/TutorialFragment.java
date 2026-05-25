package edu.ph.iota.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import edu.ph.iota.R;
import edu.ph.iota.activities.HabitSettingActivity;

public class TutorialFragment extends Fragment {

    private static final String ARG_IMAGE_RES = "image_res";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";
    private static final String ARG_PROG = "prog";

    public static TutorialFragment newInstance(int imageRes, String title, String desc, String prog) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_RES, imageRes);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESC, desc);
        args.putString(ARG_PROG, prog);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial_page, container, false);

        ImageView ivStep = view.findViewById(R.id.ivStepImage);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDesc = view.findViewById(R.id.tvDescription);
        TextView tvProg = view.findViewById(R.id.tvProgress);

        TextView tvPart1 = view.findViewById(R.id.tvPart1);
        TextView tvHighlightedPart = view.findViewById(R.id.tvHighlightedPart);
        TextView tvPart2 = view.findViewById(R.id.tvPart2);

        Button btnNext = view.findViewById(R.id.btnNext);
        Button btnSkip = view.findViewById(R.id.btnSkip);

        if (getArguments() != null) {
            String title = getArguments().getString(ARG_TITLE);
            String description = getArguments().getString(ARG_DESC);
            String progressStr = getArguments().getString(ARG_PROG);

            ivStep.setImageResource(getArguments().getInt(ARG_IMAGE_RES));
            tvTitle.setText(title);
            tvProg.setText(progressStr);

            if (tvDesc != null && description != null) {
                tvDesc.setText(description);
            }

            if (progressStr != null && progressStr.contains("3/3")) {
                btnSkip.setVisibility(View.GONE);
                btnNext.setText("Get started");
            } else {
                btnSkip.setVisibility(View.VISIBLE);
                btnNext.setText("Next");
            }

            if (progressStr != null) {
                if (progressStr.contains("1/3")) {
                    tvPart1.setText("I will ");
                    tvHighlightedPart.setText("meditate for 5 mins,");
                    tvPart2.setText("when I wake up so that I can become a mindful person.");
                } else if (progressStr.contains("2/3")) {
                    tvPart1.setText("I will meditate for 5 mins, ");
                    tvHighlightedPart.setText("when I wake up");
                    tvPart2.setText(" so that I can become a mindful person.");
                } else if (progressStr.contains("3/3")) {
                    tvPart1.setText("I will meditate for 5 mins, when I wake up so that I can become ");
                    tvHighlightedPart.setText("a mindful person.");
                    tvPart2.setText("");
                }
            }
        }

        btnNext.setOnClickListener(v -> {
            if (getActivity() != null) {
                ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);
                if (viewPager != null) {
                    int current = viewPager.getCurrentItem();
                    if (current < 2) {
                        viewPager.setCurrentItem(current + 1);
                    } else if (current == 2) {
                        viewPager.setCurrentItem(3);
                    }
                }
            }
        });

        btnSkip.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HabitSettingActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        return view;
    }
}