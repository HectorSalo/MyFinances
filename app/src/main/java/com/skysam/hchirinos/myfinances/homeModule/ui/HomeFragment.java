package com.skysam.hchirinos.myfinances.homeModule.ui;


import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
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
import com.google.android.material.card.MaterialCardView;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD;
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon;
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenter;
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenterClass;
import com.skysam.hchirinos.myfinances.homeModule.viewmodel.MainViewModel;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class HomeFragment extends Fragment implements HomeView {

    private HomePresenter homePresenter;
    private PieChart pieBalance;
    private MainViewModel viewModel;
    private float montoIngresos, montoGastos;
    private TextView tvCotizacionDolar, tvSuperDeficit, tvMontoTotal, tvSuma, tvDeudas, tvAhorros,
            tvPrestamos, tvGastosVarios, tvCotizacionDolarBCV, tvCotizacionDolarParalelo, tvCotizacionDolarPromedio;
    private MaterialCardView linearLayout;
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
        tvCotizacionDolarBCV = view.findViewById(R.id.textView_cotizacion_dolar_bcv);
        tvCotizacionDolarParalelo = view.findViewById(R.id.textView_cotizacion_dolar_paralelo);
        tvCotizacionDolarPromedio = view.findViewById(R.id.textView_cotizacion_dolar_promedio);
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
                linearLayout.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_green_300));
            } else if (montoTotal < 0) {
                tvSuperDeficit.setText("Tiene un déficit de:");
                linearLayout.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_red_900));
            } else if (montoTotal == 0) {
                tvSuperDeficit.setText("Balance en cero");
                linearLayout.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_green_300));
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
    public void valorCotizacionWebOk(float valorBCV, float valorParalelo, String fechaBCV, String fechaParalelo) {
        if (tvCotizacionDolar != null) {
            Drawable arrowDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.arrow_circle_up_24px);
            arrowDrawable.setBounds(0, 0, 40, 40);
            tvCotizacionDolar.setText("Últ. actualización\n" + "BCV: " + ClassesCommon.INSTANCE.convertDateToCotizaciones(fechaBCV)
                    + "\nParalelo: " + ClassesCommon.INSTANCE.convertDateToCotizaciones(fechaParalelo));
            if (valorBCV > SharedPreferencesBD.INSTANCE.getCotizacion(requireContext())) {
                SpannableStringBuilder spannable = new SpannableStringBuilder();
                spannable.append("BCV\n");
                spannable.append(ClassesCommon.INSTANCE.convertFloatToString(valorBCV));
                spannable.append("  "); // Espacio antes del ícono
                ImageSpan imageSpan = new ImageSpan(arrowDrawable, ImageSpan.ALIGN_BASELINE);
                spannable.setSpan(imageSpan, spannable.length() - 1, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                tvCotizacionDolarBCV.setText(spannable);
            } else {
                tvCotizacionDolarBCV.setText("BCV\n" + ClassesCommon.INSTANCE.convertFloatToString(valorBCV));
            }
            if (valorParalelo > SharedPreferencesBD.INSTANCE.getCotizacionParalelo(requireContext())) {
                SpannableStringBuilder spannable = new SpannableStringBuilder();
                spannable.append("Paralelo\n");
                spannable.append(ClassesCommon.INSTANCE.convertFloatToString(valorParalelo));
                spannable.append("  "); // Espacio antes del ícono
                ImageSpan imageSpan = new ImageSpan(arrowDrawable, ImageSpan.ALIGN_BASELINE);
                spannable.setSpan(imageSpan, spannable.length() - 1, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                tvCotizacionDolarParalelo.setText(spannable);
            } else {
                tvCotizacionDolarParalelo.setText("Paralelo\n" + ClassesCommon.INSTANCE.convertFloatToString(valorParalelo));
            }
            float promedio = (valorBCV + valorParalelo) / 2;
            tvCotizacionDolarPromedio.setText("Promedio\n" + ClassesCommon.INSTANCE.convertFloatToString(promedio));
            tvCotizacionDolarBCV.setVisibility(View.VISIBLE);
            tvCotizacionDolarParalelo.setVisibility(View.VISIBLE);
            tvCotizacionDolarPromedio.setVisibility(View.VISIBLE);
            homePresenter.guardarCotizacionShared(valorBCV, valorParalelo);
        }
    }

    @Override
    public void valorCotizacionWebError(float valorBCV, float valorParalelo) {
        if (tvCotizacionDolar != null) {
            tvCotizacionDolar.setText("Error al obtener la cotización.\nSe muestran los últimos valores guardados");
            tvCotizacionDolarBCV.setText("BCV\n" + ClassesCommon.INSTANCE.convertFloatToString(valorBCV));
            tvCotizacionDolarParalelo.setText("Paralelo\n" + ClassesCommon.INSTANCE.convertFloatToString(valorParalelo));
            tvCotizacionDolarPromedio.setText("Promedio\n" + ClassesCommon.INSTANCE.convertFloatToString((valorBCV + valorParalelo) / 2));
            tvCotizacionDolarBCV.setVisibility(View.VISIBLE);
            tvCotizacionDolarParalelo.setVisibility(View.VISIBLE);
            tvCotizacionDolarPromedio.setVisibility(View.VISIBLE);
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
