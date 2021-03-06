package com.johnsimon.payback.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.adapter.PeopleListAdapter;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.DataLinker;
import com.johnsimon.payback.data.PeopleOrder;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.ui.base.BaseActivity;
import com.johnsimon.payback.ui.dialog.PeopleDetailDialogFragment;
import com.johnsimon.payback.ui.dialog.PersonPickerDialogFragment;
import com.johnsimon.payback.util.ColorPalette;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.Undo;
import com.johnsimon.payback.view.DragSortRecycler;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;

public class PeopleManagerActivity extends DataActivity implements DragSortRecycler.OnItemMovedListener, PeopleDetailDialogFragment.PeopleDetailCallbacks {

	private PeopleListAdapter adapter;
    private RecyclerView recyclerView;

    private int sortAzX;
    private int sortAzY;

    private View masterLayout;

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

        masterLayout = findViewById(R.id.people_manager_master);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

		recyclerView = (RecyclerView) findViewById(R.id.people_recycler_view);

		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setItemAnimator(null);

		DragSortRecycler dragSortRecycler = new DragSortRecycler();
		dragSortRecycler.setViewHandleId(R.id.people_list_item_handle);
		dragSortRecycler.setFloatingAlpha(0.4f);
		dragSortRecycler.setFloatingBgColor(getResources().getColor(android.R.color.transparent));
		dragSortRecycler.setAutoScrollSpeed(0.3f);
		dragSortRecycler.setAutoScrollWindow(0.1f);

		dragSortRecycler.setOnItemMovedListener(this);

		recyclerView.addItemDecoration(dragSortRecycler);
		recyclerView.addOnItemTouchListener(dragSortRecycler);
		recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());

		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {

			int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.people_fab);

            if (Resource.isLOrAbove()) {
				FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fab.getLayoutParams();
				params.setMargins(0, actionBarHeight + Resource.getPx(48, getResources()) - Math.round(getResources().getDimension(R.dimen.fab_size) / 2), Math.round(getResources().getDimension(R.dimen.fab_right_margin)), 0);

				fab.setLayoutParams(params);
			} else {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fab.getLayoutParams();
                params.setMargins(0, actionBarHeight + Resource.getPx(28, getResources()) - Resource.getPx(28, getResources()),
                        (int) (getResources().getDimension(R.dimen.fab_right_margin)) - (int) (getResources().getDimension(R.dimen.people_offset)), 0);

                fab.setLayoutParams(params);
            }
            fab.setOnClickListener(fabClickListener);
        }

		setupTreeObserver();
    }

	@Override
	protected void onDataReceived() {
		adapter = new PeopleListAdapter(this, findViewById(R.id.people_manager_empty), data, (TextView) findViewById(R.id.people_manager_title), data.peopleOrdered());
		recyclerView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		adapter.updateEmptyViewVisibility();

		adapter.clickListener = new PeopleListAdapter.PeopleListClickListener() {
			@Override
			public void onListItemClick(int position) {
				Person person = adapter.getItem(position);
				PeopleDetailDialogFragment peopleDetailDialogFragment = PeopleDetailDialogFragment.newInstance(person);
                peopleDetailDialogFragment.callbacks = PeopleManagerActivity.this;
				peopleDetailDialogFragment.show(getFragmentManager(), "people_detail_dialog");
			}
		};
	}

	@Override
	protected void onDataLinked() {
		adapter.notifyDataSetChanged();
	}

	private View.OnClickListener fabClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			PersonPickerDialogFragment fragment = new PersonPickerDialogFragment();
			Bundle args = new Bundle();
			args.putString(PersonPickerDialogFragment.TITLE_KEY, getString(R.string.add_person));
			args.putBoolean(PersonPickerDialogFragment.NO_EXISTING_PEOPLE_FLAG, true);
			fragment.setArguments(args);

			fragment.completeCallback = new PersonPickerDialogFragment.PersonSelectedCallback() {
				@Override
				public void onSelected(String name) {
					Person person = new Person(name, ColorPalette.getInstance(PeopleManagerActivity.this));
                    data.add(person);
                    DataLinker.link(person, data.contacts);
                    adapter.people.add(person);
					storage.commit(getApplicationContext());
					adapter.notifyDataSetChanged();
                    adapter.updateEmptyViewVisibility();
				}
			};

			fragment.show(getFragmentManager(), "person_picker");
		}
	};

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

        final PeopleManagerActivity self = this;

        switch (item.getItemId()) {

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

			case R.id.action_sort_az:

				final ArrayList<Person> list = adapter.people;

				final PeopleOrder.SortResult result = data.peopleOrder.sortAlphabetically(data.people);

				if (Resource.isLOrAbove() && !(sortAzX == 0 && sortAzY == 0)) {

                    final PathInterpolator pathInterpolator = new PathInterpolator(0.16f, 0.83f, 0.55f, 0.90f);

					final int initialRadius = recyclerView.getWidth();

					Animator anim = ViewAnimationUtils.createCircularReveal(recyclerView, sortAzX, sortAzY, initialRadius, 0);
                    anim.setInterpolator(pathInterpolator);

					anim.addListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							super.onAnimationEnd(animation);

							sort(self, result, list);

							adapter.notifyDataSetChanged();
                            adapter.updateEmptyViewVisibility();

                            Animator anim = ViewAnimationUtils.createCircularReveal(recyclerView, sortAzX, sortAzY, 0, initialRadius);
                            anim.setInterpolator(new PathInterpolator(0.72f, 0.16f, 0.85f, 0.69f));

                            anim.setDuration(400);
                            anim.start();

						}
					});

					anim.setDuration(400);
					anim.start();
                } else {
					sort(this, result, list);
				}

				break;

		}
		return super.onOptionsItemSelected(item);
	}

	private void sort(BaseActivity self, final PeopleOrder.SortResult result, final ArrayList<Person> list) {
		Undo.executeAction(self, R.string.sort_list, masterLayout, new Undo.UndoableAction() {
			@Override
			public void onDisplay() {
                adapter.people = result.people;
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onRevert() {
                adapter.people = list;
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onCommit() {
				data.peopleOrder = result.order;
				data.touchPeopleOrder();
				storage.commit(getApplicationContext());
			}
		});
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

    @Override
    public void onItemMoved(int from, int to) {
        if (from != to) {
			Undo.completeAction();

            Person item = adapter.getItem(from);
            adapter.remove(from);

            boolean toLast;

            if (to == adapter.people.size()) {
                adapter.people.add(item);
                toLast = true;
            } else {
                adapter.insert(item, to);
                toLast = false;
            }
            adapter.notifyDataSetChanged();

            data.peopleOrder.reorder(from, to, toLast);
			data.touchPeopleOrder();
            storage.commit(getApplicationContext());
        }
    }

    @Override
    public void onDelete(final Person person) {
        final int listIndex = adapter.people.indexOf(person);

        Undo.executeAction(PeopleManagerActivity.this, R.string.deleted_person, masterLayout, new Undo.UndoableAction() {
            @Override
            public void onDisplay() {
                adapter.people.remove(listIndex);
                adapter.notifyDataSetChanged();
                adapter.updateEmptyViewVisibility();
            }

            @Override
            public void onRevert() {
                adapter.people.add(listIndex, person);
                adapter.notifyDataSetChanged();
                adapter.updateEmptyViewVisibility();
            }

            @Override
            public void onCommit() {
                data.delete(PeopleManagerActivity.this, person);
                storage.commit(getApplicationContext());
            }
        });
    }

    @Override
    public void onRename(final Person person, final String name) {
        final String oldName = person.getName();

        Undo.executeAction(PeopleManagerActivity.this, R.string.renamed_person, masterLayout, new Undo.UndoableAction() {
            @Override
            public void onDisplay() {
                person.setName(name);
                DataLinker.link(person, data.contacts);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onRevert() {
                person.setName(oldName);
                DataLinker.link(person, data.contacts);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCommit() {
                storage.commit(getApplicationContext());
            }
        });
    }

    @Override
    public void onMerge(Person person, String name) {
        Person other = data.findPersonByName(name);
        int index = adapter.people.indexOf(person);

        adapter.people.remove(index);
        adapter.notifyDataSetChanged();
        adapter.updateEmptyViewVisibility();

        data.merge(this, person, other);
        storage.commit(getApplicationContext());
    }
}