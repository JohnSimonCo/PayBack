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


import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.R;
import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Subscription;
import com.johnsimon.payback.storage.DriveStorage;
import com.johnsimon.payback.util.Resource;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends MaterialPreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    public static Preference pref_currency;

    private ListPreference pref_background;
    private String backgroundPrefValue;

	public Subscription<String> loginSubscription = null;

	private Preference pref_cloud_sync_account;

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

        pref_currency = findPreference("pref_currency");

        pref_currency.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                WelcomeDialogFragment aboutDialogFragment = new WelcomeDialogFragment();

                Bundle args = new Bundle();
                args.putBoolean("SETTINGS", true);

                aboutDialogFragment.setArguments(args);
                aboutDialogFragment.show(getFragmentManager(), "settings_currency");
                return false;
            }
        });

        final CheckBoxPreference pref_cloud_sync = (CheckBoxPreference) findPreference("pref_cloud_sync");

		pref_cloud_sync_account = findPreference("pref_cloud_sync_account");
		pref_cloud_sync_account.setSummary(storage.getPreferences().getString(DriveStorage.PREFERENCE_ACCOUNT_NAME, null));
		pref_cloud_sync_account.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				((DriveStorage) storage).changeAccount();
				return false;
			}
		});

        if (!Resource.isFull) {
            pref_cloud_sync.setSummary(R.string.cloud_sync_not_full);
            pref_cloud_sync.setEnabled(false);
        } else {

		}

        final Activity self = this;

        pref_cloud_sync.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (pref_cloud_sync.isChecked()) {
                    Resource.preferences.edit().putBoolean(Resource.SAVE_KEY_USE_CLOUD_SYNC, false).apply();
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
                                    Resource.preferences.edit().putBoolean(Resource.SAVE_KEY_USE_CLOUD_SYNC, true).apply();

                                    dialog.dismiss();
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
									pref_cloud_sync.setChecked(false);
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

		if(storage.isDriveStorage()) {
			loginSubscription = storage.asDriveStorage().loginSubscription;
		}
    }

	@Override
	protected void onDataReceived() {
		bindPreferenceSummaryToValue(pref_background);

		bindPreferenceSummaryToValue(pref_currency);
	}

	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


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
                preference.setSummary(data.preferences.getCurrency());
                return true;
            }

			if (preference.getKey().equals("pref_background")) {
				data.preferences.set("background", value);
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

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onStart() {
        super.onStart();
        backgroundPrefValue = pref_background.getValue();

		if(loginSubscription != null) {
			loginSubscription.listen(onLoginCallback);
		}
    }

    @Override
    protected void onStop() {
        super.onStop();

		if(loginSubscription != null) {
			loginSubscription.unregister(onLoginCallback);
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
}