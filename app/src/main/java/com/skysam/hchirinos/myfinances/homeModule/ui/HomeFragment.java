package com.skysam.hchirinos.myfinances.homeModule.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenter;
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenterClass;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class HomeFragment extends Fragment implements HomeView {

    private HomePresenter homePresenter;
    private PieChart pieBalance;
    private float montoIngresos, montoGastos, montoDeudas, montoPrestamos, montoAhorros;
    private ProgressBar progressBar;
    private TextView tvCotizacionDolar, tvSuperDeficit, tvMontoTotal, tvSuma;
    private SharedPreferences sharedPreferences;
    private float valorCotizacion;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Calendar calendar = Calendar.getInstance();
    private int mesSelected, mesItemAhorro, yearSelected;
    private LinearLayout linearLayout;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static final int INTERVALO = 2500;
    private long tiempoPrimerClick;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        homePresenter = new HomePresenterClass(this, requireContext());

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (tiempoPrimerClick + INTERVALO > System.currentTimeMillis()) {
                    requireActivity().finishAffinity();
                } else {
                    Toast.makeText(requireContext(), "Vuelve a presionar para salir", Toast.LENGTH_SHORT).show();
                }
                tiempoPrimerClick = System.currentTimeMillis();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        pieBalance = view.findViewById(R.id.pie_balance);
        progressBar = view.findViewById(R.id.progressBar_pie);
        tvCotizacionDolar = view.findViewById(R.id.textView_cotizacion_dolar);
        linearLayout = view.findViewById(R.id.linearLayout_resultado_balance);
        tvSuma = view.findViewById(R.id.textView_suma);
        tvSuperDeficit = view.findViewById(R.id.textView_deficit_superhabil);
        tvMontoTotal = view.findViewById(R.id.textView_monto_total);
        Spinner spinner = view.findViewById(R.id.spinner_meses);
        List<String> listaMeses = Arrays.asList(getResources().getStringArray(R.array.meses));
        ArrayAdapter<String> adapterMeses = new ArrayAdapter<String>(getContext(), R.layout.layout_spinner, listaMeses);
        spinner.setAdapter(adapterMeses);

        montoIngresos = 0;
        montoAhorros = 0;
        montoDeudas = 0;
        montoGastos = 0;
        montoPrestamos = 0;

        Calendar calendar = Calendar.getInstance();
        mesSelected = calendar.get(Calendar.MONTH);
        yearSelected = calendar.get(Calendar.YEAR);

        spinner.setSelection(mesSelected);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mesSelected = position;
                cargarIngresos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cargarFolios();

        return view;
    }

    private void cargarIngresos() {
        progressBar.setVisibility(View.VISIBLE);
        homePresenter.getIngresos(yearSelected, mesSelected);
    }


    private void cargarAhorros() {
        db.collection(Constants.BD_AHORROS).document(user.getUid()).collection(yearSelected + "-" + mesSelected)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double montototal = 0;
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Date date = document.getDate(Constants.BD_FECHA_INGRESO);
                                calendar.setTime(date);
                                mesItemAhorro = calendar.get(Calendar.MONTH);

                                if (mesSelected >= mesItemAhorro) {
                                    double montoDetal = document.getDouble(Constants.BD_MONTO);
                                    boolean dolar = document.getBoolean(Constants.BD_DOLAR);

                                        if (dolar) {
                                            montototal = montototal + montoDetal;
                                        } else {
                                            montototal = montototal + (montoDetal / valorCotizacion);
                                        }
                                }
                            }
                            montoAhorros = (float) montototal;
                            cargarPrestamos();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error getting data: ", e);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void cargarPrestamos() {
        db.collection(Constants.BD_PRESTAMOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double montototal = 0;
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                    double montoDetal = document.getDouble(Constants.BD_MONTO);
                                    boolean dolar = document.getBoolean(Constants.BD_DOLAR);

                                    if (dolar) {
                                        montototal = montototal + montoDetal;
                                    } else {
                                        montototal = montototal + (montoDetal / valorCotizacion);
                                    }

                            }
                            montoPrestamos = (float) montototal;
                            cargarGastos();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error getting data: ", e);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void cargarGastos() {
        db.collection(Constants.BD_GASTOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double montototal = 0;
                            int mesPago = 0;
                            int yearPago = 0;
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Boolean activo = document.getBoolean(Constants.BD_MES_ACTIVO);
                                if (activo == null || activo) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    double montoDetal = document.getDouble(Constants.BD_MONTO);
                                    boolean dolar = document.getBoolean(Constants.BD_DOLAR);

                                    String tipoFrecuencia = document.getString(Constants.BD_TIPO_FRECUENCIA);
                                    if (tipoFrecuencia != null) {
                                        Calendar calendarInicial = Calendar.getInstance();
                                        Calendar calendarPago = Calendar.getInstance();
                                        Date fechaInicial = document.getDate(Constants.BD_FECHA_INCIAL);
                                        double duracionFrecuencia = document.getDouble(Constants.BD_DURACION_FRECUENCIA);
                                        int duracionFrecuenciaInt = (int) duracionFrecuencia;

                                        int multiploCobranza = 0;

                                        calendarInicial.setTime(fechaInicial);
                                        mesPago = calendarInicial.get(Calendar.MONTH);
                                        yearPago = calendarInicial.get(Calendar.YEAR);

                                        if (mesPago == mesSelected) {
                                            multiploCobranza = 1;
                                        }


                                        if (tipoFrecuencia.equals("Dias")) {
                                            for (int j = 1; (mesPago <= mesSelected && yearPago == yearSelected); j++) {
                                                calendarInicial.add(Calendar.DAY_OF_YEAR, (duracionFrecuenciaInt * j));
                                                calendarPago.setTime(calendarInicial.getTime());
                                                mesPago = calendarPago.get(Calendar.MONTH);
                                                yearPago = calendarPago.get(Calendar.YEAR);
                                                calendarInicial.setTime(fechaInicial);

                                                if (mesPago == mesSelected) {
                                                    multiploCobranza = multiploCobranza + 1;
                                                }
                                            }
                                        } else if (tipoFrecuencia.equals("Semanas")) {
                                            for (int j = 1; mesPago <= mesSelected && yearPago == yearSelected; j++) {
                                                calendarInicial.add(Calendar.DAY_OF_YEAR, (duracionFrecuenciaInt * j * 7));
                                                calendarPago.setTime(calendarInicial.getTime());
                                                mesPago = calendarPago.get(Calendar.MONTH);
                                                yearPago = calendarPago.get(Calendar.YEAR);
                                                calendarInicial.setTime(fechaInicial);

                                                if (mesPago == mesSelected) {
                                                    multiploCobranza = multiploCobranza + 1;
                                                }
                                            }
                                        } else if (tipoFrecuencia.equals("Meses")) {
                                            for (int j = 1; mesPago <= mesSelected && yearPago == yearSelected; j++) {
                                                calendarInicial.add(Calendar.MONTH, (duracionFrecuenciaInt * j));
                                                calendarPago.setTime(calendarInicial.getTime());
                                                mesPago = calendarPago.get(Calendar.MONTH);
                                                yearPago = calendarPago.get(Calendar.YEAR);
                                                calendarInicial.setTime(fechaInicial);

                                                if (mesPago == mesSelected) {
                                                    multiploCobranza = multiploCobranza + 1;
                                                }
                                            }
                                        }
                                        if (dolar) {
                                            montototal = montototal + (montoDetal * multiploCobranza);
                                        } else {
                                            montototal = montototal + ((montoDetal / valorCotizacion) * multiploCobranza);
                                        }
                                    } else {
                                        if (dolar) {
                                            montototal = montototal + montoDetal;
                                        } else {
                                            montototal = montototal + (montoDetal / valorCotizacion);
                                        }
                                    }
                                }
                            }
                            montoGastos = (float) montototal;
                            cargarDeudas();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error getting data: ", e);
            }
        });
    }

    private void cargarDeudas() {
        db.collection(Constants.BD_DEUDAS).document(user.getUid()).collection(yearSelected + "-" + mesSelected)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double montototal = 0;
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                double montoDetal = document.getDouble(Constants.BD_MONTO);
                                boolean dolar = document.getBoolean(Constants.BD_DOLAR);

                                if (dolar) {
                                    montototal = montototal + montoDetal;
                                } else {
                                    montototal = montototal + (montoDetal / valorCotizacion);
                                }
                            }
                            montoDeudas = (float) montototal;
                            cargarFolios();
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error getting data: ", e);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void cargarFolios() {

        if (getContext() != null) {

            pieBalance.setDescription(null);
            pieBalance.setCenterText("Balance Mensual\n($)");
            pieBalance.setCenterTextSize(24);
            pieBalance.setDrawEntryLabels(false);
            pieBalance.setRotationEnabled(false);

            ArrayList<PieEntry> pieEntries = new ArrayList<>();
            pieEntries.add(new PieEntry(montoIngresos, requireContext().getString(R.string.pie_ingresos)));
            pieEntries.add(new PieEntry(montoAhorros, getContext().getString(R.string.pie_ahorros)));
            pieEntries.add(new PieEntry(montoPrestamos, getContext().getString(R.string.pie_prestamos)));
            pieEntries.add(new PieEntry(montoGastos, getContext().getString(R.string.pie_egresos)));
            pieEntries.add(new PieEntry(montoDeudas, getContext().getString(R.string.pie_deudas)));


            PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
            pieDataSet.setValueTextSize(18);
            pieDataSet.setColors(ContextCompat.getColor(requireContext(), R.color.md_green_300), ContextCompat.getColor(getContext(), R.color.md_green_700), ContextCompat.getColor(getContext(), R.color.md_light_green_A700),
                    ContextCompat.getColor(getContext(), R.color.md_red_400), ContextCompat.getColor(getContext(), R.color.md_red_900));
            pieDataSet.setFormSize(16);
            PieData pieData = new PieData(pieDataSet);

            pieBalance.setData(pieData);
            pieBalance.getLegend().setTextColor(ContextCompat.getColor(requireContext(), R.color.md_teal_700));
            pieBalance.invalidate();

            float montoTotal = montoIngresos + montoAhorros + montoPrestamos - montoGastos - montoDeudas;
            if (montoTotal > 0) {
                tvSuperDeficit.setText("Tiene un superávit de:");
                linearLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_green_300));
            } else if (montoTotal < 0) {
                tvSuperDeficit.setText("Tiene un déficit de:");
                linearLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_red_900));
            } else if (montoTotal == 0) {
                tvSuperDeficit.setText("Balance en cero");
                linearLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_green_300));
            }
            tvSuma.setText("Suma: " + montoIngresos + " + " + montoAhorros + " + " + montoPrestamos + " - " + montoGastos + " - " + montoDeudas);
            tvMontoTotal.setText("$" + montoTotal);

            if (mesSelected == mesItemAhorro) {
                if (sharedPreferences != null) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat("ahorros_disponible", montoAhorros);
                    editor.apply();
                }
            }
        }
    }


    private void actualizarCotizacion() {
        homePresenter.obtenerCotizacionWeb();
    }

    @Override
    public void valorCotizacionWebOk(@NotNull String valor, float valorFloat) {
        tvCotizacionDolar.setText(valor);
        homePresenter.guardarCotizacionShared(valorFloat);
        cargarIngresos();
    }

    @Override
    public void valorCotizacionWebError(float valorFloat) {
        tvCotizacionDolar.setText("Bs.S " + valorCotizacion);
        cargarIngresos();
    }


    @Override
    public void onResume() {
        super.onResume();
        actualizarCotizacion();
    }

    @Override
    public void statusValorIngresos(boolean statusOk, float ingresos, @NotNull String message) {
        if (statusOk) {
            montoIngresos = ingresos;
            cargarAhorros();
        } else {
            montoIngresos = ingresos;
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
