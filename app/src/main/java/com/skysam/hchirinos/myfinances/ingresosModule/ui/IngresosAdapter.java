package com.skysam.hchirinos.myfinances.ingresosModule.ui;

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
import com.skysam.hchirinos.myfinances.common.model.constructores.IngresosGastosConstructor;
import com.skysam.hchirinos.myfinances.common.utils.ClassesCommon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class IngresosAdapter extends RecyclerView.Adapter<IngresosAdapter.ViewHolder> {

    private ArrayList<IngresosGastosConstructor> listIngresos;
    private final Context context;
    private final Calendar calendarActual = Calendar.getInstance(Locale.getDefault());
    private final Date fechaActual = calendarActual.getTime();
    private final Activity activity;

    public IngresosAdapter(ArrayList<IngresosGastosConstructor> listIngresos, Context context, Activity activity) {
        this.listIngresos = listIngresos;
        this.context = context;
        this.activity = activity;
    }


    @NonNull
    @Override
    public IngresosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_ingresos, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final IngresosAdapter.ViewHolder holder, int position) {
        holder.tvConcepto.setText(listIngresos.get(position).getConcepto());

        if (listIngresos.get(position).isDolar()) {
            holder.tvMonto.setText("$" + ClassesCommon.INSTANCE.convertDoubleToString(listIngresos.get(position).getMonto()));
        } else {
            holder.tvMonto.setText("Bs. " + ClassesCommon.INSTANCE.convertDoubleToString(listIngresos.get(position).getMonto()));
        }

        if(listIngresos.get(position).isMesActivo()) {
            if (listIngresos.get(position).getTipoFrecuencia() != null) {
                holder.tvFrecuencia.setText("Se cobra cada " + listIngresos.get(position).getDuracionFrecuencia() + " " + listIngresos.get(position).getTipoFrecuencia());


                Date dateInicial = listIngresos.get(position).getFechaIncial();
                int duracionFrecuencia = listIngresos.get(position).getDuracionFrecuencia();
                String tipoFrecuencia = listIngresos.get(position).getTipoFrecuencia();
                Calendar calendarInicial = Calendar.getInstance();
                calendarInicial.setTime(dateInicial);

                switch (tipoFrecuencia) {
                    case "Dias":
                        for (int j = 1; fechaActual.after(calendarInicial.getTime()); j++) {
                            calendarInicial.add(Calendar.DAY_OF_YEAR, (duracionFrecuencia));
                        }
                        break;
                    case "Semanas":
                        for (int j = 1; fechaActual.after(calendarInicial.getTime()); j++) {
                            calendarInicial.add(Calendar.DAY_OF_YEAR, (duracionFrecuencia * 7));
                        }
                        break;
                    case "Meses":
                        for (int j = 1; fechaActual.after(calendarInicial.getTime()); j++) {
                            calendarInicial.add(Calendar.MONTH, (duracionFrecuencia));
                        }
                        break;
                }

                holder.tvProximoCobro.setText("Fecha próximo cobro: " + new SimpleDateFormat("EEE d MMM yyyy").format(calendarInicial.getTime()));
            } else {
                holder.tvFrecuencia.setText("Ingreso único para este mes");
                holder.tvProximoCobro.setVisibility(View.GONE);
            }
        } else {
            holder.tvFrecuencia.setText("Ingreso suspendido este mes");
            holder.tvProximoCobro.setVisibility(View.GONE);
        }

        if (position == 0) {
            configTutorial(holder.cardView);
        }

    }

    @Override
    public int getItemCount() {
        return listIngresos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvConcepto, tvMonto, tvFrecuencia, tvProximoCobro, tvMenu;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvConcepto = itemView.findViewById(R.id.textView_concepto);
            tvMonto = itemView.findViewById(R.id.textView_monto);
            tvFrecuencia = itemView.findViewById(R.id.textView_frecuencia);
            tvProximoCobro = itemView.findViewById(R.id.textView_proxima_fecha);
            tvMenu = itemView.findViewById(R.id.tvmenu_ingresos);
            cardView = itemView.findViewById(R.id.cardview_ingresos);
        }
    }


    public void updateList (ArrayList<IngresosGastosConstructor> newList) {
        listIngresos = new ArrayList<>();
        listIngresos.addAll(newList);
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
                .singleUse(context.getString(R.string.swipe_ingresos_tuto_id))
                .setDelay(2000)
                .setFadeDuration(600)
                .setDismissOnTargetTouch(true)
                .setDismissOnTouch(false)
                .show();
    }

}
