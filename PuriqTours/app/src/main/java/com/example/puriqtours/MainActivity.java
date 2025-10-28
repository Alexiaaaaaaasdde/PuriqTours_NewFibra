package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.admin.MainAdminActivity;
import com.example.puriqtours.guia.MainGuiaActivity;
import com.example.puriqtours.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnCliente, btnGuia, btnAdmin, btnSuperadmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencias a los botones
        btnCliente = findViewById(R.id.btnCliente);
        btnGuia = findViewById(R.id.btnGuia);
        btnAdmin = findViewById(R.id.btnAdmin);
        btnSuperadmin = findViewById(R.id.btnSuperadmin);

        // Cliente → LoginActivity
        btnCliente.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Guía → MainGuiaActivity
        btnGuia.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainGuiaActivity.class);
            startActivity(intent);
        });

        // Admin → PrincipalActivity
        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainAdminActivity.class);
            startActivity(intent);
        });

        // Superadmin → pendiente
        btnSuperadmin.setOnClickListener(v -> {
            // Aquí puedes agregar el intent cuando tengas la actividad lista
            // Por ahora, puedes mostrar un mensaje temporal
            // Toast.makeText(this, "Funcionalidad pendiente", Toast.LENGTH_SHORT).show();
        });
    }
}
