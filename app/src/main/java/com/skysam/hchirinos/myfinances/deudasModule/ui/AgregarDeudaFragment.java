package com.skysam.hchirinos.myfinances.deudasModule.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.Constants;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class AgregarDeudaFragment extends Fragment {

    public AgregarDeudaFragment() {
        // Required empty public constructor
    }

    private TextInputEditText etPrestamista, etMonto, etConcepto;
    private TextInputLayout etPrestamistaLayout, etMontoLayout, etConceptoLayout;
    private String prestamista, concepto;
    private double monto;
    private RadioButton rbBs, rbDolar;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ProgressBar progressBar;
    private Button btnGuardar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_agregar_deuda, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etPrestamista = view.findViewById(R.id.et_prestamista);
        etPrestamistaLayout = view.findViewById(R.id.outlined_prestamista);
        etMonto = view.findViewById(R.id.et_monto);
        etMontoLayout = view.findViewById(R.id.outlined_monto);
        etConcepto = view.findViewById(R.id.et_concepto);
        etConceptoLayout = view.findViewById(R.id.outlined_concepto);
        rbBs = view.findViewById(R.id.radioButton_bolivares);
        rbDolar = view.findViewById(R.id.radioButton_dolares);

        progressBar = view.findViewById(R.id.progressBar);

        rbDolar.setChecked(true);


        btnGuardar = view.findViewById(R.id.button_guardar);
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarDatos();
            }
        });
    }


    private void validarDatos() {
        etPrestamistaLayout.setError(null);
        etMontoLayout.setError(null);
        etConceptoLayout.setError(null);
        prestamista = etPrestamista.getText().toString();
        concepto = etConcepto.getText().toString();
        String montoS = etMonto.getText().toString();
        boolean destinatarioValido;
        boolean conceptoValido;
        boolean montovalido = false;

        if (!prestamista.isEmpty()) {
            destinatarioValido = true;
        } else {
            destinatarioValido = false;
            etPrestamistaLayout.setError("Campo obligatorio");
        }
        if (!concepto.isEmpty()) {
            conceptoValido = true;
        } else {
            conceptoValido = false;
            etConceptoLayout.setError("Campo obligatorio");
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


        if (montovalido && destinatarioValido && conceptoValido) {
            guardarDatos();
        }
    }


    private void guardarDatos() {
        progressBar.setVisibility(View.VISIBLE);
        etMontoLayout.setEnabled(false);
        etPrestamistaLayout.setEnabled(false);
        etConceptoLayout.setEnabled(false);
        btnGuardar.setEnabled(false);
        Calendar calendar = Calendar.getInstance();
        int mes = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        Date fechaIngreso = calendar.getTime();
        boolean dolar = false;

        if (rbDolar.isChecked()) {
            dolar = true;
        } else if (rbBs.isChecked()) {
            dolar = false;
        }

        Map<String, Object> docData = new HashMap<>();
        docData.put(Constants.BD_PRESTAMISTA, prestamista);
        docData.put(Constants.BD_CONCEPTO, concepto);
        docData.put(Constants.BD_MONTO, monto);
        docData.put(Constants.BD_FECHA_INGRESO, fechaIngreso);
        docData.put(Constants.BD_DOLAR, dolar);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (int j = mes; j < 12; j++) {
            final int finalJ = j;
            db.collection(Constants.BD_DEUDAS).document(user.getUid()).collection(year + "-" + j).document(String.valueOf(fechaIngreso.getTime()))
                    .set(docData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot written succesfully");
                            if (finalJ == 11) {
                                progressBar.setVisibility(View.GONE);
                                requireActivity().finish();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                            Toast.makeText(getContext(), "Error al guardar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            etPrestamistaLayout.setEnabled(true);
                            etConceptoLayout.setEnabled(true);
                            etMontoLayout.setEnabled(true);
                            btnGuardar.setEnabled(true);
                        }
                    });
        }
    }
}
