package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 🎨 ESTA ES LA LÍNEA NUEVA - Instalar el splash screen
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 🔹 Vincular elementos del layout
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);

        // 🔹 Acción "¿Olvidaste tu contraseña?"
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Intent i = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(i);
            });
        }

        // 🔹 Acción "Registrarse"
        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            });
        }

        // 🔹 Acción "Iniciar sesión"
        btnLogin.setOnClickListener(v -> {
            String correo = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString();

            if (correo.isEmpty()) {
                etEmail.setError("Ingresa tu correo");
                return;
            }
            if (pass.isEmpty()) {
                etPassword.setError("Ingresa tu contraseña");
                return;
            }

            // 🚀 Aquí en el futuro podrás validar credenciales
            // Por ahora solo redirige a la pantalla principal
            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(i);
            finish(); // evita volver al login con "atrás"
        });
    }
}

