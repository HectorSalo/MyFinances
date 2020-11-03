package com.skysam.hchirinos.myfinances.inicioSesionModule.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.Constants;


public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            SharedPreferences sharedPreferences = getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);

            String tema = sharedPreferences.getString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_SISTEMA);

            switch (tema) {
                case Constants.PREFERENCE_TEMA_SISTEMA:
                    setTheme(R.style.AppTheme);
                    break;
                case Constants.PREFERENCE_TEMA_OSCURO:
                    setTheme(R.style.AppThemeNight);
                    break;
                case Constants.PREFERENCE_TEMA_CLARO:
                    setTheme(R.style.AppThemeDay);
                    break;
            }
        }
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
        }, 4000);
    }
}