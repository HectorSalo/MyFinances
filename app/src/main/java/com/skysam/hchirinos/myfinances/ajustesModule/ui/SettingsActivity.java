package com.skysam.hchirinos.myfinances.ajustesModule.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.messaging.FirebaseMessaging;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.databinding.DialogHuellaSettingsBinding;
import com.skysam.hchirinos.myfinances.databinding.DialogPinSettingsBinding;

import org.jetbrains.annotations.NotNull;

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
                    .replace(R.id.settings,new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(
                () -> {
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        setTitle(R.string.title_activity_settings);
                    }
                });

        Toolbar toolbar = findViewById(R.id.toolbar);
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
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        } else {
            finish();
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
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

            if (preferenceActualizarPass != null) {
                preferenceActualizarPass.setVisible(!providerId.equals("google.com"));


                preferenceActualizarPass.setOnPreferenceClickListener(preference -> {
                    ActualizarPassDialog actualizarPassDialog = new ActualizarPassDialog();
                    actualizarPassDialog.show(requireActivity().getSupportFragmentManager(), getTag());
                    return true;
                });
            }

            if (preferenceCerrarSesion != null) {
                preferenceCerrarSesion.setOnPreferenceClickListener(preference -> {
                    CerrarSesionDialog cerrarSesionDialog = new CerrarSesionDialog();
                    cerrarSesionDialog.show(requireActivity().getSupportFragmentManager(), getTag());
                    return true;
                });
            }
        }
    }

    public static class PreferenciasFragment extends PreferenceFragmentCompat {

        private SharedPreferences.Editor editor;
        private ListPreference listaBloqueo;
        private DialogPinSettingsBinding dialogPinSettingsBinding;
        private DialogHuellaSettingsBinding dialogHuellaSettingsBinding;
        private String bloqueo, pinRespaldo, bloqueoEscogido, temaInicial, temaEscogido;
        private boolean pinNuevo;
        private boolean guardarPin;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferencias_preferences, rootKey);

            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();

            bloqueo = sharedPreferences.getString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_SIN_BLOQUEO);
            temaInicial = sharedPreferences.getString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_SISTEMA);
            boolean notificationActive = sharedPreferences.getBoolean(Constants.PREFERENCE_NOTIFICATION_ACTIVE, true);

            listaBloqueo = findPreference(Constants.PREFERENCE_TIPO_BLOQUEO);
            SwitchPreferenceCompat notificacionesSwitch = findPreference(Constants.PREFERENCE_NOTIFICATION_MAIN_TOPIC);
            ListPreference listaTema = findPreference(Constants.PREFERENCE_TEMA);

            switch (bloqueo){
                case Constants.PREFERENCE_SIN_BLOQUEO:
                    listaBloqueo.setValue(Constants.PREFERENCE_SIN_BLOQUEO);
                    break;
                case Constants.PREFERENCE_BLOQUEO_HUELLA:
                    listaBloqueo.setValue(Constants.PREFERENCE_BLOQUEO_HUELLA);
                    break;
                case Constants.PREFERENCE_BLOQUEO_PIN:
                    listaBloqueo.setValue(Constants.PREFERENCE_BLOQUEO_PIN);
                    break;
            }

            if (notificacionesSwitch != null) {
                notificacionesSwitch.setChecked(notificationActive);
            }

            if (listaTema != null) {
                switch (temaInicial) {
                    case Constants.PREFERENCE_TEMA_SISTEMA:
                        listaTema.setValue(Constants.PREFERENCE_TEMA_SISTEMA);
                        break;
                    case Constants.PREFERENCE_TEMA_OSCURO:
                        listaTema.setValue(Constants.PREFERENCE_TEMA_OSCURO);
                        break;
                    case Constants.PREFERENCE_TEMA_CLARO:
                        listaTema.setValue(Constants.PREFERENCE_TEMA_CLARO);
                        break;
                }
            }


            listaBloqueo.setOnPreferenceChangeListener((preference, newValue) -> {
                bloqueo = sharedPreferences.getString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_SIN_BLOQUEO);
                bloqueoEscogido = (String) newValue;

                switch (bloqueoEscogido){
                    case Constants.PREFERENCE_SIN_BLOQUEO:
                        if (!bloqueoEscogido.equals(bloqueo)) {
                            dialogPinSettingsBinding = DialogPinSettingsBinding.inflate(getLayoutInflater());
                            if (bloqueo.equalsIgnoreCase(Constants.PREFERENCE_BLOQUEO_HUELLA)) {
                                dialogPinSettingsBinding.titlePin.setText(getString(R.string.text_ingrese_pin_respaldo));
                            } else {
                                dialogPinSettingsBinding.titlePin.setText(getString(R.string.text_ingrese_pin_actual));
                            }
                            dialogPinSettingsBinding.inputRepetirPin.setVisibility(View.GONE);
                            pinRespaldo = sharedPreferences.getString(Constants.PREFERENCE_PIN_ALMACENADO, "0000");
                            crearDialogSinBloqueo();
                        }
                        break;
                    case Constants.PREFERENCE_BLOQUEO_HUELLA:
                        if (!bloqueoEscogido.equals(bloqueo)) {
                            dialogHuellaSettingsBinding = DialogHuellaSettingsBinding.inflate(getLayoutInflater());
                            if (!bloqueo.equalsIgnoreCase(Constants.PREFERENCE_SIN_BLOQUEO)) {
                                pinNuevo = false;
                                pinRespaldo = sharedPreferences.getString(Constants.PREFERENCE_PIN_ALMACENADO, "0000");
                            } else {
                                pinNuevo = true;
                            }
                            guardarPin = false;
                            crearDialogHuella();
                        }
                        break;
                    case Constants.PREFERENCE_BLOQUEO_PIN:
                        if (!bloqueoEscogido.equals(bloqueo)) {
                            dialogPinSettingsBinding = DialogPinSettingsBinding.inflate(getLayoutInflater());
                            if (bloqueo.equalsIgnoreCase(Constants.PREFERENCE_SIN_BLOQUEO)) {
                                pinNuevo = true;
                                dialogPinSettingsBinding.titlePin.setText(getString(R.string.text_ingrese_pin));
                            } else {
                                pinNuevo = false;
                                dialogPinSettingsBinding.titlePin.setText(getString(R.string.text_ingrese_pin_respaldo));
                                dialogPinSettingsBinding.inputRepetirPin.setVisibility(View.GONE);
                                pinRespaldo = sharedPreferences.getString(Constants.PREFERENCE_PIN_ALMACENADO, "0000");
                            }
                            crearDialogPin();
                        }
                        break;
                }
                return true;
            });


            if (notificacionesSwitch != null) {
                notificacionesSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean switchOn = (boolean) newValue;
                    if (switchOn) {
                        FirebaseMessaging.getInstance().subscribeToTopic(Constants.PREFERENCE_NOTIFICATION_MAIN_TOPIC)
                                .addOnSuccessListener(aVoid -> {
                                    editor.putBoolean(Constants.PREFERENCE_NOTIFICATION_ACTIVE, true);
                                    editor.apply();
                                });
                    } else {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.PREFERENCE_NOTIFICATION_MAIN_TOPIC)
                                .addOnSuccessListener(aVoid -> {
                                    editor.putBoolean(Constants.PREFERENCE_NOTIFICATION_ACTIVE, false);
                                    editor.apply();
                                });
                    }
                    return true;
                });
            }


            if (listaTema != null) {
                listaTema.setOnPreferenceChangeListener((preference, newValue) -> {
                    temaInicial = sharedPreferences.getString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_SISTEMA);
                    temaEscogido = (String) newValue;

                    switch (temaEscogido) {
                        case Constants.PREFERENCE_TEMA_SISTEMA:
                            if (!temaEscogido.equalsIgnoreCase(temaInicial)) {
                                editor.putString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_SISTEMA);
                                editor.apply();
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            }
                            break;
                        case Constants.PREFERENCE_TEMA_CLARO:
                            if (!temaEscogido.equalsIgnoreCase(temaInicial)) {
                                editor.putString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_CLARO);
                                editor.apply();
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            }
                            break;
                        case Constants.PREFERENCE_TEMA_OSCURO:
                            if (!temaEscogido.equalsIgnoreCase(temaInicial)) {
                                editor.putString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_OSCURO);
                                editor.apply();
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            }
                            break;
                    }
                   /* Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.PREFERENCE_TEMA, true);
                    Intent intent = new Intent(getContext(), SettingsActivity.class);
                    intent.putExtras(bundle);
                    getActivity().finish();
                    getActivity().startActivity(intent);
                    getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);*/
                    return true;
                });
            }
        }

        private void crearDialogSinBloqueo() {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(dialogPinSettingsBinding.getRoot());
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.btn_ingresar), null)
                    .setNegativeButton(getString(R.string.btn_cancelar), (dialog, which) -> listaBloqueo.setValue(bloqueo));
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                dialogPinSettingsBinding.inputPin.setError(null);
                dialogPinSettingsBinding.inputPin.setErrorIconDrawable(null);
                String pin = dialogPinSettingsBinding.etRegistrarPin.getText().toString();
                if (pin.isEmpty()) {
                    dialogPinSettingsBinding.inputPin.setError(getString(R.string.error_campo_vacio));
                    return;
                }
                if (pin.equalsIgnoreCase(pinRespaldo)) {
                    editor.putString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_SIN_BLOQUEO);
                    editor.putString(Constants.PREFERENCE_PIN_ALMACENADO, "0000");
                    editor.apply();
                    dialog.dismiss();
                } else {
                    dialogPinSettingsBinding.inputPin.setError(getString(R.string.error_pin_code));
                }

            });
        }

        private void crearDialogHuella() {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(dialogHuellaSettingsBinding.getRoot());
            builder.setCancelable(false);
            builder.setNegativeButton(getString(R.string.btn_cancelar), (dialog, which) -> listaBloqueo.setValue(bloqueo));

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
            final Button buttonNegative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            final Button buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            buttonPositive.setOnClickListener(view -> {
                if (!guardarPin) {
                    if (pinNuevo) {
                        dialogHuellaSettingsBinding.tvInfoHuella.setText(getString(R.string.text_ingrese_pin_respaldo));
                    } else {
                        dialogHuellaSettingsBinding.inputRepetirPin.setVisibility(View.GONE);
                        dialogHuellaSettingsBinding.tvInfoHuella.setText(getString(R.string.text_ingrese_pin_actual));
                    }
                    dialogHuellaSettingsBinding.linearLayout.setVisibility(View.VISIBLE);
                    dialogHuellaSettingsBinding.lottieAnimationView.setVisibility(View.GONE);
                    buttonPositive.setText(getString(R.string.btn_ingresar));
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
                            editor.putString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_BLOQUEO_HUELLA);
                            editor.putString(Constants.PREFERENCE_PIN_ALMACENADO, pin);
                            editor.apply();

                            buttonPositive.setVisibility(View.GONE);
                            buttonNegative.setVisibility(View.GONE);
                            dialogHuellaSettingsBinding.linearLayout.setVisibility(View.GONE);
                            dialogHuellaSettingsBinding.lottieAnimationView.setVisibility(View.VISIBLE);
                            dialogHuellaSettingsBinding.lottieAnimationView.setAnimation("huella_check.json");
                            dialogHuellaSettingsBinding.lottieAnimationView.playAnimation();
                            dialogHuellaSettingsBinding.tvInfoHuella.setText("Guardando...");

                            new Handler(Looper.myLooper()).postDelayed(() -> dialogHuellaSettingsBinding.tvInfoHuella.setText("¡Listo!"), 2500);

                            new Handler(Looper.myLooper()).postDelayed(dialog::dismiss, 4000);
                        } else {
                            dialogHuellaSettingsBinding.inputRepetirPin.setError(getString(R.string.error_pin_match));
                        }
                    } else {
                        if (pin.equalsIgnoreCase(pinRespaldo)) {
                            dialogHuellaSettingsBinding.tvInfoHuella.setText(getString(R.string.text_ingrese_pin_respaldo));
                            dialogHuellaSettingsBinding.inputRepetirPin.setVisibility(View.VISIBLE);
                            dialogHuellaSettingsBinding.etRegistrarPin.setText("");
                            pinNuevo = true;
                        } else {
                            dialogHuellaSettingsBinding.inputPin.setError(getString(R.string.error_pin_code));
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
                    .setNegativeButton(getString(R.string.btn_cancelar), (dialog, which) -> listaBloqueo.setValue(bloqueo));
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
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

                        editor.putString(Constants.PREFERENCE_TIPO_BLOQUEO, Constants.PREFERENCE_BLOQUEO_PIN);
                        editor.putString(Constants.PREFERENCE_PIN_ALMACENADO, pin);
                        editor.apply();

                        dialogPinSettingsBinding.linearLayout.setVisibility(View.GONE);
                        dialogPinSettingsBinding.lottieAnimationView.setVisibility(View.VISIBLE);

                        dialogPinSettingsBinding.lottieAnimationView.setAnimation("pin_check.json");
                        dialogPinSettingsBinding.lottieAnimationView.playAnimation();
                        new Handler(Looper.myLooper()).postDelayed(dialog::dismiss, 2500);
                    } else {
                        dialogPinSettingsBinding.inputRepetirPin.setError(getString(R.string.error_pin_match));
                    }
                } else {
                    if (pin.equalsIgnoreCase(pinRespaldo)) {
                        pinNuevo = true;
                        dialogPinSettingsBinding.etRegistrarPin.setText("");
                        dialogPinSettingsBinding.titlePin.setText(getString(R.string.text_ingrese_pin));
                        dialogPinSettingsBinding.inputRepetirPin.setVisibility(View.VISIBLE);
                    } else {
                        dialogPinSettingsBinding.inputPin.setError(getString(R.string.error_pin_code));
                    }
                }
            });
        }
    }
}