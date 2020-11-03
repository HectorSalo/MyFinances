package com.skysam.hchirinos.myfinances.homeModule.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.skysam.hchirinos.myfinances.Utils.Constantes;

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


public class HomeFragment extends Fragment {

    private PieChart pieBalance;
    private float montoIngresos, montoGastos, montoDeudas, montoPrestamos, montoAhorros;
    private ProgressBar progressBar;
    private TextView tvCotizacionDolar, tvSuperDeficit, tvMontoTotal, tvSuma;
    private SharedPreferences sharedPreferences;
    private float valorCotizacion;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Calendar calendar = Calendar.getInstance();
    private int mesSelected, mesItemAhorro, yearSelected;
    private String valor;
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

        moveToNextYear();

        cargarFolios();

        return view;
    }

    private void cargarIngresos() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection(Constantes.BD_INGRESOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double montototal = 0;
                            int mesCobro;
                            int yearCobro;

                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Boolean activo = document.getBoolean(Constantes.BD_MES_ACTIVO);
                                if (activo == null || activo) {
                                    Calendar calendarInicial = Calendar.getInstance();
                                    double montoDetal = document.getDouble(Constantes.BD_MONTO);
                                    boolean dolar = document.getBoolean(Constantes.BD_DOLAR);
                                    String tipoFrecuencia = document.getString(Constantes.BD_TIPO_FRECUENCIA);
                                    if (tipoFrecuencia != null) {
                                        Date fechaInicial = document.getDate(Constantes.BD_FECHA_INCIAL);
                                        double duracionFrecuencia = document.getDouble(Constantes.BD_DURACION_FRECUENCIA);
                                        int duracionFrecuenciaInt = (int) duracionFrecuencia;

                                        int multiploIngreso = 0;

                                        calendarInicial.setTime(fechaInicial);
                                        mesCobro = calendarInicial.get(Calendar.MONTH);
                                        yearCobro = calendarInicial.get(Calendar.YEAR);

                                        if (mesCobro == mesSelected) {
                                            multiploIngreso = 1;
                                        }

                                        if (tipoFrecuencia.equals("Dias")) {
                                            for (int j = 1; (mesCobro <= mesSelected && yearCobro == yearSelected); j++) {
                                                calendarInicial.add(Calendar.DAY_OF_YEAR, (duracionFrecuenciaInt));
                                                mesCobro = calendarInicial.get(Calendar.MONTH);
                                                yearCobro = calendarInicial.get(Calendar.YEAR);

                                                if (mesCobro == mesSelected) {
                                                    multiploIngreso = multiploIngreso + 1;
                                                }
                                            }
                                        } else if (tipoFrecuencia.equals("Semanas")) {
                                            for (int j = 1; mesCobro <= mesSelected && yearCobro == yearSelected; j++) {
                                                calendarInicial.add(Calendar.DAY_OF_YEAR, (duracionFrecuenciaInt * 7));
                                                mesCobro = calendarInicial.get(Calendar.MONTH);
                                                yearCobro = calendarInicial.get(Calendar.YEAR);

                                                if (mesCobro == mesSelected) {
                                                    multiploIngreso = multiploIngreso + 1;
                                                }
                                            }
                                        } else if (tipoFrecuencia.equals("Meses")) {
                                            for (int j = 1; mesCobro <= mesSelected && yearCobro == yearSelected; j++) {
                                                calendarInicial.add(Calendar.MONTH, (duracionFrecuenciaInt));
                                                mesCobro = calendarInicial.get(Calendar.MONTH);
                                                yearCobro = calendarInicial.get(Calendar.YEAR);

                                                if (mesCobro == mesSelected) {
                                                    multiploIngreso = multiploIngreso + 1;
                                                }
                                            }
                                        }
                                        if (dolar) {
                                            montototal = montototal + (montoDetal * multiploIngreso);
                                        } else {
                                            montototal = montototal + ((montoDetal / valorCotizacion) * multiploIngreso);
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
                                montoIngresos = (float) montototal;
                                cargarAhorros();

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


    private void cargarAhorros() {
        db.collection(Constantes.BD_AHORROS).document(user.getUid()).collection(yearSelected + "-" + mesSelected)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double montototal = 0;
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Date date = document.getDate(Constantes.BD_FECHA_INGRESO);
                                calendar.setTime(date);
                                mesItemAhorro = calendar.get(Calendar.MONTH);

                                if (mesSelected >= mesItemAhorro) {
                                    double montoDetal = document.getDouble(Constantes.BD_MONTO);
                                    boolean dolar = document.getBoolean(Constantes.BD_DOLAR);

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
        db.collection(Constantes.BD_PRESTAMOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double montototal = 0;
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                    double montoDetal = document.getDouble(Constantes.BD_MONTO);
                                    boolean dolar = document.getBoolean(Constantes.BD_DOLAR);

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
        db.collection(Constantes.BD_GASTOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double montototal = 0;
                            int mesPago = 0;
                            int yearPago = 0;
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Boolean activo = document.getBoolean(Constantes.BD_MES_ACTIVO);
                                if (activo == null || activo) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    double montoDetal = document.getDouble(Constantes.BD_MONTO);
                                    boolean dolar = document.getBoolean(Constantes.BD_DOLAR);

                                    String tipoFrecuencia = document.getString(Constantes.BD_TIPO_FRECUENCIA);
                                    if (tipoFrecuencia != null) {
                                        Calendar calendarInicial = Calendar.getInstance();
                                        Calendar calendarPago = Calendar.getInstance();
                                        Date fechaInicial = document.getDate(Constantes.BD_FECHA_INCIAL);
                                        double duracionFrecuencia = document.getDouble(Constantes.BD_DURACION_FRECUENCIA);
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
        db.collection(Constantes.BD_DEUDAS).document(user.getUid()).collection(yearSelected + "-" + mesSelected)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double montototal = 0;
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                double montoDetal = document.getDouble(Constantes.BD_MONTO);
                                boolean dolar = document.getBoolean(Constantes.BD_DOLAR);

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
        sharedPreferences = getActivity().getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.execute();

    }


    private class Cotizacion extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (valor != null) {
                tvCotizacionDolar.setText(valor);
            } else {
                valorCotizacion = sharedPreferences.getFloat("valor_cotizacion", 1.0f);

                tvCotizacionDolar.setText("Bs.S " + valorCotizacion);
            }
            cargarIngresos();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            if (sharedPreferences != null) {
                valorCotizacion = sharedPreferences.getFloat("valor_cotizacion", 1);

                tvCotizacionDolar.setText("Bs.S " + valorCotizacion);

                cargarIngresos();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String url = "https://monitordolarvenezuela.com/";
            valor = null;
            try {
                Document doc = Jsoup.connect(url).get();

                Elements data = doc.select("div.back-white-tabla");

                valor = data.select("h6.text-center").text();

                if (sharedPreferences == null) {
                    sharedPreferences = getActivity().getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);
                }

                if (valor != null) {
                    String valor1 = valor.replace("Bs.S", "");
                    String valor2 = valor1.replace(".", "");
                    String valorNeto = valor2.replace(",", ".");
                    valorCotizacion = Float.parseFloat(valorNeto);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat("valor_cotizacion", valorCotizacion);
                    editor.apply();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    private void moveToNextYear() {
        Calendar calendar = Calendar.getInstance();
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);

        if ((mes == 11 && dia > 15) || (mes == 0 && dia < 15)) {

        }
    }


    @Override
    public void onResume() {
        super.onResume();
        actualizarCotizacion();
    }
}
