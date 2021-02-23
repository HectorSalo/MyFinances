package com.skysam.hchirinos.myfinances.ui.activityGeneral;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.gastosModule.ui.EditarGastoFragment;
import com.skysam.hchirinos.myfinances.ingresosModule.ui.EditarIngresoFragment;

import java.util.Objects;

public class EditarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);

        String tema = sharedPreferences.getString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_SISTEMA);

        switch (tema){
            case Constants.PREFERENCE_TEMA_SISTEMA:
                setTheme(R.style.AppTheme);
                break;
            case Constants.PREFERENCE_TEMA_OSCURO:
                setTheme(R.style.AppThemeNight);
                break;
            case Constants.PREFERENCE_TEMA_CLARO:
                setTheme(R.style.AppThemeDay);
                break;
        }
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
        boolean mesUnico = myBundle.getBoolean("mesUnico");

        Bundle bundleFragment = new Bundle();
        bundleFragment.putString("idDoc", idDoc);
        bundleFragment.putInt("mes", mes);
        bundleFragment.putInt("year", year);
        bundleFragment.putBoolean("mesUnico", mesUnico);


        switch (fragment) {
            case 0:
                editarIngresoFragment.setArguments(bundleFragment);
                getSupportFragmentManager().beginTransaction().add(R.id.editar_container_fragment, editarIngresoFragment).commit();
                getSupportActionBar().setTitle("Editar Ingreso");
                break;

            case 1:
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
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
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
