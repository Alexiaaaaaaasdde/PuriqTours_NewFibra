package com.example.puriqtours;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationView;

public class HistorialActivity extends AppCompatActivity {

    private TextView tvEstado1, tvEstado2, tvEstado3, tvEstado4;
    private TextView tvTitulo1, tvTitulo2, tvTitulo3, tvTitulo4;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

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

        // 🔹 Referencias a títulos
        tvTitulo1 = findViewById(R.id.tvTitulo1);
        tvTitulo2 = findViewById(R.id.tvTitulo2);
        tvTitulo3 = findViewById(R.id.tvTitulo3);
        tvTitulo4 = findViewById(R.id.tvTitulo4);

        // 🔹 Referencias a estados
        tvEstado1 = findViewById(R.id.tvEstado1);
        tvEstado2 = findViewById(R.id.tvEstado2);
        tvEstado3 = findViewById(R.id.tvEstado3);
        tvEstado4 = findViewById(R.id.tvEstado4);

        // 🔹 Pintar colores
        pintarEstado(tvEstado1);
        pintarEstado(tvEstado2);
        pintarEstado(tvEstado3);
        pintarEstado(tvEstado4);

        // 🔹 Buscador
        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarTours(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 🔹 Chips de filtro
        Chip chipTodos = findViewById(R.id.chipTodos);
        Chip chipEnProceso = findViewById(R.id.chipEnProceso);
        Chip chipFinalizado = findViewById(R.id.chipFinalizado);
        Chip chipReservado = findViewById(R.id.chipReservado);

        chipTodos.setOnClickListener(v -> aplicarFiltroEstado("")); // muestra todo
        chipEnProceso.setOnClickListener(v -> aplicarFiltroEstado("En proceso"));
        chipFinalizado.setOnClickListener(v -> aplicarFiltroEstado("Finalizado"));
        chipReservado.setOnClickListener(v -> aplicarFiltroEstado("Reservado"));

        // 🔹 Drawer references
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // 👉 abrir menú lateral con ☰
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
            } else if (id == R.id.nav_historial) {
                // ya estás en historial
            } else if (id == R.id.nav_logout) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void pintarEstado(TextView tvEstado) {
        if (tvEstado == null) return;
        String texto = tvEstado.getText().toString().toLowerCase();
        if (texto.contains("finalizado")) {
            tvEstado.setTextColor(Color.RED);
        } else if (texto.contains("en proceso")) {
            tvEstado.setTextColor(Color.parseColor("#FFA500")); // naranja
        } else if (texto.contains("reservado")) {
            tvEstado.setTextColor(Color.parseColor("#388E3C")); // verde
        }
    }

    private void filtrarTours(String query) {
        filtrarCard(tvTitulo1, query);
        filtrarCard(tvTitulo2, query);
        filtrarCard(tvTitulo3, query);
        filtrarCard(tvTitulo4, query);
    }

    private void filtrarCard(TextView tvTitulo, String query) {
        if (tvTitulo == null) return;
        View card = (View) tvTitulo.getParent().getParent(); // sube hasta CardView
        if (tvTitulo.getText().toString().toLowerCase().contains(query.toLowerCase())) {
            card.setVisibility(View.VISIBLE);
        } else {
            card.setVisibility(View.GONE);
        }
    }

    private void aplicarFiltroEstado(String estado) {
        aplicarFiltro(tvEstado1, estado);
        aplicarFiltro(tvEstado2, estado);
        aplicarFiltro(tvEstado3, estado);
        aplicarFiltro(tvEstado4, estado);
    }

    private void aplicarFiltro(TextView tvEstado, String estado) {
        if (tvEstado == null) return;
        View card = (View) tvEstado.getParent().getParent();
        if (estado.isEmpty() || tvEstado.getText().toString().toLowerCase().contains(estado.toLowerCase())) {
            card.setVisibility(View.VISIBLE);
        } else {
            card.setVisibility(View.GONE);
        }
    }
}
