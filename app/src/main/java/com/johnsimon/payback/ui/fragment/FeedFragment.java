package com.johnsimon.payback.ui.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.async.Subscription;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.ui.CreateDebtActivity;
import com.johnsimon.payback.ui.FeedActivity;
import com.johnsimon.payback.ui.dialog.DebtDetailDialogFragment;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.Undo;

import java.util.ArrayList;

public class FeedFragment extends DataFragment implements FeedListAdapter.OnItemClickListener, DebtDetailDialogFragment.Callback {

	public FeedListAdapter adapter;
    public FrameLayout headerView;

	private TextView totalDebtTextView;
    private TextView feed_header_balance;
    private FloatingActionButton feed_fab;

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;
    private View emptyView;
	public QuickReturnListViewOnScrollListener scrollListener;

	private ImageView headerImage;

	private Subscription<ArrayList<Debt>> feedSubscription;
	private Notification feedLinkedNotification;

    public OnFeedChangeCallback feedChangeCallback;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.feed_list);
		recyclerView.setHasFixedSize(true);

		layoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(layoutManager);

        headerView = (FrameLayout) rootView.findViewById(R.id.feed_list_header_master);
        feed_header_balance = (TextView) headerView.findViewById(R.id.feed_header_balance);

        totalDebtTextView = (TextView) headerView.findViewById(R.id.total_debt);

        feed_fab = (FloatingActionButton) headerView.findViewById(R.id.feed_fab);
        feed_fab.setOnClickListener(fabClickListener);

        if (Resource.isLOrAbove()) {
            feed_fab.setTransitionName("fab");
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
        scrollListener = new QuickReturnListViewOnScrollListener(QuickReturnType.HEADER,
                headerView, -headerHeight, null, 0, headerImage);
        scrollListener.setCanSlideInIdleScrollState(false);
        recyclerView.addOnScrollListener(scrollListener);

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
		displayTotalDebt(getResources());

		if (data.preferences.getBackground().equals("mountains")) {
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
	public void onRefresh() {
		adapter.notifyDataSetChanged();
        feedChangeCallback.onFeedChange();
		displayTotalDebt(getResources());

		Resource.actionComplete(getActivity());
	}

    public void displayTotalDebt(Resources resources) {
		float total = AppData.total(FeedActivity.feed);

        if (total == 0) {
            feed_header_balance.setVisibility(View.GONE);
        } else {
            feed_header_balance.setVisibility(View.VISIBLE);
        }

		totalDebtTextView.setText(Debt.totalString(total, data.preferences.getCurrency(), resources.getString(R.string.even), FeedActivity.isAll(), resources.getString(R.string.debt_free)));
	}

	private View.OnClickListener fabClickListener = new View.OnClickListener() {
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
		public void onClick(View v) {

            if (Resource.canHold(data.debts.size(), 1)) {
                Intent intent = new Intent(getActivity(), CreateDebtActivity.class)
                        .putExtra(CreateDebtActivity.ARG_FROM_FEED, true);

                if(!FeedActivity.isAll()) {
                    intent.putExtra(CreateDebtActivity.ARG_FROM_PERSON_NAME, FeedActivity.person.getName());
                }
                if (Resource.isLOrAbove()) {

                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                            v, "fab");

                    startActivity(intent, transitionActivityOptions.toBundle());
                } else {
                    startActivity(intent, ActivityOptions.makeCustomAnimation(getActivity(), R.anim.activity_in, R.anim.activity_out).toBundle());
                }
            } else {

                new MaterialDialog.Builder(getActivity())
                        .title(getString(R.string.upgrade_title))
                        .content(getString(R.string.upgrade_text))
                        .positiveText(R.string.upgrade_confirm_text)
                        .negativeText(R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
								((FeedActivity) getActivity()).purchaseFullVersion();
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

    public void showDetail(Debt debt) {
        DebtDetailDialogFragment detailDialogFragment = new DebtDetailDialogFragment();
		Bundle args = new Bundle();
		args.putString(DebtDetailDialogFragment.ARG_ID, debt.id.toString());
		detailDialogFragment.setArguments(args);
        detailDialogFragment.callback = this;
        detailDialogFragment.show(getFragmentManager(), "detail_screen");
    }

    @Override
	public void onDelete(final Debt debt) {
		final int index = FeedActivity.feed.indexOf(debt);

		final Activity activity = getActivity();

        new MaterialDialog.Builder(activity)
			.content(R.string.delete_entry)
			.positiveText(R.string.delete)
			.negativeText(R.string.cancel)
			.callback(new MaterialDialog.ButtonCallback() {
				@Override
				public void onPositive(MaterialDialog dialog) {
					super.onPositive(dialog);
					Undo.executeAction(getBaseActivity(), R.string.deleted_debt, getView(), new Undo.UndoableAction() {
						@Override
						public void onDisplay() {
							FeedActivity.feed.remove(index);
							adapter.notifyItemRemoved(index);
							adapter.checkAdapterIsEmpty();
                            feedChangeCallback.onFeedChange();
						}

						@Override
						public void onRevert() {
							FeedActivity.feed.add(index, debt);
							adapter.notifyItemInserted(index);
							adapter.checkAdapterIsEmpty();
                            feedChangeCallback.onFeedChange();
						}

						@Override
						public void onCommit() {
							data.delete(activity, debt);
							storage.commit(getActivity());
						}
					});

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
		storage.commit(getActivity());

		if (!FeedActivity.isAll()) {
            int index = FeedActivity.feed.indexOf(debt);
			FeedActivity.feed.remove(index);
            feedChangeCallback.onFeedChange();
		}

		((FeedActivity) getActivity()).changePerson(person);

		Resource.actionComplete(getActivity());
		adapter.notifyDataSetChanged();
        feedChangeCallback.onFeedChange();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
	public void onEdit(Debt debt) {
		Intent intent = new Intent(getActivity(), CreateDebtActivity.class)
				.putExtra(CreateDebtActivity.ARG_FROM_FEED, true)
                .putExtra(CreateDebtActivity.ARG_ANIMATE_TOOLBAR, false)
				.putExtra(CreateDebtActivity.ARG_ID, debt.id);

        if (Resource.isLOrAbove()) {
            ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), feed_fab, "fab");

            startActivity(intent, transitionActivityOptions.toBundle());
        } else {
            startActivity(intent, ActivityOptions.makeCustomAnimation(getActivity(), R.anim.activity_in, R.anim.activity_out).toBundle());
        }
	}

    @Override
    public void onItemClick(View view, int position, Debt debt) {
        showDetail(debt);
    }

    public interface OnFeedChangeCallback {
        void onFeedChange();
    }

}