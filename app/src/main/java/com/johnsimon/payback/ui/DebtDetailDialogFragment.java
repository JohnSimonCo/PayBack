package com.johnsimon.payback.ui;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.Resource;
import com.makeramen.RoundedImageView;

public class DebtDetailDialogFragment extends DialogFragment implements PaidBackDialogFragment.CompleteCallback {

    public static Debt debtAccessible = null;
    private static Debt debt = null;

    public Callback callback = null;
    public AlertDialog alertDialog;

    public static DebtDetailDialogFragment newInstance(Debt debt) {
        DebtDetailDialogFragment.debt = debt;
        DebtDetailDialogFragment.debtAccessible = debt;

        return new DebtDetailDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View rootView = inflater.inflate(R.layout.detail_dialog, null);

        Button dialog_custom_confirm = (Button) rootView.findViewById(R.id.dialog_custom_confirm);
        Button dialog_custom_cancel = (Button) rootView.findViewById(R.id.dialog_custom_cancel);


        if (debt.isPaidBack) {
            dialog_custom_confirm.setText(R.string.undo_pay_back);
            dialog_custom_confirm.setTextColor(getResources().getColor(R.color.red));
        }

        //This is the share button
        dialog_custom_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, debt.getShareString(getActivity()));
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.share)));
            }
        });

        final DebtDetailDialogFragment self = this;
        dialog_custom_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PaidBackDialogFragment paidBackDialogFragment;

                if (debt.isPaidBack) {
                    paidBackDialogFragment = PaidBackDialogFragment.newInstance(PaidBackDialogFragment.UNDO_PAY_BACK, debt);
                } else {
                    paidBackDialogFragment = PaidBackDialogFragment.newInstance(PaidBackDialogFragment.PAY_BACK, debt);
                }
                paidBackDialogFragment.show(getFragmentManager().beginTransaction(), "paid_back_dialog");
                paidBackDialogFragment.completeCallback = self;

                alertDialog.cancel();
            }
        });

        TextView dialog_custom_amount = (TextView) rootView.findViewById(R.id.dialog_custom_amount);
        if (debt.amount < 0) {
            //negative
            dialog_custom_amount.setText(debt.amountAsString);
            dialog_custom_amount.setTextColor(getResources().getColor(R.color.red));
        } else {
            dialog_custom_amount.setText(debt.amountAsString);
            dialog_custom_amount.setTextColor(getResources().getColor(R.color.green_strong));
        }


        TextView dialog_custom_title = (TextView) rootView.findViewById(R.id.dialog_custom_title);
        TextView dialog_custom_content = (TextView) rootView.findViewById(R.id.dialog_custom_content);

        dialog_custom_title.setText(debt.owner.name);

        if (debt.note == null) {
            dialog_custom_content.setText(R.string.cash);
        } else {
            dialog_custom_content.setText(debt.note);
        }

        ImageButton detailDialogOverflow = (ImageButton) rootView.findViewById(R.id.detail_dialog_overflow);
        detailDialogOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                popupMenu.inflate(R.menu.detail_dialog_popup);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.detail_dialog_edit:
                                if (callback != null) {
                                    callback.onEdit(debt);
                                }
                                alertDialog.cancel();

                                return true;
                            case R.id.detail_dialog_delete:
                                if (callback != null) {
                                    callback.onDelete(debt);
                                }
                                alertDialog.cancel();

                                return true;
                            case R.id.detail_dialog_change:

                                PersonPickerDialogFragment personPickerDialogFragment = new PersonPickerDialogFragment();

                                Bundle args = new Bundle();
                                args.putString(PersonPickerDialogFragment.TITLE_KEY, PersonPickerDialogFragment.USE_DEFAULT_TITLE);
                                args.putBoolean(PersonPickerDialogFragment.PEOPLE_KEY, true);
                                personPickerDialogFragment.setArguments(args);

                                personPickerDialogFragment.show(getFragmentManager(), "person_dialog");
                                personPickerDialogFragment.completeCallback = changePersonCallback;

                                alertDialog.cancel();
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        RoundedImageView avatar = (RoundedImageView) rootView.findViewById(R.id.detail_dialog_avatar);
        TextView avatarLetter = (TextView) rootView.findViewById(R.id.detail_dialog_avatar_letter);

        Resource.createProfileImage(debt.owner, avatar, avatarLetter);

        builder.setView(rootView);

        alertDialog = builder.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        return alertDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        debtAccessible = null;
    }

    public PersonPickerDialogFragment.PersonSelectedCallback changePersonCallback = new PersonPickerDialogFragment.PersonSelectedCallback() {
        @Override
        public void onSelected(String name) {
            if (callback != null) {
                callback.onMove(debt, Resource.data.findPersonByName(name));
            }
        }
    };

    @Override
    public void onComplete(Debt _debt) {
        if (callback != null) {
            callback.onPaidBack(_debt);
        }
    }

    public interface Callback {
        public void onPaidBack(Debt debt);

        public void onDelete(Debt debt);

        public void onEdit(Debt debt);

        public void onMove(Debt debt, Person person);
    }
}