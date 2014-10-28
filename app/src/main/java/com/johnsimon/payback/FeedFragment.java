package com.johnsimon.payback;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
import com.shamanland.fab.FloatingActionButton;

import java.util.ArrayList;

public class FeedFragment extends Fragment implements DebtDetailDialogFragment.PaidBackCallback, DebtDetailDialogFragment.EditCallback {
	private static String ARG_PREFIX = Resource.prefix("FEED_FRAGMENT");

	public static ArrayList<Debt> debts;
	public static FeedListAdapter adapter;
    public static RelativeLayout headerView;

	public static TextView total_debt;

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        final ListView listView = (ListView) rootView.findViewById(android.R.id.list);

        headerView = (RelativeLayout) rootView.findViewById(R.id.feed_list_header_master);

		debts = FeedActivity.feed;
		final Person person = FeedActivity.person;

        total_debt = (TextView) headerView.findViewById(R.id.total_debt);

		displayTotalDebt(getActivity());

		adapter = new FeedListAdapter(getActivity(), debts);
        listView.setAdapter(adapter);

        //We're done animating.
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                FeedActivity.animateListItems = false;
            }
        };
        handler.postDelayed(r, 400);

        FloatingActionButton fab = (FloatingActionButton) headerView.findViewById(R.id.feed_fab);

        fab.setOnClickListener(new View.OnClickListener() {
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
		});

        View header = new View(getActivity());
        header.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, headerView.getLayoutParams().height));
        listView.addHeaderView(header, null, false);

        listView.setEmptyView(inflater.inflate(R.layout.list_empty_view, null));

		final FeedFragment self = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				DebtDetailDialogFragment dialog = DebtDetailDialogFragment.newInstance(debts.get(position - listView.getHeaderViewsCount()));
                dialog.show(getFragmentManager().beginTransaction(), "dialog");
				dialog.paidBackCallback = self;
				dialog.editCallback = self;
            }
        });

        int headerHeight = headerView.getLayoutParams().height;
        QuickReturnListViewOnScrollListener scrollListener = new QuickReturnListViewOnScrollListener(QuickReturnType.HEADER,
                headerView, -headerHeight, null, 0);
        // Setting to true will slide the header and/or footer into view or slide out of view based
        // on what is visible in the idle scroll state
        scrollListener.setCanSlideInIdleScrollState(false);
        listView.setOnScrollListener(scrollListener);

		return rootView;
	}

	public static FeedFragment newInstance(NavigationDrawerItem item) {
		FeedFragment fragment = new FeedFragment();

		return fragment;
	}

	@Override
	public void onPaidBack(Debt debt) {
		debt.isPaidBack = !debt.isPaidBack;
		Resource.commit();
		adapter.animationDebt = debt;
		adapter.notifyDataSetChanged();
		displayTotalDebt(getActivity());
	}
	public static void displayTotalDebt(Context ctx) {
		int debt = AppData.totalDebt(debts);

		total_debt.setText(Debt.totalString(debt, ctx.getResources().getString(R.string.even)));
	}
	@Override
	public void onDelete(Debt debt) {
		Resource.debts.remove(debt);
		Resource.commit();
		adapter.notifyDataSetChanged();
		displayTotalDebt(getActivity());
	}

	@Override
	public void onEdit(Debt debt) {
		Intent intent = new Intent(getActivity(), CreateDebtActivity.class)
				.putExtra(CreateDebtActivity.ARG_FROM_FEED, true)
				.putExtra(CreateDebtActivity.ARG_TIMESTAMP, debt.timestamp);

		startActivity(intent);
	}
}
