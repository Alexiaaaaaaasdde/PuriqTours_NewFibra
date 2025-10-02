package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView recyclerHistorial;
    private HistorialAdapter adapter;
    private List<HistorialTour> listaTours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // 🔹 BottomNavigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_historial);
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
                return true; // ya estamos aquí
            }
            return false;
        });

        // 🔹 Drawer references
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
            } else if (id == R.id.nav_logout) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // 🔹 RecyclerView
        recyclerHistorial = findViewById(R.id.recyclerHistorial);
        recyclerHistorial.setLayoutManager(new LinearLayoutManager(this));

        // 🔹 Datos estáticos
        listaTours = new ArrayList<>();
        listaTours.add(new HistorialTour("Tour número 1", "Kuelap", "Reservado", "3h", 4, "165", R.drawable.kuelap));
        listaTours.add(new HistorialTour("Tour número 2", "Lima", "En proceso", "3h", 4, "120", R.drawable.kuelap));
        listaTours.add(new HistorialTour("Tour número 3", "Cusco", "Finalizado", "3h", 4, "120", R.drawable.kuelap));

        // ⚠️ IMPORTANTE: pasar context en el adapter
        adapter = new HistorialAdapter(listaTours, this);
        recyclerHistorial.setAdapter(adapter);

        // 🔹 Buscador
        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 🔹 Chips de filtro
        Chip chipTodos = findViewById(R.id.chipTodos);
        Chip chipEnProceso = findViewById(R.id.chipEnProceso);
        Chip chipFinalizado = findViewById(R.id.chipFinalizado);
        Chip chipReservado = findViewById(R.id.chipReservado);

        chipTodos.setOnClickListener(v -> adapter.filtrarEstado(""));
        chipEnProceso.setOnClickListener(v -> adapter.filtrarEstado("En proceso"));
        chipFinalizado.setOnClickListener(v -> adapter.filtrarEstado("Finalizado"));
        chipReservado.setOnClickListener(v -> adapter.filtrarEstado("Reservado"));
    }
}
