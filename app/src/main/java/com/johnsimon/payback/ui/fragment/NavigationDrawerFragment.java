package com.johnsimon.payback.ui.fragment;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoButton;
import com.johnsimon.payback.R;
import com.johnsimon.payback.adapter.NavigationDrawerAdapter;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.core.DataFragment;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.core.NavigationDrawerItem;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.ui.FeedActivity;
import com.johnsimon.payback.util.Resource;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends DataFragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    public ActionBarDrawerToggle mDrawerToggle;

	public NavigationDrawerAdapter adapter;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;
	public RobotoButton footerUpgrade;

    private ImageButton headerArrow;
    private LinearLayout headerTextContainer;
    private TextView headerName;
    private TextView headerPlus;
    private TextView headerMinus;

    public static int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
	private boolean inHeaderDetailScreen = false;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mDrawerListView = (ListView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
		mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		        selectItem(position - mDrawerListView.getHeaderViewsCount());
	        }
        });

        View footerView = inflater.inflate(R.layout.navigation_drawer_list_footer, null);

        footerUpgrade = (RobotoButton) footerView.findViewById(R.id.navigation_drawer_footer_upgrade);

        if (Resource.isFull) {
            footerUpgrade.setVisibility(View.GONE);
        }

		mDrawerListView.addFooterView(footerView);

		View headerView = inflater.inflate(R.layout.navigation_drawer_list_header, null);

		headerTextContainer = (LinearLayout) headerView.findViewById(R.id.navigation_drawer_header_text_container);
		headerName = (TextView) headerView.findViewById(R.id.navigation_drawer_header_name);
		headerPlus = (TextView) headerView.findViewById(R.id.navigation_drawer_header_plus);
		headerMinus = (TextView) headerView.findViewById(R.id.navigation_drawer_header_minus);
		headerArrow = (ImageButton) headerView.findViewById(R.id.navigation_drawer_header_arrow);

		Button navigation_drawer_header_button = (Button) headerView.findViewById(R.id.navigation_drawer_header_button);

		navigation_drawer_header_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleHeaderVisibility();
            }
        });

		headerTextContainer.setTranslationY(Resource.getPx(58, getActivity().getResources()));
        headerPlus.setAlpha(0f);
        headerMinus.setAlpha(0f);

		mDrawerListView.addHeaderView(headerView);

        adapter = new NavigationDrawerAdapter((DataActivity) getActivity());
        mDrawerListView.setAdapter(adapter);

        super.onCreateView(inflater, container, savedInstanceState);

        return mDrawerListView;
    }

    @Override
    protected void onDataReceived() {
        adapter.setItems(data.peopleOrdered());
        adapter.notifyDataSetChanged();
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        setSelectedPerson(FeedActivity.person);
        selectItem(mCurrentSelectedPosition);

        updateBalance();
    }

    @Override
    protected void onUserLoaded() {
        updateName();
    }

    /*
    Sets correct navigation drawer height using documentation found here:

        http://www.google.com/design/spec/patterns/navigation-drawer.html

        " The width of the side nav is equal to the
          width of the screen minus the height of the
          action bar, or in this case 56dp from the
          right edge of the screen. The maximum width
          of the nav drawer is 5 times the standard
          increment (56dp on mobile and 64dp on tablet). "
     */
    private void setAppropriateNavDrawerWidth(View view) {
		int screenWidth = Resource.getScreenWidth(getActivity());
		int toolbarHeight;

		TypedValue tv = new TypedValue();
		if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			toolbarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
		} else {
			return;
		}

		DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) view.getLayoutParams();
		params.width = Math.min(screenWidth - toolbarHeight, toolbarHeight * 6);

		view.setLayoutParams(params);
		view.requestLayout();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void toggleHeaderVisibility() {

        float scale = 1.1f;
        float transX = (float) ((headerTextContainer.getWidth() * 1.1) - headerTextContainer.getWidth()) / 4;

        if (inHeaderDetailScreen) {
            //Spin to down arrow

			headerArrow.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			headerTextContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			headerPlus.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			headerMinus.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            headerName.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            headerArrow.setRotation(180f);
            headerTextContainer.setTranslationY(0);
            headerTextContainer.setTranslationX(transX);
            headerTextContainer.setScaleX(scale);
            headerTextContainer.setScaleY(scale);
            headerPlus.setAlpha(1f);
            headerMinus.setAlpha(1f);
            headerName.setTranslationY(Resource.getPx(6, getResources()));

            ObjectAnimator rotation = ObjectAnimator.ofFloat(headerArrow,"rotation", 360f);
            ObjectAnimator animY = ObjectAnimator.ofFloat(headerTextContainer, "translationY", Resource.getPx(58, getActivity().getResources()));
            ObjectAnimator animX = ObjectAnimator.ofFloat(headerTextContainer, "translationX", 0f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(headerTextContainer, "scaleX", 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(headerTextContainer, "scaleY", 1f);
            ObjectAnimator alphaP = ObjectAnimator.ofFloat(headerPlus, "alpha", 0f);
            ObjectAnimator alphaM = ObjectAnimator.ofFloat(headerMinus, "alpha", 0f);
            ObjectAnimator transY = ObjectAnimator.ofFloat(headerName, "translationY", 0f);

            rotation.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            animY.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            animX.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            scaleX.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            scaleY.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            alphaP.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            alphaM.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            transY.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));

            if (Resource.isLOrAbove()) {
                PathInterpolator pathInterpolator = new PathInterpolator(0.1f, 0.4f, 0.5f, 1f);
                rotation.setInterpolator(pathInterpolator);
                animY.setInterpolator(pathInterpolator);
                animX.setInterpolator(pathInterpolator);
                scaleX.setInterpolator(pathInterpolator);
                scaleY.setInterpolator(pathInterpolator);
                alphaP.setInterpolator(pathInterpolator);
                alphaM.setInterpolator(pathInterpolator);
                transY.setInterpolator(pathInterpolator);
            } else {
                DecelerateInterpolator pathInterpolator = new DecelerateInterpolator();
                rotation.setInterpolator(pathInterpolator);
                animY.setInterpolator(pathInterpolator);
                animX.setInterpolator(pathInterpolator);
                scaleX.setInterpolator(pathInterpolator);
                scaleY.setInterpolator(pathInterpolator);
                alphaP.setInterpolator(pathInterpolator);
                alphaM.setInterpolator(pathInterpolator);
                transY.setInterpolator(pathInterpolator);
            }

            rotation.start();
            animY.start();
            animX.start();
            scaleX.start();
            scaleY.start();
            alphaP.start();
            alphaM.start();
            transY.start();

			alphaM.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					headerArrow.setLayerType(View.LAYER_TYPE_NONE, null);
					headerTextContainer.setLayerType(View.LAYER_TYPE_NONE, null);
					headerPlus.setLayerType(View.LAYER_TYPE_NONE, null);
					headerMinus.setLayerType(View.LAYER_TYPE_NONE, null);
                    headerName.setLayerType(View.LAYER_TYPE_NONE, null);
				}

				@Override
				public void onAnimationCancel(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}
			});

            inHeaderDetailScreen = false;
        } else {
			headerArrow.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			headerTextContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			headerPlus.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			headerMinus.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            headerName.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            headerArrow.setRotation(0f);
            headerTextContainer.setTranslationY(Resource.getPx(58, getActivity().getResources()));
            headerTextContainer.setTranslationX(0f);
            headerTextContainer.setScaleX(1f);
            headerTextContainer.setScaleY(1f);
            headerPlus.setAlpha(0f);
            headerMinus.setAlpha(0f);
            headerName.setTranslationY(0f);

            ObjectAnimator rotation = ObjectAnimator.ofFloat(headerArrow, "rotation", 180f);
            ObjectAnimator animY = ObjectAnimator.ofFloat(headerTextContainer, "translationY", 0);
            ObjectAnimator animX = ObjectAnimator.ofFloat(headerTextContainer, "translationX", transX);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(headerTextContainer, "scaleX", scale);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(headerTextContainer, "scaleY", scale);
            ObjectAnimator alphaP = ObjectAnimator.ofFloat(headerPlus, "alpha", 1f);
            ObjectAnimator alphaM = ObjectAnimator.ofFloat(headerMinus, "alpha", 1f);
            ObjectAnimator transY = ObjectAnimator.ofFloat(headerName, "translationY", Resource.getPx(6, getResources()));

            rotation.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            animY.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            animX.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            scaleX.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            scaleY.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            alphaP.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            alphaM.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            transY.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));

            if (Resource.isLOrAbove()) {
                PathInterpolator pathInterpolator = new PathInterpolator(0.1f, 0.4f, 0.5f, 1f);
                rotation.setInterpolator(pathInterpolator);
                animY.setInterpolator(pathInterpolator);
                animX.setInterpolator(pathInterpolator);
                scaleX.setInterpolator(pathInterpolator);
                scaleY.setInterpolator(pathInterpolator);
                alphaP.setInterpolator(pathInterpolator);
                alphaM.setInterpolator(pathInterpolator);
                transY.setInterpolator(pathInterpolator);
            } else {
                DecelerateInterpolator pathInterpolator = new DecelerateInterpolator();
                rotation.setInterpolator(pathInterpolator);
                animY.setInterpolator(pathInterpolator);
                animX.setInterpolator(pathInterpolator);
                scaleX.setInterpolator(pathInterpolator);
                scaleY.setInterpolator(pathInterpolator);
                alphaP.setInterpolator(pathInterpolator);
                alphaM.setInterpolator(pathInterpolator);
                transY.setInterpolator(pathInterpolator);
            }

            rotation.start();
            animY.start();
            animX.start();
            scaleX.start();
            scaleY.start();
            alphaP.start();
            alphaM.start();
            transY.start();

			alphaM.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					headerArrow.setLayerType(View.LAYER_TYPE_NONE, null);
					headerTextContainer.setLayerType(View.LAYER_TYPE_NONE, null);
					headerPlus.setLayerType(View.LAYER_TYPE_NONE, null);
					headerMinus.setLayerType(View.LAYER_TYPE_NONE, null);
                    headerName.setLayerType(View.LAYER_TYPE_NONE, null);
				}

				@Override
				public void onAnimationCancel(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}
			});

            inHeaderDetailScreen = true;
        }
    }

	public void updateBalance() {
		headerPlus.setText("+ " + data.preferences.getCurrency().render(data.totalPlus()));
		headerMinus.setText("- " + data.preferences.getCurrency().render(data.totalMinus()));
	}

	private void updateName() {
        headerName.setText(user.getName(getResources()));
	}

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

		setAppropriateNavDrawerWidth(mFragmentContainerView);

        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
            }
        };
        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

	public void setSelectedPerson(Person p) {
		mCurrentSelectedPosition = adapter.selectPerson(p);
		adapter.notifyDataSetChanged();
	}

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;

        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(adapter.getItem(position));
        }

		adapter.notifyDataSetChanged();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        mCallbacks.onShowGlobalContextActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(NavigationDrawerItem item);

        void onShowGlobalContextActionBar();
    }
}
