package com.johnsimon.payback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.micromobs.android.floatlabel.FloatLabelEditText;

import java.util.ArrayList;

public class WelcomeDialogFragment extends DialogFragment {

	private AlertDialog alertDialog;
	private boolean hasNfc = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.welcome_dialog, null);

        final Button welcome_continue = (Button) rootView.findViewById(R.id.welcome_continue);
        welcome_continue.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "robotomedium.ttf"));

        FloatLabelEditText currencyEditTextFloat = (FloatLabelEditText) rootView.findViewById(R.id.welcome_currency_edit_float);
		EditText currencyEditText = currencyEditTextFloat.getEditText();
		currencyEditText.setTextColor(getResources().getColor(R.color.gray_text_normal));

        new RequiredValidator(new EditText[] {
                currencyEditText
        }, new ValidatorListener() {
            @Override
            public void onValid() {
                welcome_continue.setTextColor(getResources().getColor(R.color.green_strong));
				welcome_continue.setClickable(true);
				welcome_continue.setOnClickListener(clickListener);
			}

            @Override
            public void onInvalid() {
                welcome_continue.setTextColor(getResources().getColor(R.color.green_disabled));
				welcome_continue.setClickable(false);
				welcome_continue.setOnClickListener(null);

			}
        });

		hasNfc = NfcAdapter.getDefaultAdapter(getActivity()) != null;

		if (hasNfc) {
			welcome_continue.setText(R.string.welcome_got_it);
		}

		builder.setView(rootView);

		alertDialog = builder.create();

        return alertDialog;
    }

	View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			//Button was disabled when no currency so we're free
			//to continue since the user was able to press the button
			if (hasNfc) {
				WelcomeNfcDialogFragment welcomeNfcDialogFragment = new WelcomeNfcDialogFragment();
				welcomeNfcDialogFragment.show(getFragmentManager(), "welcome_nfc");
			}

			alertDialog.cancel();
		}
	};

}