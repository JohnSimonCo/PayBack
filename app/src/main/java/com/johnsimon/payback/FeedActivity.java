package com.johnsimon.payback;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.MaterialMenuIcon;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.UUID;

public class FeedActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks, NfcAdapter.CreateNdefMessageCallback {

	private static String ARG_PREFIX = Resource.prefix("FEED");

	public static String ARG_GOTO_PERSON_ID = Resource.arg(ARG_PREFIX, "GOTO_PERSON");

	public static String SAVE_PERSON_ID = "SAVE_PERSON_ID";

    public static boolean animateListItems = true;

	public static ArrayList<Debt> feed;
	public static Person person;

    private ActionBar actionBar;

    private NavigationDrawerFragment navigationDrawerFragment;

	private NfcAdapter nfcAdapter;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;
    private CharSequence subtitle;

    private MaterialMenuIcon materialMenu;
    private boolean lastOpen = true;

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

        materialMenu = new MaterialMenuIcon(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);

	    SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.primary_color_darker));

        navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
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

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
				.build();
		ImageLoader.getInstance().init(config);

		readIntent();
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

		setSubtitle();
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
	private void setSubtitle() {
		subtitle = isAll()
			? getString(R.string.all)
			: person.name;
		actionBar.setSubtitle(subtitle);
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

		setSubtitle();

		getFragmentManager().beginTransaction()
				.replace(R.id.container, FeedFragment.newInstance(item), "feed_fragment_tag")
				.commit();
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (navigationDrawerFragment.openDrawer != lastOpen) {
            if (navigationDrawerFragment.openDrawer) {
                materialMenu.animateState(MaterialMenuDrawable.IconState.ARROW);
            } else {
                materialMenu.animateState(MaterialMenuDrawable.IconState.BURGER);
            }
            lastOpen = navigationDrawerFragment.openDrawer;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);

        if (item.getItemId() == android.R.id.home) {
            if (navigationDrawerFragment.openDrawer) {
                materialMenu.animatePressedState(MaterialMenuDrawable.IconState.BURGER);
                navigationDrawerFragment.openDrawer = false;
            } else {
                materialMenu.animatePressedState(MaterialMenuDrawable.IconState.ARROW);
                navigationDrawerFragment.openDrawer = true;
            }

            //We're using the animating flag to avoid doing onDrawerSlide
            // calculations when animating the drawer sliding.
            navigationDrawerFragment.isAnimatingSlide = true;

            //Using a delay handler is necessary because the user can "catch"
            // the animtion before it's finished thereby never resetting the
            // flag since onDrawerClosed/Opened never is called.
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigationDrawerFragment.isAnimatingSlide = false;
                }
            }, 300);

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
		materialMenu.onSaveInstanceState(outState);

		if(person != null) {
			outState.putString(SAVE_PERSON_ID, person.id.toString());
		}
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        animateListItems = savedInstanceState.getBoolean("ANIMATE_FEED_LIST_ITEMS", true);
        navigationDrawerFragment.openDrawer = navigationDrawerFragment.isDrawerOpen();

		String personId = savedInstanceState.getString(SAVE_PERSON_ID, null);
		if(personId == null) {
			showAll();
		} else {
			showPerson(personId);
		}

		setSubtitle();
	}
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        materialMenu.syncState(savedInstanceState);
    }
}