package com.example.puriqtours.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SolicitudesActivity extends AppCompatActivity {

    private RecyclerView rvSolicitudes;
    private SolicitudesAdapter adapter;
    private List<DocumentSnapshot> solicitudList;
    private FirebaseFirestore db;

    private MaterialButton btnPendientes, btnRechazados, btnHabilitados, btnTodos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitudes);

        // 🔹 Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationSuperAdmin);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_solicitudes);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_principal) {
                    startActivity(new Intent(this, MainSuperAdminActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                if (id == R.id.nav_usuarios) {
                    startActivity(new Intent(this, UsuariosActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                if (id == R.id.nav_logs) {
                    startActivity(new Intent(this, LogsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                return id == R.id.nav_solicitudes;
            });
        }

        rvSolicitudes = findViewById(R.id.rvSolicitudes);
        rvSolicitudes.setLayoutManager(new LinearLayoutManager(this));
        solicitudList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        btnPendientes = findViewById(R.id.btnPendientes);
        btnRechazados = findViewById(R.id.btnRechazados);
        btnHabilitados = findViewById(R.id.btnHabilitados);
        btnTodos = findViewById(R.id.btnTodosSolicitudes);

        // 🔹 Carga inicial
        cargarSolicitudes("No habilitado");
        highlightFilter(R.id.btnPendientes);

        // 🔹 Buscador
        TextInputEditText etSearch = findViewById(R.id.etSearchSolicitudes);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarSolicitudes(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 🔹 Filtros (ahora cada uno resalta el suyo)
        btnPendientes.setOnClickListener(v -> {
            cargarSolicitudes("No habilitado");
            highlightFilter(R.id.btnPendientes);
        });

        btnHabilitados.setOnClickListener(v -> {
            cargarSolicitudes("Habilitado");
            highlightFilter(R.id.btnHabilitados);
        });

        btnRechazados.setOnClickListener(v -> {
            cargarSolicitudes("Rechazado");
            highlightFilter(R.id.btnRechazados);
        });

        btnTodos.setOnClickListener(v -> {
            cargarSolicitudes(null);
            highlightFilter(R.id.btnTodosSolicitudes);
        });

        // 🔹 Ordenar
        LinearLayout ordenarLayout = findViewById(R.id.ordenarLayoutSolicitudes);
        ordenarLayout.setOnClickListener(v -> {
            ordenarPorNombre();
            Toast.makeText(this, "🔤 Ordenadas alfabéticamente", Toast.LENGTH_SHORT).show();
        });
    }

    // =====================================================
    // 🔹 CARGAR SOLICITUDES DESDE FIRESTORE
    // =====================================================
    private void cargarSolicitudes(String estado) {
        var query = db.collection("users").whereEqualTo("rol", "Guia");

        if (estado != null) {
            query = query.whereEqualTo("guide_status", estado);
        }

        query.get().addOnSuccessListener(snapshot -> {
            solicitudList.clear();
            for (QueryDocumentSnapshot doc : snapshot) {
                solicitudList.add(doc);
            }

            boolean mostrarBotones = !"Habilitado".equals(estado);
            adapter = new SolicitudesAdapter(this, solicitudList, mostrarBotones);
            rvSolicitudes.setAdapter(adapter);

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error al cargar solicitudes.", Toast.LENGTH_SHORT).show());
    }

    // =====================================================
    // 🔹 BUSCADOR
    // =====================================================
    private void filtrarSolicitudes(String texto) {
        if (adapter == null) return;

        List<DocumentSnapshot> filtradas = new ArrayList<>();
        for (DocumentSnapshot doc : solicitudList) {
            String nombre = doc.getString("name");
            String apellido = doc.getString("last_name");
            String email = doc.getString("email");

            if ((nombre != null && nombre.toLowerCase().contains(texto.toLowerCase())) ||
                    (apellido != null && apellido.toLowerCase().contains(texto.toLowerCase())) ||
                    (email != null && email.toLowerCase().contains(texto.toLowerCase()))) {
                filtradas.add(doc);
            }
        }

        adapter = new SolicitudesAdapter(this, filtradas, true);
        rvSolicitudes.setAdapter(adapter);
    }

    // =====================================================
    // 🔹 ORDENAR
    // =====================================================
    private void ordenarPorNombre() {
        if (solicitudList == null || solicitudList.isEmpty()) return;

        solicitudList.sort((d1, d2) -> {
            String n1 = d1.getString("name") != null ? d1.getString("name") : "";
            String n2 = d2.getString("name") != null ? d2.getString("name") : "";
            return n1.compareToIgnoreCase(n2);
        });

        adapter.notifyDataSetChanged();
    }

    // =====================================================
    // ✅ SOMBREADO IGUAL A USUARIOS
    // =====================================================
    private void highlightFilter(int selectedId) {
        int[] ids = {R.id.btnPendientes, R.id.btnRechazados, R.id.btnHabilitados, R.id.btnTodosSolicitudes};
        for (int id : ids) {
            View btn = findViewById(id);
            if (btn != null) {
                btn.setBackgroundColor(getResources().getColor(id == selectedId ? R.color.teal_50 : android.R.color.white));
            }
        }
    }
}
