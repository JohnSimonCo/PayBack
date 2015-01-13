package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;

import com.johnsimon.payback.core.Debt;

public class SwishLauncher {

	public static void startSwish(Activity activity, Debt debt) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setComponent(ComponentName.unflattenFromString("se.bankgirot.swish/.ui.PaymentActivity"));
		intent.addCategory(Intent.CATEGORY_LAUNCHER );

		intent.putExtra("phone", "123");

		activity.startActivity(intent);
	}

}
