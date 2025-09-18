package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Arrays;

public class ToursActivity extends AppCompatActivity {

    private CardView cardKuelap;  // 👈 Solo un Card
    private Button btnFiltrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tours);

        // 🔹 Inicializamos vistas
        btnFiltrar = findViewById(R.id.btnFiltro);
        cardKuelap = findViewById(R.id.cardKuelap);

        // 🔹 Icono de perfil en toolbar
        ShapeableImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ToursActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // 🔹 Listener del botón filtrar
        btnFiltrar.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_filtro_departamentos, null);

            EditText etBuscar = dialogView.findViewById(R.id.etBuscar);
            ListView listDepartamentos = dialogView.findViewById(R.id.listDepartamentos);

            // Cargar departamentos
            String[] departamentos = getResources().getStringArray(R.array.departamentos_peru);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    new ArrayList<>(Arrays.asList(departamentos))
            );

            listDepartamentos.setAdapter(adapter);

            // Filtro búsqueda
            etBuscar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.getFilter().filter(s);
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            // Acción al elegir un departamento
            listDepartamentos.setOnItemClickListener((parent, view1, position, id) -> {
                String seleccionado = adapter.getItem(position);

                if ("Amazonas".equalsIgnoreCase(seleccionado)) {
                    cardKuelap.setVisibility(View.VISIBLE);
                } else {
                    cardKuelap.setVisibility(View.GONE);
                }

                Toast.makeText(ToursActivity.this,
                        "Filtrando tours de " + seleccionado,
                        Toast.LENGTH_SHORT).show();
            });

            new AlertDialog.Builder(ToursActivity.this)
                    .setTitle("Filtrar por departamento")
                    .setView(dialogView)
                    .setNegativeButton("Cerrar", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        cardKuelap.setOnClickListener(v -> {
            Intent intent = new Intent(ToursActivity.this, DetalleTourActivity.class);
            intent.putExtra("titulo", "Ciudadela de Kuélap");
            intent.putExtra("precio", "S/ 165.02");
            intent.putExtra("ubicacion", "Chachapoyas - Amazonas");
            // ✅ Ya no mandamos array, solo mostramos una fija en DetalleTourActivity
            startActivity(intent);
        });

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_tours);

// Seleccionamos por defecto la pestaña "Tours"
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.nav_tours) {
                // Ya estamos en tours
                return true;

            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });


    }
}
