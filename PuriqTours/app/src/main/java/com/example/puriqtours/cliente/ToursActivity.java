package com.example.puriqtours.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.BaseActivity;
import com.example.puriqtours.R;
import com.example.puriqtours.adapter.TourClienteAdapter;
import com.example.puriqtours.entity.Tour;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;

public class ToursActivity extends BaseActivity {

    private Button btnFiltrar;
    private EditText searchBar;
    private RecyclerView recyclerTours;

    private ArrayList<Tour> tourList = new ArrayList<>();
    private ArrayList<Tour> listaFiltrada = new ArrayList<>();

    private TourClienteAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tours_cliente);

        // --------------------------------------------------------------------
        // 🔥 Establecer toolbar de BaseActivity (foto + cerrar sesión)
        // --------------------------------------------------------------------
        setupSharedToolbar();


        inicializarVistas();
        inicializarBottomNav();

        db = FirebaseFirestore.getInstance();

        configurarAdapter();
        cargarToursDesdeFirebase();
        configurarBusqueda();
        configurarFiltroDepartamentos();
    }

    private void inicializarVistas() {
        btnFiltrar = findViewById(R.id.btnFiltro);
        searchBar = findViewById(R.id.searchBar);
        recyclerTours = findViewById(R.id.recyclerTours);

        if (recyclerTours == null) {
            Toast.makeText(this, "Error: RecyclerView no encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void inicializarBottomNav() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_tours);

        bottomNavigation.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            if (item.getItemId() == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            if (item.getItemId() == R.id.nav_historial) {
                overridePendingTransition(0, 0);
                return true;
            }

            return true;
        });
    }

    private void configurarAdapter() {
        adapter = new TourClienteAdapter(listaFiltrada, tour -> {

            Intent intent = new Intent(ToursActivity.this, DetalleTourActivity.class);

            intent.putExtra("tourId", tour.getIdTour());
            intent.putExtra("titulo", tour.getTitle() != null ? tour.getTitle() : "Sin título");
            intent.putExtra("precio", String.valueOf(tour.getPrice() != null ? tour.getPrice() : 0));
            intent.putExtra("desc", tour.getDesc() != null ? tour.getDesc() : "Sin descripción");
            intent.putExtra("img", tour.getImageUrl() != null ? tour.getImageUrl() : "");
            intent.putExtra("rating", tour.getRating() != null ? tour.getRating() : 0);
            intent.putExtra("location", tour.getLocation() != null ? tour.getLocation() : "Sin ubicación");

            startActivity(intent);
        });

        recyclerTours.setLayoutManager(new LinearLayoutManager(this));
        recyclerTours.setAdapter(adapter);
    }

    private void cargarToursDesdeFirebase() {
        db.collection("tours").get()
                .addOnSuccessListener(query -> {
                    tourList.clear();
                    listaFiltrada.clear();

                    for (DocumentSnapshot doc : query) {
                        try {
                            Tour t = doc.toObject(Tour.class);
                            if (t != null) {
                                t.setIdTour(doc.getId());
                                tourList.add(t);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    listaFiltrada.addAll(tourList);
                    adapter.notifyDataSetChanged();

                    Toast.makeText(this,
                            "Cargados " + tourList.size() + " tours",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error Firebase: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void configurarBusqueda() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarPorTexto(s.toString());
            }
        });
    }

    private void filtrarPorTexto(String texto) {
        listaFiltrada.clear();

        if (texto.trim().isEmpty()) {
            listaFiltrada.addAll(tourList);
        } else {
            String filtro = texto.toLowerCase();

            for (Tour t : tourList) {

                String title = t.getTitle() != null ? t.getTitle().toLowerCase() : "";
                String location = t.getLocation() != null ? t.getLocation().toLowerCase() : "";

                if (title.contains(filtro) || location.contains(filtro)) {
                    listaFiltrada.add(t);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void configurarFiltroDepartamentos() {
        btnFiltrar.setOnClickListener(v -> abrirFiltroDepartamentos());
    }

    private void abrirFiltroDepartamentos() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filtro_departamentos, null);

        EditText etBuscar = dialogView.findViewById(R.id.etBuscar);
        ListView listDepartamentos = dialogView.findViewById(R.id.listDepartamentos);

        String[] departamentos = getResources().getStringArray(R.array.departamentos_peru);

        ArrayAdapter<String> adapterDept = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>(Arrays.asList(departamentos))
        );

        listDepartamentos.setAdapter(adapterDept);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapterDept.getFilter().filter(s);
            }
        });

        listDepartamentos.setOnItemClickListener((parent, view, position, id) -> {
            String dep = adapterDept.getItem(position);
            filtrarPorDepartamento(dep);
            Toast.makeText(this, "Filtrando: " + dep, Toast.LENGTH_SHORT).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Filtrar por departamento")
                .setView(dialogView)
                .setNegativeButton("Cerrar", null)
                .setPositiveButton("Mostrar todos", (d, w) -> mostrarTodos())
                .show();
    }

    private void filtrarPorDepartamento(String dep) {
        listaFiltrada.clear();

        for (Tour t : tourList) {
            try {
                if (t.getLocation() != null &&
                        t.getLocation().equalsIgnoreCase(dep)) {
                    listaFiltrada.add(t);
                }
            } catch (Exception ignored) {}
        }

        adapter.notifyDataSetChanged();
    }

    private void mostrarTodos() {
        listaFiltrada.clear();
        listaFiltrada.addAll(tourList);
        adapter.notifyDataSetChanged();
    }
}
