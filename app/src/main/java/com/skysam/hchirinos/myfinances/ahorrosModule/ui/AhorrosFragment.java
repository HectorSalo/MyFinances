package com.skysam.hchirinos.myfinances.ahorrosModule.ui;

import android.graphics.Canvas;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.common.model.constructores.AhorrosConstructor;
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class AhorrosFragment extends Fragment {

    private RecyclerView recyclerView;
    private AhorrosAdapter ahorrosAdapter;
    private ArrayList<AhorrosConstructor> listaAhorros, newList;
    private ProgressBar progressBar;
    private TextView tvSinLista;
    private LottieAnimationView lottieAnimationView;
    private boolean fragmentCreado;
    private boolean fromSearch = false;
    private CoordinatorLayout coordinatorLayout;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private int mesSelected, yearSelected;
    private Toolbar toolbar;
    private MenuItem itemBuscar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        configurarToolbar();
        return inflater.inflate(R.layout.fragment_ahorros, container, false);
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

        progressBar = view.findViewById(R.id.progressBar_ahorros);
        tvSinLista = view.findViewById(R.id.textView_sin_lista);
        coordinatorLayout = view.findViewById(R.id.coordinator_snackbar);
        lottieAnimationView = view.findViewById(R.id.lottieAnimationView);

        listaAhorros = new ArrayList<>();

        recyclerView = view.findViewById(R.id.rv_ahorros);

        ahorrosAdapter = new AhorrosAdapter(listaAhorros, getContext(), requireActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(ahorrosAdapter);

        Spinner spinnerMes = view.findViewById(R.id.spinner_ahorro_mes);
        Spinner spinnerYear = view.findViewById(R.id.spinner_ahorro_year);

        List<String> listaMeses = Arrays.asList(getResources().getStringArray(R.array.meses));
        ArrayAdapter<String> adapterMeses = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, listaMeses);
        spinnerMes.setAdapter(adapterMeses);

        List<String> listaYear = Arrays.asList(getResources().getStringArray(R.array.years));
        ArrayAdapter<String> adapterYear = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, listaYear);
        spinnerYear.setAdapter(adapterYear);

        fragmentCreado = true;

        switch (yearSelected) {
            case 2020:
                spinnerYear.setSelection(0);
                break;
            case 2021:
                spinnerYear.setSelection(1);
                break;
            case 2022:
                spinnerYear.setSelection(2);
                break;
        }

        spinnerMes.setSelection(mesSelected);
        spinnerMes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mesSelected = position;
                if (!fragmentCreado) {
                    cargarAhorros();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        yearSelected = 2020;
                        break;
                    case 1:
                        yearSelected = 2021;
                        break;
                    case 2:
                        yearSelected = 2022;
                        break;
                }
                if (!fragmentCreado) {
                    cargarAhorros();
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
    }


    private final ItemTouchHelper.SimpleCallback itemSwipe = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();

            switch (direction) {
                case ItemTouchHelper.RIGHT:
                    ArrayList<AhorrosConstructor> itemToRestored;
                    final AhorrosConstructor finalItemSwipe;
                    if (newList == null || newList.isEmpty()) {
                        fromSearch = false;
                        AhorrosConstructor itemSwipe = listaAhorros.get(position);
                        listaAhorros.remove(position);
                        ahorrosAdapter.updateList(listaAhorros);
                        itemToRestored = new ArrayList<>();
                        itemToRestored.add(itemSwipe);

                        finalItemSwipe = itemSwipe;

                        final Snackbar snackbar = Snackbar.make(coordinatorLayout, itemSwipe.getConcepto() + " borrado", Snackbar.LENGTH_LONG)
                                .setAction("Deshacer", view -> {
                            listaAhorros.add(position, finalItemSwipe);
                            ahorrosAdapter.updateList(listaAhorros);
                            itemToRestored.clear();
                        });
                        snackbar.show();
                    } else {
                        fromSearch = true;
                        AhorrosConstructor itemSwipe = newList.get(position);
                        newList.remove(position);
                        ahorrosAdapter.updateList(newList);
                        itemToRestored = new ArrayList<>();
                        itemToRestored.add(itemSwipe);

                        finalItemSwipe = itemSwipe;

                        final Snackbar snackbar = Snackbar.make(coordinatorLayout, itemSwipe.getConcepto() + " borrado", Snackbar.LENGTH_LONG)
                                .setAction("Deshacer", view -> {
                                    newList.add(position, finalItemSwipe);
                                    ahorrosAdapter.updateList(newList);
                                    itemToRestored.clear();
                                });
                        snackbar.show();
                    }

                    new Handler(Looper.myLooper()).postDelayed(() -> {
                        if (!itemToRestored.isEmpty()) {
                            deleteItemSwipe(finalItemSwipe.getIdAhorro());
                        }
                    }, 3500);
                break;
                case ItemTouchHelper.LEFT:
                    if (newList != null) {
                        String monto;
                        final String idDoc;
                        final boolean dolar;
                        if (newList.isEmpty()) {
                            monto = String.valueOf(listaAhorros.get(position).getMonto());
                            idDoc = listaAhorros.get(position).getIdAhorro();
                            dolar = listaAhorros.get(position).isDolar();
                        } else {
                            monto = String.valueOf(newList.get(position).getMonto());
                            idDoc = newList.get(position).getIdAhorro();
                            dolar = newList.get(position).isDolar();
                        }
                        editarItem(monto, idDoc, dolar);
                    } else {
                        String monto = String.valueOf(listaAhorros.get(position).getMonto());
                        final String idDoc = listaAhorros.get(position).getIdAhorro();
                        final boolean dolar = listaAhorros.get(position).isDolar();
                        editarItem(monto, idDoc, dolar);
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

    private void cargarAhorros() {
        progressBar.setVisibility(View.VISIBLE);
        String userID = user.getUid();
        listaAhorros = new ArrayList<>();
        ahorrosAdapter = new AhorrosAdapter(listaAhorros, getContext(), requireActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(ahorrosAdapter);

        CollectionReference reference = db.collection(Constants.BD_AHORROS).document(userID).collection(yearSelected + "-" + mesSelected);

        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listaAhorros.clear();
                for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                    AhorrosConstructor ahorro = new AhorrosConstructor();

                    ahorro.setIdAhorro(doc.getId());
                    ahorro.setConcepto(doc.getString(Constants.BD_CONCEPTO));
                    ahorro.setOrigen(doc.getString(Constants.BD_ORIGEN));
                    ahorro.setDolar(doc.getBoolean(Constants.BD_DOLAR));
                    ahorro.setMonto(doc.getDouble(Constants.BD_MONTO));
                    ahorro.setFechaIngreso(doc.getDate(Constants.BD_FECHA_INGRESO));

                    listaAhorros.add(ahorro);

                }
                ahorrosAdapter.updateList(listaAhorros);
                if (listaAhorros.isEmpty()) {
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

    private void editarItem(String monto, final String idDoc, boolean dolar) {
        LayoutInflater inflater = LayoutInflater.from(coordinatorLayout.getContext());
        View v = inflater.inflate(R.layout.layout_editar_ahorro, null);
        final TextInputEditText textInputEditText = v.findViewById(R.id.et_monto);
        final RadioButton radioButtonDolar = v.findViewById(R.id.radioButton_dolares);
        RadioButton radioButtonBolivares = v.findViewById(R.id.radioButton_bolivares);
        textInputEditText.setText(monto);
        if (dolar) {
            radioButtonDolar.setChecked(true);
        } else {
            radioButtonBolivares.setChecked(true);
        }


        AlertDialog.Builder dialog = new AlertDialog.Builder(coordinatorLayout.getContext());
        dialog.setTitle("Ingrese el nuevo monto")
                .setView(v)
                .setCancelable(false)
                .setPositiveButton("Actualizar", (dialog1, which) -> {
                    if (!textInputEditText.getText().toString().isEmpty()) {
                        double valor = Double.parseDouble(textInputEditText.getText().toString());
                        if (valor > 0) {
                            boolean dolarEnviar;
                            dolarEnviar = radioButtonDolar.isChecked();
                            guardarItem(dolarEnviar, valor, idDoc);
                            Toast.makeText(getContext(), "Actualizando...", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "El valor ingresado no puede ser cero", Toast.LENGTH_SHORT).show();
                            cargarAhorros();
                        }
                    } else {
                        Toast.makeText(getContext(), "El campo no puede estar vacío", Toast.LENGTH_SHORT).show();
                        cargarAhorros();
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancelar), (dialog12, which) -> cargarAhorros()).show();
    }


    private void guardarItem(boolean dolar, double monto, String idDoc) {
        progressBar.setVisibility(View.VISIBLE);
        for (int i = mesSelected; i < 12; i++) {
            final int finalI = i;
            db.collection(Constants.BD_AHORROS).document(user.getUid()).collection(yearSelected + "-" + i).document(idDoc)
                    .update(Constants.BD_DOLAR, dolar, Constants.BD_MONTO, monto)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                        if (finalI == 11) {
                            cargarAhorros();
                            Toast.makeText(getContext(), getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error updating document", e);
                        if (finalI > mesSelected) {
                            Toast.makeText(getContext(), getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), getString(R.string.error_guardar_data), Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                        cargarAhorros();
                    });
        }
    }

    private void deleteItemSwipe(String id) {
        for (int i = mesSelected; i < 12; i++) {
            final int finalI = i;
            db.collection(Constants.BD_AHORROS).document(user.getUid()).collection(yearSelected + "-" + i).document(id)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Delete", "DocumentSnapshot successfully deleted!");
                        if (finalI == 11) {
                            if (fromSearch) {
                                cargarAhorros();
                            }
                            Log.d("Delete", "DocumentSnapshot successfully deleted, all them!");
                        }
                    })
                    .addOnFailureListener(e -> Log.w("Delete", "Error deleting document", e));
        }

    }


    public void buscarItem(String text) {
        if (listaAhorros != null) {
            if (listaAhorros.isEmpty()) {
                Toast.makeText(getContext(), "No hay lista cargada", Toast.LENGTH_SHORT).show();
            } else {
                String userInput = text.toLowerCase();
                newList = new ArrayList<>();

                for (AhorrosConstructor name : listaAhorros) {
                    if (name.getConcepto().toLowerCase().contains(userInput)) {
                        newList.add(name);
                    }
                }
                if (newList.isEmpty()) {
                    lottieAnimationView.setVisibility(View.VISIBLE);
                    lottieAnimationView.playAnimation();
                }
                ahorrosAdapter.updateList(newList);
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
                toolbar.setTitle(getString(R.string.pie_ahorros));
                itemBuscar.setVisible(true);
            }, 300);
        }
        cargarAhorros();
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
