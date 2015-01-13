package com.johnsimon.payback.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class RequiredValidator implements TextWatcher {

	EditText[] editTexts;
	ValidatorListener listener;

	public RequiredValidator(EditText[] editTexts, ValidatorListener listener) {
		this.editTexts = editTexts;
		this.listener = listener;
		for(EditText editText : editTexts) {
			editText.addTextChangedListener(this);
		}
	}

	public void validate() {
		for(EditText editText : editTexts) {
			if(editText.getText().toString().isEmpty()) {
				listener.onInvalid();
				return;
			}
		}

		listener.onValid();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		validate();

	}
}
