package com.ncorti.slidetoact.example;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.WindowManager;

import com.ncorti.slidetoact.SlideToActView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.ncorti.slidetoact.example.testutil.SlideViewActions.clickCenter;
import static junit.framework.Assert.assertFalse;
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
                    Intent result = new Intent(targetContext, SampleActivity.class);
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
            public void onSlideComplete(@NonNull SlideToActView view) {
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
            public void onSlideReset(@NonNull SlideToActView view) {
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
    public void testOnSlideClickListener() throws Throwable {
        final boolean[] flag = {false};
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivityRule.getActivity().findViewById(R.id.slide_1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        flag[0] = true;
                    }
                });
            }
        });
        onView(withId(R.id.slide_1)).perform(click());
        assertTrue(flag[0]);
    }

    @Test
    public void testOnSlideUserFailedListener_withUserFailure() {
        final boolean[] flag = {false};
        ((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_1)).setOnSlideUserFailedListener(new SlideToActView.OnSlideUserFailedListener() {
            @Override
            public void onSlideFailed(@NonNull SlideToActView view, boolean isOutside) {
                // We test if the user clicked outside of the cursor.
                flag[0] = isOutside;
            }
        });
        onView(withId(R.id.slide_1)).perform(clickCenter());
        assertTrue(flag[0]);
    }

    @Test
    public void testOnSlideUserFailedListener_withCorrectSwipe() {
        final boolean[] flag = {false};
        ((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_1)).setOnSlideUserFailedListener(new SlideToActView.OnSlideUserFailedListener() {
            @Override
            public void onSlideFailed(@NonNull SlideToActView view, boolean isOutside) {
                // We test if the user clicked outside of the cursor.
                flag[0] = isOutside;
            }
        });
        onView(withId(R.id.slide_1)).perform(swipeRight());
        assertFalse(flag[0]);
    }

    @Test
    public void testOnSlideAnimationEventListener() throws InterruptedException {
        final boolean[] flag = {false, false, false, false};
        ((SlideToActView) mActivityRule.getActivity().findViewById(R.id.slide_1)).setOnSlideToActAnimationEventListener(new SlideToActView.OnSlideToActAnimationEventListener() {
            @Override
            public void onSlideCompleteAnimationEnded(@NonNull SlideToActView view) {
                flag[0] = true;
            }

            @Override
            public void onSlideCompleteAnimationStarted(@NonNull SlideToActView view, float threshold) {
                flag[1] = true;
            }

            @Override
            public void onSlideResetAnimationEnded(@NonNull SlideToActView view) {
                flag[2] = true;
            }

            @Override
            public void onSlideResetAnimationStarted(@NonNull SlideToActView view) {
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