package com.johnsimon.payback.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.ui.dialog.DebtDetailDialogFragment;
import com.johnsimon.payback.util.Resource;
import com.makeramen.RoundedImageView;

import java.util.ArrayList;

public class FeedListAdapter extends RecyclerView.Adapter<FeedListAdapter.ViewHolder> {
	public ArrayList<Debt> list;
	private final DataActivity activity;
	private final View emptyView;
	private DebtDetailDialogFragment.Callback callback;

	public static class ViewHolder extends RecyclerView.ViewHolder {

		public TextView person;
		public TextView amount;
		public TextView note;
		public RoundedImageView avatar;
		public TextView avatarLetter;
		public TextView date;
        public LinearLayout detailContainer;

		public ViewHolder(View itemView) {
			super(itemView);

			this.person = (TextView) itemView.findViewById(R.id.list_item_person);
			this.amount = (TextView) itemView.findViewById(R.id.list_item_amount);
			this.note = (TextView) itemView.findViewById(R.id.list_item_note);
			this.avatar = (RoundedImageView) itemView.findViewById(R.id.list_item_avatar);
			this.avatarLetter = (TextView) itemView.findViewById(R.id.list_item_avatar_letter);
			this.date = (TextView) itemView.findViewById(R.id.list_item_date);
            this.detailContainer = (LinearLayout) itemView.findViewById(R.id.feed_list_detail_container);
		}
	}

	public FeedListAdapter(ArrayList<Debt> debts, DataActivity _activity, DebtDetailDialogFragment.Callback _callback, View _emptyView) {
		list = debts;
		activity = _activity;
		callback = _callback;
		emptyView = _emptyView;
	}

	@Override
	public FeedListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_list_item, parent, false));
    }

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		final Debt debt = list.get(position);
		Person owner = debt.getOwner();
		Resources resources = activity.getResources();

		holder.person.setText(owner.getName());
		holder.amount.setText(activity.data.preferences.getCurrency().render(debt));

		holder.date.setText(" - " + Resource.getRelativeTimeString(activity, debt.timestamp));

		Resource.createProfileImage(activity, owner, holder.avatar, holder.avatarLetter);

		if (debt.isPaidBack()) {
			holder.person.setTextColor(activity.getResources().getColor(R.color.gray_text_very_light));
			holder.note.setTextColor(activity.getResources().getColor(R.color.gray_oncolor_light));
			holder.amount.setTextColor(activity.getResources().getColor(debt.getDisabledColor()));
			holder.avatar.setAlpha(0.5f);
		} else {
			holder.person.setTextColor(activity.getResources().getColor(R.color.gray_text_normal));
			holder.note.setTextColor(activity.getResources().getColor(R.color.gray_text_light));
			holder.amount.setTextColor(activity.getResources().getColor(debt.getColor()));
			holder.avatar.setAlpha(1f);
		}

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DebtDetailDialogFragment dialog = DebtDetailDialogFragment.newInstance(debt);
				dialog.show(activity.getFragmentManager().beginTransaction(), "dialog");
				dialog.callback = callback;
			}
		});

		holder.note.setText(debt.getNote() == null ? resources.getString(R.string.cash) : debt.getNote());

		holder.note.post(new Runnable() {
			@Override
			public void run() {
				int widthTextView2 = measureTextWidthTextView(holder.date);
				if (holder.note.getWidth() + widthTextView2 > holder.detailContainer.getWidth()) {
					holder.note.setMaxWidth(holder.note.getWidth() - widthTextView2);
                    holder.note.setEllipsize(TextUtils.TruncateAt.END);
                    holder.note.setHorizontallyScrolling(true);
				}
			}
		});
	}

	private int measureTextWidthTextView(TextView textView) {
		int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(Resource.getScreenWidth(activity), View.MeasureSpec.AT_MOST);
		int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		textView.measure(widthMeasureSpec, heightMeasureSpec);
		return textView.getMeasuredWidth();
	}

	public void checkAdapterIsEmpty () {
		if (getItemCount() == 0) {
			emptyView.setVisibility(View.VISIBLE);
		} else {
			emptyView.setVisibility(View.GONE);
		}
	}

    public void updateList(ArrayList<Debt> feed) {
        list = feed;
    }

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return list.size();
	}
}