package com.johnsimon.payback;

import android.support.test.runner.AndroidJUnit4;

import com.johnsimon.payback.ui.FeedActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class FeedActivityTest {

    @Rule public final ActivityRule<FeedActivity> main = new ActivityRule<>(FeedActivity.class);

    @Test
    public void clearData() {
        DrawerActions.openDrawer(R.id.drawer_layout);

        onView(withId(R.id.navigation_drawer_footer_settings)).perform(click());
        onView(withText(R.string.wipe_data)).perform(click());
        onView(withText(R.string.wipe)).perform(click());

        pressBack();

        onView(withId(R.id.feed_list_empty_view)).check(matches(isDisplayed()));
        onView(withText(R.string.debt_free)).check(matches(isDisplayed()));
    }

    @Test
    public void addPersonFromPeopleManager() {

        DrawerActions.openDrawer(R.id.drawer_layout);
        onView(withId(R.id.navigation_drawer_footer_people)).perform(click());
        onView(withId(R.id.people_manager_fab_container)).perform(click());

        onView(withText(R.string.add_person)).check(matches(isDisplayed()));
        onView(withHint(R.string.name)).perform(typeTextIntoFocusedView("First Sur"));
        onView(withText(R.string.ok)).check(matches(isEnabled()));
        onView(withText(R.string.ok)).perform(click());

        /*
        CURRENTLY BROKEN
        onView(withId(R.id.people_recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, click()));
        */
        onView(withText("First Sure")).perform(click());

        onView(withId(R.id.person_detail_title)).check(matches(isDisplayed()));

        pressBack();
        pressBack();
    }
}
