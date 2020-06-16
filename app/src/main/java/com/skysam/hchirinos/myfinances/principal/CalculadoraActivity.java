package com.skysam.hchirinos.myfinances.principal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skysam.hchirinos.myfinances.R;

public class CalculadoraActivity extends AppCompatActivity {

    private TextInputLayout layoutBolivares, layoutDolares;
    private TextInputEditText etBolivares, etDolares;
    private float valorCotizacion;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculadora);

        layoutBolivares = findViewById(R.id.outlined_bolivares);
        layoutDolares = findViewById(R.id.outlined_dolares);
        etBolivares = findViewById(R.id.et_bolivares);
        etDolares = findViewById(R.id.et_dolares);

        SharedPreferences sharedPreferences = getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);
        valorCotizacion = sharedPreferences.getFloat("valor_cotizacion", 1);

        etBolivares.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String monto = String.valueOf(s);
                convertirMoneda(monto, true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etDolares.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String monto = String.valueOf(s);
                convertirMoneda(monto, false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void convertirMoneda (String monto, boolean bolivares) {
        float montoIngresado = Float.parseFloat(monto);
        float montoTotal;

        if (bolivares) {
            montoTotal = montoIngresado / valorCotizacion;
            etDolares.setText("$" + montoTotal);
        } else {
            montoTotal = montoIngresado * valorCotizacion;
            etBolivares.setText("Bs. " + montoTotal);
        }
    }
}