package com.johnsimon.payback.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.johnsimon.payback.R;
import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Notification;
import com.johnsimon.payback.async.NotificationCallback;
import com.johnsimon.payback.async.Subscription;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.storage.DriveLoginManager;
import com.johnsimon.payback.storage.DriveStorage;
import com.johnsimon.payback.storage.StorageManager;
import com.johnsimon.payback.util.Resource;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.williammora.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends MaterialPreferenceActivity implements BillingProcessor.IBillingHandler {

    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    public static Preference pref_currency;

    private ListPreference pref_background;

	public Subscription<String> loginSubscription = null;

	private Preference pref_cloud_sync_account;
    private CheckBoxPreference pref_cloud_sync;

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

        final Activity self = this;
        /*
        Preference pref_export_data = findPreference("pref_export_data");
        pref_export_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("savedata.txt", Context.MODE_PRIVATE));
                    outputStreamWriter.write(data.save());
                    outputStreamWriter.close();

                    Snackbar.with(self)
                            .text(getString(R.string.save_success))
                            .show(self);
                }
                catch (IOException e) {
                    Snackbar.with(self)
                            .text(getString(R.string.save_fail))
                            .show(self);
                }
                return false;
            }
        });

        //TODO finish import/export
        Preference pref_import_data = findPreference("pref_import_data");
        pref_import_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                try {
                    InputStream inputStream = openFileInput("savedata.txt");

                    if ( inputStream != null ) {
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String receiveString;
                        StringBuilder stringBuilder = new StringBuilder();

                        while ( (receiveString = bufferedReader.readLine()) != null ) {
                            stringBuilder.append(receiveString);
                        }

                        inputStream.close();

                        data = AppData.fromJson(stringBuilder.toString());
						storage.commit(data);

                        Toast.makeText(self, getString(R.string.restore_success), Toast.LENGTH_LONG).show();

                        finishAffinity();
                        startActivity(new Intent(self, FeedActivity.class));
                    }
                }
                catch (FileNotFoundException e) {
                    Snackbar.with(self)
                            .text(getString(R.string.no_file))
                            .show(self);
                } catch (IOException e) {
                    Snackbar.with(self)
                            .text(getString(R.string.read_failed))
                            .show(self);
                }

                return false;
            }
        });*/

        pref_currency = findPreference("pref_currency");

        pref_currency.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
				CurrencyDialogFragment currencyDialogFragment = new CurrencyDialogFragment();

                Bundle args = new Bundle();
				args.putBoolean(CurrencyDialogFragment.CONTINUE, false);
				args.putBoolean(CurrencyDialogFragment.SHOW_INFO_TEXT, false);
				args.putBoolean(CurrencyDialogFragment.CANCELABLE, true);

				currencyDialogFragment.setArguments(args);
				currencyDialogFragment.show(getFragmentManager(), "settings_currency");
                return false;
            }
        });

        pref_cloud_sync = (CheckBoxPreference) findPreference("pref_cloud_sync");

		pref_cloud_sync_account = findPreference("pref_cloud_sync_account");
		pref_cloud_sync_account.setSummary(storage.getPreferences().getString(DriveLoginManager.PREFERENCE_ACCOUNT_NAME, null));
		pref_cloud_sync_account.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(storage.isDriveStorage()) {
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
        if (!Resource.isFull) {
            pref_cloud_sync.setSummary(R.string.cloud_sync_not_full);
            pref_cloud_sync.setEnabled(false);
        } else {
			pref_cloud_sync.setChecked(StorageManager.isDrive(this));
		}

        pref_cloud_sync.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (pref_cloud_sync.isChecked()) {
					StorageManager.migrateToLocal(self);
                    pref_cloud_sync_account.setSummary("");
                } else {
                    new MaterialDialog.Builder(self)
                            .cancelable(false)
                            .title(R.string.cloud_sync)
                            .content(R.string.cloud_sync_description)
                            .positiveText(R.string.activate)
                            .negativeText(R.string.not_now)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
									StorageManager.migrateToDrive(SettingsActivity.this).then(new Callback<DriveLoginManager.LoginResult>() {
										@Override
										public void onCalled(DriveLoginManager.LoginResult result) {
											if(result.success) {
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

        pref_background = (ListPreference) findPreference("pref_background");

		Preference pref_wipe_data = findPreference("pref_wipe_data");
		pref_wipe_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new MaterialDialog.Builder(self)
						.cancelable(true)
						.title(R.string.wipe_data)
						.content(Resource.isFull ? R.string.wipe_data_confirm_full : R.string.wipe_data_confirm_free)
						.positiveText(R.string.wipe)
						.negativeText(R.string.cancel)
						.callback(new MaterialDialog.ButtonCallback() {
							@Override
							public void onPositive(MaterialDialog dialog) {
								super.onPositive(dialog);

								storage.wipe();

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
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(StorageManager.loginManager != null) {
			StorageManager.loginManager.handleActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	protected void onDataReceived() {
		bindPreferenceSummaryToValue(pref_background);
		bindPreferenceSummaryToValue(pref_currency);

		pref_currency.setSummary(data.getPreferences().getCurrency().id);
		pref_background.setSummary(getResources().getStringArray(R.array.bg_display)[Arrays.asList(getResources().getStringArray(R.array.bg_entries)).indexOf(data.preferences.getBackground())]);

	}

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     *
    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /** {@inheritDoc} */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            //loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference.getKey().equals("pref_currency")) {
                preference.setSummary(data.getPreferences().getCurrency().id);
                return true;
            }

			if (preference.getKey().equals("pref_background")) {
				data.getPreferences().background.setValue((String) value);
				storage.commit();

				pref_background.setSummary(getResources().getStringArray(R.array.bg_display)[Arrays.asList(getResources().getStringArray(R.array.bg_entries)).indexOf(data.getPreferences().getBackground())]);

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
    protected void onStart() {
        super.onStart();

        if (storage.isDriveStorage()) {

        }
		if(loginSubscription != null) {
		}
    }

    @Override
    protected void onStop() {
        super.onStop();

		if(loginSubscription != null) {
		}
    }

	Callback<String> onLoginCallback = new Callback<String>() {
		@Override
		public void onCalled(String name) {
			pref_cloud_sync_account.setSummary(name);
		}
	};

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
        Resource.checkFull(bp);

        if (!Resource.isFull) {
            return;
        }

        new MaterialDialog.Builder(this)
                .title(R.string.cloud_sync)
                .content(R.string.cloud_sync_description_first)
                .positiveText(R.string.activate)
                .negativeText(R.string.not_now)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .show();
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		bp.release();
	}

	@Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int i, Throwable throwable) {

    }

    @Override
    public void onBillingInitialized() {

    }

}