package edu.ph.iota.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

import edu.ph.iota.R;
import edu.ph.iota.databinding.FragmentHabitProgressMonthBinding;
import edu.ph.iota.viewmodels.HabitProgressMonthViewModel;

public class HabitProgressCardAdapter extends RecyclerView.Adapter<HabitProgressCardAdapter.ViewHolder> {

    private final List<HabitProgressMonthViewModel> monthList;

    public HabitProgressCardAdapter(List<HabitProgressMonthViewModel> monthList) {
        this.monthList = monthList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentHabitProgressMonthBinding binding =
            FragmentHabitProgressMonthBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
            );
        return new ViewHolder(binding);
    }
    @Override
    public int getItemCount() {
        return monthList.size();
    }

    // THIS DETERMINES WHAT IS PUT IN THE XML AND HOW
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HabitProgressMonthViewModel month = monthList.get(position);
        holder.setupMonth(month);
    }

    // THIS
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final FragmentHabitProgressMonthBinding binding;
        public ViewHolder(@NonNull FragmentHabitProgressMonthBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        public void setupMonth(HabitProgressMonthViewModel data) {

            // SETTING UP THE TITLE
            String[] months = {
                    "Jan","Feb","Mar","Apr","May","Jun",
                    "Jul","Aug","Sep","Oct","Nov","Dec"
            };
            int monthIndex = data.getMonth() - 1; // 1–12 -> 0–11 safe index
            if (monthIndex < 0 || monthIndex > 11) return;
            binding.monthTitle.setText(months[monthIndex]);

            // SETTING UP THE BOXIDS
            int[] boxes = {
                    R.id.week1day1, R.id.week1day2, R.id.week1day3, R.id.week1day4, R.id.week1day5, R.id.week1day6, R.id.week1day7,
                    R.id.week2day1, R.id.week2day2, R.id.week2day3, R.id.week2day4, R.id.week2day5, R.id.week2day6, R.id.week2day7,
                    R.id.week3day1, R.id.week3day2, R.id.week3day3, R.id.week3day4, R.id.week3day5, R.id.week3day6, R.id.week3day7,
                    R.id.week4day1, R.id.week4day2, R.id.week4day3, R.id.week4day4, R.id.week4day5, R.id.week4day6, R.id.week4day7,
                    R.id.week5day1, R.id.week5day2, R.id.week5day3, R.id.week5day4, R.id.week5day5, R.id.week5day6, R.id.week5day7,
                    R.id.week6day1, R.id.week6day2, R.id.week6day3, R.id.week6day4, R.id.week6day5, R.id.week6day6, R.id.week6day7
            };

            // GETTING THE NECESSARY DATE DATA
            Calendar cal = Calendar.getInstance();
            cal.set(data.getYear(), monthIndex, 1);
            int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sun ... 7=Sat
            int startDay = (firstDayOfWeek == Calendar.SUNDAY) ? 7 : firstDayOfWeek - 1;
            int totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            // SETS ALL DATES TO TRANSPARENT
            for (int boxId : boxes) {
                CardView card = binding.getRoot().findViewById(boxId);
                card.setBackgroundTintList(
                        ColorStateList.valueOf(
                                ContextCompat.getColor(binding.getRoot().getContext(), R.color.month_n)
                        )
                );
            }

            // SETS ALL VALID DAYS TO VISIBLE
            for (int i = startDay - 1; i < startDay - 1 + totalDays; i++) {
                if (i >= 0 && i < boxes.length) {
                    CardView card = binding.getRoot().findViewById(boxes[i]);
                    card.setBackgroundTintList(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(binding.getRoot().getContext(), R.color.habit_n)
                        )
                    );
                }
            }

            // SETS ALL THE HABIT DAYS TO ORANGE
            for (int habitDate : data.getHabit_dates()) {
                if (habitDate > 0 && habitDate <= totalDays) {
                    int index = habitDate + startDay - 2;
                    CardView card = binding.getRoot().findViewById(boxes[index]);
                    card.setBackgroundTintList(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(binding.getRoot().getContext(), R.color.habit_y)
                        )
                    );
                }
            }
        }
    }
}