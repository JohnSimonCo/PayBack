package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.text.format.DateUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.google.gson.Gson;
import com.johnsimon.payback.BuildConfig;
import com.johnsimon.payback.R;
import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.core.DataActivityInterface;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.drawable.AvatarPlaceholderDrawable;
import com.johnsimon.payback.storage.DriveLoginManager;
import com.johnsimon.payback.storage.StorageManager;
import com.johnsimon.payback.ui.SettingsActivity;
import com.makeramen.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

public class Resource {
    private final static int MAX_ACTIONS = 20;
    private final static int MAX_FREE_DEBTS = 5;

    public final static long ONE_WEEK = 604800000;
    public final static long ONE_DAY = 86400000;
    public final static long ONE_HOUR = 3600000;

    private final static String SAVE_KEY_FIRST_RUN = "FIRST_RUN";
	private final static String SAVE_KEY_ACTIONS = "SAVE_KEY_ACTIONS";
	private final static String SAVE_KEY_NEVER_RATE = "SAVE_KEY_NEVER_RATE";

    private final static String PACKAGE_NAME = "com.johnsimon.payback";
    private final static String ARG_PREFIX = PACKAGE_NAME + ".ARG_";

	private static int actions;
	private static boolean neverRate;

    public static boolean isFull = false;

    private static boolean isInitialized = false;


	private static Gson _gson;
    public static Gson gson() {
        if(_gson == null) {
            _gson = new Gson();
        }
        return _gson;
    }

    public static void init(Context context) {
        if (isInitialized) return;

        isInitialized = true;

		SharedPreferences preferences = StorageManager.getPreferences(context);

		neverRate = preferences.getBoolean(SAVE_KEY_NEVER_RATE, false);
		if(!neverRate) {
			actions = preferences.getInt(SAVE_KEY_ACTIONS, 0);
		}

        if(BuildConfig.DEBUG) {
            StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();
            StrictMode.setVmPolicy(policy);
        }

    }

    /*  Method to detect if it's the first time the user uses the app.
        Will return true if a preference with the key "FIRST_TIME"
        already exists.  */
    public static boolean isFirstRun(SharedPreferences preferences, boolean saveCheck) {
        if (preferences.getBoolean(SAVE_KEY_FIRST_RUN, true)) {
            if (saveCheck) {
                preferences.edit().putBoolean(SAVE_KEY_FIRST_RUN, false).apply();
            }
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

    public static CharSequence getRelativeTimeString(Context context, long timestamp) {
        long now = System.currentTimeMillis();
        return (now - timestamp) < 60000
                ? context.getString(R.string.justnow)
                : DateUtils.getRelativeTimeSpanString(
					timestamp,
					now,
					DateUtils.SECOND_IN_MILLIS,
					DateUtils.FORMAT_ABBREV_ALL);
    }

	public static void createProfileImage(DataActivityInterface dataActivity, Person person, RoundedImageView avatar, TextView avatarLetter) {

        Picasso.with(dataActivity.getContext()).cancelRequest(avatar);

		if (person.hasImage()) {
			avatarLetter.setVisibility(View.GONE);

            Picasso.with(dataActivity.getContext())
                    .load(person.link.photoURI)
                    .fit()
                    .placeholder(R.drawable.ic_person_placeholder)
                    .into(avatar);
		} else {
            avatar.setImageBitmap(null);
			avatar.setImageDrawable(new AvatarPlaceholderDrawable(ColorPalette.getInstance(dataActivity), person.paletteIndex));
			avatarLetter.setVisibility(View.VISIBLE);
			avatarLetter.setText(person.getAvatarLetter());
		}
	}

    public static int mixTwoColors( int color1, int color2, float amount )
    {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL   = 16;
        final byte GREEN_CHANNEL =  8;
        final byte BLUE_CHANNEL  =  0;

        final float inverseAmount = 1.0f - amount;

        int a = ((int)(((float)(color1 >> ALPHA_CHANNEL & 0xff )*amount) +
                ((float)(color2 >> ALPHA_CHANNEL & 0xff )*inverseAmount))) & 0xff;
        int r = ((int)(((float)(color1 >> RED_CHANNEL & 0xff )*amount) +
                ((float)(color2 >> RED_CHANNEL & 0xff )*inverseAmount))) & 0xff;
        int g = ((int)(((float)(color1 >> GREEN_CHANNEL & 0xff )*amount) +
                ((float)(color2 >> GREEN_CHANNEL & 0xff )*inverseAmount))) & 0xff;
        int b = ((int)(((float)(color1 & 0xff )*amount) +
                ((float)(color2 & 0xff )*inverseAmount))) & 0xff;

        return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b << BLUE_CHANNEL;
    }

	//TODO[Innan 1.3] fördela actionCompletes
	public static void actionComplete(final Activity activity) {
		//Don't do anything if user pressed "never rate"
		if(neverRate) return;

		final SharedPreferences preferences = StorageManager.getPreferences(activity);

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

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = activity.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static Bitmap roundBitmap(Bitmap sbmp, int radius) {
        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(),
                sbmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        //paint.setAntiAlias(true);
        //paint.setFilterBitmap(true);
        //paint.setDither(true);
        //canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#ff0000"));
        //canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f, sbmp.getHeight() / 2 + 0.7f,
        //       sbmp.getWidth() / 2 + 0.1f, paint);
        canvas.drawCircle(0, 0, (float)Math.sqrt(2f)/1.999999995f/*.707f*/, paint);
        //canvas.drawPaint(paint);
        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //canvas.drawBitmap(sbmp, rect, rect, paint);
        return output;
    }

    public static void purchasedFull(final Activity activity, BillingProcessor bp, final View masterView) {
        Resource.checkFull(bp);

        if (!Resource.isFull) {
            return;
        }

        SharedPreferences preferences = StorageManager.getPreferences(activity);
        preferences.edit().putBoolean(SettingsActivity.PREFERENCE_AUTO_BACKUP, true).apply();

        //TODO Purshase skärm där man kan se alla features
        new MaterialDialog.Builder(activity)
                .title(R.string.cloud_sync)
                .content(R.string.cloud_sync_description_first)
                .positiveText(R.string.activate)
                .negativeText(R.string.not_now)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                        StorageManager.migrateToDrive(activity).then(new Callback<DriveLoginManager.LoginResult>() {
							@Override
							public void onCalled(DriveLoginManager.LoginResult result) {
								if (result.success) {
									if (masterView != null) {
										Snackbar.make(masterView, R.string.login_successful, Snackbar.LENGTH_LONG).show();
									}
								}
							}
						});
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .show();
    }
    
    public static SimpleDateFormat monthDateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

    public static class AmountComparator implements Comparator<Debt> {
        @Override
        public int compare(Debt a, Debt b) {
            if (a.isPaidBack()) {
                if (!b.isPaidBack()) {
                    //Only a is paid back
                    return 1;
                }
            } else if (b.isPaidBack()) {
                //Only b is paid back
                return -1;
            }
            //Both or none is paid back
            return (int) Math.round(b.getRemainingAbsoluteDebt() - a.getRemainingAbsoluteDebt());
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
        isFull = bp.isPurchased("full_version");

		if(BuildConfig.DEBUG) {
            isFull = true;
        }
    }

    public static boolean canHold(int debts, int addition) {
        return isFull || debts + addition <= MAX_FREE_DEBTS;
    }

	public static <T> boolean nullEquals(T a, T b) {
		return a == null ? b == null : a.equals(b);
	}
}