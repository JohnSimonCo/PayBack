package com.johnsimon.payback.storage;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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
import com.johnsimon.payback.util.AppData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DriveStorage extends Storage implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final static int REQUEST_CODE_RESOLUTION = 14795;
    private final static String FILE_NAME = "data.json";

    private Activity activity;

    private GoogleApiClient client;

    private DriveFile file = null;

    public DriveStorage(Activity context) {
        super(context);

        activity = context;

        client = new GoogleApiClient.Builder(context)
           .addApi(Drive.API)
           .addScope(Drive.SCOPE_APPFOLDER)
           .addConnectionCallbacks(this)
           .addOnConnectionFailedListener(this)
           .build();
    }


    @Override
    public void commit() {
        write(data.save(), file, new ResultCallback<FileResult>() {
            @Override
            public void onResult(FileResult result) {
                if(!result.getStatus().isSuccess()) {
                    show("Error when commiting");
                    return;
                }
            }
        });
    }

    @Override
    public void connect() {
        client.connect();
    }

    @Override
    public void disconnect() {
        client.disconnect();
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
                        int count = buffer.getCount();
                        if(count > 0) {
                            Metadata data = buffer.get(0);
                            show("File exists " + count);

                            read(data.getDriveId(), fileFoundCallback);
                        } else {
                            show("File doesn't exists");

                            emit(new AppData());

                            createFile(data.save(), fileCreatedCallback);
                        }
                        buffer.release();
                    }
                });
    }

    private DriveStorage self = this;
    private ResultCallback<FileResult> fileFoundCallback = new ResultCallback<FileResult>() {
        @Override
        public void onResult(FileResult result) {
            if(!result.getStatus().isSuccess()) {
                show("Error when reading file");
                return;
            }

            self.file = result.getFile();

            String JSON = result.getText();

            show("File says " + JSON);

            emit(new AppData(JSON));
        }
    };

    private ResultCallback<FileResult> fileCreatedCallback = new ResultCallback<FileResult>() {
        @Override
        public void onResult(FileResult result) {
            if (!result.getStatus().isSuccess()) {
                show("Error while trying to write file");
                return;
            }

            self.file = result.getFile();

            show("Created a file in App Folder");
        }
    };

    private void createFile(final String text, final ResultCallback<FileResult> callback) {
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

                                write(text, result.getDriveFile(), callback);
                            }
                        });
                }
            });
    }

    private void read(final DriveId id, ResultCallback<FileResult> callback) {
        read(Drive.DriveApi.getFile(client, id), callback);
    }

    private void write(final String text, final DriveFile file, final ResultCallback<FileResult> callback) {
        file.open(client, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    callback.onResult(new FileResult(result.getStatus()));
                    return;
                }

                DriveContents contents = result.getDriveContents();
                try {
                    contents.getOutputStream().write(text.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                contents.commit(client, null).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        callback.onResult(new FileResult(file, text, status));
                    }
                });
            }
        });

    }

    private void read(final DriveFile file, final ResultCallback<FileResult> callback) {
        file.open(client, DriveFile.MODE_READ_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    callback.onResult(new FileResult(result.getStatus()));
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
                callback.onResult(new FileResult(file, builder.toString(), result.getStatus()));
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        int j = 0;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
                show("Shit fuckd up");
                //client.connect();
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
        }
    }

    @Override
    public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == Activity.RESULT_OK) {
                    client.connect();
                }
                return true;
        }
        return super.handleActivityResult(requestCode, resultCode, data);
    }

    private static class FileResult implements Result {
        private DriveFile file;
        private String text;
        private Status status;

        public FileResult(DriveFile file, String text, Status status) {
            this.file = file;
            this.text = text;
            this.status = status;
        }

        public FileResult(Status status) {
            this(null, null, status);
        }

        public DriveFile getFile() {
            return file;
        }

        public String getText() {
            return text;
        }

        @Override
        public Status getStatus() {
            return status;
        }
    }
}
