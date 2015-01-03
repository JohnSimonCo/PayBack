package com.johnsimon.payback.adapter;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.Resource;
import com.makeramen.RoundedImageView;

import java.util.ArrayList;

public class PeopleListAdapter extends ArrayAdapter<Person> {
	private final Activity context;
	private View emptyView;
    private AppData data;
    private TextView managerTitle;

	public PeopleListAdapter(Activity context, View emptyView, AppData data, TextView managerTitle) {
		super(context, R.layout.people_list_item, data.people);
		this.context = context;
		this.emptyView = emptyView;
        this.data = data;
        this.managerTitle = managerTitle;
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
                    (TextView) convertView.findViewById(R.id.people_list_item_avatar_letter),
                    (TextView) convertView.findViewById(R.id.people_list_item_debts)
			);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Person person = data.people.get(position);
		holder.name.setText(person.name);
        int debts = data.feed(person).size();
        if (debts == 1) {
            holder.debtsCount.setText(debts +  " " + context.getString(R.string.debt_single));
        } else {
            holder.debtsCount.setText(debts +  " " + context.getString(R.string.debt_plural));
        }
		Resource.createProfileImage(person, holder.avatar, holder.avatarLetter);

		return convertView;
	}

	private static class ViewHolder {
		public TextView name;
		public RoundedImageView avatar;
        public TextView avatarLetter;
        public TextView debtsCount;

		ViewHolder(TextView name, RoundedImageView avatar, TextView avatarLetter, TextView debtsCount) {
			this.name = name;
			this.avatar = avatar;
            this.avatarLetter = avatarLetter;
            this.debtsCount = debtsCount;
		}
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();

        if (data.people.size() == 1) {
            managerTitle.setText("1 " + context.getString(R.string.person));
        } else {
            managerTitle.setText(data.people.size() + " " + context.getString(R.string.people));
        }

		if (data.people.size() == 0) {
			emptyView.setVisibility(View.VISIBLE);
		} else {
			emptyView.setVisibility(View.GONE);
		}
	}
}