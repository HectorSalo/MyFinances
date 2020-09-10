package com.skysam.hchirinos.myfinances.ui.inicio;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.skysam.hchirinos.myfinances.adaptadores.IngresosAdapter;
import com.skysam.hchirinos.myfinances.constructores.IngresosConstructor;
import com.skysam.hchirinos.myfinances.ui.editar.EditarActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class IngresosFragment extends Fragment {

    private RecyclerView recyclerView;
    private IngresosAdapter ingresosAdapter;
    private ArrayList<IngresosConstructor> listaIngresos, newList;
    private ProgressBar progressBar;
    private TextView tvSinLista;
    private boolean fragmentCreado;
    private CoordinatorLayout coordinatorLayout;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private int mesSelected, yearSelected;


    public IngresosFragment() {
        // Required empty public constructor
    }

    public static IngresosFragment newInstance(String param1, String param2) {
        IngresosFragment fragment = new IngresosFragment();
        return fragment;
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

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().
                        beginTransaction().replace(R.id.container_fragments, new HomeFragment(), "home").commit();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        fragmentCreado = true;

        Calendar calendar = Calendar.getInstance();
        yearSelected = calendar.get(Calendar.YEAR);
        mesSelected = calendar.get(Calendar.MONTH);

        recyclerView = view.findViewById(R.id.rv_ingresos);

        listaIngresos = new ArrayList<>();
        ingresosAdapter = new IngresosAdapter(listaIngresos, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(ingresosAdapter);

        progressBar = view.findViewById(R.id.progressBar_ingresos);
        tvSinLista = view.findViewById(R.id.textView_sin_lista);
        coordinatorLayout = view.findViewById(R.id.coordinator_snackbar);
        Spinner spinner = view.findViewById(R.id.spinner_ingreso);

        List<String> listaMeses = Arrays.asList(getResources().getStringArray(R.array.meses));
        ArrayAdapter<String> adapterMeses = new ArrayAdapter<String>(getContext(), R.layout.layout_spinner, listaMeses);
        spinner.setAdapter(adapterMeses);

        spinner.setSelection(mesSelected);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemSwipe);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        return view;
    }


    private ItemTouchHelper.SimpleCallback itemSwipe = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            IngresosConstructor itemSwipe;
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
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_red_A700))
                    .addSwipeLeftActionIcon(R.drawable.ic_edit_item_24dp)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_orange_A700))
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    };

    private void cargarIngresos() {
        progressBar.setVisibility(View.VISIBLE);
        String userID = user.getUid();

        listaIngresos = new ArrayList<>();

        CollectionReference reference = db.collection(Constantes.BD_INGRESOS).document(userID).collection(yearSelected + "-" + mesSelected);

        Query query = reference.orderBy(Constantes.BD_MONTO, Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                        IngresosConstructor ingreso = new IngresosConstructor();
                        ingreso.setIdIngreso(doc.getId());
                        ingreso.setConcepto(doc.getString(Constantes.BD_CONCEPTO));
                        ingreso.setMonto(doc.getDouble(Constantes.BD_MONTO));
                        ingreso.setDolar(doc.getBoolean(Constantes.BD_DOLAR));

                        Boolean activo = doc.getBoolean(Constantes.BD_MES_ACTIVO);
                        if (activo == null) {
                            ingreso.setMesActivo(true);
                        } else {
                            ingreso.setMesActivo(activo);
                        }

                        String tipoFrecuencia = doc.getString(Constantes.BD_TIPO_FRECUENCIA);
                        if (tipoFrecuencia != null) {
                            double duracionFrecuencia = doc.getDouble(Constantes.BD_DURACION_FRECUENCIA);
                            int duracionFrecuenciaInt = (int) duracionFrecuencia;
                            ingreso.setDuracionFrecuencia(duracionFrecuenciaInt);
                            ingreso.setFechaIncial(doc.getDate(Constantes.BD_FECHA_INCIAL));
                            ingreso.setTipoFrecuencia(doc.getString(Constantes.BD_TIPO_FRECUENCIA));
                        } else {
                            ingreso.setTipoFrecuencia(null);
                        }

                        listaIngresos.add(ingreso);

                    }
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
        });

    }

    private void crearDialog(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("¿Qué desea hacer?")
                .setItems(R.array.opciones_borrar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                suspenderMes(position);
                                break;
                            case 1:
                                eliminarDefinitivo(position);
                                break;
                        }
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancelar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cargarIngresos();
                    }
                }).show();
    }

    private void suspenderMes(int position) {
        db.collection(Constantes.BD_INGRESOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected).document(listaIngresos.get(position).getIdIngreso())
                .update(Constantes.BD_MES_ACTIVO, false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        cargarIngresos();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), getString(R.string.error_cargar_data), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void eliminarDefinitivo(final int position) {
        final IngresosConstructor itemSwipe = listaIngresos.get(position);

        listaIngresos.remove(position);
        ingresosAdapter.updateList(listaIngresos);

        Snackbar snackbar = Snackbar.make(coordinatorLayout, itemSwipe.getConcepto() + " borrado", Snackbar.LENGTH_LONG).setAction("Deshacer", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listaIngresos.add(position, itemSwipe);
                ingresosAdapter.updateList(listaIngresos);
            }
        });
        snackbar.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!listaIngresos.contains(itemSwipe)) {
                    deleteItemSwipe(itemSwipe.getIdIngreso());
                }
            }
        }, 4500);
    }


    private void deleteItemSwipe(String id) {
        for (int i = mesSelected; i < 12; i++) {
            final int finalI = i;
            db.collection(Constantes.BD_INGRESOS).document(user.getUid()).collection(yearSelected + "-" + i).document(id)
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

    }

    private void editarItem(String id, String tipoFrecuencia) {
        Intent myIntent = new Intent(getContext(), EditarActivity.class);
        Bundle myBundle = new Bundle();
        myBundle.putString("idDoc", id);
        myBundle.putInt("fragment", 0);
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
        if (listaIngresos.isEmpty()) {
            Toast.makeText(getContext(), "No hay lista cargada", Toast.LENGTH_SHORT).show();
        } else {
            String userInput = text.toLowerCase();
            newList = new ArrayList<>();

            for (IngresosConstructor name : listaIngresos) {

                if (name.getConcepto().toLowerCase().contains(userInput)) {
                    newList.add(name);
                }
            }

            ingresosAdapter.updateList(newList);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarIngresos();
    }
}
