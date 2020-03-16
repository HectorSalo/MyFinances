package com.skysam.hchirinos.myfinances.editar;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.errorprone.annotations.Var;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.Utils.VariablesEstaticas;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class EditarIngresoFragment extends Fragment {

    public EditarIngresoFragment() {}

    private String conceptoViejo, tipoFrecuenciaViejo, conceptoNuevo;
    private double montoNuevo, montoViejo;
    private int duracionFrecuenciaViejo, duracionFrecuenciaNuevo;
    private TextInputEditText etConcepto, etMonto;
    private TextInputLayout etConceptoLayout, etMontoLayout;
    private Spinner spFrecuencia;
    private RadioButton rbBs, rbDolar, rbDias, rbSemanas, rbMeses;
    private TextView tvFecha;
    private Date fechaSelec;
    private FirebaseUser user;
    private ProgressBar progressBar;
    private Button btnEditar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_editar_ingreso, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        etConcepto = view.findViewById(R.id.et_concepto_editar);
        etConceptoLayout = view.findViewById(R.id.outlined_concepto_editar);
        etMonto = view.findViewById(R.id.et_monto_editar);
        etMontoLayout = view.findViewById(R.id.outlined_monto_editar);
        rbBs = view.findViewById(R.id.radioButton_bolivares_editar);
        rbDolar = view.findViewById(R.id.radioButton_dolares_editar);
        rbDias = view.findViewById(R.id.radioButton_dias_editar);
        rbSemanas = view.findViewById(R.id.radioButton_semanas_editar);
        rbMeses = view.findViewById(R.id.radioButton_meses_editar);
        tvFecha = view.findViewById(R.id.textView_fecha_inicio_editar);

        progressBar = view.findViewById(R.id.progressBar_editar_ingreso);

        spFrecuencia = view.findViewById(R.id.spinner_frecuencia_editar);

        List<String> listaFrecuencia = Arrays.asList(getResources().getStringArray(R.array.numero_frecuencia));
        ArrayAdapter<String> adapterFrecuencia = new ArrayAdapter<String>(Objects.requireNonNull(getContext()), R.layout.layout_spinner, listaFrecuencia);
        spFrecuencia.setAdapter(adapterFrecuencia);

        String idDoc = getArguments().getString("idDoc");

        btnEditar = view.findViewById(R.id.button_editar);
        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarDatos();
            }
        });

        cargarItem(idDoc);

    }


    private void cargarItem(String id) {
        progressBar.setVisibility(View.VISIBLE);
        etConceptoLayout.setEnabled(false);
        etMontoLayout.setEnabled(false);
        btnEditar.setEnabled(false);
        db.collection(VariablesEstaticas.BD_PROPIETARIOS).document(user.getUid()).collection(VariablesEstaticas.BD_INGRESOS).document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        conceptoViejo = document.getString(VariablesEstaticas.BD_CONCEPTO);
                        etConcepto.setText(conceptoViejo);

                        montoViejo = document.getDouble(VariablesEstaticas.BD_MONTO);
                        String montoS = String.valueOf(montoViejo);
                        etMonto.setText(montoS);

                        boolean dolar = document.getBoolean(VariablesEstaticas.BD_DOLAR);
                        if (dolar) {
                            rbDolar.setChecked(true);
                        } else {
                            rbBs.setChecked(true);
                        }

                        double duracionFrecuenciaD = document.getDouble(VariablesEstaticas.BD_DURACION_FRECUENCIA);
                        duracionFrecuenciaViejo = (int) duracionFrecuenciaD;
                        spFrecuencia.setSelection(duracionFrecuenciaViejo - 1);

                        String tipoFrecuencia = document.getString(VariablesEstaticas.BD_TIPO_FRECUENCIA);
                        if (tipoFrecuencia.equals("Dias")) {
                            rbDias.setChecked(true);
                        } else if (tipoFrecuencia.equals("Semanas")) {
                            rbSemanas.setChecked(true);
                        } else if (tipoFrecuencia.equals("Meses")) {
                            rbMeses.setChecked(true);
                        }

                        Date fechaInicial = document.getDate(VariablesEstaticas.BD_FECHA_INCIAL);
                        tvFecha.setText(new SimpleDateFormat("EEE d MMM yyyy").format(fechaInicial));

                        progressBar.setVisibility(View.GONE);
                        etConceptoLayout.setEnabled(true);
                        etMontoLayout.setEnabled(true);
                        btnEditar.setEnabled(true);
                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(getContext(), "Error al cargar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        etConceptoLayout.setEnabled(true);
                        etMontoLayout.setEnabled(true);
                        btnEditar.setEnabled(true);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Toast.makeText(getContext(), "Error al cargar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    etConceptoLayout.setEnabled(true);
                    etMontoLayout.setEnabled(true);
                    btnEditar.setEnabled(true);
                }
            }
        });
    }


    private void validarDatos() {
        boolean conceptoValido;
        boolean montoValido;

        conceptoNuevo = etConcepto.getText().toString();
        String monto = etMonto.getText().toString();

        if (!conceptoNuevo.isEmpty()) {
            conceptoValido = true;
        } else {
            etConceptoLayout.setError("Campo obligatorio");
            conceptoValido = false;
        }

        if (!monto.isEmpty()) {
            montoNuevo = Double.parseDouble(monto);
            if (montoNuevo > 0) {
                montoValido = true;
            } else {
                etMontoLayout.setError("El monto debe ser mayor a cero");
                montoValido = false;
            }
        } else {
            etMontoLayout.setError("Campo obligatorio");
            montoValido = false;
        }

        if (montoValido && conceptoValido) {
            actualizarItem();
        }

    }

    private void actualizarItem() {
        duracionFrecuenciaNuevo = spFrecuencia.getSelectedItemPosition() + 1;
        Map<String, Object> item = new HashMap<>();

        if (!conceptoViejo.equals(conceptoNuevo)) {
            item.put(VariablesEstaticas.BD_CONCEPTO, conceptoNuevo);
        }
        if (montoNuevo != montoViejo) {
            item.put(VariablesEstaticas.BD_MONTO, montoNuevo);
        }
        if (rbBs.isChecked()) {
            item.put(VariablesEstaticas.BD_DOLAR, false);
        }
        if (rbDolar.isChecked()) {
            item.put(VariablesEstaticas.BD_DOLAR, true);
        }
        if (duracionFrecuenciaNuevo != duracionFrecuenciaViejo) {
            item.put(VariablesEstaticas.BD_DURACION_FRECUENCIA, duracionFrecuenciaNuevo);
        }
        if (rbDias.isChecked()) {
            item.put(VariablesEstaticas.BD_TIPO_FRECUENCIA, "Dias");
        }
        if (rbSemanas.isChecked()) {
            item.put(VariablesEstaticas.BD_TIPO_FRECUENCIA, "Semanas");
        }
        if (rbMeses.isChecked()) {
            item.put(VariablesEstaticas.BD_TIPO_FRECUENCIA, "Meses");
        }


    }
}
