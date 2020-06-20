package com.skysam.hchirinos.myfinances.principal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.errorprone.annotations.Var;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.Utils.VariablesEstaticas;
import com.skysam.hchirinos.myfinances.adaptadores.ItemGastoAdapter;
import com.skysam.hchirinos.myfinances.adaptadores.ListasAdapter;
import com.skysam.hchirinos.myfinances.constructores.IngresosConstructor;
import com.skysam.hchirinos.myfinances.constructores.ItemGastosConstructor;
import com.skysam.hchirinos.myfinances.constructores.ListasConstructor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ListaGastosActivity extends AppCompatActivity {

    private RecyclerView recyclerListas, recyclerItems;
    private ListasAdapter listasAdapter;
    private ItemGastoAdapter itemGastoAdapter;
    private ArrayList<ItemGastosConstructor> listItems;
    private ArrayList<ListasConstructor> listListas;
    private TextView tvSinListas, tvInfoLista;
    private String idLista, nombreLista;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Date fechaIngreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_gastos);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Calendar calendar = Calendar.getInstance();
        fechaIngreso = calendar.getTime();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        tvSinListas = findViewById(R.id.tv_sin_listas);
        tvInfoLista = findViewById(R.id.tv_info_lista);

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

        Button buttonNuevaLista = findViewById(R.id.button_nueva_lista);
        buttonNuevaLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearLista();
            }
        });

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

        tvInfoLista.setText(getResources().getString(R.string.sin_lista_seleccionada));

        cargarTodasListas(true);
    }

    private void cargarTodasListas(final boolean inicio) {
        String userID = user.getUid();

        listListas = new ArrayList<>();

        CollectionReference reference = db.collection(VariablesEstaticas.BD_LISTA_GASTOS).document(userID).collection(VariablesEstaticas.BD_TODAS_LISTAS);

        Query query = reference.orderBy(VariablesEstaticas.BD_FECHA_INGRESO, Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                        ListasConstructor lista = new ListasConstructor();

                        lista.setIdLista(doc.getId());
                        double cantidadD = doc.getDouble(VariablesEstaticas.BD_CANTIDAD_ITEMS);
                        int cantidad = (int) cantidadD;
                        lista.setCantidadItems(cantidad);
                        lista.setNombreLista(doc.getString(VariablesEstaticas.BD_NOMBRE));

                        listListas.add(lista);
                    }
                    listasAdapter.updateList(listListas);

                    if (listListas.isEmpty()) {
                        tvSinListas.setVisibility(View.VISIBLE);
                        recyclerListas.setVisibility(View.GONE);
                    } else {
                        tvSinListas.setVisibility(View.GONE);
                        recyclerListas.setVisibility(View.VISIBLE);

                        if (!inicio) {
                            idLista = listListas.get(0).getIdLista();
                            nombreLista = listListas.get(0).getNombreLista();
                            cargarLista();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error al cargar la lista. Intente nuevamente", Toast.LENGTH_SHORT).show();
                }
            }
        });

        listasAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lista = listListas.get(recyclerListas.getChildAdapterPosition(v)).getIdLista();
                nombreLista = listListas.get(recyclerListas.getChildAdapterPosition(v)).getNombreLista();

                if (!lista.equals(idLista)) {
                    idLista = lista;
                    tvInfoLista.setText(nombreLista);
                    cargarLista();
                }
            }
        });

    }

    private void crearLista() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText editText = new EditText(this);
        editText.setTextSize(24);
        editText.setPadding(50, 75, 5, 5);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editText.setHint("Nombre:");


        layout.addView(editText);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Ingrese nombre de la lista")
                .setView(layout)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nombre = editText.getText().toString();
                        if (nombre.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Error al guardar: El nombre no puede estar vacío", Toast.LENGTH_LONG).show();
                        } else {
                            guardarLista(nombre);
                        }
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    private void guardarLista(String nombre) {
        Map<String, Object> docData = new HashMap<>();
        docData.put(VariablesEstaticas.BD_NOMBRE, nombre);
        docData.put(VariablesEstaticas.BD_CANTIDAD_ITEMS, 0);
        docData.put(VariablesEstaticas.BD_FECHA_INGRESO, fechaIngreso);

            db.collection(VariablesEstaticas.BD_LISTA_GASTOS).document(user.getUid()).collection(VariablesEstaticas.BD_TODAS_LISTAS).document()
                    .set(docData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot written succesfully");
                            cargarTodasListas(false);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                            Toast.makeText(getApplicationContext(), "Error al guardar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                        }
                    });
    }

    private void cargarLista() {
        String userID = user.getUid();

        CollectionReference reference = db.collection(VariablesEstaticas.BD_LISTA_GASTOS).document(userID).collection(idLista);

        Query query = reference.orderBy(VariablesEstaticas.BD_FECHA_INGRESO, Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                        ItemGastosConstructor item = new ItemGastosConstructor();

                        item.setConcepto(doc.getString(VariablesEstaticas.BD_CONCEPTO));
                        item.setMontoAproximado(doc.getDouble(VariablesEstaticas.BD_MONTO));
                        item.setFechaIngreso(doc.getDate(VariablesEstaticas.BD_FECHA_INGRESO));
                        item.setFechaAproximada(doc.getDate(VariablesEstaticas.BD_FECHA_APROXIMADA));

                        listItems.add(item);
                    }
                    itemGastoAdapter.updateList(listItems);
                } else {
                    Toast.makeText(getApplicationContext(), "Error al cargar la lista. Intente nuevamente", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (listItems.isEmpty()) {
            String sinItems = getResources().getString(R.string.sin_items) + " " + nombreLista;
            tvInfoLista.setText(sinItems);
        } else {
            tvInfoLista.setText(nombreLista);
        }

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