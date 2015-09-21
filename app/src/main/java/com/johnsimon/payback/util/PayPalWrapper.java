package com.johnsimon.payback.util;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.johnsimon.payback.R;
import com.johnsimon.payback.ui.FeedActivity;
import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalPayment;

import java.math.BigDecimal;
import java.util.Locale;

public class PayPalWrapper {

	public static void init(Context context) {
		PayPal pp = PayPal.getInstance();

		if (pp == null) {  // Test to see if the library is already initialized

			// This main initialization call takes your Context, AppID, and target server
			//Vårt är JR6SKYK836KSQ
			pp = PayPal.initWithAppID(context, "APP-80W284485P519543T", PayPal.ENV_NONE);

			// Required settings:

			// Set the language for the library
			//pp.setLanguage("en_US");
			pp.setLanguage(Locale.getDefault().toString());

			//pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER);

		}
	}

	public static void requestPayment(Activity context, String recipent, BigDecimal amount, String currency) {
		/*
// Create a basic PayPal payment
		PayPalPayment payment = new PayPalPayment();

// Set the currency type
		payment.setCurrencyType("SEK");
//		payment.setCurrencyType(currency);

// Set the recipient for the payment (can be a phone number)
//		payment.setRecipient(recipent);
		payment.setRecipient("swesnowme@gmail.com");

// Set the payment amount, excluding tax and shipping costs
		payment.setSubtotal(new BigDecimal(10));
//		payment.setSubtotal(amount);

		payment.setPaymentType(PayPal.PAYMENT_TYPE_PERSONAL);

		Intent checkout = PayPal.getInstance().checkout(payment, context);

		context.startActivityForResult(checkout, REQUEST_CODE);*/

		PayPalPayment newPayment = new PayPalPayment();
		newPayment.setSubtotal(new BigDecimal(10.f));
		newPayment.setCurrencyType("USD");
		newPayment.setRecipient("my@email.com");
		newPayment.setMerchantName("My Company");
		Intent paypalIntent = PayPal.getInstance().checkout(newPayment, context);
		context.startActivityForResult(paypalIntent, 1);
	}

	public final static int REQUEST_CODE = 123; //Totally random (mashed my keyboard)

	public static boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(requestCode != REQUEST_CODE) {
			return false;
		}

		switch (resultCode) {
			// The payment succeeded
			case Activity.RESULT_OK:
				String payKey = intent.getStringExtra(PayPalActivity.EXTRA_PAY_KEY);
				//this.paymentSucceeded(payKey);
				break;

			// The payment was canceled
			case Activity.RESULT_CANCELED:
				//this.paymentCanceled();
				break;

			// The payment failed, get the error from the EXTRA_ERROR_ID and EXTRA_ERROR_MESSAGE
			case PayPalActivity.RESULT_FAILURE:
				String errorID = intent.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
				String errorMessage = intent.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE);
				//this.paymentFailed(errorID, errorMessage);
		}

		return true;
	}
}
