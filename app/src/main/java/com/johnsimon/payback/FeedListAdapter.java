package com.johnsimon.payback;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

public class FeedListAdapter extends ArrayAdapter<Debt> {
	private final Activity context;
	private final ArrayList<Debt> list;

	public Debt animationDebt = null;

	public FeedListAdapter(Activity context, ArrayList<Debt> list) {
		super(context, R.layout.feed_list_item, list);
		this.context = context;
		this.list = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			convertView = inflater.inflate(R.layout.feed_list_item, null);

			holder = new ViewHolder(
					(TextView) convertView.findViewById(R.id.list_item_person),
					(TextView) convertView.findViewById(R.id.list_item_amount),
					(TextView) convertView.findViewById(R.id.list_item_note),
                    (ImageView) convertView.findViewById(R.id.list_item_avatar),
					(TextView) convertView.findViewById(R.id.list_item_paid_back)
					);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Debt debt = list.get(position);
		Resources resources = context.getResources();

		holder.person.setText(debt.owner.name);
		holder.note.setText(debt.note == null ? resources.getString(R.string.cash) : debt.note);
		holder.amount.setText(debt.amountAsString);
		holder.amount.setTextColor(resources.getColor(Debt.getColor(debt.amount)));

        boolean hasAvatar = false;
        if (hasAvatar) {
            //Set avatar as image like some stupid faggot
        } else {
            holder.avatar.setImageDrawable(new RoundedAvatarDrawable(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_772b5027830c46519a7fd8bccf4c2c94)));
        }

		if (debt.isPaidBack) {
			holder.person.setTextColor(context.getResources().getColor(R.color.gray_text_very_light));
			holder.note.setTextColor(context.getResources().getColor(R.color.gray_oncolor_light));
			holder.amount.setTextColor(context.getResources().getColor(Debt.getDisabledColor(debt.amount)));
			holder.avatar.setAlpha(0.5f);

			Resource.toast(context, animationDebt == debt);

			if (holder.paidBack.getVisibility() == View.GONE) {



				if (animationDebt == debt) {
					Resource.expand(holder.paidBack);
					animationDebt = null;
				} else {
					Resource.expand(holder.paidBack, false);
				}
			}
		} else {
			holder.person.setTextColor(context.getResources().getColor(R.color.gray_text_normal));
			holder.note.setTextColor(context.getResources().getColor(R.color.gray_text_light));
			holder.amount.setTextColor(context.getResources().getColor(Debt.getColor(debt.amount)));
			holder.avatar.setAlpha(1f);


			if (holder.paidBack.getVisibility() == View.VISIBLE) {

				if (animationDebt == debt) {
					Resource.collapse(holder.paidBack);
					animationDebt = null;
				} else {
					Resource.collapse(holder.paidBack, false);
				}
			}
		}

		return convertView;
	}

	static class ViewHolder {
		public TextView person;
		public TextView amount;
		public TextView note;
		public ImageView avatar;
		public TextView paidBack;

		ViewHolder(TextView person, TextView amount, TextView note, ImageView avatar, TextView paidBack) {
			this.person = person;
			this.amount = amount;
			this.note = note;
			this.avatar = avatar;
			this.paidBack = paidBack;
		}
	}
}