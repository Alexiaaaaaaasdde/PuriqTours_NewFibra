package com.example.puriqtours.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button btnForgotOk;
    private ImageButton btnBackForgot; // 👈 declaramos el botón atrás

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Vincular botones
        btnForgotOk = findViewById(R.id.btnForgotOk);
        btnBackForgot = findViewById(R.id.btnBackForgot);

        // Acción al presionar "Aceptar"
        btnForgotOk.setOnClickListener(v -> {
            Intent i = new Intent(this, TempPasswordActivity.class);
            startActivity(i);
        });

        // Acción al presionar "Atrás"
        btnBackForgot.setOnClickListener(v -> {
            finish(); // 👈 vuelve a la pantalla anterior automáticamente

            // 👉 Si quieres que siempre regrese al LoginActivity, usa esto en lugar de finish():
            // Intent i = new Intent(this, LoginActivity.class);
            // startActivity(i);
            // finish();
        });
    }
}
