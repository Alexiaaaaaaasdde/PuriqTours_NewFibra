package com.example.puriqtours.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.BaseActivity;
import com.example.puriqtours.R;
import com.example.puriqtours.adapter.HistorialAdapter;
import com.example.puriqtours.entity.HistorialTour;
import com.example.puriqtours.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends BaseActivity {

    private RecyclerView recyclerHistorial;
    private HistorialAdapter adapter;
    private List<HistorialTour> listaTours = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        setupSharedToolbar();
        enableDrawerIcon();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        inicializarBottomNav();
        inicializarVistas();
        configurarBuscador();
        configurarChips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 🔥 Recargar cuando volvemos de ValoracionActivity
        cargarHistorialDesdeFirebase();
    }

    private void inicializarBottomNav() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_historial);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            if (id == R.id.nav_historial) {
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    private void inicializarVistas() {
        recyclerHistorial = findViewById(R.id.recyclerHistorial);
        recyclerHistorial.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistorialAdapter(listaTours, this);
        recyclerHistorial.setAdapter(adapter);
    }

    private void configurarBuscador() {
        EditText searchBar = findViewById(R.id.searchBar);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }
        });
    }

    private void configurarChips() {
        Chip chipTodos = findViewById(R.id.chipTodos);
        Chip chipEnProceso = findViewById(R.id.chipEnProceso);
        Chip chipFinalizado = findViewById(R.id.chipFinalizado);
        Chip chipReservado = findViewById(R.id.chipReservado);

        chipTodos.setOnClickListener(v -> adapter.filtrarEstado(""));
        chipEnProceso.setOnClickListener(v -> adapter.filtrarEstado("En proceso"));
        chipFinalizado.setOnClickListener(v -> adapter.filtrarEstado("Finalizado"));
        chipReservado.setOnClickListener(v -> adapter.filtrarEstado("Reservado"));
    }

    private void cargarHistorialDesdeFirebase() {
        String idCliente = auth.getCurrentUser().getUid();

        db.collection("reservas")
                        .whereArrayContains("idClientes", idCliente)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            listaTours.clear();
                            for(DocumentSnapshot doc : querySnapshot.getDocuments()) {

                                HistorialTour historialTour = new HistorialTour();
                                historialTour.setIdReserva(doc.getId());
                                historialTour.setIdTour(doc.getString("idTour"));
                                historialTour.setIdGuia(doc.getString("idGuia"));
                                historialTour.setTitulo(doc.getString("title"));
                                historialTour.setFecha(doc.getString("date"));
                                historialTour.setHora(doc.getString("hour"));
                                historialTour.setEstado(doc.getString("status"));
                                historialTour.setImageUrl(doc.getString("imageUrl"));

                                cargarReservaIndividual(historialTour, idCliente, historial -> {
                                    listaTours.add(historial);
                                    adapter.actualizarLista(listaTours);
                                });
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this,
                                        "Error al cargar historial: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
    }

    private void cargarReservaIndividual(HistorialTour historialTour, String idCliente, OnHistorialLoaded callback){
        db.collection("reservas")
                .document(historialTour.getIdReserva())
                .collection("reservaIndividual")
                .whereEqualTo("idCliente", idCliente)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);

                        historialTour.setPrecio(doc.getDouble("price"));
                        historialTour.setViajeros(doc.getString("travelers"));
                        historialTour.setTokenInicio(doc.getString("tokenStart"));
                        historialTour.setTokenFin(doc.getString("tokenEnd"));

                        Boolean valorada = doc.getBoolean("valorada");
                        historialTour.setValorada(valorada != null && valorada);
                    }
                    callback.onLoaded(historialTour);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al cargar reservaIndividual", e);
                    callback.onLoaded(historialTour); // opcional
                });
    }

    public interface OnHistorialLoaded {
        void onLoaded(HistorialTour historialTour);
    }


}
