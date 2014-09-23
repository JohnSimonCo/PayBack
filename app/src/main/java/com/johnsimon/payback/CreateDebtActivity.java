package com.johnsimon.payback;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RadioGroup;

import com.micromobs.android.floatlabel.FloatLabelEditText;
import com.micromobs.android.floatlabel.FloatLabelEditTextDark;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateDebtActivity extends Activity {

	private static String ARG_PREFIX = Resource.prefix("CREATE_DEBT");

	public static String ARG_FROM_FEED = Resource.arg(ARG_PREFIX, "FROM_FEED");
	public static String ARG_FROM_PERSON_ID = Resource.arg(ARG_PREFIX, "FROM_PERSON");

	//Views
    private AutoCompleteTextView contactsInputField;
	private EditText floatingLabelEditText;
	private FloatLabelEditText floatLabelAmount;
	private RadioGroup radioGroup;
	private FloatingActionButton create_fab;

	private RequiredValidator validator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.accent_color));

        setContentView(R.layout.activity_create_debt);

		String fromId = getIntent().getStringExtra(ARG_FROM_PERSON_ID);
		final Person fromPerson = fromId == null ? null : Resource.data.findPerson(UUID.fromString(fromId));

        getActionBar().setDisplayHomeAsUpEnabled(true);
        contactsInputField = (AutoCompleteTextView) findViewById(R.id.floating_label_edit_text_auto);
		if(fromPerson != null) {
			contactsInputField.setText(fromPerson.name);
			contactsInputField.setSelection(contactsInputField.getText().length());
		}

		//This is the container for the float label edit text...
		floatLabelAmount = (FloatLabelEditText) findViewById(R.id.create_float_label_amount);
		floatLabelAmount.setHint(getResources().getString(R.string.amount) + " (" + Resource.getCurrency() + ")");

		//...while this is the internal edit text
		floatingLabelEditText = (EditText) findViewById(R.id.floating_label_edit_text);
		floatingLabelEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

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
				create_fab.setActive(true);
				create_fab.setAlpha(1f);
			}

			@Override
			public void onInvalid() {
				create_fab.setActive(false);
				create_fab.setAlpha(0.6f);
			}
		});

		create_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

				if (create_fab.mActive) {
					String name = contactsInputField.getText().toString();
					boolean iOwe = radioGroup.getCheckedRadioButtonId() == R.id.create_radio_i_owe;

					int amount = Integer.parseInt(floatingLabelEditText.getText().toString());
					if(iOwe) {
						amount = -amount;
					}

					String note = FloatLabelEditTextDark.mEditTextView.getText().toString();
					if (note.equals("")) {
						note = getString(R.string.cash);
					}

                    //Just because activity was started as adding debt for
                    //for a specific person it doesn't mean the user can't change the name
					Resource.debts.add(0, new Debt(fromPerson == null ? Resource.people.get(0) : fromPerson, amount, note));
					Resource.commit();

					finish();
					startActivity(new Intent(ctx, FeedActivity.class), ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
				} else {
					Resource.toast(ctx, getString(R.string.create_fab_error));
				}
            }
        });

        contactsInputField.setAdapter(new ArrayAdapter<String>(this, R.layout.autocomplete_list_item, R.id.autocomplete_list_item_title, getAllContactNames()));
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

	private ArrayList<String> getAllContactNames() {
		ArrayList<String> contactNames = new ArrayList<String>();
		ArrayList<String> uris = new ArrayList<String>();
		Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if(cursor.getCount() > 0) {
			while(cursor.moveToNext()) {
				String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				String uri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
				uris.add(uri);
				if(!name.matches(".*@.*\\..*") && !contactNames.contains(name)) {
					contactNames.add(name);
				}
			}
		}
		return contactNames;
	}
}