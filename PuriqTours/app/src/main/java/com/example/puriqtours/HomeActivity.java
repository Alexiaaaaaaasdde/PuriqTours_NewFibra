package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.entity.Company;
import com.example.puriqtours.entity.Tour;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvFavoriteCompanies;
    private RecyclerView rvRecentTours;
    private CompanyAdapter companyAdapter;
    private TourAdapter tourAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Configurar el nombre de usuario
        String username = getIntent().getStringExtra("username");
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        if (username != null) {
            tvWelcome.setText("Hey, " + username);
        }

        // Configurar la barra de búsqueda
        setupSearchBar();

        // Configurar filtros
        setupFilters();

        // Configurar botón de búsqueda
        setupSearchButton();

        // Configurar RecyclerViews
        setupRecyclerViews();

        // Configurar la navegación inferior
        setupBottomNavigation();

        // Configurar el texto "Ver todos"
        setupViewAll();
    }

    private void setupSearchBar() {
        // Implementar funcionalidad para la barra de búsqueda
    }

    private void setupFilters() {
        // Implementar funcionalidad para los filtros
    }

    private void setupSearchButton() {
        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Buscando tours...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerViews() {
        // Configurar RecyclerView para empresas favoritas
        rvFavoriteCompanies = findViewById(R.id.rvFavoriteCompanies);
        rvFavoriteCompanies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Datos de ejemplo para empresas
        List<Company> companies = new ArrayList<>();
        companies.add(new Company("Pixels Tower", R.drawable.company1, 4.5f));
        companies.add(new Company("Androm Explorer", R.drawable.company2, 4.8f));
        companies.add(new Company("Inca Pathway", R.drawable.company3, 4.9f));
        companies.add(new Company("Thebin Picchu Dulder", R.drawable.company4, 4.7f));

        companyAdapter = new CompanyAdapter(companies);
        rvFavoriteCompanies.setAdapter(companyAdapter);

        // Configurar RecyclerView para tours recientes
        rvRecentTours = findViewById(R.id.rvRecentTours);
        rvRecentTours.setLayoutManager(new LinearLayoutManager(this));

        // Datos de ejemplo para tours
        List<Tour> tours = new ArrayList<>();
        tours.add(new Tour("Tour número 2", "Custom", "Finalized", 5));
        tours.add(new Tour("Tour número 3", "Arcquipe", "En process", 6));
        tours.add(new Tour("Tour número 4", "Line", "Reservado", 0));
        tours.add(new Tour("Tour número 5", "Limbas", "Reservado", 0));

        tourAdapter = new TourAdapter(tours);
        rvRecentTours.setAdapter(tourAdapter);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_search) {
                // Ya estamos en la búsqueda (Home)
                return true;
            } else if (itemId == R.id.navigation_explore) {
                // Ir a la actividad de exploración
                Intent exploreIntent = new Intent(HomeActivity.this, ExploreActivity.class);
                startActivity(exploreIntent);
                return true;
            } else if (itemId == R.id.navigation_trips) {
                // Ir a la actividad de viajes
                Intent tripsIntent = new Intent(HomeActivity.this, TripsActivity.class);
                startActivity(tripsIntent);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Ir a la actividad de perfil
                Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            }
            return false;
        });
    }

    private void setupViewAll() {
        TextView tvViewAll = findViewById(R.id.tvViewAll);
        tvViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent historyIntent = new Intent(HomeActivity.this, ReservationHistoryActivity.class);
                startActivity(historyIntent);
            }
        });
    }
}