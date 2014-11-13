package com.johnsimon.payback.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.ui.DebtDetailDialogFragment;
import com.johnsimon.payback.ui.FeedActivity;
import com.johnsimon.payback.util.Resource;
import com.makeramen.RoundedImageView;

import java.util.ArrayList;

public class FeedListAdapterRecycler extends RecyclerView.Adapter<FeedListAdapterRecycler.ViewHolder> {
	private final ArrayList<Debt> list;
	private final Activity context;
	private DebtDetailDialogFragment.Callback callback;

	public static class ViewHolder extends RecyclerView.ViewHolder {

		public TextView person;
		public TextView amount;
		public TextView note;
		public RoundedImageView avatar;
		public TextView avatarLetter;
		public TextView date;

		public ViewHolder(View itemView) {
			super(itemView);

			this.person = (TextView) itemView.findViewById(R.id.list_item_person);
			this.amount = (TextView) itemView.findViewById(R.id.list_item_amount);
			this.note = (TextView) itemView.findViewById(R.id.list_item_note);
			this.avatar = (RoundedImageView) itemView.findViewById(R.id.list_item_avatar);
			this.avatarLetter = (TextView) itemView.findViewById(R.id.list_item_avatar_letter);
			this.date = (TextView) itemView.findViewById(R.id.list_item_date);
		}
	}

	public FeedListAdapterRecycler(ArrayList<Debt> debts, Activity ctx, DebtDetailDialogFragment.Callback _callback) {
		list = debts;
		context = ctx;
		callback = _callback;
	}

	@Override
	public FeedListAdapterRecycler.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
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
		} else {
			holder.person.setTextColor(context.getResources().getColor(R.color.gray_text_normal));
			holder.note.setTextColor(context.getResources().getColor(R.color.gray_text_light));
			holder.amount.setTextColor(context.getResources().getColor(Debt.getColor(debt.amount)));
			holder.avatar.setAlpha(1f);
		}

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DebtDetailDialogFragment dialog = DebtDetailDialogFragment.newInstance(FeedActivity.feed.get(position));
				dialog.show(context.getFragmentManager().beginTransaction(), "dialog");
				dialog.callback = callback;
			}
		});

	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return list.size();
	}
}