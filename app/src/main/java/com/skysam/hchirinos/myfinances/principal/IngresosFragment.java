package com.skysam.hchirinos.myfinances.principal;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.skysam.hchirinos.myfinances.adaptadores.IngresosAdapter;
import com.skysam.hchirinos.myfinances.constructores.IngresosConstructor;

import java.util.ArrayList;
import java.util.Objects;


public class IngresosFragment extends Fragment {

    private RecyclerView recyclerView;
    private IngresosAdapter ingresosAdapter;
    private ArrayList<IngresosConstructor> listaIngresos;
    private ProgressBar progressBar;
    private TextView tvSinLista;
    private boolean fragmentCreado;


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

        progressBar = view.findViewById(R.id.progressBar_ingresos);
        tvSinLista = view.findViewById(R.id.textView_sin_lista);

        recyclerView = view.findViewById(R.id.rv_ingresos);

        fragmentCreado = true;

        cargarIngresos();

        return view;
    }

    private void cargarIngresos() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        progressBar.setVisibility(View.VISIBLE);
        String userID = user.getUid();
        listaIngresos = new ArrayList<>();
        ingresosAdapter = new IngresosAdapter(listaIngresos, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(ingresosAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection(VariablesEstaticas.BD_PROPIETARIOS).document(userID).collection(VariablesEstaticas.BD_INGRESOS);

        Query query = reference.orderBy(VariablesEstaticas.BD_MONTO, Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                        IngresosConstructor ingreso = new IngresosConstructor();
                        ingreso.setIdIngreso(doc.getId());
                        ingreso.setConcepto(doc.getString(VariablesEstaticas.BD_CONCEPTO));
                        ingreso.setMonto(doc.getDouble(VariablesEstaticas.BD_MONTO));
                        ingreso.setDolar(doc.getBoolean(VariablesEstaticas.BD_DOLAR));

                        double duracionFrecuencia = doc.getDouble(VariablesEstaticas.BD_DURACION_FRECUENCIA);
                        int duracionFrecuenciaInt = (int) duracionFrecuencia;
                        ingreso.setDuracionFrecuencia(duracionFrecuenciaInt);
                        ingreso.setFechaIncial(doc.getDate(VariablesEstaticas.BD_FECHA_INCIAL));
                        ingreso.setTipoFrecuencia(doc.getString(VariablesEstaticas.BD_TIPO_FRECUENCIA));

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
                    Toast.makeText(getContext(), "Error al cargar la lista. Intente nuevamente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!fragmentCreado) {
            cargarIngresos();
        }
        fragmentCreado = false;

    }
}
