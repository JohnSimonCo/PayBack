package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.nispok.snackbar.Snackbar;

public class DriveStorage implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_CODE_RESOLUTION = 14795;

    private GoogleApiClient client;
    private Activity context;

    public DriveStorage(Activity context) {
        this.context = context;

        client = new GoogleApiClient.Builder(context)
           .addApi(Drive.API)
           .addScope(Drive.SCOPE_APPFOLDER)
           .addConnectionCallbacks(this)
           .addOnConnectionFailedListener(this)
           .build();
    }

    public void connect() {
        client.connect();
    }

    public void disconnect() {
        client.disconnect();
    }

    public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == Activity.RESULT_OK) {
                    client.connect();
                }
                return true;
        }
        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Drive.DriveApi.newDriveContents(client)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            show("Error while trying to create new file contents");
                            return;
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("data.json")
                            .setMimeType("text/json")
                            .build();
                        Drive.DriveApi.getAppFolder(client)
                                .createFile(client, changeSet, result.getDriveContents())
                                .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                    @Override
                                    public void onResult(DriveFolder.DriveFileResult result) {
                                        if (!result.getStatus().isSuccess()) {
                                            show("Error while trying to create the file");
                                            return;
                                        }
                                        show("Created a file in App Folder: "
                                                + result.getDriveFile().getDriveId());
                                    }
                                });
                    }
                });

    }

    private void show(String text) {
        Snackbar.with(context)
                .text(text)
                .show(context);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(context, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
                show("Shit fuckd up");
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), context, 0).show();
        }
    }
}
