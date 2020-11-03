package com.skysam.hchirinos.myfinances.gastosModule.ui;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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


public class EditarGastoFragment extends Fragment {

    public EditarGastoFragment() {
        // Required empty public constructor
    }

    private String conceptoViejo, conceptoNuevo, idDoc;
    private double montoNuevo, montoViejo;
    private int duracionFrecuenciaViejo, duracionFrecuenciaNuevo, yearSelected, mesSelected, mesFinal;
    private boolean mesUnico;
    private TextInputEditText etConcepto, etMonto;
    private TextInputLayout etConceptoLayout, etMontoLayout;
    private Spinner spFrecuencia;
    private RadioButton rbBs, rbDolar, rbDias, rbSemanas, rbMeses;
    private TextView tvFechaInicial, tvFechaFinal;
    private FirebaseUser user;
    private ProgressBar progressBar;
    private Button btnEditar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_editar_gasto, container, false);
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        tvFechaInicial = view.findViewById(R.id.tv_fecha_inicial);
        tvFechaFinal = view.findViewById(R.id.tv_fecha_final);
        LinearLayout linearLayoutFrecuencia = view.findViewById(R.id.linearLayout2);
        LinearLayout linearLayoutFecha = view.findViewById(R.id.linear_periodo);
        RadioGroup radioGroupFrecuencia = view.findViewById(R.id.radioGroup3);

        progressBar = view.findViewById(R.id.progressBar);

        idDoc = getArguments().getString("idDoc");
        mesSelected = getArguments().getInt("mes");
        yearSelected = getArguments().getInt("year");
        mesUnico = getArguments().getBoolean("mesUnico");

        spFrecuencia = view.findViewById(R.id.spinner_frecuencia);

        List<String> listaFrecuencia = Arrays.asList(getResources().getStringArray(R.array.numero_frecuencia));
        ArrayAdapter<String> adapterFrecuencia = new ArrayAdapter<String>(requireContext(), R.layout.layout_spinner, listaFrecuencia);
        spFrecuencia.setAdapter(adapterFrecuencia);

        btnEditar = view.findViewById(R.id.button_editar);
        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarDatos();
            }
        });

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



    }


    private void cargarItemPeriodico() {
        progressBar.setVisibility(View.VISIBLE);
        etConceptoLayout.setEnabled(false);
        etMontoLayout.setEnabled(false);
        btnEditar.setEnabled(false);

        db.collection(Constantes.BD_GASTOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected).document(idDoc).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        conceptoViejo = document.getString(Constantes.BD_CONCEPTO);
                        etConcepto.setText(conceptoViejo);

                        montoViejo = document.getDouble(Constantes.BD_MONTO);
                        String montoS = String.valueOf(montoViejo);
                        etMonto.setText(montoS);

                        boolean dolar = document.getBoolean(Constantes.BD_DOLAR);
                        if (dolar) {
                            rbDolar.setChecked(true);
                        } else {
                            rbBs.setChecked(true);
                        }

                        double duracionFrecuenciaD = document.getDouble(Constantes.BD_DURACION_FRECUENCIA);
                        duracionFrecuenciaViejo = (int) duracionFrecuenciaD;
                        spFrecuencia.setSelection(duracionFrecuenciaViejo - 1);

                        String tipoFrecuencia = document.getString(Constantes.BD_TIPO_FRECUENCIA);
                        if (tipoFrecuencia.equals("Dias")) {
                            rbDias.setChecked(true);
                        } else if (tipoFrecuencia.equals("Semanas")) {
                            rbSemanas.setChecked(true);
                        } else if (tipoFrecuencia.equals("Meses")) {
                            rbMeses.setChecked(true);
                        }

                        Date fechaInicial = document.getDate(Constantes.BD_FECHA_INCIAL);
                        tvFechaInicial.setText(new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault()).format(fechaInicial));

                        Date fechaFinal = document.getDate(Constantes.BD_FECHA_FINAL);
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
                        Toast.makeText(getContext(), "Error al cargar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        etConceptoLayout.setEnabled(true);
                        etMontoLayout.setEnabled(true);
                        btnEditar.setEnabled(true);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Toast.makeText(getContext(), getContext().getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    requireActivity().finish();
                }
            }
        });
    }

    private void cargarItemUnico() {
        progressBar.setVisibility(View.VISIBLE);
        etConceptoLayout.setEnabled(false);
        etMontoLayout.setEnabled(false);
        btnEditar.setEnabled(false);

        db.collection(Constantes.BD_GASTOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected).document(idDoc).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        conceptoViejo = document.getString(Constantes.BD_CONCEPTO);
                        etConcepto.setText(conceptoViejo);

                        montoViejo = document.getDouble(Constantes.BD_MONTO);
                        String montoS = String.valueOf(montoViejo);
                        etMonto.setText(montoS);

                        boolean dolar = document.getBoolean(Constantes.BD_DOLAR);
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
                        Toast.makeText(getContext(), "Error al cargar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        etConceptoLayout.setEnabled(true);
                        etMontoLayout.setEnabled(true);
                        btnEditar.setEnabled(true);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Toast.makeText(getContext(), getContext().getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    requireActivity().finish();
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
            if (mesUnico) {
                actualizarItemUnico();
            } else {
                actualizarItemPeriodico();
            }
        }

    }


    private void actualizarItemPeriodico() {
        progressBar.setVisibility(View.VISIBLE);
        etConceptoLayout.setEnabled(false);
        etMontoLayout.setEnabled(false);
        btnEditar.setEnabled(false);

        duracionFrecuenciaNuevo = spFrecuencia.getSelectedItemPosition() + 1;
        Map<String, Object> item = new HashMap<>();

        if (!conceptoViejo.equals(conceptoNuevo)) {
            item.put(Constantes.BD_CONCEPTO, conceptoNuevo);
        }
        if (montoNuevo != montoViejo) {
            item.put(Constantes.BD_MONTO, montoNuevo);
        }
        if (rbBs.isChecked()) {
            item.put(Constantes.BD_DOLAR, false);
        }
        if (rbDolar.isChecked()) {
            item.put(Constantes.BD_DOLAR, true);
        }
        if (duracionFrecuenciaNuevo != duracionFrecuenciaViejo) {
            item.put(Constantes.BD_DURACION_FRECUENCIA, duracionFrecuenciaNuevo);
        }
        if (rbDias.isChecked()) {
            item.put(Constantes.BD_TIPO_FRECUENCIA, "Dias");
        }
        if (rbSemanas.isChecked()) {
            item.put(Constantes.BD_TIPO_FRECUENCIA, "Semanas");
        }
        if (rbMeses.isChecked()) {
            item.put(Constantes.BD_TIPO_FRECUENCIA, "Meses");
        }

        for (int i = mesSelected; i < (mesFinal+1); i++) {
            final int finalI = i;
            db.collection(Constantes.BD_GASTOS).document(user.getUid()).collection(yearSelected + "-" + i).document(idDoc)
                    .update(item)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                            if (finalI == mesFinal) {
                                Toast.makeText(getContext(), getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                requireActivity().finish();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
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
            item.put(Constantes.BD_CONCEPTO, conceptoNuevo);
        }
        if (montoNuevo != montoViejo) {
            item.put(Constantes.BD_MONTO, montoNuevo);
        }
        if (rbBs.isChecked()) {
            item.put(Constantes.BD_DOLAR, false);
        }
        if (rbDolar.isChecked()) {
            item.put(Constantes.BD_DOLAR, true);
        }

            db.collection(Constantes.BD_GASTOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected).document(idDoc)
                    .update(item)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                            Toast.makeText(getContext(), getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            requireActivity().finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                            Toast.makeText(getContext(), "Error al modificar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            etConceptoLayout.setEnabled(true);
                            etMontoLayout.setEnabled(true);
                            btnEditar.setEnabled(true);
                        }
                    });

    }
}
