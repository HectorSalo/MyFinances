package com.skysam.hchirinos.myfinances.inicioSesion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.principal.HomeActivity;

public class InicSesionActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPass;
    private TextInputLayout etEmailLayout, etPassLayout;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inic_sesion);

        user = FirebaseAuth.getInstance().getCurrentUser();

        etEmail = findViewById(R.id.et_email);
        etPass = findViewById(R.id.et_password);
        etEmailLayout = findViewById(R.id.outlined_email);
        etPassLayout = findViewById(R.id.outlined_password);

        Button buttonInciarSesion = findViewById(R.id.button_iniciar_sesion);
        Button buttonRegistrar = findViewById(R.id.button_registrar);

        buttonInciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarInciarSesion();
            }
        });

        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            //progressBarInicSesion.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("msg", "signInWithEmail:success");
                                //progressBarInicSesion.setVisibility(View.GONE);
                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            } else {
                                // If sign in fails, display a message to the user.
                                //progressBarInicSesion.setVisibility(View.GONE);
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
}
