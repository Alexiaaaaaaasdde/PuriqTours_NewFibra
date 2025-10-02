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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.adapter.GuideAdapter;
import com.example.puriqtours.model.Guide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuidesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewGuides;
    private GuideAdapter guideAdapter;
    private List<Guide> guideList;
    private Button btnFiltrar;
    private TextInputEditText etBuscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guides);

        // Inicializar vistas
        initViews();
        
        // Crear datos de ejemplo
        createSampleData();
        
        // Configurar RecyclerView
        setupRecyclerView();
        
        // Configurar listeners
        setupListeners();
        
        // Configurar bottom navigation
        setupBottomNavigation();
    }

    private void initViews() {
        recyclerViewGuides = findViewById(R.id.recyclerViewGuides);
        btnFiltrar = findViewById(R.id.btnFiltro);
        etBuscar = findViewById(R.id.etBuscar);
        
        // Verificar que las vistas críticas existan
        if (recyclerViewGuides == null) {
            Toast.makeText(this, "Error: RecyclerView no encontrado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    private void createSampleData() {
        guideList = new ArrayList<>();
        guideList.add(new Guide(1, "Nassim Ahmed", "Cusco", 5, true, R.drawable.avatar));
        guideList.add(new Guide(2, "Carlos Mendoza", "Cusco", 4, false, R.drawable.avatar));
        guideList.add(new Guide(3, "Ana Torres", "Cusco", 5, true, R.drawable.avatar));
        guideList.add(new Guide(4, "Pedro Silva", "Cusco", 5, true, R.drawable.avatar));
        guideList.add(new Guide(5, "María Quispe", "Cusco", 4, false, R.drawable.avatar));
    }

    private void setupRecyclerView() {
        guideAdapter = new GuideAdapter(this, guideList);
        recyclerViewGuides.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewGuides.setAdapter(guideAdapter);
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

        // Configurar toolbar navigation (botón de logout)
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                // TODO: Implementar cerrar sesión
                Toast.makeText(this, "Cerrar sesión", Toast.LENGTH_SHORT).show();
            });
        }

        // Botón de filtro por provincia
        if (btnFiltrar != null) {
            btnFiltrar.setOnClickListener(v -> showFilterDialog());
        }

        // Configurar búsqueda en tiempo real
        if (etBuscar != null) {
            etBuscar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (guideAdapter != null) {
                        guideAdapter.getFilter().filter(s);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
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
            
            // Usar el método filterByDepartment del adapter
            if (guideAdapter != null) {
                guideAdapter.filterByDepartment(departamentoSeleccionado);
            }
            
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
                    if (guideAdapter != null) {
                        guideAdapter.filterByDepartment("todos");
                    }
                    dialog.dismiss();
                })
                .show();
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
