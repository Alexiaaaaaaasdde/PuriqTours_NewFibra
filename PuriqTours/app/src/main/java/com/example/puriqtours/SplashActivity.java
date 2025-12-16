package com.example.puriqtours;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.example.puriqtours.WelcomeActivity;
import com.example.puriqtours.admin.MainAdminActivity;
import com.example.puriqtours.cliente.ProfileActivity;
import com.example.puriqtours.entity.Usuario;
import com.example.puriqtours.guia.GuidePendingActivity;
import com.example.puriqtours.guia.MainGuiaActivity;
import com.example.puriqtours.helper.UserSessionManager;
import com.example.puriqtours.helper.WelcomePrefs;
import com.example.puriqtours.login.LoginActivity;
import com.example.puriqtours.superadmin.MainSuperAdminActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000;
    private FirebaseAuth mAuth;
    private UserSessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        session = new UserSessionManager(this);

        boolean fromLogin = getIntent().getBooleanExtra("FROM_LOGIN", false);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if (fromLogin) {
                checkSession(); // decide rol
                return;
            }

            if (!WelcomePrefs.hasSeen(this)) {
                startActivity(new Intent(this, WelcomeActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();

        }, SPLASH_DURATION);
    }


    private void checkSession() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, WelcomeActivity.class));
                        finish();
                        return;
                    }

                    Usuario user = Usuario.fromSnapshot(snapshot);

                    // 🔥 ACTUALIZAR SESIÓN
                    session.saveUser(user);

                    // 🔒 GUÍA NO HABILITADO
                    if ("Guia".equalsIgnoreCase(user.getRol())
                            && !"Habilitado".equalsIgnoreCase(user.getGuide_status())) {

                        startActivity(new Intent(this, GuidePendingActivity.class));
                        finish();
                        return;
                    }

                    Intent intent;
                    switch (user.getRol().toLowerCase()) {
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
                            intent = new Intent(this, WelcomeActivity.class);
                            break;
                    }

                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    startActivity(new Intent(this, WelcomeActivity.class));
                    finish();
                });
    }

}
