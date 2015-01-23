package com.johnsimon.payback.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.ui.DebtDetailDialogFragment;
import com.johnsimon.payback.ui.FeedActivity;
import com.johnsimon.payback.util.Resource;
import com.makeramen.RoundedImageView;

import java.util.ArrayList;

public class FeedListAdapter extends RecyclerView.Adapter<FeedListAdapter.ViewHolder> {
	public ArrayList<Debt> list;
	private final DataActivity context;
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

	public FeedListAdapter(ArrayList<Debt> debts, DataActivity ctx, DebtDetailDialogFragment.Callback _callback, View _emptyView) {
		list = debts;
		context = ctx;
		callback = _callback;
		emptyView = _emptyView;
	}

	@Override
	public FeedListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		final Debt debt = list.get(position);
		final Person owner = debt.getOwner();
		final Resources resources = context.getResources();

		holder.person.setText(owner.getName());
		holder.amount.setText(context.data.getPreferences().getCurrency().render(debt));
		holder.amount.setTextColor(resources.getColor(debt.getColor()));

		holder.date.setText(" - " + Resource.getRelativeTimeString(context, debt.timestamp));

		Resource.createProfileImage(context, owner, holder.avatar, holder.avatarLetter);

		if (debt.isPaidBack()) {
			holder.person.setTextColor(context.getResources().getColor(R.color.gray_text_very_light));
			holder.note.setTextColor(context.getResources().getColor(R.color.gray_oncolor_light));
			holder.amount.setTextColor(context.getResources().getColor(debt.getDisabledColor()));
			holder.avatar.setAlpha(0.5f);
		} else {
			holder.person.setTextColor(context.getResources().getColor(R.color.gray_text_normal));
			holder.note.setTextColor(context.getResources().getColor(R.color.gray_text_light));
			holder.amount.setTextColor(context.getResources().getColor(debt.getColor()));
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
		int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.AT_MOST);
		int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		textView.measure(widthMeasureSpec, heightMeasureSpec);
		return textView.getMeasuredWidth();
	}

	private int getScreenWidth() {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size.x;
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