package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import com.johnsimon.payback.R;

import java.util.List;

public class SwishLauncher {

	public static void startSwish(Activity activity, String amount, String phoneNumber) {
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

	public static boolean hasService(Context ctx) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setComponent(ComponentName.unflattenFromString("se.bankgirot.swish/.ui.PaymentActivity"));

		List<ResolveInfo> list = ctx.getPackageManager().queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);

		return list.size() > 0;
	}

}
