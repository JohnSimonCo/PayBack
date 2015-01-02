package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.CursorJoiner;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.nispok.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class DriveStorage implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final static int REQUEST_CODE_RESOLUTION = 14795;
    private final static String FILE_NAME = "data.json";

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
        Query query = new Query.Builder()
            .addFilter(Filters.eq(SearchableField.TITLE, FILE_NAME))
            .build();

        Drive.DriveApi.getAppFolder(client)
                .queryChildren(client, query)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult result) {
                        if (!result.getStatus().isSuccess()) {
                            show("Error listing children");
                            return;
                        }

                        MetadataBuffer buffer = result.getMetadataBuffer();
                        if(buffer.getCount() > 0) {
                            for(int i = 0, c = buffer.getCount(); i < c; i++) {
                                Metadata data = buffer.get(i);
                                DriveId id = data.getDriveId();
                                show("File exists " + c);
                                readFile(id, new ResultCallback<StringResult>() {
                                    @Override
                                    public void onResult(StringResult result) {
                                        if(!result.getStatus().isSuccess()) {
                                            show("Error when reading file");
                                            return;
                                        }

                                        show("File says " + result.getString());
                                    }
                                });
                            }
                        } else {
                            show("File doesn't exists");
                            createFile("Hej hopp", new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    if (!status.isSuccess()) {
                                        show("Error while trying to write file");
                                        return;
                                    }

                                    show("Created a file in App Folder");

                                    /*
                                    read(file, new ResultCallback<StringResult>() {
                                        @Override
                                        public void onResult(StringResult result) {
                                            if (!result.getStatus().isSuccess()) {
                                                show("Error while trying to read file");
                                                return;
                                            }

                                            show("Read a file in App Folder: " + result.getString());
                                        }
                                    });
                                    */
                                }
                            });
                        }
                    }
                });
    }

    private void createFile(final String text, final ResultCallback<Status> callback) {
        Drive.DriveApi.newDriveContents(client)
            .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        show("Error while trying to create new file contents");
                        return;
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(FILE_NAME)
                        .setMimeType("text/json")
                        .build();


                    Drive.DriveApi.getAppFolder(client)
                        .createFile(client, changeSet, result.getDriveContents())
                        .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                            @Override
                            public void onResult(DriveFolder.DriveFileResult result) {
                                if (!result.getStatus().isSuccess()) {
                                    show("Error while trying to create file");
                                    return;
                                }

                                final DriveFile file = result.getDriveFile();

                                show("Created a file in App Folder: " + file.getDriveId());

                                write(text, file, callback);

                            }
                        });
                }
            });
    }

    private void readFile(final DriveId id, ResultCallback<StringResult> callback) {
        read(Drive.DriveApi.getFile(client, id), callback);
    }

    private void write(final String text, DriveFile file, final ResultCallback<Status> callback) {
        file.open(client, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    callback.onResult(result.getStatus());
                    return;
                }

                DriveContents contents = result.getDriveContents();
                try {
                    contents.getOutputStream().write(text.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                contents.commit(client, null).setResultCallback(callback);
            }
        });

    }

    private void read(DriveFile file, final ResultCallback<StringResult> callback) {
        file.open(client, DriveFile.MODE_READ_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult result) {
                if(!result.getStatus().isSuccess()) {
                    callback.onResult(new StringResult(null, result.getStatus()));
                    return;
                }

                DriveContents contents = result.getDriveContents();
                BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()));
                StringBuilder builder = new StringBuilder();
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                callback.onResult(new StringResult(builder.toString(), result.getStatus()));
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

    private static class StringResult implements Result {
        private String string;
        private Status status;

        public StringResult(String string, Status status) {
            this.string = string;
            this.status = status;
        }

        public String getString() {
            return string;
        }

        @Override
        public Status getStatus() {
            return status;
        }
    }
}
