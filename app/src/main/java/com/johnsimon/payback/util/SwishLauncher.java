package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.R;
import com.johnsimon.payback.data.Person;

import java.util.List;

public class SwishLauncher {

    public static void startSwish(final Activity activity, final float amount, final Person owner) {
        if (owner.hasNumbers()) {

            String[] numbers = owner.link.numbers;

            if (numbers.length > 1) {

                new MaterialDialog.Builder(activity)
                        .title(R.string.phone_number)
                        .items(numbers)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence number) {
                                SwishLauncher.startSwishApp(activity, amount, number.toString());
                            }
                        })
                        .show();
            } else {
                SwishLauncher.startSwishApp(activity, amount, numbers[0]);
            }
        } else {
            SwishLauncher.startSwishApp(activity, amount);
        }
    }

    private static void startSwishApp(Activity activity, float amount) {
        startSwishApp(activity, amount, null);
    }

	private static void startSwishApp(Activity activity, float amount, String phoneNumber) {
        startSwishApp(activity, amountToString(amount), phoneNumber);
    }

	private static void startSwishApp(Activity activity, String amount, String phoneNumber) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setComponent(ComponentName.unflattenFromString("se.bankgirot.swish/.ui.PaymentActivity"));
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		intent.putExtra("phone", phoneNumber);
		intent.putExtra("amount", amount); //one can hope...

		activity.startActivity(intent);

		ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText(activity.getString(R.string.amount), amount);
		clipboard.setPrimaryClip(clip);

		Toast.makeText(activity, activity.getString(R.string.amount) + " " + "\"" + amount + "\"" + " " + activity.getString(R.string.swish_copy_toast_end), Toast.LENGTH_LONG).show();

	}

    private static String amountToString(float amount) {
        return Float.toString(Math.abs(amount)).replaceAll("\\.0*$", "");
    }

	public static boolean hasService(PackageManager pkm) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setComponent(ComponentName.unflattenFromString("se.bankgirot.swish/.ui.PaymentActivity"));

		List<ResolveInfo> list = pkm.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);

		return list.size() > 0;
	}

}
