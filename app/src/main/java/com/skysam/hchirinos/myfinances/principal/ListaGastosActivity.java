package com.skysam.hchirinos.myfinances.principal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Constraints;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.Utils.Constantes;
import com.skysam.hchirinos.myfinances.adaptadores.ItemGastoAdapter;
import com.skysam.hchirinos.myfinances.adaptadores.ListasAdapter;
import com.skysam.hchirinos.myfinances.constructores.ItemGastosConstructor;
import com.skysam.hchirinos.myfinances.constructores.ListasConstructor;

import java.text.SimpleDateFormat;
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
    private TableLayout layoutItem;
    private TextView tvSinListas, tvInfoLista, tvFechaAproximada;
    private TextInputEditText etConcepto, etMonto;
    private TextInputLayout layoutConcepto, layoutMonto;
    private String idLista, idItem, nombreLista, conceptoViejo, concepto;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Date fechaIngreso, fechaSelec, fechaViejaAproximada;
    private Button buttonGuardarActualizar, buttonCancelar;
    private ImageButton imageButtonSelecFecha;
    private ProgressBar progressBarListas, progressBarItems;
    private int cantidadItems, positionLista;
    private double montoViejo, monto;
    private boolean nuevoItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_gastos);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Calendar calendar = Calendar.getInstance();
        fechaIngreso = calendar.getTime();

        fechaSelec = new Date();
        fechaSelec = null;

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        tvSinListas = findViewById(R.id.tv_sin_listas);
        tvInfoLista = findViewById(R.id.tv_info_lista);
        tvFechaAproximada = findViewById(R.id.textView_fecha_aproximada);
        layoutItem = findViewById(R.id.layout_crear_item);

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

        etConcepto = findViewById(R.id.et_concepto);
        etMonto = findViewById(R.id.et_monto);
        layoutConcepto = findViewById(R.id.outlined_concepto);
        layoutMonto = findViewById(R.id.outlined_monto);

        progressBarListas = findViewById(R.id.progressBar_listas);
        progressBarItems = findViewById(R.id.progressBar_items);

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
                    recyclerItems.setVisibility(View.GONE);
                    layoutItem.setVisibility(View.VISIBLE);
                    layoutConcepto.setEnabled(true);
                    layoutMonto.setEnabled(true);
                    buttonCancelar.setEnabled(true);
                    buttonGuardarActualizar.setEnabled(true);
                    imageButtonSelecFecha.setEnabled(true);
                    layoutMonto.setError(null);
                    layoutConcepto.setError(null);
                    etConcepto.setText("");
                    etMonto.setText("");
                    tvFechaAproximada.setText(getResources().getString(R.string.fecha_aproximada));
                    fechaSelec = null;
                    nuevoItem = true;
                    cantidadItems = listListas.get(positionLista).getCantidadItems();
                } else {
                    Toast.makeText(getApplicationContext(), "Debe seleccionar una lista para agregar un ítem.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageButtonSelecFecha = findViewById(R.id.imageButton);
        imageButtonSelecFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarFecha();
            }
        });

        buttonGuardarActualizar = findViewById(R.id.button_actualizar);
        buttonGuardarActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarDatosGuardar();
            }
        });

        buttonCancelar = findViewById(R.id.button_cancelar);
        buttonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerItems.setVisibility(View.VISIBLE);
                layoutItem.setVisibility(View.GONE);
            }
        });

        tvInfoLista.setText(getResources().getString(R.string.sin_lista_seleccionada));

        cargarTodasListas(true);
    }

    private void cargarTodasListas(final boolean inicio) {
        progressBarListas.setVisibility(View.VISIBLE);
        String userID = user.getUid();

        listListas = new ArrayList<>();

        CollectionReference reference = db.collection(Constantes.BD_LISTA_GASTOS).document(userID).collection(Constantes.BD_TODAS_LISTAS);

        Query query = reference.orderBy(Constantes.BD_FECHA_INGRESO, Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                        ListasConstructor lista = new ListasConstructor();

                        lista.setIdLista(doc.getId());
                        double cantidadD = doc.getDouble(Constantes.BD_CANTIDAD_ITEMS);
                        int cantidad = (int) cantidadD;
                        lista.setCantidadItems(cantidad);
                        lista.setNombreLista(doc.getString(Constantes.BD_NOMBRE));

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
                            cantidadItems = listListas.get(0).getCantidadItems();
                            positionLista = 0;
                            cargarLista();
                        }
                    }
                    progressBarListas.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getApplicationContext(), "Error al cargar la lista. Intente nuevamente", Toast.LENGTH_SHORT).show();
                    progressBarListas.setVisibility(View.GONE);
                }
            }
        });

        listasAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lista = listListas.get(recyclerListas.getChildAdapterPosition(v)).getIdLista();
                nombreLista = listListas.get(recyclerListas.getChildAdapterPosition(v)).getNombreLista();
                cantidadItems = listListas.get(recyclerListas.getChildAdapterPosition(v)).getCantidadItems();
                positionLista = recyclerListas.getChildLayoutPosition(v);
                recyclerItems.setVisibility(View.VISIBLE);
                layoutItem.setVisibility(View.GONE);
                layoutMonto.setError(null);
                layoutConcepto.setError(null);
                etConcepto.setText("");
                etMonto.setText("");

                if (!lista.equals(idLista)) {
                    idLista = lista;
                    cargarLista();
                }
            }
        });

        listasAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final View vista = v;
                AlertDialog.Builder dialog = new AlertDialog.Builder(ListaGastosActivity.this);
                dialog.setTitle("¿Qué desea hacer con la Lista?")
                        .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                eliminarLista(recyclerListas.getChildLayoutPosition(vista), vista);
                            }
                        }).setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setNegativeButton("Editar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editarLista(listListas.get(recyclerListas.getChildAdapterPosition(vista)).getNombreLista(), listListas.get(recyclerListas.getChildAdapterPosition(vista)).getIdLista(), recyclerListas.getChildLayoutPosition(vista));
                    }
                }).show();
                return true;
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
        docData.put(Constantes.BD_NOMBRE, nombre);
        docData.put(Constantes.BD_CANTIDAD_ITEMS, 0);
        docData.put(Constantes.BD_FECHA_INGRESO, fechaIngreso);

        db.collection(Constantes.BD_LISTA_GASTOS).document(user.getUid()).collection(Constantes.BD_TODAS_LISTAS).document()
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

    private void editarLista(final String nombreActual, final String idLista, final int i) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText editText = new EditText(this);
        editText.setTextSize(24);
        editText.setPadding(50, 75, 5, 5);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editText.setHint("Nombre:");
        editText.setText(nombreActual);


        layout.addView(editText);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Editar nombre de la lista")
                .setView(layout)
                .setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nombre = editText.getText().toString();
                        if (nombre.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Error al guardar: El nombre no puede estar vacío", Toast.LENGTH_LONG).show();
                        } else {
                            if (!nombre.equals(nombreActual)) {
                                actualizarLista(nombre, idLista, i);
                                Toast.makeText(getApplicationContext(), "Actualizando...", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    private void actualizarLista(final String nombre, String id, final int position) {
        db.collection(Constantes.BD_LISTA_GASTOS).document(user.getUid()).collection(Constantes.BD_TODAS_LISTAS).document(id)
                .update(Constantes.BD_NOMBRE, nombre)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(Constraints.TAG, "DocumentSnapshot successfully updated!");
                        listListas.get(position).setNombreLista(nombre);
                        listasAdapter.updateList(listListas);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(Constraints.TAG, "Error updating document", e);
                        Toast.makeText(getApplicationContext(), "Error al modificar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void eliminarLista(final int i, View view) {
        final ListasConstructor lista = listListas.get(i);
        listListas.remove(i);
        listasAdapter.updateList(listListas);

        Snackbar snackbar = Snackbar.make(view, lista.getNombreLista() + " borrado", Snackbar.LENGTH_LONG).setAction("Deshacer", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listListas.add(i, lista);
                listasAdapter.updateList(listListas);
            }
        });
        snackbar.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!listListas.contains(lista)) {
                    Toast.makeText(getApplicationContext(), "Eliminando lista", Toast.LENGTH_SHORT).show();
                    deleteLista(lista.getIdLista());
                }
            }
        }, 3000);
    }

    private void deleteLista(final String id) {
        db.collection(Constantes.BD_LISTA_GASTOS).document(user.getUid()).collection(Constantes.BD_TODAS_LISTAS).document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Delete", "DocumentSnapshot successfully deleted!");
                        deleteCollection(id);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Delete", "Error deleting document", e);
                        Toast.makeText(getApplicationContext(), "Error al borrar la lista. Intente nuevamente.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteCollection(final String id) {
        db.collection(Constantes.BD_LISTA_GASTOS).document(user.getUid()).collection(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(ContentValues.TAG, document.getId() + " => " + document.getData());
                                db.collection(Constantes.BD_LISTA_GASTOS).document(user.getUid()).collection(id).document(document.getId())
                                        .delete();
                            }
                            listItems.clear();
                            itemGastoAdapter.updateList(listItems);
                            tvInfoLista.setText(getResources().getString(R.string.sin_lista_seleccionada));
                        } else {
                            Log.d(ContentValues.TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        Toast.makeText(getApplicationContext(), "Lista eliminada exitosamente", Toast.LENGTH_SHORT).show();
    }

    private void cargarLista() {
        progressBarItems.setVisibility(View.VISIBLE);
        listItems = new ArrayList<>();

        String userID = user.getUid();

        CollectionReference reference = db.collection(Constantes.BD_LISTA_GASTOS).document(userID).collection(idLista);

        Query query = reference.orderBy(Constantes.BD_FECHA_INGRESO, Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                        ItemGastosConstructor item = new ItemGastosConstructor();

                        item.setIdItem(doc.getId());
                        item.setConcepto(doc.getString(Constantes.BD_CONCEPTO));
                        item.setMontoAproximado(doc.getDouble(Constantes.BD_MONTO));
                        item.setFechaIngreso(doc.getDate(Constantes.BD_FECHA_INGRESO));
                        item.setFechaAproximada(doc.getDate(Constantes.BD_FECHA_APROXIMADA));

                        listItems.add(item);
                    }

                    if (listItems.isEmpty()) {
                        String sinItems = getResources().getString(R.string.sin_items) + " " + nombreLista;
                        tvInfoLista.setText(sinItems);
                    } else {
                        tvInfoLista.setText(nombreLista);
                    }

                    itemGastoAdapter.updateList(listItems);
                    progressBarItems.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getApplicationContext(), "Error al cargar la lista. Intente nuevamente", Toast.LENGTH_SHORT).show();
                    progressBarItems.setVisibility(View.GONE);
                }
            }
        });

        itemGastoAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                cantidadItems = listListas.get(positionLista).getCantidadItems();
                final View vista = v;
                idItem = listItems.get(recyclerItems.getChildAdapterPosition(vista)).getIdItem();
                AlertDialog.Builder dialog = new AlertDialog.Builder(ListaGastosActivity.this);
                dialog.setTitle("¿Qué desea hacer?")
                        .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), "Eliminando...", Toast.LENGTH_SHORT).show();
                                eliminarItem();
                            }
                        }).setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setNegativeButton("Editar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editarItem();
                    }
                }).show();
                return true;
            }
        });
    }



    private void validarDatosGuardar() {
        layoutMonto.setError(null);
        layoutConcepto.setError(null);
        concepto = etConcepto.getText().toString();

        if (!concepto.isEmpty()) {
            if (!(etMonto.getText().toString().isEmpty())) {
                monto = Double.parseDouble(etMonto.getText().toString());
                if (monto > 0) {
                        layoutConcepto.setEnabled(false);
                        layoutMonto.setEnabled(false);
                        buttonCancelar.setEnabled(false);
                        buttonGuardarActualizar.setEnabled(false);
                        imageButtonSelecFecha.setEnabled(false);

                        if (nuevoItem) {
                            guardarDatos();
                        } else {
                            actualizarItem();
                        }
                } else {
                    layoutMonto.setError("El monto debe ser mayor a cero");
                }
            } else {
                layoutMonto.setError("Debe ingresar un monto");
            }
        } else {
            layoutConcepto.setError("Debe ingresar un Concepto");
        }
    }

    private void guardarDatos() {
        progressBarItems.setVisibility(View.VISIBLE);
        Map<String, Object> docData = new HashMap<>();
        docData.put(Constantes.BD_CONCEPTO, concepto);
        docData.put(Constantes.BD_MONTO, monto);
        docData.put(Constantes.BD_FECHA_APROXIMADA, fechaSelec);
        docData.put(Constantes.BD_FECHA_INGRESO, fechaIngreso);

        db.collection(Constantes.BD_LISTA_GASTOS).document(user.getUid()).collection(idLista).document()
                    .set(docData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot written succesfully");
                            recyclerItems.setVisibility(View.VISIBLE);
                            layoutItem.setVisibility(View.GONE);
                            actualizarCantidadItems(true);
                            cargarLista();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                            Toast.makeText(getApplicationContext(), "Error al guardar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                            layoutConcepto.setEnabled(true);
                            layoutMonto.setEnabled(true);
                            buttonGuardarActualizar.setEnabled(true);
                            buttonCancelar.setEnabled(true);
                            imageButtonSelecFecha.setEnabled(true);
                            progressBarItems.setVisibility(View.GONE);
                        }
                    });
        }

    private void actualizarCantidadItems (boolean sumarItems) {
        final int cantidad;
        if (sumarItems) {
            cantidad = cantidadItems + 1;
        } else {
            cantidad = cantidadItems - 1;
        }

        db.collection(Constantes.BD_LISTA_GASTOS).document(user.getUid()).collection(Constantes.BD_TODAS_LISTAS).document(idLista)
                .update(Constantes.BD_CANTIDAD_ITEMS, cantidad)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(Constraints.TAG, "DocumentSnapshot successfully updated!");
                        listListas.get(positionLista).setCantidadItems(cantidad);
                        listasAdapter.updateList(listListas);
                    }
                });
    }


    private void seleccionarFecha() {
        final Calendar calendarSelec = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendarSelec.set(year, month, dayOfMonth);
                fechaSelec = calendarSelec.getTime();
                tvFechaAproximada.setText(new SimpleDateFormat("EEE d MMM yyyy").format(fechaSelec));
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void editarItem() {
        nuevoItem = false;
        progressBarItems.setVisibility(View.VISIBLE);
        recyclerItems.setVisibility(View.GONE);
        layoutItem.setVisibility(View.VISIBLE);
        layoutConcepto.setEnabled(false);
        layoutMonto.setEnabled(false);
        buttonCancelar.setEnabled(false);
        buttonGuardarActualizar.setEnabled(false);
        imageButtonSelecFecha.setEnabled(false);
        layoutMonto.setError(null);
        layoutConcepto.setError(null);
        etConcepto.setText("");
        etMonto.setText("");
        fechaSelec = null;


        db.collection(Constantes.BD_LISTA_GASTOS).document(user.getUid()).collection(idLista).document(idItem).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        conceptoViejo = document.getString(Constantes.BD_CONCEPTO);
                        etConcepto.setText(conceptoViejo);

                        montoViejo = document.getDouble(Constantes.BD_MONTO);
                        String montoS = String.valueOf(montoViejo);
                        etMonto.setText(montoS);


                        fechaViejaAproximada = document.getDate(Constantes.BD_FECHA_APROXIMADA);
                        if (fechaViejaAproximada != null) {
                            tvFechaAproximada.setText(new SimpleDateFormat("EEE d MMM yyyy").format(fechaViejaAproximada));
                        } else {
                            tvFechaAproximada.setText("Sin fecha establecida");
                        }

                        progressBarItems.setVisibility(View.GONE);
                        layoutConcepto.setEnabled(true);
                        layoutMonto.setEnabled(true);
                        buttonCancelar.setEnabled(true);
                        buttonGuardarActualizar.setEnabled(true);
                        imageButtonSelecFecha.setEnabled(true);

                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(getApplicationContext(), "Error al cargar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                        progressBarItems.setVisibility(View.GONE);
                        recyclerItems.setVisibility(View.VISIBLE);
                        layoutItem.setVisibility(View.GONE);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Toast.makeText(getApplicationContext(), "Error al cargar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                    progressBarItems.setVisibility(View.GONE);
                    recyclerItems.setVisibility(View.VISIBLE);
                    layoutItem.setVisibility(View.GONE);
                }
            }
        });
    }

    private void actualizarItem() {
        progressBarItems.setVisibility(View.VISIBLE);
        layoutConcepto.setEnabled(false);
        layoutMonto.setEnabled(false);
        buttonCancelar.setEnabled(false);
        buttonGuardarActualizar.setEnabled(false);
        imageButtonSelecFecha.setEnabled(false);

        Map<String, Object> item = new HashMap<>();

        if (!conceptoViejo.equals(concepto)) {
            item.put(Constantes.BD_CONCEPTO, concepto);
        }
        if (monto != montoViejo) {
            item.put(Constantes.BD_MONTO, monto);
        }

        if(fechaSelec != null) {
            if (!fechaSelec.equals(fechaViejaAproximada)) {
                item.put(Constantes.BD_FECHA_APROXIMADA, fechaSelec);
            }
        }

            db.collection(Constantes.BD_LISTA_GASTOS).document(user.getUid()).collection(idLista).document(idItem)
                    .update(item)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                                Toast.makeText(getApplicationContext(), "Ítem modificado", Toast.LENGTH_SHORT).show();
                                progressBarItems.setVisibility(View.GONE);
                            recyclerItems.setVisibility(View.VISIBLE);
                            layoutItem.setVisibility(View.GONE);
                            cargarLista();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                            Toast.makeText(getApplicationContext(), "Error al modificar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                            progressBarItems.setVisibility(View.GONE);
                            recyclerItems.setVisibility(View.VISIBLE);
                            layoutItem.setVisibility(View.GONE);
                        }
                    });
    }

    private void eliminarItem() {
        db.collection(Constantes.BD_LISTA_GASTOS).document(user.getUid()).collection(idLista).document(idItem)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Delete", "DocumentSnapshot successfully deleted!");
                        Toast.makeText(getApplicationContext(), "Eliminado", Toast.LENGTH_SHORT).show();
                        actualizarCantidadItems(false);
                        cargarLista();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Delete", "Error deleting document", e);
                        Toast.makeText(getApplicationContext(), "Error al borrar. Intente nuevamente.", Toast.LENGTH_SHORT).show();
                    }
                });
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