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

import java.util.ArrayList;
import java.util.Arrays;

public class GuidesActivity extends AppCompatActivity {

    private CardView cardGuide1, cardGuide2, cardGuide3, cardGuide4, cardGuide5;
    private Button btnFiltrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guides);

        // Inicializar vistas
        initViews();
        
        // Configurar listeners
        setupListeners();
        
        // Configurar bottom navigation
        setupBottomNavigation();
    }

    private void initViews() {
        btnFiltrar = findViewById(R.id.btnFiltro);
        
        // Cards de guías
        cardGuide1 = findViewById(R.id.cardGuide1);
        cardGuide2 = findViewById(R.id.cardGuide2);
        cardGuide3 = findViewById(R.id.cardGuide3);
        cardGuide4 = findViewById(R.id.cardGuide4);
        cardGuide5 = findViewById(R.id.cardGuide5);
    }

    private void setupListeners() {
        // Icono de notificaciones en toolbar
        ImageView notificationIcon = findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                // TODO: Implementar vista de notificaciones
                Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show();
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

        // Botón de filtro por provincia
        btnFiltrar.setOnClickListener(v -> showFilterDialog());

        // Click listeners para guías
        setupGuideClickListeners();
    }

    private void setupGuideClickListeners() {
        if (cardGuide1 != null) {
            cardGuide1.setOnClickListener(v -> {
                Intent intent = new Intent(GuidesActivity.this, GuideDetailActivity.class);
                intent.putExtra("guide_id", 1);
                intent.putExtra("guide_name", "Nassim Ahmed");
                intent.putExtra("guide_location", "Cusco");
                intent.putExtra("guide_rating", 5);
                intent.putExtra("guide_available", true);
                startActivity(intent);
            });
        }

        if (cardGuide2 != null) {
            cardGuide2.setOnClickListener(v -> {
                Intent intent = new Intent(GuidesActivity.this, GuideDetailActivity.class);
                intent.putExtra("guide_id", 2);
                intent.putExtra("guide_name", "Carlos Mendoza");
                intent.putExtra("guide_location", "Cusco");
                intent.putExtra("guide_rating", 4);
                intent.putExtra("guide_available", false);
                startActivity(intent);
            });
        }

        if (cardGuide3 != null) {
            cardGuide3.setOnClickListener(v -> {
                Intent intent = new Intent(GuidesActivity.this, GuideDetailActivity.class);
                intent.putExtra("guide_id", 3);
                intent.putExtra("guide_name", "Ana Torres");
                intent.putExtra("guide_location", "Cusco");
                intent.putExtra("guide_rating", 5);
                intent.putExtra("guide_available", true);
                startActivity(intent);
            });
        }

        if (cardGuide4 != null) {
            cardGuide4.setOnClickListener(v -> {
                Intent intent = new Intent(GuidesActivity.this, GuideDetailActivity.class);
                intent.putExtra("guide_id", 4);
                intent.putExtra("guide_name", "Pedro Silva");
                intent.putExtra("guide_location", "Cusco");
                intent.putExtra("guide_rating", 5);
                intent.putExtra("guide_available", true);
                startActivity(intent);
            });
        }

        if (cardGuide5 != null) {
            cardGuide5.setOnClickListener(v -> {
                Intent intent = new Intent(GuidesActivity.this, GuideDetailActivity.class);
                intent.putExtra("guide_id", 5);
                intent.putExtra("guide_name", "María Quispe");
                intent.putExtra("guide_location", "Cusco");
                intent.putExtra("guide_rating", 4);
                intent.putExtra("guide_available", false);
                startActivity(intent);
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
            filterGuidesByDepartment(departamentoSeleccionado);
            
            Toast.makeText(GuidesActivity.this,
                    "Filtrando guías de " + departamentoSeleccionado,
                    Toast.LENGTH_SHORT).show();
        });

        // Mostrar dialog
        new AlertDialog.Builder(this)
                .setTitle("Filtrar por departamento")
                .setView(dialogView)
                .setNegativeButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Mostrar todos", (dialog, which) -> {
                    showAllGuides();
                    dialog.dismiss();
                })
                .show();
    }

    private void filterGuidesByDepartment(String departamento) {
        // Ocultar todos los guías primero
        hideAllGuides();

        // Mostrar guías según el departamento seleccionado
        switch (departamento.toLowerCase()) {
            case "cusco":
                if (cardGuide1 != null) cardGuide1.setVisibility(View.VISIBLE);
                if (cardGuide2 != null) cardGuide2.setVisibility(View.VISIBLE);
                if (cardGuide3 != null) cardGuide3.setVisibility(View.VISIBLE);
                if (cardGuide4 != null) cardGuide4.setVisibility(View.VISIBLE);
                if (cardGuide5 != null) cardGuide5.setVisibility(View.VISIBLE);
                break;
            case "lima":
                // Aquí mostrarías guías de Lima si los tienes
                Toast.makeText(this, "No hay guías disponibles para " + departamento, Toast.LENGTH_SHORT).show();
                break;
            case "arequipa":
                // Aquí mostrarías guías de Arequipa si los tienes
                Toast.makeText(this, "No hay guías disponibles para " + departamento, Toast.LENGTH_SHORT).show();
                break;
            default:
                // Si no hay guías para ese departamento, mostrar mensaje
                Toast.makeText(this, "No hay guías disponibles para " + departamento, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void hideAllGuides() {
        if (cardGuide1 != null) cardGuide1.setVisibility(View.GONE);
        if (cardGuide2 != null) cardGuide2.setVisibility(View.GONE);
        if (cardGuide3 != null) cardGuide3.setVisibility(View.GONE);
        if (cardGuide4 != null) cardGuide4.setVisibility(View.GONE);
        if (cardGuide5 != null) cardGuide5.setVisibility(View.GONE);
    }

    private void showAllGuides() {
        if (cardGuide1 != null) cardGuide1.setVisibility(View.VISIBLE);
        if (cardGuide2 != null) cardGuide2.setVisibility(View.VISIBLE);
        if (cardGuide3 != null) cardGuide3.setVisibility(View.VISIBLE);
        if (cardGuide4 != null) cardGuide4.setVisibility(View.VISIBLE);
        if (cardGuide5 != null) cardGuide5.setVisibility(View.VISIBLE);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            // No seleccionar ningún ítem por defecto en esta vista
            
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
}
