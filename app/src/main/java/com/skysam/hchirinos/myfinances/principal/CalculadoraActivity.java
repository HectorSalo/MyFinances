package com.skysam.hchirinos.myfinances.principal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skysam.hchirinos.myfinances.R;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CalculadoraActivity extends AppCompatActivity {

    private TextInputLayout layoutIngreso, layoutResultado;
    private TextInputEditText etIngreso, etResultado;
    private float valorCotizacion;
    private boolean bolivares;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculadora);

        etIngreso = findViewById(R.id.et_ingreso);
        etResultado = findViewById(R.id.et_resultado);
        layoutIngreso = findViewById(R.id.outlined_ingreso);
        layoutResultado = findViewById(R.id.outlined_resultado);

        bolivares = true;

        SharedPreferences sharedPreferences = getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);
        valorCotizacion = sharedPreferences.getFloat("valor_cotizacion", 1);

        etIngreso.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String monto = String.valueOf(s);
                if (monto.isEmpty()) {
                    convertirMoneda("0");
                } else {
                    convertirMoneda(monto);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ImageButton imageButton = findViewById(R.id.imageButton_convertir);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarMoneda();
            }
        });
    }

    private void cambiarMoneda() {
        if (bolivares) {
            layoutIngreso.setHint(getResources().getString(R.string.calculadora_dolares));
            layoutResultado.setHint(getResources().getString(R.string.calculadora_bolivares));
            bolivares = false;
        } else {
            layoutIngreso.setHint(getResources().getString(R.string.calculadora_bolivares));
            layoutResultado.setHint(getResources().getString(R.string.calculadora_dolares));
            bolivares = true;
        }
        etIngreso.setText("");
    }

    private void convertirMoneda (String monto) {
        float montoIngresado = Float.parseFloat(monto);
        double montoTotal;

        if (bolivares) {
            montoTotal = montoIngresado / valorCotizacion;
        } else {
            montoTotal = montoIngresado * valorCotizacion;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            DecimalFormat df = new DecimalFormat("#,###.0000");
            etResultado.setText(df.format(montoTotal));
        } else {
            etResultado.setText(String.format("%.4f", montoTotal));
        }
    }
}