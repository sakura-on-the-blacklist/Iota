package edu.ph.iota.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.ph.iota.activities.TEMPBackendRequestActivity_02;
import edu.ph.iota.models.HabitLog;

import com.google.firebase.firestore.FirebaseFirestore;
import edu.ph.iota.repositories.HabitLogRepository;
import edu.ph.iota.viewmodels.HabitProgressMonthViewModel;
import kotlin.Unit;

public class HabitToMonthFormatting {
    public List<HabitLog> HabitLogs;
    public List<YMD> YMDs = new ArrayList<>();
    public static class YMD{
        public int year;
        public int month;
        public ArrayList<Integer> dates = new ArrayList<>();

        public YMD(int year, int month){
            this.year = year;
            this.month = month;
        }
        public void add_date(int day) {
            dates.add(day);
        }
    }

    public void ListHabitLogToListYMD(){
        int i = 0;
        for (HabitLog log : this.HabitLogs) {
            String[] parts = log.getDate().split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            if (this.YMDs.isEmpty()){
                this.YMDs.add(new YMD(year,month));
            } else if (
                this.YMDs.get(i).year != year ||
                this.YMDs.get(i).month != month
            ) {
                this.YMDs.add(new YMD(year,month));
                i++;
            }
            this.YMDs.get(i).add_date(day);
        }
    }
}
