package com.skysam.hchirinos.myfinances.inicioSesionModule.ui;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.common.utils.Constants;
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeActivity;
import com.skysam.hchirinos.myfinances.inicioSesionModule.presenter.LoginPresenter;
import com.skysam.hchirinos.myfinances.inicioSesionModule.presenter.LoginPresenterClass;

import org.jetbrains.annotations.NotNull;

public class InicSesionActivity extends AppCompatActivity implements InitSessionView {

    private LoginPresenter loginPresenter;
    private TextInputEditText etEmail, etPass;
    private TextInputLayout etEmailLayout, etPassLayout;
    private ProgressBar progressBar;
    private Button buttonIniciarSesion, buttonRegistrar, buttonRestablecimientoPass;
    private ImageButton buttonGoogle;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 101;
    private String TAG = "MsjSesion";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inic_sesion);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);

        loginPresenter = new LoginPresenterClass(this, this);

        etEmail = findViewById(R.id.et_email);
        etPass = findViewById(R.id.et_password);
        etEmailLayout = findViewById(R.id.outlined_email);
        etPassLayout = findViewById(R.id.outlined_password);
        progressBar = findViewById(R.id.progressBar_registrar);

        buttonIniciarSesion = findViewById(R.id.button_iniciar_sesion);
        buttonRegistrar = findViewById(R.id.button_registrar);
        buttonGoogle = findViewById(R.id.imageButton_google);
        buttonRestablecimientoPass = findViewById(R.id.button_restablecimiento_pass);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        buttonIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarInciarSesion();
            }
        });

        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegistrarActivity.class));
            }
        });

        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        buttonRestablecimientoPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarEmailRestablecimiento();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loginPresenter.getCurrentUser();
    }

    private void validarInciarSesion() {
        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();
        boolean emailValido;
        boolean passwordValido;

        if (!email.isEmpty()) {
            if (email.contains("@")) {
                emailValido = true;
            } else {
                etEmailLayout.setError("Formato incorrecto para correo");
                emailValido = false;
            }
        } else {
            etEmailLayout.setError(getString(R.string.error_campo_vacio));
            emailValido = false;
        }

        if (password.isEmpty()) {
            passwordValido = false;
            etPassLayout.setError(getString(R.string.error_campo_vacio));
        } else {
            passwordValido = true;

        }

        if (passwordValido && emailValido) {
            progressBar.setVisibility(View.VISIBLE);
            buttonIniciarSesion.setEnabled(false);
            buttonRegistrar.setEnabled(false);
            buttonGoogle.setEnabled(false);
            loginPresenter.authWithEmail(email, password);
        }
    }

    private void enviarEmailRestablecimiento() {
        String email = etEmail.getText().toString();

        if (!email.isEmpty()) {
            if (email.contains("@")) {
                loginPresenter.sendEmailRecovery(email);
            } else {
                etEmailLayout.setError("Formato incorrecto para correo");
            }
        } else {
            etEmailLayout.setError(getString(R.string.error_campo_vacio));
        }


    }


    private void mostrarAlertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("¡Listo!");
        dialog.setMessage("Fue enviado un correo a la dirección ingresada. Por favor, revise su Bandeja de Entrada y siga las instrucciones para restablecer su contraseña.");
        dialog.setPositiveButton("Ok", null).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        } else {
            Log.e(TAG, "Failed");
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        progressBar.setVisibility(View.VISIBLE);
        loginPresenter.authWithGoogle(acct);
    }

    @Override
    public void userActive(boolean active, FirebaseUser user) {
        if (active) {
            loginPresenter.getTipoBloqueo(user.getUid());
        }
    }

    @Override
    public void emailRecoverySuccesfully() {
        mostrarAlertDialog();
    }

    @Override
    public void authWithGoogleStatus(boolean ok) {
        if (ok) {
            progressBar.setVisibility(View.GONE);
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Error al iniciar sesión\nPor favor, verifique los datos del Usuario y su conexión a internet",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void tipoBloqueo(@NotNull String bloqueo) {
        if (bloqueo.equalsIgnoreCase(Constants.PREFERENCE_SIN_BLOQUEO)) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        } else {
            Intent intent = new Intent(this, BloqueoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.PREFERENCE_TIPO_BLOQUEO, bloqueo);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    public void authWithEmailStatus(boolean ok) {
        if (ok) {
            progressBar.setVisibility(View.GONE);
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        } else {
            progressBar.setVisibility(View.GONE);
            buttonIniciarSesion.setEnabled(true);
            buttonRegistrar.setEnabled(true);
            buttonGoogle.setEnabled(true);
            buttonRestablecimientoPass.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), "Error al iniciar sesión\nPor favor, verifique los datos del Usuario y su conexión a internet",
                    Toast.LENGTH_LONG).show();
        }
    }
}