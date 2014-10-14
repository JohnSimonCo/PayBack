package com.johnsimon.payback;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.provider.ContactsContract;
import android.text.format.DateUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Locale;

import static android.nfc.NdefRecord.createMime;

public class Resource {
	private final static String SAVE_KEY_FIRST_RUN = "FIRST_RUN";
	private final static String SAVE_KEY_APP_DATA = "APP_DATA";
	private final static String SAVE_KEY_CURRENCY = "CURRENCY_SAVE_KEY";

	private final static String PACKAGE_NAME = "se.jrp.deptapp";
	private final static String ARG_PREFIX = PACKAGE_NAME + ".ARG_";

	public static AppData data = null;
	public static ArrayList<Person> people;
	public static ArrayList<Debt> debts;
	public static ArrayList<Contact> contacts;

	private static Activity context;
	private static SharedPreferences preferences;

	public static void fetchData(Activity context) {
		if (data != null) return;

		Resource.context = context;
		Resource.preferences = context.getPreferences(Context.MODE_PRIVATE);

		String JSON = preferences.getString(SAVE_KEY_APP_DATA, null);
		data = JSON == null ? new AppData() : new Gson().fromJson(JSON, AppDataSerializable.class).extract();
		people = data.people;
		debts = data.debts;

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
		contacts = Resource.getAllContacts(context);



		/* Censored Version
		if(isFirstRun(preferences)) {
			Person john = new Person("John Rapp", UUID.randomUUID());
			Person simon = new Person("Simon Halvdansson", UUID.randomUUID());
			Person agge = new Person("Agge Eklöf", UUID.randomUUID());
			people.add(john);
			people.add(simon);
			people.add(agge);

			debts.add(new Debt(john, 100, "Dyr kebab"));
			debts.add(new Debt(simon, -200, "Pokemonkort"));
			debts.add(new Debt(simon, -1000, "Glömde kortet på ICA"));
			debts.add(new Debt(agge, 40, null));
			debts.add(new Debt(john, 200, "Lampor till dator"));
			debts.add(new Debt(agge, 2.5f, "Äpple delat på 2"));
			commit();
		}
		*/


		/* Non-Censored Version
		Person druggie = new Person("Random druggie", UUID.randomUUID());
		people.add(druggie);
		Person dealer = new Person("Ma hagsätra dealer", UUID.randomUUID());
		people.add(dealer);
		Person ica = new Person("killen på ica", UUID.randomUUID());
		people.add(ica);

		debts.add(new Debt(druggie, -2000, "weed an sum white shit"));
		debts.add(new Debt(dealer, -20000, "100 l moskovskaya, ingen grossistrabbat :/"));
		debts.add(new Debt(ica, 350, "sög av mig"));
		debts.add(new Debt(ica, -10, "snabbt knull"));
		debts.add(new Debt(ica, 500, "hand job på plattan"));
		*/
	}

	public static void commit() {
		String JSON = new Gson().toJson(new AppDataSerializable(data), AppDataSerializable.class);
		preferences.edit().putString(SAVE_KEY_APP_DATA, JSON).apply();
	}

	/*  Method to detect if it's the first time the user uses the app.
		Will return true if a preference with the key "FIRST_TIME"
		already exists.  */
	public static boolean isFirstRun() {
		if(preferences.getBoolean(SAVE_KEY_FIRST_RUN, true)) {
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

	private static ArrayList<Contact> getAllContacts(Context ctx) {
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		Cursor cursor = ctx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if(cursor.getCount() > 0) {
			while(cursor.moveToNext()) {
				String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				String photoURI = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
				//If it's not an email adress
				if(name != null && !name.matches(".*@.*\\..*")) {
					//Make sure it's unique
					boolean unique = true;
					//First search in people (since that list is generally smaller)
					for(Person person: people) {
						if(person.name.equals(name)) unique = false;
					}
					//If not found, continue to search in contacts
					if(unique) {
						for(Contact contact : contacts) {
							if(contact.name.equals(name)) unique = false;
						}
					}
					//If unique, add to contacts
					if(unique) {
						contacts.add(new Contact(name, photoURI));
					}
				}
			}
		}
		return contacts;
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

	public static void expand(final View v) {
		expand(v, true, 4);
	}

	public static void expand(final View v, boolean b) {
		expand(v, b, 4);
	}

	public static void expand(final View v, boolean animate, int msPerDp) {
		v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		final int targtetHeight = v.getMeasuredHeight();

		if(animate) {
			v.getLayoutParams().height = 0;
			v.setVisibility(View.VISIBLE);
			Animation a = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					v.getLayoutParams().height = interpolatedTime == 1
							? LinearLayout.LayoutParams.WRAP_CONTENT
							: (int)(targtetHeight * interpolatedTime);
					v.requestLayout();
				}

				@Override
				public boolean willChangeBounds() {
					return true;
				}
			};

			// 0.333dp/ms
			a.setDuration((int)(targtetHeight / v.getContext().getResources().getDisplayMetrics().density) * msPerDp);
			v.startAnimation(a);
		} else {
			v.setVisibility(View.VISIBLE);
			v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
			v.requestLayout();
		}
	}

	public static void collapse(final View v) {
		collapse(v, true, 4);
	}

	public static void collapse(final View v, boolean b) {
		collapse(v, b, 4);
	}

	public static void collapse(final View v, boolean animate, int msPerDp) {
		final int initialHeight = v.getMeasuredHeight();

		if(animate) {
			Animation a = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					if(interpolatedTime == 1){
						v.setVisibility(View.GONE);
					} else {
						v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
						v.requestLayout();
					}
				}

				@Override
				public boolean willChangeBounds() {
					return true;
				}
			};

			// 0.333dp/ms
			a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density) * msPerDp);
			v.startAnimation(a);
		} else {
			v.setVisibility(View.GONE);
			v.getLayoutParams().height = 0;
			v.requestLayout();
		}
	}

	public static Person getPerson(String name) {
		for(Person person : people) {
			if(person.name.equals(name)) return person;
		}

		Person person = null;
		for(Contact c : contacts) {
			if(c.name.equals(name)) {
				contacts.remove(c);
				if(c.photoURI != null) {
					//Will be handled later otherwise
					person = new Person(c.name, c.photoURI);
				}
				break;
			}
		}
		if(person == null) {
			person = new Person(name, ColorPalette.getInstance(context));
		}
		people.add(person);
		return person;
	}

	public static ArrayList<String> getAllNames() {
		ArrayList<String> names = new ArrayList<String>();
		for(Person person : people) {
			names.add(person.name);
		}
		for(Contact contact : contacts) {
			names.add(contact.name);
		}

		return names;
	}

	//Enter size in dp, returns it in px
	// http://stackoverflow.com/questions/5255184/android-and-setting-width-and-height-programmatically-in-dp-units
	// (lookin' professional n' shit)
	public static int getPx(int dp, Context ctx) {
		return (int) (dp * ctx.getResources().getDisplayMetrics().density + 0.5f);
	}

	public static void doListAnimation(final View view, int offset) {
		view.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		view.setAlpha(0f);

		ObjectAnimator animAlpha = ObjectAnimator.ofFloat(view,
				"alpha", 1);
		animAlpha.setDuration(450);
		animAlpha.setStartDelay(offset);
		animAlpha.start();

		view.setRotation(20f);

		ObjectAnimator rotation = ObjectAnimator.ofFloat(view,
				"rotation", 0f);
		rotation.setDuration(350);
		rotation.setStartDelay(offset);
		rotation.start();

		view.setTranslationY(620f);

		ObjectAnimator animY = ObjectAnimator.ofFloat(view,
				"translationY", 0);
		animY.setDuration(450);
		animY.setStartDelay(offset);

		animY.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		});

		animY.start();
	}

	public static void animateHardwareFadeIn(final View view, int duration, int offset) {

		view.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		view.setAlpha(0f);

		ObjectAnimator animAlpha = ObjectAnimator.ofFloat(view,
				"alpha", 1);
		animAlpha.setDuration(duration);
		animAlpha.setStartDelay(offset);

		animAlpha.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				view.setVisibility(View.VISIBLE);
				view.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		});

		animAlpha.start();

	}

    public static void animateHardwareFadeOut(final View view, int duration, int offset) {

        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        view.setAlpha(1f);

        ObjectAnimator animAlpha = ObjectAnimator.ofFloat(view,
                "alpha", 0);
        animAlpha.setDuration(duration);
        animAlpha.setStartDelay(offset);

        animAlpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.VISIBLE);
                view.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });

        animAlpha.start();

    }

	public static NdefRecord createRecord(String contents) {
		return createMime("application/vnd.com.johnsimon.payback", contents.getBytes());
	}

	public static String getContents(NdefRecord record) {
		return new String(record.getPayload());
	}

	public static CharSequence getRelativeTimeString(Context ctx, long timestamp) {
		long now = System.currentTimeMillis();
		return (now - timestamp < 60000)
				?	ctx.getString(R.string.justnow)
				:	DateUtils.getRelativeTimeSpanString(
						timestamp,
						now,
						DateUtils.SECOND_IN_MILLIS,
						DateUtils.FORMAT_ABBREV_ALL);


	}

	public static NdefMessage createMessage(ArrayList<Debt> feed) {
		Debt[] debts = feed.toArray(new Debt[feed.size()]);
		String JSON = new Gson().toJson(debts, Debt[].class);
		return new NdefMessage(
				new NdefRecord[] {
						Resource.createRecord(JSON)
						/**
						 * The Android Application Record (AAR) is commented out. When a device
						 * receives a push with an AAR in it, the application specified in the AAR
						 * is guaranteed to run. The AAR overrides the tag dispatch system.
						 * You can add it back in to guarantee that this
						 * activity starts when receiving a beamed message. For now, this code
						 * uses the tag dispatch system.
						 */

				});
	}

	public static Debt[] readMessage(NdefMessage message) {
		NdefRecord[] records = message.getRecords();
		String JSON = Resource.getContents(records[0]);

		// record 0 contains the MIME type, record 1 is the AAR, if present
		//textView.setText(new String(msg.getRecords()[0].getPayload()));

		return new Gson().fromJson(JSON, Debt[].class);
	}
}