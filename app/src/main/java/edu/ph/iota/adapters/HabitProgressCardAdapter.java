package edu.ph.iota.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.ph.iota.databinding.FragmentHabitProgressCardBinding;
import edu.ph.iota.viewmodels.HabitProgressCardViewModel;

public class HabitProgressCardAdapter extends RecyclerView.Adapter<HabitProgressCardAdapter.ViewHolder> {

    private final List<HabitProgressCardViewModel> cardList;

    public HabitProgressCardAdapter(List<HabitProgressCardViewModel> cardList) {
        this.cardList = cardList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentHabitProgressCardBinding binding =
                FragmentHabitProgressCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
            );
        return new ViewHolder(binding);
    }
    @Override
    public int getItemCount() {
        return cardList.size();
    }

    // THIS DETERMINES WHAT IS PUT IN THE XML AND HOW
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HabitProgressCardViewModel card = cardList.get(position);
        holder.setupCard(card);
    }

    // THIS
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final FragmentHabitProgressCardBinding binding;
        public ViewHolder(@NonNull FragmentHabitProgressCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        public void setupCard(HabitProgressCardViewModel data) {

            binding.habitTrackingStartDate.setText(data.getHabitTrackingStartDate());
            binding.habitTrackingGoal.setText(data.getHabitTrackingGoal());
            binding.habitTrackingEndNumber.setText(data.getHabitTrackingEndNumber());

            binding.habitProgressMonths.setLayoutManager(
                new LinearLayoutManager(
                    binding.getRoot().getContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            );
            HabitProgressMonthAdapter monthAdapter = new HabitProgressMonthAdapter(data.getListMonthModel());
            binding.habitProgressMonths.setAdapter(monthAdapter);
        }
    }
}