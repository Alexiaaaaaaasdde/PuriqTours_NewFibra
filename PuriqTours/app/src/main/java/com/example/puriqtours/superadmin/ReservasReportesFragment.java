package com.example.puriqtours.superadmin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.puriqtours.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;

public class ReservasReportesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservas_reportes, container, false);
        // Gráfico de líneas (reservas por mes)
        LineChart lineChart = view.findViewById(R.id.lineChartReservas);
        ArrayList<Entry> lineEntries = new ArrayList<>();
        lineEntries.add(new Entry(0, 50));
        lineEntries.add(new Entry(1, 60));
        lineEntries.add(new Entry(2, 80));
        lineEntries.add(new Entry(3, 90));
        lineEntries.add(new Entry(4, 70));
        lineEntries.add(new Entry(5, 100));
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Reservas");
        lineDataSet.setColor(android.graphics.Color.parseColor("#26A69A"));
        lineDataSet.setCircleColor(android.graphics.Color.parseColor("#26A69A"));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(5f);
        lineDataSet.setDrawValues(false);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
        final String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun"};
        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                return (i >= 0 && i < meses.length) ? meses[i] : "";
            }
        });
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setGranularityEnabled(true);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.invalidate();
        // Gráfico de barras (reservas por empresa de turismo)
        BarChart barChart = view.findViewById(R.id.barChartEmpresas);
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, 30));
        barEntries.add(new BarEntry(1, 50));
        barEntries.add(new BarEntry(2, 40));
        barEntries.add(new BarEntry(3, 60));
        barEntries.add(new BarEntry(4, 35));
        barEntries.add(new BarEntry(5, 20));
        BarDataSet barDataSet = new BarDataSet(barEntries, "Empresas");
        barDataSet.setColor(android.graphics.Color.parseColor("#26A69A"));
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        final String[] empresas = {"Tour 1", "Tour 2", "Tour 3", "Tour 4", "Tour 5", "Otros"};
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                return (i >= 0 && i < empresas.length) ? empresas[i] : "";
            }
        });
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.invalidate();
        return view;
    }
}
