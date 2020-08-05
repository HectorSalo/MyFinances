package com.skysam.hchirinos.myfinances.principal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.airbnb.lottie.LottieAnimationView;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.inicioSesion.InicSesionActivity;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        final LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimationView);

        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                lottieAnimationView.cancelAnimation();
            }
        }, 4000);

        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), InicSesionActivity.class);
                startActivity(intent);
                finish();
            }
        }, 4500);
    }
}