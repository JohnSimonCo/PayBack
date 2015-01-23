package com.johnsimon.payback.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
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
import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Notification;
import com.johnsimon.payback.async.NotificationCallback;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.core.DataFragment;
import com.johnsimon.payback.core.UserCurrency;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.async.Subscription;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.Undo;
import com.shamanland.fab.FloatingActionButton;
import com.williammora.snackbar.Snackbar;

import java.util.ArrayList;

public class FeedFragment extends DataFragment implements DebtDetailDialogFragment.Callback {
	private static String ARG_PREFIX = Resource.prefix("FEED_FRAGMENT");

	public static FeedListAdapter adapter;
    public static FrameLayout headerView;

	public static TextView totalDebtTextView;
    public static TextView feed_header_balance;

    public RecyclerView recyclerView;
    private View emptyView;

	ImageView headerImage;

	private Subscription<ArrayList<Debt>> feedSubscription;
	private Notification feedLinkedNotification;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.feed_list);
		recyclerView.setHasFixedSize(true);

		final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(layoutManager);

        headerView = (FrameLayout) rootView.findViewById(R.id.feed_list_header_master);
        feed_header_balance = (TextView) headerView.findViewById(R.id.feed_header_balance);

        totalDebtTextView = (TextView) headerView.findViewById(R.id.total_debt);

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

        headerImage = (ImageView) rootView.findViewById(R.id.feed_list_image);

        int headerHeight = headerView.getLayoutParams().height;
        QuickReturnListViewOnScrollListener scrollListener = new QuickReturnListViewOnScrollListener(QuickReturnType.HEADER,
                headerView, -headerHeight, null, 0, headerImage);
        scrollListener.setCanSlideInIdleScrollState(false);
        recyclerView.setOnScrollListener(scrollListener);

		FeedActivity host = (FeedActivity) getActivity();
		feedSubscription = host.feedSubscription;
		feedLinkedNotification = host.feedLinkedNotification;

        super.onCreateView(inflater, container, savedInstanceState);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		feedSubscription.listen(onFeedCallback);
		feedLinkedNotification.listen(onFeedLinkedCallback);
	}

	@Override
	public void onStop() {
		super.onStop();

		feedSubscription.unregister(onFeedCallback);
		feedLinkedNotification.unregister(onFeedLinkedCallback);
	}

	private FeedFragment self = this;
	Callback<ArrayList<Debt>> onFeedCallback = new Callback<ArrayList<Debt>>() {
		@Override
		public void onCalled(ArrayList<Debt> feed) {
            if (recyclerView.getAdapter() == null) {
                adapter = new FeedListAdapter(feed, (DataActivity) getActivity(), self, emptyView);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateList(feed);
                adapter.notifyDataSetChanged();
            }

			adapter.checkAdapterIsEmpty();
		}
	};

	@Override
	protected void onDataReceived() {
		displayTotalDebt(getResources(), data.getPreferences().getCurrency());

		if (data.getPreferences().getBackground().equals("mountains")) {
			headerImage.setImageResource(R.drawable.art);
		} else {
			headerImage.setImageResource(R.drawable.art_old);
		}
	}

	NotificationCallback onFeedLinkedCallback = new NotificationCallback() {
		@Override
		public void onNotify() {
			adapter.notifyDataSetChanged();
		}
	};

    @Override
	public void onPaidBack(Debt debt) {
		debt.setPaidBack(!debt.isPaidBack());
        storage.commit();
		adapter.notifyDataSetChanged();

		if (getActivity() != null && getResources() != null) {
			if (getResources() != null) {
				displayTotalDebt(getResources(), data.getPreferences().getCurrency());
			}
		}
	}
	public static void displayTotalDebt(Resources resources, UserCurrency currency) {
		float debt = AppData.total(FeedActivity.feed);

        if (debt == 0) {
            feed_header_balance.setVisibility(View.GONE);
        } else {
            feed_header_balance.setVisibility(View.VISIBLE);
        }

		totalDebtTextView.setText(Debt.totalString(debt, currency, resources.getString(R.string.even), FeedActivity.isAll(), resources.getString(R.string.debt_free)));
	}

	private View.OnClickListener fabClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

            if (Resource.canHold(data.debts.size(), 1)) {
                Intent intent = new Intent(getActivity(), CreateDebtActivity.class)
                        .putExtra(CreateDebtActivity.ARG_FROM_FEED, true);

                if(!FeedActivity.isAll()) {
                    intent.putExtra(CreateDebtActivity.ARG_FROM_PERSON_NAME, FeedActivity.person.getName());
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
                                dialog.dismiss();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                            }
                        })
                        .show();
            }

		}
	};

	@Override
	public void onDelete(final Debt debt) {

		final int index = FeedActivity.feed.indexOf(debt);

        new MaterialDialog.Builder(getActivity())
			.content(R.string.delete_entry)
			.positiveText(R.string.delete)
			.negativeText(R.string.cancel)
			.callback(new MaterialDialog.ButtonCallback() {
				@Override
				public void onPositive(MaterialDialog dialog) {
					super.onPositive(dialog);

					Undo.executeAction(getActivity(), R.string.deleted_debt, new Undo.UndoableAction() {
						@Override
						public void onDisplay() {
							FeedActivity.feed.remove(index);
							adapter.notifyItemRemoved(index);
							adapter.checkAdapterIsEmpty();

							displayTotalDebt(getResources(), data.getPreferences().getCurrency());
						}

						@Override
						public void onRevert() {
							FeedActivity.feed.add(index, debt);
							adapter.notifyItemInserted(index);
							adapter.checkAdapterIsEmpty();

							displayTotalDebt(getResources(), data.getPreferences().getCurrency());
						}

						@Override
						public void onCommit() {
							data.delete(debt);
							storage.commit();
						}
					});

					/*
					data.delete(debt);
					storage.commit();
					if(!FeedActivity.isAll()) {
						FeedActivity.feed.remove(debt);
					}

					if (getActivity() != null && getResources() != null) {
						displayTotalDebt(getResources());
					}
					adapter.notifyItemRemoved(index);
					adapter.checkAdapterIsEmpty();
					*/

					dialog.dismiss();
				}

				@Override
				public void onNegative(MaterialDialog dialog) {
					super.onNegative(dialog);
					dialog.dismiss();
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
				.putExtra(CreateDebtActivity.ARG_ID, debt.id);

		startActivity(intent);
	}
}