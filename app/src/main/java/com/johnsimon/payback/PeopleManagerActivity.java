package com.johnsimon.payback;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.Arrays;


public class PeopleManagerActivity extends Activity {

	DragSortListView listView;
	ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setTintColor(getResources().getColor(R.color.accent_color));

        setContentView(R.layout.activity_people_manager);

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

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener()
	{
		@Override
		public void drop(int from, int to)
		{
			if (from != to)
			{
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
