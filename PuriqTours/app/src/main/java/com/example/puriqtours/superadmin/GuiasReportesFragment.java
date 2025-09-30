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
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class GuiasReportesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guias_reportes, container, false);
        // PieChart: Idiomas más hablados
        PieChart pieChart = view.findViewById(R.id.pieChartIdiomas);
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(50f, "Español"));
        pieEntries.add(new PieEntry(30f, "Quechua"));
        pieEntries.add(new PieEntry(15f, "Inglés"));
        pieEntries.add(new PieEntry(5f, "Otros"));
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(android.graphics.Color.parseColor("#26A69A"));
        colors.add(android.graphics.Color.parseColor("#FFD54F"));
        colors.add(android.graphics.Color.parseColor("#F06292"));
        colors.add(android.graphics.Color.parseColor("#FFA726"));
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextColor(android.graphics.Color.TRANSPARENT);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
        // HorizontalBarChart: Cantidad de tours atendidos por guías
        HorizontalBarChart barChart = view.findViewById(R.id.barChartGuias);
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, 1402));
        barEntries.add(new BarEntry(1, 1360));
        barEntries.add(new BarEntry(2, 1120));
        barEntries.add(new BarEntry(3, 600));
        BarDataSet barDataSet = new BarDataSet(barEntries, "Tours");
        barDataSet.setColor(android.graphics.Color.parseColor("#26A69A"));
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        final String[] guias = {"Juan Perez", "Enrique Herrera", "Dorian Felix", "Alejandro Bienvenide"};
        barChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                return (i >= 0 && i < guias.length) ? guias[i] : "";
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
        RecyclerView rvGuiasHabilitados = view.findViewById(R.id.rvGuiasHabilitados);
        rvGuiasHabilitados.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        java.util.List<String> nombres = new java.util.ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            nombres.add("Guía " + i);
        }
        AvatarNombreAdapter adapter = new AvatarNombreAdapter(getContext(), nombres, R.drawable.ic_guide_superadmin);
        rvGuiasHabilitados.setAdapter(adapter);
        return view;
    }
}
