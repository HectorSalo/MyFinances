package com.skysam.hchirinos.myfinances.agregar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.Utils.VariablesEstaticas;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AgregarAhorroFragment extends Fragment {

    private TextInputEditText etConcepto, etMonto, etOrigen;
    private TextInputLayout etConceptoLayout, etMontoLayout, etOrigenLayout;
    private String concepto;
    private double monto;
    private RadioButton rbBs, rbDolar;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ProgressBar progressBar;
    private Button btnGuardar;
    private boolean descontar;

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
        rbBs = view.findViewById(R.id.radioButton_bolivares);
        rbDolar = view.findViewById(R.id.radioButton_dolares);

        progressBar = view.findViewById(R.id.progressBar_agregar_ahorro);

        descontar = getArguments().getBoolean("descontar");
        if (descontar) {
            etOrigenLayout.setVisibility(View.GONE);
        }

        rbBs.setChecked(true);


        btnGuardar = view.findViewById(R.id.button_guardar);
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarDatos();
            }
        });
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
        Date fechaIngreso = calendar.getTime();
        boolean dolar = false;

        if (rbDolar.isChecked()) {
            dolar = true;
        } else if (rbBs.isChecked()) {
            dolar = false;
        }

        if(origen.isEmpty()) {
            origen = null;
        }

        Map<String, Object> docData = new HashMap<>();
        docData.put(VariablesEstaticas.BD_CONCEPTO, concepto);
        docData.put(VariablesEstaticas.BD_MONTO, monto);
        docData.put(VariablesEstaticas.BD_FECHA_INGRESO, fechaIngreso);
        docData.put(VariablesEstaticas.BD_DOLAR, dolar);
        docData.put(VariablesEstaticas.BD_ORIGEN, origen);
        docData.put(VariablesEstaticas.BD_DESCONTAR, descontar);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(VariablesEstaticas.BD_PROPIETARIOS).document(user.getUid()).collection(VariablesEstaticas.BD_AHORROS)
                .add(docData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        Objects.requireNonNull(getActivity()).finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getContext(), "Error al guardar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        etConceptoLayout.setEnabled(true);
                        etMontoLayout.setEnabled(true);
                        etOrigenLayout.setEnabled(true);
                        btnGuardar.setEnabled(true);
                    }
                });
    }
}
