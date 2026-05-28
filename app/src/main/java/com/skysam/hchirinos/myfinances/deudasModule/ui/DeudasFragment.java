package com.skysam.hchirinos.myfinances.deudasModule.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.common.model.constructores.AhorrosConstructor;
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class DeudasFragment extends Fragment {

    private enum DebtFilter { PENDING, PAID, ALL }

    public DeudasFragment() {
        // Required empty public constructor
    }

    private RecyclerView recyclerView;
    private DeudasAdapter deudasAdapter;
    private ArrayList<AhorrosConstructor> listaDeudas;
    private ProgressBar progressBar;
    private TextView tvSinLista;
    private LottieAnimationView lottieAnimationView;
    private boolean fragmentCreado;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int mesSelected, yearSelected;
    private Toolbar toolbar;
    private MenuItem itemBuscar;
    private DebtFilter currentFilter = DebtFilter.PENDING;
    private MaterialButtonToggleGroup toggleGroupFiltro;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        configurarToolbar();
        return inflater.inflate(R.layout.fragment_deudas, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ((HomeActivity) requireActivity()).goHome();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);

        Calendar calendar = Calendar.getInstance();
        yearSelected = calendar.get(Calendar.YEAR);
        mesSelected = calendar.get(Calendar.MONTH);

        progressBar = view.findViewById(R.id.progressBar);
        tvSinLista = view.findViewById(R.id.textView_sin_lista);
        lottieAnimationView = view.findViewById(R.id.lottieAnimationView);

        Spinner spinner = view.findViewById(R.id.spinner_mes);
        Spinner spinnerYear = view.findViewById(R.id.spinner_year);

        fragmentCreado = true;

        List<String> listaMeses = Arrays.asList(getResources().getStringArray(R.array.meses));
        ArrayAdapter<String> adapterMeses = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, listaMeses);
        spinner.setAdapter(adapterMeses);

        List<String> listaYear = Arrays.asList(getResources().getStringArray(R.array.years));
        ArrayAdapter<String> adapterYears = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, listaYear);
        spinnerYear.setAdapter(adapterYears);

        spinnerYear.setSelection(yearSelected - 2020);

        spinner.setSelection(mesSelected);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mesSelected = position;
                if (!fragmentCreado) {
                    cargarDeudas();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                yearSelected = 2020 + position;
                if (!fragmentCreado) {
                    cargarDeudas();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        recyclerView = view.findViewById(R.id.rv_deudas);

        toggleGroupFiltro = view.findViewById(R.id.toggleGroup_filtro);
        toggleGroupFiltro.check(R.id.btn_filtro_pendientes);
        toggleGroupFiltro.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_filtro_pendientes) {
                currentFilter = DebtFilter.PENDING;
            } else if (checkedId == R.id.btn_filtro_pagadas) {
                currentFilter = DebtFilter.PAID;
            } else {
                currentFilter = DebtFilter.ALL;
            }
            applyDebtFilter();
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0){
                    fab.hide();
                } else{
                    fab.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void configurarToolbar() {
        toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.top_bar_menu);
        Menu menu = toolbar.getMenu();
        itemBuscar = menu.findItem(R.id.menu_buscar);
        SearchView searchView = (SearchView) itemBuscar.getActionView();
        searchView.setQueryHint(getString(R.string.searchview_hint_concepto_prestamista));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                buscarItem(newText);
                return false;
            }
        });
        searchView.setOnCloseListener(() -> {
            lottieAnimationView.setVisibility(View.GONE);
            return false;
        });
    }


    private void cargarDeudas() {
        progressBar.setVisibility(View.VISIBLE);
        listaDeudas = new ArrayList<>();
        deudasAdapter = new DeudasAdapter(listaDeudas, getContext(), yearSelected, mesSelected);
        deudasAdapter.setOnDebtUpdatedListener(() -> applyDebtFilter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(deudasAdapter);

        CollectionReference reference = db.collection(Constants.BD_DEUDAS).document(Auth.INSTANCE.uidCurrentUser())
                .collection(yearSelected + "-" + mesSelected);

        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listaDeudas.clear();
                for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                    AhorrosConstructor deuda = new AhorrosConstructor();

                    deuda.setIdDeuda(doc.getId());
                    deuda.setPrestamista(doc.getString(Constants.BD_PRESTAMISTA));
                    deuda.setConcepto(doc.getString(Constants.BD_CONCEPTO));
                    deuda.setDolar(doc.getBoolean(Constants.BD_DOLAR));
                    deuda.setMonto(doc.getDouble(Constants.BD_MONTO));
                    deuda.setFechaIngreso(doc.getDate(Constants.BD_FECHA_INGRESO));

                    listaDeudas.add(deuda);
                }
                applyDebtFilter();
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error al cargar la lista. Intente nuevamente", Toast.LENGTH_SHORT).show();
            }
            fragmentCreado = false;
        });
    }

    private void applyDebtFilter() {
        if (listaDeudas == null) return;
        ArrayList<AhorrosConstructor> filtered = new ArrayList<>();
        for (AhorrosConstructor debt : listaDeudas) {
            boolean isPaid = debt.getMonto() == 0;
            if (currentFilter == DebtFilter.PENDING && !isPaid) {
                filtered.add(debt);
            } else if (currentFilter == DebtFilter.PAID && isPaid) {
                filtered.add(debt);
            } else if (currentFilter == DebtFilter.ALL) {
                filtered.add(debt);
            }
        }
        if (currentFilter == DebtFilter.ALL) {
            Collections.sort(filtered, (d1, d2) -> {
                boolean d1Paid = d1.getMonto() == 0;
                boolean d2Paid = d2.getMonto() == 0;
                if (d1Paid == d2Paid) return 0;
                return d1Paid ? 1 : -1;
            });
        }
        deudasAdapter.updateList(filtered);
        if (filtered.isEmpty()) {
            String emptyText;
            if (currentFilter == DebtFilter.PENDING) {
                emptyText = getString(R.string.deudas_sin_pendientes);
            } else if (currentFilter == DebtFilter.PAID) {
                emptyText = getString(R.string.deudas_sin_pagadas);
            } else {
                emptyText = getString(R.string.deudas_sin_registradas);
            }
            tvSinLista.setText(emptyText);
            tvSinLista.setVisibility(View.VISIBLE);
        } else {
            tvSinLista.setVisibility(View.GONE);
        }
    }

    public void buscarItem(String text) {
        if (listaDeudas != null) {
            if (listaDeudas.isEmpty()) {
                Toast.makeText(getContext(), "No hay lista cargada", Toast.LENGTH_SHORT).show();
            } else {
                String userInput = text.toLowerCase();
                final ArrayList<AhorrosConstructor> newList = new ArrayList<>();

                for (AhorrosConstructor name : listaDeudas) {
                    boolean matchesSearch = name.getConcepto().toLowerCase().contains(userInput)
                            || name.getPrestamista().toLowerCase().contains(userInput);
                    if (!matchesSearch) continue;
                    boolean isPaid = name.getMonto() == 0;
                    if (currentFilter == DebtFilter.PENDING && isPaid) continue;
                    if (currentFilter == DebtFilter.PAID && !isPaid) continue;
                    newList.add(name);
                }
                if (newList.isEmpty()) {
                    lottieAnimationView.setVisibility(View.VISIBLE);
                    lottieAnimationView.playAnimation();
                }
                deudasAdapter.updateList(newList);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (toolbar != null) {
            itemBuscar.setVisible(false);
            new Handler(Looper.myLooper()).postDelayed(() -> {
                toolbar.animate().translationY(0)
                        .setDuration(500);
                toolbar.setTitle(getString(R.string.pie_deudas));
                itemBuscar.setVisible(true);
            }, 300);
        }
        cargarDeudas();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (toolbar != null) {
            toolbar.animate().translationY(toolbar.getHeight())
                    .setDuration(300);
        }
    }

}
