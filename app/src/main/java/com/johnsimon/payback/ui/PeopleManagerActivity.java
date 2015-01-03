package com.johnsimon.payback.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.adapter.PeopleListAdapter;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.Resource;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.williammora.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;

public class PeopleManagerActivity extends DataActivity {

	private static String ARG_PREFIX = Resource.prefix("CREATE_DEBT");

	private PeopleListAdapter adapter;
    private DragSortListView listView;

    private int sortAzX;
    private int sortAzY;

    private ArrayList<Person> personListBeforeSort;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Resource.isLOrAbove()) {
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_launcher), getResources().getColor(R.color.primary_color)));

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.primary_color));
        } else {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.primary_color_darker));
        }

        setContentView(R.layout.activity_people_manager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		adapter = new PeopleListAdapter(this, data.people, findViewById(R.id.people_manager_empty));

        listView = (DragSortListView) findViewById(R.id.people_listview);

        listView.setAdapter(adapter);
        listView.setDropListener(onDrop);

        DragSortController controller = new DragSortController(listView);
        controller.setDragHandleId(R.id.people_list_item_handle);
        controller.setRemoveEnabled(false);
        controller.setSortEnabled(true);
        controller.setDragInitMode(DragSortController.ON_DRAG);

        listView.setFloatViewManager(controller);
        listView.setOnTouchListener(controller);
        listView.setDragEnabled(true);

        SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(listView);
        simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
        listView.setFloatViewManager(simpleFloatViewManager);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Person person = adapter.getItem(position);
				PeopleDetailDialogFragment peopleDetailDialogFragment = PeopleDetailDialogFragment.newInstance(person);
				peopleDetailDialogFragment.show(getFragmentManager(), "people_detail_dialog");
				peopleDetailDialogFragment.editPersonCallback = new PeopleDetailDialogFragment.EditPersonCallback() {

					@Override
					public void onEdit() {
						adapter.notifyDataSetChanged();
                        storage.commit();
						Resource.actionComplete(getFragmentManager());
					}
				};
			}
		});

		adapter.notifyDataSetChanged();

        final ImageView people_manager_empty_image = (ImageView) findViewById(R.id.people_manager_empty_image);
        people_manager_empty_image.setBackgroundResource(R.anim.hand_wave);
        people_manager_empty_image.post(new Runnable() {
            @Override
            public void run() {
                AnimationDrawable frameAnimation = (AnimationDrawable) people_manager_empty_image.getBackground();
                frameAnimation.start();
            }
        });

        setupTreeObserver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show items in the action bar relevant to this screen
        // if the drawer is not showing. Otherwise, let the drawer
        // decide what to show in the action bar.
        getMenuInflater().inflate(R.menu.people_manager, menu);
        return true;
    }

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				returnToFeed();
				break;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.action_people_plus:
                PersonPickerDialogFragment fragment = new PersonPickerDialogFragment();
                Bundle args = new Bundle();
                args.putString(PersonPickerDialogFragment.TITLE_KEY, getString(R.string.add_person));
                fragment.setArguments(args);
                fragment.completeCallback = new PersonPickerDialogFragment.PersonSelectedCallback() {
                    @Override
                    public void onSelected(String name) {
                        //TODO add person
                    //    Person person = new Person();
                    //    data.people.add()
                    }
                };

                break;

			case R.id.action_sort_az:

                personListBeforeSort = (ArrayList<Person>) data.people.clone();

                Collections.sort(data.people, new Resource.AlphabeticalComparator());
                storage.commit();

                if (Resource.areIdenticalLists(personListBeforeSort, data.people)) {
					Snackbar.with(getApplicationContext())
							.text(getString(R.string.already_sorted))
							.show(this);
                    break;
                }

                if (!Resource.isLOrAbove() || (sortAzX == 0 && sortAzY == 0)) {
                    adapter.notifyDataSetChanged();

                    Snackbar.with(getApplicationContext())
                            .text(getString(R.string.sort_list))
                            .actionLabel(getString(R.string.undo))
                            .actionColor(getResources().getColor(R.color.green))
                            .actionListener(new Snackbar.ActionClickListener() {
                                @Override
                                public void onActionClicked() {
                                    data.people = personListBeforeSort;
                                    storage.commit();

                                    adapter.clear();
                                    adapter.addAll(data.people);
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .show(this);

                    break;
                }

                int initialRadius = listView.getWidth();

                final PeopleManagerActivity self = this;

                Animator anim =
                        ViewAnimationUtils.createCircularReveal(listView, sortAzX, sortAzY, initialRadius, 0);

                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        adapter.notifyDataSetChanged();

                        int finalRadius = Math.max(listView.getWidth(), listView.getHeight());

                        Animator anim =
                                ViewAnimationUtils.createCircularReveal(listView, sortAzX, sortAzY, 0, finalRadius);

                        listView.setVisibility(View.VISIBLE);
                        anim.setDuration(300);

                        anim.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                //Show snackbar for listView resort possibility
                                Snackbar.with(getApplicationContext())
                                        .text(getString(R.string.sort_list))
                                        .actionLabel(getString(R.string.undo))
										.actionColor(getResources().getColor(R.color.green))
                                        .actionListener(new Snackbar.ActionClickListener() {
                                            @Override
                                            public void onActionClicked() {
                                                data.people = personListBeforeSort;
                                                storage.commit();

                                                adapter.clear();
                                                adapter.addAll(data.people);
                                                adapter.notifyDataSetChanged();
                                            }
                                        })
                                        .show(self);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        });

                        anim.start();

                    }
                });

                anim.setDuration(300);
                anim.start();

				break;

		}
		return super.onOptionsItemSelected(item);
	}

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			if (from != to) {
				Person item = adapter.getItem(from);
				adapter.remove(item);
				adapter.insert(item, to);
			}
		}
	};

	@Override
	public void onBackPressed() {
		returnToFeed();
	}

    private void setupTreeObserver() {
        final ViewTreeObserver viewTreeObserver = getWindow().getDecorView().getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View menuButton = findViewById(R.id.action_sort_az);
                if (menuButton != null) {
                    int[] location = new int[2];
                    menuButton.getLocationInWindow(location);

                    sortAzX = location[0];
                    sortAzY = location[1];

                    if (viewTreeObserver.isAlive()) {
                        viewTreeObserver.removeGlobalOnLayoutListener(this);
                    }
                }
            }
        });
    }

	public void returnToFeed() {
		Intent intent = new Intent(this, FeedActivity.class);
		if(!data.people.contains(FeedActivity.person)) {
			FeedActivity.person = null;
		}

        storage.commit();

		finishAffinity();
		startActivity(intent);
	}

}