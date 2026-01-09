package com.skysam.hchirinos.myfinances.ahorrosModule.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth;
import com.skysam.hchirinos.myfinances.common.utils.Constants;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AgregarAhorroFragment extends Fragment {

    private TextInputEditText etConcepto, etMonto, etOrigen;
    private TextInputLayout etConceptoLayout, etMontoLayout, etOrigenLayout;
    private String concepto;
    private double monto;
    private RadioButton rbDolar;
    private ProgressBar progressBar;
    private Button btnGuardar;
    private TextWatcher montoWatcher;
    private CheckBox cbCapital;
    private boolean isFormattingMonto = false;

    public AgregarAhorroFragment(){}

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_agregar_ahorro, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etConcepto = view.findViewById(R.id.et_concepto);
        etConceptoLayout = view.findViewById(R.id.outlined_concepto);
        etMonto = view.findViewById(R.id.et_monto);
        etOrigen = view.findViewById(R.id.et_origen);
        etOrigenLayout = view.findViewById(R.id.outlined_origen);
        etMontoLayout = view.findViewById(R.id.outlined_monto);
        rbDolar = view.findViewById(R.id.radioButton_dolares);
        cbCapital = view.findViewById(R.id.cb_capital);

        progressBar = view.findViewById(R.id.progressBar_agregar_ahorro);

        rbDolar.setChecked(true);

        montoWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormattingMonto) return;
                formatAmountInput(s.toString(), etMonto, this);
            }
        };
        etMonto.addTextChangedListener(montoWatcher);

        btnGuardar = view.findViewById(R.id.button_guardar);
        btnGuardar.setOnClickListener(view1 -> validarDatos());
    }

    private void formatAmountInput(String raw, TextInputEditText editText, TextWatcher watcher) {
        if (raw == null) raw = "";

        // Permitir vacío
        String cleaned = raw.replace(",", "").replace(".", "").replace(" ", "");
        if (cleaned.isEmpty()) {
            isFormattingMonto = true;
            editText.removeTextChangedListener(watcher);
            editText.setText("");
            editText.addTextChangedListener(watcher);
            isFormattingMonto = false;
            return;
        }

        double cantidad;
        try {
            // "Traslado a la izquierda": 1234 -> 12.34
            cantidad = Double.parseDouble(cleaned) / 100d;
        } catch (Exception e) {
            return;
        }

        // Formato con separadores + 2 decimales
        String formatted = String.format(java.util.Locale.GERMANY, "%,.2f", cantidad);

        isFormattingMonto = true;
        editText.removeTextChangedListener(watcher);
        editText.setText(formatted);
        editText.setSelection(formatted.length()); // cursor al final
        editText.addTextChangedListener(watcher);
        isFormattingMonto = false;
    }


    private void validarDatos() {
        etConceptoLayout.setError(null);
        etMontoLayout.setError(null);
        concepto = etConcepto.getText().toString();
        String montoS = etMonto.getText().toString();
        boolean conceptoValido;
        boolean montovalido;

        if (!concepto.isEmpty()) {
            conceptoValido = true;
        } else {
            conceptoValido = false;
            etConceptoLayout.setError("Campo obligatorio");
        }
        if (!montoS.isEmpty()) {
            String normalized = montoS.replace(".", "").replace(",", ".");
            monto = Double.parseDouble(normalized);

            if (monto > 0) {
                montovalido = true;
            } else {
                montovalido = false;
                etMontoLayout.setError("El monto debe ser mayor a cero");
            }
        } else {
            montovalido = false;
            etMontoLayout.setError("Campo obligatorio");
        }

        if (montovalido && conceptoValido) {
            guardarDatos();
        }
    }

    private void guardarDatos() {
        String origen = etOrigen.getText().toString();
        progressBar.setVisibility(View.VISIBLE);
        etMontoLayout.setEnabled(false);
        etConceptoLayout.setEnabled(false);
        etOrigenLayout.setEnabled(false);
        btnGuardar.setEnabled(false);
        Calendar calendar = Calendar.getInstance();
        int mes = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        Date fechaIngreso = calendar.getTime();
        boolean dolar = rbDolar.isChecked();
        boolean capital = cbCapital != null && cbCapital.isChecked();

        if(origen.isEmpty()) {
            origen = null;
        }

        Map<String, Object> docData = new HashMap<>();
        docData.put(Constants.BD_CONCEPTO, concepto);
        docData.put(Constants.BD_MONTO, monto);
        docData.put(Constants.BD_FECHA_INGRESO, fechaIngreso);
        docData.put(Constants.BD_DOLAR, dolar);
        docData.put(Constants.BD_ORIGEN, origen);
        docData.put(Constants.BD_CAPITAL, capital);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (int j = mes; j < 12; j++) {
            final int finalJ = j;
            db.collection(Constants.BD_AHORROS).document(Auth.INSTANCE.uidCurrentUser())
                    .collection(year + "-" + j).document(String.valueOf(fechaIngreso.getTime()))
                    .set(docData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "DocumentSnapshot written succesfully");
                        if (finalJ == 11) {
                            progressBar.setVisibility(View.GONE);
                            requireActivity().finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getContext(), "Error al guardar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        etConceptoLayout.setEnabled(true);
                        etMontoLayout.setEnabled(true);
                        etOrigenLayout.setEnabled(true);
                        btnGuardar.setEnabled(true);
                    });
        }
    }
}
