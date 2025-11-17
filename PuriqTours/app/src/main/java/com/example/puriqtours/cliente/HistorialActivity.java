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

import com.example.puriqtours.R;
import com.example.puriqtours.adapter.HistorialAdapter;
import com.example.puriqtours.entity.HistorialTour;
import com.example.puriqtours.login.LoginLegacyActivity;
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

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginLegacyActivity.class));
            finish();
            return;
        }

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_historial);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
                return true;
            } else if (id == R.id.nav_historial) {
                return true;
            }
            return false;
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                mAuth.signOut();
                startActivity(new Intent(this, LoginLegacyActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        recyclerHistorial = findViewById(R.id.recyclerHistorial);
        recyclerHistorial.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistorialAdapter(listaTours, this);
        recyclerHistorial.setAdapter(adapter);

        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        Chip chipTodos = findViewById(R.id.chipTodos);
        Chip chipEnProceso = findViewById(R.id.chipEnProceso);
        Chip chipFinalizado = findViewById(R.id.chipFinalizado);
        Chip chipReservado = findViewById(R.id.chipReservado);

        chipTodos.setOnClickListener(v -> adapter.filtrarEstado(""));
        chipEnProceso.setOnClickListener(v -> adapter.filtrarEstado("En proceso"));
        chipFinalizado.setOnClickListener(v -> adapter.filtrarEstado("Finalizado"));
        chipReservado.setOnClickListener(v -> adapter.filtrarEstado("Reservado"));

        cargarHistorialDesdeFirebase();
    }

    private void cargarHistorialDesdeFirebase() {
        String idCliente = mAuth.getCurrentUser().getUid();

        db.collection("reservas")
                .whereEqualTo("idCliente", idCliente)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaTours.clear();

                    for (var doc : querySnapshot.getDocuments()) {

                        String idTour = doc.getString("idTour");
                        String titulo = doc.getString("titulo");
                        String fecha = doc.getString("fecha");
                        String hora = doc.getString("hora");
                        String estado = doc.getString("estado");
                        String precio = doc.getString("precio");
                        String viajeros = doc.getString("viajeros");
                        String imageUrl = doc.getString("imageUrl");

                        int imagen = R.drawable.kuelap;
                        float rating = 4.5f;

                        HistorialTour ht = new HistorialTour(
                                idTour,
                                titulo,
                                fecha,
                                hora,
                                estado,
                                precio,
                                viajeros,
                                imagen,
                                rating,
                                imageUrl
                        );

                        ht.setIdReserva(doc.getId());

                        listaTours.add(ht);
                    }

                    adapter.actualizarLista(listaTours);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
