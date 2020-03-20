package com.skysam.hchirinos.myfinances.adaptadores;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.constructores.AhorrosConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PrestamosAdapter extends RecyclerView.Adapter<PrestamosAdapter.ViewHolder> {
    private ArrayList<AhorrosConstructor> listaPrestamos;
    private Context context;

    public PrestamosAdapter(ArrayList<AhorrosConstructor> listaPrestamos, Context context) {
        this.listaPrestamos = listaPrestamos;
        this.context = context;
    }

    @NonNull
    @Override
    public PrestamosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_prestamos, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PrestamosAdapter.ViewHolder holder, int position) {
        int i = position;

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
                                ingresarCobro();
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
        TextView destinatario, monto, fechaIngreso, tvMenu;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            destinatario = itemView.findViewById(R.id.textView_destinatario);
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

    private void ingresarCobro() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.layout_cotizacion_dolar, null);
        final EditText editText = v.findViewById(R.id.editText_cotizacion);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Ingrese el monto cobrado")
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().isEmpty()) {
                            float valor = Float.parseFloat(editText.getText().toString());
                            if (valor > 0) {

                            } else {
                                Toast.makeText(context, "El valor ingresado no puede ser cero", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).show();
    }
}
