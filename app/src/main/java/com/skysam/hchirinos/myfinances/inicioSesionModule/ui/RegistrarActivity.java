package com.skysam.hchirinos.myfinances.inicioSesionModule.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeActivity;

public class RegistrarActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPass, etPassRepetir;
    private TextInputLayout etEmailLayout, etPassLayout, etPassRepetirLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        etEmail = findViewById(R.id.et_email_registrar);
        etPass = findViewById(R.id.et_password_registrar);
        etPassRepetir = findViewById(R.id.et_password_registrar_repetir);
        etEmailLayout = findViewById(R.id.outlined_email_registrar);
        etPassLayout = findViewById(R.id.outlined_password_registrar);
        etPassRepetirLayout = findViewById(R.id.outlined_password_registrar_repetir);

        progressBar = findViewById(R.id.progressBar_registrar);

        Button btnRegistrar = findViewById(R.id.button_guardar_registro);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarDatos();
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar");
        builder.setMessage("¿Desea salir? Se perderá la información ingresada.");
        builder.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }


    private void validarDatos() {
        String email = etEmail.getText().toString();
        String pass = etPass.getText().toString();
        String passRepetir = etPassRepetir.getText().toString();

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

        if (pass.isEmpty() || (pass.length() < 6)) {
            passwordValido = false;
            etPassLayout.setError("Mínimo 6 caracteres");
        } else {
            if (pass.equals(passRepetir)) {
                passwordValido = true;
            } else {
                passwordValido = false;
                etPassLayout.setError("Las contraseñas deben coincidir");
                etPassRepetirLayout.setError("Las contraseñas deben coincidir");
            }

        }

        if (passwordValido && emailValido) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            progressBar.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("msg", "createUserWithEmail:success");
                                progressBar.setVisibility(View.GONE);
                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            } else {
                                // If sign in fails, display a message to the user.
                                progressBar.setVisibility(View.GONE);
                                Log.w("msg", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegistrarActivity.this, "Error al Registrar\nPor favor, intente nuevamente",
                                        Toast.LENGTH_LONG).show();
                                etEmailLayout.setError(null);
                                etPassLayout.setError(null);
                                etPassRepetirLayout.setError(null);

                            }

                        }
                    });
        }
    }
}
