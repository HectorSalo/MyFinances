package com.skysam.hchirinos.myfinances.gastosModule.ui;

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

public class GastosAdapter extends RecyclerView.Adapter<GastosAdapter.ViewHolder> {


    private ArrayList<IngresosGastosConstructor> listGastos;
    private final Context context;
    private final Calendar calendarActual = Calendar.getInstance(Locale.getDefault());
    private final Date fechaActual = calendarActual.getTime();
    private final Activity activity;

    public GastosAdapter(ArrayList<IngresosGastosConstructor> listGastos, Context context, Activity activity) {
        this.listGastos = listGastos;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public GastosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_ingresos, null, false);
        return new GastosAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GastosAdapter.ViewHolder holder, int position) {
        holder.tvConcepto.setText(listGastos.get(position).getConcepto());

        if (listGastos.get(position).isDolar()) {
            holder.tvMonto.setText("$" + ClassesCommon.INSTANCE.convertDoubleToString(listGastos.get(position).getMonto()));
        } else {
            holder.tvMonto.setText("Bs. " + ClassesCommon.INSTANCE.convertDoubleToString(listGastos.get(position).getMonto()));
        }

        if (listGastos.get(position).isMesActivo()) {

            if (listGastos.get(position).getTipoFrecuencia() != null) {
                if (!listGastos.get(position).isPagado()) {
                    holder.tvFrecuencia.setText("Se paga cada " + listGastos.get(position).getDuracionFrecuencia() + " "
                            + listGastos.get(position).getTipoFrecuencia());
                } else {
                    holder.tvFrecuencia.setText("Pagado");
                }

                Date dateInicial = listGastos.get(position).getFechaIncial();
                int duracionFrecuencia = listGastos.get(position).getDuracionFrecuencia();
                String tipoFrecuencia = listGastos.get(position).getTipoFrecuencia();
                Calendar calendarInicial = Calendar.getInstance();
                calendarInicial.setTime(dateInicial);

                switch (tipoFrecuencia) {
                    case "Dias":
                        for (int j = 1; fechaActual.after(calendarInicial.getTime()); j++) {
                            calendarInicial.add(Calendar.DAY_OF_YEAR, (duracionFrecuencia * j));
                        }
                        break;
                    case "Semanas":
                        for (int j = 1; fechaActual.after(calendarInicial.getTime()); j++) {
                            calendarInicial.add(Calendar.DAY_OF_YEAR, (duracionFrecuencia * j * 7));
                        }
                        break;
                    case "Meses":
                        for (int j = 1; fechaActual.after(calendarInicial.getTime()); j++) {
                            calendarInicial.add(Calendar.MONTH, (duracionFrecuencia * j));
                        }
                        break;
                }

                holder.tvProximoCobro.setText("Fecha próximo pago: " + new SimpleDateFormat("EEE d MMM yyyy").format(calendarInicial.getTime()));
            } else {
                holder.tvFrecuencia.setText("Gasto único para este mes");
                holder.tvProximoCobro.setVisibility(View.GONE);
            }
        } else {
            holder.tvFrecuencia.setText("Gasto suspendido en este mes");
            holder.tvProximoCobro.setVisibility(View.GONE);
        }

        if (position == 0) {
            configTutorial(holder.cardView);
        }
    }

    @Override
    public int getItemCount() {
        return listGastos.size();
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
        listGastos = new ArrayList<>();
        listGastos.addAll(newList);
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
                .singleUse(context.getString(R.string.swipe_gastos_tuto_id))
                .setDelay(2000)
                .setFadeDuration(600)
                .setDismissOnTargetTouch(true)
                .setDismissOnTouch(false)
                .show();
    }
}
