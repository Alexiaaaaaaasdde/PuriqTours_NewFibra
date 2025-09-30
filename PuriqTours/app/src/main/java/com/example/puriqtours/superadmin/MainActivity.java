package com.example.puriqtours.superadmin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Vinculamos el layout activity_superadmin_home.xml
        setContentView(R.layout.activity_superadmin_home);
        // Status bar blanco y iconos oscuros (solo método moderno, sin warning)
        getWindow().setStatusBarColor(Color.WHITE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().getInsetsController().setSystemBarsAppearance(
                android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            );
        }
        // Forzar color del TopAppBar
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(Color.parseColor("#1DE9B6"));
        }
        // Navegación al hacer click en el botón Usuarios
        findViewById(R.id.btnUsuarios).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.example.puriqtours.superadmin.UsuariosActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        // Navegación al hacer click en el botón Reportes
        findViewById(R.id.btnReportes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.example.puriqtours.superadmin.ReportesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        // Navegación al hacer click en el botón Logs
        findViewById(R.id.btnLogs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.example.puriqtours.superadmin.LogsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        // Inicializar gráfico de líneas de empresas registradas
        LineChart lineChart = findViewById(R.id.lineChartEmpresas);
        if (lineChart != null) {
            // Datos de ejemplo
            String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun"};
            float[] valores = {10, 20, 12, 25, 30, 22};
            java.util.List<Entry> entries = new java.util.ArrayList<>();
            for (int i = 0; i < valores.length; i++) {
                entries.add(new Entry(i, valores[i]));
            }
            LineDataSet dataSet = new LineDataSet(entries, "Empresas");
            dataSet.setColor(Color.parseColor("#26A69A"));
            dataSet.setCircleColor(Color.parseColor("#26A69A"));
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(5f);
            dataSet.setDrawValues(false);
            dataSet.setDrawFilled(false);
            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            lineChart.getDescription().setEnabled(false);
            lineChart.getLegend().setEnabled(false);
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f);
            xAxis.setTextColor(Color.parseColor("#26A69A"));
            xAxis.setTextSize(12f);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    if (index >= 0 && index < meses.length) {
                        return meses[index];
                    } else {
                        return "";
                    }
                }
            });
            lineChart.getAxisLeft().setDrawGridLines(false);
            lineChart.getAxisRight().setEnabled(false);
            lineChart.getAxisLeft().setTextColor(Color.parseColor("#26A69A"));
            lineChart.setExtraOffsets(10, 10, 10, 10);
            lineChart.invalidate();
        }

        // Inicializar RecyclerView horizontal de guías
        RecyclerView rvGuiasHorizontal = findViewById(R.id.rvGuiasHorizontal);
        if (rvGuiasHorizontal != null) {
            rvGuiasHorizontal.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            java.util.List<UsuarioGuia> guias = new java.util.ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                guias.add(new UsuarioGuia("Guía " + i, "Ciudad " + i, (i % 5) + 1));
            }
            GuiasHorizontalAdapter adapter = new GuiasHorizontalAdapter(this, guias);
            rvGuiasHorizontal.setAdapter(adapter);
        }
    }
}