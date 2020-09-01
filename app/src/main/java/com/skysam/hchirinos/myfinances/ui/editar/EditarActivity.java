package com.skysam.hchirinos.myfinances.ui.editar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;

import com.skysam.hchirinos.myfinances.R;

import java.util.Objects;

public class EditarActivity extends AppCompatActivity {

    private String idDoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        EditarIngresoFragment editarIngresoFragment = new EditarIngresoFragment();
        EditarGastoFragment editarGastoFragment = new EditarGastoFragment();

        Bundle myBundle = this.getIntent().getExtras();
        int fragment = myBundle.getInt("fragment");
        String idDoc = myBundle.getString("idDoc");
        int mes = myBundle.getInt("mes");
        int year = myBundle.getInt("year");

        Bundle bundleFragment = new Bundle();
        bundleFragment.putString("idDoc", idDoc);
        bundleFragment.putInt("mes", mes);
        bundleFragment.putInt("year", year);


        switch (fragment) {
            case 0:
                editarIngresoFragment.setArguments(bundleFragment);
                getSupportFragmentManager().beginTransaction().add(R.id.editar_container_fragment, editarIngresoFragment).commit();
                getSupportActionBar().setTitle("Editar Ingreso");
                break;

            case 1:
                boolean mesUnico = myBundle.getBoolean("mesUnico");
                bundleFragment.putBoolean("mesUnico", mesUnico);
                editarGastoFragment.setArguments(bundleFragment);
                getSupportFragmentManager().beginTransaction().add(R.id.editar_container_fragment, editarGastoFragment).commit();
                getSupportActionBar().setTitle("Editar Gasto");
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
