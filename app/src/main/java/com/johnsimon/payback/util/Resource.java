package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataActivityInterface;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.drawable.AvatarPlaceholderDrawable;
import com.makeramen.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.Comparator;
import java.util.Currency;

public class Resource {
    private final static int MAX_ACTIONS = 25;
    private final static int MAX_FREE_DEBTS = 5;

    private final static String SAVE_KEY_FIRST_RUN = "FIRST_RUN";
	private final static String SAVE_KEY_CURRENCY = "CURRENCY_SAVE_KEY";
	private final static String SAVE_KEY_ACTIONS = "SAVE_KEY_ACTIONS";
	private final static String SAVE_KEY_NEVER_RATE = "SAVE_KEY_NEVER_RATE";
    public final static String SAVE_KEY_USE_CLOUD_SYNC = "SAVE_KEY_USE_CLOUD_SYNC";

    private final static String PACKAGE_NAME = "com.johnsimon.payback";
    private final static String ARG_PREFIX = PACKAGE_NAME + ".ARG_";

    public static SharedPreferences preferences;

	private static int actions;
	private static boolean neverRate;

    public static boolean isFull = false;

    private static boolean isInitialized = false;

    public static void init(Activity context) {
        if (isInitialized) return;

        isInitialized = true;

        Resource.preferences = PreferenceManager.getDefaultSharedPreferences(context);

		neverRate = preferences.getBoolean(SAVE_KEY_NEVER_RATE, false);
		if(!neverRate) {
			actions = preferences.getInt(SAVE_KEY_ACTIONS, 0);
		}

		//TODO innan release: Ta bort detta
		StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder()
				.detectAll()
				.penaltyLog()
				.build();
		StrictMode.setVmPolicy(policy);
    }

    /*  Method to detect if it's the first time the user uses the app.
        Will return true if a preference with the key "FIRST_TIME"
        already exists.  */
    public static boolean isFirstRun() {
        if (preferences.getBoolean(SAVE_KEY_FIRST_RUN, true)) {
            preferences.edit().putBoolean(SAVE_KEY_FIRST_RUN, false).apply();
            return true;
        } else {
            return false;
        }
    }

    public static String prefix(String prefix) {
        return prefix + "_";
    }

    public static String arg(String prefix, String arg) {
        return ARG_PREFIX + prefix + "_" + arg;
    }

    /*public static void setCurrency(String currencyId) {
        preferences.edit().putString(SAVE_KEY_CURRENCY, currencyId).apply();
    }*/

    /*public static String getCurrency() {
        return preferences.getString(SAVE_KEY_CURRENCY, "$");
    }*/

    public static void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context context, Boolean bool) {
        toast(context, Boolean.toString(bool));
    }

    public static void toast(Context context, int i) {
        toast(context, Integer.toString(i));
    }

    //Enter size in dp, returns it in px
    // http://stackoverflow.com/questions/5255184/android-and-setting-width-and-height-programmatically-in-dp-units
    // (lookin' professional n' shit)
    public static int getPx(int dp, Resources res) {
        return (int) (dp * res.getDisplayMetrics().density + 0.5f);
    }

	public static boolean isKkOrAbove() {
		return Build.VERSION.SDK_INT >= 19;
	}
	public static boolean isLOrAbove() {
		return Build.VERSION.SDK_INT >= 21;
	}

    public static CharSequence getRelativeTimeString(Context ctx, long timestamp) {
        long now = System.currentTimeMillis();
        return (now - timestamp < 60000)
                ? ctx.getString(R.string.justnow)
                : DateUtils.getRelativeTimeSpanString(
					timestamp,
					now,
					DateUtils.SECOND_IN_MILLIS,
					DateUtils.FORMAT_ABBREV_ALL);
    }

	public static void createProfileImage(DataActivityInterface dataActivity, Person person, final RoundedImageView avatar, TextView avatarLetter) {
		if(person.hasImage()) {
			avatarLetter.setVisibility(View.GONE);

            Picasso.with(dataActivity.getContext())
                    .load(person.link.photoURI)
                    .into(avatar);
		} else {
			avatar.setImageDrawable(new AvatarPlaceholderDrawable(dataActivity, person.paletteIndex));
			avatarLetter.setVisibility(View.VISIBLE);
			avatarLetter.setText(person.getAvatarLetter());
		}
	}

	//TODO fördela
	public static void actionComplete(final Activity activity) {
		//Don't do anything if user pressed "never rate"
		if(neverRate) return;

		//Increment actions and compare to MAX_ACTIONS
		if(++actions >= MAX_ACTIONS) {
			actions = 0;

			//Open the request rate dialog

            new MaterialDialog.Builder(activity)
                    .title(R.string.rate_title)
                    .content(R.string.rate_text)
                    .positiveText(R.string.rate_now)
                    .neutralText(R.string.rate_later)
                    .negativeText(R.string.rate_never)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            final String appPackageName = activity.getPackageName();
                            try {
                                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                            }

                            neverRate = true;
                            preferences.edit().putBoolean(SAVE_KEY_NEVER_RATE, true).apply();
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            neverRate = true;
                            preferences.edit().putBoolean(SAVE_KEY_NEVER_RATE, true).apply();
                        }

                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            super.onNeutral(dialog);
                            dialog.dismiss();
                        }
                    })
                    .show();

		}

		//Save the new action count
		preferences.edit().putInt(SAVE_KEY_ACTIONS, actions).apply();
	}

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = activity.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static class AmountComparator implements Comparator<Debt> {
        @Override
        public int compare(Debt debt1, Debt debt2) {
            return Math.round(debt2.getAmount() - debt1.getAmount());
        }
    }

    public static class TimeComparator implements Comparator<Debt> {
        @Override
        public int compare(Debt debt1, Debt debt2) {
            return Math.round(debt2.timestamp - debt1.timestamp);
        }
    }

	public static class AlphabeticalStringComparator implements Comparator<String> {
		@Override
		public int compare(String string1, String string2) {
			return string1.compareToIgnoreCase(string2);
		}
	}

	public static class AlphabeticalCurrencyComparator implements Comparator<Currency> {
		@Override
		public int compare(Currency currency1, Currency currency2) {
			return currency1.getSymbol().compareToIgnoreCase(currency2.getSymbol());
		}
	}

    public static void checkFull(BillingProcessor bp) {
        isFull =  bp.isPurchased("full_version");

		//TODO innan release: Ta bort detta
		isFull = true;
    }

    public static boolean canHold(int debts, int addition) {
        return isFull || debts + addition <= MAX_FREE_DEBTS;
    }

	public static <T> boolean nullEquals(T a, T b) {
		return a == null ? b == null : a.equals(b);
	}
}