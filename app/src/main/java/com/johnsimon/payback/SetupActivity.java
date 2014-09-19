package com.johnsimon.payback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.readystatesoftware.systembartint.SystemBarTintManager;


public class SetupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

		//Dirty shit, hoppas John inte ser :)
		//(on a serious note, det var det här elelr skapa ett nytt tema)
		getActionBar().setIcon(android.R.color.transparent);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.accent_color));

		FloatingActionButton setup_fab = (FloatingActionButton) findViewById(R.id.setup_fab);
		setup_fab.setDrawable(getResources().getDrawable(R.drawable.ic_check_white));
		setup_fab.setColor(getResources().getColor(R.color.accent_color));

		final Context ctx = this;

		setup_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finishAffinity();
				startActivity(new Intent(ctx, FeedActivity.class));
			}
		});

    }
}