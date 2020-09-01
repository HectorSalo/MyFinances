package com.skysam.hchirinos.myfinances.ui.ajustes;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.skysam.hchirinos.myfinances.databinding.DialogPinSettingsBinding;
import com.skysam.hchirinos.myfinances.databinding.FragmentPinBinding;

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

        private ListPreference listaBloqueo;
        private DialogPinSettingsBinding dialogPinSettingsBinding;
        private String bloqueo;
        private boolean pinNuevo;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferencias_preferences, rootKey);

            dialogPinSettingsBinding = DialogPinSettingsBinding.inflate(getLayoutInflater());


            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);

            bloqueo = sharedPreferences.getString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_SIN_BLOQUEO);

            listaBloqueo = findPreference(Constantes.PREFERENCE_TIPO_BLOQUEO);

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
                    bloqueo = sharedPreferences.getString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_SIN_BLOQUEO);
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
                                dialogPinSettingsBinding = DialogPinSettingsBinding.inflate(getLayoutInflater());
                                if (bloqueo.equalsIgnoreCase(Constantes.PREFERENCE_SIN_BLOQUEO)) {
                                    pinNuevo = true;
                                    dialogPinSettingsBinding.titlePin.setText(getString(R.string.text_ingrese_pin));
                                } else {
                                    pinNuevo = false;
                                    dialogPinSettingsBinding.titlePin.setText(getString(R.string.text_ingrese_pin_respaldo));
                                    dialogPinSettingsBinding.inputRepetirPin.setVisibility(View.GONE);
                                }
                                crearDialogPin();
                            }
                            break;
                    }
                    return true;
                }
            });
        }

        private void crearDialogPin() {
            final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(dialogPinSettingsBinding.getRoot());
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.btn_ingresar), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).setNegativeButton(getString(R.string.btn_cancelar), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listaBloqueo.setValue(bloqueo);
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pin = dialogPinSettingsBinding.etRegistrarPin.getText().toString();
                    if (pinNuevo) {
                        String pinRepetir = dialogPinSettingsBinding.etPinRepetir.getText().toString();
                        if (pin.equalsIgnoreCase(pinRepetir)) {
                            dialog.dismiss();
                        } else {
                            dialogPinSettingsBinding.inputRepetirPin.setError("El PIN debe coincidir");
                        }
                    }

                }
            });
            dialog.show();
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