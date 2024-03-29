package com.skysam.hchirinos.myfinances.ingresosModule.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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


import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeActivity;
import com.skysam.hchirinos.myfinances.common.model.constructores.IngresosGastosConstructor;
import com.skysam.hchirinos.myfinances.ingresosModule.presenter.IngresosPresenter;
import com.skysam.hchirinos.myfinances.ingresosModule.presenter.IngresosPresenterClass;
import com.skysam.hchirinos.myfinances.ui.activityGeneral.EditarActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class IngresosFragment extends Fragment implements IngresosView {

    private IngresosAdapter ingresosAdapter;
    private ArrayList<IngresosGastosConstructor> listaIngresos, newList;
    private ProgressBar progressBar;
    private TextView tvSinLista;
    private LottieAnimationView lottieAnimationView;
    private boolean fragmentCreado;
    private boolean fromSearch = false;
    private CoordinatorLayout coordinatorLayout;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int mesSelected, yearSelected;
    private Toolbar toolbar;
    private MenuItem itemBuscar;
    private IngresosPresenter ingresosPresenter;


    public IngresosFragment() {
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
        View view = inflater.inflate(R.layout.fragment_ingresos, container, false);

        setHasOptionsMenu(true);
        configurarToolbar();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ((HomeActivity) requireActivity()).goHome();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);

        ingresosPresenter = new IngresosPresenterClass(this);

        fragmentCreado = true;

        Calendar calendar = Calendar.getInstance();
        yearSelected = calendar.get(Calendar.YEAR);
        mesSelected = calendar.get(Calendar.MONTH);

        RecyclerView recyclerView = view.findViewById(R.id.rv_ingresos);

        listaIngresos = new ArrayList<>();

        ingresosAdapter = new IngresosAdapter(listaIngresos, getContext(), requireActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(ingresosAdapter);

        progressBar = view.findViewById(R.id.progressBar_ingresos);
        tvSinLista = view.findViewById(R.id.textView_sin_lista);
        lottieAnimationView = view.findViewById(R.id.lottieAnimationView);
        coordinatorLayout = view.findViewById(R.id.coordinator_snackbar);
        Spinner spinnerMes = view.findViewById(R.id.spinner_ingreso_mes);
        Spinner spinnerYear = view.findViewById(R.id.spinner_ingreso_year);

        List<String> listaMeses = Arrays.asList(getResources().getStringArray(R.array.meses));
        ArrayAdapter<String> adapterMeses = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, listaMeses);
        spinnerMes.setAdapter(adapterMeses);

        List<String> listaYear = Arrays.asList(getResources().getStringArray(R.array.years));
        ArrayAdapter<String> adapterYears = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, listaYear);
        spinnerYear.setAdapter(adapterYears);

        spinnerYear.setSelection(yearSelected - 2020);

        spinnerMes.setSelection(mesSelected);
        spinnerMes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mesSelected = position;
                if (!fragmentCreado) {
                    cargarIngresos();
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
                    cargarIngresos();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
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

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemSwipe);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        return view;
    }

    private void configurarToolbar() {
        toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.top_bar_menu);
        Menu menu = toolbar.getMenu();
        itemBuscar = menu.findItem(R.id.menu_buscar);
        SearchView searchView = (SearchView) itemBuscar.getActionView();
        searchView.setQueryHint(getString(R.string.searchview_hint_concepto));
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


    private final ItemTouchHelper.SimpleCallback itemSwipe = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            IngresosGastosConstructor itemSwipe;
            itemSwipe = listaIngresos.get(position);

            switch (direction) {
                case ItemTouchHelper.RIGHT:
                    if (itemSwipe.getTipoFrecuencia() != null) {
                        crearDialog(position);
                    } else {
                        eliminarDefinitivo(position);
                    }
                    break;
                case ItemTouchHelper.LEFT:
                    if (newList != null) {
                        if (newList.isEmpty()) {
                            editarItem(listaIngresos.get(position).getIdIngreso(), listaIngresos.get(position).getTipoFrecuencia());
                        } else {
                            editarItem(newList.get(position).getIdIngreso(), newList.get(position).getTipoFrecuencia());
                        }
                    } else {
                        editarItem(listaIngresos.get(position).getIdIngreso(), listaIngresos.get(position).getTipoFrecuencia());
                    }
                    break;
            }

        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeRightActionIcon(R.drawable.ic_delete_item)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_red_A700))
                    .addSwipeLeftActionIcon(R.drawable.ic_edit_item_24dp)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_orange_A700))
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    };

    private void cargarIngresos() {
        progressBar.setVisibility(View.VISIBLE);
        listaIngresos = new ArrayList<>();
        ingresosPresenter.getIngresos(yearSelected, mesSelected);
    }

    private void crearDialog(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("¿Qué desea hacer?")
                .setItems(R.array.opciones_borrar_ingreso, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            suspenderMes(position);
                            break;
                        case 1:
                            eliminarDefinitivo(position);
                            break;
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancelar), (dialogInterface, i) -> cargarIngresos()).show();
    }

    private void suspenderMes(int position) {
        String id;
        if (newList == null || newList.isEmpty()) {
            id = listaIngresos.get(position).getIdIngreso();
        } else {
            id = newList.get(position).getIdIngreso();
        }
        ingresosPresenter.suspenderMes(yearSelected, mesSelected, id);
    }

    private void eliminarDefinitivo(final int position) {
        final IngresosGastosConstructor itemSwipe;
        ArrayList<IngresosGastosConstructor> itemToResored;
        if (newList == null || newList.isEmpty()) {
            fromSearch = false;
            itemSwipe = listaIngresos.get(position);

            itemToResored = new ArrayList<>();
            itemToResored.add(itemSwipe);

            listaIngresos.remove(position);
            ingresosAdapter.updateList(listaIngresos);

            Snackbar snackbar = Snackbar.make(coordinatorLayout, itemSwipe.getConcepto() + " borrado", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer", view -> {
                listaIngresos.add(position, itemSwipe);
                ingresosAdapter.updateList(listaIngresos);
                itemToResored.clear();
            });
            snackbar.show();
        } else {
            fromSearch = true;
            itemSwipe = newList.get(position);

            itemToResored = new ArrayList<>();
            itemToResored.add(itemSwipe);

            newList.remove(position);
            ingresosAdapter.updateList(newList);

            Snackbar snackbar = Snackbar.make(coordinatorLayout, itemSwipe.getConcepto() + " borrado", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer", view -> {
                        newList.add(position, itemSwipe);
                        ingresosAdapter.updateList(newList);
                        itemToResored.clear();
                    });
            snackbar.show();
        }

        new Handler(Looper.myLooper()).postDelayed(() -> {
            if (!itemToResored.isEmpty()) {
                deleteItemSwipe(itemSwipe.getIdIngreso(), itemSwipe.getFechaFinal());
            }
        }, 3500);
    }


    private void deleteItemSwipe(String id, Date fechaFinal) {
        if (fechaFinal != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fechaFinal);
            final int mesFinal = calendar.get(Calendar.MONTH);
            for (int i = mesSelected; i <= mesFinal; i++) {
                final int finalI = i;
                db.collection(Constants.BD_INGRESOS).document(Auth.INSTANCE.uidCurrentUser())
                        .collection(yearSelected + "-" + i).document(id)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Delete", "DocumentSnapshot successfully deleted!");
                            if (finalI == mesFinal) {
                                if (fromSearch) {
                                    cargarIngresos();
                                }
                                Log.d("Delete", "DocumentSnapshot successfully deleted, all them!");
                            }
                        })
                        .addOnFailureListener(e -> Log.w("Delete", "Error deleting document", e));
            }
        } else {
            db.collection(Constants.BD_INGRESOS).document(Auth.INSTANCE.uidCurrentUser())
                    .collection(yearSelected + "-" + mesSelected).document(id)
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d("Delete", "DocumentSnapshot successfully deleted!"))
                    .addOnFailureListener(e -> Log.w("Delete", "Error deleting document", e));
        }

    }

    private void editarItem(String id, String tipoFrecuencia) {
        Intent myIntent = new Intent(getContext(), EditarActivity.class);
        Bundle myBundle = new Bundle();
        myBundle.putString("idDoc", id);
        myBundle.putInt("fragment", 0);
        myBundle.putInt("mes", mesSelected);
        myBundle.putInt("year", yearSelected);

        myBundle.putBoolean("mesUnico", tipoFrecuencia == null);

        myIntent.putExtras(myBundle);
        startActivity(myIntent);
    }


    public void buscarItem(String text) {
        if (listaIngresos != null) {
            if (listaIngresos.isEmpty()) {
                Toast.makeText(getContext(), "No hay lista cargada", Toast.LENGTH_SHORT).show();
            } else {
                String userInput = text.toLowerCase();
                newList = new ArrayList<>();

                for (IngresosGastosConstructor name : listaIngresos) {
                    if (name.getConcepto().toLowerCase().contains(userInput)) {
                        newList.add(name);
                    }
                }
                if (newList.isEmpty()) {
                    lottieAnimationView.setVisibility(View.VISIBLE);
                    lottieAnimationView.playAnimation();
                }
                ingresosAdapter.updateList(newList);
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
                toolbar.setTitle(getString(R.string.pie_ingresos));
                itemBuscar.setVisible(true);
            }, 300);
        }
        cargarIngresos();
    }

    @Override
    public void statusListaIngresos(boolean statusOk, @NotNull ArrayList<IngresosGastosConstructor> ingresos, @NotNull String message) {
        if (statusOk) {
            listaIngresos.clear();
            listaIngresos = ingresos;
            ingresosAdapter.updateList(listaIngresos);

            if (listaIngresos.isEmpty()) {
                tvSinLista.setVisibility(View.VISIBLE);
            } else {
                tvSinLista.setVisibility(View.GONE);
            }

            progressBar.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show();
        }

        fragmentCreado = false;
    }

    @Override
    public void statusSuspenderMes(boolean statusOk) {
        if (statusOk) {
            cargarIngresos();
        } else {
            Toast.makeText(getContext(), getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show();
        }
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
