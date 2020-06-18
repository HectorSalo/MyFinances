package com.skysam.hchirinos.myfinances.principal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.adaptadores.ListasAdapter;
import com.skysam.hchirinos.myfinances.constructores.ListasConstructor;

import java.util.ArrayList;

public class ListaGastosActivity extends AppCompatActivity {

    private RecyclerView recyclerListas;
    private ListasAdapter listasAdapter;
    private ArrayList<ListasConstructor> listListas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_gastos);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listListas = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        recyclerListas = findViewById(R.id.rv_listas);
        listasAdapter = new ListasAdapter(listListas, this);
        recyclerListas.setLayoutManager(layoutManager);
        recyclerListas.setHasFixedSize(true);
        recyclerListas.setAdapter(listasAdapter);

        cargarListas();
    }

    private void cargarListas() {

        for (int j = 0; j < 5; j++) {
            ListasConstructor list = new ListasConstructor();
            list.setNombreLista("Mercado" + j);
            list.setCantidadItems(j);

            listListas.add(list);
        }

        listasAdapter.updateList(listListas);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }


}