package com.johnsimon.payback.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataDialogFragment;
import com.johnsimon.payback.util.Resource;

public class FromWhoDialogFragment extends DataDialogFragment {

	public FromWhoSelected completeCallback = null;
	private AlertDialog alertDialog;
    private AutoCompleteTextView actv;
    private ImageButton from_who_clear;

    public final static String KEY_NAME = "FROM_WHO_LEY_NAME";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.from_who_dialog, null);

		final Button confirmButton = (Button) rootView.findViewById(R.id.from_who_dialog_confirm);
		Button cancelButton = (Button) rootView.findViewById(R.id.from_who_dialog_cancel);

		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});

		from_who_clear = (ImageButton) rootView.findViewById(R.id.from_who_clear);
		from_who_clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				actv.setText("");
				disableButton(confirmButton);
			}
		});

		actv = (AutoCompleteTextView) rootView.findViewById(R.id.from_who_actv);
		actv.setTextColor(getResources().getColor(R.color.gray_text_dark));

		Resources res = getResources();
		actv.setPadding(
				Resource.getPx(8, res),
				Resource.getPx(8, res),
				Resource.getPx(42, res),
				Resource.getPx(8, res)
		);



		Bundle args = getArguments();
		if (args != null) {
			String sentName = args.getString(KEY_NAME, "");
			if (!TextUtils.isEmpty(sentName)) {

				actv.setText(sentName);

				actv.setSelection(0, actv.getText().length());
				actv.requestFocus();

				enableButton(confirmButton);
			}
		}

		if (TextUtils.isEmpty(actv.getText().toString())) {
			disableButton(confirmButton);
		}

			actv.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {

				String name = s.toString();

				if (TextUtils.isEmpty(name)) {
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
	protected void onDataReceived() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.autocomplete_list_item,
                R.id.autocomplete_list_item_title,
                data.getAllNames());

        actv.setAdapter(adapter);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                actv.setAdapter(adapter);
            }
        }, 200);

        actv.setSelection(actv.getText().length());
	}

	@Override
	protected void onDataLinked() {

	}

    private void disableButton(Button btn) {
		btn.setTextColor(getResources().getColor(R.color.green_disabled));
		btn.setOnClickListener(null);
		btn.setClickable(false);
		btn.setEnabled(false);
		from_who_clear.setVisibility(View.GONE);
	}

	private void enableButton(Button btn) {
		btn.setTextColor(getResources().getColor(R.color.green_strong));
		btn.setOnClickListener(clickListener);
		btn.setClickable(true);
		btn.setEnabled(true);
		from_who_clear.setVisibility(View.VISIBLE);
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			completeCallback.onSelected(actv.getText().toString());
			alertDialog.dismiss();
		}
	};

	public interface FromWhoSelected {
		void onSelected(String name);
	}
}