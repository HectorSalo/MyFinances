package com.skysam.hchirinos.myfinances.ui.ajustes;

import android.Manifest;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
import com.skysam.hchirinos.myfinances.databinding.DialogHuellaSettingsBinding;
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

        private SharedPreferences.Editor editor;
        private ListPreference listaBloqueo;
        private DialogPinSettingsBinding dialogPinSettingsBinding;
        private DialogHuellaSettingsBinding dialogHuellaSettingsBinding;
        private String bloqueo, pinRespaldo, bloqueoEscogido;
        private boolean pinNuevo;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferencias_preferences, rootKey);

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
                    bloqueoEscogido = (String) newValue;
                    editor = sharedPreferences.edit();

                    switch (bloqueoEscogido){
                        case Constantes.PREFERENCE_SIN_BLOQUEO:
                            if (!bloqueoEscogido.equals(bloqueo)) {
                                editor.putString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_SIN_BLOQUEO);
                                editor.apply();
                            }
                            break;
                        case Constantes.PREFERENCE_BLOQUEO_HUELLA:
                            if (!bloqueoEscogido.equals(bloqueo)) {
                                dialogHuellaSettingsBinding = DialogHuellaSettingsBinding.inflate(getLayoutInflater());
                                if (bloqueo.equalsIgnoreCase(Constantes.PREFERENCE_SIN_BLOQUEO)) {
                                    dialogHuellaSettingsBinding.tvInfoHuella.setText(getString(R.string.text_coloque_huella));
                                    crearDialogHuella();
                                }
                            }
                            break;
                        case Constantes.PREFERENCE_BLOQUEO_PIN:
                            if (!bloqueoEscogido.equals(bloqueo)) {
                                dialogPinSettingsBinding = DialogPinSettingsBinding.inflate(getLayoutInflater());
                                if (bloqueo.equalsIgnoreCase(Constantes.PREFERENCE_SIN_BLOQUEO)) {
                                    pinNuevo = true;
                                    dialogPinSettingsBinding.titlePin.setText(getString(R.string.text_ingrese_pin));
                                    crearDialogPin();
                                } else {
                                    /*pinNuevo = false;
                                    dialogPinSettingsBinding.titlePin.setText(getString(R.string.text_ingrese_pin_respaldo));
                                    dialogPinSettingsBinding.inputRepetirPin.setVisibility(View.GONE);
                                    pinRespaldo = sharedPreferences.getString(Constantes.PREFERENCE_PIN_ALMACENADO, "0000");*/
                                }
                            }
                            break;
                    }
                    return true;
                }
            });
        }

        private void crearDialogHuella() {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(dialogHuellaSettingsBinding.getRoot());
            builder.setCancelable(false);
            builder.setNegativeButton(getString(R.string.btn_cancelar), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listaBloqueo.setValue(bloqueo);
                }
            });

            BiometricManager biometricManager = BiometricManager.from(requireActivity());

                switch (biometricManager.canAuthenticate()) {
                    case BiometricManager.BIOMETRIC_SUCCESS:
                        dialogHuellaSettingsBinding.lottieAnimationView.setAnimation("huella_init.json");
                        dialogHuellaSettingsBinding.lottieAnimationView.playAnimation();
                        dialogHuellaSettingsBinding.tvInfoHuella.setText("¿Desea bloquear la App con su huella?");
                        builder.setPositiveButton(getString(R.string.btn_bloquear), null);
                        break;
                    case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                        dialogHuellaSettingsBinding.lottieAnimationView.setAnimation("huella_wrong.json");
                        dialogHuellaSettingsBinding.lottieAnimationView.playAnimation();
                        dialogHuellaSettingsBinding.tvInfoHuella.setText("No está disponible el bloqueo por huella en este dispositivo actualmente");
                        break;
                    case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                        dialogHuellaSettingsBinding.lottieAnimationView.setAnimation("huella_wrong.json");
                        dialogHuellaSettingsBinding.lottieAnimationView.playAnimation();
                        dialogHuellaSettingsBinding.tvInfoHuella.setText("No está disponible el bloqueo por huella en este dispositivo actualmente");
                        break;
                    case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                        dialogHuellaSettingsBinding.lottieAnimationView.setAnimation("huella_wrong.json");
                        dialogHuellaSettingsBinding.lottieAnimationView.playAnimation();
                        dialogHuellaSettingsBinding.tvInfoHuella.setText("No está disponible el bloqueo por huella en este dispositivo actualmente");
                        break;
                }

            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editor.putString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_BLOQUEO_HUELLA);
                    editor.apply();

                    dialogHuellaSettingsBinding.lottieAnimationView.setAnimation("huella_check.json");
                    dialogHuellaSettingsBinding.lottieAnimationView.playAnimation();
                    dialogHuellaSettingsBinding.tvInfoHuella.setText("Guardando...");

                    new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialogHuellaSettingsBinding.tvInfoHuella.setText("¡Listo!");
                        }
                    }, 2500);

                    new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    }, 4000);
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
            dialog.show();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogPinSettingsBinding.inputRepetirPin.setError(null);
                    dialogPinSettingsBinding.inputPin.setError(null);
                    dialogPinSettingsBinding.inputRepetirPin.setErrorIconDrawable(null);
                    dialogPinSettingsBinding.inputPin.setErrorIconDrawable(null);
                    String pin = dialogPinSettingsBinding.etRegistrarPin.getText().toString();
                    if (pin.isEmpty()) {
                        dialogPinSettingsBinding.inputPin.setError(getString(R.string.error_campo_vacio));
                        return;
                    }
                    if (pinNuevo) {
                        String pinRepetir = dialogPinSettingsBinding.etPinRepetir.getText().toString();
                        if (pinRepetir.isEmpty()) {
                            dialogPinSettingsBinding.inputRepetirPin.setError(getString(R.string.error_campo_vacio));
                            return;
                        }
                        if (pin.equalsIgnoreCase(pinRepetir)) {
                            InputMethodManager imm = (InputMethodManager)requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                            editor.putString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_BLOQUEO_PIN);
                            editor.putString(Constantes.PREFERENCE_PIN_ALMACENADO, pin);
                            editor.apply();

                            dialogPinSettingsBinding.linearLayout.setVisibility(View.GONE);
                            dialogPinSettingsBinding.lottieAnimationView.setVisibility(View.VISIBLE);

                            dialogPinSettingsBinding.lottieAnimationView.setAnimation("pin_check.json");
                            dialogPinSettingsBinding.lottieAnimationView.playAnimation();
                            new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                }
                            }, 2500);
                        } else {
                            dialogPinSettingsBinding.inputRepetirPin.setError("El PIN debe coincidir");
                            return;
                        }
                    } else {
                        /*if (pin.equalsIgnoreCase(pinRespaldo)) {
                            if (bloqueoEscogido.equalsIgnoreCase(Constantes.PREFERENCE_SIN_BLOQUEO)) {
                                editor.putString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_SIN_BLOQUEO);
                                editor.putString(Constantes.PREFERENCE_PIN_ALMACENADO, "0000");
                                editor.apply();
                            }
                            dialog.dismiss();
                        } else {
                            dialogPinSettingsBinding.inputPin.setError("PIN incorrecto");
                            return;
                        }*/
                    }
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