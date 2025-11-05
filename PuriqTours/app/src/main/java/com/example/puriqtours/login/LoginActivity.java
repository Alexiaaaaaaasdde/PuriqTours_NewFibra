package com.example.puriqtours.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.puriqtours.SplashActivity;
import com.example.puriqtours.entity.Usuario;
import com.example.puriqtours.helper.UserSessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.puriqtours.guia.MainGuiaActivity;
import com.example.puriqtours.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // 👉 Ir a registro
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 👉 Recuperar contraseña
        tvForgotPassword.setOnClickListener(v -> resetPassword());

        // 👉 Iniciar sesión
        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Campo requerido");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Campo requerido");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Verificando...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Iniciar Sesión");

                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        verifyUserData(uid);
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void verifyUserData(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Usuario user = Usuario.fromSnapshot(snapshot);

                        UserSessionManager session = new UserSessionManager(this);
                        session.saveUser(user);

                        Toast.makeText(this, "Bienvenido, " + user.getUsername(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, SplashActivity.class));
                        finish();
                    } else {
                        // Si no existe documento Firestore, forzar logout (inconsistencia)
                        mAuth.signOut();
                        Toast.makeText(this,
                                "Cuenta sin datos registrados. Regístrate nuevamente.",
                                Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, RegisterActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error al obtener datos del usuario.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Ingresa tu correo para restablecer contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Correo de restablecimiento enviado.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al enviar correo.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
