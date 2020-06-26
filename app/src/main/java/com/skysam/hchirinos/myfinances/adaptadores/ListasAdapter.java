package com.skysam.hchirinos.myfinances.adaptadores;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Constraints;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.Utils.VariablesEstaticas;
import com.skysam.hchirinos.myfinances.constructores.IngresosConstructor;
import com.skysam.hchirinos.myfinances.constructores.ListasConstructor;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class ListasAdapter extends RecyclerView.Adapter<ListasAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private ArrayList<ListasConstructor> listas;
    private View.OnClickListener listener;
    private View.OnLongClickListener longClickListener;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

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

    public void setOnLongClickListener (View.OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @Override
    public boolean onLongClick(View v) {
        if (longClickListener != null) {
            longClickListener.onLongClick(v);
        }
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
