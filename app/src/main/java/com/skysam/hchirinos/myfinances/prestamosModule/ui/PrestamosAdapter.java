package com.skysam.hchirinos.myfinances.prestamosModule.ui;

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
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.common.model.constructores.AhorrosConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

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
                        switch (item.getItemId()) {
                            case R.id.menu_cobro:
                                ingresarCobro(i);
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
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                                        actualizarMonto(position, total);
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

    private void actualizarMonto(final int position, final double montoNuevo) {
        String idDoc = listaPrestamos.get(position).getIdAhorro();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (int j = mes; j < 12; j++) {
            final int finalJ = j;
            db.collection(Constants.BD_PRESTAMOS).document(user.getUid()).collection(year + "-" + j).document(idDoc)
                    .update(Constants.BD_MONTO, montoNuevo)
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
}
