package com.johnsimon.payback;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.micromobs.android.floatlabel.FloatLabelAutoCompleteTextView;
import com.micromobs.android.floatlabel.FloatLabelEditText;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class CreateDebtActivity extends Activity {

	private static String ARG_PREFIX = Resource.prefix("CREATE_DEBT");

	public static String ARG_FROM_FEED = Resource.arg(ARG_PREFIX, "FROM_FEED");
	public static String ARG_FROM_PERSON_NAME = Resource.arg(ARG_PREFIX, "FROM_PERSON_NAME");
	public static String ARG_TIMESTAMP = Resource.arg(ARG_PREFIX, "AMOUNT");

	//Views
	private FloatLabelEditText floatLabelAmount;
	private FloatLabelEditText floatLabelNote;
	private FloatLabelAutoCompleteTextView floatLabelName;

	private EditText floatLabelAmountEditText;
	private EditText floatLabelNoteEditText;
	private AutoCompleteTextView floatLabelNameAutoCompleteTextView;

	private RadioGroup radioGroup;
	private FloatingActionButton create_fab;

	private RequiredValidator validator;

	private Debt editingDebt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.accent_color));

        setContentView(R.layout.activity_create_debt);

		Intent intent = getIntent();

        getActionBar().setDisplayHomeAsUpEnabled(true);

		floatLabelAmount = (FloatLabelEditText) findViewById(R.id.create_float_label_amount);
		floatLabelNote = (FloatLabelEditText) findViewById(R.id.create_float_label_note);
		floatLabelName = (FloatLabelAutoCompleteTextView) findViewById(R.id.create_float_label_name);

		floatLabelAmountEditText = floatLabelAmount.getEditText();
		floatLabelNoteEditText = floatLabelNote.getEditText();
		floatLabelNameAutoCompleteTextView = floatLabelName.getEditText();

		floatLabelNoteEditText.setTextColor(getResources().getColor(R.color.gray_text_normal));

		floatLabelAmountEditText.setHint(getResources().getString(R.string.amount) + " (" + Resource.getCurrency() + ")");
		floatLabelAmountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

		radioGroup = (RadioGroup) findViewById(R.id.create_radio);

		if(intent.hasExtra(ARG_TIMESTAMP)) {
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

		create_fab = (FloatingActionButton) findViewById(R.id.create_fab);
		create_fab.setColor(getResources().getColor(android.R.color.white));
		create_fab.setDrawable(getResources().getDrawable(R.drawable.ic_fab_check));

		final Context ctx = this;

		validator = new RequiredValidator(new EditText[] {
				floatLabelNameAutoCompleteTextView,
				floatLabelAmountEditText
		}, new ValidatorListener() {
			@Override
			public void onValid() {
				//Some dirty Simme-style validation up in here
				if(floatLabelAmountEditText.getText().equals("0")) return;
				create_fab.setActive(true);
				create_fab.setAlpha(1f);
			}

			@Override
			public void onInvalid() {
				create_fab.setActive(false);
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

				if (create_fab.mActive) {
					saveDebt(
						floatLabelNameAutoCompleteTextView.getText().toString().trim(),
						radioGroup.getCheckedRadioButtonId() == R.id.create_radio_i_owe,
						Float.parseFloat(floatLabelAmountEditText.getText().toString()),
						floatLabelNoteEditText.getText().toString().trim()
					);

					startActivity(new Intent(ctx, FeedActivity.class), ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
					finish();
				} else {
					Resource.toast(ctx, getString(R.string.create_fab_error));
				}
            }
        });

    }

	public void saveDebt(String name, boolean iOwe, float amount, String note) {
		if(iOwe) {
			amount = -amount;
		}
		if (note.equals("")) {
			note = null;
		}

		if(editingDebt == null) {
			Resource.debts.add(0, new Debt(Resource.getPerson(name), amount, note));
		} else {
			editingDebt.edit(
				editingDebt.owner.name.equals(name)
					? editingDebt.owner
					: Resource.getPerson(name),
				amount,
				note
			);
		}

		Resource.commit();
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
            } else {
                startActivity(new Intent(this, FeedActivity.class));
                finishAffinity();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}