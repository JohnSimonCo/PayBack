package com.johnsimon.payback.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.johnsimon.payback.R;
import com.johnsimon.payback.async.Background;
import com.johnsimon.payback.async.BackgroundBlock;
import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.data.backup.Backup;
import com.johnsimon.payback.data.backup.BackupManager;
import com.johnsimon.payback.storage.DriveLoginManager;
import com.johnsimon.payback.storage.StorageManager;
import com.johnsimon.payback.ui.dialog.BackupRestoreDialog;
import com.johnsimon.payback.ui.dialog.CurrencyDialogFragment;
import com.johnsimon.payback.util.PermissionManager;
import com.johnsimon.payback.util.Resource;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.Arrays;

//TODO SET BACKGROUND PREF VALUE ONDATARECIEVED TO AVOID BACKUP/RESTORE FUCKUP

public class SettingsActivity extends MaterialPreferenceActivity implements BillingProcessor.IBillingHandler {

    public final static String[] backgroundSettingsEntries = {"mountains", "city"};

    public Preference pref_currency;

    private ListPreference pref_background;

	private Preference pref_cloud_sync_account;
    private Preference pref_import_data;
    private SwitchPreference pref_cloud_sync;
    private SwitchPreference pref_auto_backup;

    public final static String PREFERENCE_CURRENCY = "pref_currency";
    public final static String PREFERENCE_BACKGROUND = "pref_background";
    public final static String PREFERENCE_CLOUD_SYNC = "pref_cloud_sync";
    public final static String PREFERENCE_CLOUD_SYNC_ACCOUNT = "pref_cloud_sync_account";
    public final static String PREFERENCE_WIPE_DATA = "pref_wipe_data";
    public final static String PREFERENCE_EXPORT_DATA = "pref_export_data";
    public final static String PREFERENCE_IMPORT_DATA = "pref_import_data";
    public final static String PREFERENCE_AUTO_BACKUP = "pref_auto_backup";
    public final static String PREFERENCE_CATEGORY_DATA = "pref_category_data";



    private BillingProcessor bp;

	@Override
	protected int getPreferencesXmlId() {
		return R.xml.prefs;
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setEnabledActionBarShadow(true);

        if (Resource.isLOrAbove()) {
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_launcher), getResources().getColor(R.color.primary_color)));

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.primary_color));
        } else {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.primary_color_darker));
        }

		Preference pref_export_data = findPreference(PREFERENCE_EXPORT_DATA);
		pref_export_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
                BackupManager.createBackupAsync(getApplicationContext(), data, Backup.Type.Manual).then(new Callback<Boolean>() {
                    @Override
                    public void onCalled(Boolean backupCreated) {
                        if (backupCreated) {
                            Snackbar.make(masterView, R.string.backup_success, Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(masterView, R.string.backup_failed, Snackbar.LENGTH_LONG).show();
                        }
                        updateBackupStatus();

                    }
                });
				return true;
			}
		});

        pref_auto_backup = (SwitchPreference) findPreference(PREFERENCE_AUTO_BACKUP);

		pref_import_data = findPreference(PREFERENCE_IMPORT_DATA);
		pref_import_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {

                BackupRestoreDialog.attemptRestore(SettingsActivity.this, storage, false).then(new Callback<BackupRestoreDialog.RestoreResult>() {
                    @Override
                    public void onCalled(BackupRestoreDialog.RestoreResult result) {
                        switch(result) {
                            case Success:
                                FeedActivity.goToAll();
                                snackbar(R.string.restore_success);
                                break;
                            case Deleted:
                                snackbar(R.string.delete_successful);
                                updateBackupStatus();
                                break;
                            case FileNotFound:
                                displayReadError(Backup.ReadError.FileNotFound);
                                break;
                            case Unknown:
                                displayReadError(Backup.ReadError.Unknown);
                                break;
                            case DeleteFailed:
                                snackbar(getString(R.string.delete_failed));
                                break;
                            default:
                                break;
                        }
                    }
                });
                return true;
            }
        });

        pref_auto_backup = (SwitchPreference) findPreference(PREFERENCE_AUTO_BACKUP);

        pref_currency = findPreference(PREFERENCE_CURRENCY);

        pref_currency.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
				CurrencyDialogFragment currencyDialogFragment = new CurrencyDialogFragment();

                Bundle args = new Bundle();
				args.putBoolean(CurrencyDialogFragment.CONTINUE, false);
				args.putBoolean(CurrencyDialogFragment.SHOW_INFO_TEXT, false);
				args.putBoolean(CurrencyDialogFragment.CANCELABLE, true);

                currencyDialogFragment.currencySelectedCallback = new CurrencyDialogFragment.CurrencySelectedCallback() {
                    @Override
                    public void onCurrencySelected() {
                        pref_currency.setSummary(data.preferences.getCurrency().renderSelf());
                    }
                };

				currencyDialogFragment.setArguments(args);
				currencyDialogFragment.show(getFragmentManager(), "settings_currency");
                return false;
            }
        });

        pref_cloud_sync = (SwitchPreference) findPreference(PREFERENCE_CLOUD_SYNC);

		pref_cloud_sync_account = findPreference(PREFERENCE_CLOUD_SYNC_ACCOUNT);
		pref_cloud_sync_account.setSummary(storage.getPreferences().getString(DriveLoginManager.PREFERENCE_ACCOUNT_NAME, null));
		pref_cloud_sync_account.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (storage.isDriveStorage()) {
                    StorageManager.changeDriveAccount(SettingsActivity.this).then(new Callback<DriveLoginManager.LoginResult>() {
                        @Override
                        public void onCalled(DriveLoginManager.LoginResult result) {
                            pref_cloud_sync_account.setSummary(result.accountName);
                        }
                    });
                }
                return false;
            }
        });

        Resource.checkFull(bp = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsrcl2UtkJQ4UkkI9Az7rW4jXcxWHR+AWh+5MIa2byY9AkfiNL7HYsUB7T6KMUmjsdpUYcGKw4TuiVUMUu8hy4TlhTZ0Flitx4h7yCxJgPBiUGC34CO1f6Yk0n2LBnJCLKKwrIasnpteqTxWvWLEsPdhxjQgURDmTpR2RCAsNb1Zzn07U2PSQE07Qo34SvA4kr+VCb5pPpJ/+OodQJSdIKka56bBMpS5Ea+2iYbTfsch8nnghZTnwr6dOieOSqWnMtBPQp5VV8kj1tHd/0iaQrYVmtqnkpQ+mG/3/p55gxJUdv9uGNbF0tzMytSxyvXfICnd4oMYK66DurLfNDXoc3QIDAQAB", null));

        if (Resource.isFull) {
            pref_auto_backup.setEnabled(true);
            pref_export_data.setEnabled(true);
            pref_import_data.setEnabled(true);
            pref_export_data.setSummary(null);
            pref_import_data.setSummary(getRestoreSummary());
        } else {
            pref_auto_backup.setChecked(false);
            pref_auto_backup.setEnabled(false);
            pref_auto_backup.setChecked(false);
            pref_export_data.setEnabled(false);
            pref_import_data.setEnabled(false);
            pref_export_data.setSummary(R.string.not_full_version);
            pref_import_data.setSummary(R.string.not_full_version);
        }

        if (!Resource.isFull) {
            pref_cloud_sync.setSummary(R.string.not_full_version);
            pref_cloud_sync.setEnabled(false);
        } else {
			pref_cloud_sync.setChecked(StorageManager.isDrive(this));
		}

        if (Resource.isFull) {
            if (!PermissionManager.getPermission(Manifest.permission.READ_EXTERNAL_STORAGE, SettingsActivity.this)) {
                //Didn't get permission, let's hide some things
                //TODO get screen and remove



                Preference pref = new Preference(SettingsActivity.this);
                pref.setTitle(getString(R.string.enable_storage_access));
                pref.setSummary(getString(R.string.enable_storage_access_summary));
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (PermissionManager.getPermission(Manifest.permission.READ_EXTERNAL_STORAGE, SettingsActivity.this)) {
                            startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
                            finish();
                        } else {
                            try {
                                //Open the specific App Info page:
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);

                            } catch (ActivityNotFoundException e ) {
                                //Open the generic Apps page:
                                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                startActivity(intent);
                            }
                        }
                        return false;
                    }
                });

                ((PreferenceCategory) findPreference(PREFERENCE_CATEGORY_DATA)).addPreference(pref);

                ((PreferenceCategory) findPreference(PREFERENCE_CATEGORY_DATA)).removePreference(pref_export_data);
                ((PreferenceCategory) findPreference(PREFERENCE_CATEGORY_DATA)).removePreference(pref_import_data);
                ((PreferenceCategory) findPreference(PREFERENCE_CATEGORY_DATA)).removePreference(pref_auto_backup);
            }
        }

        pref_cloud_sync.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (pref_cloud_sync.isChecked()) {

                    new MaterialDialog.Builder(SettingsActivity.this)
							.title(R.string.disable_cloud_sync_title)
							.content(R.string.disable_cloud_sync_content)
							.positiveText(R.string.disable_cloud_sync_affirmative)
							.negativeText(R.string.cancel)
							.cancelable(false)
							.callback(new MaterialDialog.ButtonCallback() {
								@Override
								public void onNegative(MaterialDialog dialog) {
									super.onNegative(dialog);
									pref_cloud_sync.setChecked(true);
									dialog.dismiss();
								}

								@Override
								public void onPositive(MaterialDialog dialog) {
									super.onPositive(dialog);
									StorageManager.migrateToLocal(SettingsActivity.this);
									pref_cloud_sync_account.setSummary("");
									dialog.dismiss();
								}
							})
					.show();
                } else {
                    new MaterialDialog.Builder(SettingsActivity.this)
                            .cancelable(false)
                            .title(R.string.cloud_sync)
                            .content(R.string.cloud_sync_description)
                            .positiveText(R.string.activate)
                            .negativeText(R.string.not_now)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);

                                    pref_cloud_sync.setChecked(false);

                                    StorageManager.migrateToDrive(SettingsActivity.this).then(new Callback<DriveLoginManager.LoginResult>() {
										@Override
										public void onCalled(DriveLoginManager.LoginResult result) {
											if (result.success) {
												pref_cloud_sync.setChecked(true);
												pref_cloud_sync_account.setSummary(result.accountName);
											} else {
												pref_cloud_sync.setChecked(false);
											}
										}
									});

                                    dialog.dismiss();
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
									pref_cloud_sync.setChecked(false);
                                    pref_cloud_sync_account.setSummary("");
									super.onNegative(dialog);
                                }
                            })
                            .show();
                }
                return true;
            }
        });

        pref_background = (ListPreference) findPreference(PREFERENCE_BACKGROUND);
        pref_background.setEntryValues(backgroundSettingsEntries);

		Preference pref_wipe_data = findPreference(PREFERENCE_WIPE_DATA);
		pref_wipe_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new MaterialDialog.Builder(SettingsActivity.this)
                        .cancelable(true)
                        .title(R.string.wipe_data)
                        .content(Resource.isFull ? R.string.wipe_data_confirm_full : R.string.wipe_data_confirm_free)
                        .positiveText(R.string.wipe)
                        .negativeText(R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);

                                BackupManager.createBackupAsync(getApplicationContext(), data, Backup.Type.Wipe).then(new Callback<Boolean>() {
                                    @Override
                                    public void onCalled(Boolean data) {
                                        updateBackupStatus();
                                        storage.wipe(SettingsActivity.this);
                                    }
                                });

                                dialog.dismiss();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                            }
                        })
                        .show();
                return false;
            }
        });

        updateBackupStatus();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        _toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_settings_rate) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:John+Simon+Co")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pub:John+Simon+Co")));
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    private String getRestoreSummary() {
        Backup latest = BackupManager.latestBackup();

        if (latest != null) {
            return String.format(getString(R.string.pref_backup_last), latest.generateDateString(this));
        } else {
            return null;
        }
    }

	private void displayReadError(Backup.ReadError result) {
		snackbar(getString(result == Backup.ReadError.FileNotFound ? R.string.no_file : R.string.read_failed));
	}

    private void snackbar(int id) {
        snackbar(getString(id));
    }

	private void snackbar(String text) {
        Snackbar.make(masterView, text, Snackbar.LENGTH_SHORT).show();
	}

    private void updateBackupStatus() {
        if (!BackupManager.hasBackups()) {
            if (Resource.isFull) {
                pref_import_data.setEnabled(false);
                pref_import_data.setSummary(R.string.no_backup_available);
            }
        } else {
            if (Resource.isFull) {
                pref_import_data.setEnabled(true);
                pref_import_data.setSummary(getRestoreSummary());
            }
        }
    }

	@Override
	protected void onDataReceived() {
		bindPreferenceSummaryToValue(pref_background);
		bindPreferenceSummaryToValue(pref_currency);

		pref_currency.setSummary(data.preferences.getCurrency().renderSelf());
		pref_background.setSummary(getResources().getStringArray(R.array.bg_display)[Arrays.asList(backgroundSettingsEntries).indexOf(data.preferences.getBackground())]);

	}

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     *
    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(getResources()) && !isSimplePreferences(getResources());
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Resources resources) {
        return (resources.getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static boolean isSimplePreferences(Resources resources) {
        return !isXLargeTablet(resources);
    }

    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference.getKey().equals("pref_currency")) {
                preference.setSummary(data.preferences.getCurrency().id);
                return true;
            }

			if (preference.getKey().equals("pref_background")) {
				data.preferences.background.setValue((String) value);
				storage.commit(SettingsActivity.this);

				pref_background.setSummary(getResources().getStringArray(R.array.bg_display)[Arrays.asList(backgroundSettingsEntries).indexOf(data.preferences.getBackground())]);

				return true;
			}

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
    }

    @Override
    public void onBackPressed() {
		finishAffinity();
		startActivity(new Intent(this, FeedActivity.class));
    }

    @Override
    public boolean onNavigateUp() {
		finishAffinity();
		startActivity(new Intent(this, FeedActivity.class));

		return true;
    }

    @Override
    public void onProductPurchased(String s, TransactionDetails transactionDetails) {
        Resource.purchasedFull(this, bp, masterView);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		bp.release();
	}

	@Override
    public void onPurchaseHistoryRestored() {
        Resource.checkFull(bp);
    }

    @Override
    public void onBillingError(int i, Throwable throwable) {

    }

    @Override
    public void onBillingInitialized() {

    }

}