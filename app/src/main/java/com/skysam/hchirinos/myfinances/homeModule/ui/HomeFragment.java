package com.skysam.hchirinos.myfinances.homeModule.ui;


import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.model.constructores.ExchangeRateDto;
import com.skysam.hchirinos.myfinances.common.model.constructores.RateHistoryUi;
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon;
import com.skysam.hchirinos.myfinances.homeModule.interactor.RatesHistoryResult;
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenter;
import com.skysam.hchirinos.myfinances.homeModule.presenter.HomePresenterClass;
import com.skysam.hchirinos.myfinances.homeModule.viewmodel.MainViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class HomeFragment extends Fragment implements HomeView {

    private HomePresenter homePresenter;
    private MainViewModel viewModel;
    private float montoIngresos, montoGastos;
    private TextView tvMontoTotal;
    private View tileBcv, tileParalelo, tilePromedio, tileEuro;
    private TextView tvRatesUpdated;
    private TextView tvBadge;
    private TextView tvIngresosValue, tvGastosValue, tvRatioLabel;
    private LinearProgressIndicator progressRatio;
    private View rowAhorros, rowPrestamos, rowDeudas, rowGastosVarios, rowCapital;
    private static final int INTERVALO = 2500;
    private long tiempoPrimerClick;
    private MoveToNextYearDialog moveToNextYearDialog;
    private final Object summaryLock = new Object();
    private String summaryGastosVarios = "--";
    private String summaryDeudas = "--";
    private String summaryAhorros = "--";
    private String summaryPrestamos = "--";
    private String summaryCapital = "--";
    private String lastDateBcv = "";
    private String lastBcvStr = "--";
    private String lastEurStr = "--";
    private float lastBcvPrev = 0f;
    private float lastEurPrev = 0f;

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

        tvMontoTotal = view.findViewById(R.id.textView_monto_total);
        ImageButton ibTransfer = view.findViewById(R.id.ib_transfer);

        tvIngresosValue = view.findViewById(R.id.tv_ingresos_value);
        tvGastosValue = view.findViewById(R.id.tv_gastos_value);
        progressRatio = view.findViewById(R.id.progress_ratio);

        tvRatesUpdated = view.findViewById(R.id.tv_rates_updated);
        tvRatioLabel = view.findViewById(R.id.tv_ratio_label);
        ImageButton ibRatesHistory = view.findViewById(R.id.ib_rates_history);

        tileBcv = view.findViewById(R.id.tile_bcv);
        tileParalelo = view.findViewById(R.id.tile_paralelo);
        tilePromedio = view.findViewById(R.id.tile_promedio);
        tileEuro = view.findViewById(R.id.tile_euro);
        tvRatesUpdated = view.findViewById(R.id.tv_rates_updated);

        rowAhorros = view.findViewById(R.id.row_ahorros);
        rowPrestamos = view.findViewById(R.id.row_prestamos);
        rowDeudas = view.findViewById(R.id.row_deudas);
        rowGastosVarios = view.findViewById(R.id.row_gastos_varios);
        rowCapital = view.findViewById(R.id.row_capital);
        tvBadge = view.findViewById(R.id.tv_balance_badge);

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
        ibRatesHistory.setOnClickListener(v -> {
            ArrayList<RateHistoryUi> cache = new ArrayList<>();

            // Cache (2 días): hoy + anterior
            if (!lastDateBcv.isEmpty() && !lastBcvStr.equals("--")) {
                cache.add(new RateHistoryUi(
                        lastDateBcv,
                        "USD " + lastBcvStr,
                        "EUR " + lastEurStr
                ));

                if (lastBcvPrev != 0f) {
                    cache.add(new RateHistoryUi(
                            "Día anterior",
                            "USD " + ClassesCommon.INSTANCE.convertFloatToString(lastBcvPrev),
                            lastEurPrev == 0f ? null : "EUR " + ClassesCommon.INSTANCE.convertFloatToString(lastEurPrev)
                    ));
                }
            }

            RatesHistoryBottomSheet sheet = RatesHistoryBottomSheet.Companion.newInstance(cache);

            sheet.setListener((from, to) -> homePresenter.obtenerHistorialTasas(from, to));

            sheet.show(requireActivity().getSupportFragmentManager(), "rates_history_sheet");
        });

        montoIngresos = 0;
        montoGastos = 0;

        tvRatesUpdated.setText(getString(R.string.home_rates_loading));

        loadViewModels();

        return view;
    }

    private void loadViewModels() {
        viewModel.getAmountIngresos().observe(getViewLifecycleOwner(), ingresos -> {
            montoIngresos = Float.parseFloat(ingresos.toString());
            cargarFolios();
        });

        viewModel.getAmountGastos().observe(getViewLifecycleOwner(), gastos -> {
            montoGastos = Float.parseFloat(gastos.toString());
            cargarFolios();
        });

        // Render inicial con placeholders (opcional pero recomendado)
        renderSummaryOrdered();

        viewModel.getAmountGastosNoFijos().observe(getViewLifecycleOwner(), gastos -> {
            String value = "$" + ClassesCommon.INSTANCE.convertDoubleToString(gastos);
            synchronized (summaryLock) {
                summaryGastosVarios = value;
            }
            renderSummaryOrdered();
        });

        viewModel.getAmountDeudas().observe(getViewLifecycleOwner(), deudas -> {
            String value = "$" + ClassesCommon.INSTANCE.convertDoubleToString(deudas);
            synchronized (summaryLock) {
                summaryDeudas = value;
            }
            renderSummaryOrdered();
        });

        viewModel.getAmountAhorros().observe(getViewLifecycleOwner(), ahorros -> {
            String value = "$" + ClassesCommon.INSTANCE.convertDoubleToString(ahorros);
            synchronized (summaryLock) {
                summaryAhorros = value;
            }
            renderSummaryOrdered();
        });

        viewModel.getAmountCapital().observe(getViewLifecycleOwner(), capital -> {
            String value = "$" + ClassesCommon.INSTANCE.convertDoubleToString(capital);
            synchronized (summaryLock) {
                summaryCapital = value;
            }
            renderSummaryOrdered();
        });

        viewModel.getAmountPrestamos().observe(getViewLifecycleOwner(), prestamos -> {
            String value = "$" + ClassesCommon.INSTANCE.convertDoubleToString(prestamos);
            synchronized (summaryLock) {
                summaryPrestamos = value;
            }
            renderSummaryOrdered();
        });
    }

    private void setRateTile(
            View tile,
            String label,
            String value,
            @Nullable Float currentValue,
            @Nullable Float previousValue,
            @Nullable String updatedDate
    ) {
        setRateTile(tile, label, value, currentValue, previousValue, updatedDate, false, null);
    }


    private void setRateTile(
            View tile,
            String label,
            String value,
            @Nullable Float currentValue,
            @Nullable Float previousValue,
            @Nullable String updatedDate,
            boolean includeDirectionWord,
            @Nullable String deltaUnit // ej "pp"
    ) {
        TextView tvLabel = tile.findViewById(R.id.tv_rate_label);
        TextView tvValue = tile.findViewById(R.id.tv_rate_value);
        TextView tvUpdated = tile.findViewById(R.id.tv_rate_updated);
        TextView tvChange = tile.findViewById(R.id.tv_rate_change);

        tvLabel.setText(label);
        tvValue.setText(value);

        // Fecha de actualización (debajo del título)
        if (tvUpdated != null) {
            if (updatedDate == null || updatedDate.trim().isEmpty()) {
                tvUpdated.setVisibility(View.GONE);
            } else {
                tvUpdated.setVisibility(View.VISIBLE);
                tvUpdated.setText("Actualización: " + updatedDate);
            }
        }

        if (tvChange == null) return;

        if (currentValue == null || previousValue == null || previousValue == 0f) {
            tvChange.setVisibility(View.GONE);
            return;
        }

        float delta = currentValue - previousValue;
        float percent = (delta / previousValue) * 100f;

        // Texto de dirección (solo si lo pides)
        String directionWord = "";
        if (includeDirectionWord) {
            if (delta > 0f) directionWord = "Subió ";
            else if (delta < 0f) directionWord = "Bajó ";
            else directionWord = "Sin cambio ";
        }

        // Formato de delta / percent
        String deltaAbsStr = ClassesCommon.INSTANCE.convertFloatToString(Math.abs(delta));
        String percentAbsStr = ClassesCommon.INSTANCE.convertFloatToString(Math.abs(percent));

        final boolean isUp = delta > 0f;

        String deltaStr = (isUp ? "+" : "-") + deltaAbsStr;
        String percentStr = "(" + (isUp ? "+" : "-") + percentAbsStr + "%)";

        // Sufijo para delta (pp)
        String unit = "";
        if (deltaUnit != null) {
            String u = deltaUnit.trim();
            if (!u.isEmpty()) {
                // Si es "%", se pega sin espacio: "+1,20%"
                unit = "%".equals(u) ? "%" : " " + u;
            }
        }

        tvChange.setText(directionWord + deltaStr + unit + " " + percentStr);
        tvChange.setVisibility(View.VISIBLE);

        // Color + icono según dirección (tu regla actual)
        final int color = ContextCompat.getColor(
                requireContext(),
                isUp ? R.color.badge_negative : R.color.badge_positive
        );
        tvChange.setTextColor(color);
        TextViewCompat.setCompoundDrawableTintList(tvChange, ColorStateList.valueOf(color));

        int iconRes = isUp ? R.drawable.arrow_circle_up_24px : R.drawable.ic_arrow_circle_down_24;

        // Tamaño del icono igual al texto
        int iconSizePx = Math.round(tvChange.getTextSize());

        try {
            Drawable start = AppCompatResources.getDrawable(requireContext(), iconRes);
            if (start != null) {
                start = start.mutate();
                start.setBounds(0, 0, iconSizePx, iconSizePx);

                TextViewCompat.setCompoundDrawablesRelative(tvChange, start, null, null, null);
            }
        } catch (Exception ignored) {}
    }

    private void setSummaryRow(View row, String label, String value) {
        TextView tvLabel = row.findViewById(R.id.tv_summary_label);
        TextView tvValue = row.findViewById(R.id.tv_summary_value);
        tvLabel.setText(label);
        tvValue.setText(value);
    }

    private void renderSummaryOrdered() {
        if (getContext() == null) return;

        // Orden fijo: Gastos varios, Deudas, Ahorros, Préstamos
        setSummaryRow(rowGastosVarios, getString(R.string.summary_gastos_varios), summaryGastosVarios);
        setSummaryRow(rowDeudas, getString(R.string.menu_deudas), summaryDeudas);
        setSummaryRow(rowAhorros, getString(R.string.menu_ahorros), summaryAhorros);
        setSummaryRow(rowPrestamos, getString(R.string.menu_prestamos), summaryPrestamos);
        setSummaryRow(rowCapital, getString(R.string.summary_capital), summaryCapital);
    }


    private void cargarFolios() {
        if (getContext() == null) return;

        float montoTotal = montoIngresos - montoGastos;

        // Monto total con color según superávit/déficit
        tvMontoTotal.setText("$" + ClassesCommon.INSTANCE.convertFloatToString(montoTotal));

        if (montoTotal > 0) {
            tvBadge.setText("Superávit");
            tvBadge.setBackgroundResource(R.drawable.bg_badge_positive);
            tvMontoTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.badge_positive));
        } else if (montoTotal < 0) {
            tvBadge.setText("Déficit");
            tvBadge.setBackgroundResource(R.drawable.bg_badge_negative);
            tvMontoTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.badge_negative));
        } else {
            tvBadge.setText("En cero");
            tvBadge.setBackgroundResource(R.drawable.bg_badge_neutral);
            tvMontoTotal.setTextColor(
                    MaterialColors.getColor(tvMontoTotal, com.google.android.material.R.attr.colorOnSurface)
            );
        }

        // Ingresos/Gastos
        tvIngresosValue.setText("$" + ClassesCommon.INSTANCE.convertFloatToString(montoIngresos));
        tvGastosValue.setText("$" + ClassesCommon.INSTANCE.convertFloatToString(montoGastos));

        // Disponible (0..100): 100% si no hay gastos, baja a medida que gastas
        float ingresos = Math.max(montoIngresos, 1f);           // evita división por 0
        float disponible = montoIngresos - montoGastos;         // puede ser negativo
        float disponibleClamped = Math.max(0f, Math.min(disponible, montoIngresos));
        int progress = Math.round((disponibleClamped / ingresos) * 100f);
        progressRatio.setProgress(progress);
        tvRatioLabel.setText("Disponible: " + progress + "%");

    }

    private void actualizarCotizacion() {
        homePresenter.obtenerCotizacionWeb();
    }

    @Override
    public void valorCotizacionWebOk(
            float valorBCV,
            float valorBCVPrev,
            float valorParalelo,
            float valorParaleloPrev,
            float valorEuro,
            float valorEuroPrev,
            String fechaBCV,
            String fechaParalelo
    ) {
        if (getContext() == null) return;

        tvRatesUpdated.setText("");
        tvRatesUpdated.setVisibility(View.GONE);

        String bcvStr = ClassesCommon.INSTANCE.convertFloatToString(valorBCV);
        String parStr = ClassesCommon.INSTANCE.convertFloatToString(valorParalelo);
        String eurStr = ClassesCommon.INSTANCE.convertFloatToString(valorEuro);

        String dateBcv = ClassesCommon.INSTANCE.convertDateToCotizaciones(fechaBCV);
        String datePar = (fechaParalelo == null || fechaParalelo.trim().isEmpty())
                ? ""
                : ClassesCommon.INSTANCE.convertDateToCotizaciones(fechaParalelo);

        Float diffPct = (valorBCV != 0f)
                ? ((valorParalelo - valorBCV) / valorBCV) * 100f
                : null;

        Float diffPctPrev = (valorBCVPrev != 0f)
                ? ((valorParaleloPrev - valorBCVPrev) / valorBCVPrev) * 100f
                : null;

        String diffStr = (diffPct == null)
                ? "--"
                : ClassesCommon.INSTANCE.convertFloatToString(diffPct) + "%";

        // Nota: ya NO pasas showUp; pasas current/previous
        setRateTile(tileBcv, getString(R.string.rate_bcv), bcvStr, valorBCV, valorBCVPrev, dateBcv);
        setRateTile(tileParalelo, getString(R.string.rate_paralelo), parStr, valorParalelo, valorParaleloPrev, datePar);
        setRateTile(tileEuro, getString(R.string.rate_euro), eurStr, valorEuro, valorEuroPrev, dateBcv);
        setRateTile(tilePromedio,"Diferencial BCV vs. Paralelo", diffStr, diffPct, diffPctPrev, "", true, "%");

        lastDateBcv = dateBcv;
        lastBcvStr = bcvStr;
        lastEurStr = eurStr;
        lastBcvPrev = valorBCVPrev;
        lastEurPrev = valorEuroPrev;
    }

    @Override
    public void valorCotizacionWebError(float valorBCV, float valorParalelo, float valorEuro) {
        if (getContext() == null) return;

        tvRatesUpdated.setText("Error al obtener cotización. Mostrando últimos valores guardados");
        tvRatesUpdated.setVisibility(View.VISIBLE);

        String bcvStr = ClassesCommon.INSTANCE.convertFloatToString(valorBCV);
        String parStr = ClassesCommon.INSTANCE.convertFloatToString(valorParalelo);
        String eurStr = ClassesCommon.INSTANCE.convertFloatToString(valorEuro);

        Float diffPct = (valorBCV != 0f)
                ? ((valorParalelo - valorBCV) / valorBCV) * 100f
                : null;

        String diffStr = (diffPct == null)
                ? "--"
                : ClassesCommon.INSTANCE.convertFloatToString(diffPct) + "%";

        setRateTile(tileBcv, getString(R.string.rate_bcv), bcvStr, null, null, null);
        setRateTile(tileParalelo, getString(R.string.rate_paralelo), parStr, null, null, null);
        setRateTile(tileEuro, getString(R.string.rate_euro), eurStr, null, null, null);
        setRateTile(
                tilePromedio,
                "Diferencial BCV vs. Paralelo",
                diffStr,
                null,  // sin current/previous => oculta el change
                null,
                null,
                false,
                "%"
        );
    }

    @Override
    public void historialTasasResult(@NotNull RatesHistoryResult result) {
        Fragment f = requireActivity().getSupportFragmentManager()
                .findFragmentByTag("rates_history_sheet");

        if (!(f instanceof RatesHistoryBottomSheet)) return;
        RatesHistoryBottomSheet sheet = (RatesHistoryBottomSheet) f;

        if (result instanceof RatesHistoryResult.Success) {
            RatesHistoryResult.Success success = (RatesHistoryResult.Success) result;
            List<RateHistoryUi> uiList = mapResultToUi(success.getItems());
            sheet.renderHistory(uiList);

        } else if (result instanceof RatesHistoryResult.Empty) {
            sheet.renderHistory(java.util.Collections.emptyList());

        } else if (result instanceof RatesHistoryResult.Error) {
            RatesHistoryResult.Error err = (RatesHistoryResult.Error) result;
            sheet.renderError(err.getMessage());
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

    private List<RateHistoryUi> mapResultToUi(List<ExchangeRateDto> items) {
        ArrayList<RateHistoryUi> out = new ArrayList<>();

        for (ExchangeRateDto it : items) {

            // Ajusta nombres según tu DTO real
            String date = it.getDate(); // ejemplo
            String dateLabel = ClassesCommon.INSTANCE.convertDateToCotizaciones(date);

            String usd = "USD " + ClassesCommon.INSTANCE.convertDoubleToString(it.getUsd()); // ejemplo
            String eur = null;

            // si tu DTO trae eur
            if (it.getEur() != 0.0) {
                eur = "EUR " + ClassesCommon.INSTANCE.convertDoubleToString(it.getEur());
            }

            out.add(new RateHistoryUi(dateLabel, usd, eur));
        }

        return out;
    }
}
