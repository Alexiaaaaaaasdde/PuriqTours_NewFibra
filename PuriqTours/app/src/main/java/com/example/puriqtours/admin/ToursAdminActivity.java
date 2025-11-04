package com.example.puriqtours.admin;

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

import com.example.puriqtours.helper.NotificationHelper;
import com.example.puriqtours.R;
import com.example.puriqtours.helper.StorageHelper;
import com.example.puriqtours.adapter.TourAdapter;
import com.example.puriqtours.cliente.ProfileActivity;
import com.example.puriqtours.entity.TourAdmin;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToursAdminActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTours;
    private TourAdapter tourAdapter;
    private List<TourAdmin> tourAdminList;
    private Button btnFiltrar;
    private FloatingActionButton fabCrearTour;
    private TextInputEditText searchBar;
    private StorageHelper storageHelper;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tours_admin);

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
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar la lista cuando se regrese a esta actividad
        if (storageHelper != null && tourAdapter != null) {
            refreshToursList();
        }
    }

    private void initViews() {
        recyclerViewTours = findViewById(R.id.recyclerViewTours);
        btnFiltrar = findViewById(R.id.btnFiltro);
        fabCrearTour = findViewById(R.id.fabCrearTour);
        searchBar = findViewById(R.id.searchBar);
    }

    private void createSampleData() {
        storageHelper = new StorageHelper(this);
        notificationHelper = new NotificationHelper(this);
        
        // Cargar tours desde SharedPreferences
        tourAdminList = storageHelper.loadTours();
    }

    private void setupRecyclerView() {
        tourAdapter = new TourAdapter(this, tourAdminList);
        recyclerViewTours.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTours.setAdapter(tourAdapter);
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

        // Botón de filtro por departamento
        btnFiltrar.setOnClickListener(v -> showFilterDialog());

        // FloatingActionButton para crear nuevo tour
        fabCrearTour.setOnClickListener(v -> {
            Intent intent = new Intent(ToursAdminActivity.this, CreateTourActivity.class);
            startActivityForResult(intent, 100);
        });

        // Configurar búsqueda en tiempo real
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tourAdapter != null) {
                        tourAdapter.getFilter().filter(s);
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
            if (tourAdapter != null) {
                tourAdapter.filterByDepartment(departamentoSeleccionado);
            }
            
            Toast.makeText(ToursAdminActivity.this,
                    "Filtrando tours de " + departamentoSeleccionado,
                    Toast.LENGTH_SHORT).show();
        });

        // Mostrar dialog
        new AlertDialog.Builder(this)
                .setTitle("Filtrar por departamento")
                .setView(dialogView)
                .setNegativeButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Mostrar todos", (dialog, which) -> {
                    if (tourAdapter != null) {
                        tourAdapter.filterByDepartment("todos");
                    }
                    dialog.dismiss();
                })
                .show();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard); // No hay item específico para tours
            
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_dashboard) {
                    startActivity(new Intent(this, MainAdminActivity.class));
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
                Toast.makeText(this, "¡TourLegacy creado exitosamente!", Toast.LENGTH_LONG).show();
                
                // Obtener datos del tour creado
                String tourName = data.getStringExtra("tour_name");
                String destination = data.getStringExtra("tour_destination");
                
                // Mostrar notificación
                if (tourName != null && destination != null) {
                    notificationHelper.notifyTourCreated(tourName, destination);
                }
                
                // Recargar la lista de tours
                refreshToursList();
            }
        } else if (requestCode == 200 && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("tour_updated", false)) {
                Toast.makeText(this, "¡TourLegacy actualizado exitosamente!", Toast.LENGTH_LONG).show();
                // Recargar la lista de tours
                refreshToursList();
            }
        }
    }
    
    private void refreshToursList() {
        if (storageHelper != null && tourAdapter != null) {
            tourAdminList = storageHelper.loadTours();
            tourAdapter.updateTours(tourAdminList);
            
            // Log para debug
            System.out.println("DEBUG: Tours cargados: " + tourAdminList.size());
            for (TourAdmin tourAdmin : tourAdminList) {
                System.out.println("DEBUG: TourLegacy - " + tourAdmin.getName() + " | " + tourAdmin.getLocation());
            }
        }
    }
}
