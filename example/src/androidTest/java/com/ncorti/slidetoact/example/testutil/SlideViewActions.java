package com.ncorti.slidetoact.example.testutil;

import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.view.View;

public class SlideViewActions {
    /**
     * Perform a click in the center of the View.
     */
    public static ViewAction clickCenter() {
        return new GeneralClickAction(
                Tap.SINGLE,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {

                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);
                        final float width = view.getWidth();
                        final float height = view.getHeight();

                        final float centerX = screenPos[0] + (width / 2);
                        final float centerY = screenPos[1] + (height / 2);

                        float[] coordinates = {centerX, centerY};

                        return coordinates;
                    }
                },
                Press.FINGER);
    }
}
