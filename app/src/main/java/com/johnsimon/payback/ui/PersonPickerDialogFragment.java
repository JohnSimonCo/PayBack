package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.internal.widget.TintEditText;

import com.johnsimon.payback.R;
import com.johnsimon.payback.util.RequiredValidator;
import com.johnsimon.payback.util.ValidatorListener;
import com.johnsimon.payback.util.FontCache;

public class PersonPickerDialogFragment extends DialogFragment {

	public PersonSelectedCallback completeCallback;

	private AutoCompleteTextView autoCompleteTextView;
	private AlertDialog alertDialog;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.person_picker_dialog, null);

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

		if (autoCompleteTextView.getText().toString().equals("")) {
			disableButton(confirmButton);
		}

		new RequiredValidator(new EditText[] {autoCompleteTextView}, new ValidatorListener() {
			@Override
			public void onValid() {
				enableButton(confirmButton);
			}

			@Override
			public void onInvalid() {
				disableButton(confirmButton);
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
		public void onSelected(String currency);
	}

}
