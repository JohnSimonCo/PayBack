package com.johnsimon.payback.adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.johnsimon.payback.core.NavigationDrawerItem;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.R;
import com.johnsimon.payback.ui.NavigationDrawerFragment;
import com.johnsimon.payback.util.Resource;
import com.makeramen.RoundedImageView;

import java.util.ArrayList;

public class NavigationDrawerAdapter extends BaseAdapter {
	private final static NavigationDrawerItem allItem = new NavigationDrawerItem(NavigationDrawerItem.Type.All);
	private final ArrayList<NavigationDrawerItem> items = new ArrayList<NavigationDrawerItem>();

	private final Activity context;

	public NavigationDrawerAdapter(Activity context, ArrayList<Person> people) {
		this.context = context;

		setItems(people);
	}

	private void setItems(ArrayList<Person> people) {
		for(Person person : people) {
			items.add(new NavigationDrawerItem(person.toString(), person.id, null, person));
		}
	}

	public void selectPerson(Person person) {
		if(person == null) {
			NavigationDrawerFragment.mCurrentSelectedPosition = 0;
			return;
		}
		for(int i = 0, l = items.size(); i < l; i++) {
			NavigationDrawerItem item = items.get(i);
			if(item.personId == person.id) {
				NavigationDrawerFragment.mCurrentSelectedPosition = i + 1;
				break;
			}
		}
	}

	@Override
	//One for allItem
	public int getCount() {
		return items.size() + 1;
	}

	@Override
	public NavigationDrawerItem getItem(int i) {
		return i == 0
			? allItem
			: items.get(--i);
	}

	@Override
	public long getItemId(int i) {
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		ViewHolder holder;
		boolean isSelected = (position == NavigationDrawerFragment.mCurrentSelectedPosition);

		if (position == 0) {
			return getAllView(convertView, isSelected);
		}

		NavigationDrawerItem item = items.get(--position);

		if (convertView == null) {
			convertView = context.getLayoutInflater().inflate(R.layout.navigation_drawer_list_item, null);

			holder = new ViewHolder(
				(TextView) convertView.findViewById(R.id.navigation_drawer_list_item_text),
				(RoundedImageView) convertView.findViewById(R.id.navigation_drawer_list_item_avatar),
				(TextView) convertView.findViewById(R.id.navigation_drawer_list_item_avatar_letter)
			);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Person owner = item.owner;

		Resource.createProfileImage(owner, holder.avatar, holder.avatarLetter);

		holder.title.setText(item.title);

        //As per the new design guidelines
		if (isSelected) {
			holder.title.setTypeface(null, Typeface.BOLD);
            holder.title.setTextColor(context.getResources().getColor(R.color.green));
		} else {
			holder.title.setTypeface(null, Typeface.NORMAL);
            holder.title.setTextColor(context.getResources().getColor(R.color.gray_text_light));
		}

		return convertView;
	}

	public View getAllView(View view, boolean isSelected) {
		ViewHolder holder;
		if (view == null) {
			view = context.getLayoutInflater().inflate(R.layout.navigation_drawer_list_item, null);

			holder = new ViewHolder(
					(TextView) view.findViewById(R.id.navigation_drawer_list_item_text),
					(RoundedImageView) view.findViewById(R.id.navigation_drawer_list_item_avatar),
					null
			);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.title.setText(R.string.all);

        if (isSelected) {
            holder.title.setTypeface(null, Typeface.BOLD);
            holder.title.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            holder.title.setTypeface(null, Typeface.NORMAL);
            holder.title.setTextColor(context.getResources().getColor(R.color.gray_text_light));
        }

		return view;
	}

	static class ViewHolder {
		public TextView title;
        public RoundedImageView avatar;
		public TextView avatarLetter;

		ViewHolder(TextView title, RoundedImageView avatar, TextView avatarLetter) {
			this.title = title;
            this.avatar = avatar;
			this.avatarLetter = avatarLetter;
		}
	}
}
