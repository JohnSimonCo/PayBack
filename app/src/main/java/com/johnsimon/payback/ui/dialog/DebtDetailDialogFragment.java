package com.johnsimon.payback.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataDialogFragment;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.util.Alarm;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.SwishLauncher;
import com.makeramen.RoundedImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DebtDetailDialogFragment extends DataDialogFragment {

    public static Debt debt = null;

    public Callback callback = null;
    private Button dialog_custom_payback;
    private Button dialog_custom_send;
    private Button dialog_custom_payment;
    private TextView dialog_paid_back_date;
    private TextView dialog_reminder_date;
    private TextView dialog_custom_title;
    private TextView dialog_custom_content;
    private TextView dialog_custom_amount_payment;
    private ImageButton detailDialogOverflow;
    private RoundedImageView avatar;
    private TextView avatarLetter;
    private TextView dialog_custom_amount;

    public MenuItem detailMenuPay;
    public AlertDialog alertDialog;

    public static DebtDetailDialogFragment newInstance(Debt debt) {
        DebtDetailDialogFragment.debt = debt;

        return new DebtDetailDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View rootView = inflater.inflate(R.layout.detail_dialog, null);

        dialog_custom_payback = (Button) rootView.findViewById(R.id.dialog_custom_payback);
        dialog_custom_send = (Button) rootView.findViewById(R.id.dialog_custom_send);
        dialog_custom_payment = (Button) rootView.findViewById(R.id.dialog_custom_payment);

        dialog_custom_amount = (TextView) rootView.findViewById(R.id.dialog_custom_amount);
        dialog_custom_amount_payment = (TextView) rootView.findViewById(R.id.dialog_custom_amount_payment);
        dialog_paid_back_date = (TextView) rootView.findViewById(R.id.dialog_paid_back_date);
        dialog_reminder_date = (TextView) rootView.findViewById(R.id.dialog_reminder_date);

        dialog_custom_title = (TextView) rootView.findViewById(R.id.dialog_custom_title);
        dialog_custom_content = (TextView) rootView.findViewById(R.id.dialog_custom_content);

        detailDialogOverflow = (ImageButton) rootView.findViewById(R.id.detail_dialog_overflow);

        avatar = (RoundedImageView) rootView.findViewById(R.id.detail_dialog_avatar);
        avatarLetter = (TextView) rootView.findViewById(R.id.detail_dialog_avatar_letter);

        builder.setView(rootView);

        alertDialog = builder.create();

        return alertDialog;
    }

	@Override
	protected void onDataReceived() {
        DebtDetailDialogFragment.debt = data.findDebt(debt.id);

        Alarm.cancelNotification(getActivity().getApplicationContext(), debt);

        Resource.createProfileImage(getDataActivity(), debt.getOwner(), avatar, avatarLetter);

		dialog_custom_amount.setText(data.preferences.getCurrency().render(debt));
        dialog_custom_amount.setTextColor(debt.getColor());

		dialog_custom_amount.setTextColor(getResources().getColor(
                debt.getAmount() < 0 ? debt.getColor() : R.color.green_strong));

        if (debt.isPartiallyPaidBack()) {
            dialog_custom_amount_payment.setVisibility(View.VISIBLE);
            dialog_custom_amount_payment.setTextColor(getResources().getColor(
                    debt.getAmount() < 0 ? debt.getColor() : R.color.green_strong));
            dialog_custom_amount_payment.setText(data.preferences.getCurrency().render(debt.getRemainingDebt()));
            dialog_custom_amount.setTextColor(getResources().getColor(debt.getDisabledColor()));
        } else {
            dialog_custom_amount_payment.setVisibility(View.GONE);
        }

        dialog_custom_title.setText(debt.getOwner().getName());

        if (debt.getNote() == null) {
            dialog_custom_content.setText(R.string.cash);
        } else {
            dialog_custom_content.setText(debt.getNote());
        }

        if (debt.hasReminder()) {
            dialog_reminder_date.setVisibility(View.VISIBLE);
            Date remindDate = new Date(debt.getRemindDate());
            SimpleDateFormat simpleDateFormat = Resource.monthDateFormat;
            String time = DateFormat.getTimeInstance(
                    DateFormat.SHORT).format(
                    remindDate);

            Calendar remindCalendar = Calendar.getInstance();
            Calendar now = Calendar.getInstance();

            remindCalendar.setTime(remindDate);

            boolean today = (now.get(Calendar.DAY_OF_MONTH) == remindCalendar.get(Calendar.DAY_OF_MONTH) &&
                            now.get(Calendar.DAY_OF_MONTH) == remindCalendar.get(Calendar.DAY_OF_MONTH) &&
                    now.get(Calendar.DAY_OF_MONTH) == remindCalendar.get(Calendar.DAY_OF_MONTH));


            dialog_reminder_date.setText(String.format(getString(R.string.reminder_detail), today ? getString(R.string.today).toLowerCase() : simpleDateFormat.format(remindDate), time));
        } else {
            dialog_reminder_date.setVisibility(View.GONE);
        }

        if (debt.isPaidBack()) {
            dialog_custom_payback.setText(R.string.undo_pay_back);
            dialog_custom_payback.setTextColor(getResources().getColor(R.color.red));

            dialog_custom_payment.setVisibility(View.GONE);

            Long datePaidBack = debt.getDatePaidBack();
            if (datePaidBack != null) {
                dialog_paid_back_date.setVisibility(View.VISIBLE);

                SimpleDateFormat simpleDateFormat = Resource.monthDateFormat;

                Calendar target = Calendar.getInstance();
                target.setTimeInMillis(datePaidBack);

                dialog_paid_back_date.setText(getString(R.string.paid_back_when, simpleDateFormat.format(target.getTime())));
            }
        } else {
            dialog_custom_payback.setText(R.string.pay_back);
            dialog_custom_payback.setTextColor(getResources().getColor(R.color.button_color));

            dialog_custom_payment.setVisibility(View.VISIBLE);

            dialog_paid_back_date.setVisibility(View.GONE);
        }

        dialog_custom_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, debt.getShareString(getActivity(), data.preferences.getCurrency()));
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, Resource.isLOrAbove() ? debt.getShareString(getActivity(), data.preferences.getCurrency()) : getString(R.string.share)));
            }
        });

        dialog_custom_payback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean payback = !debt.isPaidBack();
                if (payback) {
                    debt.payback();
                } else {
                    debt.unpayback();
                }

                if (payback && debt.hasReminder()) {
                    Alarm.cancelAlarm(getActivity(), debt);
                    debt.setRemindDate(null);
                }

                storage.commit(getActivity());
                displayPaybackAnimation();
            }
        });

        dialog_custom_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NumberPickerBuilder builder = new NumberPickerBuilder()
                        .setLabelText(data.preferences.getCurrency().getDisplayName())
                        .setFragmentManager(((AppCompatActivity) getActivity()).getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment_CustomLight)
                        .setPlusMinusVisibility(View.INVISIBLE)
                        .setMaxNumber((int) Math.ceil(debt.getRemainingAbsoluteDebt()));


                builder.addNumberPickerDialogHandler(new NumberPickerDialogFragment.NumberPickerDialogHandler() {
                    @Override
                    public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
                        debt.addPayment(fullNumber);
                        storage.commit(getActivity());

                        if (debt.isPaidBack()) {
                            displayPaybackAnimation();
                        } else {
                            if (callback != null) {
                                callback.onRefresh();
                            }
                            alertDialog.dismiss();
                        }
                    }
                });

                builder.show();
            }
        });

        detailDialogOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                popupMenu.inflate(R.menu.detail_dialog_popup);

                detailMenuPay = popupMenu.getMenu().findItem(R.id.detail_dialog_pay_back);

                if (debt.getAmount() < 0) {
                    if (SwishLauncher.hasService(getActivity().getPackageManager())) {
                        detailMenuPay.setEnabled(true);
                    } else {
                        detailMenuPay.setEnabled(false);
                    }
                } else {
                    detailMenuPay.setVisible(false);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.detail_dialog_edit:
                                if (callback != null) {
                                    callback.onEdit(debt);
                                }
                                alertDialog.dismiss();

                                return true;
                            case R.id.detail_dialog_delete:
                                if (callback != null) {
                                    callback.onDelete(debt);
                                }
                                alertDialog.dismiss();

                                return true;
                            case R.id.detail_dialog_change:

                                PersonPickerDialogFragment personPickerDialogFragment = new PersonPickerDialogFragment();

                                Bundle args = new Bundle();
                                args.putString(PersonPickerDialogFragment.TITLE_KEY, PersonPickerDialogFragment.USE_DEFAULT_TITLE);
                                args.putBoolean(PersonPickerDialogFragment.PEOPLE_KEY, true);
                                personPickerDialogFragment.setArguments(args);

                                FragmentManager fm = getFragmentManager();
                                if (fm != null) {
                                    personPickerDialogFragment.show(fm, "person_dialog");
                                    personPickerDialogFragment.completeCallback = changePersonCallback;
                                }

                                alertDialog.dismiss();
                                return true;

                            case R.id.detail_dialog_pay_back:
                                SwishLauncher.startSwish(getActivity(), debt.getRemainingAbsoluteDebt(), debt.getOwner());
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });
	}

    public void displayPaybackAnimation() {
        PaidBackDialogFragment paidBackDialogFragment = PaidBackDialogFragment.newInstance(
                debt.isPaidBack() ? PaidBackDialogFragment.PAY_BACK : PaidBackDialogFragment.UNDO_PAY_BACK, false);

        paidBackDialogFragment.show(getFragmentManager().beginTransaction(), "paid_back_dialog");
        paidBackDialogFragment.completeCallback = new PaidBackDialogFragment.CompleteCallback() {
            @Override
            public void onComplete() {
                if (callback != null) {
                    callback.onRefresh();
                }
            }
        };

        alertDialog.dismiss();
    }

	public PersonPickerDialogFragment.PersonSelectedCallback changePersonCallback = new PersonPickerDialogFragment.PersonSelectedCallback() {
		@Override
		public void onSelected(String name) {
			if (callback != null) {
				callback.onMove(debt, data.findPersonByName(name));
			}
		}
	};

	public interface Callback {
		void onRefresh();
		void onDelete(Debt debt);
		void onEdit(Debt debt);
		void onMove(Debt debt, Person person);
	}


}