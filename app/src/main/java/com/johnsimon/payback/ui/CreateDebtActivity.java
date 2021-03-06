package com.johnsimon.payback.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListPopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TimePicker;

import com.johnsimon.payback.R;
import com.johnsimon.payback.adapter.CreateSpinnerAdapter;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.util.Alarm;
import com.johnsimon.payback.util.ColorPalette;
import com.johnsimon.payback.util.RequiredValidator;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.ValidatorListener;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class CreateDebtActivity extends DataActivity {

	private static String ARG_PREFIX = Resource.prefix("CREATE_DEBT");

	public final static String ARG_FROM_FEED = Resource.arg(ARG_PREFIX, "FROM_FEED");
    public final static String ARG_ANIMATE_TOOLBAR = Resource.arg(ARG_PREFIX, "ANIMATE_TOOLBAR");
	public final static String ARG_FROM_PERSON_NAME = Resource.arg(ARG_PREFIX, "FROM_PERSON_NAME");
	public final static String ARG_ID = Resource.arg(ARG_PREFIX, "AMOUNT");

    public final static String KEY_CALENDAR = "KEY_CALENDAR";
    public final static String KEY_ADDED_CALENDAR = "KEY_ADDED_CALENDAR";
    public final static String KEY_CHANGED_ADDED = "KEY_CHANGED_ADDED";
    public final static String KEY_NO_FAB_ANIM = "KEY_NO_FAB_ANIM";

	private AppCompatEditText floatLabelAmountEditText;
	private AppCompatEditText floatLabelNoteEditText;
	private AppCompatAutoCompleteTextView floatLabelNameAutoCompleteTextView;
	private Button reminderButton;
    private Button reminderDayButton;
    private Button reminderTimeButton;
    private View reminderDivider;
	private ImageButton clearReminderButton;
	private RadioGroup radioGroup;
    private ScrollView mainScrollView;
    private TextInputLayout float_label_layout_amount;
    private RelativeLayout create_master;
    private FloatingActionButton create_fab;

	private RequiredValidator validator;
	private Debt editingDebt = null;

    private Calendar reminderCalendar = Calendar.getInstance();
	private boolean usingCustomDate = false;

    private Calendar addedCalendar = Calendar.getInstance();
    private boolean changedAddedDate = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_debt);

        if (Resource.isLOrAbove()) {
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_launcher), getResources().getColor(R.color.primary_color)));

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.primary_color));
        } else {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.primary_color_darker));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.create_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

		floatLabelAmountEditText = (AppCompatEditText) findViewById(R.id.create_edittext_amount);
		floatLabelNoteEditText = (AppCompatEditText) findViewById(R.id.create_edittext_note);
		floatLabelNameAutoCompleteTextView = (AppCompatAutoCompleteTextView) findViewById(R.id.create_edittext_name);
        float_label_layout_amount = (TextInputLayout) findViewById(R.id.float_label_layout_amount);
        create_master = (RelativeLayout) findViewById(R.id.create_master);

		floatLabelNoteEditText.setTextColor(getResources().getColor(R.color.gray_text_normal));


		radioGroup = (RadioGroup) findViewById(R.id.create_radio);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getBoolean(ARG_ANIMATE_TOOLBAR, true)) {
                animateIn(toolbar, true);
                animateIn(findViewById(R.id.create_lower_master), false);
            }
        }

		Resources res = getResources();

		floatLabelNameAutoCompleteTextView.setPadding(
			Resource.getPx(8, res),
			Resource.getPx(8, res),
			Resource.getPx(42, res),
			Resource.getPx(8, res)
		);

		final ImageButton clearEditText = (ImageButton) findViewById(R.id.create_clear);
		clearEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatLabelNameAutoCompleteTextView.setText("");
                floatLabelNameAutoCompleteTextView.requestFocus();
            }
        });

		if (TextUtils.isEmpty(floatLabelNameAutoCompleteTextView.getText().toString())) {
			clearEditText.setVisibility(View.GONE);
		}

        mainScrollView = (ScrollView) findViewById(R.id.create_scroll_view);

        floatLabelNoteEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mainScrollView.smoothScrollTo(0, mainScrollView.getBottom());
                        }
                    }, 200);
                }
            }
        });

        floatLabelNameAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    clearEditText.setVisibility(View.GONE);
                } else {
                    clearEditText.setVisibility(View.VISIBLE);
                }
            }
        });

		final ArrayList<CreateSpinnerAdapter.CalendarOptionItem> dayList = new ArrayList<CreateSpinnerAdapter.CalendarOptionItem>() {{
			add(new CreateSpinnerAdapter.CalendarOptionItem(getString(R.string.today), null, CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_TODAY, null));
			add(new CreateSpinnerAdapter.CalendarOptionItem(getString(R.string.tomorrow), null, CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_TOMORROW, null));
			add(new CreateSpinnerAdapter.CalendarOptionItem(getString(R.string.pick_date), null, CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_CUSTOM, null));
		}};

        final Calendar morning = Calendar.getInstance();
        final Calendar afternoon = Calendar.getInstance();
        final Calendar evening = Calendar.getInstance();
        final Calendar night = Calendar.getInstance();

        morning.set(Calendar.HOUR_OF_DAY, 9);
        afternoon.set(Calendar.HOUR_OF_DAY, 13);
        evening.set(Calendar.HOUR_OF_DAY, 17);
        night.set(Calendar.HOUR_OF_DAY, 20);

        morning.set(Calendar.MINUTE, 0);
        afternoon.set(Calendar.MINUTE, 0);
        evening.set(Calendar.MINUTE, 0);
        night.set(Calendar.MINUTE, 0);

		final ArrayList<CreateSpinnerAdapter.CalendarOptionItem> timeList = new ArrayList<CreateSpinnerAdapter.CalendarOptionItem>() {{
			add(new CreateSpinnerAdapter.CalendarOptionItem(
                    getString(R.string.morning),
                    DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(morning.getTime()),
                    CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_MORNING,
                    null));
			add(new CreateSpinnerAdapter.CalendarOptionItem(getString(R.string.afternoon),
                    DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(afternoon.getTime()),
                    CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_AFTERNOON,
                    null));
			add(new CreateSpinnerAdapter.CalendarOptionItem(getString(R.string.evening),
                    DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(evening.getTime()),
                    CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_EVENING,
                    null));
			add(new CreateSpinnerAdapter.CalendarOptionItem(getString(R.string.night),
                    DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(night.getTime()),
                    CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_NIGHT,
                    null));
			add(new CreateSpinnerAdapter.CalendarOptionItem(getString(R.string.pick_time),
                    null,
                    CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_CUSTOM,
                    null));
		}};

        CreateSpinnerAdapter dayAdapter = new CreateSpinnerAdapter(getApplicationContext(), R.layout.create_spinner_item, dayList, false);
		CreateSpinnerAdapter timeAdapter = new CreateSpinnerAdapter(getApplicationContext(), R.layout.create_spinner_item, timeList, true);

        reminderDayButton = (Button) findViewById(R.id.create_button_day);
        reminderTimeButton = (Button) findViewById(R.id.create_button_time);
        reminderDivider = findViewById(R.id.create_button_divider);

        final ListPopupWindow popupWindowDay = new ListPopupWindow(this);
        final ListPopupWindow popupWindowTime = new ListPopupWindow(this);

        popupWindowDay.setContentWidth(Resource.getPx(140, getResources()));
        popupWindowTime.setContentWidth(Resource.getPx(170, getResources()));

        popupWindowDay.setAnchorView(reminderDayButton);
        popupWindowTime.setAnchorView(reminderTimeButton);

        popupWindowDay.setAdapter(dayAdapter);
        popupWindowTime.setAdapter(timeAdapter);

        reminderDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowDay.show();
            }
        });

        reminderTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowTime.show();
            }
        });

        popupWindowDay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (dayList.get(position).calendarFlag) {
                    case CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_TODAY:
                        Calendar nowToday = Calendar.getInstance();
                        nowToday.setTimeInMillis(nowToday.getTimeInMillis());

                        reminderCalendar.set(Calendar.YEAR, nowToday.get(Calendar.YEAR));
                        reminderCalendar.set(Calendar.MONTH, nowToday.get(Calendar.MONTH));
                        reminderCalendar.set(Calendar.DAY_OF_MONTH, nowToday.get(Calendar.DAY_OF_MONTH));

                        updateDate(false);
                        break;

                    case CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_TOMORROW:
                        Calendar nowTomorrow = Calendar.getInstance();
                        nowTomorrow.setTimeInMillis(nowTomorrow.getTimeInMillis() + Resource.ONE_DAY);

                        reminderCalendar.set(Calendar.YEAR, nowTomorrow.get(Calendar.YEAR));
                        reminderCalendar.set(Calendar.MONTH, nowTomorrow.get(Calendar.MONTH));
                        reminderCalendar.set(Calendar.DAY_OF_MONTH, nowTomorrow.get(Calendar.DAY_OF_MONTH));
                        updateDate(false);
                        break;

                    case CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_CUSTOM:
                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                CreateDebtActivity.this,
                                dateSetCallback,
                                reminderCalendar.get(Calendar.YEAR),
                                reminderCalendar.get(Calendar.MONTH),
                                reminderCalendar.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.show();
                        break;
                }

                popupWindowDay.dismiss();

            }
        });

        popupWindowTime.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (timeList.get(position).calendarFlag) {
                    case CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_MORNING:
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, 9);
                        reminderCalendar.set(Calendar.MINUTE, 0);
                        updateDate(false);
                        break;

                    case CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_AFTERNOON:
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, 13);
                        reminderCalendar.set(Calendar.MINUTE, 0);
                        updateDate(false);
                        break;

                    case CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_EVENING:
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, 17);
                        reminderCalendar.set(Calendar.MINUTE, 0);
                        updateDate(false);
                        break;

                    case CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_NIGHT:
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, 20);
                        reminderCalendar.set(Calendar.MINUTE, 0);
                        updateDate(false);
                        break;

                    case CreateSpinnerAdapter.CalendarOptionItem.FLAG_CALENDAR_CUSTOM:
                        TimePickerDialog timePickerDialog = new TimePickerDialog(CreateDebtActivity.this, timeSetCallback, reminderCalendar.get(Calendar.HOUR_OF_DAY), reminderCalendar.get(Calendar.MINUTE), android.text.format.DateFormat.is24HourFormat(CreateDebtActivity.this));
                        timePickerDialog.show();
                        break;
                }

                popupWindowTime.dismiss();

            }
        });

		if (savedInstanceState == null) {
			popupWindowDay.setSelection(1);
			popupWindowTime.setSelection(0);
		}

		clearReminderButton = (ImageButton) findViewById(R.id.create_clear_reminder);
		clearReminderButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   usingCustomDate = false;
                   updateDate(true);
               }
            });

        reminderButton = (Button) findViewById(R.id.create_reminder_button);
        reminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usingCustomDate = true;
                updateDate(true);
            }
        });

        create_fab = (FloatingActionButton) findViewById(R.id.create_fab);

        if (Resource.isLOrAbove() && !getIntent().getBooleanExtra(KEY_NO_FAB_ANIM, false)) {
            create_fab.setTransitionName("fab");
            animateInFab();
        }

        create_fab.setOnClickListener(fabClickListener);

        validator = new RequiredValidator(new EditText[] {
                floatLabelNameAutoCompleteTextView,
                floatLabelAmountEditText
        }, new ValidatorListener() {
            @Override
            public void onValid() {
                if (floatLabelAmountEditText.getText().toString().equals("0")) return;
                create_fab.setActivated(true);
                create_fab.setAlpha(1f);
            }

            @Override
            public void onInvalid() {
                create_fab.setActivated(false);
                create_fab.setAlpha(0.6f);
            }
        });

		updateDate(false);

    }

	@Override
	protected void onDataReceived() {
		Intent intent = getIntent();

		String currencyText = getResources().getString(R.string.amount) + " (" + data.preferences.getCurrency().getDisplayName() + ")";
        float_label_layout_amount.setHint(currencyText);

		if (intent.hasExtra(ARG_ID)) {
			editingDebt = data.findDebt((UUID) intent.getSerializableExtra(ARG_ID));
            if (editingDebt.isPaidBack()) {
                findViewById(R.id.reminder_layout).setVisibility(View.GONE);
            }

            usingCustomDate = editingDebt.hasReminder();
            if (usingCustomDate) {
                reminderCalendar.setTimeInMillis(editingDebt.getRemindDate());
                updateDate(false);
            }

			floatLabelNameAutoCompleteTextView.setText(editingDebt.getOwner().getName());

            floatLabelAmountEditText.setText(new DecimalFormat("###.###").format(editingDebt.getAbsoluteAmount()));

			floatLabelNoteEditText.setText(editingDebt.getNote());
			//Assume that the user wants to change the note
			floatLabelNoteEditText.setSelection(floatLabelNoteEditText.getText().length());
			floatLabelNoteEditText.requestFocus();

			boolean iOwe = editingDebt.getAmount() < 0;
			radioGroup.check(iOwe ? R.id.create_radio_i_owe : R.id.create_radio_they_owe);
		} else if(intent.hasExtra(ARG_FROM_PERSON_NAME)) {
			floatLabelNameAutoCompleteTextView.setText(intent.getStringExtra(ARG_FROM_PERSON_NAME));
			radioGroup.requestFocus();
		}
	}

    private void updateDate(boolean anim) {
		if (usingCustomDate) {

            Calendar now = Calendar.getInstance();

            int year = reminderCalendar.get(Calendar.YEAR);
            int month = reminderCalendar.get(Calendar.MONTH);
            int day = reminderCalendar.get(Calendar.DAY_OF_MONTH);

            reminderDayButton.setText(getDayString(year, month, day, now));
            reminderTimeButton.setText(getTimeString(reminderCalendar));

            if (anim) {

                reminderButton.setVisibility(View.VISIBLE);

                Animation fadeOut = AnimationUtils.loadAnimation(CreateDebtActivity.this, R.anim.fade_out_fast);

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        reminderButton.setVisibility(View.GONE);

                        Animation fadeIn = AnimationUtils.loadAnimation(CreateDebtActivity.this, R.anim.fade_in_fast);

                        clearReminderButton.setVisibility(View.VISIBLE);
                        reminderDayButton.setVisibility(View.VISIBLE);
                        reminderTimeButton.setVisibility(View.VISIBLE);
                        reminderDivider.setVisibility(View.VISIBLE);

                        clearReminderButton.startAnimation(fadeIn);
                        reminderDayButton.startAnimation(fadeIn);
                        reminderTimeButton.startAnimation(fadeIn);
                        reminderDivider.startAnimation(fadeIn);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                reminderButton.startAnimation(fadeOut);

            } else {
                reminderButton.setVisibility(View.GONE);
                clearReminderButton.setVisibility(View.VISIBLE);
                reminderDayButton.setVisibility(View.VISIBLE);
                reminderTimeButton.setVisibility(View.VISIBLE);
                reminderDivider.setVisibility(View.VISIBLE);
            }
		} else {

            if (anim) {

                clearReminderButton.setVisibility(View.VISIBLE);
                reminderDayButton.setVisibility(View.VISIBLE);
                reminderTimeButton.setVisibility(View.VISIBLE);
                reminderDivider.setVisibility(View.VISIBLE);

                Animation fadeOut = AnimationUtils.loadAnimation(CreateDebtActivity.this, R.anim.fade_out_fast);

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        clearReminderButton.setVisibility(View.GONE);
                        reminderDayButton.setVisibility(View.GONE);
                        reminderTimeButton.setVisibility(View.GONE);
                        reminderDivider.setVisibility(View.GONE);

                        Animation fadeIn = AnimationUtils.loadAnimation(CreateDebtActivity.this, R.anim.fade_in_fast);

                        reminderButton.setVisibility(View.VISIBLE);
                        reminderButton.startAnimation(fadeIn);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                clearReminderButton.startAnimation(fadeOut);
                reminderDayButton.startAnimation(fadeOut);
                reminderTimeButton.startAnimation(fadeOut);
                reminderDivider.startAnimation(fadeOut);

            } else {
                reminderButton.setVisibility(View.VISIBLE);
                clearReminderButton.setVisibility(View.GONE);
                reminderDayButton.setVisibility(View.GONE);
                reminderTimeButton.setVisibility(View.GONE);
                reminderDivider.setVisibility(View.GONE);
            }

		}
	}

    private String getTimeString(Calendar reminderCalendar) {

        if (reminderCalendar.get(Calendar.MINUTE) == 0) {
            switch (reminderCalendar.get(Calendar.HOUR_OF_DAY)) {
                case 9:
                    return getString(R.string.morning);
                case 13:
                    return getString(R.string.afternoon);
                case 17:
                    return getString(R.string.evening);
                case 20:
                    return getString(R.string.night);
            }
        }

        return android.text.format.DateFormat.getTimeFormat(this).format(reminderCalendar.getTime());
    }

    private String getDayString(int year, int month, int day, Calendar now) {

        int yearNow = now.get(Calendar.YEAR);
        int monthNow = now.get(Calendar.MONTH);
        int dayNow = now.get(Calendar.DAY_OF_MONTH);

        if (year == yearNow && month == monthNow && day == dayNow) {
            return getString(R.string.today);
        }

        now.setTimeInMillis(now.getTimeInMillis() + Resource.ONE_DAY);

        yearNow = now.get(Calendar.YEAR);
        monthNow = now.get(Calendar.MONTH);
        dayNow = now.get(Calendar.DAY_OF_MONTH);

        if (year == yearNow && month == monthNow && day == dayNow) {
            return getString(R.string.tomorrow);
        }

        SimpleDateFormat simpleDateFormat = Resource.monthDateFormat;

        Calendar target = Calendar.getInstance();
        target.set(Calendar.YEAR, year);
        target.set(Calendar.MONTH, month);
        target.set(Calendar.DAY_OF_MONTH, day);

        return simpleDateFormat.format(target.getTime());
    }

    @Override
    protected void onDataLinked() {
        floatLabelNameAutoCompleteTextView.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.autocomplete_list_item,
                R.id.autocomplete_list_item_title,
                data.getAllNames()
        ));
    }

    private View.OnClickListener fabClickListener = new View.OnClickListener() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {

            if (v.isActivated()) {

                float amount;

                try {
                    amount = Float.parseFloat(floatLabelAmountEditText.getText().toString().replace(',', '.'));
                } catch (Exception e) {
                    //Weird formatting
                    Snackbar.make(create_master, R.string.number_format_error, Snackbar.LENGTH_LONG).show();
                    return;
                }

                Debt debt = saveDebt(
                        floatLabelNameAutoCompleteTextView.getText().toString().trim(),
                        radioGroup.getCheckedRadioButtonId() == R.id.create_radio_i_owe,
                        amount,
                        floatLabelNoteEditText.getText().toString().trim());

                if (usingCustomDate) {
                    debt.setRemindDate(reminderCalendar.getTimeInMillis());
                    Alarm.addAlarm(CreateDebtActivity.this, debt);
                }

                finishAffinity();
                final Intent intent = new Intent(getApplicationContext(), FeedActivity.class)
						.putExtra(FeedActivity.ARG_FROM_CREATE, true);

                FeedActivity.person = debt.getOwner();

                if (Resource.isLOrAbove()) {
                    startActivity(intent);
                } else {
                    startActivity(intent, ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.activity_out_reverse, R.anim.activity_in_reverse).toBundle());
                }
            } else {
                Snackbar.make(create_master, R.string.create_fab_error, Snackbar.LENGTH_SHORT).show();

                Resource.hideKeyboard(CreateDebtActivity.this);
            }
        }
    };

	private TimePickerDialog.OnTimeSetListener timeSetCallback = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			reminderCalendar.set(Calendar.MINUTE, minute);
			usingCustomDate = true;

			updateDate(false);
		}
	};

	private DatePickerDialog.OnDateSetListener dateSetCallback = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			reminderCalendar.set(year, monthOfYear, dayOfMonth);
			usingCustomDate = true;
			updateDate(false);
		}
	};

    private DatePickerDialog.OnDateSetListener dateAddedSetCallback = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, monthOfYear);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            addedCalendar = cal;
            changedAddedDate = true;
        }
    };

	public Debt saveDebt(String name, boolean iOwe, float amount, String note) {
		if(iOwe) {
			amount = -amount;
		}
		if (TextUtils.isEmpty(note)) {
			note = null;
		}

		Debt debt;
		if(editingDebt == null) {
			Person person = data.getOrCreatePerson(name, ColorPalette.getInstance(this));
			data.addFirst(debt = new Debt(person, amount, note, data.preferences.getCurrency().id));

            debt.changeDate(addedCalendar.getTimeInMillis());
		} else {
			Person person = editingDebt.getOwner().getName().equals(name)
				? editingDebt.getOwner()
				: data.getOrCreatePerson(name, ColorPalette.getInstance(this));

            if (changedAddedDate) {
                editingDebt.changeDate(addedCalendar.getTimeInMillis());
            }

			editingDebt.edit(person, amount, note);

            debt = editingDebt;
		}

        if (usingCustomDate) {
            debt.setRemindDate(reminderCalendar.getTimeInMillis());
        } else {
            debt.setRemindDate(null);
        }

		storage.commit(this);

		return debt;
	}

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateIn(final View view, boolean fromTop) {
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        view.setAlpha(0f);
        if (fromTop) {
            view.setTranslationY(Resource.getPx(-100, getResources()));
        } else {
            view.setTranslationY(Resource.getPx(80, getResources()));
        }

        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY", 0);
        animY.setStartDelay(260);
        animY.setDuration(450);

        ObjectAnimator animAlpha = ObjectAnimator.ofFloat(view, "alpha", 1f);
        animAlpha.setStartDelay(260);
        animAlpha.setDuration(450);

        if (Resource.isLOrAbove()) {
            PathInterpolator pathInterpolator = new PathInterpolator(0.1f, 0.4f, 0.5f, 1f);

            animY.setInterpolator(pathInterpolator);
            animAlpha.setInterpolator(pathInterpolator);
        } else {
            animY.setInterpolator(new DecelerateInterpolator());
            animAlpha.setInterpolator(new DecelerateInterpolator());
        }

        animY.start();
        animAlpha.start();

        animAlpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

	@Override
	public void onResume() {
		super.onResume();
		validator.validate();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_debt, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_create_date) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(CreateDebtActivity.this, dateAddedSetCallback, addedCalendar.get(Calendar.YEAR), addedCalendar.get(Calendar.MONTH), addedCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();

        } else if (id == android.R.id.home) {
            if (getIntent().getBooleanExtra(ARG_FROM_FEED, false)) {
                if (Resource.isLOrAbove()) {
                    animateOutFab();
                    create_fab.animate()
                            .alpha(1f)
                            .setDuration(600)
                            .start();
					create_fab.setImageResource(R.drawable.ic_action_content_new);

                    finishAfterTransition();
                } else {
                    finish();
                    overridePendingTransition(R.anim.activity_out_reverse, R.anim.activity_in_reverse);
                }
            } else {
                if (Resource.isLOrAbove()) {
                    startActivity(new Intent(getApplicationContext(), FeedActivity.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), FeedActivity.class), ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.activity_out_reverse, R.anim.activity_in_reverse).toBundle());
                }

                finishAffinity();

            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
	public void onBackPressed() {
        if (Resource.isLOrAbove()) {
            animateOutFab();
			create_fab.animate()
					.alpha(1f)
					.setDuration(600)
					.start();
			create_fab.setImageResource(R.drawable.ic_action_content_new);
            finishAfterTransition();
        } else {
            finish();
            overridePendingTransition(R.anim.activity_out_reverse, R.anim.activity_in_reverse);
        }
	}

	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		Log.i("my_app", "New intent with flags " + intent.getFlags());
	}

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void animateInFab() {
        if (Resource.isLOrAbove()) {
            final int colorWhite = getResources().getColor(android.R.color.white);
            final int colorOrange = getResources().getColor(R.color.accent_color);

            ViewPropertyAnimator vpa = create_fab.animate();
            vpa.setDuration(200);
            vpa.setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int color = Resource.mixTwoColors(colorWhite, colorOrange, valueAnimator.getAnimatedFraction());
                    create_fab.setBackgroundTintList(ColorStateList.valueOf(color));
                }
            });

            vpa.start();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void animateOutFab() {
        if (Resource.isLOrAbove()) {
            final int colorWhite = getResources().getColor(android.R.color.white);
            final int colorOrange = getResources().getColor(R.color.accent_color);

            ViewPropertyAnimator vpa = create_fab.animate();
            vpa.setDuration(600);
            vpa.setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int color = Resource.mixTwoColors(colorOrange, colorWhite, valueAnimator.getAnimatedFraction());
                    create_fab.setBackgroundTintList(ColorStateList.valueOf(color));
                }
            });

            vpa.start();
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        long time = savedInstanceState.getLong(KEY_CALENDAR, 0);
        long timeAdded = savedInstanceState.getLong(KEY_ADDED_CALENDAR, 0);

        if (time != 0) {
			reminderCalendar.setTimeInMillis(time);
			usingCustomDate = true;
			updateDate(false);
		}

        if (timeAdded > 0) {
            addedCalendar.setTimeInMillis(timeAdded);
        }

        changedAddedDate = savedInstanceState.getBoolean(KEY_CHANGED_ADDED, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
		if (usingCustomDate) {
			outState.putLong(KEY_CALENDAR, reminderCalendar.getTimeInMillis());
		}

        outState.putLong(KEY_ADDED_CALENDAR, addedCalendar.getTimeInMillis());
        outState.putBoolean(KEY_CHANGED_ADDED, changedAddedDate);
    }
}