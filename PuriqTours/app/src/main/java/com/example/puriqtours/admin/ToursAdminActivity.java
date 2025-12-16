package com.example.puriqtours.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.puriqtours.helper.FirestoreHelper;
import com.example.puriqtours.helper.TourConverter;
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
    private com.example.puriqtours.adapter.ReservaAdapter reservaAdapter;
    private List<TourAdmin> tourAdminList;
    private List<com.example.puriqtours.entity.Reserva> reservaList;
    private com.google.android.material.button.MaterialButton btnMisTours, btnReservas;
    private FloatingActionButton fabCrearTour;
    private TextInputEditText searchBar;
    private StorageHelper storageHelper;
    private FirestoreHelper firestoreHelper;
    private NotificationHelper notificationHelper;
    private boolean showingTours = true; // true = tours, false = reservas

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
        
        // Configurar toolbar
        setupToolbar();
        
        // Configurar bottom navigation
        setupBottomNavigation();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar la lista cuando se regrese a esta actividad
        if (showingTours) {
            loadToursFromEmpresa();
        } else {
            loadReservasFromEmpresa();
        }
    }

    private void initViews() {
        recyclerViewTours = findViewById(R.id.recyclerViewTours);
        btnMisTours = findViewById(R.id.btnMisTours);
        btnReservas = findViewById(R.id.btnReservas);
        fabCrearTour = findViewById(R.id.fabCrearTour);
        searchBar = findViewById(R.id.searchBar);
        
        // Icono de notificaciones
        ImageView notificationIcon = findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                Intent intent = new Intent(ToursAdminActivity.this, NotificationsActivity.class);
                startActivity(intent);
            });
        }
    }

    private void createSampleData() {
        storageHelper = new StorageHelper(this);
        firestoreHelper = new FirestoreHelper();
        notificationHelper = new NotificationHelper(this);
        
        // Inicializar listas vacías
        tourAdminList = new ArrayList<>();
        reservaList = new ArrayList<>();
        
        // Cargar tours desde Firestore
        loadToursFromEmpresa();
    }
    
    private void loadToursFromEmpresa() {
        // Obtener ID del admin/empresa logueado
        String idEmpresa = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        
        if (idEmpresa == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        firestoreHelper.loadToursByEmpresa(idEmpresa, tours -> {
            if (tours != null && !tours.isEmpty()) {
                // Convertir Tours de Firestore a TourAdmins para el adapter
                tourAdminList = TourConverter.toursToTourAdmins(tours);
                
                if (tourAdapter != null) {
                    tourAdapter.updateTours(tourAdminList);
                }
                
                Log.d("ToursAdmin", "Tours de empresa cargados: " + tourAdminList.size());
            } else {
                // No hay tours, mostrar lista vacía
                tourAdminList.clear();
                
                if (tourAdapter != null) {
                    tourAdapter.updateTours(tourAdminList);
                }
                
                Log.d("ToursAdmin", "No hay tours creados por esta empresa");
            }
        });
    }
    
    private void loadReservasFromEmpresa() {
        // Obtener ID del admin/empresa logueado
        String idEmpresa = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        
        if (idEmpresa == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        firestoreHelper.loadReservasByEmpresa(idEmpresa, reservas -> {
            if (reservas != null && !reservas.isEmpty()) {
                reservaList = reservas;
                
                if (reservaAdapter != null) {
                    reservaAdapter.updateList(reservaList);
                }
                
                Log.d("ToursAdmin", "Reservas cargadas: " + reservaList.size());
            } else {
                // No hay reservas, mostrar lista vacía
                reservaList.clear();
                
                if (reservaAdapter != null) {
                    reservaAdapter.updateList(reservaList);
                }
                
                Log.d("ToursAdmin", "No hay reservas para los tours de esta empresa");
            }
        });
    }

    private void setupRecyclerView() {
        tourAdapter = new TourAdapter(this, tourAdminList);
        reservaAdapter = new com.example.puriqtours.adapter.ReservaAdapter(this, reservaList);
        recyclerViewTours.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTours.setAdapter(tourAdapter); // Por defecto mostramos tours
    }

    private void setupListeners() {
        // Botón Mis Tours
        btnMisTours.setOnClickListener(v -> showMisTours());

        // Botón Reservas
        btnReservas.setOnClickListener(v -> showReservas());

        // FloatingActionButton para crear nuevo tour
        fabCrearTour.setOnClickListener(v -> {
            Intent intent = new Intent(ToursAdminActivity.this, CreateTourActivity.class);
            startActivityForResult(intent, 100);
        });

        // Botón de filtro por región
        Button btnFiltro = findViewById(R.id.btnFiltro);
        if (btnFiltro != null) {
            btnFiltro.setOnClickListener(v -> showFilterDialog());
        }

        // Configurar búsqueda en tiempo real
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (showingTours && tourAdapter != null) {
                        tourAdapter.getFilter().filter(s);
                    } else if (!showingTours && reservaAdapter != null) {
                        reservaAdapter.filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }
    
    private void showMisTours() {
        showingTours = true;
        
        // Cambiar colores de botones
        btnMisTours.setBackgroundColor(getResources().getColor(R.color.teal_700));
        btnMisTours.setTextColor(getResources().getColor(R.color.white));
        btnReservas.setBackgroundColor(getResources().getColor(R.color.gray_light));
        btnReservas.setTextColor(getResources().getColor(R.color.gray_dark));
        
        // Mostrar FAB para crear tour
        fabCrearTour.show();
        
        // Cambiar adapter
        recyclerViewTours.setAdapter(tourAdapter);
        
        // Limpiar búsqueda
        if (searchBar != null) {
            searchBar.setText("");
        }
    }
    
    private void showReservas() {
        showingTours = false;
        
        // Cambiar colores de botones
        btnReservas.setBackgroundColor(getResources().getColor(R.color.teal_700));
        btnReservas.setTextColor(getResources().getColor(R.color.white));
        btnMisTours.setBackgroundColor(getResources().getColor(R.color.gray_light));
        btnMisTours.setTextColor(getResources().getColor(R.color.gray_dark));
        
        // Ocultar FAB (no se pueden crear reservas desde admin)
        fabCrearTour.hide();
        
        // Cambiar adapter
        recyclerViewTours.setAdapter(reservaAdapter);
        
        // Cargar reservas si aún no se han cargado
        if (reservaList.isEmpty()) {
            loadReservasFromEmpresa();
        }
        
        // Limpiar búsqueda
        if (searchBar != null) {
            searchBar.setText("");
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
            
            if (showingTours) {
                // Filtrar tours
                if (tourAdapter != null) {
                    tourAdapter.filterByDepartment(departamentoSeleccionado);
                }
                Toast.makeText(ToursAdminActivity.this,
                        "Filtrando tours de " + departamentoSeleccionado,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Filtrar reservas
                filterReservasByDepartment(departamentoSeleccionado);
                Toast.makeText(ToursAdminActivity.this,
                        "Filtrando reservas de " + departamentoSeleccionado,
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Mostrar dialog
        new AlertDialog.Builder(this)
                .setTitle("Filtrar por departamento")
                .setView(dialogView)
                .setNegativeButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Mostrar todos", (dialog, which) -> {
                    if (showingTours) {
                        if (tourAdapter != null) {
                            tourAdapter.filterByDepartment("todos");
                        }
                    } else {
                        filterReservasByDepartment("todos");
                    }
                    dialog.dismiss();
                })
                .show();
    }
    
    private void filterReservasByDepartment(String department) {
        if (department.equalsIgnoreCase("todos")) {
            // Mostrar todas las reservas
            if (reservaAdapter != null) {
                reservaAdapter.updateList(reservaList);
            }
            return;
        }
        
        // Obtener los IDs de los tours que pertenecen a este departamento
        List<String> tourIdsInDepartment = new ArrayList<>();
        for (TourAdmin tour : tourAdminList) {
            if (tour.getRegion() != null && 
                tour.getRegion().toLowerCase().contains(department.toLowerCase())) {
                tourIdsInDepartment.add(tour.getId());
            }
        }
        
        // Filtrar reservas que correspondan a esos tours
        List<com.example.puriqtours.entity.Reserva> filteredReservas = new ArrayList<>();
        for (com.example.puriqtours.entity.Reserva reserva : reservaList) {
            if (tourIdsInDepartment.contains(reserva.getIdTour())) {
                filteredReservas.add(reserva);
            }
        }
        
        // Actualizar adapter
        if (reservaAdapter != null) {
            reservaAdapter.updateList(filteredReservas);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            // No seleccionar ningún item por defecto en esta vista (no hay nav_tours)
            
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("tour_created", false)) {
                Toast.makeText(this, "¡Tour creado exitosamente!", Toast.LENGTH_LONG).show();
                
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
            if (data != null) {
                if (data.getBooleanExtra("tour_updated", false)) {
                    Toast.makeText(this, "¡Tour actualizado exitosamente!", Toast.LENGTH_LONG).show();
                    refreshToursList();
                } else if (data.getBooleanExtra("tour_deleted", false)) {
                    Toast.makeText(this, "¡Tour eliminado exitosamente!", Toast.LENGTH_LONG).show();
                    refreshToursList();
                }
            }
        }
    }
    
    private void refreshToursList() {
        // Recargar tours desde Firestore
        loadToursFromEmpresa();
    }
    
    private void setupToolbar() {
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> cerrarSesion());
        }
    }
    
    private void cerrarSesion() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                    android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    Intent intent = new Intent(ToursAdminActivity.this, com.example.puriqtours.login.LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
