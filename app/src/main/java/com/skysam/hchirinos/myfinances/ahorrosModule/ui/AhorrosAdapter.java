package com.skysam.hchirinos.myfinances.ahorrosModule.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.model.constructores.AhorrosConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class AhorrosAdapter extends RecyclerView.Adapter<AhorrosAdapter.ViewHolder> {
    private ArrayList<AhorrosConstructor> listaAhorros;
    private Context context;

    public AhorrosAdapter() {
    }

    public AhorrosAdapter(ArrayList<AhorrosConstructor> listaAhorros, Context context) {
        this.listaAhorros = listaAhorros;
        this.context = context;
    }

    @NonNull
    @Override
    public AhorrosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_ahorros, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AhorrosAdapter.ViewHolder holder, int position) {
        int i = position;

        holder.concepto.setText(listaAhorros.get(i).getConcepto());

        if (listaAhorros.get(i).getOrigen() != null) {
            holder.origen.setText(listaAhorros.get(i).getOrigen());
        } else {
            holder.origen.setVisibility(View.GONE);
        }

        holder.fechaIngreso.setText("Agregado el: " + new SimpleDateFormat("EEE d MMM yyyy").format(listaAhorros.get(i).getFechaIngreso()));


        if (listaAhorros.get(i).isDolar()) {
            holder.monto.setText("$" + listaAhorros.get(i).getMonto());
        } else {
            holder.monto.setText("Bs. " + listaAhorros.get(i).getMonto());
        }

    }

    @Override
    public int getItemCount() {
        return listaAhorros.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView concepto, origen, monto, fechaIngreso;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            concepto = itemView.findViewById(R.id.textView_concepto);
            origen = itemView.findViewById(R.id.textView_origen);
            monto = itemView.findViewById(R.id.textView_monto);
            fechaIngreso = itemView.findViewById(R.id.textView_fecha_ingreso);
        }
    }

    public void updateList (ArrayList<AhorrosConstructor> newList) {
        listaAhorros = new ArrayList<>();
        listaAhorros.addAll(newList);
        notifyDataSetChanged();
    }
}
