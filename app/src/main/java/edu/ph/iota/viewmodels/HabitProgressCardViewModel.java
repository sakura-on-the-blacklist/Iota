package edu.ph.iota.viewmodels;

import java.util.ArrayList;
import java.util.List;

public class HabitProgressCardViewModel {

    public String habitTrackingStartDate;
    public String habitTrackingGoal;
    public String habitTrackingEndNumber;
    public List<HabitProgressMonthViewModel> listMonthModel = new ArrayList<>();

    public void setListMonthModel(List<HabitProgressMonthViewModel> listMonthModel) {
        this.listMonthModel = listMonthModel;
    }

    public HabitProgressCardViewModel(
            String habitTrackingStartDate,
            String habitTrackingGoal,
            String habitTrackingEndNumber,
            List<HabitProgressMonthViewModel> listMonthModel
    ) {
        this.habitTrackingStartDate = habitTrackingStartDate;
        this.habitTrackingGoal = habitTrackingGoal;
        this.habitTrackingEndNumber = habitTrackingEndNumber;
        this.listMonthModel = listMonthModel;
    }

    public String getHabitTrackingStartDate() {
        return habitTrackingStartDate;
    }

    public String getHabitTrackingGoal() {
        return habitTrackingGoal;
    }

    public String getHabitTrackingEndNumber() {
        return habitTrackingEndNumber;
    }

    public List<HabitProgressMonthViewModel> getListMonthModel() {
        return listMonthModel;
    }
}
