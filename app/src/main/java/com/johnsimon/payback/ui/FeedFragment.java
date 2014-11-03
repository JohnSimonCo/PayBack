package com.johnsimon.payback.ui;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.adapter.FeedListAdapter;
import com.johnsimon.payback.core.NavigationDrawerItem;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.R;
import com.johnsimon.payback.util.Resource;
import com.shamanland.fab.FloatingActionButton;


public class FeedFragment extends Fragment implements DebtDetailDialogFragment.PaidBackCallback, DebtDetailDialogFragment.EditCallback {
	private static String ARG_PREFIX = Resource.prefix("FEED_FRAGMENT");

	public static FeedListAdapter adapter;
    public static FrameLayout headerView;

	public static TextView totalDebtTextView;

    private final Person person = FeedActivity.person;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        final ListView listView = (ListView) rootView.findViewById(android.R.id.list);

        headerView = (FrameLayout) rootView.findViewById(R.id.feed_list_header_master);

        totalDebtTextView = (TextView) headerView.findViewById(R.id.total_debt);

		displayTotalDebt(getActivity());

		adapter = new FeedListAdapter(getActivity(), FeedActivity.feed);
        listView.setAdapter(adapter);

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
        listView.addHeaderView(header, null, false);

        listView.setEmptyView(inflater.inflate(R.layout.list_empty_view, null));

		final FeedFragment self = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				DebtDetailDialogFragment dialog = DebtDetailDialogFragment.newInstance(FeedActivity.feed.get(position - listView.getHeaderViewsCount()));
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
		int debt = AppData.totalDebt(FeedActivity.feed);

		totalDebtTextView.setText(Debt.totalString(debt, ctx.getResources().getString(R.string.even)));
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
}
