package com.johnsimon.payback;

import android.app.Activity;
import android.app.ActivityOptions;
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
    private ArrayList<Person> list;

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
				create_fab.setClickable(true);
			}

			@Override
			public void onInvalid() {
				create_fab.setActive(false);
				create_fab.setAlpha(0.6f);
				create_fab.setClickable(false);
			}
		});

		create_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

				if (create_fab.mActive) {
					String name = contactsInputField.getText().toString();
					int amount = Integer.parseInt(floatingLabelEditText.getText().toString());
					boolean theyOwe = radioGroup.getCheckedRadioButtonId() == R.id.create_radio_they_owe;

					String note = FloatLabelEditTextDark.mEditTextView.getText().toString();

					Resource.debts.add(0, new Debt(fromPerson == null ? Resource.people.get(0) : fromPerson, theyOwe ? amount : -amount, note));
					Resource.commit();

					finish();
					startActivity(new Intent(ctx, FeedActivity.class), ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
				} else {
					Resource.toast(ctx, "You need to enter a name and a amount");
				}
            }
        });

        //TEST
        contactsInputField.setAdapter(new ArrayAdapter<String>(this, R.layout.autocomplete_list_item, R.id.autocomplete_list_item_title, getAllContactNames()));



        //END TEST
        /*
        list = getAllContactNames();

        for (int i = 0; i < 20; i ++) {
            list.add(new Person(i + " Person"));
        }

        ContactsAutoCompleteAdapter adapter = new ContactsAutoCompleteAdapter(this, list);
        contactsInputField.setAdapter(adapter);

        */

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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

	private List<String> getAllContactNames() {
		List<String> lContactNamesList = new ArrayList<String>();
		try {
			// Get all Contacts
			Cursor lPeople = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			if (lPeople != null) {
				while (lPeople.moveToNext()) {
					// Add Contact's Name into the List
					lContactNamesList.add(lPeople.getString(lPeople.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
				}
			}
		} catch (NullPointerException e) {
			Log.e("getAllContactNames()", e.getMessage());
		}
		return lContactNamesList;
	}

	private class PersonAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
		private ArrayList<String> resultList;

		public PersonAutoCompleteAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public int getCount() {
			return resultList.size();
		}

		@Override
		public String getItem(int index) {
			return resultList.get(index);
		}

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults filterResults = new FilterResults();
					if (constraint != null) {
						// Retrieve the autocomplete results.
						resultList = autocomplete();

						// Assign the data to the FilterResults
						filterResults.values = resultList;
						filterResults.count = resultList.size();
					}
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					if (results != null && results.count > 0) {
						notifyDataSetChanged();
					}
					else {
						notifyDataSetInvalidated();
					}
				}};
			return filter;
		}

		private ArrayList<String> autocomplete() {

			ArrayList<String> lContactNamesList = new ArrayList<String>();
			try {
				// Get all Contacts
				Cursor lPeople = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
				if (lPeople != null) {
					while (lPeople.moveToNext()) {
						// Add Contact's Name into the List
						lContactNamesList.add(lPeople.getString(lPeople.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
					}
				}
			} catch (NullPointerException e) {
				Log.e("getAllContactNames()", e.getMessage());
			}
			return lContactNamesList;
		}

	}
}