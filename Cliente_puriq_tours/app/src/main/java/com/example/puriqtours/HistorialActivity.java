package com.example.puriqtours;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;

public class HistorialActivity extends AppCompatActivity {

    private TextView tvEstado1, tvEstado2, tvEstado3, tvEstado4;
    private TextView tvTitulo1, tvTitulo2, tvTitulo3, tvTitulo4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

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
                return true; // ya estamos aquí
            }
            return false;
        });

        // 🔹 Referencias a títulos
        tvTitulo1 = findViewById(R.id.tvTitulo1);
        tvTitulo2 = findViewById(R.id.tvTitulo2);
        tvTitulo3 = findViewById(R.id.tvTitulo3);
        tvTitulo4 = findViewById(R.id.tvTitulo4);

        // 🔹 Referencias a estados
        tvEstado1 = findViewById(R.id.tvEstado1);
        tvEstado2 = findViewById(R.id.tvEstado2);
        tvEstado3 = findViewById(R.id.tvEstado3);
        tvEstado4 = findViewById(R.id.tvEstado4);

        // 🔹 Pintar colores
        pintarEstado(tvEstado1);
        pintarEstado(tvEstado2);
        pintarEstado(tvEstado3);
        pintarEstado(tvEstado4);

        // 🔹 Buscador
        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarTours(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 🔹 Botón Filtro
        Button btnFiltro = findViewById(R.id.btnFiltro);
        btnFiltro.setOnClickListener(v -> mostrarDialogoFiltro());
        // 🔹 Referencias a los CardView
        View cardReservado1 = findViewById(R.id.cardReservado1);
        View cardEnProceso = findViewById(R.id.cardEnProceso);
        View cardFinalizado = findViewById(R.id.cardFinalizado);
        View cardFinalizado2 = findViewById(R.id.cardFinalizado2);

// 🔹 Click para abrir detalle según estado
        cardReservado1.setOnClickListener(v -> {
            Intent intent = new Intent(HistorialActivity.this, ReservadoActivity.class);
            startActivity(intent);
        });

        cardEnProceso.setOnClickListener(v -> {
            Intent intent = new Intent(HistorialActivity.this, EnProcesoActivity.class);
            startActivity(intent);
        });

        cardFinalizado.setOnClickListener(v -> {
            Intent intent = new Intent(HistorialActivity.this, FinalizadoActivity.class);
            startActivity(intent);
        });

        cardFinalizado2.setOnClickListener(v -> {
            Intent intent = new Intent(HistorialActivity.this, FinalizadoActivity.class);
            startActivity(intent);
        });

    }

    private void pintarEstado(TextView tvEstado) {
        if (tvEstado == null) return;
        String texto = tvEstado.getText().toString().toLowerCase();
        if (texto.contains("finalizado")) {
            tvEstado.setTextColor(Color.RED);
        } else if (texto.contains("en proceso")) {
            tvEstado.setTextColor(Color.parseColor("#FFA500")); // naranja
        } else if (texto.contains("reservado")) {
            tvEstado.setTextColor(Color.parseColor("#388E3C")); // verde
        }
    }

    private void filtrarTours(String query) {
        filtrarCard(tvTitulo1, query);
        filtrarCard(tvTitulo2, query);
        filtrarCard(tvTitulo3, query);
        filtrarCard(tvTitulo4, query);
    }

    private void filtrarCard(TextView tvTitulo, String query) {
        if (tvTitulo == null) return;
        View card = (View) tvTitulo.getParent().getParent(); // sube hasta CardView
        if (tvTitulo.getText().toString().toLowerCase().contains(query.toLowerCase())) {
            card.setVisibility(View.VISIBLE);
        } else {
            card.setVisibility(View.GONE);
        }
    }

    private void mostrarDialogoFiltro() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filtro_departamentos, null);

        EditText etBuscar = dialogView.findViewById(R.id.etBuscar);
        ListView listEstados = dialogView.findViewById(R.id.listDepartamentos);

        String[] estados = {"Finalizado", "En proceso", "Reservado"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>(Arrays.asList(estados))
        );

        listEstados.setAdapter(adapter);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        listEstados.setOnItemClickListener((parent, view1, position, id) -> {
            String seleccionado = adapter.getItem(position);
            aplicarFiltroEstado(seleccionado);
        });

        new AlertDialog.Builder(this)
                .setTitle("Filtrar por estado")
                .setView(dialogView)
                .setNegativeButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void aplicarFiltroEstado(String estado) {
        aplicarFiltro(tvEstado1, estado);
        aplicarFiltro(tvEstado2, estado);
        aplicarFiltro(tvEstado3, estado);
        aplicarFiltro(tvEstado4, estado);
    }

    private void aplicarFiltro(TextView tvEstado, String estado) {
        if (tvEstado == null) return;
        View card = (View) tvEstado.getParent().getParent();
        if (tvEstado.getText().toString().toLowerCase().contains(estado.toLowerCase())) {
            card.setVisibility(View.VISIBLE);
        } else {
            card.setVisibility(View.GONE);
        }
    }
}
