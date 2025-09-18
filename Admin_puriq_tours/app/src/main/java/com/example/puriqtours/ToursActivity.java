package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;

public class ToursActivity extends AppCompatActivity {

    private CardView cardTour1, cardTour2, cardTour3, cardTour4, cardTour5;
    private Button btnFiltrar;
    private FloatingActionButton fabCrearTour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tours);

        // Inicializar vistas
        initViews();
        
        // Configurar listeners
        setupListeners();
        
        // Configurar bottom navigation
        setupBottomNavigation();
    }

    private void initViews() {
        btnFiltrar = findViewById(R.id.btnFiltro);
        fabCrearTour = findViewById(R.id.fabCrearTour);
        
        // Cards de tours
        cardTour1 = findViewById(R.id.cardTour1);
        cardTour2 = findViewById(R.id.cardTour2);
        cardTour3 = findViewById(R.id.cardTour3);
        cardTour4 = findViewById(R.id.cardTour4);
        cardTour5 = findViewById(R.id.cardTour5);
    }

    private void setupListeners() {
        // Icono de notificaciones en toolbar
        ImageView notificationIcon = findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                // TODO: Implementar vista de notificaciones
                // Intent intent = new Intent(ToursActivity.this, NotificationsActivity.class);
                // startActivity(intent);
            });
        }

        // Configurar toolbar navigation (botón atrás)
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> {
                finish(); // Volver a MainActivity
            });
        }

        // Botón de filtro por departamento
        btnFiltrar.setOnClickListener(v -> showFilterDialog());

        // FloatingActionButton para crear nuevo tour
        fabCrearTour.setOnClickListener(v -> {
            Intent intent = new Intent(ToursActivity.this, CreateTourActivity.class);
            startActivityForResult(intent, 100);
        });

        // Click listeners para tours existentes
        setupTourClickListeners();
    }

    private void setupTourClickListeners() {
        if (cardTour1 != null) {
            cardTour1.setOnClickListener(v -> {
                Intent intent = new Intent(ToursActivity.this, TourDetailActivity.class);
                intent.putExtra("tour_id", 1);
                intent.putExtra("tour_name", "Tour Machupicchu");
                startActivityForResult(intent, 200);
            });
        }

        if (cardTour2 != null) {
            cardTour2.setOnClickListener(v -> {
                Intent intent = new Intent(ToursActivity.this, TourDetailActivity.class);
                intent.putExtra("tour_id", 2);
                intent.putExtra("tour_name", "Tour Lima Centro");
                startActivityForResult(intent, 200);
            });
        }

        if (cardTour3 != null) {
            cardTour3.setOnClickListener(v -> {
                Intent intent = new Intent(ToursActivity.this, TourDetailActivity.class);
                intent.putExtra("tour_id", 3);
                intent.putExtra("tour_name", "Tour Cusco Aventura");
                startActivityForResult(intent, 200);
            });
        }

        if (cardTour4 != null) {
            cardTour4.setOnClickListener(v -> {
                Intent intent = new Intent(ToursActivity.this, TourDetailActivity.class);
                intent.putExtra("tour_id", 4);
                intent.putExtra("tour_name", "Tour Amazonas");
                startActivityForResult(intent, 200);
            });
        }

        if (cardTour5 != null) {
            cardTour5.setOnClickListener(v -> {
                Intent intent = new Intent(ToursActivity.this, TourDetailActivity.class);
                intent.putExtra("tour_id", 5);
                intent.putExtra("tour_name", "Tour Arequipa");
                startActivityForResult(intent, 200);
            });
        }
    }

    private void showFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filtro_departamentos, null);

        EditText etBuscar = dialogView.findViewById(R.id.etBuscar);
        ListView listDepartamentos = dialogView.findViewById(R.id.listDepartamentos);

        // Cargar departamentos del Perú
        String[] departamentos = getResources().getStringArray(R.array.departamentos_peru);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>(Arrays.asList(departamentos))
        );

        listDepartamentos.setAdapter(adapter);

        // Filtro de búsqueda en tiempo real
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Acción al seleccionar un departamento
        listDepartamentos.setOnItemClickListener((parent, view, position, id) -> {
            String departamentoSeleccionado = adapter.getItem(position);
            filterToursByDepartment(departamentoSeleccionado);
            
            Toast.makeText(ToursActivity.this,
                    "Filtrando tours de " + departamentoSeleccionado,
                    Toast.LENGTH_SHORT).show();
        });

        // Mostrar dialog
        new AlertDialog.Builder(this)
                .setTitle("Filtrar por departamento")
                .setView(dialogView)
                .setNegativeButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Mostrar todos", (dialog, which) -> {
                    showAllTours();
                    dialog.dismiss();
                })
                .show();
    }

    private void filterToursByDepartment(String departamento) {
        // Ocultar todos los tours primero
        hideAllTours();

        // Mostrar tours según el departamento seleccionado
        switch (departamento.toLowerCase()) {
            case "cusco":
                if (cardTour1 != null) cardTour1.setVisibility(View.VISIBLE);
                if (cardTour3 != null) cardTour3.setVisibility(View.VISIBLE);
                break;
            case "lima":
                if (cardTour2 != null) cardTour2.setVisibility(View.VISIBLE);
                break;
            case "amazonas":
                if (cardTour4 != null) cardTour4.setVisibility(View.VISIBLE);
                break;
            case "arequipa":
                if (cardTour5 != null) cardTour5.setVisibility(View.VISIBLE);
                break;
            default:
                // Si no hay tours para ese departamento, mostrar mensaje
                Toast.makeText(this, "No hay tours disponibles para " + departamento, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void hideAllTours() {
        if (cardTour1 != null) cardTour1.setVisibility(View.GONE);
        if (cardTour2 != null) cardTour2.setVisibility(View.GONE);
        if (cardTour3 != null) cardTour3.setVisibility(View.GONE);
        if (cardTour4 != null) cardTour4.setVisibility(View.GONE);
        if (cardTour5 != null) cardTour5.setVisibility(View.GONE);
    }

    private void showAllTours() {
        if (cardTour1 != null) cardTour1.setVisibility(View.VISIBLE);
        if (cardTour2 != null) cardTour2.setVisibility(View.VISIBLE);
        if (cardTour3 != null) cardTour3.setVisibility(View.VISIBLE);
        if (cardTour4 != null) cardTour4.setVisibility(View.VISIBLE);
        if (cardTour5 != null) cardTour5.setVisibility(View.VISIBLE);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard); // No hay item específico para tours
            
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_dashboard) {
                    startActivity(new Intent(this, MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("tour_created", false)) {
                Toast.makeText(this, "¡Tour creado exitosamente!", Toast.LENGTH_LONG).show();
                // Aquí podrías actualizar la lista de tours o recargar los datos
                // refreshToursList();
            }
        } else if (requestCode == 200 && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("tour_updated", false)) {
                Toast.makeText(this, "¡Tour actualizado exitosamente!", Toast.LENGTH_LONG).show();
                // Aquí podrías actualizar la lista de tours o recargar los datos
                // refreshToursList();
            }
        }
    }
}
