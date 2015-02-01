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

	public Promise<LoginResult> loginResult = new Promise<>();
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
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
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

					String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

					loginResult.fire(new LoginResult(accountName));
				} else if(resultCode == Activity.RESULT_CANCELED) {
					loginResult.fire(new LoginResult());
				}
				return true;
		}
		return false;
	}

	public static class LoginResult {
		public boolean success = false;
		public String accountName = null;

		//Cancelled
		public LoginResult() {
		}

		//Success
		public LoginResult(String accountName) {
			success = true;
			this.accountName = accountName;
		}
	}
}
