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
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import com.google.firebase.auth.UserInfo;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.Utils.Constantes;
import com.skysam.hchirinos.myfinances.databinding.DialogHuellaSettingsBinding;
import com.skysam.hchirinos.myfinances.databinding.DialogPinSettingsBinding;
import com.skysam.hchirinos.myfinances.databinding.FragmentPinBinding;
import com.skysam.hchirinos.myfinances.ui.inicio.HomeActivity;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {


    private static final String TITLE_TAG = "settingsActivityTitle";
    private HeaderFragment headerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);

        String tema = sharedPreferences.getString(Constantes.PREFERENCE_TEMA, Constantes.PREFERENCE_TEMA_SISTEMA);

        switch (tema){
            case Constantes.PREFERENCE_TEMA_SISTEMA:
                setTheme(R.style.AppTheme);
                break;
            case Constantes.PREFERENCE_TEMA_OSCURO:
                setTheme(R.style.AppThemeNight);
                break;
            case Constantes.PREFERENCE_TEMA_CLARO:
                setTheme(R.style.AppThemeDay);
                break;
        }
        setContentView(R.layout.settings_activity);

        headerFragment = new HeaderFragment();

        Bundle bundle = this.getIntent().getExtras();
        if (savedInstanceState == null) {
            if (bundle == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settings, headerFragment)
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settings, new PreferenciasFragment())
                        .commit();
                setTitle(R.string.preferencias_header);
            }
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

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onSupportNavigateUp();
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);
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
        } else {
            if (!headerFragment.isAdded()) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settings, headerFragment)
                        .commit();
                setTitle(R.string.title_activity_settings);
            } else {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
            }

        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onSupportNavigateUp();
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
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            Preference preferenceCerrarSesion = findPreference("cerrar_sesion_header");
            Preference preferenceActualizarPass = findPreference("actualizar_pass_header");

            String providerId = "";

            if (user != null) {
                for (UserInfo profile : user.getProviderData()) {
                    providerId = profile.getProviderId();
                }
            }

            if (providerId.equals("google.com")) {
                preferenceActualizarPass.setVisible(false);
            } else {
                preferenceActualizarPass.setVisible(true);
            }


            preferenceActualizarPass.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ActualizarPassDialog actualizarPassDialog = new ActualizarPassDialog();
                    actualizarPassDialog.show(requireActivity().getSupportFragmentManager(), getTag());
                    return true;
                }
            });

            preferenceCerrarSesion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    CerrarSesionDialog cerrarSesionDialog = new CerrarSesionDialog();
                    cerrarSesionDialog.show(requireActivity().getSupportFragmentManager(), getTag());
                    return true;
                }
            });
        }
    }

    public static class PreferenciasFragment extends PreferenceFragmentCompat {

        private SharedPreferences.Editor editor;
        private ListPreference listaBloqueo, listaTema;
        private DialogPinSettingsBinding dialogPinSettingsBinding;
        private DialogHuellaSettingsBinding dialogHuellaSettingsBinding;
        private String bloqueo, pinRespaldo, bloqueoEscogido, temaInicial, temaEscogido;
        private boolean pinNuevo, guardarPin;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferencias_preferences, rootKey);

            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();

            bloqueo = sharedPreferences.getString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_SIN_BLOQUEO);
            temaInicial = sharedPreferences.getString(Constantes.PREFERENCE_TEMA, Constantes.PREFERENCE_TEMA_SISTEMA);

            listaBloqueo = findPreference(Constantes.PREFERENCE_TIPO_BLOQUEO);
            listaTema = findPreference(Constantes.PREFERENCE_TEMA);

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

            switch (temaInicial) {
                case Constantes.PREFERENCE_TEMA_SISTEMA:
                    listaTema.setValue(Constantes.PREFERENCE_TEMA_SISTEMA);
                    break;
                case Constantes.PREFERENCE_TEMA_OSCURO:
                    listaTema.setValue(Constantes.PREFERENCE_TEMA_OSCURO);
                    break;
                case Constantes.PREFERENCE_TEMA_CLARO:
                    listaTema.setValue(Constantes.PREFERENCE_TEMA_CLARO);
                    break;
            }


            listaBloqueo.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    bloqueo = sharedPreferences.getString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_SIN_BLOQUEO);
                    bloqueoEscogido = (String) newValue;

                    switch (bloqueoEscogido){
                        case Constantes.PREFERENCE_SIN_BLOQUEO:
                            if (!bloqueoEscogido.equals(bloqueo)) {
                                dialogPinSettingsBinding = DialogPinSettingsBinding.inflate(getLayoutInflater());
                                if (bloqueo.equalsIgnoreCase(Constantes.PREFERENCE_BLOQUEO_HUELLA)) {
                                    dialogPinSettingsBinding.titlePin.setText(getString(R.string.text_ingrese_pin_respaldo));
                                } else {
                                    dialogPinSettingsBinding.titlePin.setText(getString(R.string.text_ingrese_pin_actual));
                                }
                                dialogPinSettingsBinding.inputRepetirPin.setVisibility(View.GONE);
                                pinRespaldo = sharedPreferences.getString(Constantes.PREFERENCE_PIN_ALMACENADO, "0000");
                                crearDialogSinBloqueo();
                            }
                            break;
                        case Constantes.PREFERENCE_BLOQUEO_HUELLA:
                            if (!bloqueoEscogido.equals(bloqueo)) {
                                dialogHuellaSettingsBinding = DialogHuellaSettingsBinding.inflate(getLayoutInflater());
                                if (!bloqueo.equalsIgnoreCase(Constantes.PREFERENCE_SIN_BLOQUEO)) {
                                    pinNuevo = false;
                                    pinRespaldo = sharedPreferences.getString(Constantes.PREFERENCE_PIN_ALMACENADO, "0000");
                                } else {
                                    pinNuevo = true;
                                }
                                guardarPin = false;
                                crearDialogHuella();
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
                                    pinRespaldo = sharedPreferences.getString(Constantes.PREFERENCE_PIN_ALMACENADO, "0000");
                                }
                                crearDialogPin();
                            }
                            break;
                    }
                    return true;
                }
            });


            listaTema.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    temaInicial = sharedPreferences.getString(Constantes.PREFERENCE_TEMA, Constantes.PREFERENCE_TEMA_SISTEMA);
                    temaEscogido = (String) newValue;

                    switch (temaEscogido) {
                        case Constantes.PREFERENCE_TEMA_SISTEMA:
                            if (!temaEscogido.equalsIgnoreCase(temaInicial)) {
                                editor.putString(Constantes.PREFERENCE_TEMA, Constantes.PREFERENCE_TEMA_SISTEMA);
                                editor.apply();
                            }
                            break;
                        case Constantes.PREFERENCE_TEMA_CLARO:
                            if (!temaEscogido.equalsIgnoreCase(temaInicial)) {
                                editor.putString(Constantes.PREFERENCE_TEMA, Constantes.PREFERENCE_TEMA_CLARO);
                                editor.apply();
                            }
                            break;
                        case Constantes.PREFERENCE_TEMA_OSCURO:
                            if (!temaEscogido.equalsIgnoreCase(temaInicial)) {
                                editor.putString(Constantes.PREFERENCE_TEMA, Constantes.PREFERENCE_TEMA_OSCURO);
                                editor.apply();
                            }
                            break;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constantes.PREFERENCE_TEMA, true);
                    Intent intent = new Intent(getContext(), SettingsActivity.class);
                    intent.putExtras(bundle);
                    getActivity().finish();
                    getActivity().startActivity(intent);
                    getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }
            });
        }

        private void crearDialogSinBloqueo() {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(dialogPinSettingsBinding.getRoot());
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.btn_ingresar), null)
                    .setNegativeButton(getString(R.string.btn_cancelar), new DialogInterface.OnClickListener() {
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
                    dialogPinSettingsBinding.inputPin.setError(null);
                    dialogPinSettingsBinding.inputPin.setErrorIconDrawable(null);
                    String pin = dialogPinSettingsBinding.etRegistrarPin.getText().toString();
                    if (pin.isEmpty()) {
                        dialogPinSettingsBinding.inputPin.setError(getString(R.string.error_campo_vacio));
                        return;
                    }
                    if (pin.equalsIgnoreCase(pinRespaldo)) {
                        editor.putString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_SIN_BLOQUEO);
                        editor.putString(Constantes.PREFERENCE_PIN_ALMACENADO, "0000");
                        editor.apply();
                        dialog.dismiss();
                    } else {
                        dialogPinSettingsBinding.inputPin.setError(getString(R.string.error_pin_code));
                        return;
                    }

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
                        dialogHuellaSettingsBinding.tvInfoHuella.setText("Este dispositivo no cuenta con lector de huella");
                        break;
                    case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                        dialogHuellaSettingsBinding.lottieAnimationView.setAnimation("huella_wrong.json");
                        dialogHuellaSettingsBinding.lottieAnimationView.playAnimation();
                        dialogHuellaSettingsBinding.tvInfoHuella.setText("No está disponible el bloqueo por huella en este dispositivo actualmente");
                        break;
                    case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                        dialogHuellaSettingsBinding.lottieAnimationView.setAnimation("huella_wrong.json");
                        dialogHuellaSettingsBinding.lottieAnimationView.playAnimation();
                        dialogHuellaSettingsBinding.tvInfoHuella.setText("No tiene ninguna huella asociada a su dispositivo");
                        break;
                }

            final AlertDialog dialog = builder.create();
            dialog.show();
            final Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!guardarPin) {
                        if (pinNuevo) {
                            dialogHuellaSettingsBinding.tvInfoHuella.setText(getString(R.string.text_ingrese_pin_respaldo));
                        } else {
                            dialogHuellaSettingsBinding.inputRepetirPin.setVisibility(View.GONE);
                            dialogHuellaSettingsBinding.tvInfoHuella.setText(getString(R.string.text_ingrese_pin_actual));
                        }
                        dialogHuellaSettingsBinding.linearLayout.setVisibility(View.VISIBLE);
                        dialogHuellaSettingsBinding.lottieAnimationView.setVisibility(View.GONE);
                        button.setText(getString(R.string.btn_ingresar));
                        guardarPin = true;
                    } else {
                        dialogHuellaSettingsBinding.inputPin.setError(null);
                        dialogHuellaSettingsBinding.inputPin.setErrorIconDrawable(null);
                        dialogHuellaSettingsBinding.inputRepetirPin.setErrorIconDrawable(null);
                        dialogHuellaSettingsBinding.inputRepetirPin.setError(null);
                        String pin = dialogHuellaSettingsBinding.etRegistrarPin.getText().toString();
                        if (pinNuevo) {
                            String pinRepetir = dialogHuellaSettingsBinding.etPinRepetir.getText().toString();
                            if (pin.isEmpty()) {
                                dialogHuellaSettingsBinding.inputPin.setError(getString(R.string.error_campo_vacio));
                                return;
                            }
                            if (pinRepetir.isEmpty()) {
                                dialogHuellaSettingsBinding.inputRepetirPin.setError(getString(R.string.error_campo_vacio));
                                return;
                            }
                            if (pin.equalsIgnoreCase(pinRepetir)) {
                                InputMethodManager imm = (InputMethodManager)requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                editor.putString(Constantes.PREFERENCE_TIPO_BLOQUEO, Constantes.PREFERENCE_BLOQUEO_HUELLA);
                                editor.putString(Constantes.PREFERENCE_PIN_ALMACENADO, pin);
                                editor.apply();

                                dialogHuellaSettingsBinding.linearLayout.setVisibility(View.GONE);
                                dialogHuellaSettingsBinding.lottieAnimationView.setVisibility(View.VISIBLE);
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
                            } else {
                                dialogHuellaSettingsBinding.inputRepetirPin.setError(getString(R.string.error_pin_match));
                                return;
                            }
                        } else {
                            if (pin.equalsIgnoreCase(pinRespaldo)) {
                                dialogHuellaSettingsBinding.tvInfoHuella.setText(getString(R.string.text_ingrese_pin_respaldo));
                                dialogHuellaSettingsBinding.inputRepetirPin.setVisibility(View.VISIBLE);
                                dialogHuellaSettingsBinding.etRegistrarPin.setText("");
                                pinNuevo = true;
                            } else {
                                dialogHuellaSettingsBinding.inputPin.setError(getString(R.string.error_pin_code));
                                return;
                            }
                        }
                    }
                }
            });
        }

        private void crearDialogPin() {
            final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(dialogPinSettingsBinding.getRoot());
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.btn_ingresar), null)
                    .setNegativeButton(getString(R.string.btn_cancelar), new DialogInterface.OnClickListener() {
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
                            dialogPinSettingsBinding.inputRepetirPin.setError(getString(R.string.error_pin_match));
                            return;
                        }
                    } else {
                        if (pin.equalsIgnoreCase(pinRespaldo)) {
                            pinNuevo = true;
                            dialogPinSettingsBinding.etRegistrarPin.setText("");
                            dialogPinSettingsBinding.titlePin.setText(getString(R.string.text_ingrese_pin));
                            dialogPinSettingsBinding.inputRepetirPin.setVisibility(View.VISIBLE);
                        } else {
                            dialogPinSettingsBinding.inputPin.setError(getString(R.string.error_pin_code));
                            return;
                        }
                    }
                }
            });
        }
    }
}