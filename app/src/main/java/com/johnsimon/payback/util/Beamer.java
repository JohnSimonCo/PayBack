package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;

import com.google.gson.Gson;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.core.User;
import com.johnsimon.payback.send.DebtSendable;
import com.johnsimon.payback.send.NfcData;
import com.johnsimon.payback.ui.FeedActivity;
import com.williammora.snackbar.Snackbar;

import java.util.ArrayList;

import static android.nfc.NdefRecord.createMime;

public class Beamer implements NfcAdapter.CreateNdefMessageCallback {
	private FeedActivity activity;

	public Beamer(FeedActivity activity) {
		this.activity = activity;
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		return FeedActivity.isAll() ? null : createMessage(FeedActivity.feed);
	}

	public void processIntent(Intent intent) {
		if(intent.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)) {
			NfcData data = readMessage((NdefMessage) intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)[0]);
			activity.onRecievedBeam(data.debts, data.sender);

			intent.removeExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		}
	}

	private NdefMessage createMessage(ArrayList<Debt> debts) {
		String JSON = new Gson().toJson(new NfcData(debts), NfcData.class);
		return new NdefMessage(
				new NdefRecord[]{
						createMime("application/vnd.com.johnsimon.payback", JSON.getBytes()),
						NdefRecord.createApplicationRecord("com.johnsimon.payback")
				});
	}

	private NfcData readMessage(NdefMessage message) {
		NdefRecord[] records = message.getRecords();
		String JSON = new String(records[0].getPayload());

		// record 0 contains the MIME type, record 1 is the AAR, if present
		//textView.setText(new String(msg.getRecords()[0].getPayload()));

		return new Gson().fromJson(JSON, NfcData.class);
	}
	/*
	private NdefRecord createRecord(String contents) {
		return createMime("application/vnd.com.johnsimon.payback", contents.getBytes());
	}
	private String getContents(NdefRecord record) {
		return new String(record.getPayload());
	}
	*/

	public interface BeamListener {
		void onRecievedBeam(DebtSendable[] debts, User sender);
	}
}
