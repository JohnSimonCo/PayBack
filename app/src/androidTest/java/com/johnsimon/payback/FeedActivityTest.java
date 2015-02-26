package com.johnsimon.payback;

import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.runner.AndroidJUnit4;

import com.johnsimon.payback.ui.FeedActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class FeedActivityTest {

    @Rule public final ActivityRule<FeedActivity> main = new ActivityRule<>(FeedActivity.class);

    @Test
    public void test() {
        //onView(withText("Balance:")).check(ViewAssertions.matches(isDisplayed()));
    }

}
