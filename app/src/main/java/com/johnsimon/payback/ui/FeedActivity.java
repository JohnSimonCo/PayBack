package com.johnsimon.payback.ui;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.johnsimon.payback.BuildConfig;
import com.johnsimon.payback.R;
import com.johnsimon.payback.async.Callback;
import com.johnsimon.payback.async.Notification;
import com.johnsimon.payback.async.NullCallback;
import com.johnsimon.payback.async.NullPromise;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.core.NavigationDrawerItem;
import com.johnsimon.payback.data.DebtState;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.async.Subscription;
import com.johnsimon.payback.data.User;
import com.johnsimon.payback.send.DebtSendable;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.ui.dialog.AboutDialogFragment;
import com.johnsimon.payback.ui.dialog.CurrencyDialogFragment;
import com.johnsimon.payback.ui.dialog.FromWhoDialogFragment;
import com.johnsimon.payback.ui.dialog.PaidBackDialogFragment;
import com.johnsimon.payback.ui.dialog.InitialRestoreBackupDialog;
import com.johnsimon.payback.ui.dialog.PayPalRecipientPickerDialogFragment;
import com.johnsimon.payback.ui.dialog.WelcomeDialogFragment;
import com.johnsimon.payback.ui.fragment.FeedFragment;
import com.johnsimon.payback.ui.fragment.NavigationDrawerFragment;
import com.johnsimon.payback.util.Alarm;
import com.johnsimon.payback.util.Beamer;
import com.johnsimon.payback.util.ColorPalette;
import com.johnsimon.payback.util.PayPalManager;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.ShareStringGenerator;
import com.johnsimon.payback.util.SwishLauncher;
import com.johnsimon.payback.util.Undo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

public class FeedActivity extends DataActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks, Beamer.BeamListener,
        BillingProcessor.IBillingHandler, CurrencyDialogFragment.CurrencySelectedCallback,
		FeedFragment.OnFeedChangeCallback, PayPalRecipientPickerDialogFragment.RecipientSelected {

    public BillingProcessor bp;

	private static String ARG_PREFIX = Resource.prefix("FEED");
	public static String ARG_FROM_CREATE = Resource.arg(ARG_PREFIX, "FROM_CREATE");

	public Toolbar toolbar;
	public DrawerLayout masterLayout;
	public static Person person = null;
	public static ArrayList<Debt> feed;

	public Subscription<ArrayList<Debt>> feedSubscription = new Subscription<>();
	public Notification feedLinkedNotification = new Notification();

	private MenuItem filterAmount;
    private MenuItem menu_even_out;

	private NavigationDrawerFragment navigationDrawerFragment;

	private Beamer beamer;

	private FeedFragment feedFragment;

    private boolean attemptCheckFilterAmount = false;

	private NullPromise bpInitialized = new NullPromise();

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

		bp.loadOwnedPurchasesFromGoogle();

		Resource.checkFull(bp);

		if (Resource.isLOrAbove()) {
			setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_launcher), getResources().getColor(R.color.primary_color)));
		}

		Resource.init(getApplicationContext());

		setContentView(R.layout.activity_feed);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);

		// Set up the drawer.
		navigationDrawerFragment.setUp(
				R.id.navigation_drawer,
				masterLayout = (DrawerLayout) findViewById(R.id.drawer_layout)
		);

		if (Resource.isFirstRun(storage.getPreferences())) {

			if (Resource.isFull) {
				InitialRestoreBackupDialog.attemptRestore(this, storage, masterLayout).then(new Callback<Boolean>() {
					@Override
					public void onCalled(Boolean successful) {
						if (!successful) {
							WelcomeDialogFragment welcomeDialogFragment = new WelcomeDialogFragment();
							welcomeDialogFragment.show(getFragmentManager(), "welcome_dialog_fragment");
						}
					}
				});
			} else {
				WelcomeDialogFragment welcomeDialogFragment = new WelcomeDialogFragment();
				welcomeDialogFragment.show(getFragmentManager(), "welcome_dialog_fragment");
			}


		}

		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
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

        feedFragment = new FeedFragment();
        feedFragment.feedChangeCallback = this;

		getFragmentManager().beginTransaction()
				.replace(R.id.container, feedFragment, "feed_fragment_tag")
				.commit();

        if (BuildConfig.DEBUG) {
			Snackbar.make(masterLayout, "Debug build " + BuildConfig.VERSION_NAME, Snackbar.LENGTH_SHORT).show();
        }

		PayPalManager.init(this);
	}

    @Override
    protected void onDataReceived() {
        if (isAll()) {
            feed = data.debts;
        } else {
            feed = data.feed(person);
        }
        sort();
		getSupportActionBar().setSubtitle(isAll() ? getString(R.string.all) : person.getName());

		navigationDrawerFragment.adapter.setItems(data.peopleOrdered());
		navigationDrawerFragment.adapter.notifyDataSetChanged();

		feedSubscription.broadcast(feed);

        //TODO REMOVE
        //LocalStorage.test(this, data);
    }

    @Override
    protected void onDataLinked() {
        navigationDrawerFragment.adapter.notifyDataSetChanged();

		feedLinkedNotification.broadcast();
    }

	public static boolean isAll() {
		return person == null;
	}
	public static void goToAll() {
		person = null;
	}

    @Override
	public void onResume() {
		super.onResume();

		// Check to see that the Activity started due to an Android Beam
		Intent intent = getIntent();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			beamer.processIntent(intent);
		} else if (intent.getExtras() != null && intent.getExtras().get(Alarm.ALARM_ID) != null) {
			UUID debtId = (UUID) intent.getExtras().get(Alarm.ALARM_ID);
			Debt debt = data.findDebt(debtId);

			Alarm.cancelNotification(getApplicationContext(), debtId);

			if(debt == null) {
				return;
			}

            person = debt.getOwner();
            feed = data.feed(person);
            feedSubscription.broadcast(feed);
            onFeedChange();
			sort();

            feedFragment.showDetail(debt);

        }
    }

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
	public void onNavigationDrawerItemSelected(NavigationDrawerItem item) {
		Undo.completeAction();

		if(item.type == NavigationDrawerItem.Type.All) {
			changePerson(null);
		} else if(item.type == NavigationDrawerItem.Type.Person) {
			changePerson(item.owner);
		}

		storage.requestRefresh();
	}

	public void changePerson(Person newPerson) {
		if (newPerson == person) {
			return;
		}

		person = newPerson;

		feed = data.feed(person);
		feedFragment.adapter.updateList(feed);
		sort();

		feedFragment.adapter.animate = true;

		feedSubscription.broadcast(feed);
		feedLinkedNotification.broadcast();

		handler.postDelayed(new Runnable() {
			public void run() {
				feedFragment.adapter.animate = false;
			}
		}, 200);

		getSupportActionBar().setSubtitle(isAll() ? getString(R.string.all) : person.getName());

		invalidateOptionsMenu();

		feedFragment.adapter.notifyDataSetChanged();

		feedFragment.recyclerView.getLayoutManager().scrollToPosition(0);
		feedFragment.scrollListener.mHeader.setTranslationY(0);
		feedFragment.scrollListener.mHeaderDiffTotal = 0;

		feedFragment.displayTotalDebt(getResources());

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
		MenuItem fullMenuPaySwish = menu.findItem(R.id.feed_menu_pay_back_swish);
		MenuItem fullMenuPayPayPal = menu.findItem(R.id.feed_menu_pay_back_paypal);
		MenuItem menuShare = menu.findItem(R.id.feed_menu_share);

        if (attemptCheckFilterAmount) {
            filterAmount.setChecked(true);
        }

        menu_even_out = menu.findItem(R.id.menu_even_out);

        if (isAll() || AppData.isEven(feed)) {
            menu_even_out.setVisible(false);
        }

        sort();

		if (isAll()) {
			menuShare.setVisible(false);
		} else {
			menuShare.setVisible(feed.size() != 0);
		}

		if (isAll()) {
			fullMenuPaySwish.setVisible(false);
			fullMenuPayPayPal.setVisible(false);
		} else {
			fullMenuPayPayPal.setVisible(true);
			if(AppData.total(feed) < 0) {
				fullMenuPaySwish.setVisible(SwishLauncher.hasService(getPackageManager()));
				fullMenuPayPayPal.setEnabled(true);
			} else {
				fullMenuPaySwish.setVisible(false);
				fullMenuPayPayPal.setEnabled(false);
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

			case R.id.feed_menu_pay_back_swish:
				SwishLauncher.startSwish(this, AppData.total(feed), person);
				break;

			case R.id.feed_menu_pay_back_paypal:
				startPayPal(person.link, Math.abs(AppData.total(feed))).then(new Callback<Boolean>() {
					@Override
					public void onCalled(Boolean success) {
						if (success) {
							onEvenOut();
						}
					}
				});
				break;

            case R.id.menu_even_out:
                onEvenOut();
                break;

			case R.id.feed_menu_share:
				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				shareIntent.putExtra(Intent.EXTRA_TEXT, ShareStringGenerator.generateDebtSummary(
						getApplicationContext(), feed, data.preferences.getCurrency()));
				shareIntent.setType("text/plain");
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
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

                navigationDrawerFragment.closeDrawer();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(FeedActivity.this, PeopleManagerActivity.class));
                    }
                }, 200);
				break;
			case R.id.navigation_drawer_footer_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				break;
			case R.id.navigation_drawer_footer_about:
				AboutDialogFragment aboutDialogFragment = new AboutDialogFragment();
				aboutDialogFragment.show(getFragmentManager(), "about_dialog");
				break;
            case R.id.navigation_drawer_footer_upgrade:

                new MaterialDialog.Builder(this)
                        .title(getString(R.string.upgrade_title))
                        .content(getString(R.string.upgrade_text))
                        .positiveText(R.string.upgrade_confirm_text)
                        .negativeText(R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
								purchaseFullVersion();
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

	public void purchaseFullVersion() {
		bpInitialized.thenUnique(billingInitializedCallback);

	}

	private NullCallback billingInitializedCallback = new NullCallback() {
		@Override
		public void onCalled() {
			bp.purchase(FeedActivity.this, "full_version");
		}
	};

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        if (filterAmount != null) {
            outState.putBoolean("AMOUNT_USED_SORT", filterAmount.isChecked());
        }
	}

	@Override
	public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
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

            new MaterialDialog.Builder(this)
                    .title(getString(R.string.upgrade_title))
                    .content(getString(R.string.upgrade_text))
                    .positiveText(R.string.upgrade_confirm_text)
                    .negativeText(R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
							purchaseFullVersion();
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

                    new MaterialDialog.Builder(FeedActivity.this)
                            .content(String.format(self.getString(R.string.overwrite_nfc_text), person.getName()))
                            .positiveText(R.string.overwrite_nfc_title)
                            .negativeText(R.string.cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    data.sync(FeedActivity.this, person, debts);
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
        storage.commit(this);

		feed = data.feed(person);

		sort();

		navigationDrawerFragment.adapter.setItems(data.peopleOrdered());
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
        Resource.purchasedFull(this, bp, masterLayout);
    }

    @Override
    public void onPurchaseHistoryRestored() {
		Resource.checkFull(bp);
    }

    @Override
    public void onBillingError(int error, Throwable throwable) {
	}

	@Override
	public void onBillingInitialized() {
		bpInitialized.fire();
		Resource.checkFull(bp);
    }

    @Override
    public void onCurrencySelected() {
        feedFragment.adapter.notifyDataSetChanged();
        navigationDrawerFragment.updateBalance();

		feedFragment.displayTotalDebt(getResources());
    }

	public void onEvenOut() {

        PaidBackDialogFragment paidBackDialogFragment;

        paidBackDialogFragment = PaidBackDialogFragment.newInstance(PaidBackDialogFragment.PAY_BACK, true);
        paidBackDialogFragment.show(getFragmentManager().beginTransaction(), "evened_out_dialog");
        paidBackDialogFragment.completeCallback = new PaidBackDialogFragment.CompleteCallback() {
            @Override
            public void onComplete() {
				final ArrayList<DebtState> oldState = new ArrayList<>();

				for(Debt debt : feed) {
					oldState.add(new DebtState(debt));
				}

                Undo.executeAction(FeedActivity.this, R.string.evened_out, masterLayout, new Undo.UndoableAction() {
                    @Override
                    public void onDisplay() {
                        for(Debt debt: feed) {
							if(!debt.isPaidBack()) {
								debt.payback();
							}

							if (debt.getRemindDate() != null) {
								Alarm.cancelAlarm(FeedActivity.this, debt);
								debt.setRemindDate(null);
							}
						}
						feedFragment.adapter.notifyDataSetChanged();
						feedFragment.feedChangeCallback.onFeedChange();
					}

					@Override
					public void onRevert() {
						for (int i = 0; i < feed.size(); i++) {
							Debt debt = feed.get(i);
							oldState.get(i).restore(debt);

							if (debt.getRemindDate() != null) {
								Alarm.addAlarm(FeedActivity.this, debt);
							}
						}
						feedFragment.adapter.notifyDataSetChanged();
						feedFragment.feedChangeCallback.onFeedChange();
					}

					@Override
					public void onCommit() {
						storage.commit(getApplicationContext());
					}
				});
            }
        };
	}

    @Override
    public void onFeedChange() {
        navigationDrawerFragment.updateBalance();
        if (menu_even_out != null) {
            if (AppData.isEven(feed)) {
                menu_even_out.setVisible(false);
            } else {
                menu_even_out.setVisible(true);
            }
        }

        feedFragment.displayTotalDebt(getResources());
    }

	public Promise<Boolean> startPayPal(Contact contact, double amount) {
		PayPalRecipientPickerDialogFragment p = new PayPalRecipientPickerDialogFragment();

		Bundle args = new Bundle();

		ArrayList<String> suggestions = new ArrayList<>();
		if(contact != null) {
			if(contact.hasNumbers()) {
				suggestions.addAll(Arrays.asList(contact.numbers));
			}
			if(contact.hasEmails()) {
				suggestions.addAll(Arrays.asList(contact.emails));
			}
		}

		args.putStringArray(PayPalRecipientPickerDialogFragment.KEY_SUGGESTIONS, suggestions.toArray(new String[suggestions.size()]));
		args.putDouble(PayPalRecipientPickerDialogFragment.KEY_AMOUNT, amount);

		p.setArguments(args);
		p.show(getFragmentManager(), "pp");

		payPalPromise = new Promise<>();
		return payPalPromise;
	}

	private static Promise<Boolean> payPalPromise;

	@Override
	public void onRecipientSelected(String recipient, double amount) {
		String currency = data.preferences.getCurrency().id;
		PayPalManager.requestPayment(FeedActivity.this, recipient, new BigDecimal(amount), currency).then(new Callback<Boolean>() {
			@Override
			public void onCalled(Boolean data) {
				payPalPromise.fire(data);
			}
		});
	}
}