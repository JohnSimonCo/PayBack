package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.internal.widget.TintEditText;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.RequiredValidator;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.RobotoMediumTextView;
import com.johnsimon.payback.util.ValidatorListener;
import com.johnsimon.payback.util.FontCache;

import java.util.ArrayList;

public class PersonPickerDialogFragment extends DialogFragment {

	public PersonSelectedCallback completeCallback = null;
	private AlertDialog alertDialog;
	private String title;

	public final static String USE_DEFAULT_TITLE = "PERSON_PICKER_DIALOG_FRAGMENT_NO_TITLE";
	public final static String TITLE_KEY = "PERSON_PICKER_DIALOG_FRAGMENT_TITLE_KEY";

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
		}

		boolean useOnlyContacts = false;

		RobotoMediumTextView person_picker_dialog_title = (RobotoMediumTextView) rootView.findViewById(R.id.person_picker_dialog_title);
		if (!title.equals(USE_DEFAULT_TITLE)) {
			person_picker_dialog_title.setText(title);
			if (title.equals(getResources().getString(R.string.rename))) {
				useOnlyContacts = true;
			}
		}

		final Button confirmButton = (Button) rootView.findViewById(R.id.dialog_select_person_confirm);
		Button cancelButton = (Button) rootView.findViewById(R.id.dialog_select_person_cancel);
		cancelButton.setTypeface(FontCache.get(getActivity(), FontCache.RobotoMedium));

		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.cancel();
			}
		});

		autoCompleteTextView = (AutoCompleteTextView) rootView.findViewById(R.id.select_person_actv);
		autoCompleteTextView.setTextColor(getResources().getColor(R.color.gray_text_dark));

		ArrayList<String> people;

		if (useOnlyContacts) {
			people = Resource.getContactNames();
		} else {
			people = Resource.getAllNames();
		}
		autoCompleteTextView.setAdapter(new ArrayAdapter<String>(
				getActivity(),
				R.layout.autocomplete_list_item,
				R.id.autocomplete_list_item_title,
				people));

		if (autoCompleteTextView.getText().toString().equals("")) {
			disableButton(confirmButton);
		}

		autoCompleteTextView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				if (TextUtils.isEmpty(s.toString())) {
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

	private void disableButton(Button btn) {
		btn.setTextColor(getResources().getColor(R.color.green_disabled));
		btn.setOnClickListener(null);
		btn.setClickable(false);
		btn.setEnabled(false);
	}

	private void enableButton(Button btn) {
		btn.setTextColor(getResources().getColor(R.color.green_strong));
		btn.setOnClickListener(clickListener);
		btn.setClickable(true);
		btn.setEnabled(true);
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			completeCallback.onSelected(autoCompleteTextView.getText().toString());
			alertDialog.cancel();
		}
	};

	public interface PersonSelectedCallback {
		public void onSelected(String name);
	}
}