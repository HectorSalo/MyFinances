package com.skysam.hchirinos.myfinances.homeModule.ui;


import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenter;
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenterClass;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;



public class HomeFragment extends Fragment implements HomeView {

    private HomePresenter homePresenter;
    private PieChart pieBalance;
    private float montoIngresos, montoGastos;
    private ProgressBar progressBar;
    private TextView tvCotizacionDolar, tvSuperDeficit, tvMontoTotal, tvSuma, tvDeudas, tvAhorros, tvPrestamos;
    private int mesSelected, yearSelected;
    private LinearLayout linearLayout;
    private static final int INTERVALO = 2500;
    private long tiempoPrimerClick;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
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
        tvDeudas = view.findViewById(R.id.tv_total_deudas);
        tvAhorros = view.findViewById(R.id.tv_total_ahorros);
        tvPrestamos = view.findViewById(R.id.tv_total_prestamos);

        Calendar calendar = Calendar.getInstance();
        mesSelected = calendar.get(Calendar.MONTH);
        yearSelected = calendar.get(Calendar.YEAR);

        montoIngresos = 0;
        montoGastos = 0;

        cargarFolios();

        return view;
    }

    private void cargarIngresos() {
        homePresenter.getIngresos(yearSelected, mesSelected);
    }

    private void cargarGastos() {
        homePresenter.getGastos(yearSelected, mesSelected);
    }

    private void cargarDeudas() {
        homePresenter.getDeudas(yearSelected, mesSelected);
    }

    private void cargarAhorros() {
        homePresenter.getAhorros(yearSelected, mesSelected);
    }

    private void cargarPrestamos() {
        homePresenter.getPrestamos(yearSelected, mesSelected);
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
            pieEntries.add(new PieEntry(montoGastos, getContext().getString(R.string.pie_egresos)));


            PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
            pieDataSet.setValueTextSize(18);
            pieDataSet.setColors(ContextCompat.getColor(requireContext(), R.color.md_green_300),
                    ContextCompat.getColor(getContext(), R.color.md_red_900));
            pieDataSet.setFormSize(16);
            PieData pieData = new PieData(pieDataSet);

            pieBalance.setData(pieData);
            pieBalance.getLegend().setTextColor(ContextCompat.getColor(requireContext(), R.color.md_teal_700));
            pieBalance.invalidate();

            float montoTotal = montoIngresos - montoGastos;
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
            tvSuma.setText(getString(R.string.text_total_balance_mensual, montoIngresos, montoGastos));
            tvMontoTotal.setText("$" + montoTotal);

            progressBar.setVisibility(View.GONE);
        }
    }


    private void actualizarCotizacion() {
        progressBar.setVisibility(View.VISIBLE);
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
            cargarGastos();
        } else {
            montoIngresos = ingresos;
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void statusValorGastos(boolean statusOk, float gastos, @NotNull String message) {
        if (statusOk) {
            montoGastos = gastos;
            cargarDeudas();
            cargarFolios();
        } else {
            montoGastos = gastos;
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void statusMoveNextYear(boolean statusOk, @NotNull String message) {

    }

    @Override
    public void statusValorDeudas(boolean statusOk, float deudas, @NotNull String message) {
        if (statusOk) {
            tvDeudas.setText("Deudas hasta la fecha: $" + message);
            cargarPrestamos();
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void statusValorPrestamos(boolean statusOk, float prestamos, @NotNull String message) {
        if (statusOk) {
            tvPrestamos.setText("Préstamos hasta la fecha: $" + message);
            cargarAhorros();
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void statusValorAhorros(boolean statusOk, float ahorros, @NotNull String message) {
        if (statusOk) {
            tvAhorros.setText("Ahorros hasta la fecha: $" + message);
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
