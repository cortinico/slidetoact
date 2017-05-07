package com.ncorti.slidetoact.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.ncorti.slidetoact.SlideToActView;


public class MainSlideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);

        final SlideToActView stav1 = (SlideToActView) findViewById(R.id.slide1);
        final SlideToActView stav2 = (SlideToActView) findViewById(R.id.slide2);
        final SlideToActView stav3 = (SlideToActView) findViewById(R.id.slide3);
        final SlideToActView stav4 = (SlideToActView) findViewById(R.id.slide4);

        stav1.setLocked(true);

        Button b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stav1.isCompleted()) stav1.resetSlider();
                if (stav2.isCompleted()) stav2.resetSlider();
                if (stav3.isCompleted()) stav3.resetSlider();
                if (stav4.isCompleted()) stav4.resetSlider();
            }
        });

    }

}
