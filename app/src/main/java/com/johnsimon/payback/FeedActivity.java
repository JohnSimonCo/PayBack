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

public class FeedActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment navigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		Resource.fetchData(this);

/*
        if (Resource.isFirstRun(preferences)) {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
        } else {
            Resource.generateContent(preferences);
        }
*/

        WelcomeDialogFragment welcomeDialogFragment = new WelcomeDialogFragment();
        welcomeDialogFragment.show(getFragmentManager().beginTransaction(), "welcome_dialog_fragment");

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
				.replace(R.id.container, FeedFragment.newInstance(item))
				.commit();
    }

    public void onSectionAttached(int index) {
	    title = Resource.people.get(index - 1).name;
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
	public void navigationDraweFooterClick(View v) {
		if (v.getId() == R.id.navigation_drawer_footer_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
		} else if (v.getId() == R.id.navigation_drawer_footer_about) {
			AboutDialogFragment aboutDialogFragment = new AboutDialogFragment();
			aboutDialogFragment.show(getFragmentManager(), "about_dialog");
		}
	}
}
