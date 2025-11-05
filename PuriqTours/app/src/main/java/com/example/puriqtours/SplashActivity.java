package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.admin.MainAdminActivity;
import com.example.puriqtours.cliente.ProfileActivity;
import com.example.puriqtours.entity.Usuario;
import com.example.puriqtours.guia.MainGuiaActivity;
import com.example.puriqtours.helper.UserSessionManager;
import com.example.puriqtours.login.LoginActivity;
import com.example.puriqtours.superadmin.MainSuperAdminActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 segundos
    private FirebaseAuth mAuth;
    private UserSessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Inicializar Firebase y sesión
        mAuth = FirebaseAuth.getInstance();
        session = new UserSessionManager(this);

        // Opcional: animación o logo
        ImageView logo = findViewById(R.id.ivLogo);
        TextView tvTitle = findViewById(R.id.tvAppName);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkSession, SPLASH_DURATION);
    }

    private void checkSession() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        Usuario user = session.getUser();
        session.debugPrintUser();

        if (firebaseUser != null && user != null && user.getRol() != null) {
            String role = user.getRol().toLowerCase();
            Intent intent;

            switch (role) {
                case "cliente":
                    intent = new Intent(this, ProfileActivity.class);
                    break;
                case "guia":
                    intent = new Intent(this, MainGuiaActivity.class);
                    break;
                case "admin":
                    intent = new Intent(this, MainAdminActivity.class);
                    break;
                case "superadmin":
                    intent = new Intent(this, MainSuperAdminActivity.class);
                    break;
                default:
                    intent = new Intent(this, LoginActivity.class);
                    break;
            }

            startActivity(intent);
            finish();
        } else {
            // No hay sesión guardada
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
