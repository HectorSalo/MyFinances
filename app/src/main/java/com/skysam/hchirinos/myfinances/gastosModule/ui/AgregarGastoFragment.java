package com.skysam.hchirinos.myfinances.gastosModule.ui;

import android.app.AlertDialog;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.common.utils.OnClickDatePicker;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class AgregarGastoFragment extends Fragment implements OnClickDatePicker {

    private TextInputEditText etConcepto, etMonto;
    private TextInputLayout etConceptoLayout, etMontoLayout;
    private Spinner spFrecuencia, spinnerEscogerMes;
    private RadioButton rbBs;
    private RadioButton rbDias;
    private RadioButton rbSemanas;
    private RadioButton rbMeses;
    private RadioButton rbGastoFijo;
    private RadioButton rbGastoMes;
    private TextView tvFechaInicio, tvFechaFinal;
    private Date fechaSelecInicial, fechaSelecFinal;
    private FirebaseUser user;
    private ProgressBar progressBar;
    private Button btnGuardar;
    private ImageButton ibFechaInicial, ibFechaFinal;
    private int anualActual, mesSelecInicial, mesSelecFinal, cantidadItems;
    private Calendar calendarActual;
    private double monto;
    private String idLista, idItem;
    private boolean itemListGastos;
    private boolean fechaIncial;

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
        RadioButton rbDolar = view.findViewById(R.id.radioButton_dolares);
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
        ArrayAdapter<String> adapterFrecuencia = new ArrayAdapter<>(requireContext(), R.layout.layout_spinner, listaFrecuencia);
        spFrecuencia.setAdapter(adapterFrecuencia);

        spinnerEscogerMes = view.findViewById(R.id.spinner_escoger_mes);

        List<String> listaEscogerMes = Arrays.asList(getResources().getStringArray(R.array.meses));
        ArrayAdapter<String> adapterEscogerMes = new ArrayAdapter<>(requireContext(), R.layout.layout_spinner, listaEscogerMes);
        spinnerEscogerMes.setAdapter(adapterEscogerMes);
        spinnerEscogerMes.setSelection(mesActual);

        fechaSelecInicial = new Date();
        fechaSelecInicial = null;
        fechaSelecFinal = new Date();
        fechaSelecFinal = null;

        radioGasto.setOnCheckedChangeListener((group, checkedId) -> {
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
        });

        btnGuardar = view.findViewById(R.id.button_first);
        btnGuardar.setOnClickListener(view1 -> validarDatos());

        ibFechaInicial = view.findViewById(R.id.imageButton_inicial);
        ibFechaInicial.setOnClickListener(v -> {
            fechaIncial = true;
            ClassesCommon.INSTANCE.selectDate(requireActivity().getSupportFragmentManager(), this);
        });

        ibFechaFinal = view.findViewById(R.id.imageButton_final);
        ibFechaFinal.setOnClickListener(view12 -> crearDialogFechaFinal());

        if (getArguments() != null) {
            String concepto = getArguments().getString(Constants.BD_CONCEPTO);
            double monto = getArguments().getDouble(Constants.BD_MONTO);
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
            progressBar.setVisibility(View.VISIBLE);
            etConceptoLayout.setEnabled(false);
            etMontoLayout.setEnabled(false);
            btnGuardar.setEnabled(false);
            ibFechaInicial.setEnabled(false);
            ibFechaFinal.setEnabled(false);
            if (rbGastoFijo.isChecked()) {
                guardarDatosFijos(concepto);
            } else {
                guardarDatosMes(concepto);
            }
        }
    }


    private void guardarDatosFijos(String concepto) {
        boolean dolar;
        String tipoFrecuencia = null;
        int duracionFrecuencia = spFrecuencia.getSelectedItemPosition() + 1;

        dolar = !rbBs.isChecked();

        if (rbDias.isChecked()) {
            tipoFrecuencia = "Dias";
        } else if (rbSemanas.isChecked()) {
            tipoFrecuencia = "Semanas";
        } else if (rbMeses.isChecked()) {
            tipoFrecuencia = "Meses";
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> docData = new HashMap<>();
        docData.put(Constants.BD_CONCEPTO, concepto);
        docData.put(Constants.BD_MONTO, monto);
        docData.put(Constants.BD_FECHA_INCIAL, fechaSelecInicial);
        docData.put(Constants.BD_FECHA_FINAL, fechaSelecFinal);
        docData.put(Constants.BD_DOLAR, dolar);
        docData.put(Constants.BD_DURACION_FRECUENCIA, duracionFrecuencia);
        docData.put(Constants.BD_TIPO_FRECUENCIA, tipoFrecuencia);
        docData.put(Constants.BD_MES_ACTIVO, true);

        for (int j = mesSelecInicial; j < (mesSelecFinal+1); j++) {
            final int finalJ = j;
            db.collection(Constants.BD_GASTOS).document(user.getUid()).collection(anualActual + "-" + finalJ).document(String.valueOf(fechaSelecInicial.getTime()))
                    .set(docData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "DocumentSnapshot written succesfully");
                        if (finalJ == mesSelecFinal) {
                            if (itemListGastos) {
                                borrarItemListGastos(true);
                            } else {
                                finalizarProceso();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getContext(), "Error al guardar en el mes " + (finalJ + 1) + ". Intente nuevamente", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        etConceptoLayout.setEnabled(true);
                        etMontoLayout.setEnabled(true);
                        btnGuardar.setEnabled(true);
                        ibFechaInicial.setEnabled(true);
                        ibFechaFinal.setEnabled(true);
                    });
        }
    }


    private void guardarDatosMes(String concepto) {
        boolean dolar;
        int mesSelec = spinnerEscogerMes.getSelectedItemPosition();

        dolar = !rbBs.isChecked();


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> docData = new HashMap<>();
        docData.put(Constants.BD_CONCEPTO, concepto);
        docData.put(Constants.BD_MONTO, monto);
        docData.put(Constants.BD_FECHA_INCIAL, fechaSelecInicial);
        docData.put(Constants.BD_FECHA_FINAL, null);
        docData.put(Constants.BD_DOLAR, dolar);
        docData.put(Constants.BD_DURACION_FRECUENCIA, null);
        docData.put(Constants.BD_TIPO_FRECUENCIA, null);
        docData.put(Constants.BD_MES_ACTIVO, true);


            db.collection(Constants.BD_GASTOS).document(user.getUid()).collection(anualActual + "-" + mesSelec).document(String.valueOf(fechaSelecInicial.getTime()))
                    .set(docData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "DocumentSnapshot written succesfully");
                        if (itemListGastos) {
                            borrarItemListGastos(false);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            requireActivity().finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getContext(), "Error al guardar en el mes. Intente nuevamente", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        etConceptoLayout.setEnabled(true);
                        etMontoLayout.setEnabled(true);
                        btnGuardar.setEnabled(true);
                        ibFechaInicial.setEnabled(true);
                        ibFechaFinal.setEnabled(true);
                    });

    }

    private void crearDialogFechaFinal() {
        Calendar calendar = Calendar.getInstance();
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Seleccione")
                .setItems(R.array.opciones_fin_periodo, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            calendar.set(anualActual, 11, 31);
                            mesSelecFinal = 11;
                            fechaSelecFinal = calendar.getTime();
                            tvFechaFinal.setText(new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault()).format(fechaSelecFinal));
                            break;
                        case 1:
                            fechaIncial = false;
                            ClassesCommon.INSTANCE.selectDate(requireActivity().getSupportFragmentManager(), this);
                            break;
                    }
                }).show();
    }

    private void borrarItemListGastos(boolean notification) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BD_LISTA_GASTOS).document(user.getUid()).collection(idLista).document(idItem)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Delete", "DocumentSnapshot successfully deleted!");
                    actualizarCantidadItems();
                })
                .addOnFailureListener(e -> {
                    Log.w("Delete", "Error deleting document", e);
                    Toast.makeText(getContext(), "Error al borrar el item. Intente nuevamente.", Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarCantidadItems () {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BD_LISTA_GASTOS).document(user.getUid()).collection(Constants.BD_TODAS_LISTAS).document(idLista)
                .update(Constants.BD_CANTIDAD_ITEMS, (cantidadItems - 1))
                .addOnSuccessListener(aVoid -> {
                    Log.d(Constraints.TAG, "DocumentSnapshot successfully updated!");
                    finalizarProceso();
                });
    }

    private void finalizarProceso() {
        Toast.makeText(getContext(), getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
        requireActivity().finish();
    }

    @Override
    public void date(@NotNull Calendar calendar) {
        if (fechaIncial) {
            fechaSelecInicial = calendar.getTime();
            mesSelecInicial = calendar.get(Calendar.MONTH);
            tvFechaInicio.setText(new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault()).format(fechaSelecInicial));
        } else {
            fechaSelecFinal = calendar.getTime();
            mesSelecFinal = calendar.get(Calendar.MONTH);
            tvFechaFinal.setText(new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault()).format(fechaSelecFinal));
        }
    }
}
