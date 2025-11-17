package com.example.puriqtours.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.entity.HistorialTour;
import com.example.puriqtours.entity.Tour;
import com.example.puriqtours.login.LoginLegacyActivity;
import com.example.puriqtours.R;
import com.example.puriqtours.adapter.HistorialAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private static final String TAG = "HistorialActivity";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView recyclerHistorial;
    private HistorialAdapter adapter;
    private List<HistorialTour> listaTours = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Verificar que hay usuario autenticado
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginLegacyActivity.class));
            finish();
            return;
        }

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
                return true;
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
                mAuth.signOut();
                startActivity(new Intent(this, LoginLegacyActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // 🔹 RecyclerView - INICIALIZAR ANTES DE CARGAR DATOS
        recyclerHistorial = findViewById(R.id.recyclerHistorial);
        recyclerHistorial.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar adapter con lista vacía
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

        // 🔹 Cargar datos DESPUÉS de configurar el RecyclerView
        cargarHistorialDesdeFirebase();
    }

    private void cargarHistorialDesdeFirebase() {
        String idCliente = mAuth.getCurrentUser().getUid();

        Log.d(TAG, "🔍 Buscando reservas para cliente: " + idCliente);

        db.collection("reservas")
                .whereEqualTo("idCliente", idCliente)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    Log.d(TAG, "✅ Documentos encontrados: " + querySnapshot.size());

                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "No tienes reservas aún", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaTours.clear();

                    for (var doc : querySnapshot.getDocuments()) {

                        Log.d(TAG, "📄 Procesando documento: " + doc.getId());

                        // OBTENER DATOS CON VALIDACIÓN
                        String idTour = doc.getString("idTour");
                        String titulo = doc.getString("titulo");
                        String fecha = doc.getString("fecha");
                        String hora = doc.getString("hora");
                        String estado = doc.getString("estado");
                        String precio = doc.getString("precio");
                        String viajeros = doc.getString("viajeros");
                        String imageUrl = doc.getString("imageUrl");

                        // VALIDAR QUE LOS CAMPOS CRÍTICOS NO SEAN NULL
                        if (titulo == null || titulo.isEmpty()) {
                            titulo = "Tour sin nombre";
                        }
                        if (fecha == null || fecha.isEmpty()) {
                            fecha = "Fecha no disponible";
                        }
                        if (hora == null || hora.isEmpty()) {
                            hora = "Hora no disponible";
                        }
                        if (estado == null || estado.isEmpty()) {
                            estado = "Desconocido";
                        }
                        if (precio == null || precio.isEmpty()) {
                            precio = "Precio no disponible";
                        }
                        if (viajeros == null || viajeros.isEmpty()) {
                            viajeros = "No especificado";
                        }
                        if (idTour == null) {
                            idTour = doc.getId();
                        }

                        // Imagen: usar placeholder si no hay URL
                        int imagen = R.drawable.kuelap;
                        float rating = 4.5f;

                        Log.d(TAG, "➕ Agregando: " + titulo + " - Estado: " + estado);

                        listaTours.add(
                                new HistorialTour(
                                        idTour,
                                        titulo,
                                        fecha,
                                        hora,
                                        estado,
                                        precio,
                                        viajeros,
                                        imagen,
                                        rating,
                                        imageUrl  // ✅ Pasar la URL de imagen
                                )
                        );
                    }

                    Log.d(TAG, "📊 Total tours cargados: " + listaTours.size());

                    // ACTUALIZAR AMBAS LISTAS DEL ADAPTER
                    adapter.actualizarLista(listaTours);

                    Toast.makeText(this,
                            "Cargadas " + listaTours.size() + " reservas",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ ERROR FIRESTORE: " + e.getMessage(), e);
                    Toast.makeText(this,
                            "Error al cargar reservas: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
