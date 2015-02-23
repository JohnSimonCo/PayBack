package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.gson.Gson;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.ui.dialog.PaidBackDialogFragment;
import com.johnsimon.payback.ui.dialog.PersonPickerDialogFragment;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.SwishLauncher;
import com.makeramen.RoundedImageView;

public class DebtDetailDialogActivity extends DataActivity implements PaidBackDialogFragment.CompleteCallback {

    public Callback callback = null;
    public MenuItem detailMenuPay;

    private TextView dialog_custom_amount;

    public static Debt debt;
    private Person person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.detail_dialog);

        debt = new Gson().fromJson(getIntent().getStringExtra("debt"), Debt.class);
        person = new Gson().fromJson(getIntent().getStringExtra("person"), Person.class);

        Button dialog_custom_confirm = (Button) findViewById(R.id.dialog_custom_confirm);
        Button dialog_custom_cancel = (Button) findViewById(R.id.dialog_custom_cancel);

        if (debt.isPaidBack()) {
            dialog_custom_confirm.setText(R.string.undo_pay_back);
            dialog_custom_confirm.setTextColor(getResources().getColor(R.color.red));
        }

        //This is the share button
        dialog_custom_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, debt.getShareString(DebtDetailDialogActivity.this, data.preferences.getCurrency()));
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, Resource.isLOrAbove() ? debt.getShareString(DebtDetailDialogActivity.this, data.preferences.getCurrency()) : getString(R.string.share)));
            }
        });

        dialog_custom_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PaidBackDialogFragment paidBackDialogFragment;

                if (debt.isPaidBack()) {
                    paidBackDialogFragment = PaidBackDialogFragment.newInstance(PaidBackDialogFragment.UNDO_PAY_BACK, debt, false);
                } else {
                    paidBackDialogFragment = PaidBackDialogFragment.newInstance(PaidBackDialogFragment.PAY_BACK, debt, false);
                }
                paidBackDialogFragment.show(getFragmentManager().beginTransaction(), "paid_back_dialog");
                paidBackDialogFragment.completeCallback = DebtDetailDialogActivity.this;

                finish();
            }
        });

        dialog_custom_amount = (TextView) findViewById(R.id.dialog_custom_amount);

        TextView dialog_custom_title = (TextView) findViewById(R.id.dialog_custom_title);
        TextView dialog_custom_content = (TextView) findViewById(R.id.dialog_custom_content);

        dialog_custom_title.setText(person.getName());

        if (debt.getNote() == null) {
            dialog_custom_content.setText(R.string.cash);
        } else {
            dialog_custom_content.setText(debt.getNote());
        }

        ImageButton detailDialogOverflow = (ImageButton) findViewById(R.id.detail_dialog_overflow);
        detailDialogOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(DebtDetailDialogActivity.this, v);
                popupMenu.inflate(R.menu.detail_dialog_popup);

                detailMenuPay = popupMenu.getMenu().findItem(R.id.detail_dialog_pay_back);

                if (debt.getAmount() < 0) {
                    if (SwishLauncher.hasService(getPackageManager())) {
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
                                finish();

                                return true;
                            case R.id.detail_dialog_delete:
                                if (callback != null) {
                                    callback.onDelete(debt);
                                }
                                finish();

                                return true;
                            case R.id.detail_dialog_change:

                                PersonPickerDialogFragment personPickerDialogFragment = new PersonPickerDialogFragment();

                                Bundle args = new Bundle();
                                args.putString(PersonPickerDialogFragment.TITLE_KEY, PersonPickerDialogFragment.USE_DEFAULT_TITLE);
                                args.putBoolean(PersonPickerDialogFragment.PEOPLE_KEY, true);
                                personPickerDialogFragment.setArguments(args);

                                FragmentManager fm = getFragmentManager();
                                personPickerDialogFragment.show(fm, "person_dialog");
                                personPickerDialogFragment.completeCallback = changePersonCallback;

                                finish();
                                return true;

                            case R.id.detail_dialog_pay_back:
                                SwishLauncher.startSwish(DebtDetailDialogActivity.this, debt.getAmount(), person);
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        RoundedImageView avatar = (RoundedImageView) findViewById(R.id.detail_dialog_avatar);
        TextView avatarLetter = (TextView) findViewById(R.id.detail_dialog_avatar_letter);

        Resource.createProfileImage(
                this,
                avatar,
                avatarLetter,
                getIntent().getBooleanExtra("detailHasImage", false),
                getIntent().getStringExtra("detailPhotoUri"),
                getIntent().getIntExtra("detailPaletteIndex", 0),
                getIntent().getStringExtra("detailAvatarLetter"));

        ViewCompat.setTransitionName(dialog_custom_title, "person" + getIntent().getIntExtra("pos", -1));
        ViewCompat.setTransitionName(avatar, "avatar" + getIntent().getIntExtra("pos", -1));
        ViewCompat.setTransitionName(avatarLetter, "avatarLetter" + getIntent().getIntExtra("pos", -1));
        ViewCompat.setTransitionName(dialog_custom_amount, "amount" + getIntent().getIntExtra("pos", -1));
        ViewCompat.setTransitionName(dialog_custom_content, "note" + getIntent().getIntExtra("pos", -1));

    }

	@Override
	protected void onDataReceived() {
		if (debt.getAmount() < 0) {
			//negative
			dialog_custom_amount.setText(data.preferences.getCurrency().render(debt));
			dialog_custom_amount.setTextColor(getResources().getColor(debt.getColor()));
		} else {
			dialog_custom_amount.setText(data.preferences.getCurrency().render(debt));
			dialog_custom_amount.setTextColor(getResources().getColor(R.color.green_strong));
		}
	}

    @Override
    public void onComplete(Debt _debt) {
        if (callback != null) {
            callback.onPaidBack(_debt);
        }
    }

    @Override
    protected void onDestroy() {
        debt = null;
        super.onDestroy();
    }

    public interface Callback {
		public void onPaidBack(Debt debt);
		public void onDelete(Debt debt);
		public void onEdit(Debt debt);
		public void onMove(Debt debt, Person person);
	}

    public PersonPickerDialogFragment.PersonSelectedCallback changePersonCallback = new PersonPickerDialogFragment.PersonSelectedCallback() {
        @Override
        public void onSelected(String name) {
            if (callback != null) {
                callback.onMove(debt, data.findPersonByName(name));
            }
        }
    };
}