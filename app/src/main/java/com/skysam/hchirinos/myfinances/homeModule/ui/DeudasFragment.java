package com.skysam.hchirinos.myfinances.homeModule.ui;

import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.deudasModule.ui.DeudasAdapter;
import com.skysam.hchirinos.myfinances.common.model.constructores.AhorrosConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;


public class DeudasFragment extends Fragment {

    public DeudasFragment() {
        // Required empty public constructor
    }

    private RecyclerView recyclerView;
    private DeudasAdapter deudasAdapter;
    private ArrayList<AhorrosConstructor> listaDeudas;
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
        return inflater.inflate(R.layout.fragment_deudas, container, false);
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

        Spinner spinner = view.findViewById(R.id.spinner);

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
                    cargarDeudas();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        recyclerView = view.findViewById(R.id.rv_deudas);
    }


    private void cargarDeudas() {
        progressBar.setVisibility(View.VISIBLE);
        String userID = user.getUid();
        listaDeudas = new ArrayList<>();
        deudasAdapter = new DeudasAdapter(listaDeudas, getContext(), yearSelected, mesSelected);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(deudasAdapter);

        CollectionReference reference = db.collection(Constants.BD_DEUDAS).document(userID).collection(yearSelected + "-" + mesSelected);

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                        AhorrosConstructor deuda = new AhorrosConstructor();

                        deuda.setIdDeuda(doc.getId());
                        deuda.setPrestamista(doc.getString(Constants.BD_PRESTAMISTA));
                        deuda.setConcepto(doc.getString(Constants.BD_CONCEPTO));
                        deuda.setDolar(doc.getBoolean(Constants.BD_DOLAR));
                        deuda.setMonto(doc.getDouble(Constants.BD_MONTO));
                        deuda.setFechaIngreso(doc.getDate(Constants.BD_FECHA_INGRESO));

                        listaDeudas.add(deuda);

                    }
                    deudasAdapter.updateList(listaDeudas);
                    if (listaDeudas.isEmpty()) {
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

    public void buscarItem(String text) {
        if (listaDeudas.isEmpty()) {
            Toast.makeText(getContext(), "No hay lista cargada", Toast.LENGTH_SHORT).show();
        } else {
            String userInput = text.toLowerCase();
            final ArrayList<AhorrosConstructor> newList = new ArrayList<>();

            for (AhorrosConstructor name : listaDeudas) {

                if (name.getConcepto().toLowerCase().contains(userInput) || name.getPrestamista().toLowerCase().contains(userInput)) {
                    newList.add(name);
                }
            }

            deudasAdapter.updateList(newList);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDeudas();
    }

}
