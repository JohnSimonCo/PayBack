package com.johnsimon.payback;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class PeopleListAdapter extends ArrayAdapter<Debt> {
	private final Activity context;
	private final ArrayList<Debt> list;

	public PeopleListAdapter(Activity context, ArrayList<Debt> list) {
		super(context, R.layout.feed_list_item, list);
		this.context = context;
		this.list = list;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			convertView = inflater.inflate(R.layout.people_list_item, null);

			holder = new ViewHolder(
					(TextView) convertView.findViewById(R.id.list_item_paid_back),
					(ImageView) convertView.findViewById(R.id.list_item_avatar)
			);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Debt debt = list.get(position);

		holder.person.setText(debt.owner.name);

		boolean hasAvatar = false;
		if (hasAvatar) {
			//Set avatar as image like some stupid faggot
		} else {
			holder.avatar.setImageDrawable(new RoundedAvatarDrawable(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_772b5027830c46519a7fd8bccf4c2c94)));
		}

		return convertView;
	}

	static class ViewHolder {
		public TextView person;
		public ImageView avatar;

		ViewHolder(TextView person, ImageView avatar) {
			this.person = person;
			this.avatar = avatar;
		}
	}
}