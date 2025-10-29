package com.example.puriqtours.superadmin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.puriqtours.R;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import java.util.ArrayList;

public class EmpresasReportesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_empresas_reportes, container, false);
        // HorizontalBarChart: Ranking de empresas con más reservas
        HorizontalBarChart barChart = view.findViewById(R.id.barChartEmpresas);
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, 1402));
        barEntries.add(new BarEntry(1, 1360));
        barEntries.add(new BarEntry(2, 1120));
        barEntries.add(new BarEntry(3, 600));
        BarDataSet barDataSet = new BarDataSet(barEntries, "Reservas");
        barDataSet.setColor(android.graphics.Color.parseColor("#26A69A"));
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        final String[] empresas = {"Silk Road", "Oxapampa Tour", "InkaTravel", "Otros"};
        barChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
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

        RecyclerView rvEmpresasRegistradas = view.findViewById(R.id.rvEmpresasRegistradas);
        rvEmpresasRegistradas.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        java.util.List<String> nombres = new java.util.ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            nombres.add("Empresa " + i);
        }
        AvatarNombreAdapter adapter = new AvatarNombreAdapter(getContext(), nombres, R.drawable.ic_company_superadmin);
        rvEmpresasRegistradas.setAdapter(adapter);

        return view;
    }
}
