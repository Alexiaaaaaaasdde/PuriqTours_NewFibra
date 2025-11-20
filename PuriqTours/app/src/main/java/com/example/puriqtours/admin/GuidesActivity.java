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
import com.example.puriqtours.adapter.GuideAdapter;
import com.example.puriqtours.entity.GuideAdmin;
import com.example.puriqtours.helper.StorageHelper;
import com.example.puriqtours.helper.FirestoreHelper;
import com.example.puriqtours.helper.GuideConverter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuidesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewGuides;
    private GuideAdapter guideAdapter;
    private List<GuideAdmin> guideAdminList;
    private Button btnFiltrar;
    private TextInputEditText etBuscar;
    private StorageHelper storageHelper;
    private FirestoreHelper firestoreHelper;
    private NotificationHelper notificationHelper;

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
        storageHelper = new StorageHelper(this);
        firestoreHelper = new FirestoreHelper();
        notificationHelper = new NotificationHelper(this);
        
        // Inicializar lista vacía
        guideAdminList = new ArrayList<>();
        
        // Cargar guías desde Firestore
        loadGuidesFromFirestore();
    }
    
    private void loadGuidesFromFirestore() {
        firestoreHelper.loadGuides(usuarios -> {
            if (usuarios != null && !usuarios.isEmpty()) {
                // Convertir Usuarios (Guías) de Firestore a GuideAdmins para el adapter
                guideAdminList = GuideConverter.usuariosToGuideAdmins(usuarios);
                
                if (guideAdapter != null) {
                    guideAdapter.updateGuides(guideAdminList);
                }
                
                android.util.Log.d("GuidesAdmin", "Guías cargados desde Firestore: " + guideAdminList.size());
            } else {
                // Si no hay guías en Firestore, cargar desde local como fallback
                guideAdminList = storageHelper.loadGuides();
                
                if (guideAdapter != null) {
                    guideAdapter.updateGuides(guideAdminList);
                }
                
                android.util.Log.d("GuidesAdmin", "Guías cargados desde local: " + guideAdminList.size());
            }
        });
    }

    private void setupRecyclerView() {
        guideAdapter = new GuideAdapter(this, guideAdminList);
        recyclerViewGuides.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewGuides.setAdapter(guideAdapter);
    }

    private void setupListeners() {
        // Icono de notificaciones en toolbar (simular propuesta a guía)
        ImageView notificationIcon = findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                // Simular propuesta de tour a guía seleccionado
                if (!guideAdminList.isEmpty()) {
                    GuideAdmin randomGuideAdmin = guideAdminList.get((int) (Math.random() * guideAdminList.size()));
                    notificationHelper.notifyTourProposedToGuide("Tour Machu Picchu", randomGuideAdmin.getName(), "Cusco");
                    Toast.makeText(this, "Simulando propuesta de tour a " + randomGuideAdmin.getName(), Toast.LENGTH_SHORT).show();
                }
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
}
