package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Arrays;

public class ToursActivity extends AppCompatActivity {

    private CardView cardKuelap, cardLima1, cardCusco2, cardCusco3;
    private Button btnFiltrar;
    private ChipGroup chipGroupFiltros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tours);

        // 🔹 Inicializamos vistas
        btnFiltrar = findViewById(R.id.btnFiltro);
        cardKuelap = findViewById(R.id.cardKuelap);
        cardLima1 = findViewById(R.id.cardLima1);
        cardCusco2 = findViewById(R.id.cardCusco2);
        cardCusco3 = findViewById(R.id.cardCusco3);
        chipGroupFiltros = findViewById(R.id.chipGroupFiltros);

        // 🔹 Configurar chips de filtro
        setupChipFilters();

        // 🔹 Icono de perfil en toolbar
        ShapeableImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ToursActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // 🔹 Listener del botón filtrar (mantener funcionalidad existente)
        if (btnFiltrar != null) {
            btnFiltrar.setOnClickListener(v -> {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_filtro_departamentos, null);

                EditText etBuscar = dialogView.findViewById(R.id.etBuscar);
                ListView listDepartamentos = dialogView.findViewById(R.id.listDepartamentos);

                // Cargar departamentos
                String[] departamentos = getResources().getStringArray(R.array.departamentos_peru);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        new ArrayList<>(Arrays.asList(departamentos))
                );

                listDepartamentos.setAdapter(adapter);

                // Filtro búsqueda
                etBuscar.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        adapter.getFilter().filter(s);
                    }
                    @Override public void afterTextChanged(Editable s) {}
                });

                // Acción al elegir un departamento
                listDepartamentos.setOnItemClickListener((parent, view1, position, id) -> {
                    String seleccionado = adapter.getItem(position);
                    filterToursByDepartment(seleccionado);
                    Toast.makeText(ToursActivity.this,
                            "Filtrando tours de " + seleccionado,
                            Toast.LENGTH_SHORT).show();
                });

                new AlertDialog.Builder(ToursActivity.this)
                        .setTitle("Filtrar por departamento")
                        .setView(dialogView)
                        .setNegativeButton("Cerrar", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        // 🔹 Click listeners para las cards
        cardKuelap.setOnClickListener(v -> {
            Intent intent = new Intent(ToursActivity.this, DetalleTourActivity.class);
            intent.putExtra("titulo", "Ciudadela de Kuélap");
            intent.putExtra("precio", "S/ 165.02");
            intent.putExtra("ubicacion", "Chachapoyas - Amazonas");
            startActivity(intent);
        });

        cardLima1.setOnClickListener(v -> {
            Intent intent = new Intent(ToursActivity.this, DetalleTourActivity.class);
            intent.putExtra("titulo", "Tour Lima Centro Histórico");
            intent.putExtra("precio", "S/ 120.00");
            intent.putExtra("ubicacion", "Lima Centro");
            startActivity(intent);
        });

        cardCusco2.setOnClickListener(v -> {
            Intent intent = new Intent(ToursActivity.this, DetalleTourActivity.class);
            intent.putExtra("titulo", "Tour Cusco Mágico");
            intent.putExtra("precio", "S/ 120.00");
            intent.putExtra("ubicacion", "Cusco");
            startActivity(intent);
        });

        cardCusco3.setOnClickListener(v -> {
            Intent intent = new Intent(ToursActivity.this, DetalleTourActivity.class);
            intent.putExtra("titulo", "Cusco Imperial");
            intent.putExtra("precio", "S/ 120.00");
            intent.putExtra("ubicacion", "Cusco");
            startActivity(intent);
        });

        // 🔹 Bottom Navigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_tours);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.nav_tours) {
                // Ya estamos en tours
                return true;

            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    private void setupChipFilters() {
        chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // Si no hay ningún chip seleccionado, mostrar todos
                showAllTours();
                return;
            }

            // Obtener el chip seleccionado
            int checkedId = checkedIds.get(0);
            Chip selectedChip = findViewById(checkedId);

            if (selectedChip != null) {
                String filterText = selectedChip.getText().toString();
                filterToursByDepartment(filterText);

                Toast.makeText(this,
                        "Filtrando tours de " + filterText,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterToursByDepartment(String department) {
        // Ocultar todas las cards primero
        cardKuelap.setVisibility(View.GONE);
        cardLima1.setVisibility(View.GONE);
        cardCusco2.setVisibility(View.GONE);
        cardCusco3.setVisibility(View.GONE);

        // Mostrar cards según el departamento seleccionado
        switch (department.toLowerCase()) {
            case "amazonas":
                cardKuelap.setVisibility(View.VISIBLE);
                break;
            case "lima":
                cardLima1.setVisibility(View.VISIBLE);
                break;
            case "cusco":
                cardCusco2.setVisibility(View.VISIBLE);
                cardCusco3.setVisibility(View.VISIBLE);
                break;
            case "arequipa":
            case "tumbes":
            case "piura":
                // Por ahora no tienes tours de estos departamentos
                Toast.makeText(this, "No hay tours disponibles para " + department, Toast.LENGTH_SHORT).show();
                break;
            default:
                showAllTours();
                break;
        }
    }

    private void showAllTours() {
        cardKuelap.setVisibility(View.VISIBLE);
        cardLima1.setVisibility(View.VISIBLE);
        cardCusco2.setVisibility(View.VISIBLE);
        cardCusco3.setVisibility(View.VISIBLE);
    }
}