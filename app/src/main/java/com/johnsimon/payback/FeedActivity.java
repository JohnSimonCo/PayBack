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

		Resource.fetchData(getPreferences(Context.MODE_PRIVATE));

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

        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.accent_color)));

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
