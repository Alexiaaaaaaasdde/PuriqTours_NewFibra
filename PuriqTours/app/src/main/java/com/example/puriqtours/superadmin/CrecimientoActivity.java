package com.example.puriqtours.superadmin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class CrecimientoActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private PieChart pieUsuariosEstado;
    private BarChart barUsuariosRol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crecimiento);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationSuperAdmin);

        if (bottomNav != null) {

            // Esta vista depende de PRINCIPAL
            bottomNav.setSelectedItemId(R.id.nav_principal);

            bottomNav.setOnItemSelectedListener(item -> {

                int id = item.getItemId();

                if (id == R.id.nav_principal) {
                    finish(); // volver a Principal
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

                if (id == R.id.nav_logs) {
                    startActivity(new Intent(this, LogsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }

                return false;
            });
        }
        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        pieUsuariosEstado = findViewById(R.id.pieUsuariosEstado);
        barUsuariosRol = findViewById(R.id.barUsuariosRol);

        cargarUsuariosEstado();
        cargarUsuariosPorRol();
    }

    // =====================================================
    // 👤 USUARIOS ACTIVOS VS INACTIVOS (SIN SUPERADMIN)
    // =====================================================
    private void cargarUsuariosEstado() {

        db.collection("users")
                .get()
                .addOnSuccessListener(snapshot -> {

                    int activos = 0;
                    int inactivos = 0;

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String rol = doc.getString("rol");
                        String status = doc.getString("status");

                        // ❌ excluir SuperAdmin
                        if ("SuperAdmin".equalsIgnoreCase(rol)) continue;

                        if ("Activo".equalsIgnoreCase(status)) {
                            activos++;
                        } else {
                            inactivos++;
                        }
                    }

                    // 🔹 Datos del gráfico
                    ArrayList<PieEntry> entries = new ArrayList<>();
                    entries.add(new PieEntry(activos, "Activos"));
                    entries.add(new PieEntry(inactivos, "Inactivos"));

                    PieDataSet dataSet = new PieDataSet(entries, "");
                    dataSet.setColors(
                            Color.parseColor("#4CAF50"),
                            Color.parseColor("#F44336")
                    );
                    dataSet.setValueTextSize(14f);
                    dataSet.setValueTextColor(Color.WHITE);

                    PieData data = new PieData(dataSet);
                    pieUsuariosEstado.setData(data);

                    // =================================================
                    // 🔥 AQUÍ VA EL TEXTO DENTRO DEL CÍRCULO BLANCO
                    // =================================================
                    pieUsuariosEstado.setData(data);

// 🔥 CAMBIO CLAVE AQUÍ
                    pieUsuariosEstado.setDrawHoleEnabled(true);
                    pieUsuariosEstado.setHoleRadius(60f);
                    pieUsuariosEstado.setTransparentCircleRadius(65f);

                    pieUsuariosEstado.setCenterText("Usuarios activos\nvs\nInactivos");
                    pieUsuariosEstado.setCenterTextSize(14f);
                    pieUsuariosEstado.setCenterTextColor(Color.parseColor("#009688"));
                    pieUsuariosEstado.setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD);

                    pieUsuariosEstado.invalidate();

                });
    }


    // =====================================================
    // 🧑‍💼 USUARIOS POR ROL
    // =====================================================
    private void cargarUsuariosPorRol() {

        db.collection("users")
                .get()
                .addOnSuccessListener(snapshot -> {

                    int clientes = 0;
                    int guias = 0;
                    int admins = 0;

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String rol = doc.getString("rol");

                        if ("Cliente".equalsIgnoreCase(rol)) clientes++;
                        else if ("Guia".equalsIgnoreCase(rol)) guias++;
                        else if ("Admin".equalsIgnoreCase(rol)) admins++;
                    }

                    ArrayList<BarEntry> entries = new ArrayList<>();
                    entries.add(new BarEntry(0, clientes));
                    entries.add(new BarEntry(1, guias));
                    entries.add(new BarEntry(2, admins));

                    BarDataSet dataSet = new BarDataSet(entries, "Usuarios");
                    dataSet.setColor(Color.parseColor("#009688"));
                    dataSet.setValueTextSize(14f);

                    BarData data = new BarData(dataSet);
                    data.setBarWidth(0.6f);

                    barUsuariosRol.setData(data);

                    XAxis xAxis = barUsuariosRol.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(
                            new String[]{"Clientes", "Guías", "Admins"}
                    ));
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f);
                    xAxis.setDrawGridLines(false);

                    barUsuariosRol.getAxisRight().setEnabled(false);
                    barUsuariosRol.getAxisLeft().setDrawGridLines(false);
                    barUsuariosRol.getDescription().setEnabled(false);
                    barUsuariosRol.getLegend().setEnabled(false);
                    barUsuariosRol.invalidate();
                });
    }
}
