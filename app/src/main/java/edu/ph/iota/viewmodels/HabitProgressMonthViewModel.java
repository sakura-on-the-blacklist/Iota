package edu.ph.iota.viewmodels;

public class HabitProgressMonthViewModel {

    private int year;
    private int month;
    private int[] habit_dates;

    public HabitProgressMonthViewModel(int month, int year, int[] habit_dates) {
        this.month = month;
        this.year = year;
        this.habit_dates = habit_dates;
    }
    public int getMonth() {
        return month;
    }
    public int getYear() {
        return year;
    }
    public int[] getHabit_dates() {
        return habit_dates;
    }
}
