package com.skysam.hchirinos.myfinances.ui.ajustes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.Utils.Constantes;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                            setTitle(R.string.title_activity_settings);
                        }
                    }
                });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
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
        super.onBackPressed();
        /*if (headerFragment.isAdded()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, headerFragment)
                    .commit();
            setTitle(R.string.title_activity_settings);
        }*/
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    public static class HeaderFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey);
        }
    }

    public static class PreferenciasFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferencias_preferences, rootKey);


            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);

            final String bloqueo = sharedPreferences.getString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_SIN_BLOQUEO);

            ListPreference listaBloqueo = findPreference(Constantes.PREFERENCE_TIPO_BLOQUEO);

            switch (bloqueo){
                case Constantes.PREFERENCE_SIN_BLOQUEO:
                    listaBloqueo.setValue(Constantes.PREFERENCE_SIN_BLOQUEO);
                    break;
                case Constantes.PREFERENCE_BLOQUEO_HUELLA:
                    listaBloqueo.setValue(Constantes.PREFERENCE_BLOQUEO_HUELLA);
                    break;
                case Constantes.PREFERENCE_BLOQUEO_PIN:
                    listaBloqueo.setValue(Constantes.PREFERENCE_BLOQUEO_PIN);
                    break;
            }


            listaBloqueo.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String bloqueoEscogido = (String) newValue;
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    switch (bloqueoEscogido){
                        case Constantes.PREFERENCE_SIN_BLOQUEO:
                            if (!bloqueoEscogido.equals(bloqueo)) {
                                editor.putString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_SIN_BLOQUEO);
                                editor.apply();
                            }
                            break;
                        case Constantes.PREFERENCE_BLOQUEO_HUELLA:
                            if (!bloqueoEscogido.equals(bloqueo)) {
                                editor.putString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_BLOQUEO_HUELLA);
                                editor.apply();
                            }
                            break;
                        case Constantes.PREFERENCE_BLOQUEO_PIN:
                            if (!bloqueoEscogido.equals(bloqueo)) {
                                Intent intent = new Intent(getContext(), BloqueoActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_BLOQUEO_PIN);
                                bundle.putString(Constantes.USER, user.getUid());
                                bundle.putBoolean("inicio", false);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                            break;
                    }
                    return true;
                }
            });

        }
    }

    public static class CerrarSesionFragment extends DialogFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            confirmarCerrarSesion();
        }

        private void confirmarCerrarSesion() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirmar");
            builder.setMessage("¿Desea cerrar la sesión?");
            builder.setCancelable(false);
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //cerrarSesion();
                }
            }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).show();
        }
    }
}