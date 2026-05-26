package edu.ph.iota.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import java.util.Collections;
import edu.ph.iota.adapters.ReflectionsAdapter;
import edu.ph.iota.databinding.ActivityPrevReflectionsBinding;
import edu.ph.iota.viewmodels.ReflectionViewModel;

public class PrevReflectionsActivity extends AppCompatActivity {

    private ActivityPrevReflectionsBinding binding;
    private ReflectionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrevReflectionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ReflectionViewModel.class);

        setupCloseButton();
        setupPebbleClick();
        setupRecyclerView();
        setupObservers();

        // Load reflections from Firebase
        viewModel.loadAllReflections();
    }

    private void setupCloseButton() {
        binding.closeButton.setOnClickListener(v -> finish());
    }

    private void setupPebbleClick() {
        binding.pebbleView.setOnClickListener(v -> {
            // Optional: Navigate to add reflection screen
        });
    }

    private void setupRecyclerView() {
        ReflectionsAdapter adapter = new ReflectionsAdapter(Collections.emptyList());
        binding.recyclerViewReflections.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getReflections().observe(this, reflections -> {
            ReflectionsAdapter adapter = new ReflectionsAdapter(reflections);
            binding.recyclerViewReflections.setAdapter(adapter);
        });

        viewModel.isLoading().observe(this, isLoading -> {
            // Show/hide loading indicator if you have one
        });

        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.trim().isEmpty()) {
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
