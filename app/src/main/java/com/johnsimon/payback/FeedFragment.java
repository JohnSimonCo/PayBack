package com.johnsimon.payback;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;

import java.util.ArrayList;
import java.util.UUID;

public class FeedFragment extends Fragment implements DebtDetailDialogFragment.PaidBackCallback, DebtDetailDialogFragment.EditCallback {
	private static String ARG_PREFIX = Resource.prefix("FEED_FRAGMENT");

	public final static String ARG_ALL = Resource.arg(ARG_PREFIX, "ARG_ALL");
	public final static String ARG_PERSON_ID = Resource.arg(ARG_PREFIX, "ARG_PERSON_ID");

	private ArrayList<Debt> debts;
	private FeedListAdapter adapter;
	private ActionBar actionBar;

	private TextView total_debt;

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        final ListView listView = (ListView) rootView.findViewById(android.R.id.list);

		try {
			actionBar = getActivity().getActionBar();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}

        RelativeLayout headerView = (RelativeLayout) rootView.findViewById(R.id.feed_list_header_master);

		final boolean showAll;
		final Person person;
		Intent intent = getActivity().getIntent();
		Bundle args = getArguments();
		if(args.getBoolean(ARG_ALL, false)){
			showAll = true;
			person = null;
			debts = Resource.debts;
		} else {
			showAll = false;
			String uuid = null;
			if(intent.hasExtra(FeedActivity.ARG_GOTO_PERSON_ID)) {
				uuid = intent.getStringExtra(FeedActivity.ARG_GOTO_PERSON_ID);
				intent.removeExtra(FeedActivity.ARG_GOTO_PERSON_ID);
			} else if(args.containsKey(ARG_PERSON_ID)) {
				uuid = args.getString(ARG_PERSON_ID);
			}
			person = Resource.data.findPerson(UUID.fromString(uuid));
			debts = Resource.data.personalizedFeed(person);
		}

        total_debt = (TextView) headerView.findViewById(R.id.total_debt);

		displayTotalDebt();

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
        fab.setColor(getResources().getColor(R.color.accent_color));
        fab.setDrawable(getResources().getDrawable(R.drawable.ic_action_content_new));

        fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), CreateDebtActivity.class)
					.putExtra(CreateDebtActivity.ARG_FROM_FEED, true);

				if(!showAll) {
					intent.putExtra(CreateDebtActivity.ARG_FROM_PERSON_NAME, person.name);
				}

				startActivity(intent);
			}
		});

        View footer = new View(getActivity());
        footer.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, headerView.getLayoutParams().height));
        listView.addHeaderView(footer, null, false);

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

        int headerHeight = headerView.getLayoutParams().height;//getActivity().getResources().getDimensionPixelSize(R.dimen.header_height);
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
		Bundle args = new Bundle();

		if(item.type == NavigationDrawerItem.Type.All) {
			args.putBoolean(ARG_ALL, true);
		} else if(item.type == NavigationDrawerItem.Type.Person) {
			args.putString(ARG_PERSON_ID, item.personId.toString());
		}
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onPaidBack(Debt debt) {
		debt.isPaidBack = !debt.isPaidBack;
		Resource.commit();
		adapter.animationDebt = debt;
		adapter.notifyDataSetChanged();
		displayTotalDebt();
	}
	public void displayTotalDebt() {
		int debt = AppData.totalDebt(debts);

		total_debt.setText(Debt.totalString(debt, getResources().getString(R.string.even)));
	}
	@Override
	public void onDelete(Debt debt) {
		Resource.debts.remove(debt);
		Resource.commit();
		adapter.notifyDataSetChanged();
		displayTotalDebt();
	}

	@Override
	public void onEdit(Debt debt) {
		Intent intent = new Intent(getActivity(), CreateDebtActivity.class)
				.putExtra(CreateDebtActivity.ARG_FROM_FEED, true)
				.putExtra(CreateDebtActivity.ARG_TIMESTAMP, debt.timestamp);

		startActivity(intent);
	}
}
