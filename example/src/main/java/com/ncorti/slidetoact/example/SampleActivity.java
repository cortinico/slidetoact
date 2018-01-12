package com.ncorti.slidetoact.example;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ncorti.slidetoact.SlideToActView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SampleActivity extends AppCompatActivity {

    public static final String EXTRA_PRESSED_BUTTON = "extra_pressed_button";
    private List<SlideToActView> mSlideList;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        final int pressedButton = getIntent().getExtras().getInt(EXTRA_PRESSED_BUTTON, 0);
        dateFormat = new SimpleDateFormat("HH:mm:ss", getResources().getConfiguration().locale);

        switch (pressedButton) {
            case R.id.button_area_margin:
                setContentView(R.layout.content_area_margin);
                break;
            case R.id.button_colors:
                setContentView(R.layout.content_color);
                break;
            case R.id.button_border_radius:
                setContentView(R.layout.content_border_radius);
                break;
            case R.id.button_elevation:
                setContentView(R.layout.content_elevation);
                break;
            case R.id.button_text_size:
                setContentView(R.layout.content_text_size);
                break;
            case R.id.button_slider_dimension:
                setContentView(R.layout.content_slider_dimensions);
                break;
            case R.id.button_event_callbacks:
                setContentView(R.layout.content_event_callbacks);
                setupEventCallbacks();
                break;
            case R.id.button_locked_slider:
                setContentView(R.layout.content_locked_slider);
                break;
            default:
                finish();
                break;
        }
        mSlideList = getSlideList();

    }

    private List<SlideToActView> getSlideList() {
        final List<SlideToActView> slideList = new ArrayList<>();
        final LinearLayout container = findViewById(R.id.slide_container);
        for (int i = 0; i < container.getChildCount(); i++) {
            final View child = container.getChildAt(i);
            if (child instanceof SlideToActView) {
                slideList.add((SlideToActView) child);
            }
        }
        return slideList;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
                for (SlideToActView slide : mSlideList) {
                    slide.resetSlider();
                }
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setupEventCallbacks() {
        final SlideToActView slide = findViewById(R.id.event_slider);
        final TextView log = findViewById(R.id.event_log);
        slide.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NonNull SlideToActView view) {
                log.append("\n" + getTime() + " onSlideComplete");
            }
        });
        slide.setOnSlideResetListener(new SlideToActView.OnSlideResetListener() {
            @Override
            public void onSlideReset(@NonNull SlideToActView view) {
                log.append("\n" + getTime() + " onSlideReset");
            }
        });
        slide.setOnSlideToActAnimationEventListener(new SlideToActView.OnSlideToActAnimationEventListener() {
            @Override
            public void onSlideCompleteAnimationStarted(@NonNull SlideToActView view, float threshold) {
                log.append("\n" + getTime() + " onSlideCompleteAnimationStarted - " + threshold + "");
            }

            @Override
            public void onSlideCompleteAnimationEnded(@NonNull SlideToActView view) {
                log.append("\n" + getTime() + " onSlideCompleteAnimationEnded");
            }

            @Override
            public void onSlideResetAnimationStarted(@NonNull SlideToActView view) {
                log.append("\n" + getTime() + " onSlideResetAnimationStarted");
            }

            @Override
            public void onSlideResetAnimationEnded(@NonNull SlideToActView view) {
                log.append("\n" + getTime() + " onSlideResetAnimationEnded");
            }
        });
    }

    private String getTime() {
        return dateFormat.format(new Date());
    }
}
