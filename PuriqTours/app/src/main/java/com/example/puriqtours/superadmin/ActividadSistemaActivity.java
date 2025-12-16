package com.example.puriqtours.superadmin;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ActividadSistemaActivity extends AppCompatActivity {

    private LineChart lineChartMeses;
    private FirebaseFirestore db;

    // Cards
    private TextView tvReservadoCount;
    private TextView tvProcesoCount;
    private TextView tvFinalizadoCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_sistema);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationSuperAdmin);

        if (bottomNav != null) {

            // ESTA VISTA VIENE DESDE "Principal" → NO es un tab del footer
            // pero lo correcto es dejar seleccionado Principal
            bottomNav.setSelectedItemId(R.id.nav_principal);

            bottomNav.setOnItemSelectedListener(item -> {

                int id = item.getItemId();

                if (id == R.id.nav_principal) {
                    // ya estamos relacionados a Principal
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

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish(); // vuelve a la vista anterior
        });

        lineChartMeses = findViewById(R.id.lineChartMeses);

        tvReservadoCount = findViewById(R.id.tvReservadoCount);
        tvProcesoCount = findViewById(R.id.tvProcesoCount);
        tvFinalizadoCount = findViewById(R.id.tvFinalizadoCount);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btn3m).setOnClickListener(v -> cargarReservas(3));
        findViewById(R.id.btn6m).setOnClickListener(v -> cargarReservas(6));
        findViewById(R.id.btn12m).setOnClickListener(v -> cargarReservas(12));

        // Default
        cargarReservas(3);
    }

    // -------------------------------------------------------------------------
    // 📊 CARGA UNIFICADA (gráfico + cards)
    // -------------------------------------------------------------------------
    private void cargarReservas(int meses) {

        db.collection("reservas")
                .get()
                .addOnSuccessListener(snapshot -> {

                    Map<Integer, Integer> conteoMeses = new HashMap<>();
                    int reservado = 0;
                    int proceso = 0;
                    int finalizado = 0;

                    Calendar hoy = Calendar.getInstance();
                    hoy.set(Calendar.DAY_OF_MONTH, 1);

                    // Inicializar meses en 0
                    for (int i = 0; i < meses; i++) {
                        Calendar temp = (Calendar) hoy.clone();
                        temp.add(Calendar.MONTH, -i);
                        int key = temp.get(Calendar.YEAR) * 100 + temp.get(Calendar.MONTH);
                        conteoMeses.put(key, 0);
                    }

                    for (QueryDocumentSnapshot doc : snapshot) {

                        String docId = doc.getId();
                        String status = doc.getString("status");
                        Object tsObj = doc.get("timestamp");

                        Log.d(TAG, "------------------------------");
                        Log.d(TAG, "Doc ID: " + docId);
                        Log.d(TAG, "Status: " + status);
                        Log.d(TAG, "Timestamp raw: " + tsObj);

                        if (status == null || tsObj == null) {
                            Log.d(TAG, "⛔ Ignorado: status o timestamp nulo");
                            continue;
                        }

                        Calendar cal = Calendar.getInstance();

                        if (tsObj instanceof com.google.firebase.Timestamp) {
                            cal.setTime(((com.google.firebase.Timestamp) tsObj).toDate());
                            Log.d(TAG, "Timestamp tipo Firebase.Timestamp");

                        } else if (tsObj instanceof Long) {
                            cal.setTimeInMillis((Long) tsObj);
                            Log.d(TAG, "Timestamp tipo Long (millis)");

                        } else if (tsObj instanceof String) {
                            String dateStr = (String) tsObj;
                            Log.d(TAG, "Timestamp tipo String: " + dateStr);

                            String[] parts = dateStr.split("-");
                            if (parts.length < 2) {
                                Log.d(TAG, "⛔ Fecha inválida");
                                continue;
                            }

                            cal.set(
                                    Integer.parseInt(parts[0]),
                                    Integer.parseInt(parts[1]) - 1,
                                    1
                            );
                        } else {
                            Log.d(TAG, "⛔ Timestamp tipo desconocido");
                            continue;
                        }

                        cal.set(Calendar.DAY_OF_MONTH, 1);

                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH); // 0-based
                        int key = year * 100 + month;

                        Log.d(TAG, "Fecha normalizada → Año: " + year + " Mes: " + (month + 1));
                        Log.d(TAG, "Key calculada: " + key);
                        Log.d(TAG, "¿Existe en rango?: " + conteoMeses.containsKey(key));

                        if (!conteoMeses.containsKey(key)) {
                            Log.d(TAG, "⛔ Fuera del rango seleccionado");
                            continue;
                        }

                        // Conteo gráfico
                        conteoMeses.put(key, conteoMeses.get(key) + 1);

                        Log.d(TAG, "✅ SUMA al mes → nuevo valor: " + conteoMeses.get(key));

                        // Conteo cards
                        switch (status) {
                            case "Reservado":
                                reservado++;
                                break;
                            case "En proceso":
                                proceso++;
                                break;
                            case "Finalizado":
                                finalizado++;
                                break;
                        }

                        Log.d(TAG, "Cards → R:" + reservado + " P:" + proceso + " F:" + finalizado);
                    }





                    mostrarGraficoUltimosMeses(conteoMeses, meses);

                    tvReservadoCount.setText(String.valueOf(reservado));
                    tvProcesoCount.setText(String.valueOf(proceso));
                    tvFinalizadoCount.setText(String.valueOf(finalizado));
                });
    }

    // -------------------------------------------------------------------------
    // 📈 GRAFICO LINEAL
    // -------------------------------------------------------------------------
    private void mostrarGraficoUltimosMeses(
            Map<Integer, Integer> conteo,
            int meses
    ) {

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        Calendar hoy = Calendar.getInstance();
        String[] nombresMeses = new DateFormatSymbols(
                new Locale("es", "PE")
        ).getShortMonths();

        int index = 0;

        for (int i = meses - 1; i >= 0; i--) {
            Calendar cal = (Calendar) hoy.clone();
            cal.add(Calendar.MONTH, -i);

            int key = cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.MONTH);
            int valor = conteo.getOrDefault(key, 0);

            entries.add(new Entry(index, valor));
            labels.add(nombresMeses[cal.get(Calendar.MONTH)]);
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Reservas últimos meses");
        configurarDataSetAzul(dataSet);

        LineData data = new LineData(dataSet);
        lineChartMeses.setData(data);

        configurarLineChart(lineChartMeses, labels);

        lineChartMeses.animateX(700);
        lineChartMeses.invalidate();
    }

    // -------------------------------------------------------------------------
    // 🎨 ESTILO
    // -------------------------------------------------------------------------
    private void configurarLineChart(LineChart chart, ArrayList<String> labels) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
    }

    private void configurarDataSetAzul(LineDataSet dataSet) {
        int azul = Color.parseColor("#2196F3");
        dataSet.setColor(azul);
        dataSet.setCircleColor(azul);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4.5f);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(azul);
    }
}
