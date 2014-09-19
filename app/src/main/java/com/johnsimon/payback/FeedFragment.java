package com.johnsimon.payback;

import android.app.ActivityOptions;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

public class FeedFragment extends Fragment {
	private static String ARG_PREFIX = Resource.prefix("FEED_FRAGMENT");

	public final static String ARG_ALL = Resource.arg(ARG_PREFIX, "ARG_ALL");
	public final static String ARG_PERSON_ID = Resource.arg(ARG_PREFIX, "ARG_PERSON_ID");

	private ArrayList<Debt> debts;

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        final ListView listView = (ListView) rootView.findViewById(R.id.feed_list);

        View headerView = getActivity().getLayoutInflater().inflate(R.layout.feed_list_header, null);

        Bundle args = getArguments();
		final boolean showAll = args.getBoolean(ARG_ALL, false);
		final Person person;
		if(showAll) {
			debts = Resource.getEntries(true, null);
			person = null;
		} else {
			UUID id = UUID.fromString(args.getString(ARG_PERSON_ID));
			person =  Resource.data.findPerson(id);
			debts = Resource.getEntries(false, person);
		}

		TextView total_debt = (TextView) headerView.findViewById(R.id.total_debt);
		int debt = AppData.totalDebt(debts);

		String totalString = Debt.totalString(debt, getResources().getString(R.string.even));
		total_debt.setText(totalString);

        listView.setAdapter(new FeedListAdapter(getActivity(), debts));

        FloatingActionButton fab = (FloatingActionButton) headerView.findViewById(R.id.feed_fab);
        fab.setColor(getResources().getColor(R.color.accent_color));
        fab.setDrawable(getResources().getDrawable(R.drawable.ic_action_content_new));

        fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), CreateDebtActivity.class)
					.putExtra(CreateDebtActivity.ARG_FROM_FEED, true);

				if(!showAll) {
					intent.putExtra(CreateDebtActivity.ARG_FROM_PERSON_ID, person.id.toString());
				}

				startActivity(intent, ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
			}
		});

        listView.addHeaderView(headerView, null, false);
        listView.setEmptyView(inflater.inflate(R.layout.list_empty_view, null));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogFragment newFragment = DebtDetailDialogFragment.newInstance(debts.get(position - listView.getHeaderViewsCount()));
                newFragment.show(getFragmentManager().beginTransaction(), "dialog");
            }
        });

		return rootView;
	}

	public static FeedFragment newInstance(NavigationDrawerItem item) {
		FeedFragment fragment = new FeedFragment();
		Bundle args = new Bundle();

		if(item.all) {
			args.putBoolean(ARG_ALL, true);
		} else {
			args.putString(ARG_PERSON_ID, item.personId.toString());
		}
		fragment.setArguments(args);
		return fragment;
	}

}
