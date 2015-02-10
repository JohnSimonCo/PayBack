package com.johnsimon.payback.storage;

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
import com.johnsimon.payback.async.NullCallback;
import com.johnsimon.payback.async.NullPromise;
import com.johnsimon.payback.data.AppData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DriveStorage extends ExternalStorage {
	//TODO loading toast
	private final static String FILE_NAME = "data.json";

    private GoogleApiClient client;

    private DriveFile file = null;

    public DriveStorage(GoogleApiClient client, final LocalStorage localStorage) {
		super(localStorage);

		this.client = client;
	}

	public void listen(NullPromise connected) {
		connected.then(new NullCallback() {
			@Override
			public void onCalled() {
				refresh();
			}
		});
	}

	public GoogleApiClient getClient() {
		return client;
	}


	@Override
    protected void commitExternally(String JSON) {
        if(file == null) return;
        write(JSON, file, new ResultCallback<FileResult>() {
            @Override
            public void onResult(FileResult result) {
                if(!result.getStatus().isSuccess()) {
                    error("Error when commiting", result.getStatus());
                    return;
                }
                show("commited data to drive");
            }
        });
    }

	public void refresh() {
		lastRefresh = System.currentTimeMillis();
		Drive.DriveApi.requestSync(client).setResultCallback(requestSyncCallback);
	}

	@Override
	public void requestRefresh() {
		if(client.isConnected() && mayRefresh()) {
			refresh();
		}
	}

	private final static long MAX_REFRESH_FREQ = 20000;
	private long lastRefresh;
	private boolean mayRefresh() {
		return System.currentTimeMillis() - lastRefresh > MAX_REFRESH_FREQ;
	}

	private ResultCallback<Status> requestSyncCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            if(!status.isSuccess()) {
                show("Sync error");
            }

			if(!client.isConnected()) return;

            Drive.DriveApi.getAppFolder(client)
				.listChildren(client)
				.setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
					@Override
					public void onResult(DriveApi.MetadataBufferResult result) {
						if (!result.getStatus().isSuccess()) {
							error("Error listing children", result.getStatus());
							return;
						}

						MetadataBuffer buffer = result.getMetadataBuffer();
						int count = buffer.getCount();
						if (count > 0) {
							//File exists
							Metadata data = buffer.get(0);
							read(data.getDriveId(), fileFoundCallback);
						} else {
							createFile(data.save(), fileCreatedCallback);
						}
						buffer.release();
					}
				});
        }
    };

    private ResultCallback<FileResult> fileFoundCallback = new ResultCallback<FileResult>() {
        @Override
        public void onResult(FileResult result) {
            if(!result.getStatus().isSuccess()) {
                error("Error when reading file", result.getStatus());
                return;
            }

			setFile(result.getFile());

            String JSON = result.getText();

            sync(AppData.fromJson(JSON));
        }
    };

    private ResultCallback<FileResult> fileCreatedCallback = new ResultCallback<FileResult>() {
        @Override
        public void onResult(FileResult result) {
            if (!result.getStatus().isSuccess()) {
                error("Error while trying to write file", result.getStatus());
                return;
            }

			setFile(result.getFile());

            show("Created a file");
        }
    };


	private void setFile(DriveFile file) {
		this.file = file;
	}

    private void createFile(final String text, final ResultCallback<FileResult> callback) {
        Drive.DriveApi.newDriveContents(client)
            .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        error("Error while trying to create new file contents", result.getStatus());
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
                                    error("Error while trying to create file", result.getStatus());
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
					reader.close();
				} catch (IOException e) {
                    e.printStackTrace();
                }

				callback.onResult(new FileResult(file, builder.toString(), result.getStatus()));
            }
        });
    }

	private boolean keepAlive = false;
    @Override
    public void connect() {
		if(!client.isConnected()) {
			client.connect();
		} else {
			requestRefresh();
			keepAlive = true;
		}
    }

    @Override
    public void disconnect() {
		if(!keepAlive) {
			client.disconnect();
		}
		keepAlive = false;
    }

	protected void error(String title, Status status) {
		error(title, status.getStatus() + ": " + status.getStatusMessage());
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