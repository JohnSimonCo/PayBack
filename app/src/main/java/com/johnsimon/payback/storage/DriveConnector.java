package com.johnsimon.payback.storage;

import android.content.Context;
import android.os.Bundle;

	import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.johnsimon.payback.async.NullPromise;

public class DriveConnector implements GoogleApiClient.ConnectionCallbacks {

	public final GoogleApiClient client;
	public final NullPromise connectedPromise = new NullPromise();

	public DriveConnector(Context context) {
		client = new GoogleApiClient.Builder(context)
			.addApi(Drive.API)
					//TODO innan release: använda app folder
			.addScope(Drive.SCOPE_FILE)
			.addConnectionCallbacks(this)
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
