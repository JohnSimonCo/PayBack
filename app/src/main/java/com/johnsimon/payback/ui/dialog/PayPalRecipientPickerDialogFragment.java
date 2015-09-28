package com.johnsimon.payback.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.devspark.robototextview.widget.RobotoButton;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataDialogFragment;

public class PayPalRecipientPickerDialogFragment extends DataDialogFragment {

    public final static String KEY_SUGGESTIONS = "KEY_SUGGESTIONS";
    public RecipientSelected recipientSelectedCallback = null;

    private AlertDialog alertDialog;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        recipientSelectedCallback = (RecipientSelected) activity;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.paypal_recipient_picker_dialog, null);

        final LinearLayout paypal_suggestions = (LinearLayout) rootView.findViewById(R.id.paypal_suggestions);
        final RobotoButton dialogContinue = (RobotoButton) rootView.findViewById(R.id.paypal_dialog_ok);
        final RobotoButton dialogCancel = (RobotoButton) rootView.findViewById(R.id.paypal_dialog_cancel);
        final AppCompatEditText editText = (AppCompatEditText) rootView.findViewById(R.id.email_phone_picker_edittext);

        Bundle args = getArguments();
        if (args != null) {
            String[] suggestions = args.getStringArray(KEY_SUGGESTIONS);
            if (suggestions != null && suggestions.length > 0) {
                for (final String suggestion : suggestions) {
                    LayoutInflater buttonInflater = LayoutInflater.from(getActivity());
                    RobotoButton button = (RobotoButton) buttonInflater.inflate(R.layout.paypal_suggestion_button, null, false);

                    button.setText(suggestion);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            recipientSelectedCallback.onRecipientSelected(suggestion);
                            alertDialog.dismiss();
                        }
                    });

                    paypal_suggestions.addView(button);
                }
            } else {
                paypal_suggestions.setVisibility(View.GONE);
            }
        } else {
            paypal_suggestions.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(editText.getText().toString())) {
            disableButton(dialogContinue);
        } else {
            enableButton(dialogContinue);
        }

        dialogContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recipientSelectedCallback.onRecipientSelected(editText.getText().toString());
                alertDialog.dismiss();
            }
        });

        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    disableButton(dialogContinue);
                } else {
                    enableButton(dialogContinue);
                }
            }
        });

        builder.setView(rootView);

        alertDialog = builder.create();

        return alertDialog;
    }

    private void disableButton(Button btn) {
        btn.setTextColor(getResources().getColor(R.color.button_color_disabled));
        btn.setClickable(false);
        btn.setEnabled(false);
    }

    private void enableButton(Button btn) {
        btn.setTextColor(getResources().getColor(R.color.button_color));
        btn.setClickable(true);
        btn.setEnabled(true);
    }

    @Override
    protected void onDataReceived() {

    }

    @Override
    protected void onDataLinked() {

    }


    public interface RecipientSelected {
        void onRecipientSelected(String recipient);
    }

}
