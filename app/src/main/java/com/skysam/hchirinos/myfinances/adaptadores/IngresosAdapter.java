package com.skysam.hchirinos.myfinances.adaptadores;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.Utils.VariablesEstaticas;
import com.skysam.hchirinos.myfinances.constructores.IngresosConstructor;
import com.skysam.hchirinos.myfinances.editar.EditarActivity;
import com.skysam.hchirinos.myfinances.inicioSesion.InicSesionActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class IngresosAdapter extends RecyclerView.Adapter<IngresosAdapter.ViewHolder> {

    private ArrayList<IngresosConstructor> listIngresos;
    private Context context;
    private Calendar calendarActual = Calendar.getInstance(Locale.getDefault());
    private Date fechaActual = calendarActual.getTime();

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
    public void onBindViewHolder(@NonNull final IngresosAdapter.ViewHolder holder, int position) {
        final int i = position;
        holder.tvConcepto.setText(listIngresos.get(position).getConcepto());

        if (listIngresos.get(position).isDolar()) {
            holder.tvMonto.setText("$" + listIngresos.get(position).getMonto());
        } else {
            holder.tvMonto.setText("Bs. " + listIngresos.get(position).getMonto());
        }

        holder.tvFrecuencia.setText("Se cobra cada " + listIngresos.get(position).getDuracionFrecuencia() + " " + listIngresos.get(position).getTipoFrecuencia());


        Date dateInicial = listIngresos.get(position).getFechaIncial();
        int duracionFrecuencia = listIngresos.get(position).getDuracionFrecuencia();
        String tipoFrecuencia = listIngresos.get(position).getTipoFrecuencia();
        Calendar calendarInicial = Calendar.getInstance();
        calendarInicial.setTime(dateInicial);

        if (tipoFrecuencia.equals("Dias")) {
            for (int j = 1; fechaActual.after(calendarInicial.getTime()); j++) {
                calendarInicial.add(Calendar.DAY_OF_YEAR, (duracionFrecuencia * j));
            }
        } else if (tipoFrecuencia.equals("Semanas")) {
            for (int j = 1; fechaActual.after(calendarInicial.getTime()); j++) {
                calendarInicial.add(Calendar.DAY_OF_YEAR, (duracionFrecuencia * j * 7));
            }
        } else if (tipoFrecuencia.equals("Meses")) {
            for (int j = 1; fechaActual.after(calendarInicial.getTime()); j++) {
                calendarInicial.add(Calendar.MONTH, (duracionFrecuencia * j));
            }
        }

        holder.tvProximoCobro.setText("Fecha próximo cobro: " + new SimpleDateFormat("EEE d MMM yyyy").format(calendarInicial.getTime()));

        holder.tvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, holder.tvMenu);
                popupMenu.inflate(R.menu.ingresos_popmenu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_editar:
                                break;

                            case R.id.menu_eliminar:
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


    private void editar(IngresosConstructor i) {
        Intent myIntent = new Intent(context, EditarActivity.class);
        Bundle myBundle = new Bundle();
        myBundle.putString("idDoc", i.getIdIngreso());
        myBundle.putInt("fragment", 0);
        myIntent.putExtras(myBundle);
        context.startActivity(myIntent);
    }

}
