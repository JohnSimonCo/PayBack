package com.johnsimon.payback.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.Resource;
import com.makeramen.RoundedImageView;

import java.util.ArrayList;

public class PeopleListAdapter extends ArrayAdapter<Person> {
	public final ArrayList<Person> people;
	private final Activity context;
	private View emptyView;

	public PeopleListAdapter(Activity context, ArrayList<Person> people, View emptyView) {
		super(context, R.layout.people_list_item, people);
		this.context = context;
		this.people = people;
		this.emptyView = emptyView;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			convertView = inflater.inflate(R.layout.people_list_item, null);

			holder = new ViewHolder(
					(TextView) convertView.findViewById(R.id.people_list_item_name),
					(RoundedImageView) convertView.findViewById(R.id.people_list_item_avatar),
                    (TextView) convertView.findViewById(R.id.people_list_item_avatar_letter)
			);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Person person = people.get(position);

		holder.name.setText(person.name);

		Resource.createProfileImage(person, holder.avatar, holder.avatarLetter);

		return convertView;
	}

	static class ViewHolder {
		public TextView name;
		public RoundedImageView avatar;
        public TextView avatarLetter;

		ViewHolder(TextView name, RoundedImageView avatar, TextView avatarLetter) {
			this.name = name;
			this.avatar = avatar;
            this.avatarLetter = avatarLetter;
		}
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();

		if (people.size() == 0) {
			emptyView.setVisibility(View.VISIBLE);
		} else {
			emptyView.setVisibility(View.GONE);
		}
	}
}