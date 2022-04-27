package com.skysam.hchirinos.myfinances.homeModule.ui;


import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon;
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenter;
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenterClass;
import com.skysam.hchirinos.myfinances.homeModule.viewmodel.MainViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;


public class HomeFragment extends Fragment implements HomeView {

    private HomePresenter homePresenter;
    private PieChart pieBalance;
    private MainViewModel viewModel;
    private float montoIngresos, montoGastos;
    private TextView tvCotizacionDolar, tvSuperDeficit, tvMontoTotal, tvSuma, tvDeudas, tvAhorros, tvPrestamos, tvGastosVarios;
    private LinearLayout linearLayout;
    private static final int INTERVALO = 2500;
    private long tiempoPrimerClick;
    private MoveToNextYearDialog moveToNextYearDialog;

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

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

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
        tvCotizacionDolar = view.findViewById(R.id.textView_cotizacion_dolar);
        linearLayout = view.findViewById(R.id.linearLayout_resultado_balance);
        tvSuma = view.findViewById(R.id.textView_suma);
        tvSuperDeficit = view.findViewById(R.id.textView_deficit_superhabil);
        tvMontoTotal = view.findViewById(R.id.textView_monto_total);
        tvDeudas = view.findViewById(R.id.tv_total_deudas);
        tvAhorros = view.findViewById(R.id.tv_total_ahorros);
        tvPrestamos = view.findViewById(R.id.tv_total_prestamos);
        tvGastosVarios = view.findViewById(R.id.tv_total_gastos_varios);
        ImageButton ibTransfer = view.findViewById(R.id.ib_transfer);

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        if (currentMonth == 11 && currentDay > 14) {
            ibTransfer.setVisibility(View.VISIBLE);
        }

        ibTransfer.setOnClickListener(view1 -> {
            moveToNextYearDialog = new MoveToNextYearDialog(currentYear, homePresenter);
            moveToNextYearDialog.show(requireActivity().getSupportFragmentManager(), getTag());
            moveToNextYearDialog.setCancelable(false);
        });

        montoIngresos = 0;
        montoGastos = 0;

        loadViewModels();

        return view;
    }

    private void loadViewModels() {
        viewModel.getAmountIngresos().observe(getViewLifecycleOwner(), ingresos-> {
            montoIngresos = Float.parseFloat(ingresos.toString());
            cargarFolios();
        });
        viewModel.getAmountGastos().observe(getViewLifecycleOwner(), gastos-> {
            montoGastos = Float.parseFloat(gastos.toString());
            cargarFolios();
        });
        viewModel.getAmountAhorros().observe(getViewLifecycleOwner(), ahorros->
                tvAhorros.setText("Ahorros hasta la fecha: $" + ClassesCommon.INSTANCE.convertDoubleToString(ahorros)));
        viewModel.getAmountPrestamos().observe(getViewLifecycleOwner(), prestamos->
                tvPrestamos.setText("Préstamos hasta la fecha: $" + ClassesCommon.INSTANCE.convertDoubleToString(prestamos)));
        viewModel.getAmountDeudas().observe(getViewLifecycleOwner(), deudas->
                tvDeudas.setText("Deudas hasta la fecha: $" + ClassesCommon.INSTANCE.convertDoubleToString(deudas)));
        viewModel.getAmountGastosNoFijos().observe(getViewLifecycleOwner(), gastos->
                tvGastosVarios.setText("Gastos varios: $" + ClassesCommon.INSTANCE.convertDoubleToString(gastos)));
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
            tvSuma.setText(getString(R.string.text_total_balance_mensual,
                    ClassesCommon.INSTANCE.convertFloatToString(montoIngresos),
                    ClassesCommon.INSTANCE.convertFloatToString(montoGastos)));
            tvMontoTotal.setText("$" + ClassesCommon.INSTANCE.convertFloatToString(montoTotal));
        }
    }


    private void actualizarCotizacion() {
        homePresenter.obtenerCotizacionWeb();
    }

    @Override
    public void valorCotizacionWebOk(@NotNull String valor, float valorFloat) {
        if (tvCotizacionDolar != null) {
            tvCotizacionDolar.setText(valor);
            homePresenter.guardarCotizacionShared(valorFloat);
        }
    }

    @Override
    public void valorCotizacionWebError(float valorFloat) {
        if (tvCotizacionDolar != null) {
            tvCotizacionDolar.setText("Bs.S " + ClassesCommon.INSTANCE.convertFloatToString(valorFloat));
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        actualizarCotizacion();
    }

    @Override
    public void statusMoveNextYear(boolean statusOk, @NotNull String message) {
        moveToNextYearDialog.dismiss();
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
