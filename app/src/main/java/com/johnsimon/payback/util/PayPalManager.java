package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.johnsimon.payback.R;
import com.johnsimon.payback.async.NullCallback;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.ui.dialog.DebtDetailDialogFragment;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalPayment;

import java.math.BigDecimal;
import java.util.Locale;

public class PayPalManager {

	public static void init(Context context) {
		PayPal pp = PayPal.getInstance();

		if (pp == null) {  // Test to see if the library is already initialized

			//pp = PayPal.initWithAppID(context, "APP-80W284485P519543T", PayPal.ENV_NONE);
			pp = PayPal.initWithAppID(context, "APP-5ER30931KD693732X", PayPal.ENV_LIVE);

			//pp.setLanguage("en_US");
			pp.setLanguage(Locale.getDefault().toString());

			//pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER);
		}
	}

	/*public static Promise<Boolean> startPayPal(Activity context, Person recipent, BigDecimal amount, String currency) {
		Promise<Boolean> p = new Promise<>();

		if (recipent.link.hasNumbers()
	}*/

	public static Promise<PaymentResult> requestPayment(Activity activity, String recipent, BigDecimal amount, String currency) {

		PayPalPayment payment = new PayPalPayment();
		//payment.setSubtotal(new BigDecimal(1));
		payment.setSubtotal(amount);

		//payment.setCurrencyType("USD");
		payment.setCurrencyType(currency);

		//payment.setRecipient("johnsimondev@gmail.com");
		payment.setRecipient(recipent);

		payment.setMerchantName(activity.getString(R.string.app_name));
		Intent checkout = PayPal.getInstance().checkout(payment, activity);
		activity.startActivityForResult(checkout, REQUEST_CODE);

		resultPromise = new Promise<>();
		return resultPromise;
	}

	private static Promise<PaymentResult> resultPromise;

	public final static int REQUEST_CODE = 634; //Totally random (mashed my keyboard)

	public static boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(requestCode != REQUEST_CODE) {
			return false;
		}

		switch (resultCode) {
			case Activity.RESULT_OK:

				resultPromise.fire(PaymentResult.Successful);
				/*
				String payKey = intent.getStringExtra(PayPalActivity.EXTRA_PAY_KEY);
				//this.paymentSucceeded(payKey);
				*/
				break;

			case Activity.RESULT_CANCELED:
				resultPromise.fire(PaymentResult.Canceled);
				//this.paymentCanceled();
				break;



			case PayPalActivity.RESULT_FAILURE:
				resultPromise.fire(PaymentResult.Error);
				//String errorID = intent.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
				//String errorMessage = intent.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE);
				//this.paymentFailed(errorID, errorMessage);
				break;

			default:
				resultPromise.fire(PaymentResult.Canceled);
				break;
		}

		return true;
	}
}