package com.skysam.hchirinos.myfinances.adaptadores;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.constructores.ItemGastosConstructor;
import com.skysam.hchirinos.myfinances.constructores.ListasConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ItemGastoAdapter extends RecyclerView.Adapter<ItemGastoAdapter.ViewHolder> {


    private ArrayList<ItemGastosConstructor> listaItems;
    private Context context;

    public ItemGastoAdapter(ArrayList<ItemGastosConstructor> listaItems, Context context) {
        this.listaItems = listaItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemGastoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_items_listas, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemGastoAdapter.ViewHolder holder, int position) {
        SimpleDateFormat df = new SimpleDateFormat("EEE d MMM yyyy");

        holder.concepto.setText(listaItems.get(position).getConcepto());
        holder.montoAprox.setText("$" + listaItems.get(position).getMontoAproximado());
        holder.fechaAprox.setText("Realizar gasto el: " + df.format(listaItems.get(position).getFechaAproximada()));
        holder.fechaIngreso.setText("Ítem agregado el: " + df.format(listaItems.get(position).getFechaIngreso()));
    }

    @Override
    public int getItemCount() {
        return listaItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView concepto, montoAprox, fechaAprox, fechaIngreso;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            concepto = itemView.findViewById(R.id.textView_concepto);
            montoAprox = itemView.findViewById(R.id.textView_monto_aproximado);
            fechaAprox = itemView.findViewById(R.id.textView_fecha_aproximada);
            fechaIngreso = itemView.findViewById(R.id.textView_fecha_ingreso);
        }
    }

    public void updateList (ArrayList<ItemGastosConstructor> newList) {
        listaItems = new ArrayList<>();
        listaItems.addAll(newList);
        notifyDataSetChanged();
    }
}
