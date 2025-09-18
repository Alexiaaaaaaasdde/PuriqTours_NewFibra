package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FinalizadoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserva_finalizada);

        // Flecha de retroceso
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Botón Valorar → abre el formulario
        Button btnValorar = findViewById(R.id.btnValorar);
        btnValorar.setOnClickListener(v -> {
            startActivity(new Intent(FinalizadoActivity.this, ValoracionActivity.class));
        });
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        // Marcar esta pestaña como activa (ejemplo: historial)
        bottomNavigation.setSelectedItemId(R.id.nav_historial);

        // Listener para cambiar de pestaña
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}
