package com.skysam.hchirinos.myfinances.ui.activityGeneral;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

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
    private TextInputLayout layoutIngreso;
    private TextInputEditText etIngreso;

    private TextView tvLabelBcv, tvLabelParalelo, tvLabelEuro;
    private TextView tvValueBcv, tvValueParalelo, tvValueEuro;

    private float valorCotizacionBCV, valorCotizacionParalelo, valorCotizacionEuro;
    private boolean bolivares = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);

        setContentView(R.layout.activity_calculadora);

        layoutIngreso = findViewById(R.id.outlined_ingreso);
        etIngreso = findViewById(R.id.et_ingreso);

        tvLabelBcv = findViewById(R.id.tv_label_bcv);
        tvLabelParalelo = findViewById(R.id.tv_label_paralelo);
        tvLabelEuro = findViewById(R.id.tv_label_euro);

        tvValueBcv = findViewById(R.id.tv_value_bcv);
        tvValueParalelo = findViewById(R.id.tv_value_paralelo);
        tvValueEuro = findViewById(R.id.tv_value_euro);

        // Tasas guardadas (defaults seguros)
        valorCotizacionBCV = sharedPreferences.getFloat(Constants.VALOR_COTIZACION, 1f);
        valorCotizacionParalelo = sharedPreferences.getFloat(Constants.VALOR_COTIZACION_PARALELO, 1f);
        valorCotizacionEuro = sharedPreferences.getFloat(Constants.VALOR_COTIZACION_EURO, 1f);

        // Estado inicial
        renderMode();
        resetResults();

        etIngreso.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                formatAndConvert(s.toString(), etIngreso, this);
            }
        });

        MaterialButton button = findViewById(R.id.btn_convertir);
        button.setOnClickListener(v -> cambiarMoneda());
    }

    private void cambiarMoneda() {
        bolivares = !bolivares;
        renderMode();

        etIngreso.setText(R.string.text_calculadora_cero);
        etIngreso.setSelection(etIngreso.getText() != null ? etIngreso.getText().length() : 0);
        resetResults();
    }

    private void renderMode() {
        if (bolivares) {
            layoutIngreso.setHint(getString(R.string.calculadora_bolivares));

            tvLabelBcv.setText(getString(R.string.calculadora_dolares_bcv));
            tvLabelParalelo.setText(getString(R.string.calculadora_dolares_paralelo));
            tvLabelEuro.setText(getString(R.string.rate_euro));
        } else {
            layoutIngreso.setHint(getString(R.string.calculadora_dolares));

            tvLabelBcv.setText(getString(R.string.calculadora_bolivares_bcv));
            tvLabelParalelo.setText(getString(R.string.calculadora_bolivares_paralelo));
            tvLabelEuro.setText(getString(R.string.rate_euro)); // EUR desde USD (tasa implícita)
        }
    }

    private void resetResults() {
        tvValueBcv.setText(getString(R.string.text_calculadora_cero));
        tvValueParalelo.setText(getString(R.string.text_calculadora_cero));
        tvValueEuro.setText(getString(R.string.text_calculadora_cero));
    }

    private void convertirMoneda(String montoFormateado) {
        // montoFormateado viene como "1.234,56"
        if (montoFormateado == null || montoFormateado.trim().isEmpty()) {
            resetResults();
            return;
        }

        String normalized = montoFormateado.replace(".", "").replace(",", ".");
        float montoIngresado;
        try {
            montoIngresado = Float.parseFloat(normalized);
        } catch (Exception e) {
            resetResults();
            return;
        }

        if (montoIngresado <= 0f) {
            resetResults();
            return;
        }

        float resultBcv;
        float resultParalelo;
        float resultEuro;

        if (bolivares) {
            // Bs -> USD / EUR
            resultBcv = safeDiv(montoIngresado, valorCotizacionBCV);
            resultParalelo = safeDiv(montoIngresado, valorCotizacionParalelo);
            resultEuro = safeDiv(montoIngresado, valorCotizacionEuro);
        } else {
            // USD -> Bs y EUR (tasa implícita con VES)
            resultBcv = montoIngresado * valorCotizacionBCV;
            resultParalelo = montoIngresado * valorCotizacionParalelo;

            float usdToEur = safeDiv(valorCotizacionBCV, valorCotizacionEuro); // (VES/USD)/(VES/EUR) = EUR/USD
            resultEuro = montoIngresado * usdToEur;
        }

        tvValueBcv.setText(ClassesCommon.INSTANCE.convertFloatToString(resultBcv));
        tvValueParalelo.setText(ClassesCommon.INSTANCE.convertFloatToString(resultParalelo));
        tvValueEuro.setText(ClassesCommon.INSTANCE.convertFloatToString(resultEuro));
    }

    private float safeDiv(float a, float b) {
        if (b == 0f) return 0f;
        return a / b;
    }

    private void formatAndConvert(String raw, TextInputEditText editText, TextWatcher watcher) {
        if (raw == null) raw = "";

        // Permitir vacío sin crashear
        String cleaned = raw.replace(",", "").replace(".", "");
        if (cleaned.isEmpty()) {
            editText.removeTextChangedListener(watcher);
            editText.setText("");
            editText.addTextChangedListener(watcher);
            resetResults();
            return;
        }

        double cantidad;
        try {
            cantidad = Double.parseDouble(cleaned) / 100d;
        } catch (Exception e) {
            resetResults();
            return;
        }

        String formatted = String.format(Locale.GERMANY, "%,.2f", cantidad);

        editText.removeTextChangedListener(watcher);
        editText.setText(formatted);
        editText.setSelection(formatted.length());
        editText.addTextChangedListener(watcher);

        convertirMoneda(formatted);
    }
}