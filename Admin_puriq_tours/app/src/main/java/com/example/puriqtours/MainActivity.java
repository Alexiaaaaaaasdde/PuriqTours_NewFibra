package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Icono de notificaciones en toolbar
        ImageView notificationIcon = findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                // TODO: Implementar vista de notificaciones
                Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show();
            });
        }

        // 🔹 Configurar toolbar con botón de logout
        setupToolbar();

        // 🔹 Navegación a vista de tours
        TextView tvLatestTours = findViewById(R.id.tvLatestTours);
        TextView tvViewMore = findViewById(R.id.tvViewMore);

        if (tvLatestTours != null) {
            tvLatestTours.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ToursActivity.class);
                startActivity(intent);
            });
        }

        if (tvViewMore != null) {
            tvViewMore.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ToursActivity.class);
                startActivity(intent);
            });
        }

        // 🔹 Navegación a vista de guías
        TextView tvGuidesList = findViewById(R.id.tvGuidesList);
        if (tvGuidesList != null) {
            tvGuidesList.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GuidesActivity.class);
                startActivity(intent);
            });
        }

        // 🔹 BottomNavigation
        setupBottomNavigation();
    }

    private void setupToolbar() {
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                // TODO: Implementar cerrar sesión
                Toast.makeText(this, "Cerrar sesión", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
            
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_dashboard) {
                    return true; // Ya estás en dashboard
                } else if (id == R.id.nav_reports) {
                    startActivity(new Intent(this, ReportsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }
    }
}