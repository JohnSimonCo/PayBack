package com.johnsimon.payback.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.johnsimon.payback.adapter.PeopleListAdapter;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.Resource;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class PeopleManagerActivity extends ActionBarActivity {

	private static String ARG_PREFIX = Resource.prefix("CREATE_DEBT");
	public static String ARG_FROM_PEOPLE_MANAGER = Resource.arg(ARG_PREFIX, "PEOPLE_MANAGER");


	private DragSortListView listView;
	private PeopleListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		//TODO this has a long way to go

		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setTintColor(getResources().getColor(R.color.primary_color_darker));

        setContentView(R.layout.activity_people_manager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.people_toolbar);
        setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		adapter = new PeopleListAdapter(this, Resource.people);

		listView = (DragSortListView) findViewById(R.id.people_listview);

		listView.setAdapter(adapter);
		listView.setDropListener(onDrop);
		//listView.setRemoveListener(onRemove);
        listView.setEmptyView(getLayoutInflater().inflate(R.layout.people_manager_empty_view, null));

		DragSortController controller = new DragSortController(listView);
		controller.setDragHandleId(R.id.people_list_item_handle);
		//controller.setClickRemoveId(R.id.);
		controller.setRemoveEnabled(false);
		controller.setSortEnabled(true);
		controller.setDragInitMode(1); //Magic number: "Jag vet inte. Det är antagligen en flagga internt eller nånting" - Simme '14
		//controller.setRemoveMode(removeMode);

		listView.setFloatViewManager(controller);
		listView.setOnTouchListener(controller);
		listView.setDragEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show items in the action bar relevant to this screen
        // if the drawer is not showing. Otherwise, let the drawer
        // decide what to show in the action bar.
        getMenuInflater().inflate(R.menu.people_manager, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home :
				returnToFeed();
				break;

		}
		return super.onOptionsItemSelected(item);
	}

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
		if (from != to) {
			Person item = adapter.getItem(from);
			adapter.remove(item);
			adapter.insert(item, to);

			commitChange();
		}
		}
	};

	private void commitChange() {
		Resource.people = adapter.list;
		NavigationDrawerFragment.adapter.updatePeople(Resource.people);
		NavigationDrawerFragment.adapter.notifyDataSetChanged();
	}

	@Override
	public void onBackPressed() {
		returnToFeed();
	}

	public void returnToFeed() {
		finishAffinity();

	}

	/*
	private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener()
	{
		@Override
		public void remove(int which)
		{
		adapter.remove(adapter.getItem(which));
		}
	};
*/
}