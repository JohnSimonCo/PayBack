package com.johnsimon.payback.storage;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.johnsimon.payback.async.NullPromise;
import com.johnsimon.payback.async.Promise;

public class DriveLoginManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private final static int REQUEST_CODE_RESOLUTION = 14795;

	public final static String PREFERENCE_ACCOUNT_NAME = "ACCOUNT_NAME";

	private GoogleApiClient client;
	private Activity activity;

	private boolean hasLoggedIn = false;

	public Promise<Boolean> loginResult = new Promise<>();
	public NullPromise connectedPromise = new NullPromise();

	public DriveLoginManager(Activity activity) {
		this.activity = activity;
	}

	public GoogleApiClient getClient() {
		return client;
	}

	public void go() {
		this.client = new GoogleApiClient.Builder(activity)
			.addApi(Drive.API)
					//TODO innan release: använda app folder
			.addScope(Drive.SCOPE_FILE)
			.build();

		client.connect();
	}

	public void go(GoogleApiClient client) {
		this.client = client;

		if(!client.isConnectionCallbacksRegistered(this)) {
			client.registerConnectionCallbacks(this);
			client.registerConnectionFailedListener(this);
		}

		if(client.isConnected()) {
			onConnected(null);
		} else {
			client.connect();
		}
	}

	public void disconnect() {
		client.disconnect();
	}

	@Override
	public void onConnected(Bundle bundle) {
		if(hasLoggedIn) {
			connectedPromise.fire();
		} else {
			client.clearDefaultAccountAndReconnect();
		}
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (activity.isFinishing())
			return;

		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
			} catch (IntentSender.SendIntentException e) {
				// Unable to resolve, message user appropriately
				//client.connect();
			}
		} else {
			GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
		}
	}

	public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		switch (requestCode) {
			case REQUEST_CODE_RESOLUTION:
				if (resultCode == Activity.RESULT_OK) {
					hasLoggedIn = true;
					client.connect();

					//A bit of a hack, but it works :)
					String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

					StorageManager.getPreferences(activity).edit().putString(PREFERENCE_ACCOUNT_NAME, accountName).apply();

					loginResult.fire(true);
				} else if(resultCode == Activity.RESULT_CANCELED) {
					loginResult.fire(false);
				}
				return true;
		}
		return false;
	}
}
