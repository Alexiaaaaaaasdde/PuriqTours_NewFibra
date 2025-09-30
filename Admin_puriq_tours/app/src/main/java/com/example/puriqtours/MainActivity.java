package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.adapter.TourAdapter;
import com.example.puriqtours.adapter.GuideAdapter;
import com.example.puriqtours.model.Tour;
import com.example.puriqtours.model.Guide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTours;
    private RecyclerView recyclerViewGuides;
    private TourAdapter tourAdapter;
    private GuideAdapter guideAdapter;
    private List<Tour> tourList;
    private List<Guide> guideList;

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
                // Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
                // startActivity(intent);
            });
        }

        // Inicializar RecyclerViews
        setupRecyclerViews();

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

    private void setupRecyclerViews() {
        // Configurar RecyclerView para tours
        recyclerViewTours = findViewById(R.id.recyclerViewTours);
        if (recyclerViewTours != null) {
            recyclerViewTours.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            
            // Crear lista de tours de ejemplo
            tourList = createSampleTours();
            tourAdapter = new TourAdapter(this, tourList);
            recyclerViewTours.setAdapter(tourAdapter);
        }

        // Configurar RecyclerView para guías
        recyclerViewGuides = findViewById(R.id.recyclerViewGuides);
        if (recyclerViewGuides != null) {
            recyclerViewGuides.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            
            // Crear lista de guías de ejemplo
            guideList = createSampleGuides();
            guideAdapter = new GuideAdapter(this, guideList);
            recyclerViewGuides.setAdapter(guideAdapter);
        }
    }

    private List<Tour> createSampleTours() {
        List<Tour> tours = new ArrayList<>();
        
        Tour tour1 = new Tour(1, "Machu Picchu", "Descripción del lugar donde se va realizar el tour", "Cusco", 350.0, 3);
        tour1.setImageResource(R.drawable.kuelap);
        tours.add(tour1);
        
        return tours;
    }

    private List<Guide> createSampleGuides() {
        List<Guide> guides = new ArrayList<>();
        
        guides.add(new Guide(1, "Alejandro G.", "Cusco", 5, true));
        guides.add(new Guide(2, "Christian F.", "Cusco", 4, true));
        guides.add(new Guide(3, "Nassim N.", "Cusco", 5, false));
        
        return guides;
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