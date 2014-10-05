package com.johnsimon.payback;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.UUID;

//public static CharSequence getRelativeTimeSpanString (long time, long now, long minResolution)
//http://developer.android.com/reference/android/text/format/DateUtils.html#getRelativeTimeSpanString%28long%29
public class FeedActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks, NfcAdapter.CreateNdefMessageCallback {

	private static String ARG_PREFIX = Resource.prefix("FEED");

	public static String ARG_GOTO_PERSON_ID = Resource.arg(ARG_PREFIX, "GOTO_PERSON");
    public static boolean animateListItems = true;

    private ActionBar actionBar;

    private NavigationDrawerFragment navigationDrawerFragment;

	NfcAdapter nfcAdapter;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;
    private CharSequence subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionBar = getActionBar();

		Resource.fetchData(this);
        if (Resource.isFirstRun()) {
 //           WelcomeDialogFragment welcomeDialogFragment = new WelcomeDialogFragment();
 //           welcomeDialogFragment.show(getFragmentManager().beginTransaction(), "welcome_dialog_fragment");
        }

	    setContentView(R.layout.activity_feed);

	    SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.primary_color_darker));

        navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();
        subtitle = getString(R.string.all);

        // Set up the drawer.
        navigationDrawerFragment.setUp(
		        R.id.navigation_drawer,
		        (DrawerLayout) findViewById(R.id.drawer_layout)
        );

		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter != null) {
			nfcAdapter.setNdefPushMessageCallback(this, this);
		}
    }

	@Override
	public void onStart() {
		super.onStart();

		Intent intent = getIntent();
		if (intent.getBooleanExtra(FeedFragment.ARG_ALL, false)) {
			actionBar.setSubtitle(R.string.all);
			navigationDrawerFragment.setSelectedPerson(null);

		} else if (intent.hasExtra(FeedActivity.ARG_GOTO_PERSON_ID)) {
			String uuid = intent.getStringExtra(FeedActivity.ARG_GOTO_PERSON_ID);

			Person person = Resource.data.findPerson(UUID.fromString(uuid));
			actionBar.setSubtitle(person.name);
			navigationDrawerFragment.setSelectedPerson(person);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		NdefMessage msg = new NdefMessage(
				new NdefRecord[] {
						Resource.createRecord("Test")
						/**
						 * The Android Application Record (AAR) is commented out. When a device
						 * receives a push with an AAR in it, the application specified in the AAR
						 * is guaranteed to run. The AAR overrides the tag dispatch system.
						 * You can add it back in to guarantee that this
						 * activity starts when receiving a beamed message. For now, this code
						 * uses the tag dispatch system.
						 */
						//,NdefRecord.createApplicationRecord("com.example.android.beam")
				});
		return msg;
	}

	void processIntent(Intent intent) {
		NdefMessage msg = (NdefMessage) intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)[0];
		NdefRecord[] records = msg.getRecords();

		Resource.toast(this, Resource.getContents(records[0]));


		// record 0 contains the MIME type, record 1 is the AAR, if present
		//textView.setText(new String(msg.getRecords()[0].getPayload()));
	}

	@Override
    public void onNavigationDrawerItemSelected(NavigationDrawerItem item) {
		// update the main content by replacing fragments

		getFragmentManager().beginTransaction()
				.replace(R.id.container, FeedFragment.newInstance(item), "feed_fragment_tag")
				.commit();

        if(item.type == NavigationDrawerItem.Type.All) {
			subtitle = getString(R.string.all);
		} else if(item.type == NavigationDrawerItem.Type.Person) {
			subtitle = item.title;
		}

        actionBar.setSubtitle(subtitle);
    }



    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
        actionBar.setSubtitle(subtitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.feed, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

	/*  This method is called by /res/navigation_drawer_list_footer.xml
		to either navigate to settings or show the "About screen". New
		items can be added here by adding the same onClick=""
	 */
	public void navigationDrawerFooterClick(View v) {

		switch (v.getId()) {
			case R.id.navigation_drawer_footer_people:
				startActivity(new Intent(this, PeopleManagerActivity.class));
				break;
			case R.id.navigation_drawer_footer_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				break;
			case R.id.navigation_drawer_footer_about:
		//		AboutDialogFragment aboutDialogFragment = new AboutDialogFragment();
		//		aboutDialogFragment.show(getFragmentManager(), "about_dialog");

				WelcomeDialogFragment aboutDialogFragment = new WelcomeDialogFragment();
				aboutDialogFragment.show(getFragmentManager(), "about_dialog");
				break;

			default:
				break;
		}
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("ANIMATE_FEED_LIST_ITEMS", animateListItems);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        animateListItems = savedInstanceState.getBoolean("ANIMATE_FEED_LIST_ITEMS", true);
    }

}
