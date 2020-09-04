package com.skysam.hchirinos.myfinances.ui.inicio;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.Utils.Constantes;
import com.skysam.hchirinos.myfinances.adaptadores.GastosAdapter;
import com.skysam.hchirinos.myfinances.constructores.IngresosConstructor;
import com.skysam.hchirinos.myfinances.ui.editar.EditarActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class GastosFragment extends Fragment {

    private RecyclerView recyclerView;
    private GastosAdapter gastosAdapter;
    private ArrayList<IngresosConstructor> listaGastos, newList;
    private ProgressBar progressBar;
    private TextView tvSinLista;
    private boolean fragmentCreado;
    private CoordinatorLayout coordinatorLayout;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private int mesSelected, yearSelected;


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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gastos, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().
                        beginTransaction().replace(R.id.container_fragments, new HomeFragment(), "home").commit();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        Calendar calendar = Calendar.getInstance();
        yearSelected = calendar.get(Calendar.YEAR);
        mesSelected = calendar.get(Calendar.MONTH);

        progressBar = view.findViewById(R.id.progressBar);
        tvSinLista = view.findViewById(R.id.textView_sin_lista);
        coordinatorLayout = view.findViewById(R.id.coordinator_snackbar);
        Spinner spinner = view.findViewById(R.id.spinner_gastos);

        fragmentCreado = true;

        List<String> listaMeses = Arrays.asList(getResources().getStringArray(R.array.meses));
        ArrayAdapter<String> adapterMeses = new ArrayAdapter<String>(getContext(), R.layout.layout_spinner, listaMeses);
        spinner.setAdapter(adapterMeses);

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

        recyclerView = view.findViewById(R.id.rv_gastos);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemSwipe);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    private ItemTouchHelper.SimpleCallback itemSwipe = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            IngresosConstructor itemSwipe = listaGastos.get(position);

            switch (direction) {
                case ItemTouchHelper.RIGHT:

                    listaGastos.remove(position);
                    gastosAdapter.updateList(listaGastos);

                    final IngresosConstructor finalItemSwipe = itemSwipe;

                    final Snackbar snackbar = Snackbar.make(coordinatorLayout, itemSwipe.getConcepto() + " borrado", Snackbar.LENGTH_LONG).setAction("Deshacer", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listaGastos.add(position, finalItemSwipe);
                            gastosAdapter.updateList(listaGastos);
                        }
                    });
                    snackbar.show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!listaGastos.contains(finalItemSwipe)) {
                                deleteItemSwipe(position);
                            }
                        }
                    }, 4500);
                    break;
                case ItemTouchHelper.LEFT:
                    if (newList != null) {
                        if (newList.isEmpty()) {
                            String id = listaGastos.get(position).getIdGasto();
                            String tipoFrecuencia = listaGastos.get(position).getTipoFrecuencia();
                            editarItem(id, tipoFrecuencia);
                        } else {
                            String id = newList.get(position).getIdGasto();
                            String tipoFrecuencia = newList.get(position).getTipoFrecuencia();
                            editarItem(id, tipoFrecuencia);
                        }
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
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_red_A700))
                    .addSwipeLeftActionIcon(R.drawable.ic_edit_item_24dp)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_orange_A700))
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    };


    private void cargarGastos() {
        progressBar.setVisibility(View.VISIBLE);
        String userID = user.getUid();
        listaGastos = new ArrayList<>();
        gastosAdapter = new GastosAdapter(listaGastos, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(gastosAdapter);

        CollectionReference reference = db.collection(Constantes.BD_GASTOS).document(userID).collection(yearSelected + "-" + mesSelected);

        Query query = reference.orderBy(Constantes.BD_MONTO, Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                        IngresosConstructor gasto = new IngresosConstructor();
                        gasto.setIdGasto(doc.getId());
                        gasto.setConcepto(doc.getString(Constantes.BD_CONCEPTO));
                        gasto.setMonto(doc.getDouble(Constantes.BD_MONTO));
                        gasto.setDolar(doc.getBoolean(Constantes.BD_DOLAR));
                        gasto.setFechaIncial(doc.getDate(Constantes.BD_FECHA_INCIAL));
                        String tipoFrecuencia = doc.getString(Constantes.BD_TIPO_FRECUENCIA);

                        if (tipoFrecuencia != null) {
                            double duracionFrecuencia = doc.getDouble(Constantes.BD_DURACION_FRECUENCIA);
                            int duracionFrecuenciaInt = (int) duracionFrecuencia;
                            gasto.setDuracionFrecuencia(duracionFrecuenciaInt);

                            gasto.setTipoFrecuencia(doc.getString(Constantes.BD_TIPO_FRECUENCIA));
                        } else {
                            gasto.setTipoFrecuencia(null);
                        }

                        listaGastos.add(gasto);

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
            }
        });
    }


    private void deleteItemSwipe(int position) {
        String id = listaGastos.get(position).getIdGasto();
        String tipoFrecuencia = listaGastos.get(position).getTipoFrecuencia();
        if (tipoFrecuencia != null) {
            for (int i = mesSelected; i < 12; i++) {
                final int finalI = i;
                db.collection(Constantes.BD_GASTOS).document(user.getUid()).collection(yearSelected + "-" + i).document(id)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Delete", "DocumentSnapshot successfully deleted!");
                                if (finalI == 11) {
                                    Log.d("Delete", "DocumentSnapshot successfully deleted, all them!");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("Delete", "Error deleting document", e);
                            }
                        });
            }
        } else {
            db.collection(Constantes.BD_GASTOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected).document(id)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Delete", "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Delete", "Error deleting document", e);
                        }
                    });
        }

    }

    private void editarItem(String id, String tipoFrecuencia) {
        Intent myIntent = new Intent(getContext(), EditarActivity.class);
        Bundle myBundle = new Bundle();
        myBundle.putString("idDoc", id);
        myBundle.putInt("fragment", 1);
        myBundle.putInt("mes", mesSelected);
        myBundle.putInt("year", yearSelected);

        if (tipoFrecuencia != null) {
            myBundle.putBoolean("mesUnico", false);
        } else {
            myBundle.putBoolean("mesUnico", true);
        }

        myIntent.putExtras(myBundle);
        startActivity(myIntent);
    }

    public void buscarItem(String text) {
        if (listaGastos.isEmpty()) {
            Toast.makeText(getContext(), "No hay lista cargada", Toast.LENGTH_SHORT).show();
        } else {
            String userInput = text.toLowerCase();
            newList = new ArrayList<>();

            for (IngresosConstructor name : listaGastos) {

                if (name.getConcepto().toLowerCase().contains(userInput)) {
                    newList.add(name);
                }
            }
            gastosAdapter.updateList(newList);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarGastos();
    }

}
