package com.skysam.hchirinos.myfinances.principal;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.adaptadores.IngresosAdapter;
import com.skysam.hchirinos.myfinances.constructores.IngresosConstructor;

import java.util.ArrayList;


public class IngresosFragment extends Fragment {

    private RecyclerView recyclerView;
    private IngresosAdapter ingresosAdapter;
    private ArrayList<IngresosConstructor> listaIngresos;


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

        ingresosAdapter = new IngresosAdapter(listaIngresos, getContext());
        recyclerView = view.findViewById(R.id.rv_ingresos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        listaIngresos = new ArrayList<>();
        return view;
    }
}
