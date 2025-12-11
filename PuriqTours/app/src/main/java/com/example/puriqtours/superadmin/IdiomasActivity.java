package com.example.puriqtours.superadmin;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IdiomasActivity extends AppCompatActivity {

    private PieChart pieChart;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idiomas);

        pieChart = findViewById(R.id.pieChartIdiomas);
        db = FirebaseFirestore.getInstance();

        cargarIdiomas();
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
}
