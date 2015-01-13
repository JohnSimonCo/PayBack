package com.johnsimon.payback.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.etiennelawlor.quickreturn.library.listeners.QuickReturnListViewOnScrollListener;
import com.johnsimon.payback.R;
import com.johnsimon.payback.adapter.FeedListAdapter;
import com.johnsimon.payback.core.DataFragment;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.Resource;
import com.shamanland.fab.FloatingActionButton;
import com.williammora.snackbar.Snackbar;

public class FeedFragment extends DataFragment implements DebtDetailDialogFragment.Callback {
	private static String ARG_PREFIX = Resource.prefix("FEED_FRAGMENT");

	public static FeedListAdapter adapter;
    public static FrameLayout headerView;

	public static TextView totalDebtTextView;
    public static TextView feed_header_balance;

    private final Person person = FeedActivity.person;

    private RecyclerView recyclerView;
    private View emptyView;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//TODO actually sort

		final View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.feed_list);
		recyclerView.setHasFixedSize(true);

		final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(layoutManager);

        headerView = (FrameLayout) rootView.findViewById(R.id.feed_list_header_master);
        feed_header_balance = (TextView) headerView.findViewById(R.id.feed_header_balance);

        totalDebtTextView = (TextView) headerView.findViewById(R.id.total_debt);

		displayTotalDebt(getActivity());

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

        final ImageView emptyViewImage = (ImageView) rootView.findViewById(R.id.feed_list_empty_view_image);
        emptyView = rootView.findViewById(R.id.feed_list_empty_view);

        ViewTreeObserver vto = emptyViewImage.getViewTreeObserver();

        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = emptyViewImage.getViewTreeObserver();

                if (rootView.getHeight() - headerView.getHeight() < emptyView.getHeight() + 40) {
                    emptyViewImage.setVisibility(View.GONE);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });

        View header = new View(getActivity());
        header.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, headerView.getLayoutParams().height));

        ImageView headerImage = (ImageView) rootView.findViewById(R.id.feed_list_image);
        if (!PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext()).getString("pref_background", "mountains").equals("mountains")) {
            headerImage.setImageDrawable(getResources().getDrawable(R.drawable.art_old));
        }

        int headerHeight = headerView.getLayoutParams().height;
        QuickReturnListViewOnScrollListener scrollListener = new QuickReturnListViewOnScrollListener(QuickReturnType.HEADER,
                headerView, -headerHeight, null, 0, headerImage);
        scrollListener.setCanSlideInIdleScrollState(false);
        recyclerView.setOnScrollListener(scrollListener);

        super.onCreateView(inflater, container, savedInstanceState);

		return rootView;
	}

    @Override
    protected void onDataReceived() {
        adapter = new FeedListAdapter(FeedActivity.feed, getActivity(), this, emptyView);
        recyclerView.setAdapter(adapter);

        adapter.checkAdapterIsEmpty();
    }

    @Override
	public void onPaidBack(Debt debt) {
		debt.isPaidBack = !debt.isPaidBack;
        storage.commit();
		adapter.notifyDataSetChanged();
		displayTotalDebt(getActivity());
	}
	public static void displayTotalDebt(Activity ctx) {
		float debt = AppData.total(FeedActivity.feed);

        if (debt == 0) {
            feed_header_balance.setVisibility(View.GONE);
        } else {
            feed_header_balance.setVisibility(View.VISIBLE);
        }

		totalDebtTextView.setText(Debt.totalString(debt, ctx.getResources().getString(R.string.even), FeedActivity.isAll(), ctx.getResources().getString(R.string.debt_free)));
	}

	private View.OnClickListener fabClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

            if (Resource.canHold(data.debts.size(), 1)) {
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
            } else {

                final Activity self = getActivity();

                new MaterialDialog.Builder(getActivity())
                        .title(getString(R.string.upgrade_title))
                        .content(getString(R.string.upgrade_text))
                        .positiveText(R.string.upgrade_confirm_text)
                        .negativeText(R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                FeedActivity.bp.purchase(self, "full_version");
                                dialog.cancel();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                dialog.cancel();
                            }
                        })
                        .show();
            }

		}
	};

	@Override
	public void onDelete(final Debt debt) {

        new MaterialDialog.Builder(getActivity())
                .content(R.string.delete_entry)
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        final int index = data.debts.indexOf(debt);
                        final int indexFeed = FeedActivity.feed.indexOf(debt);

                        Snackbar.with(getActivity().getApplicationContext())
                                .text(getString(R.string.deleted_debt))
                                .actionLabel(getString(R.string.undo))
                                .actionColor(getResources().getColor(R.color.green))
                                .actionListener(new Snackbar.ActionClickListener() {
                                    @Override
                                    public void onActionClicked() {
                                        data.debts.add(index, debt);
                                        storage.commit();
                                        if(!FeedActivity.isAll()) {
                                            FeedActivity.feed.add(indexFeed, debt);
                                        }

                                        displayTotalDebt(getActivity());
                                        adapter.notifyItemInserted(indexFeed);
                                        adapter.checkAdapterIsEmpty();
                                    }
                                })
                                .show(getActivity());

                        data.delete(debt);
                        storage.commit();
                        if(!FeedActivity.isAll()) {
                            FeedActivity.feed.remove(debt);
                        }

                        displayTotalDebt(getActivity());
                        adapter.notifyItemRemoved(index);
                        adapter.checkAdapterIsEmpty();

                        dialog.cancel();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.cancel();
                    }
                })
                .show();
	}

	@Override
	public void onMove(Debt debt, Person person) {
		data.move(debt, person);
		storage.commit();

		if (!FeedActivity.isAll()) {
			FeedActivity.feed.remove(debt);
		}

		Resource.actionComplete(getActivity());
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