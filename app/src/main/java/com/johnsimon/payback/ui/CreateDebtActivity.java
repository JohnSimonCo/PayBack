package com.johnsimon.payback.ui;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import android.support.v7.internal.widget.TintEditText;

import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.R;
import com.johnsimon.payback.util.RequiredValidator;
import com.johnsimon.payback.util.ValidatorListener;
import com.johnsimon.payback.util.Resource;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.shamanland.fab.FloatingActionButton;

public class CreateDebtActivity extends ActionBarActivity {

	private static String ARG_PREFIX = Resource.prefix("CREATE_DEBT");

	public static String ARG_FROM_FEED = Resource.arg(ARG_PREFIX, "FROM_FEED");
	public static String ARG_FROM_PERSON_NAME = Resource.arg(ARG_PREFIX, "FROM_PERSON_NAME");
	public static String ARG_TIMESTAMP = Resource.arg(ARG_PREFIX, "AMOUNT");

	//Views
	private TintEditText floatLabelAmountEditText;
	private TintEditText floatLabelNoteEditText;
	private AutoCompleteTextView floatLabelNameAutoCompleteTextView;
    private Toolbar toolbar;

	private RadioGroup radioGroup;
	private FloatingActionButton create_fab;

	private RequiredValidator validator;

	private Debt editingDebt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.primary_color_darker));

        setContentView(R.layout.activity_create_debt);

        toolbar = (Toolbar) findViewById(R.id.create_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();

		floatLabelAmountEditText = (TintEditText) findViewById(R.id.create_edittext_amount);
		floatLabelNoteEditText = (TintEditText) findViewById(R.id.create_edittext_note);
		floatLabelNameAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.create_edittext_name);

		floatLabelNoteEditText.setTextColor(getResources().getColor(R.color.gray_text_normal));

		floatLabelAmountEditText.setHint(getResources().getString(R.string.amount) + " (" + Resource.getCurrency() + ")");
		floatLabelAmountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

		radioGroup = (RadioGroup) findViewById(R.id.create_radio);

		if (intent.hasExtra(ARG_TIMESTAMP)) {
			editingDebt = Resource.data.findDebt(intent.getLongExtra(ARG_TIMESTAMP, 0));

			floatLabelNameAutoCompleteTextView.setText(editingDebt.owner.name);

			floatLabelAmountEditText.setText(Float.toString(Math.abs(editingDebt.amount)));

			floatLabelNoteEditText.setText(editingDebt.note);
			//Assume the user wants to change the note
			floatLabelNoteEditText.setSelection(floatLabelNoteEditText.getText().length());
			floatLabelNoteEditText.requestFocus();

			boolean iOwe = editingDebt.amount < 0;
			radioGroup.check(iOwe ? R.id.create_radio_i_owe : R.id.create_radio_they_owe);
		} else if(intent.hasExtra(ARG_FROM_PERSON_NAME)) {
			floatLabelNameAutoCompleteTextView.setText(intent.getStringExtra(ARG_FROM_PERSON_NAME));
			floatLabelAmountEditText.requestFocus();
		}

		floatLabelNameAutoCompleteTextView.setPadding(
			Resource.getPx(8, this),
			Resource.getPx(8, this),
			Resource.getPx(42, this),
			Resource.getPx(8, this)
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

		create_fab = (FloatingActionButton) findViewById(R.id.create_fab);
		create_fab.setColor(getResources().getColor(android.R.color.white));

		final Context ctx = this;

		validator = new RequiredValidator(new EditText[] {
				floatLabelNameAutoCompleteTextView,
				floatLabelAmountEditText
		}, new ValidatorListener() {
			@Override
			public void onValid() {
				//Some dirty Simme-style validation up in here
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

		floatLabelNameAutoCompleteTextView.setAdapter(new ArrayAdapter<String>(
				this,
				R.layout.autocomplete_list_item,
				R.id.autocomplete_list_item_title,
				Resource.getAllNames()
		));

		create_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

				if (create_fab.isActivated()) {
					Person person = saveDebt(
						floatLabelNameAutoCompleteTextView.getText().toString().trim(),
						radioGroup.getCheckedRadioButtonId() == R.id.create_radio_i_owe,
						Float.parseFloat(floatLabelAmountEditText.getText().toString()),
						floatLabelNoteEditText.getText().toString().trim()
					);

					finishAffinity();
					final Intent intent = new Intent(ctx, FeedActivity.class);

					FeedActivity.person = person;

                    if (Resource.isLOrAbove()) {
						startActivity(intent);
					} else {
						startActivity(intent, ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.activity_out_reverse, R.anim.activity_in_reverse).toBundle());
					}
				} else {
					Resource.toast(ctx, getString(R.string.create_fab_error));
				}
            }
        });

    }

	public Person saveDebt(String name, boolean iOwe, float amount, String note) {
		if(iOwe) {
			amount = -amount;
		}
		if (note.equals("")) {
			note = null;
		}

		Person person;
		if(editingDebt == null) {
			person = Resource.getPerson(name);
			Resource.debts.add(0, new Debt(person, amount, note));
		} else {
			person = editingDebt.owner.name.equals(name)
				? editingDebt.owner
				: Resource.getPerson(name);

			editingDebt.edit(person, amount, note);
		}

		Resource.commit();
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
}