package com.johnsimon.payback.adapter;

import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.core.NavigationDrawerItem;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.R;
import com.johnsimon.payback.ui.NavigationDrawerFragment;
import com.johnsimon.payback.util.Resource;
import com.makeramen.RoundedImageView;

import java.util.ArrayList;

public class NavigationDrawerAdapter extends BaseAdapter {
	private final static NavigationDrawerItem allItem = new NavigationDrawerItem(NavigationDrawerItem.Type.All);
	private final ArrayList<NavigationDrawerItem> items = new ArrayList<>();

	private final DataActivity context;

	public NavigationDrawerAdapter(DataActivity context, ArrayList<Person> people) {
		this.context = context;

		setItems(people);
	}

	public void clearItems() {
		items.clear();
	}
	public void setItems(ArrayList<Person> people) {
		for(Person person : people) {
			items.add(new NavigationDrawerItem(person.toString(), person.id, null, person));
		}
	}

	public int selectPerson(Person person) {
		if(person == null) {
			return 0;
		}
		for(int i = 0, l = items.size(); i < l; i++) {
			NavigationDrawerItem item = items.get(i);
			if(item.personId == person.id) {
				return i + 1;
			}
		}
        return 0;
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
				: items.get(i - 1);
	}

	@Override
	public long getItemId(int i) {
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		ViewHolder holder;
		boolean isSelected = (position == NavigationDrawerFragment.mCurrentSelectedPosition);

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

		if (position == 0) {
			holder.title.setText(R.string.all);
			holder.avatar.setImageResource(R.drawable.ic_people_placeholder);
			holder.avatarLetter.setVisibility(View.GONE);
		} else {
			NavigationDrawerItem item = items.get(--position);
			Person owner = item.owner;

			Resource.createProfileImage(context, owner, holder.avatar, holder.avatarLetter);

			holder.title.setText(item.title);
		}

		if (isSelected) {
			holder.title.setTypeface(null, Typeface.BOLD);
            holder.title.setTextColor(context.getResources().getColor(R.color.green));
		} else {
			holder.title.setTypeface(null, Typeface.NORMAL);
            holder.title.setTextColor(context.getResources().getColor(R.color.gray_text_light));
		}

		return convertView;
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
