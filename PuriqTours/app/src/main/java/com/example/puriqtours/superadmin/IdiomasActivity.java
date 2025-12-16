package com.example.puriqtours.superadmin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

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
import java.util.HashMap;
import java.util.Map;

public class IdiomasActivity extends AppCompatActivity {

    private PieChart pieChart;
    private BarChart barChartRegiones;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idiomas);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationSuperAdmin);

        if (bottomNav != null) {

            // Esta vista viene desde Principal → se marca Principal
            bottomNav.setSelectedItemId(R.id.nav_principal);

            bottomNav.setOnItemSelectedListener(item -> {

                int id = item.getItemId();

                if (id == R.id.nav_principal) {
                    // Volver al dashboard principal
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

                if (id == R.id.nav_logs) {
                    startActivity(new Intent(this, LogsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }

                return false;
            });
        }
        pieChart = findViewById(R.id.pieChartIdiomas);
        barChartRegiones = findViewById(R.id.barChartServicios); // 🔥 IMPORTANTE
        db = FirebaseFirestore.getInstance();
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
        });
        cargarIdiomas();
        cargarServiciosExtras(); // 🔥 SI NO LLAMAS ESTO, NO APARECE
    }


    // ✅ CUENTA IDIOMAS DE TODOS LOS USUARIOS (CLIENTES + GUÍAS + ADMINS)
    private void cargarIdiomas() {


        db.collection("users")
                .get()
                .addOnSuccessListener(snapshot -> {

                    HashMap<String, Integer> conteoIdiomas = new HashMap<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String idioma = doc.getString("language");

                        if (idioma == null || idioma.trim().isEmpty()) continue;

                        conteoIdiomas.put(idioma,
                                conteoIdiomas.getOrDefault(idioma, 0) + 1);
                    }

                    mostrarGraficoAutomatico(conteoIdiomas);
                });
    }

    // ✅ GRÁFICO DONA AUTOMÁTICO, ELEGANTE Y ESCALABLE
    private void mostrarGraficoAutomatico(HashMap<String, Integer> conteo) {

        ArrayList<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Integer> item : conteo.entrySet()) {
            entries.add(new PieEntry(item.getValue(), item.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Idiomas más hablados");
        dataSet.setColors(
                Color.parseColor("#80DEEA"),
                Color.parseColor("#4DB6AC"),
                Color.parseColor("#AED581"),
                Color.parseColor("#FFCC80"),
                Color.parseColor("#CE93D8")
        );

        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        pieChart.setUsePercentValues(true);
        pieChart.setDrawEntryLabels(false);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(65f);
        pieChart.setCenterText("Idiomas\nmás hablados");
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(Color.parseColor("#009688"));

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setTextSize(12f);
        pieChart.getLegend().setTextColor(Color.parseColor("#009688"));

        pieChart.animateY(900);
        pieChart.invalidate();
    }

    private void cargarServiciosExtras() {

        HashMap<String, Integer> conteoServicios = new HashMap<>();

        db.collection("reservas")
                .get()
                .addOnSuccessListener(reservasSnap -> {

                    final int totalReservas = reservasSnap.size();
                    final int[] procesadas = {0};

                    for (QueryDocumentSnapshot reserva : reservasSnap) {

                        reserva.getReference()
                                .collection("reservaIndividual")
                                .get()
                                .addOnSuccessListener(individualSnap -> {

                                    for (QueryDocumentSnapshot ind : individualSnap) {

                                        ArrayList<String> servicios =
                                                (ArrayList<String>) ind.get("addedServices");

                                        if (servicios == null) continue;

                                        for (String servicio : servicios) {

                                            // 🔍 LOG CRUDO
                                            Log.d("SERVICIO_DEBUG",
                                                    "Servicio crudo = [" + servicio + "]");

                                            if (servicio == null) continue;

                                            // 🧹 LIMPIEZA
                                            String limpio = servicio.trim();

                                            if (limpio.isEmpty()) {
                                                Log.w("SERVICIO_DEBUG", "Servicio vacío detectado");
                                                continue;
                                            }

                                            // 🔠 Normalizar nombre
                                            limpio = limpio.substring(0,1).toUpperCase()
                                                    + limpio.substring(1).toLowerCase();

                                            Log.d("SERVICIO_OK",
                                                    "Servicio usado = [" + limpio + "]");

                                            conteoServicios.put(
                                                    limpio,
                                                    conteoServicios.getOrDefault(limpio, 0) + 1
                                            );
                                        }
                                    }

                                    procesadas[0]++;

                                    if (procesadas[0] == totalReservas) {

                                        // 🔎 LOG FINAL DEL MAPA
                                        for (Map.Entry<String, Integer> e : conteoServicios.entrySet()) {
                                            Log.d("SERVICIO_FINAL",
                                                    e.getKey() + " -> " + e.getValue());
                                        }

                                        mostrarGraficoServicios(conteoServicios);
                                    }
                                });
                    }
                });
    }



    private void mostrarGraficoServicios(HashMap<String, Integer> conteo) {

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Integer> item : conteo.entrySet()) {
            entries.add(new BarEntry(index, item.getValue()));
            labels.add(item.getKey());
            index++;
        }

        BarDataSet dataSet =
                new BarDataSet(entries, "Servicios extras más elegidos");

        dataSet.setColor(Color.parseColor("#42A5F5"));
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);
        barChartRegiones.setData(data);

        XAxis xAxis = barChartRegiones.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setLabelRotationAngle(-15f);

        barChartRegiones.getAxisRight().setEnabled(false);
        barChartRegiones.getDescription().setEnabled(false);

        // 🔥 ESTO ES LO QUE FALTABA
        barChartRegiones.setExtraOffsets(10f, 10f, 10f, 30f);

        barChartRegiones.animateY(800);
        barChartRegiones.invalidate();
    }


}
