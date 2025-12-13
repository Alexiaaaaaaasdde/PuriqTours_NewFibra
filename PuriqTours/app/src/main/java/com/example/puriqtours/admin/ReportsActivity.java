package com.example.puriqtours.admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.puriqtours.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String adminUid;
    
    // Views para reportes
    private LinearLayout containerIngresosBarras, containerIngresosLabels;
    private LinearLayout containerDemandaBarras, containerDemandaLabels;
    private ProgressBar progressIngresos, progressDemanda;
    private TextView tvNoDataIngresos, tvNoDataDemanda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        
        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();
        adminUid = FirebaseAuth.getInstance().getUid();
        
        // Inicializar vistas
        initViews();
        
        // Cargar reportes
        loadIngresosReport();
        loadDemandaReport();

        // 🔹 Icono de notificaciones en toolbar
        ImageView notificationIcon = findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                // TODO: Implementar vista de notificaciones
                // Intent intent = new Intent(ReportsActivity.this, NotificationsActivity.class);
                // startActivity(intent);
            });
        }

        // 🔹 BottomNavigation
        setupBottomNavigation();
    }
    
    private void initViews() {
        // Ingresos
        containerIngresosBarras = findViewById(R.id.containerIngresosBarras);
        containerIngresosLabels = findViewById(R.id.containerIngresosLabels);
        progressIngresos = findViewById(R.id.progressIngresos);
        tvNoDataIngresos = findViewById(R.id.tvNoDataIngresos);
        
        // Demanda
        containerDemandaBarras = findViewById(R.id.containerDemandaBarras);
        containerDemandaLabels = findViewById(R.id.containerDemandaLabels);
        progressDemanda = findViewById(R.id.progressDemanda);
        tvNoDataDemanda = findViewById(R.id.tvNoDataDemanda);
    }
    
    // ========== REPORTE 1: INGRESOS POR TOUR ==========
    private void loadIngresosReport() {
        progressIngresos.setVisibility(View.VISIBLE);
        
        // Primero obtener todos los tours del admin
        db.collection("tours")
            .whereEqualTo("idEmpresa", adminUid)
            .get()
            .addOnSuccessListener(tourSnapshot -> {
                Map<String, String> tourNames = new HashMap<>();
                for (QueryDocumentSnapshot tourDoc : tourSnapshot) {
                    tourNames.put(tourDoc.getId(), tourDoc.getString("title"));
                }
                
                // Ahora obtener las reservas y calcular ingresos
                db.collection("reservas")
                    .get()
                    .addOnSuccessListener(reservaSnapshot -> {
                        Map<String, Double> ingresosPorTour = new HashMap<>();
                        
                        for (QueryDocumentSnapshot doc : reservaSnapshot) {
                            String idTour = doc.getString("idTour");
                            Double price = doc.getDouble("price");
                            
                            // Solo contar si es un tour de este admin
                            if (idTour != null && price != null && tourNames.containsKey(idTour)) {
                                ingresosPorTour.put(idTour, 
                                    ingresosPorTour.getOrDefault(idTour, 0.0) + price);
                            }
                        }
                        
                        displayIngresosChart(ingresosPorTour, tourNames);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("REPORTS", "Error cargando reservas", e);
                        showNoDataIngresos();
                    });
            })
            .addOnFailureListener(e -> {
                Log.e("REPORTS", "Error cargando tours", e);
                showNoDataIngresos();
            });
    }
    
    private void displayIngresosChart(Map<String, Double> ingresosPorTour, Map<String, String> tourNames) {
        progressIngresos.setVisibility(View.GONE);
        
        if (ingresosPorTour.isEmpty()) {
            showNoDataIngresos();
            return;
        }
        
        containerIngresosBarras.setVisibility(View.VISIBLE);
        containerIngresosLabels.setVisibility(View.VISIBLE);
        
        // Convertir a lista y ordenar por ingresos
        List<Map.Entry<String, Double>> sortedList = new ArrayList<>(ingresosPorTour.entrySet());
        Collections.sort(sortedList, (a, b) -> b.getValue().compareTo(a.getValue()));
        
        // Limitar a top 5
        int maxItems = Math.min(5, sortedList.size());
        double maxIngresos = sortedList.get(0).getValue();
        
        for (int i = 0; i < maxItems; i++) {
            Map.Entry<String, Double> entry = sortedList.get(i);
            String tourId = entry.getKey();
            double ingresos = entry.getValue();
            String tourName = tourNames.getOrDefault(tourId, "Tour " + (i+1));
            
            // Calcular altura proporcional (max 180dp)
            int barHeight = (int) ((ingresos / maxIngresos) * 180);
            
            // Crear barra
            LinearLayout barContainer = new LinearLayout(this);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            barContainer.setLayoutParams(barParams);
            barContainer.setOrientation(LinearLayout.VERTICAL);
            barContainer.setGravity(Gravity.CENTER);
            
            // Etiqueta de monto
            TextView montoLabel = new TextView(this);
            montoLabel.setText("S/" + String.format("%.0f", ingresos));
            montoLabel.setTextSize(10);
            montoLabel.setTextColor(Color.WHITE);
            montoLabel.setBackgroundColor(i == 0 ? Color.parseColor("#009688") : Color.parseColor("#BDBDBD"));
            montoLabel.setPadding(8, 4, 8, 4);
            montoLabel.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.setMargins(0, 0, 0, 8);
            montoLabel.setLayoutParams(labelParams);
            
            // Barra
            View bar = new View(this);
            LinearLayout.LayoutParams barHeightParams = new LinearLayout.LayoutParams(
                (int) (40 * getResources().getDisplayMetrics().density), 
                (int) (barHeight * getResources().getDisplayMetrics().density));
            bar.setLayoutParams(barHeightParams);
            bar.setBackgroundColor(i == 0 ? Color.parseColor("#009688") : Color.parseColor("#BDBDBD"));
            
            barContainer.addView(montoLabel);
            barContainer.addView(bar);
            containerIngresosBarras.addView(barContainer);
            
            // Etiqueta de nombre
            TextView nameLabel = new TextView(this);
            nameLabel.setText(tourName.length() > 8 ? tourName.substring(0, 8) + "..." : tourName);
            nameLabel.setTextSize(11);
            nameLabel.setTextColor(Color.parseColor("#666666"));
            nameLabel.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams nameLabelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            nameLabel.setLayoutParams(nameLabelParams);
            containerIngresosLabels.addView(nameLabel);
        }
    }
    
    private void showNoDataIngresos() {
        progressIngresos.setVisibility(View.GONE);
        tvNoDataIngresos.setVisibility(View.VISIBLE);
    }
    
    // ========== REPORTE 2: TOURS MÁS DEMANDADOS ==========
    private void loadDemandaReport() {
        progressDemanda.setVisibility(View.VISIBLE);
        
        // Primero obtener todos los tours del admin
        db.collection("tours")
            .whereEqualTo("idEmpresa", adminUid)
            .get()
            .addOnSuccessListener(tourSnapshot -> {
                Map<String, String> tourNames = new HashMap<>();
                for (QueryDocumentSnapshot tourDoc : tourSnapshot) {
                    tourNames.put(tourDoc.getId(), tourDoc.getString("title"));
                }
                
                // Ahora obtener las reservas y contar demanda
                db.collection("reservas")
                    .get()
                    .addOnSuccessListener(reservaSnapshot -> {
                        Map<String, Integer> demandaPorTour = new HashMap<>();
                        
                        for (QueryDocumentSnapshot doc : reservaSnapshot) {
                            String idTour = doc.getString("idTour");
                            
                            // Solo contar si es un tour de este admin
                            if (idTour != null && tourNames.containsKey(idTour)) {
                                demandaPorTour.put(idTour, 
                                    demandaPorTour.getOrDefault(idTour, 0) + 1);
                            }
                        }
                        
                        displayDemandaChart(demandaPorTour, tourNames);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("REPORTS", "Error cargando reservas", e);
                        showNoDataDemanda();
                    });
            })
            .addOnFailureListener(e -> {
                Log.e("REPORTS", "Error cargando tours", e);
                showNoDataDemanda();
            });
    }
    
    private void displayDemandaChart(Map<String, Integer> demandaPorTour, Map<String, String> tourNames) {
        progressDemanda.setVisibility(View.GONE);
        
        if (demandaPorTour.isEmpty()) {
            showNoDataDemanda();
            return;
        }
        
        containerDemandaBarras.setVisibility(View.VISIBLE);
        containerDemandaLabels.setVisibility(View.VISIBLE);
        
        // Convertir a lista y ordenar por demanda
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(demandaPorTour.entrySet());
        Collections.sort(sortedList, (a, b) -> b.getValue().compareTo(a.getValue()));
        
        // Limitar a top 6
        int maxItems = Math.min(6, sortedList.size());
        int maxDemanda = sortedList.get(0).getValue();
        
        for (int i = 0; i < maxItems; i++) {
            Map.Entry<String, Integer> entry = sortedList.get(i);
            String tourId = entry.getKey();
            int reservas = entry.getValue();
            String tourName = tourNames.getOrDefault(tourId, "Tour " + (i+1));
            
            // Calcular altura proporcional (max 220dp)
            int barHeight = (int) ((double) reservas / maxDemanda * 220);
            
            // Crear barra
            LinearLayout barContainer = new LinearLayout(this);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            barContainer.setLayoutParams(barParams);
            barContainer.setOrientation(LinearLayout.VERTICAL);
            barContainer.setGravity(Gravity.CENTER);
            
            // Etiqueta "Max" solo para el primero
            if (i == 0) {
                TextView maxLabel = new TextView(this);
                maxLabel.setText("Max");
                maxLabel.setTextSize(10);
                maxLabel.setTextColor(Color.WHITE);
                maxLabel.setBackgroundColor(Color.parseColor("#009688"));
                maxLabel.setPadding(12, 4, 12, 4);
                maxLabel.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams maxLabelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                maxLabelParams.setMargins(0, 0, 0, 8);
                maxLabel.setLayoutParams(maxLabelParams);
                barContainer.addView(maxLabel);
            }
            
            // Barra
            View bar = new View(this);
            LinearLayout.LayoutParams barHeightParams = new LinearLayout.LayoutParams(
                (int) (40 * getResources().getDisplayMetrics().density), 
                (int) (barHeight * getResources().getDisplayMetrics().density));
            bar.setLayoutParams(barHeightParams);
            bar.setBackgroundColor(i == 0 ? Color.parseColor("#009688") : Color.parseColor("#BDBDBD"));
            
            barContainer.addView(bar);
            containerDemandaBarras.addView(barContainer);
            
            // Etiqueta de nombre con cantidad
            TextView nameLabel = new TextView(this);
            String displayName = tourName.length() > 8 ? tourName.substring(0, 8) + "..." : tourName;
            nameLabel.setText(displayName + "\n(" + reservas + ")");
            nameLabel.setTextSize(10);
            nameLabel.setTextColor(Color.parseColor("#666666"));
            nameLabel.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams nameLabelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            nameLabel.setLayoutParams(nameLabelParams);
            containerDemandaLabels.addView(nameLabel);
        }
    }
    
    private void showNoDataDemanda() {
        progressDemanda.setVisibility(View.GONE);
        tvNoDataDemanda.setVisibility(View.VISIBLE);
    }
    
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_reports);
            
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_dashboard) {
                    startActivity(new Intent(this, MainAdminActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_reports) {
                    return true; // Ya estás en reports
                } else if (id == R.id.nav_chat) {
                    startActivity(new Intent(this, ChatListActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileAdminActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }
    }

    private void setupToolbar() {
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                // Cerrar sesión
                cerrarSesion();
            });
        }
    }
    
    private void cerrarSesion() {
        // Mostrar diálogo de confirmación
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
                    // 1. Cerrar sesión de Firebase Authentication
                    FirebaseAuth.getInstance().signOut();
                    
                    // 2. Limpiar datos de sesión en SharedPreferences
                    android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    
                    // 3. Ir al login
                    android.content.Intent intent = new android.content.Intent(ReportsActivity.this, com.example.puriqtours.login.LoginActivity.class);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    
                    android.widget.Toast.makeText(this, "Sesión cerrada exitosamente", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
