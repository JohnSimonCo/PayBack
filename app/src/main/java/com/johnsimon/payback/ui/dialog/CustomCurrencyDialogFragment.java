package com.johnsimon.payback.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.widget.AppCompatEditText;

import com.johnsimon.payback.R;
import com.johnsimon.payback.util.RequiredValidator;
import com.johnsimon.payback.util.ValidatorListener;

public class CustomCurrencyDialogFragment extends DialogFragment {

	public CustomCurrencySelectedCallback completeCallback;

	private AppCompatEditText customCurrencyEditText;
	private AlertDialog alertDialog;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.custom_currency_dialog, null);

		final Button dialogCustomCurrencyConfirm = (Button) rootView.findViewById(R.id.dialog_custom_currency_confirm);
		Button dialogCustomCurrencyCancel = (Button) rootView.findViewById(R.id.dialog_custom_currency_cancel);

		dialogCustomCurrencyCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});

		customCurrencyEditText = (AppCompatEditText) rootView.findViewById(R.id.custom_currency_dialog_edittext);
		customCurrencyEditText.setTextColor(getResources().getColor(R.color.gray_text_dark));

		if (TextUtils.isEmpty(customCurrencyEditText.getText().toString())) {
			disableButton(dialogCustomCurrencyConfirm);
		}

		customCurrencyEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				updatePreview();
			}
		});

		new RequiredValidator(new EditText[] {customCurrencyEditText}, new ValidatorListener() {
			@Override
			public void onValid() {
				enableButton(dialogCustomCurrencyConfirm);
			}

			@Override
			public void onInvalid() {
				disableButton(dialogCustomCurrencyConfirm);
			}
		});

		builder.setView(rootView);

		alertDialog = builder.create();
		return alertDialog;
	}

	private void updatePreview() {

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
			completeCallback.onSelected(customCurrencyEditText.getText().toString());
			alertDialog.dismiss();
		}
	};

	public interface CustomCurrencySelectedCallback {
		void onSelected(String currency);
	}

}
