package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.drawable.AvatarPlaceholderDrawable;
import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Comparator;

public class Resource {
    private final static int MAX_ACTIONS = 25;
    private final static int MAX_FREE_DEBTS = 6;

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

		//Default configuration
		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(context).build());
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

    public static void setCurrency(String currency) {
        preferences.edit().putString(SAVE_KEY_CURRENCY, currency).apply();
    }

    public static String getCurrency() {
        //return preferences.getString(SAVE_KEY_CURRENCY, "$");
        return "$";
    }

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

	public static void createProfileImage(Person person, final RoundedImageView avatar, TextView avatarLetter) {
		if(person.hasImage()) {
			avatarLetter.setVisibility(View.GONE);

			ThumbnailLoader.getInstance().load(person.link.photoURI, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					avatar.setImageBitmap(loadedImage);
				}
			});
		} else {
			avatar.setImageDrawable(new AvatarPlaceholderDrawable(person.color));
			avatarLetter.setVisibility(View.VISIBLE);
			avatarLetter.setText(person.getAvatarLetter());
		}
	}

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
                            dialog.cancel();
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            neverRate = true;
                            preferences.edit().putBoolean(SAVE_KEY_NEVER_RATE, true).apply();
                            dialog.cancel();
                        }

                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            super.onNeutral(dialog);
                            dialog.cancel();
                        }
                    })
                    .show();

		}

		//Save the new action count
		preferences.edit().putInt(SAVE_KEY_ACTIONS, actions).apply();
	}

	/*
	public static <T extends Identifiable> boolean areIdenticalLists(ArrayList<T> before, ArrayList<T> after) {

		if (before.size() != after.size()) {
			return false;
		}

		int size = before.size();


		for (int i = 0; i < size; i++) {
			if (before.get(i).getId() != after.get(i).getId()) {
				return false;
			}
		}

		return true;

	}*/

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

    public static class AlphabeticalComparator implements Comparator<Person> {
        @Override
        public int compare(Person person1, Person person2) {
            return person1.getName().compareToIgnoreCase(person2.getName());
        }
    }

    public static void checkFull(BillingProcessor bp) {
        isFull =  bp.isPurchased("full_version");

		//TODO remove this
		isFull = true;
    }

    public static boolean canHold(int debts, int addition) {
        return isFull || debts + addition <= MAX_FREE_DEBTS;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}