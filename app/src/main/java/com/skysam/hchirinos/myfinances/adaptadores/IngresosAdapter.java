package com.skysam.hchirinos.myfinances.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.constructores.IngresosConstructor;
import com.skysam.hchirinos.myfinances.inicioSesion.InicSesionActivity;

import java.util.ArrayList;

public class IngresosAdapter extends RecyclerView.Adapter<IngresosAdapter.ViewHolder> {

    private ArrayList<IngresosConstructor> listIngresos;
    private Context context;

    public IngresosAdapter(ArrayList<IngresosConstructor> listIngresos, Context context) {
        this.listIngresos = listIngresos;
        this.context = context;
    }


    @NonNull
    @Override
    public IngresosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_ingresos, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngresosAdapter.ViewHolder holder, int position) {
        holder.tvConcepto.setText(listIngresos.get(position).getConcepto());

        if (listIngresos.get(position).isDolar()) {
            holder.tvMonto.setText("$ " + listIngresos.get(position).getMonto());
        } else {
            holder.tvMonto.setText("Bs. " + listIngresos.get(position).getMonto());
        }

        holder.tvFrecuencia.setText(listIngresos.get(position).getDuracionFrecuencia() + " " + listIngresos.get(position).getTipoFrecuencia());

    }

    @Override
    public int getItemCount() {
        return listIngresos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvConcepto, tvMonto, tvFrecuencia, tvProximoCobro, tvMenu;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvConcepto = itemView.findViewById(R.id.textView_concepto);
            tvMonto = itemView.findViewById(R.id.textView_monto);
            tvFrecuencia = itemView.findViewById(R.id.textView_frecuencia);
            tvProximoCobro = itemView.findViewById(R.id.textView_proxima_fecha);
            tvMenu = itemView.findViewById(R.id.tvmenu_ingresos);
        }
    }


    public void updateList (ArrayList<IngresosConstructor> newList) {
        listIngresos = new ArrayList<>();
        listIngresos.addAll(newList);
        notifyDataSetChanged();
    }
}
