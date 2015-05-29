package com.johnsimon.payback.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.util.Alarm;
import com.johnsimon.payback.util.Resource;

import java.util.Calendar;
import java.util.UUID;

public class RemindLaterActivity extends DataActivity {

    public final static String KEY_DEBT_ID = "KEY_DEBT_ID";

    private Calendar remindLaterCalendar = Calendar.getInstance();
    private Debt debt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDataReceived() {
        super.onDataReceived();

        debt = data.findDebt(UUID.fromString(getIntent().getStringExtra(KEY_DEBT_ID)));

        final Calendar now = Calendar.getInstance();

        String[] remindLaterOptions = getResources().getStringArray((R.array.remind_later_options));
        String[] weekdays = getResources().getStringArray((R.array.weekdays));
        remindLaterOptions[2] = String.format(remindLaterOptions[2], weekdays[now.get(Calendar.DAY_OF_WEEK)]);

        new MaterialDialog.Builder(this)
                .title(R.string.select_currency)
                .items(remindLaterOptions)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {
                        switch (which) {
                            case 0:
                                //In an hour
                                debt.setRemindDate(System.currentTimeMillis() + Resource.ONE_HOUR);
                                Alarm.addAlarm(RemindLaterActivity.this, debt);
                                break;
                            case 1:
                                //Tomorrow
                                debt.setRemindDate(System.currentTimeMillis() + Resource.ONE_DAY);
                                Alarm.addAlarm(RemindLaterActivity.this, debt);
                                break;

                            case 2:
                                //Next XXX
                                debt.setRemindDate(System.currentTimeMillis() + Resource.ONE_WEEK);
                                Alarm.addAlarm(RemindLaterActivity.this, debt);
                                break;

                            case 3:
                                //Custom
                                now.setTimeInMillis(now.getTimeInMillis() + Resource.ONE_DAY);
                                DatePickerDialog datePickerDialog = new DatePickerDialog(
                                        RemindLaterActivity.this,
                                        dateSetListener,
                                        now.get(Calendar.YEAR),
                                        now.get(Calendar.MONTH),
                                        now.get(Calendar.DAY_OF_MONTH));

                                datePickerDialog.show();

                                break;

                        }
                    }
                })
                .positiveText(R.string.select)
                .show();
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            remindLaterCalendar.set(Calendar.YEAR, year);
            remindLaterCalendar.set(Calendar.MONTH, month);
            remindLaterCalendar.set(Calendar.DAY_OF_MONTH, day);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    RemindLaterActivity.this,
                    timeSetListener,
                    remindLaterCalendar.get(Calendar.HOUR_OF_DAY),
                    remindLaterCalendar.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(RemindLaterActivity.this));

            timePickerDialog.show();
        }
    };

    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            remindLaterCalendar.set(Calendar.HOUR_OF_DAY, hour);
            remindLaterCalendar.set(Calendar.MINUTE, minute);

            debt.setRemindDate(remindLaterCalendar.getTimeInMillis());
            Alarm.addAlarm(RemindLaterActivity.this, debt);
        }
    };


}
