package com.skysam.hchirinos.myfinances.deudasModule.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
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

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth;
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.common.model.constructores.AhorrosConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class DeudasAdapter extends RecyclerView.Adapter<DeudasAdapter.ViewHolder> {

    private ArrayList<AhorrosConstructor> listaDeudas;
    private final Context context;
    private final int year;
    private final int mes;

    public DeudasAdapter(ArrayList<AhorrosConstructor> listaDeudas, Context context, int year, int mes) {
        this.listaDeudas = listaDeudas;
        this.context = context;
        this.mes = mes;
        this.year = year;
    }

    @NonNull
    @Override
    public DeudasAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_prestamos, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DeudasAdapter.ViewHolder holder, int position) {
        final int i = position;

        holder.concepto.setText(listaDeudas.get(i).getConcepto());
        holder.prestamista.setText(listaDeudas.get(i).getPrestamista());

        holder.fechaIngreso.setText("Agregado el: " + new SimpleDateFormat("EEE d MMM yyyy").format(listaDeudas.get(i).getFechaIngreso()));

        if (listaDeudas.get(i).getMonto() > 0) {
            if (listaDeudas.get(i).isDolar()) {
                holder.monto.setText("$" + ClassesCommon.INSTANCE.convertDoubleToString(listaDeudas.get(i).getMonto()));
            } else {
                holder.monto.setText("Bs. " + ClassesCommon.INSTANCE.convertDoubleToString(listaDeudas.get(i).getMonto()));
            }
        } else {
            holder.monto.setText("Deuda pagada por completo");
        }

        holder.tvMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.tvMenu);
            popupMenu.inflate(R.menu.deudas_popmenu);
            if (listaDeudas.get(i).getMonto() == 0) {
                popupMenu.getMenu().findItem(R.id.menu_abono).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_aumento).setVisible(false);
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_abono) {
                    ingresarAbono(i);
                } else if (item.getItemId() == R.id.menu_aumento) {
                    ingresarAumento(i);
                } else if (item.getItemId() == R.id.menu_historial_pagos) {
                    verPagos(i);
                }
                return false;
            });
            popupMenu.show();
        });

    }

    @Override
    public int getItemCount() {
        return listaDeudas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView concepto, prestamista, monto, fechaIngreso, tvMenu;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            concepto = itemView.findViewById(R.id.textView_concepto);
            prestamista = itemView.findViewById(R.id.textView_destinatario);
            monto = itemView.findViewById(R.id.textView_monto);
            fechaIngreso = itemView.findViewById(R.id.textView_fecha_ingreso);
            tvMenu = itemView.findViewById(R.id.tvmenu_prestamo);
        }
    }

    public void updateList (ArrayList<AhorrosConstructor> newList) {
        listaDeudas = new ArrayList<>();
        listaDeudas.addAll(newList);
        notifyDataSetChanged();
    }


    private void ingresarAbono(final int position) {
        boolean b = listaDeudas.get(position).isDolar();
        final double montoOriginal = listaDeudas.get(position).getMonto();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.layout_cotizacion_dolar, null);
        TextView textView = v.findViewById(R.id.textView_cotizacion);
        final EditText editText = v.findViewById(R.id.editText_cotizacion);


        if (b) {
            textView.setText(context.getString(R.string.moneda_dolar));
        } else {
            textView.setText(context.getString(R.string.moneda_bolivar) + " ");
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Ingrese el monto a abonar")
                .setView(v)
                .setPositiveButton("Abonar", (dialog1, which) -> {
                    if (!editText.getText().toString().isEmpty()) {
                        double valor = Double.parseDouble(editText.getText().toString());
                        if (valor > 0) {
                            double total = montoOriginal - valor;
                            if (total >= 0) {
                                Toast.makeText(context, "Actualizando deuda...", Toast.LENGTH_LONG).show();
                                if (total == 0) {
                                    eliminarDeuda(position, valor);
                                } else {
                                    actualizarMonto(position, total, true);
                                }
                            } else {
                                Toast.makeText(context, "No puede abonar un monto mayor a la deuda", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "El valor ingresado no puede ser cero", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    private void actualizarMonto(final int position, final double montoNuevo, boolean pago) {
        Calendar calendar = Calendar.getInstance();
        Date fecha;

        if (pago) {
            fecha = calendar.getTime();
        } else {
            fecha = null;
        }
        String idDoc = listaDeudas.get(position).getIdDeuda();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (int j = mes; j < 12; j++) {
            final int finalJ = j;
            db.collection(Constants.BD_DEUDAS).document(Auth.INSTANCE.uidCurrentUser())
                    .collection(year + "-" + j).document(idDoc)
                    .update(Constants.BD_MONTO, montoNuevo, Constants.BD_FECHA_HISTORIAL, FieldValue.arrayUnion(fecha))
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                        if (finalJ == 11) {
                            Toast.makeText(context, "Monto actualizado", Toast.LENGTH_SHORT).show();
                            listaDeudas.get(position).setMonto(montoNuevo);
                            updateList(listaDeudas);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error updating document", e);
                        if (finalJ > mes) {
                            Toast.makeText(context, "Monto actualizado", Toast.LENGTH_SHORT).show();
                            listaDeudas.get(position).setMonto(montoNuevo);
                            updateList(listaDeudas);
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_guardar_data), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void eliminarDeuda(final int position, double monto) {
        String idDoc = listaDeudas.get(position).getIdDeuda();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        int month;
        if (mes == 11) {
            month = mes;
        } else {
            month = mes + 1;
        }
        for (int j = month; j < 12; j++) {
            final int finalJ = j;
            db.collection(Constants.BD_DEUDAS).document(Auth.INSTANCE.uidCurrentUser())
                    .collection(year + "-" + j).document(idDoc)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        if (finalJ == 11) {
                            agregarUltPago(position, monto);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error updating document", e);
                        if (finalJ > mes) {
                            agregarUltPago(position, monto);
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_guardar_data), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void agregarUltPago(int position, double monto) {
        Calendar calendar = Calendar.getInstance();

        Map<String,Object> updates = new HashMap<>();
        updates.put(Constants.BD_MONTO, 0);
        updates.put(Constants.BD_FECHA_HISTORIAL, FieldValue.arrayUnion(calendar.getTime()));
        updates.put(Constants.BD_MONTO_ULT_PAGO, monto);

        String idDoc = listaDeudas.get(position).getIdDeuda();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(Constants.BD_DEUDAS).document(Auth.INSTANCE.uidCurrentUser())
                .collection(year + "-" + mes).document(idDoc)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                    Toast.makeText(context, "Monto actualizado", Toast.LENGTH_SHORT).show();
                    listaDeudas.get(position).setMonto(0);
                    updateList(listaDeudas);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating document", e);
                    Toast.makeText(context, "Monto actualizado", Toast.LENGTH_SHORT).show();
                    listaDeudas.get(position).setMonto(0);
                    updateList(listaDeudas);
                });
    }


    private void ingresarAumento(final int position) {
        boolean b = listaDeudas.get(position).isDolar();
        final double montoOriginal = listaDeudas.get(position).getMonto();
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
                .setPositiveButton("Aumentar", (dialog1, which) -> {
                    if (!editText.getText().toString().isEmpty()) {
                        double valor = Double.parseDouble(editText.getText().toString());
                        if (valor > 0) {
                            double total = montoOriginal + valor;
                            actualizarMonto(position, total, false);
                        } else {
                            Toast.makeText(context, "El valor ingresado no puede ser cero", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }


    private void verPagos(int position) {
        String idDoc = listaDeudas.get(position).getIdDeuda();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(Constants.BD_DEUDAS).document(Auth.INSTANCE.uidCurrentUser())
                .collection(year + "-" + mes).document(idDoc)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            double ultPago = 0;
                            if (documentSnapshot.getDouble(Constants.BD_MONTO) == 0) {
                                ultPago = documentSnapshot.getDouble(Constants.BD_MONTO_ULT_PAGO);
                            }
                            ArrayList<Timestamp> fechas = ((ArrayList<Timestamp>) documentSnapshot.get(Constants.BD_FECHA_HISTORIAL));
                            if (fechas != null && fechas.size() > 0) {
                                for (int j = 0; j < fechas.size(); j++) {
                                    if (fechas.get(j) == null) {
                                        fechas.remove(j);
                                    }
                                }
                                if (fechas.size() > 1) {
                                    Collections.sort(fechas, (o1, o2) -> o2.compareTo(o1));
                                }
                                cargarLayout(fechas, ultPago);
                            } else {
                                cargarLayout(null, ultPago);
                            }
                        }
                    }
                });
    }

    private void cargarLayout(ArrayList<Timestamp> fechas, double ultPago) {
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
                if (ultPago > 0) {
                    textView1.setText(tf.format(fechas.get(0).toDate()) + " ($" + ultPago + ")");
                } else {
                    textView1.setText(tf.format(fechas.get(0).toDate()));
                }
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
                .setPositiveButton("OK", (dialog1, which) -> dialog1.dismiss()).show();
    }

}
