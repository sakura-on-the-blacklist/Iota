package edu.ph.iota;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Locale;

public class PomodoroSettingRow extends RelativeLayout {
    private TextView titleTv;
    private TextView valueTv;

    public PomodoroSettingRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.pomodoro_setting_row_internal, this, true);

        titleTv = findViewById(R.id.row_title);
        valueTv = findViewById(R.id.row_value);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PomodoroSettingRow, 0, 0);

        try {
            String title = a.getString(R.styleable.PomodoroSettingRow_pomodoro_settingTitle);
            String defaultValue = a.getString(R.styleable.PomodoroSettingRow_pomodoro_settingValue);

            titleTv.setText(title);

            String key = getResources().getResourceEntryName(getId());
            int savedValue = context.getSharedPreferences("PomodoroPrefs", Context.MODE_PRIVATE)
                    .getInt(key, defaultValue != null ? Integer.parseInt(defaultValue) : 0);

            setValue(savedValue);
        } catch (Exception e) {
            valueTv.setText(a.getString(R.styleable.PomodoroSettingRow_pomodoro_settingValue));
        } finally {
            a.recycle();
        }
    }

    public String getTitle() {
        return titleTv.getText().toString();
    }

    public String getValue() {
        return valueTv.getText().toString();
    }

    public void setValue(int val) {
        valueTv.setText(String.format(Locale.getDefault(), "%02d", val));
    }
}