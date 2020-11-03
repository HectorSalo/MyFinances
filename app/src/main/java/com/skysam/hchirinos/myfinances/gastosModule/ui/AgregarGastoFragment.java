package com.skysam.hchirinos.myfinances.gastosModule.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Constraints;
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


public class AgregarGastoFragment extends Fragment {

    private TextInputEditText etConcepto, etMonto;
    private TextInputLayout etConceptoLayout, etMontoLayout;
    private Spinner spFrecuencia, spinnerEscogerMes;
    private RadioButton rbBs, rbDolar, rbDias, rbSemanas, rbMeses, rbGastoFijo, rbGastoMes;
    private TextView tvFechaInicio, tvFechaFinal;
    private Date fechaSelecInicial, fechaSelecFinal;
    private FirebaseUser user;
    private ProgressBar progressBar;
    private Button btnGuardar;
    private ImageButton ibFechaInicial, ibFechaFinal;
    private int anualActual, mesSelecInicial, mesSelecFinal, cantidadItems;
    private Calendar calendarSelecInicial, calendarSelecFinal, calendarActual;
    private double monto;
    private String idLista, idItem;
    private boolean itemListGastos;

    public AgregarGastoFragment() {
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
        return inflater.inflate(R.layout.fragment_agregar_gasto, container, false);
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
        rbGastoFijo = view.findViewById(R.id.radioButton_gasto_fijo);
        rbGastoMes = view.findViewById(R.id.radioButton_gasto_mes);
        RadioGroup radioGasto = view.findViewById(R.id.radioGroup2);
        tvFechaInicio = view.findViewById(R.id.tv_fecha_inicial);
        tvFechaFinal = view.findViewById(R.id.tv_fecha_final);
        final LinearLayout linearLayoutFrecuencia = view.findViewById(R.id.linearLayout_frecuencia);
        final LinearLayout linearLayoutFecha = view.findViewById(R.id.linear_periodo);
        final LinearLayout linearLayoutEscogerMes = view.findViewById(R.id.linearLayout_escoger_mes);
        final RadioGroup radioGroupFrecuencia = view.findViewById(R.id.radioGroup_frecuencia);

        rbDolar.setChecked(true);
        rbDias.setChecked(true);

        progressBar = view.findViewById(R.id.progressBar_agregar_gasto);

        spFrecuencia = view.findViewById(R.id.spinner_frecuencia);

        List<String> listaFrecuencia = Arrays.asList(getResources().getStringArray(R.array.numero_frecuencia));
        ArrayAdapter<String> adapterFrecuencia = new ArrayAdapter<String>(requireContext(), R.layout.layout_spinner, listaFrecuencia);
        spFrecuencia.setAdapter(adapterFrecuencia);

        spinnerEscogerMes = view.findViewById(R.id.spinner_escoger_mes);

        List<String> listaEscogerMes = Arrays.asList(getResources().getStringArray(R.array.meses));
        ArrayAdapter<String> adapterEscogerMes = new ArrayAdapter<String>(requireContext(), R.layout.layout_spinner, listaEscogerMes);
        spinnerEscogerMes.setAdapter(adapterEscogerMes);
        spinnerEscogerMes.setSelection(mesActual);

        fechaSelecInicial = new Date();
        fechaSelecInicial = null;
        fechaSelecFinal = new Date();
        fechaSelecFinal = null;

        radioGasto.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton_gasto_fijo:
                        linearLayoutEscogerMes.setVisibility(View.GONE);
                        linearLayoutFecha.setVisibility(View.VISIBLE);
                        linearLayoutFrecuencia.setVisibility(View.VISIBLE);
                        radioGroupFrecuencia.setVisibility(View.VISIBLE);
                        break;
                    case R.id.radioButton_gasto_mes:
                        linearLayoutEscogerMes.setVisibility(View.VISIBLE);
                        linearLayoutFecha.setVisibility(View.GONE);
                        linearLayoutFrecuencia.setVisibility(View.GONE);
                        radioGroupFrecuencia.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        });

        btnGuardar = view.findViewById(R.id.button_first);
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarDatos();
            }
        });

        ibFechaInicial = view.findViewById(R.id.imageButton_inicial);
        ibFechaInicial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarFecha(true);
            }
        });

        ibFechaFinal = view.findViewById(R.id.imageButton_final);
        ibFechaFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crearDialogFechaFinal();
            }
        });

        if (getArguments() != null) {
            String concepto = getArguments().getString(Constantes.BD_CONCEPTO);
            double monto = getArguments().getDouble(Constantes.BD_MONTO);
            idLista = getArguments().getString("idLista");
            idItem = getArguments().getString("idItem");
            cantidadItems = getArguments().getInt("cantidadItems");

            linearLayoutEscogerMes.setVisibility(View.VISIBLE);
            linearLayoutFecha.setVisibility(View.GONE);
            linearLayoutFrecuencia.setVisibility(View.GONE);
            radioGroupFrecuencia.setVisibility(View.GONE);
            rbGastoMes.setChecked(true);
            etConcepto.setText(concepto);
            etMonto.setText(String.valueOf(monto));

            itemListGastos = true;
        } else {
            itemListGastos = false;
            rbGastoFijo.setChecked(true);
        }
    }


    private void validarDatos() {
        etConceptoLayout.setError(null);
        etMontoLayout.setError(null);
        String concepto = etConcepto.getText().toString();
        String montoS = etMonto.getText().toString();
        boolean conceptoValido;
        boolean montovalido;
        boolean fechaValida;

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
        if (rbGastoMes.isChecked()) {
            fechaValida = true;
            fechaSelecInicial = calendarActual.getTime();
        } else {
            if (fechaSelecInicial != null && fechaSelecFinal!= null) {
                fechaValida = true;
            } else {
                fechaValida = false;
                Toast.makeText(getContext(), "Debe seleccionar fecha de incio y final", Toast.LENGTH_SHORT).show();
            }
        }

        if (montovalido && conceptoValido && fechaValida) {
            if (rbGastoFijo.isChecked()) {
                guardarDatosFijos(concepto);
            } else {
                guardarDatosMes(concepto);
            }
        }
    }


    private void guardarDatosFijos(String concepto) {
        progressBar.setVisibility(View.VISIBLE);
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
        docData.put(Constantes.BD_FECHA_INCIAL, fechaSelecInicial);
        docData.put(Constantes.BD_FECHA_FINAL, fechaSelecFinal);
        docData.put(Constantes.BD_DOLAR, dolar);
        docData.put(Constantes.BD_DURACION_FRECUENCIA, duracionFrecuencia);
        docData.put(Constantes.BD_TIPO_FRECUENCIA, tipoFrecuencia);
        docData.put(Constantes.BD_MES_ACTIVO, true);

        for (int j = mesSelecInicial; j < (mesSelecFinal+1); j++) {
            final int finalJ = j;
            db.collection(Constantes.BD_GASTOS).document(user.getUid()).collection(anualActual + "-" + finalJ).document(String.valueOf(fechaSelecInicial.getTime()))
                    .set(docData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot written succesfully");
                            if (finalJ == mesSelecFinal) {
                                if (itemListGastos) {
                                    borrarItemListGastos();
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    requireActivity().finish();
                                }
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
                            ibFechaInicial.setEnabled(true);
                            ibFechaFinal.setEnabled(true);
                        }
                    });
        }
    }


    private void guardarDatosMes(String concepto) {
        progressBar.setVisibility(View.VISIBLE);
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
        docData.put(Constantes.BD_FECHA_INCIAL, fechaSelecInicial);
        docData.put(Constantes.BD_FECHA_FINAL, null);
        docData.put(Constantes.BD_DOLAR, dolar);
        docData.put(Constantes.BD_DURACION_FRECUENCIA, null);
        docData.put(Constantes.BD_TIPO_FRECUENCIA, null);
        docData.put(Constantes.BD_MES_ACTIVO, true);


            db.collection(Constantes.BD_GASTOS).document(user.getUid()).collection(anualActual + "-" + mesSelec).document(String.valueOf(fechaSelecInicial.getTime()))
                    .set(docData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot written succesfully");
                            if (itemListGastos) {
                                borrarItemListGastos();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                requireActivity().finish();
                            }
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
                            ibFechaInicial.setEnabled(true);
                            ibFechaFinal.setEnabled(true);
                        }
                    });

    }


    private void seleccionarFecha(final boolean inicial) {
        Calendar calendarMax = Calendar.getInstance();
        calendarMax.set(anualActual, 11, 31);
        Calendar calendarMin = Calendar.getInstance();
        calendarMin.set(anualActual, 0, 1);
        calendarSelecInicial = Calendar.getInstance();
        calendarSelecFinal = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if (inicial) {
                    calendarSelecInicial.set(anualActual, month, dayOfMonth);
                    mesSelecInicial = month;
                    fechaSelecInicial = calendarSelecInicial.getTime();
                    tvFechaInicio.setText(new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault()).format(fechaSelecInicial));
                } else {
                    calendarSelecFinal.set(anualActual, month, dayOfMonth);
                    mesSelecFinal = month;
                    fechaSelecFinal = calendarSelecFinal.getTime();
                    tvFechaFinal.setText(new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault()).format(fechaSelecFinal));
                }
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(calendarMax.getTimeInMillis());
        datePickerDialog.getDatePicker().setMinDate(calendarMin.getTimeInMillis());
        datePickerDialog.show();
    }

    private void crearDialogFechaFinal() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Seleccione")
                .setItems(R.array.opciones_fin_periodo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                calendarSelecFinal.set(anualActual, 11, 31);
                                mesSelecFinal = 11;
                                fechaSelecFinal = calendarSelecFinal.getTime();
                                tvFechaFinal.setText(new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault()).format(fechaSelecFinal));
                                break;
                            case 1:
                                seleccionarFecha(false);
                                break;
                        }
                    }
                }).show();
    }

    private void borrarItemListGastos() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constantes.BD_LISTA_GASTOS).document(user.getUid()).collection(idLista).document(idItem)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Delete", "DocumentSnapshot successfully deleted!");
                        actualizarCantidadItems();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Delete", "Error deleting document", e);
                        Toast.makeText(getContext(), "Error al borrar el item. Intente nuevamente.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarCantidadItems () {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constantes.BD_LISTA_GASTOS).document(user.getUid()).collection(Constantes.BD_TODAS_LISTAS).document(idLista)
                .update(Constantes.BD_CANTIDAD_ITEMS, (cantidadItems - 1))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(Constraints.TAG, "DocumentSnapshot successfully updated!");
                        Toast.makeText(getContext(), getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        requireActivity().finish();
                    }
                });
    }
}
