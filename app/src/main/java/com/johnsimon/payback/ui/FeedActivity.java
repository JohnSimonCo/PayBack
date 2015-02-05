package com.johnsimon.payback.ui;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.johnsimon.payback.BuildConfig;
import com.johnsimon.payback.R;
import com.johnsimon.payback.async.Notification;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.currency.UserCurrency;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.core.NavigationDrawerItem;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.async.Subscription;
import com.johnsimon.payback.data.User;
import com.johnsimon.payback.send.DebtSendable;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.storage.StorageManager;
import com.johnsimon.payback.ui.dialog.AboutDialogFragment;
import com.johnsimon.payback.ui.dialog.CurrencyDialogFragment;
import com.johnsimon.payback.ui.dialog.FromWhoDialogFragment;
import com.johnsimon.payback.ui.dialog.WelcomeDialogFragment;
import com.johnsimon.payback.ui.fragment.FeedFragment;
import com.johnsimon.payback.ui.fragment.NavigationDrawerFragment;
import com.johnsimon.payback.util.Beamer;
import com.johnsimon.payback.util.ColorPalette;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.SwishLauncher;
import com.johnsimon.payback.util.Undo;

import java.util.ArrayList;
import java.util.Collections;

public class FeedActivity extends DataActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks, Beamer.BeamListener,
        BillingProcessor.IBillingHandler, CurrencyDialogFragment.CurrencySelectedCallback {

    public BillingProcessor bp;

	private static String ARG_PREFIX = Resource.prefix("FEED");
	public static String ARG_FROM_CREATE = Resource.arg(ARG_PREFIX, "FROM_CREATE");

	public Toolbar toolbar;
	public static Person person = null;
	public static ArrayList<Debt> feed;

	public Subscription<ArrayList<Debt>> feedSubscription = new Subscription<>();
	public Notification feedLinkedNotification = new Notification();

	private MenuItem filterAmount;
	public static MenuItem detailMenuPay;

	private NavigationDrawerFragment navigationDrawerFragment;

	private NfcAdapter nfcAdapter;
	private Beamer beamer;

	private FeedFragment feedFragment;

    private boolean attemptCheckFilterAmount = false;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent sentIntent = getIntent();

        if (sentIntent.getBooleanExtra(ARG_FROM_CREATE, false)) {
            Resource.actionComplete(this);
            sentIntent.removeExtra(ARG_FROM_CREATE);
        }

        bp = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsrcl2UtkJQ4UkkI9Az7rW4jXcxWHR+AWh+5MIa2byY9AkfiNL7HYsUB7T6KMUmjsdpUYcGKw4TuiVUMUu8hy4TlhTZ0Flitx4h7yCxJgPBiUGC34CO1f6Yk0n2LBnJCLKKwrIasnpteqTxWvWLEsPdhxjQgURDmTpR2RCAsNb1Zzn07U2PSQE07Qo34SvA4kr+VCb5pPpJ/+OodQJSdIKka56bBMpS5Ea+2iYbTfsch8nnghZTnwr6dOieOSqWnMtBPQp5VV8kj1tHd/0iaQrYVmtqnkpQ+mG/3/p55gxJUdv9uGNbF0tzMytSxyvXfICnd4oMYK66DurLfNDXoc3QIDAQAB", this);

        Resource.checkFull(bp);

        if (Resource.isLOrAbove()) {
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_launcher), getResources().getColor(R.color.primary_color)));

        }

		Resource.init(this);
		if (Resource.isFirstRun(storage.getPreferences())) {
			WelcomeDialogFragment welcomeDialogFragment = new WelcomeDialogFragment();
			welcomeDialogFragment.show(getFragmentManager().beginTransaction(), "welcome_dialog_fragment");
		}

		setContentView(R.layout.activity_feed);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);

		// Set up the drawer.
		navigationDrawerFragment.setUp(
				R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout)
		);

		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		beamer = new Beamer(this, this);
		if (nfcAdapter != null) {
			nfcAdapter.setNdefPushMessageCallback(beamer, this);
		}

		View feed_activity_status_bar_pusher = findViewById(R.id.feed_activity_status_bar_pusher);

		if (Resource.isKkOrAbove()) {
			int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
			if (resourceId > 0) {
				feed_activity_status_bar_pusher.getLayoutParams().height = getResources().getDimensionPixelSize(resourceId);
			}
			if (!Resource.isLOrAbove()) {
				feed_activity_status_bar_pusher.setBackgroundColor(getResources().getColor(R.color.primary_color_darker));
			}
		} else {
			feed_activity_status_bar_pusher.setVisibility(View.GONE);
		}

		getFragmentManager().beginTransaction()
				.replace(R.id.container, feedFragment = new FeedFragment(), "feed_fragment_tag")
				.commit();

        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "DEBUG", Toast.LENGTH_SHORT).show();
        }
	}

    @Override
    protected void onDataReceived() {
        if (isAll()) {
            feed = data.debts;
        } else {
            feed = data.feed(person);
        }
        sort();
		feedSubscription.broadcast(feed);
		getSupportActionBar().setSubtitle(isAll() ? getString(R.string.all) : person.getName());
    }

    @Override
    protected void onDataLinked() {
        NavigationDrawerFragment.adapter.notifyDataSetChanged();

		feedLinkedNotification.broadcast();
    }

	public static boolean isAll() {
		return person == null;
	}
	public static void gotoAll() {
		person = null;
	}

    @Override
	public void onResume() {
		super.onResume();

		// Check to see that the Activity started due to an Android Beam
		Intent intent = getIntent();
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			beamer.processIntent(intent);
		}
    }

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	@Override
	public void onNavigationDrawerItemSelected(NavigationDrawerItem item) {
		Undo.completeAction();

		Person oldPerson = person;

		if(item.type == NavigationDrawerItem.Type.All) {
			person = null;
		} else if(item.type == NavigationDrawerItem.Type.Person) {
			person = item.owner;
		}

        if (person == oldPerson)
            return;

        feed = data.feed(person);
        sort();
		feedSubscription.broadcast(feed);
		feedLinkedNotification.broadcast();

		getSupportActionBar().setSubtitle(isAll() ? getString(R.string.all) : person.getName());

		storage.requestRefresh();

        invalidateOptionsMenu();

		feedFragment.recyclerView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_in));

        feedFragment.displayTotalDebt(getResources(), data.preferences.getCurrency());

	}

	@Override
	public void onShowGlobalContextActionBar() {}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Only show items in the action bar relevant to this screen
		// if the drawer is not showing. Otherwise, let the drawer
		// decide what to show in the action bar.
		getMenuInflater().inflate(R.menu.feed, menu);

		filterAmount = menu.findItem(R.id.menu_filter_amount);
        MenuItem fulllMenuPay = menu.findItem(R.id.feed_menu_pay_back);

        if (attemptCheckFilterAmount) {
            filterAmount.setChecked(true);
        }

        sort();

		if (isAll()) {
			fulllMenuPay.setVisible(false);
		} else {
            if (!SwishLauncher.hasService(this) || AppData.total(feed) >= 0) {
                fulllMenuPay.setEnabled(false);
            } else {
                fulllMenuPay.setEnabled(true);
			}

		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case R.id.menu_filter_time:
				item.setChecked(true);
				sortTime();
				break;

			case R.id.menu_filter_amount:
				item.setChecked(true);
				sortAmount();
				break;

			case R.id.feed_menu_pay_back:
				SwishLauncher.startSwish(this, AppData.total(feed), person);
				break;

		}

		return result;
	}

    private void sort() {
        if (filterAmount == null)
            return;

        if (filterAmount.isChecked()) {
            sortAmount();
        } else {
            sortTime();
        }
    }

	/*  This method is called by /res/navigation_drawer_list_footer.xml
		to either navigate to settings or show the "About screen". New
		items can be added here by adding the same onClick=""
	 */
	public void navigationDrawerFooterClick(View v) {

		switch (v.getId()) {
			case R.id.navigation_drawer_footer_people:
				startActivity(new Intent(this, PeopleManagerActivity.class));
				break;
			case R.id.navigation_drawer_footer_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				break;
			case R.id.navigation_drawer_footer_about:
				AboutDialogFragment aboutDialogFragment = new AboutDialogFragment();
				aboutDialogFragment.show(getFragmentManager(), "about_dialog");
				break;
            case R.id.navigation_drawer_footer_upgrade:

                final FeedActivity self = this;

                new MaterialDialog.Builder(this)
                        .title(getString(R.string.upgrade_title))
                        .content(getString(R.string.upgrade_text))
                        .positiveText(R.string.upgrade_confirm_text)
                        .negativeText(R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                bp.purchase(self, "full_version");
                                dialog.dismiss();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
		}
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		navigationDrawerFragment.mDrawerToggle.syncState();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        if (filterAmount != null) {
            outState.putBoolean("AMOUNT_USED_SORT", filterAmount.isChecked());
        }
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (savedInstanceState.getBoolean("AMOUNT_USED_SORT", false)) {
            attemptCheckFilterAmount = true;
			sortAmount();
		}
	}

	public void sortTime() {
		Collections.sort(feedFragment.adapter.list, new Resource.TimeComparator());
        feedFragment.adapter.notifyDataSetChanged();
	}

	public void sortAmount() {
		Collections.sort(feedFragment.adapter.list, new Resource.AmountComparator());
        feedFragment.adapter.notifyDataSetChanged();
	}

	@Override
	public void onReceivedBeam(final DebtSendable[] debts, final User sender, final boolean fullSync) {

        if(!Resource.canHold(data.debts.size(), debts.length)) {

            final FeedActivity self = this;

            new MaterialDialog.Builder(this)
                    .title(getString(R.string.upgrade_title))
                    .content(getString(R.string.upgrade_text))
                    .positiveText(R.string.upgrade_confirm_text)
                    .negativeText(R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            bp.purchase(self, "full_version");
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                        }
                    })
                    .show();
            return;
        }

        FromWhoDialogFragment fragment = new FromWhoDialogFragment();

		Bundle arguments = new Bundle();
		arguments.putString(FromWhoDialogFragment.KEY_NAME, data.guessName(user, sender));
		fragment.setArguments(arguments);

		fragment.show(getFragmentManager(), "from_who");

		final FeedActivity self = this;
		fragment.completeCallback = new FromWhoDialogFragment.FromWhoSelected() {
			@Override
			public void onSelected(String name) {
				person = data.getOrCreatePerson(name, ColorPalette.getInstance(FeedActivity.this));

				if(fullSync) {

                    new MaterialDialog.Builder(self)
                            .content(String.format(self.getString(R.string.overwrite_nfc_text), person.getName()))
                            .positiveText(R.string.overwrite_nfc_title)
                            .negativeText(R.string.cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    data.sync(person, debts);
                                    commitBeam();
                                    dialog.dismiss();
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                }
                            })
                            .show();

				} else {
					data.add(debts[0].extract(person));
					commitBeam();
				}
			}
		};
	}
	private void commitBeam() {
        storage.commit();

		feed = data.feed(person);

		NavigationDrawerFragment.adapter.setItems(data.peopleOrdered());
		navigationDrawerFragment.setSelectedPerson(person);

		getFragmentManager().beginTransaction()
				.replace(R.id.container, new FeedFragment(), "feed_fragment_tag")
				.commit();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean handled = bp.handleActivityResult(requestCode, resultCode, data);

        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }

        super.onDestroy();
    }

	@Override
    public void onProductPurchased(String s, TransactionDetails transactionDetails) {
        Resource.checkFull(bp);

		navigationDrawerFragment.footerUpgrade.setVisibility(View.GONE);

        if (!Resource.isFull) {
            return;
        }

        new MaterialDialog.Builder(this)
                .title(R.string.cloud_sync)
                .content(R.string.cloud_sync_description_first)
                .positiveText(R.string.activate)
                .negativeText(R.string.not_now)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
						StorageManager.migrateToDrive(FeedActivity.this);
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
    public void onPurchaseHistoryRestored() {
    }

    @Override
    public void onBillingError(int i, Throwable throwable) {
    }

    @Override
    public void onBillingInitialized() {
    }

    @Override
    public void onCurrencySelected(UserCurrency userCurrency) {
        feedFragment.adapter.notifyDataSetChanged();
        navigationDrawerFragment.updateBalance(data);

        feedFragment.displayTotalDebt(getResources(), userCurrency);
    }
}