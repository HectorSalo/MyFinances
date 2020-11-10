package com.skysam.hchirinos.myfinances.ahorrosModule.ui;

import android.content.DialogInterface;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.ahorrosModule.ui.AhorrosAdapter;
import com.skysam.hchirinos.myfinances.common.model.constructores.AhorrosConstructor;
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeFragment;

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
    private boolean fragmentCreado;
    private CoordinatorLayout coordinatorLayout;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private int mesSelected, yearSelected;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ahorros, container, false);
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

        progressBar = view.findViewById(R.id.progressBar_ahorros);
        tvSinLista = view.findViewById(R.id.textView_sin_lista);
        coordinatorLayout = view.findViewById(R.id.coordinator_snackbar);

        listaAhorros = new ArrayList<>();

        recyclerView = view.findViewById(R.id.rv_ahorros);

        ahorrosAdapter = new AhorrosAdapter(listaAhorros, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(ahorrosAdapter);

        Spinner spinner = view.findViewById(R.id.spinner_ahorro);

        List<String> listaMeses = Arrays.asList(getResources().getStringArray(R.array.meses));
        ArrayAdapter<String> adapterMeses = new ArrayAdapter<String>(getContext(), R.layout.layout_spinner, listaMeses);
        spinner.setAdapter(adapterMeses);

        fragmentCreado = true;

        spinner.setSelection(mesSelected);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemSwipe);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        fragmentCreado = true;
    }


    private ItemTouchHelper.SimpleCallback itemSwipe = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            AhorrosConstructor itemSwipe = listaAhorros.get(position);

            switch (direction) {
                case ItemTouchHelper.RIGHT:
                listaAhorros.remove(position);
                ahorrosAdapter.updateList(listaAhorros);

                final AhorrosConstructor finalItemSwipe = itemSwipe;

                final Snackbar snackbar = Snackbar.make(coordinatorLayout, itemSwipe.getConcepto() + " borrado", Snackbar.LENGTH_LONG).setAction("Deshacer", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listaAhorros.add(position, finalItemSwipe);
                        ahorrosAdapter.updateList(listaAhorros);
                    }
                });
                snackbar.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!listaAhorros.contains(finalItemSwipe)) {
                                deleteItemSwipe(finalItemSwipe.getIdAhorro());
                            }
                        }
                    }, 4500);
                break;
                case ItemTouchHelper.LEFT:
                    if (newList != null) {
                        if (newList.isEmpty()) {
                            String monto = String.valueOf(listaAhorros.get(position).getMonto());
                            final String idDoc = listaAhorros.get(position).getIdAhorro();
                            final boolean dolar = listaAhorros.get(position).isDolar();
                            editarItem(monto, idDoc, dolar);
                        } else {
                            String monto = String.valueOf(newList.get(position).getMonto());
                            final String idDoc = newList.get(position).getIdAhorro();
                            final boolean dolar = newList.get(position).isDolar();
                            editarItem(monto, idDoc, dolar);
                        }
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
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_red_A700))
                    .addSwipeLeftActionIcon(R.drawable.ic_edit_item_24dp)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_orange_A700))
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    };

    private void cargarAhorros() {
        progressBar.setVisibility(View.VISIBLE);
        String userID = user.getUid();
        listaAhorros = new ArrayList<>();
        ahorrosAdapter = new AhorrosAdapter(listaAhorros, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(ahorrosAdapter);

        CollectionReference reference = db.collection(Constants.BD_AHORROS).document(userID).collection(yearSelected + "-" + mesSelected);

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
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
            }
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
                .setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!textInputEditText.getText().toString().isEmpty()) {
                            double valor = Double.parseDouble(textInputEditText.getText().toString());
                            if (valor > 0) {
                                boolean dolarEnviar;
                                if (radioButtonDolar.isChecked()) {
                                    dolarEnviar = true;
                                } else {
                                    dolarEnviar = false;
                                }
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
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancelar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cargarAhorros();
                    }
                }).show();
    }


    private void guardarItem(boolean dolar, double monto, String idDoc) {
        progressBar.setVisibility(View.VISIBLE);
        for (int i = mesSelected; i < 12; i++) {
            final int finalI = i;
            db.collection(Constants.BD_AHORROS).document(user.getUid()).collection(yearSelected + "-" + i).document(idDoc)
                    .update(Constants.BD_DOLAR, dolar, Constants.BD_MONTO, monto)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                            if (finalI == 11) {
                                cargarAhorros();
                                Toast.makeText(getContext(), getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                            if (finalI > mesSelected) {
                                Toast.makeText(getContext(), getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), getString(R.string.error_guardar_data), Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
                            cargarAhorros();
                        }
                    });
        }
    }

    private void deleteItemSwipe(String id) {
        for (int i = mesSelected; i < 12; i++) {
            final int finalI = i;
            db.collection(Constants.BD_AHORROS).document(user.getUid()).collection(yearSelected + "-" + i).document(id)
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


    public void buscarItem(String text) {
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

            ahorrosAdapter.updateList(newList);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarAhorros();
    }
}
