package com.skysam.hchirinos.myfinances.ingresosModule.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth;
import com.skysam.hchirinos.myfinances.common.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class EditarIngresoFragment extends Fragment {

    public EditarIngresoFragment() {}

    private String conceptoViejo, conceptoNuevo, idDoc;
    private double montoNuevo, montoViejo;
    private int duracionFrecuenciaViejo;
    private int yearSelected;
    private int mesSelected;
    private int mesFinal;
    private boolean mesUnico;
    private TextInputEditText etConcepto, etMonto;
    private TextInputLayout etConceptoLayout, etMontoLayout;
    private Spinner spFrecuencia;
    private RadioButton rbBs, rbDolar, rbDias, rbSemanas, rbMeses, rbEditAllMonth;
    private TextView tvFechaInicial, tvFechaFinal;
    private ProgressBar progressBar;
    private Button btnEditar;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_editar_ingreso, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etConcepto = view.findViewById(R.id.et_concepto_editar);
        etConceptoLayout = view.findViewById(R.id.outlined_concepto_editar);
        etMonto = view.findViewById(R.id.et_monto_editar);
        etMontoLayout = view.findViewById(R.id.outlined_monto_editar);
        rbBs = view.findViewById(R.id.radioButton_bolivares_editar);
        rbDolar = view.findViewById(R.id.radioButton_dolares_editar);
        rbDias = view.findViewById(R.id.radioButton_dias_editar);
        rbSemanas = view.findViewById(R.id.radioButton_semanas_editar);
        rbEditAllMonth = view.findViewById(R.id.rb_todos_meses);
        rbMeses = view.findViewById(R.id.radioButton_meses_editar);
        tvFechaInicial = view.findViewById(R.id.tv_fecha_inicial);
        tvFechaFinal = view.findViewById(R.id.tv_fecha_final);
        LinearLayout linearLayoutFrecuencia = view.findViewById(R.id.linearLayout2);
        LinearLayout linearLayoutFecha = view.findViewById(R.id.linear_periodo);
        RadioGroup radioGroupFrecuencia = view.findViewById(R.id.radioGroup3);
        RadioGroup radioGroupEditarMes = view.findViewById(R.id.radioGroup2);

        progressBar = view.findViewById(R.id.progressBar_editar_ingreso);

        spFrecuencia = view.findViewById(R.id.spinner_frecuencia_editar);

        List<String> listaFrecuencia = Arrays.asList(getResources().getStringArray(R.array.numero_frecuencia));
        ArrayAdapter<String> adapterFrecuencia = new ArrayAdapter<>(requireContext(), R.layout.layout_spinner, listaFrecuencia);
        spFrecuencia.setAdapter(adapterFrecuencia);

        idDoc = getArguments().getString("idDoc");
        mesSelected = getArguments().getInt("mes");
        yearSelected = getArguments().getInt("year");
        mesUnico = getArguments().getBoolean("mesUnico");

        btnEditar = view.findViewById(R.id.button_editar);
        btnEditar.setOnClickListener(v -> validarDatos());

        if (mesUnico) {
            linearLayoutFecha.setVisibility(View.GONE);
            linearLayoutFrecuencia.setVisibility(View.GONE);
            radioGroupFrecuencia.setVisibility(View.GONE);
            cargarItemUnico();
        } else {
            linearLayoutFecha.setVisibility(View.VISIBLE);
            linearLayoutFrecuencia.setVisibility(View.VISIBLE);
            radioGroupFrecuencia.setVisibility(View.VISIBLE);
            cargarItemPeriodico();
        }

        radioGroupEditarMes.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_mes) {
                linearLayoutFecha.setVisibility(View.GONE);
                linearLayoutFrecuencia.setVisibility(View.GONE);
                radioGroupFrecuencia.setVisibility(View.GONE);
            } else if (checkedId == R.id.rb_todos_meses) {
                linearLayoutFecha.setVisibility(View.VISIBLE);
                linearLayoutFrecuencia.setVisibility(View.VISIBLE);
                radioGroupFrecuencia.setVisibility(View.VISIBLE);
            }
        });
    }


    private void cargarItemPeriodico() {
        progressBar.setVisibility(View.VISIBLE);
        etConceptoLayout.setEnabled(false);
        etMontoLayout.setEnabled(false);
        btnEditar.setEnabled(false);

        db.collection(Constants.BD_INGRESOS).document(Auth.INSTANCE.uidCurrentUser())
                .collection(yearSelected + "-" + mesSelected).document(idDoc).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                            conceptoViejo = document.getString(Constants.BD_CONCEPTO);
                            etConcepto.setText(conceptoViejo);

                            montoViejo = document.getDouble(Constants.BD_MONTO);
                            String montoS = String.valueOf(montoViejo);
                            etMonto.setText(montoS);

                            boolean dolar = document.getBoolean(Constants.BD_DOLAR);
                            if (dolar) {
                                rbDolar.setChecked(true);
                            } else {
                                rbBs.setChecked(true);
                            }

                            double duracionFrecuenciaD = document.getDouble(Constants.BD_DURACION_FRECUENCIA);
                            duracionFrecuenciaViejo = (int) duracionFrecuenciaD;
                            spFrecuencia.setSelection(duracionFrecuenciaViejo - 1);

                            String tipoFrecuencia = document.getString(Constants.BD_TIPO_FRECUENCIA);
                            switch (tipoFrecuencia) {
                                case "Dias":
                                    rbDias.setChecked(true);
                                    break;
                                case "Semanas":
                                    rbSemanas.setChecked(true);
                                    break;
                                case "Meses":
                                    rbMeses.setChecked(true);
                                    break;
                            }

                            Date fechaInicial = document.getDate(Constants.BD_FECHA_INCIAL);
                            tvFechaInicial.setText(new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault()).format(fechaInicial));

                            Date fechaFinal = document.getDate(Constants.BD_FECHA_FINAL);
                            if (fechaFinal != null) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(fechaFinal);
                                mesFinal = calendar.get(Calendar.MONTH);
                                tvFechaFinal.setText(new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault()).format(fechaFinal));
                            } else {
                                mesFinal = 11;
                            }

                            progressBar.setVisibility(View.GONE);
                            etConceptoLayout.setEnabled(true);
                            etMontoLayout.setEnabled(true);
                            btnEditar.setEnabled(true);
                        } else {
                            Log.d(TAG, "No such document");
                            Toast.makeText(getContext(), getContext().getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            requireActivity().finish();
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                        Toast.makeText(getContext(), getContext().getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        requireActivity().finish();
                    }
                });
    }

    private void cargarItemUnico() {
        progressBar.setVisibility(View.VISIBLE);
        etConceptoLayout.setEnabled(false);
        etMontoLayout.setEnabled(false);
        btnEditar.setEnabled(false);

        db.collection(Constants.BD_INGRESOS).document(Auth.INSTANCE.uidCurrentUser())
                .collection(yearSelected + "-" + mesSelected).document(idDoc).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                            conceptoViejo = document.getString(Constants.BD_CONCEPTO);
                            etConcepto.setText(conceptoViejo);

                            montoViejo = document.getDouble(Constants.BD_MONTO);
                            String montoS = String.valueOf(montoViejo);
                            etMonto.setText(montoS);

                            boolean dolar = document.getBoolean(Constants.BD_DOLAR);
                            if (dolar) {
                                rbDolar.setChecked(true);
                            } else {
                                rbBs.setChecked(true);
                            }

                            progressBar.setVisibility(View.GONE);
                            etConceptoLayout.setEnabled(true);
                            etMontoLayout.setEnabled(true);
                            btnEditar.setEnabled(true);
                        } else {
                            Log.d(TAG, "No such document");
                            Toast.makeText(getContext(), getContext().getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            requireActivity().finish();
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                        Toast.makeText(getContext(), getContext().getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        requireActivity().finish();
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
            if (mesUnico) {
                actualizarItemUnico();
            } else {
                if (rbEditAllMonth.isChecked()) {
                    actualizarItemPeriodicoAllMonth();
                } else {
                    actualizarItemPeriodicoJustMonth();
                }
            }
        }

    }

    private void actualizarItemPeriodicoJustMonth() {
        progressBar.setVisibility(View.VISIBLE);
        etConceptoLayout.setEnabled(false);
        etMontoLayout.setEnabled(false);
        btnEditar.setEnabled(false);

        Map<String, Object> item = new HashMap<>();
        item.put(Constants.BD_DURACION_FRECUENCIA, duracionFrecuenciaViejo);

        if (!conceptoViejo.equals(conceptoNuevo)) {
            item.put(Constants.BD_CONCEPTO, conceptoNuevo);
        }
        if (montoNuevo != montoViejo) {
            item.put(Constants.BD_MONTO, montoNuevo);
        }
        item.put(Constants.BD_DOLAR, !rbBs.isChecked());
        if (rbDias.isChecked()) {
            item.put(Constants.BD_TIPO_FRECUENCIA, "Dias");
        }
        if (rbSemanas.isChecked()) {
            item.put(Constants.BD_TIPO_FRECUENCIA, "Semanas");
        }
        if (rbMeses.isChecked()) {
            item.put(Constants.BD_TIPO_FRECUENCIA, "Meses");
        }

        db.collection(Constants.BD_INGRESOS).document(Auth.INSTANCE.uidCurrentUser())
                .collection(yearSelected + "-" + mesSelected).document(idDoc)
                .update(item)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                    Toast.makeText(getContext(), getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating document", e);
                    Toast.makeText(getContext(), getString(R.string.error_guardar_data), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    etConceptoLayout.setEnabled(true);
                    etMontoLayout.setEnabled(true);
                    btnEditar.setEnabled(true);
                });
    }

    private void actualizarItemPeriodicoAllMonth() {
        progressBar.setVisibility(View.VISIBLE);
        etConceptoLayout.setEnabled(false);
        etMontoLayout.setEnabled(false);
        btnEditar.setEnabled(false);

        int duracionFrecuenciaNuevo = spFrecuencia.getSelectedItemPosition() + 1;
        Map<String, Object> item = new HashMap<>();

        if (!conceptoViejo.equals(conceptoNuevo)) {
            item.put(Constants.BD_CONCEPTO, conceptoNuevo);
        }
        if (montoNuevo != montoViejo) {
            item.put(Constants.BD_MONTO, montoNuevo);
        }
        if (rbBs.isChecked()) {
            item.put(Constants.BD_DOLAR, false);
        }
        if (rbDolar.isChecked()) {
            item.put(Constants.BD_DOLAR, true);
        }
        if (duracionFrecuenciaNuevo != duracionFrecuenciaViejo) {
            item.put(Constants.BD_DURACION_FRECUENCIA, duracionFrecuenciaNuevo);
        }
        if (rbDias.isChecked()) {
            item.put(Constants.BD_TIPO_FRECUENCIA, "Dias");
        }
        if (rbSemanas.isChecked()) {
            item.put(Constants.BD_TIPO_FRECUENCIA, "Semanas");
        }
        if (rbMeses.isChecked()) {
            item.put(Constants.BD_TIPO_FRECUENCIA, "Meses");
        }

        for (int i = mesSelected; i < (mesFinal+1); i++) {
            final int finalI = i;
            db.collection(Constants.BD_INGRESOS).document(Auth.INSTANCE.uidCurrentUser())
                    .collection(yearSelected + "-" + i).document(idDoc)
                    .update(item)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                        if (finalI == mesFinal) {
                            Toast.makeText(getContext(), getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            requireActivity().finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error updating document", e);
                        if (finalI > mesSelected) {
                            Toast.makeText(getContext(), getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            requireActivity().finish();
                        } else {
                            Toast.makeText(getContext(), getString(R.string.error_guardar_data), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            etConceptoLayout.setEnabled(true);
                            etMontoLayout.setEnabled(true);
                            btnEditar.setEnabled(true);
                        }
                    });
        }
    }

    private void actualizarItemUnico() {
        progressBar.setVisibility(View.VISIBLE);
        etConceptoLayout.setEnabled(false);
        etMontoLayout.setEnabled(false);
        btnEditar.setEnabled(false);

        Map<String, Object> item = new HashMap<>();

        if (!conceptoViejo.equals(conceptoNuevo)) {
            item.put(Constants.BD_CONCEPTO, conceptoNuevo);
        }
        if (montoNuevo != montoViejo) {
            item.put(Constants.BD_MONTO, montoNuevo);
        }
        if (rbBs.isChecked()) {
            item.put(Constants.BD_DOLAR, false);
        }
        if (rbDolar.isChecked()) {
            item.put(Constants.BD_DOLAR, true);
        }

        db.collection(Constants.BD_INGRESOS).document(Auth.INSTANCE.uidCurrentUser()).collection(yearSelected + "-" + mesSelected)
                .document(idDoc)
                .update(item)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                    Toast.makeText(getContext(), getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating document", e);
                    Toast.makeText(getContext(), getString(R.string.error_guardar_data), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    etConceptoLayout.setEnabled(true);
                    etMontoLayout.setEnabled(true);
                    btnEditar.setEnabled(true);
                });
    }
}
