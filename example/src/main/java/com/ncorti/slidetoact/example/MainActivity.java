package com.ncorti.slidetoact.example;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ncorti.slidetoact.SlideToActView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((TextView) findViewById(R.id.welcome_text)).setText("Welcome 😁");

        findViewById(R.id.button_area_margin).setOnClickListener(this);
        findViewById(R.id.button_icon_margin).setOnClickListener(this);
        findViewById(R.id.button_colors).setOnClickListener(this);
        findViewById(R.id.button_border_radius).setOnClickListener(this);
        findViewById(R.id.button_elevation).setOnClickListener(this);
        findViewById(R.id.button_text_size).setOnClickListener(this);
        findViewById(R.id.button_slider_dimension).setOnClickListener(this);
        findViewById(R.id.button_event_callbacks).setOnClickListener(this);
        findViewById(R.id.button_locked_slider).setOnClickListener(this);
        findViewById(R.id.button_custom_icon).setOnClickListener(this);
        findViewById(R.id.button_reversed_slider).setOnClickListener(this);
        findViewById(R.id.button_animation_duration).setOnClickListener(this);
        findViewById(R.id.button_bump_vibration).setOnClickListener(this);
        findViewById(R.id.button_completed).setOnClickListener(this);
        findViewById(R.id.button_bounce).setOnClickListener(this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reset) {
            ((SlideToActView) findViewById(R.id.welcome_slider)).setCompleted(false, true);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, SampleActivity.class);
        intent.putExtra(SampleActivity.EXTRA_PRESSED_BUTTON, view.getId());

        int position =
                ((SlideToActView) findViewById(R.id.welcome_slider)).getSliderCursorPosition();
        Log.e("SlideToActView", "Position: " + position);
        boolean isCompleted = ((SlideToActView) findViewById(R.id.welcome_slider)).isCompleted();
        if (position == 0 || isCompleted) {
            startActivity(intent);
        } else {
            Log.e("SlideToActView", "Oops! Please wait for slider to reset to Zero | Position: " + position);
        }

    }
}
