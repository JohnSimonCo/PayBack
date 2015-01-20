package com.johnsimon.payback.ui;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.internal.widget.TintEditText;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.util.RequiredValidator;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.ValidatorListener;
import com.johnsimon.payback.view.FloatLabelLayout;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.shamanland.fab.FloatingActionButton;
import com.williammora.snackbar.Snackbar;

import java.util.UUID;

public class CreateDebtActivity extends DataActivity {

	private static String ARG_PREFIX = Resource.prefix("CREATE_DEBT");

	public static String ARG_FROM_FEED = Resource.arg(ARG_PREFIX, "FROM_FEED");
	public static String ARG_FROM_PERSON_NAME = Resource.arg(ARG_PREFIX, "FROM_PERSON_NAME");
	public static String ARG_ID = Resource.arg(ARG_PREFIX, "AMOUNT");

	//Views
	private TintEditText floatLabelAmountEditText;
	private TintEditText floatLabelNoteEditText;
	private AutoCompleteTextView floatLabelNameAutoCompleteTextView;

	private RadioGroup radioGroup;

	private RequiredValidator validator;

	private Debt editingDebt = null;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.activity_create_debt);

        Toolbar toolbar = (Toolbar) findViewById(R.id.create_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		floatLabelAmountEditText = (TintEditText) findViewById(R.id.create_edittext_amount);
		floatLabelNoteEditText = (TintEditText) findViewById(R.id.create_edittext_note);
		floatLabelNameAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.create_edittext_name);

		floatLabelNoteEditText.setTextColor(getResources().getColor(R.color.gray_text_normal));

        FloatLabelLayout floatLabelLayout = (FloatLabelLayout) findViewById(R.id.float_label_layout_amount);
        floatLabelLayout.setHint(getResources().getString(R.string.amount) + " (" + data.preferences.getCurrency() + ")");
		floatLabelAmountEditText.setHint(getResources().getString(R.string.amount) + " (" + data.preferences.getCurrency() + ")");
		floatLabelAmountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

		radioGroup = (RadioGroup) findViewById(R.id.create_radio);

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

        final ScrollView mainScrollView = (ScrollView) findViewById(R.id.create_scroll_view);

        floatLabelNoteEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, boolean hasFocus) {

                if (hasFocus) {
                    new Handler().postDelayed(new Runnable() {
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

        if (Resource.isLOrAbove()) {
            final ImageButton create_fab = (ImageButton) findViewById(R.id.create_fab_l);

            create_fab.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, create_fab.getWidth(), create_fab.getHeight());
                }
            });

            create_fab.setClipToOutline(true);

            validator = new RequiredValidator(new EditText[] {
                    floatLabelNameAutoCompleteTextView,
                    floatLabelAmountEditText
            }, new ValidatorListener() {
                @Override
                public void onValid() {
                    if (floatLabelAmountEditText.getText().equals("0")) return;
                    create_fab.setActivated(true);
                    create_fab.setAlpha(1f);
                }

                @Override
                public void onInvalid() {
                    create_fab.setActivated(false);
                    create_fab.setAlpha(0.6f);
                }
            });

            create_fab.setOnClickListener(fabClickListener);
        } else {
            final FloatingActionButton create_fab = (FloatingActionButton) findViewById(R.id.create_fab);

            validator = new RequiredValidator(new EditText[] {
                    floatLabelNameAutoCompleteTextView,
                    floatLabelAmountEditText
            }, new ValidatorListener() {
                @Override
                public void onValid() {
                    if (floatLabelAmountEditText.getText().equals("0")) return;
                    create_fab.setActivated(true);
                    create_fab.setAlpha(1f);
                }

                @Override
                public void onInvalid() {
                    create_fab.setActivated(false);
                    create_fab.setAlpha(0.6f);
                }
            });

            create_fab.setOnClickListener(fabClickListener);
        }

    }

	@Override
	protected void onDataReceived() {
		Intent intent = getIntent();
		if (intent.hasExtra(ARG_ID)) {
			editingDebt = data.findDebt((UUID) intent.getSerializableExtra(ARG_ID));

			floatLabelNameAutoCompleteTextView.setText(editingDebt.getOwner().getName());

			floatLabelAmountEditText.setText(Float.toString(Math.abs(editingDebt.getAmount())).replaceAll("\\.0*$", ""));

			floatLabelNoteEditText.setText(editingDebt.getNote());
			//Assume that the user wants to change the note
			floatLabelNoteEditText.setSelection(floatLabelNoteEditText.getText().length());
			floatLabelNoteEditText.requestFocus();

			boolean iOwe = editingDebt.getAmount() < 0;
			radioGroup.check(iOwe ? R.id.create_radio_i_owe : R.id.create_radio_they_owe);
		} else if(intent.hasExtra(ARG_FROM_PERSON_NAME)) {
			floatLabelNameAutoCompleteTextView.setText(intent.getStringExtra(ARG_FROM_PERSON_NAME));
			floatLabelAmountEditText.requestFocus();
		}
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
                Person person = saveDebt(
                        floatLabelNameAutoCompleteTextView.getText().toString().trim(),
                        radioGroup.getCheckedRadioButtonId() == R.id.create_radio_i_owe,
                        Float.parseFloat(floatLabelAmountEditText.getText().toString()),
                        floatLabelNoteEditText.getText().toString().trim()
                );

                finishAffinity();
                final Intent intent = new Intent(getApplicationContext(), FeedActivity.class)
						.putExtra(FeedActivity.ARG_FROM_CREATE, true);

                FeedActivity.person = person;

                if (Resource.isLOrAbove()) {
                    startActivity(intent);
                } else {
                    startActivity(intent, ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.activity_out_reverse, R.anim.activity_in_reverse).toBundle());
                }
            } else {
                Snackbar.with(getApplicationContext())
                        .text(getString(R.string.create_fab_error))
                        .show(CreateDebtActivity.this);

                Resource.hideKeyboard(CreateDebtActivity.this);
            }
        }
    };

	public Person saveDebt(String name, boolean iOwe, float amount, String note) {
		if(iOwe) {
			amount = -amount;
		}
		if (TextUtils.isEmpty(note)) {
			note = null;
		}

		Person person;
		if(editingDebt == null) {
			person = data.getOrCreatePerson(name, this);
			data.debts.add(0, new Debt(person, amount, note, data.preferences.getCurrency()));
		} else {
			person = editingDebt.getOwner().getName().equals(name)
				? editingDebt.getOwner()
				: data.getOrCreatePerson(name, this);

			editingDebt.edit(person, amount, note);
		}

		storage.commit();
		return person;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == android.R.id.home) {
            if (getIntent().getBooleanExtra(ARG_FROM_FEED, false)) {

                finish();
                if (!Resource.isLOrAbove()) {
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

	@Override
	public void onBackPressed() {
		finish();
        if (!Resource.isLOrAbove()) {
            overridePendingTransition(R.anim.activity_out_reverse, R.anim.activity_in_reverse);
        }
	}

	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		Log.i("my_app", "New intent with flags " + intent.getFlags());
	}

}