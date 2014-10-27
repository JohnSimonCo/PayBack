package com.johnsimon.payback;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class PeopleManagerActivity extends ActionBarActivity {

	DragSortListView listView;
	ArrayAdapter<String> adapter;

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

		listView = (DragSortListView) findViewById(R.id.people_listview);

		listView.setAdapter(adapter);
		listView.setDropListener(onDrop);
		listView.setRemoveListener(onRemove);

		DragSortController controller = new DragSortController(listView);
	//	controller.setDragHandleId(R.id.imageView1);
		//controller.setClickRemoveId(R.id.);
		controller.setRemoveEnabled(false);
		controller.setSortEnabled(true);
		controller.setDragInitMode(1);
		//controller.setRemoveMode(removeMode);

		listView.setFloatViewManager(controller);
		listView.setOnTouchListener(controller);
		listView.setDragEnabled(true);

    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		}
		return super.onOptionsItemSelected(item);
	}

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			if (from != to) {
				String item = adapter.getItem(from);
				adapter.remove(item);
				adapter.insert(item, to);
			}
		}
	};

	private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener()
	{
		@Override
		public void remove(int which)
		{
			adapter.remove(adapter.getItem(which));
		}
	};

}
