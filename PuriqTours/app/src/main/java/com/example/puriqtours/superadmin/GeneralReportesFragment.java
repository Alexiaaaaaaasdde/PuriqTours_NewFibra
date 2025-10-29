package com.example.puriqtours.superadmin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.puriqtours.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import java.util.ArrayList;

public class GeneralReportesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_general_reportes, container, false);
        // PieChart Distribución de usuarios
        PieChart pieChart = view.findViewById(R.id.pieChartUsuarios);
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(50f, "Clientes"));
        pieEntries.add(new PieEntry(35f, "Administrador"));
        pieEntries.add(new PieEntry(15f, "Guía"));
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(android.graphics.Color.parseColor("#FFD54F"));
        colors.add(android.graphics.Color.parseColor("#80CBC4"));
        colors.add(android.graphics.Color.parseColor("#B2DFDB"));
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextColor(android.graphics.Color.TRANSPARENT);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
        // LineChart Reporte de venta por servicio
        LineChart lineChart = view.findViewById(R.id.lineChartVentas);
        ArrayList<Entry> lineEntries = new ArrayList<>();
        lineEntries.add(new Entry(0, 10));
        lineEntries.add(new Entry(1, 12));
        lineEntries.add(new Entry(2, 18));
        lineEntries.add(new Entry(3, 25));
        lineEntries.add(new Entry(4, 20));
        lineEntries.add(new Entry(5, 22));
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "");
        lineDataSet.setColor(android.graphics.Color.parseColor("#26A69A"));
        lineDataSet.setCircleColor(android.graphics.Color.parseColor("#26A69A"));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(5f);
        lineDataSet.setDrawValues(false);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        final String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun"};
        lineChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
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
        return view;
    }
}
