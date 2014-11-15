package com.johnsimon.payback.ui;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
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
import com.johnsimon.payback.core.User;
import com.johnsimon.payback.send.DebtSendable;
import com.johnsimon.payback.util.Beamer;
import com.johnsimon.payback.util.Resource;

import java.util.ArrayList;
import java.util.Collections;

public class FeedActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, Beamer.BeamListener {

	private static String ARG_PREFIX = Resource.prefix("FEED");

	public static String ARG_FROM_CREATE = Resource.arg(ARG_PREFIX, "FROM_CREATE");

	public static boolean animateListItems = true;
	public static Toolbar toolbar;

	public static Person person = null;
	public static ArrayList<Debt> feed;

	private MenuItem filterAmount;

	private NavigationDrawerFragment navigationDrawerFragment;

	private NfcAdapter nfcAdapter;

	Beamer beamer;

	/**
	 * Used to store the last screen title. For use in {@link #restoreActionBar()}.
	 */
	private CharSequence title;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if (Resource.isLOrAbove()) {
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_launcher), getResources().getColor(R.color.primary_color)));

        }

		Resource.init(this);
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

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		title = getTitle();

		// Set up the drawer.
		navigationDrawerFragment.setUp(
				R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout)
		);

		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		beamer = new Beamer(this);
		if (nfcAdapter != null) {
			nfcAdapter.setNdefPushMessageCallback(beamer, this);
		}

		View feed_activity_status_bar_pusher = findViewById(R.id.feed_activity_status_bar_pusher);

		if (Resource.isKkOrAbove()) {
			int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
			if (resourceId > 0) {
				feed_activity_status_bar_pusher.getLayoutParams().height = getResources().getDimensionPixelSize(resourceId);
			}
			if (!Resource.isLOrAbove()) {
				feed_activity_status_bar_pusher.setBackgroundColor(getResources().getColor(R.color.primary_color_darker));
			}
		} else {
			feed_activity_status_bar_pusher.setVisibility(View.GONE);
		}


	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = getIntent();
		if(intent.getBooleanExtra(ARG_FROM_CREATE, false)) {
			Resource.actionComplete(getFragmentManager());
			intent.removeExtra(ARG_FROM_CREATE);
		}
	}

	public static boolean isAll() {
		return person == null;
	}

	@Override
	public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		Intent intent = getIntent();
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			beamer.processIntent(intent);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	@Override
	public void onNavigationDrawerItemSelected(NavigationDrawerItem item) {
		if(item.type == NavigationDrawerItem.Type.All) {
			person = null;
			feed = Resource.debts;
		} else if(item.type == NavigationDrawerItem.Type.Person) {
			person = item.owner;
			feed = Resource.data.personalizedFeed(person);
		}

		getFragmentManager().beginTransaction()
				.replace(R.id.container, new FeedFragment(), "feed_fragment_tag")
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
        if (filterAmount != null) {
            outState.putBoolean("AMOUNT_USED_SORT", filterAmount.isChecked());
        }
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		animateListItems = savedInstanceState.getBoolean("ANIMATE_FEED_LIST_ITEMS", true);

		if (savedInstanceState.getBoolean("AMOUNT_USED_SORT", false)) {
			sortAmount();
		}
	}

	public void sortTime() {
		Collections.sort(feed, new Resource.TimeComparator());
		FeedFragment.adapter.notifyDataSetChanged();
	}

	public void sortAmount() {
		Collections.sort(feed, new Resource.AmountComparator());
		FeedFragment.adapter.notifyDataSetChanged();
	}

	@Override
	public void onReceivedBeam(DebtSendable[] debts, User sender, boolean fullSync) {
		person = test(sender);

		if(fullSync) {
			Resource.data.sync(person, debts);
		} else {
			Resource.debts.add(debts[0].extract(person));
		}

		Resource.commit();

		feed = Resource.data.personalizedFeed(person);

		navigationDrawerFragment.setSelectedPerson(person);

		getFragmentManager().beginTransaction()
			.replace(R.id.container, new FeedFragment(), "feed_fragment_tag")
			.commit();
	}

	private Person test(User sender) {
		if(isAll()) {
			Person foundPerson = Resource.data.findPersonByName(sender.name);
			return foundPerson == null ? Resource.people.get(0) : foundPerson;
		} else {
			return person;
		}
	}
}