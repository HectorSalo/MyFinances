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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class GastosAdapter extends RecyclerView.Adapter<GastosAdapter.ViewHolder> {


    private ArrayList<IngresosGastosConstructor> listGastos;
    private final Context context;
    private final int yearSelected;
    private final int mesSelected;
    private final Activity activity;

    public GastosAdapter(ArrayList<IngresosGastosConstructor> listGastos,
                         Context context,
                         Activity activity,
                         int yearSelected,
                         int mesSelected) {
        this.listGastos = listGastos;
        this.context = context;
        this.activity = activity;
        this.yearSelected = yearSelected;
        this.mesSelected = mesSelected;
    }

    @NonNull
    @Override
    public GastosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_ingresos, parent, false);
        return new GastosAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GastosAdapter.ViewHolder holder, int position) {
        IngresosGastosConstructor item = listGastos.get(position);

        holder.tvConcepto.setText(item.getConcepto());

        if (item.isDolar()) {
            holder.tvMonto.setText("$" + ClassesCommon.INSTANCE.convertDoubleToString(item.getMonto()));
        } else {
            holder.tvMonto.setText("Bs. " + ClassesCommon.INSTANCE.convertDoubleToString(item.getMonto()));
        }

        // Reset de visibilidades por reciclaje
        holder.tvFrecuencia.setVisibility(View.GONE);
        holder.tvProximoCobro.setVisibility(View.VISIBLE);

        // Mes suspendido
        if (!item.isMesActivo()) {
            holder.tvFrecuencia.setVisibility(View.VISIBLE);
            holder.tvFrecuencia.setText("Gasto suspendido en este mes");
            holder.tvProximoCobro.setVisibility(View.GONE);
            return;
        }

        // Gasto único
        if (item.getTipoFrecuencia() == null) {
            holder.tvFrecuencia.setVisibility(View.GONE);
            holder.tvProximoCobro.setText("Gasto único para este mes");
            return;
        }

        // Periódico: calcular fecha que cae dentro del mes seleccionado
        Date dueDate = computeDueDateInSelectedMonth(
                item.getFechaIncial(),
                item.getFechaFinal(),
                item.getTipoFrecuencia(),
                item.getDuracionFrecuencia(),
                yearSelected,
                mesSelected
        );

        // (Defensivo) si por data rara llega acá sin dueDate
        if (dueDate == null) {
            holder.tvProximoCobro.setText("Pagar el: --");
            return;
        }

        if (item.isPagado()) {
            // Mostrar estatus + próximo pago (siguiente ocurrencia)
            holder.tvFrecuencia.setVisibility(View.VISIBLE);
            holder.tvFrecuencia.setText("Pagado");

            Date nextDue = computeNextDueDate(dueDate, item.getFechaFinal(), item.getTipoFrecuencia(), item.getDuracionFrecuencia());
            if (nextDue != null) {
                holder.tvProximoCobro.setText("Pagar el: " + DateFormat.getDateInstance().format(nextDue));
            } else {
                holder.tvProximoCobro.setText("Pagar el: --");
            }
        } else {
            // No pagado: solo mostrar la fecha del pago correspondiente a ese mes
            holder.tvFrecuencia.setVisibility(View.GONE);
            holder.tvProximoCobro.setText("Pagar el: " + DateFormat.getDateInstance().format(dueDate));
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

    private Date computeDueDateInSelectedMonth(
            Date start,
            Date end,
            String tipo,
            int dur,
            int year,
            int month
    ) {
        if (start == null || tipo == null || dur <= 0) return null;

        Calendar monthStart = Calendar.getInstance();
        monthStart.clear();
        monthStart.set(year, month, 1, 0, 0, 0);

        Calendar monthEnd = (Calendar) monthStart.clone();
        monthEnd.add(Calendar.MONTH, 1);

        Calendar cal = Calendar.getInstance();
        cal.setTime(start);

        // Si inicia después del mes, no cae aquí
        if (!cal.before(monthEnd)) return null;

        // Avanzar hasta entrar en el mes seleccionado
        int guard = 0;
        while (cal.before(monthStart) && guard < 5000) {
            addByFrequency(cal, tipo, dur);
            guard++;
        }

        if (cal.before(monthStart) || !cal.before(monthEnd)) return null;

        Date candidate = cal.getTime();
        if (end != null && candidate.after(end)) return null;

        return candidate;
    }

    private Date computeNextDueDate(Date dueDate, Date end, String tipo, int dur) {
        if (dueDate == null || tipo == null || dur <= 0) return null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(dueDate);
        addByFrequency(cal, tipo, dur);

        Date next = cal.getTime();
        if (end != null && next.after(end)) return null;

        return next;
    }

    private void addByFrequency(Calendar cal, String tipo, int dur) {
        switch (tipo) {
            case "Dias":
                cal.add(Calendar.DAY_OF_YEAR, dur);
                break;
            case "Semanas":
                cal.add(Calendar.DAY_OF_YEAR, dur * 7);
                break;
            case "Meses":
                cal.add(Calendar.MONTH, dur);
                break;
            default:
                cal.add(Calendar.MONTH, 1);
                break;
        }
    }
}
