package com.skysam.hchirinos.myfinances.principal;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.skysam.hchirinos.myfinances.adaptadores.PrestamosAdapter;
import com.skysam.hchirinos.myfinances.constructores.AhorrosConstructor;

import java.util.ArrayList;
import java.util.Objects;

public class PrestamosFragment extends Fragment {

    private RecyclerView recyclerView;
    private PrestamosAdapter prestamosAdapter;
    private ArrayList<AhorrosConstructor> listaPrestamos;
    private ProgressBar progressBar;
    private TextView tvSinLista;
    private boolean fragmentCreado;
    private CoordinatorLayout coordinatorLayout;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    public PrestamosFragment() {
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
        return inflater.inflate(R.layout.fragment_prestamos, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar_prestamos);
        tvSinLista = view.findViewById(R.id.textView_sin_lista);
        coordinatorLayout = view.findViewById(R.id.coordinator_snackbar);

        recyclerView = view.findViewById(R.id.rv_prestamos);

        fragmentCreado = true;

        cargarPrestamos();
    }


    private void cargarPrestamos() {
        progressBar.setVisibility(View.VISIBLE);
        String userID = user.getUid();
        listaPrestamos = new ArrayList<>();
        prestamosAdapter = new PrestamosAdapter(listaPrestamos, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(prestamosAdapter);

        CollectionReference reference = db.collection(VariablesEstaticas.BD_PROPIETARIOS).document(userID).collection(VariablesEstaticas.BD_AHORROS);

        Query query = reference.whereEqualTo(VariablesEstaticas.BD_PRESTAMO, true).orderBy(VariablesEstaticas.BD_FECHA_INGRESO, Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                        AhorrosConstructor prestamo = new AhorrosConstructor();

                        prestamo.setConcepto(doc.getString(VariablesEstaticas.BD_CONCEPTO));
                        prestamo.setDolar(doc.getBoolean(VariablesEstaticas.BD_DOLAR));
                        prestamo.setMonto(doc.getDouble(VariablesEstaticas.BD_MONTO));
                        prestamo.setFechaIngreso(doc.getDate(VariablesEstaticas.BD_FECHA_INGRESO));

                        listaPrestamos.add(prestamo);

                    }
                    prestamosAdapter.updateList(listaPrestamos);
                    if (listaPrestamos.isEmpty()) {
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
            cargarPrestamos();
        }
        fragmentCreado = false;

    }

}
