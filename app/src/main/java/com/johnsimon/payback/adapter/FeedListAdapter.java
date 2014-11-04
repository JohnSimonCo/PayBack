package com.johnsimon.payback.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.R;
import com.johnsimon.payback.ui.FeedActivity;
import com.johnsimon.payback.util.Animator;
import com.johnsimon.payback.util.Resource;
import com.makeramen.RoundedImageView;

import java.util.ArrayList;

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
                    (RoundedImageView) convertView.findViewById(R.id.list_item_avatar),
                    (TextView) convertView.findViewById(R.id.list_item_paid_back),
                    (TextView) convertView.findViewById(R.id.list_item_avatar_letter),
                    (TextView) convertView.findViewById(R.id.list_item_date)
					);

            if (FeedActivity.animateListItems) {
                int offset = position;
                if (list.size() != 0) {
                    offset++;
                }
                Animator.doListAnimation(convertView, (offset * 60));
            }

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Debt debt = list.get(position);
		Person owner = debt.owner;
		Resources resources = context.getResources();

		holder.person.setText(owner.name);
		holder.note.setText(debt.note == null ? resources.getString(R.string.cash) : debt.note);
        holder.amount.setText(debt.amountAsString);
		holder.amount.setTextColor(resources.getColor(Debt.getColor(debt.amount)));

        holder.date.setText(" - " + Resource.getRelativeTimeString(context, debt.timestamp));

		Resource.createProfileImage(owner, holder.avatar, holder.avatarLetter);

		if (debt.isPaidBack) {
			holder.person.setTextColor(context.getResources().getColor(R.color.gray_text_very_light));
			holder.note.setTextColor(context.getResources().getColor(R.color.gray_oncolor_light));
			holder.amount.setTextColor(context.getResources().getColor(Debt.getDisabledColor(debt.amount)));
			holder.avatar.setAlpha(0.5f);

			if (holder.paidBack.getVisibility() == View.GONE) {

				if (animationDebt == debt) {
					Animator.expand(holder.paidBack);
					animationDebt = null;
				} else {
					Animator.expand(holder.paidBack, false);
				}
			}
		} else {
			holder.person.setTextColor(context.getResources().getColor(R.color.gray_text_normal));
			holder.note.setTextColor(context.getResources().getColor(R.color.gray_text_light));
			holder.amount.setTextColor(context.getResources().getColor(Debt.getColor(debt.amount)));
			holder.avatar.setAlpha(1f);

			if (holder.paidBack.getVisibility() == View.VISIBLE) {

				if (animationDebt == debt) {
					Animator.collapse(holder.paidBack);
					animationDebt = null;
				} else {
					Animator.collapse(holder.paidBack, false);
				}
			}
		}

		return convertView;
	}

	static class ViewHolder {
		public TextView person;
		public TextView amount;
		public TextView note;
		public RoundedImageView avatar;
		public TextView paidBack;
        public TextView avatarLetter;
        public TextView date;

		ViewHolder(TextView person, TextView amount, TextView note, RoundedImageView avatar, TextView paidBack, TextView avatarLetter, TextView date) {
			this.person = person;
			this.amount = amount;
			this.note = note;
			this.avatar = avatar;
            this.paidBack = paidBack;
            this.avatarLetter= avatarLetter;
            this.date = date;
		}
	}
}