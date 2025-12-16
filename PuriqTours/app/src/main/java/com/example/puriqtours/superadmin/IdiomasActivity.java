package com.example.puriqtours.superadmin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.puriqtours.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
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

                                            String limpio = servicio.trim().replaceAll("\\s+", " ");

                                            if (limpio.isEmpty()) {
                                                Log.w("SERVICIO_DEBUG", "Servicio vacío detectado");
                                                continue;
                                            }



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

        // 🔹 1. Ordenar servicios por cantidad
        ArrayList<Map.Entry<String, Integer>> lista =
                new ArrayList<>(conteo.entrySet());

        lista.sort((a, b) -> b.getValue() - a.getValue());

        // 🔹 2. Data
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labelsCortos = new ArrayList<>();
        ArrayList<String> labelsCompletos = new ArrayList<>();

        int otros = 0;
        int index = 0;

        for (int i = 0; i < lista.size(); i++) {
            Map.Entry<String, Integer> item = lista.get(i);

            if (i < 5) {
                entries.add(new BarEntry(index, item.getValue()));
                labelsCortos.add(acortarLabel(item.getKey()));
                labelsCompletos.add(item.getKey());
                index++;
            } else {
                otros += item.getValue();
            }
        }

        if (otros > 0) {
            entries.add(new BarEntry(index, otros));
            labelsCortos.add("Otros");
            labelsCompletos.add("Otros");
        }

        // 🔹 3. Dataset
        BarDataSet dataSet = new BarDataSet(entries, "Servicios extras más elegidos");
        dataSet.setColor(Color.parseColor("#42A5F5"));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChartRegiones.setData(data);

        // 🔹 4. X AXIS → ETIQUETAS
        XAxis xAxis = barChartRegiones.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelsCortos));
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setLabelRotationAngle(-30f);
        xAxis.setAvoidFirstLastClipping(true);

        // 🔹 5. Y AXIS → VALORES
        barChartRegiones.getAxisLeft().setGranularity(1f);
        barChartRegiones.getAxisLeft().setDrawGridLines(true);
        barChartRegiones.getAxisLeft().setTextSize(12f);
        barChartRegiones.getAxisRight().setEnabled(false);

        // 🔹 Estética
        barChartRegiones.getDescription().setEnabled(false);
        barChartRegiones.getLegend().setEnabled(false);

        // 🔹 6. MarkerView (nombre completo)
        MarkerView marker = new MarkerView(this, R.layout.marker_servicio) {

            TextView tvServicio = findViewById(R.id.tvServicio);
            TextView tvCantidad = findViewById(R.id.tvCantidad);

            @Override
            public void refreshContent(Entry e, Highlight highlight) {
                int idx = (int) e.getX();
                tvServicio.setText(labelsCompletos.get(idx));
                tvCantidad.setText("Veces: " + (int) e.getY());
                super.refreshContent(e, highlight);
            }
        };

        marker.setChartView(barChartRegiones);
        barChartRegiones.setMarker(marker);
// 🔥 NUEVOS AJUSTES DE ESPACIO
        barChartRegiones.setFitBars(true);
        barChartRegiones.setExtraBottomOffset(56f);

        barChartRegiones.animateY(900);
        barChartRegiones.invalidate();
    }



    private String acortarLabel(String texto) {
        if (texto == null) return "";
        if (texto.length() <= 18) return texto;
        return texto.substring(0, 18) + "…";
    }


}
