package com.skysam.hchirinos.myfinances.adaptadores;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.Utils.Constantes;
import com.skysam.hchirinos.myfinances.constructores.AhorrosConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class DeudasAdapter extends RecyclerView.Adapter<DeudasAdapter.ViewHolder> {

    private ArrayList<AhorrosConstructor> listaDeudas;
    private Context context;
    private int year, mes;

    public DeudasAdapter() {
    }

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
        return new DeudasAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DeudasAdapter.ViewHolder holder, int position) {
        final int i = position;

        holder.concepto.setText(listaDeudas.get(i).getConcepto());
        holder.prestamista.setText(listaDeudas.get(i).getPrestamista());

        holder.fechaIngreso.setText("Agregado el: " + new SimpleDateFormat("EEE d MMM yyyy").format(listaDeudas.get(i).getFechaIngreso()));


        if (listaDeudas.get(i).isDolar()) {
            holder.monto.setText("$" + listaDeudas.get(i).getMonto());
        } else {
            holder.monto.setText("Bs. " + listaDeudas.get(i).getMonto());
        }

        holder.tvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, holder.tvMenu);
                popupMenu.inflate(R.menu.deudas_popmenu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_abono:
                                ingresarAbono(i);
                                break;
                            case R.id.menu_aumento:
                                ingresarAumento(i);
                                break;
                            default:
                                break;
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
        return listaDeudas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
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
            textView.setText("$");
        } else {
            textView.setText("Bs. ");
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Ingrese el monto a abonar")
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().isEmpty()) {
                            double valor = Double.parseDouble(editText.getText().toString());
                            if (valor > 0) {
                                double total = montoOriginal - valor;
                                if (total >= 0) {
                                    if (total == 0) {
                                        eliminarDeuda(position);
                                    } else {
                                        actualizarMonto(position, total);
                                    }
                                } else {
                                    Toast.makeText(context, "No puede abonar un monto mayor a la deuda", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "El valor ingresado no puede ser cero", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).show();
    }

    private void actualizarMonto(final int position, final double montoNuevo) {
        String idDoc = listaDeudas.get(position).getIdDeuda();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (int j = mes; j < 12; j++) {
            final int finalJ = j;
            db.collection(Constantes.BD_DEUDAS).document(user.getUid()).collection(year + "-" + j).document(idDoc)
                    .update(Constantes.BD_MONTO, montoNuevo)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                            if (finalJ == 11) {
                                Toast.makeText(context, "Monto actualizado", Toast.LENGTH_SHORT).show();
                                listaDeudas.get(position).setMonto(montoNuevo);
                                updateList(listaDeudas);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                            Toast.makeText(context, "Error al guardar. Intente nuevamente", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void eliminarDeuda(final int position) {
        String idDoc = listaDeudas.get(position).getIdDeuda();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (int j = mes; j < 12; j++) {
            final int finalJ = j;
            db.collection(Constantes.BD_DEUDAS).document(user.getUid()).collection(year + "-" + j).document(idDoc)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            if (finalJ == 11) {
                                Toast.makeText(context, "Deuda pagada por completo", Toast.LENGTH_SHORT).show();
                                listaDeudas.remove(position);
                                updateList(listaDeudas);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                            Toast.makeText(context, "Error al agregar cobranza. Intente nuevamente", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().isEmpty()) {
                            double valor = Double.parseDouble(editText.getText().toString());
                            if (valor > 0) {
                                double total = montoOriginal + valor;
                                actualizarMonto(position, total);
                            } else {
                                Toast.makeText(context, "El valor ingresado no puede ser cero", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).show();
    }
}
