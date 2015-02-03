package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataDialogFragment;
import com.johnsimon.payback.data.Person;

import java.util.ArrayList;
import java.util.Iterator;

public class PersonPickerDialogFragment extends DataDialogFragment {

	public PersonSelectedCallback completeCallback = null;
	private AlertDialog alertDialog;
	private String title;
	private boolean useOnlyPeopleInApp = false;
	private boolean useOnlyContacts;
	private Button confirmButton;
	private ArrayAdapter adapter;
	private String blacklist;

	public final static String USE_DEFAULT_TITLE = "PERSON_PICKER_DIALOG_FRAGMENT_NO_TITLE";
	public final static String TITLE_KEY = "PERSON_PICKER_DIALOG_FRAGMENT_TITLE_KEY";
	public final static String PEOPLE_KEY = "PERSON_PICKER_DIALOG_FRAGMENT_PERSON_KEY";
	public final static String BLACKLIST_KEY = "PERSON_PICKER_DIALOG_FRAGMENT_BLACKLIST_KEY";
	public final static String NO_EXISTING_PEOPLE_FLAG = "PERSON_PICKER_DIALOG_FRAGMENT_NO_EXISTING_PEOPLE_FLAG";

	private boolean noExistingPeople = false;

	private AutoCompleteTextView autoCompleteTextView;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.person_picker_dialog, null);

        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(TITLE_KEY, USE_DEFAULT_TITLE);
            useOnlyPeopleInApp = args.getBoolean(PEOPLE_KEY, false);
			noExistingPeople = args.getBoolean(NO_EXISTING_PEOPLE_FLAG, false);
		}

        useOnlyContacts = false;

        TextView person_picker_dialog_title = (TextView) rootView.findViewById(R.id.person_picker_dialog_title);
		if (!title.equals(USE_DEFAULT_TITLE)) {
			person_picker_dialog_title.setText(title);
			if (title.equals(getResources().getString(R.string.rename))) {
				useOnlyContacts = true;
			}
		}

		confirmButton = (Button) rootView.findViewById(R.id.dialog_select_person_confirm);
		Button cancelButton = (Button) rootView.findViewById(R.id.dialog_select_person_cancel);

		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});

		autoCompleteTextView = (AutoCompleteTextView) rootView.findViewById(R.id.select_person_actv);
		autoCompleteTextView.setTextColor(getResources().getColor(R.color.gray_text_dark));

		if (TextUtils.isEmpty(autoCompleteTextView.getText())) {
			disableButton(confirmButton);
		}

		blacklist = getArguments().getString(BLACKLIST_KEY, "");

		autoCompleteTextView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void afterTextChanged(Editable s) {

				String name = s.toString();

				if (TextUtils.isEmpty(name) || (useOnlyPeopleInApp && data.findPersonByName(name) == null) || name.equals(blacklist)) {
					disableButton(confirmButton);
				} else {
					enableButton(confirmButton);
				}
			}
		});

		builder.setView(rootView);

		alertDialog = builder.create();
		return alertDialog;
	}

	@Override
	protected void onDataLinked() {
		ArrayList<String> people;

		if (useOnlyContacts) {
			people = data.getContactNames();
		} else if (useOnlyPeopleInApp) {
			people = data.getPeopleNames();
		} else {
			people = data.getAllNames();
		}

		if (!TextUtils.isEmpty(blacklist)) {

            for(Iterator<String> iterator = people.iterator(); iterator.hasNext();) {
                if (iterator.next().equals(blacklist)) {
                    iterator.remove();
                }
            }
		}

		ArrayList<String> dataPeopleNames = new ArrayList<>();
		for (Person person : data.people) {
			dataPeopleNames.add(person.getName());
		}

		if (noExistingPeople) {
			for(Iterator<String> iterator = people.iterator(); iterator.hasNext();) {
				if(dataPeopleNames.contains(iterator.next())) {
					iterator.remove();
				}
			}
		}

		adapter = new ArrayAdapter<>(
				getActivity(),
				R.layout.autocomplete_list_item,
				R.id.autocomplete_list_item_title,
				people);

		autoCompleteTextView.setAdapter(adapter);
	}

	private void disableButton(Button btn) {
		btn.setTextColor(getResources().getColor(R.color.button_color_disabled));
		btn.setOnClickListener(null);
		btn.setClickable(false);
		btn.setEnabled(false);
	}

	private void enableButton(Button btn) {
		btn.setTextColor(getResources().getColor(R.color.button_color));
		btn.setOnClickListener(clickListener);
		btn.setClickable(true);
		btn.setEnabled(true);
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			completeCallback.onSelected(autoCompleteTextView.getText().toString());
			alertDialog.dismiss();
		}
	};

	public interface PersonSelectedCallback {
		public void onSelected(String name);
	}
}