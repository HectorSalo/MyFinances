package com.skysam.hchirinos.myfinances.gastosModule.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.common.model.constructores.IngresosGastosConstructor;
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeFragment;
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
        Spinner spinner = view.findViewById(R.id.spinner_gastos_mes);
        Spinner spinnerYear = view.findViewById(R.id.spinner_gastos_year);

        fragmentCreado = true;

        List<String> listaMeses = Arrays.asList(getResources().getStringArray(R.array.meses));
        ArrayAdapter<String> adapterMeses = new ArrayAdapter<String>(getContext(), R.layout.layout_spinner, listaMeses);
        spinner.setAdapter(adapterMeses);

        List<String> listaYear = Arrays.asList(getResources().getStringArray(R.array.years));
        ArrayAdapter<String> adapterYears = new ArrayAdapter<String>(getContext(), R.layout.layout_spinner, listaYear);
        spinnerYear.setAdapter(adapterYears);


        if (yearSelected == 2020) {
            spinnerYear.setSelection(0);
        }
        if (yearSelected == 2021) {
            spinnerYear.setSelection(1);
        }

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
                switch (position) {
                    case 0:
                        yearSelected = 2020;
                        break;
                    case 1:
                        yearSelected = 2021;
                        break;
                }
                if (!fragmentCreado) {
                    cargarGastos();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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

        CollectionReference reference = db.collection(Constants.BD_GASTOS).document(userID).collection(yearSelected + "-" + mesSelected);

        Query query = reference.orderBy(Constants.BD_MONTO, Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                        IngresosGastosConstructor gasto = new IngresosGastosConstructor();
                        gasto.setIdGasto(doc.getId());
                        gasto.setConcepto(doc.getString(Constants.BD_CONCEPTO));
                        gasto.setMonto(doc.getDouble(Constants.BD_MONTO));
                        gasto.setDolar(doc.getBoolean(Constants.BD_DOLAR));
                        gasto.setFechaIncial(doc.getDate(Constants.BD_FECHA_INCIAL));
                        String tipoFrecuencia = doc.getString(Constants.BD_TIPO_FRECUENCIA);

                        Boolean activo = doc.getBoolean(Constants.BD_MES_ACTIVO);
                        if (activo == null) {
                            gasto.setMesActivo(true);
                        } else {
                            gasto.setMesActivo(activo);
                        }

                        if (tipoFrecuencia != null) {
                            double duracionFrecuencia = doc.getDouble(Constants.BD_DURACION_FRECUENCIA);
                            int duracionFrecuenciaInt = (int) duracionFrecuencia;
                            gasto.setDuracionFrecuencia(duracionFrecuenciaInt);

                            gasto.setTipoFrecuencia(doc.getString(Constants.BD_TIPO_FRECUENCIA));
                        } else {
                            gasto.setTipoFrecuencia(null);
                        }

                        Date fechaFinal = doc.getDate(Constants.BD_FECHA_FINAL);
                        if (fechaFinal != null) {
                            gasto.setFechaFinal(fechaFinal);
                        } else {
                            gasto.setFechaFinal(null);
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
                        cargarGastos();
                    }
                }).show();
    }


    private void suspenderMes(int position) {
        db.collection(Constants.BD_GASTOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected).document(listaGastos.get(position).getIdIngreso())
                .update(Constants.BD_MES_ACTIVO, false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        cargarGastos();
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
        final IngresosGastosConstructor itemSwipe = listaGastos.get(position);
        listaGastos.remove(position);
        gastosAdapter.updateList(listaGastos);

        Snackbar snackbar = Snackbar.make(coordinatorLayout, itemSwipe.getConcepto() + " borrado", Snackbar.LENGTH_LONG).setAction("Deshacer", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listaGastos.add(position, itemSwipe);
                gastosAdapter.updateList(listaGastos);
            }
        });
        snackbar.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!listaGastos.contains(itemSwipe)) {
                    deleteItemSwipe(itemSwipe.getIdGasto(), itemSwipe.getFechaFinal());
                }
            }
        }, 4500);
    }


    private void deleteItemSwipe(String id, Date fechaFinal) {
        if (fechaFinal != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fechaFinal);
            final int mesFinal = calendar.get(Calendar.MONTH);
            for (int i = mesSelected; i < mesFinal; i++) {
                final int finalI = i;
                db.collection(Constants.BD_GASTOS).document(user.getUid()).collection(yearSelected + "-" + i).document(id)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Delete", "DocumentSnapshot successfully deleted!");
                                if (finalI == mesFinal) {
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
            db.collection(Constants.BD_GASTOS).document(user.getUid()).collection(yearSelected + "-" + mesSelected).document(id)
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

            for (IngresosGastosConstructor name : listaGastos) {

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