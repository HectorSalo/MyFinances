package com.skysam.hchirinos.myfinances.inicioSesion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
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
import com.skysam.hchirinos.myfinances.principal.HomeActivity;

public class InicSesionActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPass;
    private TextInputLayout etEmailLayout, etPassLayout;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
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

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

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

        if (user != null) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }
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
            etEmailLayout.setError("El campo no puede estar vacío");
            emailValido = false;
        }

        if (password.isEmpty()) {
            passwordValido = false;
            etPassLayout.setError("El campo no puede estar vacío");
        } else {
            passwordValido = true;

        }

        if (passwordValido && emailValido) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            progressBar.setVisibility(View.VISIBLE);
            buttonIniciarSesion.setEnabled(false);
            buttonRegistrar.setEnabled(false);
            buttonGoogle.setEnabled(false);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("msg", "signInWithEmail:success");
                                progressBar.setVisibility(View.GONE);
                                buttonIniciarSesion.setEnabled(true);
                                buttonRegistrar.setEnabled(true);
                                buttonGoogle.setEnabled(true);
                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            } else {
                                // If sign in fails, display a message to the user.
                                progressBar.setVisibility(View.GONE);
                                buttonIniciarSesion.setEnabled(true);
                                buttonRegistrar.setEnabled(true);
                                buttonGoogle.setEnabled(true);
                                buttonRestablecimientoPass.setVisibility(View.VISIBLE);
                                Log.w("msg", "signInWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Error al iniciar sesión\nPor favor, verifique los datos del Usuario y su conexión a internet",
                                        Toast.LENGTH_LONG).show();

                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("msg", "Error " + e);
                }
            });
        }
    }

    private void enviarEmailRestablecimiento() {
        String email = etEmail.getText().toString();

        if (!email.isEmpty()) {
            if (email.contains("@")) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                    mostrarAlertDialog();
                                }
                            }
                        });
            } else {
                etEmailLayout.setError("Formato incorrecto para correo");
            }
        } else {
            etEmailLayout.setError("El campo no puede estar vacío");
        }


    }


    private void mostrarAlertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("¡Listo!");
        dialog.setMessage("Fue enviado un correo a la dirección ingresada. Por favor, revise su Bandeja de Entrada y siga las instrucciones para restablecer su contraseña.");
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
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
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            progressBar.setVisibility(View.GONE);
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Error al iniciar sesión\nPor favor, verifique los datos del Usuario y su conexión a internet",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }
}
