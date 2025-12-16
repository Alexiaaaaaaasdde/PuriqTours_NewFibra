package com.example.puriqtours.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button btnForgotOk;
    private ImageButton btnBackForgot;
    private EditText etEmailForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // 🔴 PRIMERO hacemos el findViewById
        btnForgotOk = findViewById(R.id.btnForgotOk);
        btnBackForgot = findViewById(R.id.btnBackForgot);
        etEmailForgot = findViewById(R.id.etEmailForgot);

        // 🔙 Botón atrás → Login
        btnBackForgot.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        });

        // 🔵 Botón "Aceptar" → enviar correo de Firebase
        btnForgotOk.setOnClickListener(v -> {

            String email = etEmailForgot.getText().toString().trim();

            if (email.isEmpty()) {
                etEmailForgot.setError("Ingresa tu correo");
                return;
            }

            FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(email)
                    .addOnSuccessListener(x -> {
                        Toast.makeText(this,
                                "Se envió un enlace a tu correo para recuperar la contraseña",
                                Toast.LENGTH_LONG).show();

                        finish(); // volver al login
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });

    }
}
