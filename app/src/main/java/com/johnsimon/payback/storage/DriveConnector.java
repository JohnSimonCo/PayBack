package com.johnsimon.payback.storage;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.johnsimon.payback.async.NullPromise;

public class DriveConnector implements GoogleApiClient.ConnectionCallbacks {

	public final GoogleApiClient client;
	public final NullPromise connectedPromise = new NullPromise();

	public DriveConnector(Activity activity) {
		client = new GoogleApiClient.Builder(activity)
			.addApi(Drive.API)
					//TODO innan release: använda app folder
			.addScope(Drive.SCOPE_FILE)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
				@Override
				public void onConnectionFailed(ConnectionResult connectionResult) {
					int i = 0;
				}
			})
			.build();

		client.connect();
	}

	@Override
	public void onConnected(Bundle bundle) {
		connectedPromise.fire();
	}

	@Override
	public void onConnectionSuspended(int i) {}
}
