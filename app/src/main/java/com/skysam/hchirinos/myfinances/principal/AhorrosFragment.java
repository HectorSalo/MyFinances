package com.skysam.hchirinos.myfinances.principal;

import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.skysam.hchirinos.myfinances.Utils.VariablesEstaticas;
import com.skysam.hchirinos.myfinances.adaptadores.AhorrosAdapter;
import com.skysam.hchirinos.myfinances.adaptadores.IngresosAdapter;
import com.skysam.hchirinos.myfinances.constructores.AhorrosConstructor;
import com.skysam.hchirinos.myfinances.constructores.IngresosConstructor;

import java.util.ArrayList;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class AhorrosFragment extends Fragment {

    private RecyclerView recyclerView;
    private AhorrosAdapter ahorrosAdapter;
    private ArrayList<AhorrosConstructor> listaAhorros;
    private ProgressBar progressBar;
    private TextView tvSinLista;
    private boolean fragmentCreado;
    private CoordinatorLayout coordinatorLayout;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


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

        progressBar = view.findViewById(R.id.progressBar_ahorros);
        tvSinLista = view.findViewById(R.id.textView_sin_lista);
        coordinatorLayout = view.findViewById(R.id.coordinator_snackbar);

        recyclerView = view.findViewById(R.id.rv_ahorros);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemSwipe);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        fragmentCreado = true;

        cargarAhorros();
    }


    private ItemTouchHelper.SimpleCallback itemSwipe = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            AhorrosConstructor itemSwipe = new AhorrosConstructor();
            itemSwipe = listaAhorros.get(position);

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
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeRightActionIcon(R.drawable.ic_delete_item)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_red_A700))
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

        CollectionReference reference = db.collection(VariablesEstaticas.BD_PROPIETARIOS).document(userID).collection(VariablesEstaticas.BD_AHORROS);

        Query query = reference.orderBy(VariablesEstaticas.BD_MONTO, Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                        AhorrosConstructor ahorro = new AhorrosConstructor();

                        ahorro.setConcepto(doc.getString(VariablesEstaticas.BD_CONCEPTO));
                        ahorro.setOrigen(doc.getString(VariablesEstaticas.BD_ORIGEN));
                        ahorro.setDolar(doc.getBoolean(VariablesEstaticas.BD_DOLAR));
                        ahorro.setMonto(doc.getDouble(VariablesEstaticas.BD_MONTO));
                        ahorro.setFechaIngreso(doc.getDate(VariablesEstaticas.BD_FECHA_INGRESO));

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
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!fragmentCreado) {
            cargarAhorros();
        }
        fragmentCreado = false;

    }
}
