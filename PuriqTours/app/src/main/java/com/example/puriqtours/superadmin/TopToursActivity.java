package com.example.puriqtours.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.Tour;
import com.example.puriqtours.entity.Usuario;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TopToursActivity extends AppCompatActivity {

    private RecyclerView rvTopTours;

    private MaterialButton btnFiltroTours, btnFiltroGuias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tours);

        // 🔹 Toolbar SIN flecha (flecha manual)
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("");
        }

        // Flecha personalizada
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 🔹 Filtros (BOTONES)
        btnFiltroTours = findViewById(R.id.btnFiltroTours);
        btnFiltroGuias = findViewById(R.id.btnFiltroGuias);

        // 🔹 RecyclerView
        rvTopTours = findViewById(R.id.rvTopToursFull);
        rvTopTours.setLayoutManager(new LinearLayoutManager(this));

        // Cargar por defecto T O U R S más visitados
        btnFiltroTours.setSelected(true);
        actualizarBotones();
        cargarTopTours(rvTopTours);

        // Eventos de los botones
        btnFiltroTours.setOnClickListener(v -> {
            btnFiltroTours.setSelected(true);
            btnFiltroGuias.setSelected(false);
            actualizarBotones();
            cargarTopTours(rvTopTours);
        });

        btnFiltroGuias.setOnClickListener(v -> {
            btnFiltroTours.setSelected(false);
            btnFiltroGuias.setSelected(true);
            actualizarBotones();
            cargarTopGuias(rvTopTours);
        });

        // Footer
        findViewById(R.id.btnPrincipal).setOnClickListener(v -> {
            startActivity(new Intent(this, MainSuperAdminActivity.class));
            finish();
        });

        findViewById(R.id.btnUsuarios).setOnClickListener(v -> {
            startActivity(new Intent(this, UsuariosActivity.class));
            finish();
        });

        findViewById(R.id.btnLogs).setOnClickListener(v -> {
            startActivity(new Intent(this, LogsActivity.class));
            finish();
        });
    }

    // 🔹 Cambia el color de los botones según selección
    private void actualizarBotones() {
        // Tours
        if (btnFiltroTours.isSelected()) {
            btnFiltroTours.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#009688")));
            btnFiltroTours.setTextColor(Color.WHITE);
            btnFiltroTours.setStrokeWidth(0);
        } else {
            btnFiltroTours.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btnFiltroTours.setTextColor(Color.parseColor("#009688"));
            btnFiltroTours.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#009688")));
            btnFiltroTours.setStrokeWidth(2);
        }

        // Guías
        if (btnFiltroGuias.isSelected()) {
            btnFiltroGuias.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#009688")));
            btnFiltroGuias.setTextColor(Color.WHITE);
            btnFiltroGuias.setStrokeWidth(0);
        } else {
            btnFiltroGuias.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btnFiltroGuias.setTextColor(Color.parseColor("#009688"));
            btnFiltroGuias.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#009688")));
            btnFiltroGuias.setStrokeWidth(2);
        }
    }
    private void obtenerRatingGuia(Usuario guia, RatingCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("reviewGuide")
                .whereEqualTo("idGuia", guia.getUid())
                .get()
                .addOnSuccessListener(snap -> {
                    double sum = 0;
                    int count = 0;

                    for (QueryDocumentSnapshot d : snap) {
                        Number r = d.getDouble("rating");
                        if (r != null) {
                            sum += r.doubleValue();
                            count++;
                        }
                    }

                    double promedio = (count == 0) ? 0 : (sum / count);
                    callback.onResult(promedio);
                });
    }

    // Callback
    interface RatingCallback {
        void onResult(double rating);
    }

    // -------------------------------------------------------------------------
    // 🔹 CARGAR TOURS
    // -------------------------------------------------------------------------
    private void cargarTopTours(RecyclerView rv) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reservas")
                .get()
                .addOnSuccessListener(reservasSnap -> {
                    Map<String, Integer> conteo = new HashMap<>();
                    for (QueryDocumentSnapshot r : reservasSnap) {
                        String idTour = r.getString("idTour");
                        if (idTour != null) {
                            conteo.put(idTour, conteo.getOrDefault(idTour, 0) + 1);
                        }
                    }
                    obtenerTours(conteo, rv);
                });
    }

    private void obtenerTours(Map<String, Integer> conteo, RecyclerView rv) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tours")
                .get()
                .addOnSuccessListener(toursSnap -> {
                    ArrayList<Tour> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot t : toursSnap) {
                        String id = t.getId();
                        if (!conteo.containsKey(id)) continue;
                        Tour tour = t.toObject(Tour.class);
                        tour.setIdTour(id);
                        lista.add(tour);
                    }

                    lista.sort((a, b) -> conteo.get(b.getIdTour()) - conteo.get(a.getIdTour()));

                    rv.setAdapter(new TopToursAdapter(lista, conteo));
                });
    }

    // -------------------------------------------------------------------------
    // 🔹 CARGAR GUÍAS
    // -------------------------------------------------------------------------
    private void cargarTopGuias(RecyclerView rv) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("reservas")
                .get()
                .addOnSuccessListener(reservasSnap -> {
                    Map<String, Integer> conteoGuias = new HashMap<>();

                    for (QueryDocumentSnapshot r : reservasSnap) {
                        String idGuia = r.getString("idGuia");
                        if (idGuia != null && !idGuia.isEmpty()) {
                            conteoGuias.put(idGuia, conteoGuias.getOrDefault(idGuia, 0) + 1);
                        }
                    }

                    obtenerGuias(conteoGuias, rv);
                });
    }

    private void obtenerGuias(Map<String, Integer> conteoGuias, RecyclerView rv) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnSuccessListener(guiasSnap -> {

                    ArrayList<Usuario> listaGuias = new ArrayList<>();

                    for (QueryDocumentSnapshot g : guiasSnap) {

                        Usuario guia = g.toObject(Usuario.class);
                        guia.setUid(g.getId());

                        String rol = (guia.getRol() != null)
                                ? guia.getRol().toLowerCase().trim()
                                : "";

                        if (!rol.equals("guia")) continue;
                        if (!conteoGuias.containsKey(guia.getUid())) continue;

                        listaGuias.add(guia);
                    }

                    // Ordenamos por cantidad de servicios realizados
                    listaGuias.sort((a, b) ->
                            conteoGuias.getOrDefault(b.getUid(), 0)
                                    - conteoGuias.getOrDefault(a.getUid(), 0));

                    // ⭐ AHORA VIENE EL CÁLCULO DE RATINGS ⭐
                    int totalGuias = listaGuias.size();
                    int[] contador = {0};

                    for (Usuario g : listaGuias) {

                        obtenerRatingGuia(g, rating -> {
                            g.setRating(rating);

                            contador[0]++;

                            // Cuando se hayan obtenido TODOS los ratings
                            if (contador[0] == totalGuias) {
                                rv.setAdapter(new TopGuiasAdapter(listaGuias, conteoGuias));
                            }
                        });
                    }
                });
    }

}
