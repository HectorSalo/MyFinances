package com.skysam.hchirinos.myfinances.prestamosModule.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.common.model.constructores.AhorrosConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class PrestamosAdapter extends RecyclerView.Adapter<PrestamosAdapter.ViewHolder> {
    private ArrayList<AhorrosConstructor> listaPrestamos;
    private Context context;
    private int year, mes;

    public PrestamosAdapter(ArrayList<AhorrosConstructor> listaPrestamos, Context context, int year, int mes) {
        this.listaPrestamos = listaPrestamos;
        this.context = context;
        this.mes = mes;
        this.year = year;
    }

    @NonNull
    @Override
    public PrestamosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_prestamos, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PrestamosAdapter.ViewHolder holder, int position) {
        final int i = position;

        holder.concepto.setVisibility(View.GONE);

        holder.destinatario.setText(listaPrestamos.get(i).getConcepto());

        if (listaPrestamos.get(i).isDolar()) {
            holder.monto.setText("$" + listaPrestamos.get(i).getMonto());
        } else {
            holder.monto.setText("Bs. " + listaPrestamos.get(i).getMonto());
        }

        holder.fechaIngreso.setText("Préstamo realizado el: " + new SimpleDateFormat("EEE d MMM yyyy").format(listaPrestamos.get(i).getFechaIngreso()));

        holder.tvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, holder.tvMenu);
                popupMenu.inflate(R.menu.prestamos_popmenu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_cobro) {
                            ingresarCobro(i);
                        } else if (item.getItemId() == R.id.menu_aumento) {
                            ingresarAumento(i);
                        } else if (item.getItemId() == R.id.menu_historial_cobros) {
                            verPagos(i);
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return listaPrestamos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView destinatario, monto, fechaIngreso, concepto, tvMenu;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            destinatario = itemView.findViewById(R.id.textView_destinatario);
            concepto = itemView.findViewById(R.id.textView_concepto);
            monto = itemView.findViewById(R.id.textView_monto);
            fechaIngreso = itemView.findViewById(R.id.textView_fecha_ingreso);
            tvMenu = itemView.findViewById(R.id.tvmenu_prestamo);
        }
    }

    public void updateList (ArrayList<AhorrosConstructor> newList) {
        listaPrestamos = new ArrayList<>();
        listaPrestamos.addAll(newList);
        notifyDataSetChanged();
    }

    private void ingresarCobro(final int position) {
        boolean b = listaPrestamos.get(position).isDolar();
        final double montoOriginal = listaPrestamos.get(position).getMonto();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.layout_cotizacion_dolar, null);
        TextView textView = v.findViewById(R.id.textView_cotizacion);
        final EditText editText = v.findViewById(R.id.editText_cotizacion);

        if (b) {
            textView.setText("$");
        } else {
            textView.setText("Bs. ");
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Ingrese el monto cobrado")
                .setView(v)
                .setPositiveButton("Cobrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().isEmpty()) {
                            double valor = Double.parseDouble(editText.getText().toString());
                            if (valor > 0) {
                                double total = montoOriginal - valor;
                                if (total >= 0) {
                                    Toast.makeText(context, "Actualizando préstamo...", Toast.LENGTH_LONG).show();
                                    if (total == 0) {
                                        eliminarPrestamo(position);
                                    } else {
                                        actualizarMonto(position, total, true);
                                    }
                                } else {
                                    Toast.makeText(context, "No puede cobrar un monto mayor al préstamo", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "El valor ingresado no puede ser cero", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).show();
    }

    private void eliminarPrestamo(final int position) {
        String idDoc = listaPrestamos.get(position).getIdAhorro();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (int j = mes; j < 12; j++) {
            final int finalJ = j;
            db.collection(Constants.BD_PRESTAMOS).document(user.getUid()).collection(year + "-" + j).document(idDoc)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            if (finalJ == 11) {
                                Toast.makeText(context, "Préstamo cobrado por completo", Toast.LENGTH_SHORT).show();
                                listaPrestamos.remove(position);
                                updateList(listaPrestamos);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                            if (finalJ > mes) {
                                Toast.makeText(context, "Préstamo cobrado por completo", Toast.LENGTH_SHORT).show();
                                listaPrestamos.remove(position);
                                updateList(listaPrestamos);
                            } else {
                                Toast.makeText(context, "Error al agregar cobranza. Intente nuevamente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void actualizarMonto(final int position, final double montoNuevo, boolean cobranza) {
        Calendar calendar = Calendar.getInstance();
        Date fecha;

        if (cobranza) {
            fecha = calendar.getTime();
        } else {
            fecha = null;
        }
        String idDoc = listaPrestamos.get(position).getIdAhorro();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (int j = mes; j < 12; j++) {
            final int finalJ = j;
            db.collection(Constants.BD_PRESTAMOS).document(user.getUid()).collection(year + "-" + j).document(idDoc)
                    .update(Constants.BD_MONTO, montoNuevo, Constants.BD_FECHA_HISTORIAL, FieldValue.arrayUnion(fecha))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                            if (finalJ == 11) {
                                Toast.makeText(context, context.getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
                                listaPrestamos.get(position).setMonto(montoNuevo);
                                updateList(listaPrestamos);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                            if (finalJ > mes) {
                                Toast.makeText(context, context.getString(R.string.process_succes), Toast.LENGTH_SHORT).show();
                                listaPrestamos.get(position).setMonto(montoNuevo);
                                updateList(listaPrestamos);
                            } else {
                                Toast.makeText(context, context.getString(R.string.error_guardar_data), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }


    private void ingresarAumento(final int position) {
        boolean b = listaPrestamos.get(position).isDolar();
        final double montoOriginal = listaPrestamos.get(position).getMonto();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.layout_cotizacion_dolar, null);
        TextView textView = v.findViewById(R.id.textView_cotizacion);
        final EditText editText = v.findViewById(R.id.editText_cotizacion);


        if (b) {
            textView.setText("$");
        } else {
            textView.setText("Bs. ");
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Ingrese el monto de aumento")
                .setView(v)
                .setPositiveButton("Aumentar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().isEmpty()) {
                            double valor = Double.parseDouble(editText.getText().toString());
                            if (valor > 0) {
                                double total = montoOriginal + valor;
                                actualizarMonto(position, total, false);
                            } else {
                                Toast.makeText(context, "El valor ingresado no puede ser cero", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).show();
    }


    private void verPagos(int position) {
        String idDoc = listaPrestamos.get(position).getIdAhorro();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(Constants.BD_PRESTAMOS).document(user.getUid()).collection(year + "-" + mes).document(idDoc)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                ArrayList<Timestamp> fechas = ((ArrayList<Timestamp>) documentSnapshot.get(Constants.BD_FECHA_HISTORIAL));
                                if (fechas != null && fechas.size() > 0) {
                                    for (int j = 0; j < fechas.size(); j++) {
                                        if (fechas.get(j) == null) {
                                            fechas.remove(j);
                                        }
                                    }
                                    cargarLayout(fechas);
                                } else {
                                    cargarLayout(null);
                                }
                            }
                        }
                    }
                });
    }

    private void cargarLayout(ArrayList<Timestamp> fechas) {
        SimpleDateFormat tf = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        final TextView textView1 = new TextView(context);
        final TextView textView2 = new TextView(context);
        final TextView textView3 = new TextView(context);
        final TextView textView4 = new TextView(context);
        final TextView textView5 = new TextView(context);
        textView1.setTextSize(24);
        textView2.setTextSize(24);
        textView3.setTextSize(24);
        textView4.setTextSize(24);
        textView5.setTextSize(24);
        textView1.setPadding(50, 5, 5, 5);
        textView2.setPadding(50, 5, 5, 5);
        textView3.setPadding(50, 5, 5, 5);
        textView4.setPadding(50, 5, 5, 5);
        textView5.setPadding(50, 5, 5, 5);
        textView1.setText("");
        textView2.setText("");
        textView3.setText("");
        textView4.setText("");
        textView5.setText("");

        if (fechas == null || fechas.size() == 0) {
            textView1.setText("No tiene historial guardado");
        } else {
            if (fechas.size() > 0) {
                textView1.setText(tf.format(fechas.get(0).toDate()));
            }
            if (fechas.size() > 1) {
                textView2.setText(tf.format(fechas.get(1).toDate()));
            }
            if (fechas.size() > 2) {
                textView3.setText(tf.format(fechas.get(2).toDate()));
            }
            if (fechas.size() > 3) {
                textView4.setText(tf.format(fechas.get(3).toDate()));
            }
            if (fechas.size() > 4) {
                textView5.setText(tf.format(fechas.get(4).toDate()));
            }
        }

        layout.addView(textView1);
        layout.addView(textView2);
        layout.addView(textView3);
        layout.addView(textView4);
        layout.addView(textView5);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Fechas")
                .setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
