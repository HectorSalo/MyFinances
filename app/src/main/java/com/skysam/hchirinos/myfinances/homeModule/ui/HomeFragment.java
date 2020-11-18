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
    private int mesSelected, yearSelected;
    private LinearLayout linearLayout;
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
        Spinner spinnerMeses = view.findViewById(R.id.spinner_meses);
        Spinner spinnerYears = view.findViewById(R.id.spinner_years);

        List<String> listaMeses = Arrays.asList(getResources().getStringArray(R.array.meses));
        ArrayAdapter<String> adapterMeses = new ArrayAdapter<String>(getContext(), R.layout.layout_spinner, listaMeses);
        spinnerMeses.setAdapter(adapterMeses);

        List<String> listaYears = Arrays.asList(getResources().getStringArray(R.array.years));
        ArrayAdapter<String> adapterYears = new ArrayAdapter<String>(getContext(), R.layout.layout_spinner, listaYears);
        spinnerYears.setAdapter(adapterYears);


        montoIngresos = 0;
        montoAhorros = 0;
        montoDeudas = 0;
        montoGastos = 0;
        montoPrestamos = 0;

        Calendar calendar = Calendar.getInstance();
        mesSelected = calendar.get(Calendar.MONTH);
        yearSelected = calendar.get(Calendar.YEAR);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        if (mesSelected == 10 && currentDay > 14) {
            MoveToNextYearDialog moveToNextYearDialog = new MoveToNextYearDialog(yearSelected, homePresenter);
            moveToNextYearDialog.show(requireActivity().getSupportFragmentManager(), getTag());
        }

        if (yearSelected == 2020) {
            spinnerYears.setSelection(0);
        }
        if (yearSelected == 2021) {
            spinnerYears.setSelection(1);
        }

        spinnerMeses.setSelection(mesSelected);
        spinnerMeses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mesSelected = position;
                cargarIngresos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerYears.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        yearSelected = 2020;
                        break;
                    case 1:
                        yearSelected = 2021;
                        break;
                }
                cargarIngresos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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
        homePresenter.getAhoros(yearSelected, mesSelected);
    }

    private void cargarPrestamos() {
        homePresenter.getPrestamos(yearSelected, mesSelected);
    }

    private void cargarGastos() {
        homePresenter.getGastos(yearSelected, mesSelected);
    }

    private void cargarDeudas() {
        homePresenter.getDeudas(yearSelected, mesSelected);
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

            progressBar.setVisibility(View.GONE);
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
        tvCotizacionDolar.setText("Bs.S " + valorFloat);
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

    @Override
    public void statusValorAhorros(boolean statusOk, float ahorros, @NotNull String message) {
        if (statusOk) {
            montoAhorros = ahorros;
            cargarPrestamos();
        } else {
            montoAhorros = ahorros;
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void statusValorDeudas(boolean statusOk, float deudas, @NotNull String message) {
        if (statusOk) {
            montoDeudas = deudas;
            cargarFolios();
        } else {
            montoDeudas = deudas;
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void statusValorPrestamos(boolean statusOk, float prestamos, @NotNull String message) {
        if (statusOk) {
            montoPrestamos = prestamos;
            cargarGastos();
        } else {
            montoPrestamos = prestamos;
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void statusValorGastos(boolean statusOk, float gastos, @NotNull String message) {
        if (statusOk) {
            montoGastos = gastos;
            cargarDeudas();
        } else {
            montoGastos = gastos;
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
