package com.johnsimon.payback.storage;

import android.content.Context;
import android.os.Bundle;

	import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.plus.Plus;
import com.johnsimon.payback.BuildConfig;
import com.johnsimon.payback.async.NullPromise;
import com.johnsimon.payback.util.Resource;

public class DriveConnector implements GoogleApiClient.ConnectionCallbacks {

	public final GoogleApiClient client;
	public final NullPromise connectedPromise = new NullPromise();

	public DriveConnector(Context context) {
        client = new GoogleApiClient.Builder(context)
            .addApi(Drive.API)
			.addApi(Plus.API) //Dirty AF
			.addScope(Drive.SCOPE_APPFOLDER)
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
