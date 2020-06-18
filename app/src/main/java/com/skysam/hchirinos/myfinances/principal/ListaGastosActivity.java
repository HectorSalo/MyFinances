package com.skysam.hchirinos.myfinances.principal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.adaptadores.ItemGastoAdapter;
import com.skysam.hchirinos.myfinances.adaptadores.ListasAdapter;
import com.skysam.hchirinos.myfinances.constructores.ItemGastosConstructor;
import com.skysam.hchirinos.myfinances.constructores.ListasConstructor;

import java.util.ArrayList;

public class ListaGastosActivity extends AppCompatActivity {

    private RecyclerView recyclerListas, recyclerItems;
    private ListasAdapter listasAdapter;
    private ItemGastoAdapter itemGastoAdapter;
    private ArrayList<ItemGastosConstructor> listItems;
    private ArrayList<ListasConstructor> listListas;
    private TextView tvSinListas, tvSinItems;
    private String idLista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_gastos);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvSinListas = findViewById(R.id.tv_sin_listas);
        tvSinItems = findViewById(R.id.tv_sin_items);

        listListas = new ArrayList<>();
        listItems = new ArrayList<>();

        LinearLayoutManager layoutManagerlistas = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManageritems = new LinearLayoutManager(this);

        recyclerListas = findViewById(R.id.rv_listas);
        listasAdapter = new ListasAdapter(listListas, this);
        recyclerListas.setLayoutManager(layoutManagerlistas);
        recyclerListas.setHasFixedSize(true);
        recyclerListas.setAdapter(listasAdapter);

        recyclerItems = findViewById(R.id.rv_items_lista);
        itemGastoAdapter = new ItemGastoAdapter(listItems, this);
        recyclerItems.setLayoutManager(layoutManageritems);
        recyclerItems.setHasFixedSize(true);
        recyclerItems.setAdapter(itemGastoAdapter);

        idLista = null;

        Button buttonNuevoItem = findViewById(R.id.button_nuevo_item);
        buttonNuevoItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idLista != null) {

                } else {
                    Toast.makeText(getApplicationContext(), "Debe seleccionar una lista para agregar un ítem.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvSinItems.setText(getResources().getString(R.string.sin_lista_seleccionada));
        tvSinItems.setVisibility(View.VISIBLE);

        cargarListas();
    }

    private void cargarListas() {

        for (int j = 0; j < 5; j++) {
            ListasConstructor list = new ListasConstructor();
            list.setNombreLista("Mercado" + j);
            list.setCantidadItems(j);

            listListas.add(list);
        }
        listListas.clear();

        if (listListas.isEmpty()) {
            tvSinListas.setVisibility(View.VISIBLE);
            recyclerListas.setVisibility(View.GONE);
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