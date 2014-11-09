package com.johnsimon.payback.util;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;

import com.google.gson.Gson;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.ui.FeedActivity;
import com.williammora.snackbar.Snackbar;

import java.nio.charset.Charset;
import java.util.ArrayList;

import static android.nfc.NdefRecord.createMime;

public class Beamer implements NfcAdapter.CreateNdefMessageCallback {
	private Context context;

	public Beamer(Context ctx) {
		this.context = ctx;
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		return FeedActivity.isAll() ? null : createMessage(FeedActivity.feed);
	}

	public void processNdefMessage(NdefMessage message, Activity activity) {
		Debt[] debts = readMessage(message);

        Snackbar.with(activity.getApplicationContext())
                .text(activity.getString(R.string.beamer_1) + " " + debts.length + " " + activity.getString(R.string.beamer_2))
                .show(activity);
	}


	private NdefRecord createRecord(String contents) {
		return createMime("application/vnd.com.johnsimon.payback", contents.getBytes());
	}

	private String getContents(NdefRecord record) {
		return new String(record.getPayload());
	}

	private NdefMessage createMessage(ArrayList<Debt> feed) {
		Debt[] debts = feed.toArray(new Debt[feed.size()]);
		String JSON = new Gson().toJson(debts, Debt[].class);
		return new NdefMessage(
				new NdefRecord[]{
						createMime("application/vnd.com.johnsimon.payback", JSON.getBytes()),
						NdefRecord.createApplicationRecord("com.johnsimon.payback")
						/**
						 * The Android Application Record (AAR) is commented out. When a device
						 * receives a push with an AAR in it, the application specified in the AAR
						 * is guaranteed to run. The AAR overrides the tag dispatch system.
						 * You can add it back in to guarantee that this
						 * activity starts when receiving a beamed message. For now, this code
						 * uses the tag dispatch system.
						 */

				});
	}

	private Debt[] readMessage(NdefMessage message) {
		NdefRecord[] records = message.getRecords();
		String JSON = getContents(records[0]);

		// record 0 contains the MIME type, record 1 is the AAR, if present
		//textView.setText(new String(msg.getRecords()[0].getPayload()));

		return new Gson().fromJson(JSON, Debt[].class);
	}

}
