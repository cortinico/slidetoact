package com.ncorti.slidetoact.example;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.ncorti.slidetoact.SlideToActView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SliderCompletedTest {

    @Rule
    public final ActivityTestRule<SampleActivity> mActivityRule =
            new ActivityTestRule<SampleActivity>(SampleActivity.class) {
                @Override
                protected Intent getActivityIntent() {
                    Context targetContext = InstrumentationRegistry.getInstrumentation()
                            .getTargetContext();
                    Intent result = new Intent(targetContext, SampleActivity.class);
                    result.putExtra(SampleActivity.EXTRA_PRESSED_BUTTON, R.id.button_completed);
                    return result;
                }
            };

    @Before
    public void setUp() {
        // Force wake up of device for Circle CI test execution.
        final SampleActivity activity = mActivityRule.getActivity();
        Runnable wakeUpDevice = new Runnable() {
            public void run() {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        };
        activity.runOnUiThread(wakeUpDevice);
    }

    @Test
    public void testCompletedSlideToActView() {
        assertTrue(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).isCompleted());
        assertFalse(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).isEnabled());
    }

    @Test
    public void testCompletedSlideToActView_withReset_returnToBaseState() throws InterruptedException {
        onView(withId(R.id.reset)).perform(click());
        Thread.sleep(1500);
        assertFalse(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).isCompleted());
        assertTrue(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).isEnabled());
    }

    @Test
    public void testCompletedSlideToActView_returnToBaseState_noAnimation() throws InterruptedException {
        ((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).setCompleted(false, false);
        Thread.sleep(1500);
        assertFalse(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).isCompleted());
        assertTrue(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).isEnabled());
        ((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).setCompleted(true, false);
        Thread.sleep(1500);
        assertTrue(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).isCompleted());
        assertFalse(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).isEnabled());
    }

    @Test
    public void testCompletedSlideToActView_returnToBaseState_withAnimation() throws InterruptedException {
        mActivityRule.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        ((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).setCompleted(false, true);
                    }
                }
        );
        Thread.sleep(2000);
        assertFalse(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).isCompleted());
        assertTrue(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).isEnabled());
        mActivityRule.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        ((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).setCompleted(true, true);
                    }
                }
        );
        Thread.sleep(2000);
        assertTrue(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).isCompleted());
        assertFalse(((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_completed)).isEnabled());
    }
}