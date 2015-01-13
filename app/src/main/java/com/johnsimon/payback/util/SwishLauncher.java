package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;

public class SwishLauncher {

	public static void startSwish(Activity activity) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setComponent(ComponentName.unflattenFromString("se.bankgirot.swish/.ui.PaymentActivity"));
		intent.addCategory(Intent.CATEGORY_LAUNCHER );

		intent.putExtra("phone", "1234");
		intent.putExtra("message", "bajs");

		startActivity(intent);
	}

}
