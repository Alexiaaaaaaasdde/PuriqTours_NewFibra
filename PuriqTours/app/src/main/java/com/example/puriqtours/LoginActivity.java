package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Vincular elementos del layout
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tvForgotPassword); // Asegúrate que exista en tu XML
        tvRegister = findViewById(R.id.tvRegister);

        // Acción "¿Olvidaste tu contraseña?"
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Intent i = new Intent(this, ForgotPasswordActivity.class);
                startActivity(i);
            });
        }

        // 👉 Acción "Registrarse"
        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                Intent i = new Intent(this, RegisterActivity.class);
                startActivity(i);
            });
        }

        // 👉 Acción "Iniciar sesión"
        btnLogin.setOnClickListener(v -> {
            String correo = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString();

            if (correo.isEmpty() || pass.isEmpty()) {
                etEmail.setError("Ingresa tu correo");
                etPassword.setError("Ingresa tu contraseña");
                return;
            }

            // ⚡ Aquí en el futuro puedes validar contra LocalAuth
            // Por ahora solo mostramos que todo funciona
            Intent i = new Intent(this, MainGuiaActivity.class); // O pantalla principal de tu app
            startActivity(i);
            finish();
        });
    }
}
