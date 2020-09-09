package com.skysam.hchirinos.myfinances.ui.editar;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class EditarIngresoFragment extends Fragment {

    public EditarIngresoFragment() {}

    private String conceptoViejo, conceptoNuevo, idDoc;
    private double montoNuevo, montoViejo;
    private int duracionFrecuenciaViejo, duracionFrecuenciaNuevo, yearSelected, mesSelected;
    private boolean mesUnico;
    private TextInputEditText etConcepto, etMonto;
    private TextInputLayout etConceptoLayout, etMontoLayout;
    private Spinner spFrecuencia;
    private RadioButton rbBs, rbDolar, rbDias, rbSemanas, rbMeses;
    private TextView tvFecha;
    private Date fechaNueva, fechaVieja;
    private FirebaseUser user;
    private ProgressBar progressBar;
    private Button btnEditar;
    private ImageButton btnSelecFecha;
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
        LinearLayout linearLayoutFrecuencia = view.findViewById(R.id.linearLayout2);
        LinearLayout linearLayoutFecha = view.findViewById(R.id.linearLayout3);
        RadioGroup radioGroupFrecuencia = view.findViewById(R.id.radioGroup3);

        progressBar = view.findViewById(R.id.progressBar_editar_ingreso);

        spFrecuencia = view.findViewById(R.id.spinner_frecuencia_editar);

        List<String> listaFrecuencia = Arrays.asList(getResources().getStringArray(R.array.numero_frecuencia));
        ArrayAdapter<String> adapterFrecuencia = new ArrayAdapter<String>(requireContext(), R.layout.layout_spinner, listaFrecuencia);
        spFrecuencia.setAdapter(adapterFrecuencia);

        idDoc = getArguments().getString("idDoc");
        mesSelected = getArguments().getInt("mes");
        yearSelected = getArguments().getInt("year");
        mesUnico = getArguments().getBoolean("mesUnico");

        fechaNueva = new Date();
        fechaNueva = null;

        btnEditar = view.findViewById(R.id.button_editar);
        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarDatos();
            }
        });

        btnSelecFecha = view.findViewById(R.id.imageButton_editar);
        btnSelecFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarFecha();
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
        btnSelecFecha.setEnabled(false);

        db.collection(Constantes.BD_INGRESOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected).document(idDoc).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

                        fechaVieja = document.getDate(Constantes.BD_FECHA_INCIAL);
                        tvFecha.setText(new SimpleDateFormat("EEE d MMM yyyy").format(fechaVieja));

                        progressBar.setVisibility(View.GONE);
                        etConceptoLayout.setEnabled(true);
                        etMontoLayout.setEnabled(true);
                        btnEditar.setEnabled(true);
                        btnSelecFecha.setEnabled(true);
                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(getContext(), "Error al cargar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        etConceptoLayout.setEnabled(true);
                        etMontoLayout.setEnabled(true);
                        btnEditar.setEnabled(true);
                        btnSelecFecha.setEnabled(true);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Toast.makeText(getContext(), "Error al cargar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    etConceptoLayout.setEnabled(true);
                    etMontoLayout.setEnabled(true);
                    btnEditar.setEnabled(true);
                    btnSelecFecha.setEnabled(true);
                }
            }
        });
    }

    private void cargarItemUnico() {
        progressBar.setVisibility(View.VISIBLE);
        etConceptoLayout.setEnabled(false);
        etMontoLayout.setEnabled(false);
        btnEditar.setEnabled(false);

        db.collection(Constantes.BD_INGRESOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected).document(idDoc).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                    Toast.makeText(getContext(), "Error al cargar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    etConceptoLayout.setEnabled(true);
                    etMontoLayout.setEnabled(true);
                    btnEditar.setEnabled(true);
                }
            }
        });
    }


    private void seleccionarFecha() {
        final Calendar calendarSelec = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaVieja);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendarSelec.set(year, month, dayOfMonth);
                fechaNueva = calendarSelec.getTime();
                tvFecha.setText(new SimpleDateFormat("EEE d MMM yyyy").format(fechaNueva));
            }
        }, year, month, day);
        datePickerDialog.show();
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
        btnSelecFecha.setEnabled(false);

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
        if(fechaNueva != null) {
            if (!fechaNueva.equals(fechaVieja)) {
                item.put(Constantes.BD_FECHA_INCIAL, fechaNueva);
            }
        }

        for (int i = mesSelected; i < 12; i++) {
            final int finalI = i;
            db.collection(Constantes.BD_INGRESOS).document(user.getUid()).collection(yearSelected + "-" + i).document(idDoc)
                    .update(item)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                            if (finalI == 11) {
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
                            Toast.makeText(getContext(), getString(R.string.error_guardar_data), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            etConceptoLayout.setEnabled(true);
                            etMontoLayout.setEnabled(true);
                            btnEditar.setEnabled(true);
                            btnSelecFecha.setEnabled(true);
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

        db.collection(Constantes.BD_INGRESOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected).document(idDoc)
                .update(item)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                        Toast.makeText(getContext(), "Ítem modificado", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        requireActivity().finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                        Toast.makeText(getContext(), "Error al modificar. Intente nuevamente " + e, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        etConceptoLayout.setEnabled(true);
                        etMontoLayout.setEnabled(true);
                        btnEditar.setEnabled(true);
                    }
                });
    }
}
