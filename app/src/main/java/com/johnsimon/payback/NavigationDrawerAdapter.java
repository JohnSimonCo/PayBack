package com.johnsimon.payback;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class NavigationDrawerAdapter extends BaseAdapter {
	private final static NavigationDrawerItem allItem = new NavigationDrawerItemAll();
	private final ArrayList<NavigationDrawerItem> items = new ArrayList<NavigationDrawerItem>();

	private final Activity context;

	NavigationDrawerAdapter(Activity context, ArrayList<Person> people) {
		this.context = context;

		items.add(allItem);
		for(Person person : people) {
			items.add(new NavigationDrawerItem(person.toString(), person.id, null));
		}
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public NavigationDrawerItem getItem(int i) {
		return items.get(i);
	}

	@Override
	public long getItemId(int i) {
		return -1;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder holder;
        NavigationDrawerItem item = getItem(i);

		if(item == allItem) {
			return getAllView(view);
		}

		if (view == null) {
			view = context.getLayoutInflater().inflate(R.layout.navigation_drawer_list_item, null);

			holder = new ViewHolder(
				(TextView) view.findViewById(R.id.navigation_drawer_list_item_text),
				(ImageView) view.findViewById(R.id.navigation_drawer_list_item_avatar)
			);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

        if (item.image == null) {
			holder.avatar.setImageDrawable(new RoundedAvatarDrawable(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_person_placeholder)));
        } else {
            holder.avatar.setImageDrawable(new RoundedAvatarDrawable(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_772b5027830c46519a7fd8bccf4c2c94)));
        }

		holder.title.setText(item.title);

		return view;
	}

	public View getAllView(View view) {
		ViewHolder holder;
		if (view == null) {
			view = context.getLayoutInflater().inflate(R.layout.navigation_drawer_list_item, null);

			holder = new ViewHolder(
					(TextView) view.findViewById(R.id.navigation_drawer_list_item_text),
					(ImageView) view.findViewById(R.id.navigation_drawer_list_item_avatar)
			);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.avatar.setImageDrawable(new RoundedAvatarDrawable(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_people_placeholder)));

		holder.title.setText(R.string.all);
		holder.title.setTypeface(null, Typeface.BOLD);

		return view;
	}

	static class ViewHolder {
		public TextView title;
        public ImageView avatar;

		ViewHolder(TextView title, ImageView avatar) {
			this.title = title;
            this.avatar = avatar;
		}
	}
}
