package com.johnsimon.payback.util;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.johnsimon.payback.core.User;
import com.johnsimon.payback.serialize.AppDataSerializable;
import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.R;
import com.johnsimon.payback.drawable.AvatarPlaceholderDrawable;
import com.johnsimon.payback.ui.ConfirmDialogFragment;
import com.johnsimon.payback.ui.FeedActivity;
import com.johnsimon.payback.ui.RequestRateDialogFragment;
import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.williammora.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Comparator;

public class Resource {
	private final static int MAX_ACTIONS = 15;

    private final static String SAVE_KEY_FIRST_RUN = "FIRST_RUN";
    private final static String SAVE_KEY_APP_DATA = "APP_DATA";
	private final static String SAVE_KEY_CURRENCY = "CURRENCY_SAVE_KEY";
	private final static String SAVE_KEY_ACTIONS = "SAVE_KEY_ACTIONS";
	private final static String SAVE_KEY_NEVER_RATE = "SAVE_KEY_NEVER_RATE";

    private final static String PACKAGE_NAME = "se.jrp.deptapp";
    private final static String ARG_PREFIX = PACKAGE_NAME + ".ARG_";

    public static AppData data = null;
    public static ArrayList<Person> people;
    public static ArrayList<Debt> debts;
	public static ArrayList<Contact> contacts;

	public static User user;

    private static SharedPreferences preferences;

	private static int actions;
	private static boolean neverRate;





/*
	ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment();

	Bundle argsDelete = new Bundle();
	argsDelete.putString(ConfirmDialogFragment.CONFIRM_TEXT, getResources().getString(R.string.overwrite_nfc_title));
	argsDelete.putString(ConfirmDialogFragment.INFO_TEXT, getResources().getString(R.string.overwrite_nfc_text) + person.name + "?");
	confirmDialogFragment.setArguments(argsDelete);

	confirmDialogFragment.show(getFragmentManager(), "overwrite_confirmation");

	confirmDialogFragment.confirm = new ConfirmDialogFragment.ConfirmCallback() {
		@Override
		public void onConfirm() {

		}
	};

	Snackbar.with(getActivity())
			.text(getString(R.string.nfc_error_all))
			.show(getActivity());


*/



    public static void init(Activity context) {
        if (data != null) return;

        Resource.preferences = context.getPreferences(Context.MODE_PRIVATE);

		contacts = getContacts(context);

        String JSON = preferences.getString(SAVE_KEY_APP_DATA, null);
        data = JSON == null ? new AppData() : new Gson().fromJson(JSON, AppDataSerializable.class).extract(contacts);
        people = data.people;
        debts = data.debts;

		//TODO remove sample data
        if (people.size() == 0) {
            ColorPalette palette = ColorPalette.getInstance(context);
            Person john = new Person("John Rapp", palette);
            people.add(john);
            Person simon = new Person("Simon Halvdansson", palette);
            people.add(simon);
            Person agge = new Person("Agge Eklöf", palette);
            people.add(agge);

            //#perfmatters
            long timestamp = System.currentTimeMillis();
            debts.add(new Debt(john, 100, "Dyr kebab", timestamp));
            debts.add(new Debt(simon, -200, "Pokemonkort", ++timestamp));
            debts.add(new Debt(simon, -1000, "Glömde kortet på ICA", ++timestamp));
            debts.add(new Debt(agge, 40, null, ++timestamp));
            debts.add(new Debt(john, 200, "Lampor till dator", ++timestamp));
            debts.add(new Debt(agge, 2.5f, "Äpple delat på 2", ++timestamp));

            commit();
        }

		user = getUser(context);

		neverRate = preferences.getBoolean(SAVE_KEY_NEVER_RATE, false);
		if(!neverRate) {
			actions = preferences.getInt(SAVE_KEY_ACTIONS, 0);
		}

		//Default configuration
		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(context).build());
    }

    public static void commit() {
        String JSON = new Gson().toJson(new AppDataSerializable(data), AppDataSerializable.class);
        preferences.edit().putString(SAVE_KEY_APP_DATA, JSON).apply();
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
        return preferences.getString(SAVE_KEY_CURRENCY, "$");
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

    public static boolean isLOrAbove() {
        return Build.VERSION.SDK_INT >= 21;
    }
	public static boolean isKkOrAbove() {
		return Build.VERSION.SDK_INT >= 19;
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
			avatarLetter.setText(person.name.substring(0, 1).toUpperCase());
		}
	}

	private static ArrayList<Contact> getContacts(Context context) {
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				//Get contact info
				String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				String photoURI = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
				long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));

				//Exlude email adresses
				if(name == null || name.matches(".*@.*\\..*")) continue;

				//Test if the name is unique
				boolean unique = true;
				for (Contact contact : contacts) {
					if (contact.name.equals(name)) unique = false;
				}

				//Exlude non-unique contacts
				if(!unique) continue;

				contacts.add(new Contact(name, photoURI, id));
			}
		}
		return contacts;
	}

	private static User getUser(Context context) {
		String name = null;

		Cursor cursor = context.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
		if(cursor.getCount() > 0) {
			cursor.moveToFirst();
			name = cursor.getString(cursor.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));
		}

		return new User(name);
	}

	public static Person getOrCreatePerson(String name, Context context) {
		//Try to find existing person
		Person person = data.findPersonByName(name);
		if(person != null) {
			return person;
		}

		//Create new person
		//Attempt to find link
		Contact link = null;
		for (Contact contact : contacts) {
			if (contact.name.equals(name)) {
				link = contact;
				break;
			}
		}
		//Create person and add to people
		person = new Person(name, link, ColorPalette.getInstance(context));
		people.add(person);
		return person;
	}

	//Returns all unique people names
	public static ArrayList<String> getPeopleNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (Person person : people) {
			if(!names.contains(person.name)) {
				names.add(person.name);
			}
		}

		return names;
	}

	//Returns all unique names (from people and contacts)
	public static ArrayList<String> getAllNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (Person person : people) {
			if(!names.contains(person.name)) {
				names.add(person.name);
			}
		}
		for (Contact contact : contacts) {
			if(!names.contains(contact.name)) {
				names.add(contact.name);
			}
		}

		return names;
	}

	//Returns all unique contact names
	public static ArrayList<String> getContactNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (Contact contact : contacts) {
			if(!names.contains(contact.name)) {
				names.add(contact.name);
			}
		}

		return names;
	}

	public static void actionComplete(FragmentManager fragmentManager) {
		//Don't do anything if user pressed "never rate"
		if(neverRate) return;

		//Increment actions and compare to MAX_ACTIONS
		if(++actions >= MAX_ACTIONS) {
			actions = 0;

			RequestRateDialogFragment fragment = new RequestRateDialogFragment();

			fragment.rateCallback = rateCallback;

			fragment.show(fragmentManager, "request_rate");
		}

		preferences.edit().putInt(SAVE_KEY_ACTIONS, actions).apply();
	}

    public static boolean areIdenticalLists(ArrayList<Person> before, ArrayList<Person> after) {

        if (before.size() != after.size()) {
            return false;
        }

        int size = before.size();


        for (int i = 0; i < size; i++) {
            if (before.get(i).id != after.get(i).id) {
                return false;
            }
        }

        return true;

    }

	private static RequestRateDialogFragment.RateCallback rateCallback = new RequestRateDialogFragment.RateCallback() {
		@Override
		public void onNeverAgain() {
			neverRate = true;

			preferences.edit().putBoolean(SAVE_KEY_NEVER_RATE, true).apply();
		}

		@Override
		public void onLater() {
		}
	};

    public static class AmountComparator implements Comparator<Debt> {
        @Override
        public int compare(Debt debt1, Debt debt2) {
            return Math.round(debt2.amount - debt1.amount);
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
            return person1.name.compareToIgnoreCase(person2.name);
        }
    }
}