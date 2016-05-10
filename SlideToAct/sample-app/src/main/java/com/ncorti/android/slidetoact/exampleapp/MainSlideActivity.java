package com.ncorti.android.slidetoact.exampleapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.ncorti.android.slidetoact.SlideToActView;


public class MainSlideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar t = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(t);

        final SlideToActView stav = (SlideToActView)findViewById(R.id.slide);

        Button b = (Button) findViewById(R.id.button);
        if (b != null && stav != null) {
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stav.resetSlider();
                }
            });
        }
    }

}
