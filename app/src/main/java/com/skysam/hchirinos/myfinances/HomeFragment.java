package com.skysam.hchirinos.myfinances;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;


public class HomeFragment extends Fragment {

    private PieChart pieBalance;
    private Spinner spinner;
    private List<String> listaMeses;


    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        pieBalance = view.findViewById(R.id.pie_balance);
        spinner = view.findViewById(R.id.spinner_meses);
        listaMeses = Arrays.asList(getResources().getStringArray(R.array.meses));
        ArrayAdapter<String> adapterMeses = new ArrayAdapter<String>(getContext(), R.layout.layout_spinner, listaMeses);
        spinner.setAdapter(adapterMeses);

        Calendar calendar = Calendar.getInstance();
        int mes = calendar.get(Calendar.MONTH);

        spinner.setSelection(mes);

        cargarFolios();

        return view;
    }

    private void cargarFolios() {

        pieBalance.setDescription(null);
        pieBalance.setCenterText("Balance Mensual");
        pieBalance.setCenterTextSize(24);
        pieBalance.setDrawEntryLabels(false);
        pieBalance.setRotationEnabled(false);

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(5313, Objects.requireNonNull(getContext()).getString(R.string.pie_ingresos)));
        pieEntries.add(new PieEntry(1200, getContext().getString(R.string.pie_ahorros)));
        pieEntries.add(new PieEntry(313, getContext().getString(R.string.pie_prestamos)));
        pieEntries.add(new PieEntry(587, getContext().getString(R.string.pie_egresos)));
        pieEntries.add(new PieEntry(2489, getContext().getString(R.string.pie_deudas)));


        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setValueTextSize(18);
        pieDataSet.setColors(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.md_green_300), ContextCompat.getColor(getContext(), R.color.md_green_700), ContextCompat.getColor(getContext(), R.color.md_light_green_A700),
                ContextCompat.getColor(getContext(), R.color.md_red_400), ContextCompat.getColor(getContext(), R.color.md_red_900));
        pieDataSet.setFormSize(16);
        PieData pieData = new PieData(pieDataSet);

        pieBalance.setData(pieData);
        pieBalance.invalidate();
    }
}
