package com.johnsimon.payback.ui;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
import com.johnsimon.payback.adapter.FeedListAdapter;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.R;
import com.johnsimon.payback.util.Resource;
import com.shamanland.fab.FloatingActionButton;
import com.williammora.snackbar.Snackbar;


public class FeedFragment extends Fragment implements DebtDetailDialogFragment.Callback {
	private static String ARG_PREFIX = Resource.prefix("FEED_FRAGMENT");

	public static FeedListAdapter adapter;
    public static FrameLayout headerView;

	public static TextView totalDebtTextView;
    public static TextView feed_header_balance;

    private final Person person = FeedActivity.person;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.feed_list);
		recyclerView.setHasFixedSize(true);

		final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(layoutManager);

        headerView = (FrameLayout) rootView.findViewById(R.id.feed_list_header_master);
        feed_header_balance = (TextView) headerView.findViewById(R.id.feed_header_balance);

        totalDebtTextView = (TextView) headerView.findViewById(R.id.total_debt);

		displayTotalDebt(getActivity());

		adapter = new FeedListAdapter(FeedActivity.feed, getActivity(), this, rootView.findViewById(R.id.feed_list_empty_view));
		recyclerView.setAdapter(adapter);

        //We're done animating.
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                FeedActivity.animateListItems = false;
            }
        };
        handler.postDelayed(r, 400);

        //FAB is different on L
        if (Resource.isLOrAbove()) {
            final ImageButton fab = (ImageButton) headerView.findViewById(R.id.feed_fab_l);

            fab.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, fab.getWidth(), fab.getHeight());
                }
            });

            fab.setClipToOutline(true);

            fab.setOnClickListener(fabClickListener);
        } else {
            FloatingActionButton fab = (FloatingActionButton) headerView.findViewById(R.id.feed_fab);
            fab.setOnClickListener(fabClickListener);
        }

        View header = new View(getActivity());
        header.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, headerView.getLayoutParams().height));

        int headerHeight = headerView.getLayoutParams().height;
        QuickReturnListViewOnScrollListener scrollListener = new QuickReturnListViewOnScrollListener(QuickReturnType.HEADER,
                headerView, -headerHeight, null, 0);
        scrollListener.setCanSlideInIdleScrollState(false);
        recyclerView.setOnScrollListener(scrollListener);

		adapter.checkAdapterIsEmpty();

		return rootView;
	}

	@Override
	public void onPaidBack(Debt debt) {
		debt.isPaidBack = !debt.isPaidBack;
		Resource.commit();
		adapter.notifyDataSetChanged();
		displayTotalDebt(getActivity());
	}
	public static void displayTotalDebt(Context ctx) {
		int debt = AppData.totalDebt(FeedActivity.feed);

        if (debt == 0) {
            feed_header_balance.setVisibility(View.GONE);
        } else {
            feed_header_balance.setVisibility(View.VISIBLE);
        }

		totalDebtTextView.setText(Debt.totalString(debt, ctx.getResources().getString(R.string.even)));
	}

	private View.OnClickListener fabClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(), CreateDebtActivity.class)
					.putExtra(CreateDebtActivity.ARG_FROM_FEED, true);

			if(!FeedActivity.isAll()) {
				intent.putExtra(CreateDebtActivity.ARG_FROM_PERSON_NAME, person.name);
			}
			if (Resource.isLOrAbove()) {
				startActivity(intent);
			} else {
				startActivity(intent, ActivityOptions.makeCustomAnimation(getActivity(), R.anim.activity_in, R.anim.activity_out).toBundle());
			}
		}
	};

	@Override
	public void onDelete(final Debt debt) {

		ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment();

		Bundle argsDelete = new Bundle();
		argsDelete.putString(ConfirmDialogFragment.INFO_TEXT, getResources().getString(R.string.delete_entry));
		argsDelete.putString(ConfirmDialogFragment.CONFIRM_TEXT, getResources().getString(R.string.delete));
		confirmDialogFragment.setArguments(argsDelete);

		confirmDialogFragment.show(getFragmentManager(), "people_detail_dialog_delete");

		confirmDialogFragment.confirm = new ConfirmDialogFragment.ConfirmCallback() {
			@Override
			public void onConfirm() {

				final int index = Resource.debts.indexOf(debt);
				final int indexFeed = FeedActivity.feed.indexOf(debt);

				Snackbar.with(getActivity())
						.text(getString(R.string.deleted_debt))
						.actionLabel(getString(R.string.undo))
						.actionColor(Color.WHITE)
						.actionListener(new Snackbar.ActionClickListener() {
							@Override
							public void onActionClicked() {
								Resource.debts.add(index, debt);
								Resource.commit();
								if(!FeedActivity.isAll()) {
									FeedActivity.feed.add(indexFeed, debt);
								}

								displayTotalDebt(getActivity());
								adapter.notifyDataSetChanged();
								adapter.checkAdapterIsEmpty();
							}
						})
						.show(getActivity());

				Resource.debts.remove(debt);
				Resource.commit();
				if(!FeedActivity.isAll()) {
					FeedActivity.feed.remove(debt);
				}

				displayTotalDebt(getActivity());
				adapter.notifyDataSetChanged();
				adapter.checkAdapterIsEmpty();
			}
		};
	}

	@Override
	public void onMove(Debt debt, Person person) {
		Resource.data.move(debt, person);
		Resource.commit();

		if (!FeedActivity.isAll()) {
			FeedActivity.feed.remove(debt);
		}

		Resource.actionComplete(getFragmentManager());
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onEdit(Debt debt) {
		Intent intent = new Intent(getActivity(), CreateDebtActivity.class)
				.putExtra(CreateDebtActivity.ARG_FROM_FEED, true)
				.putExtra(CreateDebtActivity.ARG_TIMESTAMP, debt.timestamp);

		startActivity(intent);
	}
}
