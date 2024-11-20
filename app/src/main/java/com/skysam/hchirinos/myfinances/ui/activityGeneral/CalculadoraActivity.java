package com.skysam.hchirinos.myfinances.ui.activityGeneral;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon;
import com.skysam.hchirinos.myfinances.common.utils.Constants;

import java.util.Locale;

public class CalculadoraActivity extends AppCompatActivity {

    private TextInputLayout layoutIngreso, layoutResultado, layoutResultado2, layoutResultado3;
    private TextInputEditText etIngreso, etResultado, etResultado2, etResultado3;
    private float valorCotizacionBCV, valorCotizacionParalelo;
    private boolean bolivares;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);

        setContentView(R.layout.activity_calculadora);

        etIngreso = findViewById(R.id.et_ingreso);
        etResultado = findViewById(R.id.et_resultado);
        etResultado2 = findViewById(R.id.et_resultado2);
        etResultado3 = findViewById(R.id.et_resultado3);
        layoutIngreso = findViewById(R.id.outlined_ingreso);
        layoutResultado = findViewById(R.id.outlined_resultado);
        layoutResultado2 = findViewById(R.id.outlined_resultado2);
        layoutResultado3 = findViewById(R.id.outlined_resultado3);

        bolivares = true;

        valorCotizacionBCV = sharedPreferences.getFloat(Constants.VALOR_COTIZACION, 1);
        valorCotizacionParalelo = sharedPreferences.getFloat(Constants.VALOR_COTIZACION_PARALELO, 1);

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

        MaterialButton button = findViewById(R.id.imageButton_convertir);
        button.setOnClickListener(v -> cambiarMoneda());
    }

    private void cambiarMoneda() {
        if (bolivares) {
            layoutIngreso.setHint(getResources().getString(R.string.calculadora_dolares));
            layoutResultado.setHint(getResources().getString(R.string.calculadora_bolivares_bcv));
            layoutResultado2.setHint(getResources().getString(R.string.calculadora_bolivares_paralelo));
            layoutResultado3.setHint(getResources().getString(R.string.calculadora_bolivares_promedio));
            bolivares = false;
        } else {
            layoutIngreso.setHint(getResources().getString(R.string.calculadora_bolivares));
            layoutResultado.setHint(getResources().getString(R.string.calculadora_dolares_bcv));
            layoutResultado2.setHint(getResources().getString(R.string.calculadora_dolares_paralelo));
            layoutResultado3.setHint(getResources().getString(R.string.calculadora_dolares_promedio));
            bolivares = true;
        }
        etIngreso.setText(R.string.text_calculadora_cero);
        etResultado.setText(R.string.text_calculadora_cero);
        etResultado2.setText(R.string.text_calculadora_cero);
        etResultado3.setText(R.string.text_calculadora_cero);
    }

    private void convertirMoneda (String monto) {
        monto = monto.replace(".", "").replace(",", ".");
        float montoIngresado = Float.parseFloat(monto);
        float montoBCV;
        float montoParalelo;
        float montoPromedio;

       if (bolivares) {
            montoBCV = montoIngresado / valorCotizacionBCV;
            montoParalelo = montoIngresado / valorCotizacionParalelo;
       } else {
            montoBCV = montoIngresado * valorCotizacionBCV;
            montoParalelo = montoIngresado * valorCotizacionParalelo;
       }
       montoPromedio = (montoBCV + montoParalelo) / 2;
       etResultado.setText(ClassesCommon.INSTANCE.convertFloatToString(montoBCV));
       etResultado2.setText(ClassesCommon.INSTANCE.convertFloatToString(montoParalelo));
       etResultado3.setText(ClassesCommon.INSTANCE.convertFloatToString(montoPromedio));
    }


    public void format (String cadena, TextInputEditText editText, TextWatcher textWatcher) {
        cadena = cadena.replace(",","").replace(".","");
        double cantidad = Double.parseDouble(cadena)/100;
        cadena = String.format(Locale.GERMANY, "%,.2f",cantidad);

        editText.removeTextChangedListener(textWatcher);
        editText.setText(cadena);
        editText.setSelection(cadena.length());
        editText.addTextChangedListener(textWatcher);

        convertirMoneda(cadena);
    }

}