package com.skysam.hchirinos.myfinances.ajustesModule.ui;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.skysam.hchirinos.myfinances.BuildConfig;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.model.SharedPreferencesBD;
import com.skysam.hchirinos.myfinances.common.model.firebase.Auth;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeActivity;

import org.jetbrains.annotations.NotNull;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {


    private static final String TITLE_TAG = "settingsActivityTitle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences(Auth.INSTANCE.uidCurrentUser(), Context.MODE_PRIVATE);
        String temaInicial = sharedPreferences.getString(Constants.PREFERENCE_TEMA, Constants.PREFERENCE_TEMA_SISTEMA);
        switch (temaInicial) {
            case Constants.PREFERENCE_TEMA_CLARO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case Constants.PREFERENCE_TEMA_OSCURO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case Constants.PREFERENCE_TEMA_SISTEMA:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            if (getIntent().getExtras() != null) {
                boolean actualizarTema = getIntent().getExtras().getBoolean("actualizarTema");
                if (actualizarTema) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.settings,new PreferenceSettingsFragment())
                            .commit();
                }
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settings,new HeaderFragment())
                        .commit();
            }
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

            int account = SharedPreferencesBD.INSTANCE.getAccount(requireContext());

            Preference changeAccount = findPreference("change_account");
            Preference preferenceCerrarSesion = findPreference("cerrar_sesion_header");
            Preference preferenceActualizarPass = findPreference("actualizar_pass_header");

            PreferenceScreen preferenceVersion = findPreference(getString(R.string.version));
            preferenceVersion.setSummary(BuildConfig.VERSION_NAME);

            String providerId = "";

            if (user != null) {
                for (UserInfo profile : user.getProviderData()) {
                    providerId = profile.getProviderId();
                }
            }

            if (changeAccount != null) {
                changeAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(@NonNull Preference preference) {
                        SharedPreferencesBD.INSTANCE.saveAccount(
                                requireContext(), account == 1 ? 2 : 1);
                        requireActivity().startActivity(new Intent(requireContext(), HomeActivity.class));
                        requireActivity().finish();
                        return true;
                    }
                });
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
}