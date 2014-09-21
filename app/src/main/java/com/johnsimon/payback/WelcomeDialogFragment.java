package com.johnsimon.payback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

public class WelcomeDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.welcome_dialog, null);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.currency_spinner_item, Resource.getAllAvailableCurrencies());
        Spinner curSpinner = (Spinner) rootView.findViewById(R.id.cur_spinner);

        curSpinner.setAdapter(adapter);

        builder.setView(rootView);

        return builder.create();
    }

}