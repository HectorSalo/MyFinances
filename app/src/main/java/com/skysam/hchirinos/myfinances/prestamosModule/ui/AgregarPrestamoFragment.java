package com.skysam.hchirinos.myfinances.prestamosModule.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

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

public class AgregarPrestamoFragment extends Fragment {

    private TextInputEditText etDestinatario, etMonto;
    private TextInputLayout etDestinatarioLayout, etMontoLayout;
    private String destinatario;
    private double monto;
    private RadioButton rbDolar;
    private ProgressBar progressBar;
    private Button btnGuardar;

    public AgregarPrestamoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_agregar_prestamo, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etDestinatario = view.findViewById(R.id.et_destinatario);
        etDestinatarioLayout = view.findViewById(R.id.outlined_destinatario);
        etMonto = view.findViewById(R.id.et_monto);
        etMontoLayout = view.findViewById(R.id.outlined_monto);
        rbDolar = view.findViewById(R.id.radioButton_dolares);

        progressBar = view.findViewById(R.id.progressBar_agregar_prestamo);

        rbDolar.setChecked(true);


        btnGuardar = view.findViewById(R.id.button_guardar);
        btnGuardar.setOnClickListener(view1 -> validarDatos());
    }

    private void validarDatos() {
        etDestinatarioLayout.setError(null);
        etMontoLayout.setError(null);
        destinatario = etDestinatario.getText().toString();
        String montoS = etMonto.getText().toString();
        boolean destinatarioValido;
        boolean montovalido;

        if (!destinatario.isEmpty()) {
            destinatarioValido = true;
        } else {
            destinatarioValido = false;
            etDestinatarioLayout.setError("Campo obligatorio");
        }
        if (!montoS.isEmpty()) {
            monto = Double.parseDouble(montoS);
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


        if (montovalido && destinatarioValido) {
            guardarDatos();
        }
    }


    private void guardarDatos() {
        progressBar.setVisibility(View.VISIBLE);
        etMontoLayout.setEnabled(false);
        etDestinatarioLayout.setEnabled(false);
        btnGuardar.setEnabled(false);
        Calendar calendar = Calendar.getInstance();
        int mes = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        Date fechaIngreso = calendar.getTime();
        boolean dolar;

        dolar = rbDolar.isChecked();

        Map<String, Object> docData = new HashMap<>();
        docData.put(Constants.BD_CONCEPTO, destinatario);
        docData.put(Constants.BD_MONTO, monto);
        docData.put(Constants.BD_FECHA_INGRESO, fechaIngreso);
        docData.put(Constants.BD_DOLAR, dolar);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (int j = mes; j < 12; j++) {
            final int finalJ = j;
            db.collection(Constants.BD_PRESTAMOS).document(Auth.INSTANCE.uidCurrentUser())
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
                        etDestinatarioLayout.setEnabled(true);
                        etMontoLayout.setEnabled(true);
                        btnGuardar.setEnabled(true);
                    });
        }
    }
}
