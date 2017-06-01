package com.ncorti.slidetoact.example;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import com.ncorti.slidetoact.SlideToActView;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SliderEventCallBacksTest {

    @Rule
    public final ActivityTestRule<SampleActivity> mActivityRule =
            new ActivityTestRule<SampleActivity>(SampleActivity.class) {
                @Override
                protected Intent getActivityIntent() {
                    Context targetContext = InstrumentationRegistry.getInstrumentation()
                            .getTargetContext();
                    Intent result = new Intent(targetContext, MainActivity.class);
                    result.putExtra(SampleActivity.EXTRA_PRESSED_BUTTON, R.id.button_area_margin);
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
    public void testOnSlideCompleteListener() throws InterruptedException {
        final boolean[] flag = {false};
        ((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_1)).setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NotNull SlideToActView view) {
                flag[0] = true;
            }
        });
        onView(withId(R.id.slide_1)).perform(swipeRight());
        Thread.sleep(1400);
        assertTrue(flag[0]);
    }

    @Test
    public void testOnSlideResetListener() throws InterruptedException {
        final boolean[] flag = {false};
        ((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_1)).setOnSlideResetListener(new SlideToActView.OnSlideResetListener() {
            @Override
            public void onSlideReset(@NotNull SlideToActView view) {
                flag[0] = true;
            }
        });
        onView(withId(R.id.slide_1)).perform(swipeRight());
        Thread.sleep(1400);
        onView(withId(R.id.reset)).perform(click());
        Thread.sleep(1400);
        assertTrue(flag[0]);
    }

    @Test
    public void testOnSlideAnimationEventListener() throws InterruptedException {
        final boolean[] flag = {false, false, false, false};
        ((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_1)).setOnSlideToActAnimationEventListener(new SlideToActView.OnSlideToActAnimationEventListener() {
            @Override
            public void onSlideCompleteAnimationEnded(@NotNull SlideToActView view) {
                flag[0] = true;
            }

            @Override
            public void onSlideCompleteAnimationStarted(@NotNull SlideToActView view, float threshold) {
                flag[1] = true;
            }

            @Override
            public void onSlideResetAnimationEnded(@NotNull SlideToActView view) {
                flag[2] = true;
            }

            @Override
            public void onSlideResetAnimationStarted(@NotNull SlideToActView view) {
                flag[3] = true;
            }
        });
        onView(withId(R.id.slide_1)).perform(swipeRight());
        Thread.sleep(1400);
        assertTrue(flag[0]);
        assertTrue(flag[1]);

        onView(withId(R.id.reset)).perform(click());
        Thread.sleep(1400);
        assertTrue(flag[2]);
        assertTrue(flag[3]);
    }
}