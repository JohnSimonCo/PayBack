package com.johnsimon.payback.ui;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.util.Alarm;
import com.johnsimon.payback.util.Resource;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class RemindLaterActivity extends DataActivity {

    public final static String KEY_DEBT_ID = "KEY_DEBT_ID";

    private Calendar remindLaterCalendar = Calendar.getInstance();
    private Debt debt;
    private boolean hasFinishedFirst = false;
    private boolean hasFinishedDate = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Resource.isLOrAbove()) {
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_launcher), getResources().getColor(R.color.primary_color)));
        }

    }

    @Override
    protected void onDataReceived() {
        super.onDataReceived();

        debt = data.findDebt(UUID.fromString(getIntent().getStringExtra(KEY_DEBT_ID)));

        final Calendar now = Calendar.getInstance();
        final Calendar future = Calendar.getInstance();

        String[] remindLaterOptions = getResources().getStringArray((R.array.remind_later_options));

        String day;

        switch (now.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                day = getString(R.string.monday);
                break;
            case Calendar.TUESDAY:
                day = getString(R.string.tuesday);
                break;
            case Calendar.WEDNESDAY:
                day = getString(R.string.wednesday);
                break;
            case Calendar.THURSDAY:
                day = getString(R.string.thursday);
                break;
            case Calendar.FRIDAY:
                day = getString(R.string.friday);
                break;
            case Calendar.SATURDAY:
                day = getString(R.string.saturday);
                break;
            case Calendar.SUNDAY:
                day = getString(R.string.sunday);
                break;
            default:
                day = getString(R.string.monday);
                break;
        }

        remindLaterOptions[2] = String.format(remindLaterOptions[2], day);

        remindLaterOptions[1] += (" (" + DateFormat.getTimeInstance(DateFormat.SHORT).format(future.getTime()) + ")");
        remindLaterOptions[2] += (" (" + DateFormat.getTimeInstance(DateFormat.SHORT).format(future.getTime()) + ")");

        future.setTimeInMillis(future.getTimeInMillis() + Resource.ONE_HOUR);

        remindLaterOptions[0] += (" (" + DateFormat.getTimeInstance(DateFormat.SHORT).format(future.getTime()) + ")");


        new MaterialDialog.Builder(this)
                .title(R.string.notif_remind_later)
                .items(remindLaterOptions)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (!hasFinishedFirst) {
                            finish();
                        }
                    }
                })
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

                                hasFinishedFirst = true;

                                datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        if (!hasFinishedDate) {
                                            finish();
                                        }
                                    }
                                });

                                break;

                        }
                    }
                })
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
                    android.text.format.DateFormat.is24HourFormat(RemindLaterActivity.this));

            hasFinishedDate = true;

            timePickerDialog.show();

            timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    finish();
                }
            });
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
