package com.skysam.hchirinos.myfinances.ui.agregar;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.skysam.hchirinos.myfinances.Utils.Constantes;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AgregarIngresoFragment extends Fragment {

    private TextInputEditText etConcepto, etMonto;
    private TextInputLayout etConceptoLayout, etMontoLayout;
    private Spinner spFrecuencia, spinnerEscogerMes;
    private RadioButton rbBs, rbDolar, rbDias, rbSemanas, rbMeses, rbIngresoFijo, rbIngresoMes;
    private TextView tvFecha;
    private Date fechaSelec;
    private FirebaseUser user;
    private ProgressBar progressBar;
    private Button btnGuardar;
    private ImageButton imageButtonSelecFecha;
    private int mesSelec, anualSelec, anualActual;
    private Calendar calendarSelec, calendarActual;

    public AgregarIngresoFragment() {}

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_agregar_ingreso, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        calendarActual = Calendar.getInstance();
        int mesActual = calendarActual.get(Calendar.MONTH);
        anualActual = calendarActual.get(Calendar.YEAR);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        etConcepto = view.findViewById(R.id.et_concepto);
        etConceptoLayout = view.findViewById(R.id.outlined_concepto);
        etMonto = view.findViewById(R.id.et_monto);
        etMontoLayout = view.findViewById(R.id.outlined_monto);
        rbBs = view.findViewById(R.id.radioButton_bolivares);
        rbDolar = view.findViewById(R.id.radioButton_dolares);
        rbDias = view.findViewById(R.id.radioButton_dias);
        rbSemanas = view.findViewById(R.id.radioButton_semanas);
        rbMeses = view.findViewById(R.id.radioButton_meses);
        tvFecha = view.findViewById(R.id.textView_fecha_inicio);
        rbIngresoFijo = view.findViewById(R.id.radioButton_ingreso_fijo);
        rbIngresoMes = view.findViewById(R.id.radioButton_ingreso_mes);
        RadioGroup radioIngreso = view.findViewById(R.id.radioGroup2);
        final LinearLayout linearLayoutFrecuencia = view.findViewById(R.id.linearLayout_frecuencia);
        final LinearLayout linearLayoutFecha = view.findViewById(R.id.linearLayout_fecha);
        final LinearLayout linearLayoutEscogerMes = view.findViewById(R.id.linearLayout_escoger_mes);
        final RadioGroup radioGroupFrecuencia = view.findViewById(R.id.radioGroup_frecuencia);

        rbDolar.setChecked(true);
        rbIngresoFijo.setChecked(true);
        rbDias.setChecked(true);

        progressBar = view.findViewById(R.id.progressBar_agregar_ingreso);

        spFrecuencia = view.findViewById(R.id.spinner_frecuencia);

        List<String> listaFrecuencia = Arrays.asList(getResources().getStringArray(R.array.numero_frecuencia));
        ArrayAdapter<String> adapterFrecuencia = new ArrayAdapter<String>(requireContext(), R.layout.layout_spinner, listaFrecuencia);
        spFrecuencia.setAdapter(adapterFrecuencia);

        spinnerEscogerMes = view.findViewById(R.id.spinner_escoger_mes);

        List<String> listaEscogerMes = Arrays.asList(getResources().getStringArray(R.array.meses));
        ArrayAdapter<String> adapterEscogerMes = new ArrayAdapter<String>(requireContext(), R.layout.layout_spinner, listaEscogerMes);
        spinnerEscogerMes.setAdapter(adapterEscogerMes);
        spinnerEscogerMes.setSelection(mesActual);

        radioIngreso.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton_ingreso_fijo:
                        linearLayoutEscogerMes.setVisibility(View.GONE);
                        linearLayoutFecha.setVisibility(View.VISIBLE);
                        linearLayoutFrecuencia.setVisibility(View.VISIBLE);
                        radioGroupFrecuencia.setVisibility(View.VISIBLE);
                        tvFecha.setVisibility(View.VISIBLE);
                        break;
                    case R.id.radioButton_ingreso_mes:
                        linearLayoutEscogerMes.setVisibility(View.VISIBLE);
                        linearLayoutFecha.setVisibility(View.GONE);
                        linearLayoutFrecuencia.setVisibility(View.GONE);
                        radioGroupFrecuencia.setVisibility(View.GONE);
                        tvFecha.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        });

        fechaSelec = new Date();
        fechaSelec = null;

        btnGuardar = view.findViewById(R.id.button_first);
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarDatos();
            }
        });

        imageButtonSelecFecha = view.findViewById(R.id.imageButton);
        imageButtonSelecFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarFecha();
            }
        });
    }


    private void validarDatos() {
        String concepto = etConcepto.getText().toString();

        if (!concepto.isEmpty()) {
            if (!(etMonto.getText().toString().isEmpty())) {
                double monto = Double.parseDouble(etMonto.getText().toString());
                if (monto > 0) {
                    if (rbIngresoMes.isChecked()) {
                        etConceptoLayout.setError(null);
                        etMontoLayout.setError(null);
                        progressBar.setVisibility(View.VISIBLE);
                        etConceptoLayout.setEnabled(false);
                        etMontoLayout.setEnabled(false);
                        btnGuardar.setEnabled(false);
                        fechaSelec = calendarActual.getTime();
                        guardarDatosMes(concepto, monto, fechaSelec);
                    } else {
                        if (fechaSelec != null) {
                            etConceptoLayout.setError(null);
                            etMontoLayout.setError(null);
                            progressBar.setVisibility(View.VISIBLE);
                            etConceptoLayout.setEnabled(false);
                            etMontoLayout.setEnabled(false);
                            btnGuardar.setEnabled(false);
                            imageButtonSelecFecha.setEnabled(false);
                            guardarDatosFijos(concepto, monto, fechaSelec);
                        } else {
                            Toast.makeText(getContext(), "Debe seleccionar fecha de incio", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    etMontoLayout.setError("El monto debe ser mayor a cero");
                }
            } else {
                etMontoLayout.setError("Debe ingresar un monto");
            }
        } else {
            etConceptoLayout.setError("Debe ingresar un Concepto");
        }


    }


    private void guardarDatosFijos(String concepto, double monto, Date fechaSelec) {
        boolean dolar = false;
        String tipoFrecuencia = null;
        int duracionFrecuencia = spFrecuencia.getSelectedItemPosition() + 1;

        if (rbBs.isChecked()) {
            dolar = false;
        } else if (rbDolar.isChecked()) {
            dolar = true;
        }

        if (rbDias.isChecked()) {
            tipoFrecuencia = "Dias";
        } else if (rbSemanas.isChecked()) {
            tipoFrecuencia = "Semanas";
        } else if (rbMeses.isChecked()) {
            tipoFrecuencia = "Meses";
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> docData = new HashMap<>();
        docData.put(Constantes.BD_CONCEPTO, concepto);
        docData.put(Constantes.BD_MONTO, monto);
        docData.put(Constantes.BD_FECHA_INCIAL, fechaSelec);
        docData.put(Constantes.BD_DOLAR, dolar);
        docData.put(Constantes.BD_DURACION_FRECUENCIA, duracionFrecuencia);
        docData.put(Constantes.BD_TIPO_FRECUENCIA, tipoFrecuencia);
        docData.put(Constantes.BD_MES_ACTIVO, true);

        for (int j = mesSelec; j < 12; j++) {
            final int finalJ = j;
            db.collection(Constantes.BD_INGRESOS).document(user.getUid()).collection(anualSelec + "-" + finalJ).document(String.valueOf(fechaSelec.getTime()))
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
                            Toast.makeText(getContext(), "Error al guardar en el mes " + (finalJ + 1) + ". Intente nuevamente", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            etConceptoLayout.setEnabled(true);
                            etMontoLayout.setEnabled(true);
                            btnGuardar.setEnabled(true);
                            imageButtonSelecFecha.setEnabled(true);
                        }
                    });
        }
    }

    private void guardarDatosMes(String concepto, double monto, Date fechaSelec) {
        boolean dolar;
        int mesSelec = spinnerEscogerMes.getSelectedItemPosition();

        if (rbBs.isChecked()) {
            dolar = false;
        } else {
            dolar = true;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> docData = new HashMap<>();
        docData.put(Constantes.BD_CONCEPTO, concepto);
        docData.put(Constantes.BD_MONTO, monto);
        docData.put(Constantes.BD_FECHA_INCIAL, fechaSelec);
        docData.put(Constantes.BD_DOLAR, dolar);
        docData.put(Constantes.BD_DURACION_FRECUENCIA, null);
        docData.put(Constantes.BD_TIPO_FRECUENCIA, null);
        docData.put(Constantes.BD_MES_ACTIVO, true);

        db.collection(Constantes.BD_INGRESOS).document(user.getUid()).collection(anualActual + "-" + mesSelec).document(String.valueOf(fechaSelec.getTime()))
                .set(docData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot written succesfully");
                        progressBar.setVisibility(View.GONE);
                        requireActivity().finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getContext(), "Error al guardar en el mes. Intente nuevamente", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        etConceptoLayout.setEnabled(true);
                        etMontoLayout.setEnabled(true);
                        btnGuardar.setEnabled(true);
                        imageButtonSelecFecha.setEnabled(true);
                    }
                });
    }


    private void seleccionarFecha() {
        calendarSelec = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendarSelec.set(year, month, dayOfMonth);
                mesSelec = month;
                anualSelec = year;
                fechaSelec = calendarSelec.getTime();
                tvFecha.setText(new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault()).format(fechaSelec));
            }
        }, year, month, day);
        datePickerDialog.show();
    }
}
