package com.skysam.hchirinos.myfinances.ui.activityGeneral;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon;
import com.skysam.hchirinos.myfinances.common.utils.Constants;

import java.util.Locale;

public class CalculadoraActivity extends AppCompatActivity {

    private enum CalcMode { CONVERSION, VENTA }

    // ── Modo actual ──────────────────────────────────────────────────────────
    private CalcMode currentMode = CalcMode.CONVERSION;

    // ── Tasas ────────────────────────────────────────────────────────────────
    private float valorCotizacionBCV, valorCotizacionParalelo, valorCotizacionEuro;

    // ── Secciones ────────────────────────────────────────────────────────────
    private LinearLayout sectionConversion, sectionVenta;

    // ── Conversión ───────────────────────────────────────────────────────────
    private TextInputLayout layoutIngreso;
    private TextInputEditText etIngreso;
    private TextView tvLabelBcv, tvLabelParalelo, tvLabelEuro;
    private TextView tvValueBcv, tvValueParalelo, tvValueEuro;
    private boolean bolivares = true;
    private TextWatcher watcherIngreso;

    // ── Venta ────────────────────────────────────────────────────────────────
    private TextInputEditText etDolares, etTasa;
    private TextView tvVentaRecibes, tvVentaEquivalente, tvVentaDifBs, tvVentaDifUsd;
    private TextWatcher watcherDolares, watcherTasa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);

        setContentView(R.layout.activity_calculadora);

        // Tasas guardadas
        valorCotizacionBCV      = sharedPreferences.getFloat(Constants.VALOR_COTIZACION, 1f);
        valorCotizacionParalelo = sharedPreferences.getFloat(Constants.VALOR_COTIZACION_PARALELO, 1f);
        valorCotizacionEuro     = sharedPreferences.getFloat(Constants.VALOR_COTIZACION_EURO, 1f);

        // Secciones
        sectionConversion = findViewById(R.id.section_conversion);
        sectionVenta      = findViewById(R.id.section_venta);

        // ── Vistas: Conversión ────────────────────────────────────────────────
        layoutIngreso    = findViewById(R.id.outlined_ingreso);
        etIngreso        = findViewById(R.id.et_ingreso);
        tvLabelBcv       = findViewById(R.id.tv_label_bcv);
        tvLabelParalelo  = findViewById(R.id.tv_label_paralelo);
        tvLabelEuro      = findViewById(R.id.tv_label_euro);
        tvValueBcv       = findViewById(R.id.tv_value_bcv);
        tvValueParalelo  = findViewById(R.id.tv_value_paralelo);
        tvValueEuro      = findViewById(R.id.tv_value_euro);

        // ── Vistas: Venta ─────────────────────────────────────────────────────
        etDolares         = findViewById(R.id.et_dolares);
        etTasa            = findViewById(R.id.et_tasa);
        tvVentaRecibes    = findViewById(R.id.tv_venta_recibes);
        tvVentaEquivalente= findViewById(R.id.tv_venta_equivalente);
        tvVentaDifBs      = findViewById(R.id.tv_venta_dif_bs);
        tvVentaDifUsd     = findViewById(R.id.tv_venta_dif_usd);

        // ── Estado inicial conversión ─────────────────────────────────────────
        renderModeConversion();
        resetResults();

        // ── Watcher: campo conversión ─────────────────────────────────────────
        watcherIngreso = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                formatField(s.toString(), etIngreso, watcherIngreso, () -> {
                    String txt = etIngreso.getText() != null ? etIngreso.getText().toString() : "";
                    convertirMoneda(txt);
                });
            }
        };
        etIngreso.addTextChangedListener(watcherIngreso);

        // ── Watcher: monto dólares (Venta) ────────────────────────────────────
        watcherDolares = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                formatField(s.toString(), etDolares, watcherDolares, () -> calcularVenta());
            }
        };
        etDolares.addTextChangedListener(watcherDolares);

        // ── Watcher: tasa venta (Venta) ───────────────────────────────────────
        watcherTasa = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                formatField(s.toString(), etTasa, watcherTasa, () -> calcularVenta());
            }
        };
        etTasa.addTextChangedListener(watcherTasa);

        // ── Botón cambiar moneda ──────────────────────────────────────────────
        MaterialButton btnConvertir = findViewById(R.id.btn_convertir);
        btnConvertir.setOnClickListener(v -> cambiarMoneda());

        // ── Toggle de modo ────────────────────────────────────────────────────
        MaterialButtonToggleGroup toggleModo = findViewById(R.id.toggle_modo);
        toggleModo.check(R.id.btn_modo_conversion);
        toggleModo.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_modo_conversion) {
                currentMode = CalcMode.CONVERSION;
                sectionConversion.setVisibility(View.VISIBLE);
                sectionVenta.setVisibility(View.GONE);
            } else {
                currentMode = CalcMode.VENTA;
                sectionConversion.setVisibility(View.GONE);
                sectionVenta.setVisibility(View.VISIBLE);
            }
        });
    }

    // ── Conversión ────────────────────────────────────────────────────────────

    private void cambiarMoneda() {
        bolivares = !bolivares;
        renderModeConversion();
        etIngreso.setText(R.string.text_calculadora_cero);
        etIngreso.setSelection(etIngreso.getText() != null ? etIngreso.getText().length() : 0);
    }

    private void renderModeConversion() {
        if (bolivares) {
            layoutIngreso.setHint(getString(R.string.calculadora_bolivares));
            tvLabelBcv.setText(getString(R.string.calculadora_dolares_bcv));
            tvLabelParalelo.setText(getString(R.string.calculadora_dolares_paralelo));
            tvLabelEuro.setText(getString(R.string.rate_euro));
        } else {
            layoutIngreso.setHint(getString(R.string.calculadora_dolares));
            tvLabelBcv.setText(getString(R.string.calculadora_bolivares_bcv));
            tvLabelParalelo.setText(getString(R.string.calculadora_bolivares_paralelo));
            tvLabelEuro.setText(getString(R.string.rate_euro));
        }
    }

    private void resetResults() {
        String cero = getString(R.string.text_calculadora_cero);
        tvValueBcv.setText(cero);
        tvValueParalelo.setText(cero);
        tvValueEuro.setText(cero);
    }

    private void convertirMoneda(String montoFormateado) {
        if (montoFormateado == null || montoFormateado.trim().isEmpty()) {
            resetResults();
            return;
        }
        float montoIngresado = parseFormatted(montoFormateado);
        if (montoIngresado <= 0f) {
            resetResults();
            return;
        }

        float resultBcv, resultParalelo, resultEuro;
        if (bolivares) {
            resultBcv      = safeDiv(montoIngresado, valorCotizacionBCV);
            resultParalelo = safeDiv(montoIngresado, valorCotizacionParalelo);
            resultEuro     = safeDiv(montoIngresado, valorCotizacionEuro);
        } else {
            resultBcv      = montoIngresado * valorCotizacionBCV;
            resultParalelo = montoIngresado * valorCotizacionParalelo;
            float usdToEur = safeDiv(valorCotizacionBCV, valorCotizacionEuro);
            resultEuro     = montoIngresado * usdToEur;
        }

        tvValueBcv.setText(ClassesCommon.INSTANCE.convertFloatToString(resultBcv));
        tvValueParalelo.setText(ClassesCommon.INSTANCE.convertFloatToString(resultParalelo));
        tvValueEuro.setText(ClassesCommon.INSTANCE.convertFloatToString(resultEuro));
    }

    // ── Venta ─────────────────────────────────────────────────────────────────

    private void resetVentaResults() {
        String cero = getString(R.string.text_calculadora_cero);
        tvVentaRecibes.setText(cero);
        tvVentaEquivalente.setText(cero);
        tvVentaDifBs.setText(cero);
        tvVentaDifUsd.setText(cero);
    }

    private void calcularVenta() {
        String rawDolares = etDolares.getText() != null ? etDolares.getText().toString() : "";
        String rawTasa    = etTasa.getText() != null ? etTasa.getText().toString() : "";

        if (rawDolares.isEmpty() || rawTasa.isEmpty()) {
            resetVentaResults();
            return;
        }

        float dolares = parseFormatted(rawDolares);
        float tasa    = parseFormatted(rawTasa);

        if (dolares <= 0f || tasa <= 0f) {
            resetVentaResults();
            return;
        }

        float bolivaresRecibidos       = dolares * tasa;
        float equivalenteBcvEnDolares  = safeDiv(bolivaresRecibidos, valorCotizacionBCV);
        float bolivaresBcvOriginales   = dolares * valorCotizacionBCV;
        float diferenciaBolivares      = bolivaresRecibidos - bolivaresBcvOriginales;
        float diferenciaDolaresBcv     = equivalenteBcvEnDolares - dolares;

        tvVentaRecibes.setText("Bs. " + ClassesCommon.INSTANCE.convertFloatToString(bolivaresRecibidos));
        tvVentaEquivalente.setText("$ " + ClassesCommon.INSTANCE.convertFloatToString(equivalenteBcvEnDolares));

        String signBs  = diferenciaBolivares >= 0f ? "+" : "";
        String signUsd = diferenciaDolaresBcv >= 0f ? "+" : "";
        tvVentaDifBs.setText(signBs + "Bs. " + ClassesCommon.INSTANCE.convertFloatToString(diferenciaBolivares));
        tvVentaDifUsd.setText(signUsd + "$ " + ClassesCommon.INSTANCE.convertFloatToString(diferenciaDolaresBcv));
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    /**
     * Formatea el campo como monto monetario controlado (sin edición libre).
     * El usuario ingresa dígitos; el valor se desplaza a la izquierda y se divide entre 100.
     * Llama onChange al terminar (con el campo vacío o con el valor formateado ya escrito).
     */
    private void formatField(String raw, TextInputEditText editText,
                              TextWatcher watcher, Runnable onChange) {
        if (raw == null) raw = "";
        String cleaned = raw.replace(",", "").replace(".", "");
        if (cleaned.isEmpty()) {
            editText.removeTextChangedListener(watcher);
            editText.setText("");
            editText.addTextChangedListener(watcher);
            onChange.run();
            return;
        }
        double cantidad;
        try {
            cantidad = Double.parseDouble(cleaned) / 100d;
        } catch (Exception e) {
            onChange.run();
            return;
        }
        String formatted = String.format(Locale.GERMANY, "%,.2f", cantidad);
        editText.removeTextChangedListener(watcher);
        editText.setText(formatted);
        editText.setSelection(formatted.length());
        editText.addTextChangedListener(watcher);
        onChange.run();
    }

    /** Parsea un monto formateado con Locale.GERMANY ("1.234,56") a float. */
    private float parseFormatted(String formatted) {
        if (formatted == null || formatted.isEmpty()) return 0f;
        String normalized = formatted.replace(".", "").replace(",", ".");
        try {
            return Float.parseFloat(normalized);
        } catch (Exception e) {
            return 0f;
        }
    }

    private float safeDiv(float a, float b) {
        if (b == 0f) return 0f;
        return a / b;
    }
}