package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.guia.RegisterGuideActivity;
import com.example.puriqtours.login.LoginActivity;
import com.example.puriqtours.login.RegisterActivity;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    private MaterialButton btnLogin;
    private MaterialButton btnRegisterUser;
    private MaterialButton btnRegisterGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Inicializar vistas
        initViews();

        // Configurar animaciones
        setupAnimations();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        btnLogin = findViewById(R.id.btnLogin);
        btnRegisterUser = findViewById(R.id.btnRegisterUser);
        btnRegisterGuide = findViewById(R.id.btnRegisterGuide);
    }

    private void setupAnimations() {
        // Animación de fade in para los botones
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(800);
        fadeIn.setStartOffset(300);

        btnLogin.startAnimation(fadeIn);

        Animation fadeIn2 = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn2.setDuration(800);
        fadeIn2.setStartOffset(500);
        btnRegisterUser.startAnimation(fadeIn2);

        Animation fadeIn3 = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn3.setDuration(800);
        fadeIn3.setStartOffset(700);
        btnRegisterGuide.startAnimation(fadeIn3);
    }

    private void setupListeners() {
        // Botón Iniciar Sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la actividad de login
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // Botón Registrarse como Usuario
        btnRegisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la actividad de registro de usuario
                Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                intent.putExtra("USER_TYPE", "TOURIST"); // Indicar que es un turista
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // Botón Registrarse como Guía
        btnRegisterGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la actividad de registro de guía
                Intent intent = new Intent(WelcomeActivity.this, RegisterGuideActivity.class);
                intent.putExtra("USER_TYPE", "GUIDE"); // Indicar que es un guía
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

}