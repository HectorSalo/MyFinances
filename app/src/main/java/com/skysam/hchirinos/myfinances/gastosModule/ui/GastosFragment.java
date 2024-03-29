package com.skysam.hchirinos.myfinances.gastosModule.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.common.model.constructores.IngresosGastosConstructor;
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeActivity;
import com.skysam.hchirinos.myfinances.ui.activityGeneral.EditarActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class GastosFragment extends Fragment {

    private RecyclerView recyclerView;
    private GastosAdapter gastosAdapter;
    private ArrayList<IngresosGastosConstructor> listaGastos, newList;
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


    public GastosFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        configurarToolbar();
        return inflater.inflate(R.layout.fragment_gastos, container, false);
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
        coordinatorLayout = view.findViewById(R.id.coordinator_snackbar);
        Spinner spinner = view.findViewById(R.id.spinner_gastos_mes);
        Spinner spinnerYear = view.findViewById(R.id.spinner_gastos_year);

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
                    cargarGastos();
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
                    cargarGastos();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        recyclerView = view.findViewById(R.id.rv_gastos);

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
    }


    private final ItemTouchHelper.SimpleCallback itemSwipe = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            IngresosGastosConstructor itemSwipe = listaGastos.get(position);

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
                        String id;
                        String tipoFrecuencia;
                        if (newList.isEmpty()) {
                            id = listaGastos.get(position).getIdGasto();
                            tipoFrecuencia = listaGastos.get(position).getTipoFrecuencia();
                        } else {
                            id = newList.get(position).getIdGasto();
                            tipoFrecuencia = newList.get(position).getTipoFrecuencia();
                        }
                        editarItem(id, tipoFrecuencia);
                    } else {
                        String id = listaGastos.get(position).getIdGasto();
                        String tipoFrecuencia = listaGastos.get(position).getTipoFrecuencia();
                        editarItem(id, tipoFrecuencia);
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


    private void cargarGastos() {
        progressBar.setVisibility(View.VISIBLE);
        listaGastos = new ArrayList<>();
        gastosAdapter = new GastosAdapter(listaGastos, getContext(), requireActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(gastosAdapter);

        CollectionReference reference = db.collection(Constants.BD_GASTOS).document(Auth.INSTANCE.uidCurrentUser())
                .collection(yearSelected + "-" + mesSelected);

        Query query = reference.orderBy(Constants.BD_FECHA_INCIAL, Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listaGastos.clear();
                for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                    boolean perteneceMes = true;
                    Calendar calendarPago = Calendar.getInstance();
                    IngresosGastosConstructor gasto = new IngresosGastosConstructor();
                    gasto.setIdGasto(doc.getId());
                    gasto.setConcepto(doc.getString(Constants.BD_CONCEPTO));
                    gasto.setMonto(doc.getDouble(Constants.BD_MONTO));
                    gasto.setDolar(doc.getBoolean(Constants.BD_DOLAR));
                    gasto.setFechaIncial(doc.getDate(Constants.BD_FECHA_INCIAL));
                    calendarPago.setTime(doc.getDate(Constants.BD_FECHA_INCIAL));
                    String tipoFrecuencia = doc.getString(Constants.BD_TIPO_FRECUENCIA);

                    Boolean activo = doc.getBoolean(Constants.BD_MES_ACTIVO);
                    if (activo == null) {
                        gasto.setMesActivo(true);
                    } else {
                        gasto.setMesActivo(activo);
                    }

                    Boolean pagado = doc.getBoolean(Constants.BD_PAGADO);
                    gasto.setPagado(pagado != null && pagado);

                    if (tipoFrecuencia != null) {
                        double duracionFrecuencia = doc.getDouble(Constants.BD_DURACION_FRECUENCIA);
                        int duracionFrecuenciaInt = (int) duracionFrecuencia;
                        gasto.setDuracionFrecuencia(duracionFrecuenciaInt);

                        gasto.setTipoFrecuencia(doc.getString(Constants.BD_TIPO_FRECUENCIA));

                        int mesPago = calendarPago.get(Calendar.MONTH);
                        int yearPago = calendarPago.get(Calendar.YEAR);

                        while (mesPago <= mesSelected && yearPago == yearSelected) {
                            perteneceMes = mesPago == mesSelected;

                            switch (tipoFrecuencia) {
                                case "Dias":
                                    calendarPago.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt);
                                    break;
                                case "Semanas":
                                    calendarPago.add(Calendar.DAY_OF_YEAR, duracionFrecuenciaInt * 7);
                                    break;
                                case "Meses":
                                    calendarPago.add(Calendar.MONTH, duracionFrecuenciaInt);
                                    break;
                            }

                            if (perteneceMes) {
                                mesPago += 12;
                            } else {
                                mesPago = calendarPago.get(Calendar.MONTH);
                                yearPago = calendarPago.get(Calendar.YEAR);
                            }
                        }


                    } else {
                        gasto.setTipoFrecuencia(null);
                    }

                    Date fechaFinal = doc.getDate(Constants.BD_FECHA_FINAL);
                    gasto.setFechaFinal(fechaFinal);

                    if (perteneceMes) {
                        listaGastos.add(gasto);
                    }

                }
                gastosAdapter.updateList(listaGastos);
                if (listaGastos.isEmpty()) {
                    tvSinLista.setVisibility(View.VISIBLE);
                } else {
                    tvSinLista.setVisibility(View.GONE);
                }
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error al cargar la lista. Intente nuevamente", Toast.LENGTH_SHORT).show();
            }
            fragmentCreado = false;
        });
    }


    private void crearDialog(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("¿Qué desea hacer?")
                .setItems(R.array.opciones_borrar, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            pagar(position);
                            break;
                        case 1:
                            suspenderMes(position);
                            break;
                        case 2:
                            eliminarDefinitivo(position);
                            break;
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancelar), (dialogInterface, i) -> cargarGastos()).show();
    }


    private void pagar(int position) {
        String id;
        if (newList == null || newList.isEmpty()) {
            id = listaGastos.get(position).getIdGasto();
        } else {
            id = newList.get(position).getIdGasto();
        }
        db.collection(Constants.BD_GASTOS).document(Auth.INSTANCE.uidCurrentUser())
                .collection(yearSelected + "-" + mesSelected).document(id)
                .update(Constants.BD_PAGADO, true)
                .addOnSuccessListener(aVoid -> cargarGastos())
                .addOnFailureListener(e -> Toast.makeText(getContext(), getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show());
    }


    private void suspenderMes(int position) {
        String id;
        if (newList == null || newList.isEmpty()) {
            id = listaGastos.get(position).getIdGasto();
        } else {
            id = newList.get(position).getIdGasto();
        }
        db.collection(Constants.BD_GASTOS).document(Auth.INSTANCE.uidCurrentUser())
                .collection(yearSelected + "-" + mesSelected).document(id)
                .update(Constants.BD_MES_ACTIVO, false)
                .addOnSuccessListener(aVoid -> cargarGastos())
                .addOnFailureListener(e -> Toast.makeText(getContext(), getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show());
    }

    private void eliminarDefinitivo(final int position) {
        final IngresosGastosConstructor itemSwipe;
        ArrayList<IngresosGastosConstructor> itemToResored;
        if (newList == null || newList.isEmpty()) {
            fromSearch = false;
            itemSwipe = listaGastos.get(position);
            listaGastos.remove(position);
            gastosAdapter.updateList(listaGastos);

            itemToResored = new ArrayList<>();
            itemToResored.add(itemSwipe);

            Snackbar snackbar = Snackbar.make(coordinatorLayout, itemSwipe.getConcepto() + " borrado", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer", view -> {
                        listaGastos.add(position, itemSwipe);
                        gastosAdapter.updateList(listaGastos);
                        itemToResored.clear();
                    });
            snackbar.show();
        } else {
            fromSearch = true;
            itemSwipe = newList.get(position);
            newList.remove(position);
            gastosAdapter.updateList(newList);

            itemToResored = new ArrayList<>();
            itemToResored.add(itemSwipe);

            Snackbar snackbar = Snackbar.make(coordinatorLayout, itemSwipe.getConcepto() + " borrado", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer", view -> {
                        newList.add(position, itemSwipe);
                        gastosAdapter.updateList(newList);
                        itemToResored.clear();
                    });
            snackbar.show();
        }

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (!itemToResored.isEmpty()) {
                deleteItemSwipe(itemSwipe.getIdGasto(), itemSwipe.getFechaFinal());
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
                db.collection(Constants.BD_GASTOS).document(Auth.INSTANCE.uidCurrentUser()).collection(yearSelected + "-" + i).document(id)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Delete", "DocumentSnapshot successfully deleted!");
                            if (finalI == mesFinal) {
                                if (fromSearch) {
                                    cargarGastos();
                                }
                                Log.d("Delete", "DocumentSnapshot successfully deleted, all them!");
                            }
                        })
                        .addOnFailureListener(e -> Log.w("Delete", "Error deleting document", e));
            }
        } else {
            db.collection(Constants.BD_GASTOS).document(Auth.INSTANCE.uidCurrentUser()).collection(yearSelected + "-" + mesSelected).document(id)
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d("Delete", "DocumentSnapshot successfully deleted!"))
                    .addOnFailureListener(e -> Log.w("Delete", "Error deleting document", e));
        }

    }

    private void editarItem(String id, String tipoFrecuencia) {
        Intent myIntent = new Intent(getContext(), EditarActivity.class);
        Bundle myBundle = new Bundle();
        myBundle.putString("idDoc", id);
        myBundle.putInt("fragment", 1);
        myBundle.putInt("mes", mesSelected);
        myBundle.putInt("year", yearSelected);

        myBundle.putBoolean("mesUnico", tipoFrecuencia == null);

        myIntent.putExtras(myBundle);
        startActivity(myIntent);
    }

    public void buscarItem(String text) {
        if (listaGastos != null) {
            if (listaGastos.isEmpty()) {
                Toast.makeText(getContext(), "No hay lista cargada", Toast.LENGTH_SHORT).show();
            } else {
                String userInput = text.toLowerCase();
                newList = new ArrayList<>();

                for (IngresosGastosConstructor name : listaGastos) {
                    if (name.getConcepto().toLowerCase().contains(userInput)) {
                        newList.add(name);
                    }
                }
                if (newList.isEmpty()) {
                    lottieAnimationView.setVisibility(View.VISIBLE);
                    lottieAnimationView.playAnimation();
                }
                gastosAdapter.updateList(newList);
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
                toolbar.setTitle(getString(R.string.pie_egresos));
                itemBuscar.setVisible(true);
            }, 300);
        }
        cargarGastos();
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
