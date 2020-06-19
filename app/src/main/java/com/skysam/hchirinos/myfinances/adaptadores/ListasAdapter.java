package com.skysam.hchirinos.myfinances.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.constructores.IngresosConstructor;
import com.skysam.hchirinos.myfinances.constructores.ListasConstructor;

import java.util.ArrayList;

public class ListasAdapter extends RecyclerView.Adapter<ListasAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private ArrayList<ListasConstructor> listas;
    private View.OnClickListener listener;
    private Context context;

    public ListasAdapter(ArrayList<ListasConstructor> listas, Context context) {
        this.listas = listas;
        this.context = context;
    }

    @NonNull
    @Override
    public ListasAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_listas, null, false);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
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

    @Override
    public boolean onLongClick(View v) {
        Toast.makeText(context, "Item borrado", Toast.LENGTH_SHORT).show();
        return true;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, cantidad;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nombre = itemView.findViewById(R.id.textView_nombre_lista);
            cantidad = itemView.findViewById(R.id.textView_cantidad_items);
        }
    }


    public void updateList (ArrayList<ListasConstructor> newList) {
        listas = new ArrayList<>();
        listas.addAll(newList);
        notifyDataSetChanged();
    }
}
