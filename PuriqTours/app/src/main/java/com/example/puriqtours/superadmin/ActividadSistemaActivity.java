package com.example.puriqtours.superadmin;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
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

                        String dateStr = doc.getString("date"); // yyyy-MM-dd
                        String status = doc.getString("status");

                        if (dateStr == null || status == null) continue;

                        // Parsear fecha
                        String[] parts = dateStr.split("-");
                        if (parts.length < 2) continue;

                        Calendar cal = Calendar.getInstance();
                        cal.set(
                                Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1]) - 1,
                                1
                        );

                        int key = cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.MONTH);

                        // Si no está dentro del rango → ignorar
                        if (!conteoMeses.containsKey(key)) continue;

                        // Conteo para gráfico
                        conteoMeses.put(key, conteoMeses.get(key) + 1);

                        // Conteo para cards
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
