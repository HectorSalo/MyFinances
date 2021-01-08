package com.skysam.hchirinos.myfinances.ahorrosModule.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.model.constructores.AhorrosConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class AhorrosAdapter extends RecyclerView.Adapter<AhorrosAdapter.ViewHolder> {
    private ArrayList<AhorrosConstructor> listaAhorros;
    private Context context;
    private Activity activity;

    public AhorrosAdapter() {
    }

    public AhorrosAdapter(ArrayList<AhorrosConstructor> listaAhorros, Context context, Activity activity) {
        this.listaAhorros = listaAhorros;
        this.context = context;
        this.activity = activity;
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

        if (position == 0) {
            configTutorial(holder.cardView);
        }

    }

    @Override
    public int getItemCount() {
        return listaAhorros.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView concepto, origen, monto, fechaIngreso;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            concepto = itemView.findViewById(R.id.textView_concepto);
            origen = itemView.findViewById(R.id.textView_origen);
            monto = itemView.findViewById(R.id.textView_monto);
            fechaIngreso = itemView.findViewById(R.id.textView_fecha_ingreso);
            cardView = itemView.findViewById(R.id.cardview_ahorros);
        }
    }

    public void updateList (ArrayList<AhorrosConstructor> newList) {
        listaAhorros = new ArrayList<>();
        listaAhorros.addAll(newList);
        notifyDataSetChanged();
    }

    private void configTutorial(View view) {
        new MaterialShowcaseView.Builder(activity)
                .setContentTextColor(ContextCompat.getColor(context, R.color.color_message_tutorial))
                .setDismissTextColor(ContextCompat.getColor(context, android.R.color.white))
                .setMaskColour(ContextCompat.getColor(context, R.color.color_background_tutorial))
                .setTarget(view)
                .setTargetTouchable(true)
                .setContentText(R.string.swipe_tuto_message)
                .setDismissText(R.string.btn_tuto_ok)
                .setDismissStyle(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC))
                .singleUse(context.getString(R.string.swipe_ahorros_tuto_id))
                .setDelay(2000)
                .setFadeDuration(600)
                .setDismissOnTargetTouch(true)
                .setDismissOnTouch(false)
                .show();
    }
}
