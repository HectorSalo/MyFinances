package com.skysam.hchirinos.myfinances.principal;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.skysam.hchirinos.myfinances.R;

public class AcercaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
