package com.ncorti.slidetoact.example;

import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ncorti.slidetoact.SlideToActView;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class MainSlideActivityTest {

    @Rule
    public ActivityTestRule<MainSlideActivity> mActivityRule = new ActivityTestRule<>(MainSlideActivity.class);

    @Test
    public void testSlideToActViews_checkAreNotCompleted() {
        Assert.assertFalse(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide1)).isCompleted());
        Assert.assertFalse(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide2)).isCompleted());
        Assert.assertFalse(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide3)).isCompleted());
        Assert.assertFalse(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide4)).isCompleted());
        onView(withId(R.id.slide1)).perform(ViewActions.swipeRight());
        onView(withId(R.id.slide2)).perform(ViewActions.swipeRight());
        onView(withId(R.id.slide3)).perform(ViewActions.swipeRight());
        onView(withId(R.id.slide4)).perform(ViewActions.swipeRight());
    }


    @Test
    public void testSlideToActViews_checkCompleted() throws InterruptedException {
        onView(withId(R.id.slide1)).perform(ViewActions.swipeRight());
        onView(withId(R.id.slide2)).perform(ViewActions.swipeRight());
        onView(withId(R.id.slide3)).perform(ViewActions.swipeRight());
        onView(withId(R.id.slide4)).perform(ViewActions.swipeRight());
        Thread.sleep(2000);
        Assert.assertFalse(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide1)).isCompleted());
        Assert.assertTrue(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide2)).isCompleted());
        Assert.assertTrue(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide3)).isCompleted());
        Assert.assertTrue(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide4)).isCompleted());
    }

}