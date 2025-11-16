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
import com.google.android.material.imageview.ShapeableImageView;
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

        setupDrawer();
        enableDrawerIcon();

        // Inicializar vistas
        btnFiltrar = findViewById(R.id.btnFiltro);
        searchBar = findViewById(R.id.searchBar);
        recyclerTours = findViewById(R.id.recyclerTours);

        // Verificar que las vistas no sean null
        if (recyclerTours == null) {
            Toast.makeText(this, "Error: RecyclerView no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        db = FirebaseFirestore.getInstance();

        // Configurar adapter
        adapter = new TourClienteAdapter(listaFiltrada);
        recyclerTours.setLayoutManager(new LinearLayoutManager(this));
        recyclerTours.setAdapter(adapter);

        // Cargar datos
        cargarToursDesdeFirebase();
        configurarBusqueda();
        configurarFiltroDepartamentos();

        ShapeableImageView profileIcon = findViewById(R.id.profileIcon);
        if (profileIcon != null) {
            profileIcon.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class))
            );
        }

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_tours);
        }
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

                                // ✅ AGREGAR TODOS LOS TOURS sin validación
                                tourList.add(t);
                            }
                        } catch (Exception e) {
                            // Log del error pero continuar con los demás tours
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
                    e.printStackTrace();
                });
    }

    private void configurarBusqueda() {
        if (searchBar == null) return;

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarPorTexto(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filtrarPorTexto(String texto) {
        listaFiltrada.clear();

        if (texto.trim().isEmpty()) {
            listaFiltrada.addAll(tourList);
        } else {
            for (Tour t : tourList) {
                String title = t.getTitle() != null ? t.getTitle().toLowerCase() : "";
                String location = t.getLocation() != null ? t.getLocation().toLowerCase() : "";
                String textoLower = texto.toLowerCase();

                if (title.contains(textoLower) || location.contains(textoLower)) {
                    listaFiltrada.add(t);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void configurarFiltroDepartamentos() {
        if (btnFiltrar == null) return;

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
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapterDept.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
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
                if (t.getLocation() != null && t.getLocation().equalsIgnoreCase(dep)) {
                    listaFiltrada.add(t);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void mostrarTodos() {
        listaFiltrada.clear();
        listaFiltrada.addAll(tourList);
        adapter.notifyDataSetChanged();
    }
}
