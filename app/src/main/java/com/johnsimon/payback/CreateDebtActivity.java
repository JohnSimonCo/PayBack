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

import com.micromobs.android.floatlabel.FloatLabelEditText;
import com.micromobs.android.floatlabel.FloatLabelEditTextDark;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class CreateDebtActivity extends Activity {

	private static String ARG_PREFIX = Resource.prefix("CREATE_DEBT");

	public static String ARG_FROM_FEED = Resource.arg(ARG_PREFIX, "FROM_FEED");
	public static String ARG_FROM_PERSON_NAME = Resource.arg(ARG_PREFIX, "FROM_PERSON_NAME");
	public static String ARG_TIMESTAMP = Resource.arg(ARG_PREFIX, "AMOUNT");

	//Views
    private AutoCompleteTextView contactsInputField;
	private EditText floatingLabelEditText;
	private FloatLabelEditText floatLabelAmount;
	private FloatLabelEditTextDark create_float_label_note;
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
        contactsInputField = (AutoCompleteTextView) findViewById(R.id.floating_label_edit_text_auto);

		//This is the container for the float label edit text...
		floatLabelAmount = (FloatLabelEditText) findViewById(R.id.create_float_label_amount);
		floatLabelAmount.setHint(getResources().getString(R.string.amount) + " (" + Resource.getCurrency() + ")");

		//...while this is the internal edit text
		floatingLabelEditText = (EditText) findViewById(R.id.floating_label_edit_text);
		floatingLabelEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

		create_float_label_note = (FloatLabelEditTextDark) findViewById(R.id.floating_label_edit_text);


		if(intent.hasExtra(ARG_TIMESTAMP)) {
			editingDebt = Resource.data.findDebt(intent.getLongExtra(ARG_TIMESTAMP, 0));

			contactsInputField.setText(editingDebt.owner.name);

			floatingLabelEditText.setText(Float.toString(Math.abs(editingDebt.amount)));

			EditText noteEditText = create_float_label_note.getEditText();
			noteEditText.setText(editingDebt.note);
			//Assume the user wants to change the note
			noteEditText.setSelection(noteEditText.getText().length());
			noteEditText.requestFocus();

			boolean iOwe = editingDebt.amount < 0;
			radioGroup.check(iOwe ? R.id.create_radio_i_owe : R.id.create_radio_they_owe);
		} else if(intent.hasExtra(ARG_FROM_PERSON_NAME)) {
			contactsInputField.setText(intent.getStringExtra(ARG_FROM_PERSON_NAME));
			floatingLabelEditText.requestFocus();
		}

		radioGroup = (RadioGroup) findViewById(R.id.create_radio);

		create_fab = (FloatingActionButton) findViewById(R.id.create_fab);
		create_fab.setColor(getResources().getColor(android.R.color.white));
		create_fab.setDrawable(getResources().getDrawable(R.drawable.ic_check));

		final Context ctx = this;

		validator = new RequiredValidator(new EditText[] {
				contactsInputField,
				floatingLabelEditText
		}, new ValidatorListener() {
			@Override
			public void onValid() {
				//Some dirty Simme-style validation up in here
				if(floatingLabelEditText.getText().equals("0")) return;
				create_fab.setActive(true);
				create_fab.setAlpha(1f);
			}

			@Override
			public void onInvalid() {
				create_fab.setActive(false);
				create_fab.setAlpha(0.6f);
			}
		});

		contactsInputField.setAdapter(new ArrayAdapter<String>(
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
						contactsInputField.getText().toString().trim(),
						radioGroup.getCheckedRadioButtonId() == R.id.create_radio_i_owe,
						Float.parseFloat(floatingLabelEditText.getText().toString()),
						create_float_label_note.getText().trim()
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