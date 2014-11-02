package com.johnsimon.payback.ui;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.NavigationDrawerItem;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.R;
import com.johnsimon.payback.util.Resource;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class FeedActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, NfcAdapter.CreateNdefMessageCallback {

	private static String ARG_PREFIX = Resource.prefix("FEED");

	public static String ARG_GOTO_PERSON_ID = Resource.arg(ARG_PREFIX, "GOTO_PERSON");

	public static String SAVE_PERSON_ID = "SAVE_PERSON_ID";

    public static boolean animateListItems = true;
    public static Toolbar toolbar;

	public static ArrayList<Debt> feed;
	public static Person person;

    private MenuItem filterTime;
    private MenuItem filterAmount;

    private NavigationDrawerFragment navigationDrawerFragment;

	private NfcAdapter nfcAdapter;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		Resource.fetchData(this);
        if (Resource.isFirstRun()) {
            WelcomeDialogFragment welcomeDialogFragment = new WelcomeDialogFragment();
            welcomeDialogFragment.show(getFragmentManager().beginTransaction(), "welcome_dialog_fragment");
        }

		if(isAll()) {
			feed = Resource.debts;
		} else {
			feed = Resource.data.personalizedFeed(person);
		}

	    setContentView(R.layout.activity_feed);

        toolbar = (Toolbar) findViewById(R.id.feed_toolbar);
        setSupportActionBar(toolbar);

		navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		navigationDrawerFragment.setSelectedPerson(person);
		title = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(
		        R.id.navigation_drawer,
		        (DrawerLayout) findViewById(R.id.drawer_layout)
        );

		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter != null) {
			nfcAdapter.setNdefPushMessageCallback(this, this);
		}

		View feed_activity_status_bar_pusher = findViewById(R.id.feed_activity_status_bar_pusher);

		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			feed_activity_status_bar_pusher.getLayoutParams().height = getResources().getDimensionPixelSize(resourceId);
		}

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
				.build();
		ImageLoader.getInstance().init(config);
    }

	private void readIntent() {
		Intent intent = getIntent();

		if(intent.hasExtra(ARG_GOTO_PERSON_ID)) {
			showPerson(intent.getStringExtra(FeedActivity.ARG_GOTO_PERSON_ID));

			intent.removeExtra(ARG_GOTO_PERSON_ID);
		} else {
			showAll();
		}

		navigationDrawerFragment.setSelectedPerson(person);
	}

	private void showPerson(UUID uuid) {
		person = Resource.data.findPerson(uuid);
		feed = Resource.data.personalizedFeed(person);
	}
	private void showPerson(String personId) {
		showPerson(UUID.fromString(personId));
	}
	
	private void showAll() {
		person = null;
		feed = Resource.debts;
	}
	public static boolean isAll() {
		return person == null;
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
		return isAll() ? null : Resource.createMessage(feed);
	}

	public void processIntent(Intent intent) {
		NdefMessage message = (NdefMessage) intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)[0];
		Debt[] debts = Resource.readMessage(message);

		Resource.toast(this, "Sent " + debts.length + " debts via NFC");
	}

	@Override
    public void onNavigationDrawerItemSelected(NavigationDrawerItem item) {
		if(item.type == NavigationDrawerItem.Type.All) {
			showAll();
		} else if(item.type == NavigationDrawerItem.Type.Person) {
			showPerson(item.personId);
		}

		getFragmentManager().beginTransaction()
				.replace(R.id.container, FeedFragment.newInstance(item), "feed_fragment_tag")
				.commit();
    }

    @Override
    public void onShowGlobalContextActionBar() {
        getSupportActionBar().setTitle(R.string.app_name);
    }

    public void restoreActionBar() {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		// Only show items in the action bar relevant to this screen
		// if the drawer is not showing. Otherwise, let the drawer
		// decide what to show in the action bar.
		getMenuInflater().inflate(R.menu.feed, menu);

		filterTime = menu.findItem(R.id.menu_filter_time);
		filterAmount = menu.findItem(R.id.menu_filter_amount);

		restoreActionBar();
		return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.menu_filter_time:
                item.setChecked(true);
                sortTime();
                break;

            case R.id.menu_filter_amount:
            	item.setChecked(true);
                sortAmount();
                break;

        }

        return result;
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
				AboutDialogFragment aboutDialogFragment = new AboutDialogFragment();
				aboutDialogFragment.show(getFragmentManager(), "about_dialog");
				break;

			default:
				break;
		}
	}

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navigationDrawerFragment.mDrawerToggle.syncState();

        if (animateListItems) {
            Animation toolbarEnter = AnimationUtils.loadAnimation(this, R.anim.feed_toolbar_enter);
            Animation headerEnter = AnimationUtils.loadAnimation(this, R.anim.feed_header_enter);
            toolbar.startAnimation(toolbarEnter);
            FeedFragment.headerView.startAnimation(headerEnter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("ANIMATE_FEED_LIST_ITEMS", animateListItems);

        outState.putBoolean("AMOUNT_USED_SORT", filterAmount.isChecked());
		/*
		if(person != null) {
			outState.putString(SAVE_PERSON_ID, person.id.toString());
		}
		*/
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        animateListItems = savedInstanceState.getBoolean("ANIMATE_FEED_LIST_ITEMS", true);

        if (savedInstanceState.getBoolean("AMOUNT_USED_SORT", false)) {
            sortAmount();
        }

		/*
		String personId = savedInstanceState.getString(SAVE_PERSON_ID, null);
		if(personId == null) {
			showAll();
		} else {
			showPerson(personId);
		}
		*/
	}

	public void sortTime() {
		Collections.sort(FeedFragment.debts, new Resource.TimeComparator());
		FeedFragment.adapter.notifyDataSetChanged();
	}

    public void sortAmount() {
        Collections.sort(FeedFragment.debts, new Resource.AmountComparator());
		FeedFragment.adapter.notifyDataSetChanged();
    }

}