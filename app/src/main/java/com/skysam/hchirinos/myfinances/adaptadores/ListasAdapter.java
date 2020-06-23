package com.skysam.hchirinos.myfinances.adaptadores;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Constraints;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.Utils.VariablesEstaticas;
import com.skysam.hchirinos.myfinances.constructores.IngresosConstructor;
import com.skysam.hchirinos.myfinances.constructores.ListasConstructor;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class ListasAdapter extends RecyclerView.Adapter<ListasAdapter.ViewHolder> implements View.OnClickListener {

    private ArrayList<ListasConstructor> listas;
    private View.OnClickListener listener;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public ListasAdapter(ArrayList<ListasConstructor> listas, Context context) {
        this.listas = listas;
        this.context = context;
    }

    @NonNull
    @Override
    public ListasAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_listas, null, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListasAdapter.ViewHolder holder, int position) {
        int i = position;
        int c = listas.get(i).getCantidadItems();
        holder.nombre.setText(listas.get(i).getNombreLista());

        if (c == 0) {
            holder.cantidad.setText("Sin ítmes");
        } else {
            holder.cantidad.setText("Ítems: " + c);
        }

    }

    @Override
    public int getItemCount() {
        return listas.size();
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onClick(v);
        }
    }

    public void setOnClickListener (View.OnClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, cantidad;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nombre = itemView.findViewById(R.id.textView_nombre_lista);
            cantidad = itemView.findViewById(R.id.textView_cantidad_items);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("¿Qué desea hacer con la Lista?")
                            .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    eliminarLista(getAdapterPosition(), v);
                                }
                            }).setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setNegativeButton("Editar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editarLista(getAdapterPosition());
                        }
                    }).show();
                    return true;
                }
            });
        }
    }

    private void editarLista(final int i) {
        final String nombreActual = listas.get(i).getNombreLista();
        final String idLista = listas.get(i).getIdLista();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText editText = new EditText(context);
        editText.setTextSize(24);
        editText.setPadding(50, 75, 5, 5);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editText.setHint("Nombre:");
        editText.setText(nombreActual);


        layout.addView(editText);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Editar nombre de la lista")
                .setView(layout)
                .setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nombre = editText.getText().toString();
                        if (nombre.isEmpty()) {
                            Toast.makeText(context, "Error al guardar: El nombre no puede estar vacío", Toast.LENGTH_LONG).show();
                        } else {
                            if (!nombre.equals(nombreActual)) {
                                actualizarLista(nombre, idLista, i);
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
        db.collection(VariablesEstaticas.BD_LISTA_GASTOS).document(user.getUid()).collection(VariablesEstaticas.BD_TODAS_LISTAS).document(id)
                .update(VariablesEstaticas.BD_NOMBRE, nombre)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(Constraints.TAG, "DocumentSnapshot successfully updated!");
                        listas.get(position).setNombreLista(nombre);
                        updateList(listas);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(Constraints.TAG, "Error updating document", e);
                        Toast.makeText(context, "Error al modificar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void eliminarLista(final int i, View view) {
        final ListasConstructor lista = listas.get(i);
        listas.remove(i);
        updateList(listas);

        Snackbar snackbar = Snackbar.make(view, lista.getNombreLista() + " borrado", Snackbar.LENGTH_LONG).setAction("Deshacer", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listas.add(i, lista);
                updateList(listas);
            }
        });
        snackbar.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!listas.contains(lista)) {
                    Toast.makeText(context, "Eliminando lista", Toast.LENGTH_SHORT).show();
                    deleteLista(lista.getIdLista());
                }
            }
        }, 3000);
    }

    private void deleteLista(final String id) {
        db.collection(VariablesEstaticas.BD_LISTA_GASTOS).document(user.getUid()).collection(VariablesEstaticas.BD_TODAS_LISTAS).document(id)
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
                        Toast.makeText(context, "Error al borrar la lista. Intente nuevamente.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteCollection(final String id) {
        db.collection(VariablesEstaticas.BD_LISTA_GASTOS).document(user.getUid()).collection(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                db.collection(VariablesEstaticas.BD_LISTA_GASTOS).document(user.getUid()).collection(id).document(document.getId())
                                        .delete();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        Toast.makeText(context, "Lista eliminada exitosamente", Toast.LENGTH_SHORT).show();
    }


    public void updateList (ArrayList<ListasConstructor> newList) {
        listas = new ArrayList<>();
        listas.addAll(newList);
        notifyDataSetChanged();
    }
}
