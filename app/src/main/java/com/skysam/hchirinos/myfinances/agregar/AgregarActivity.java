package com.skysam.hchirinos.myfinances.agregar;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;

import com.skysam.hchirinos.myfinances.R;

import java.util.Objects;

public class AgregarActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        AgregarIngresoFragment agregarIngresoFragment = new AgregarIngresoFragment();
        AgregarAhorroFragment agregarAhorroFragment = new AgregarAhorroFragment();

        int opcionAgregar = getIntent().getIntExtra("agregar", 0);
        switch (opcionAgregar) {
            case 0:
                getSupportFragmentManager().beginTransaction().add(R.id.agregar_container_fragment, agregarIngresoFragment).commit();
                getSupportActionBar().setTitle("Agregar un Ingreso Fijo");
                break;
            case 1:
                boolean descontar = getIntent().getBooleanExtra("descontar", false);
                Bundle bundleFragment = new Bundle();
                bundleFragment.putBoolean("descontar", descontar);
                agregarAhorroFragment.setArguments(bundleFragment);
                getSupportFragmentManager().beginTransaction().add(R.id.agregar_container_fragment, agregarAhorroFragment).commit();
                if (descontar) {
                    getSupportActionBar().setTitle("Descontar Ahorro");
                } else {
                    getSupportActionBar().setTitle("Agregar Ahorro");
                }
                break;
            case 2:
                getSupportActionBar().setTitle("Agregar un Préstamo Realizado");
                break;
            case 3:
                getSupportActionBar().setTitle("Agregar un Gasto Fijo");
                break;
            case 4:
                getSupportActionBar().setTitle("Agregar una Deuda");
                break;
            default:
                break;
        }

    }

    private void confirmarSalir() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Desea salir?");
        builder.setMessage("Se perderá la información no almacenada.");
        builder.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        confirmarSalir();
    }



}
