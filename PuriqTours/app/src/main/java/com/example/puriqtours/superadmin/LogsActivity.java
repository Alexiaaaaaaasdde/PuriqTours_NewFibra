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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LogsActivity extends AppCompatActivity {

    private RecyclerView rvLogs;
    private LogsAdapter adapter;
    private List<LogItem> logsList;
    private FirebaseFirestore db;
    private boolean ordenDescendente = true;

    private MaterialButton btnGeneral, btnUsuarios, btnPagos, btnGuias, btnEmpresas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_logs);

        // ==============================
        // 🔹 Bottom Navigation
        // ==============================
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationSuperAdmin);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_logs);
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
                if (id == R.id.nav_solicitudes) {
                    startActivity(new Intent(this, SolicitudesActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                return id == R.id.nav_logs;
            });
        }

        // ==============================
        // 🔹 Firestore + RecyclerView
        // ==============================
        db = FirebaseFirestore.getInstance();
        rvLogs = findViewById(R.id.rvLogs);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        logsList = new ArrayList<>();

        // ==============================
        // 🔹 Chips
        // ==============================
        btnGeneral   = findViewById(R.id.btnGeneral);
        btnUsuarios  = findViewById(R.id.btnUsuariosFiltro);
        btnPagos     = findViewById(R.id.btnPagos);
        btnGuias     = findViewById(R.id.btnGuias);
        btnEmpresas  = findViewById(R.id.btnEmpresas);

        // ==============================
        // 🔹 Carga inicial (TODOS)
        // ==============================
        cargarLogs(null);
        highlightFilter(R.id.btnGeneral);

        // ==============================
        // 🔍 Buscador
        // ==============================
        TextInputEditText etSearch = findViewById(R.id.etSearchLogs);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarLogs(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ==============================
        // 🔘 Chips (tipos EXACTOS de Firestore)
        // ==============================
        btnGeneral.setOnClickListener(v -> {
            cargarLogs(null);
            highlightFilter(R.id.btnGeneral);
        });

        btnUsuarios.setOnClickListener(v -> {
            cargarLogs("usuarios");
            highlightFilter(R.id.btnUsuariosFiltro);
        });

        btnPagos.setOnClickListener(v -> {
            cargarLogs("pagos");
            highlightFilter(R.id.btnPagos);
        });

        btnGuias.setOnClickListener(v -> {
            cargarLogs("guias");
            highlightFilter(R.id.btnGuias);
        });

        btnEmpresas.setOnClickListener(v -> {
            cargarLogs("empresas");
            highlightFilter(R.id.btnEmpresas);
        });

        // ==============================
        // 🔽 Ordenar por fecha
        // ==============================
        LinearLayout ordenarLayout = findViewById(R.id.ordenarLayout);
        ordenarLayout.setOnClickListener(v -> {
            ordenarPorFecha();
            Toast.makeText(this, "🕓 Logs ordenados por fecha", Toast.LENGTH_SHORT).show();
        });
    }

    // =====================================================
    // 🔹 CARGAR LOGS DESDE FIRESTORE
    // =====================================================
    private void cargarLogs(String tipo) {

        Query query = db.collection("logs")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        query.get().addOnSuccessListener(snapshot -> {
            logsList.clear();

            for (QueryDocumentSnapshot doc : snapshot) {
                String logType = doc.getString("type");

                if (tipo == null || tipo.equals(logType)) {
                    LogItem log = new LogItem(
                            logType,
                            doc.getString("desc"),
                            doc.getTimestamp("timestamp")
                    );
                    logsList.add(log);
                }
            }

            if (adapter == null) {
                adapter = new LogsAdapter(this, logsList);
                rvLogs.setAdapter(adapter);
            } else {
                adapter.setLogs(logsList);
            }

        }).addOnFailureListener(e -> {
            Toast.makeText(this,
                    "Error real: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        });
    }


    // =====================================================
    // 🔍 FILTRAR POR TEXTO
    // =====================================================
    private void filtrarLogs(String texto) {
        if (adapter == null) return;

        List<LogItem> filtradas = new ArrayList<>();

        for (LogItem log : logsList) {
            if ((log.getDesc() != null && log.getDesc().toLowerCase().contains(texto.toLowerCase()))) {
                filtradas.add(log);
            }
        }

        adapter.setLogs(filtradas);
    }

    // =====================================================
    // 🔽 ORDENAR POR FECHA DESCENDENTE
    // =====================================================
    private void ordenarPorFecha() {
        if (adapter == null || logsList == null) return;

        logsList.sort((l1, l2) -> {
            if (l1.getTimestamp() == null || l2.getTimestamp() == null) return 0;

            return ordenDescendente
                    ? l2.getTimestamp().compareTo(l1.getTimestamp()) // DESC
                    : l1.getTimestamp().compareTo(l2.getTimestamp()); // ASC
        });

        ordenDescendente = !ordenDescendente; // 🔁 CAMBIA EL ORDEN
        adapter.setLogs(logsList);
    }


    // =====================================================
    // ✅ SOMBREADO IGUAL A USUARIOS / SOLICITUDES
    // =====================================================
    private void highlightFilter(int selectedId) {
        int[] ids = {
                R.id.btnGeneral,
                R.id.btnUsuariosFiltro,
                R.id.btnPagos,
                R.id.btnGuias,
                R.id.btnEmpresas
        };

        for (int id : ids) {
            View btn = findViewById(id);
            if (btn != null) {
                btn.setBackgroundColor(getResources().getColor(
                        id == selectedId ? R.color.teal_50 : android.R.color.white
                ));
            }
        }
    }
}
