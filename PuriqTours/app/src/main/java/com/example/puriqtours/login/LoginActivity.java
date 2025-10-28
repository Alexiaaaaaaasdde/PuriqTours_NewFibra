package com.example.puriqtours.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.puriqtours.R;
import com.example.puriqtours.cliente.ProfileActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 🎨 Splash screen
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 🔹 Vincular elementos del layout
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);

        // 🔹 Instancia de almacenamiento local
        LocalAuth localAuth = new LocalAuth(this);

        // ✅ Si ya hay sesión activa, ir directo al perfil
        if (localAuth.isLogged()) {
            Intent i = new Intent(LoginActivity.this, ProfileActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }

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
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

            // ✅ Validar credenciales
            String savedEmail = localAuth.getEmail();
            String savedPass = localAuth.getPassword();

            if (correo.equals(savedEmail) && pass.equals(savedPass)) {
                // Guardar estado de sesión activa
                localAuth.setLogged(true);

                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(LoginActivity.this, ProfileActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
