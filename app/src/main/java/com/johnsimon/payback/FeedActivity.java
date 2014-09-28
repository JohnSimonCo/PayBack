package com.johnsimon.payback;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.readystatesoftware.systembartint.SystemBarTintManager;

//public static CharSequence getRelativeTimeSpanString (long time, long now, long minResolution)
//http://developer.android.com/reference/android/text/format/DateUtils.html#getRelativeTimeSpanString%28long%29
public class FeedActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	private static String ARG_PREFIX = Resource.prefix("FEED");

	public static String ARG_GOTO_PERSON_ID = Resource.arg(ARG_PREFIX, "GOTO_PERSON");
    public static boolean animateListItems = true;

    private ActionBar actionBar;

    private NavigationDrawerFragment navigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionBar = getActionBar();

		Resource.fetchData(this);

        //WelcomeDialogFragment welcomeDialogFragment = new WelcomeDialogFragment();
        //welcomeDialogFragment.show(getFragmentManager().beginTransaction(), "welcome_dialog_fragment");

	    setContentView(R.layout.activity_feed);

	    SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.accent_color));

        navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(
		        R.id.navigation_drawer,
		        (DrawerLayout) findViewById(R.id.drawer_layout)
        );
    }

    @Override
    public void onNavigationDrawerItemSelected(NavigationDrawerItem item) {
		// update the main content by replacing fragments
		getFragmentManager().beginTransaction()
				.replace(R.id.container, FeedFragment.newInstance(item), "feed_fragment_tag")
				.commit();

        if(item.type == NavigationDrawerItem.Type.All) {
            actionBar.setSubtitle(R.string.all);
        } else if(item.type == NavigationDrawerItem.Type.Person) {
            actionBar.setSubtitle(item.title);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
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
