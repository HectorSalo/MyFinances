package com.skysam.hchirinos.myfinances.ui.activityGeneral;

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
import com.skysam.hchirinos.myfinances.common.utils.Constants;

import java.util.Locale;

public class CalculadoraActivity extends AppCompatActivity {

    private TextInputLayout layoutIngreso, layoutResultado;
    private TextInputEditText etIngreso, etResultado;
    private float valorCotizacion;
    private boolean bolivares;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);

        setContentView(R.layout.activity_calculadora);

        etIngreso = findViewById(R.id.et_ingreso);
        etResultado = findViewById(R.id.et_resultado);
        layoutIngreso = findViewById(R.id.outlined_ingreso);
        layoutResultado = findViewById(R.id.outlined_resultado);

        bolivares = true;

        valorCotizacion = sharedPreferences.getFloat(Constants.VALOR_COTIZACION, 1);

        etIngreso.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                format(s.toString(), etIngreso, this);
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
        etIngreso.setText(R.string.text_calculadora_cero);
        etResultado.setText(R.string.text_calculadora_cero);
    }

    private void convertirMoneda (String monto) {
        monto = monto.replace(".", "").replace(",", ".");
        float montoIngresado = Float.parseFloat(monto);
        double montoTotal;

        if (bolivares) {
            montoTotal = montoIngresado / valorCotizacion;
        } else {
            montoTotal = montoIngresado * valorCotizacion;
        }

        monto = String.format(Locale.getDefault(), "%,.2f",montoTotal);
        etResultado.setText(monto);
    }


    public void format (String cadena, TextInputEditText editText, TextWatcher textWatcher) {
        cadena = cadena.replace(",","").replace(".","");
        double cantidad = Double.parseDouble(cadena)/100;
        cadena = String.format(Locale.getDefault(), "%,.2f",cantidad);

        editText.removeTextChangedListener(textWatcher);
        editText.setText(cadena);
        editText.setSelection(cadena.length());
        editText.addTextChangedListener(textWatcher);

        convertirMoneda(cadena);
    }

}