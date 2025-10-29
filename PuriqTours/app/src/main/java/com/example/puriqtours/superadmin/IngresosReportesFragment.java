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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;

public class IngresosReportesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingresos_reportes, container, false);
        // Gráfico de área (línea rellena)
        LineChart areaChart = view.findViewById(R.id.areaChartIngresos);
        ArrayList<Entry> areaEntries = new ArrayList<>();
        areaEntries.add(new Entry(0, 10000));
        areaEntries.add(new Entry(1, 18000));
        areaEntries.add(new Entry(2, 15000));
        areaEntries.add(new Entry(3, 22000));
        areaEntries.add(new Entry(4, 17000));
        areaEntries.add(new Entry(5, 20000));
        LineDataSet areaDataSet = new LineDataSet(areaEntries, "Reservas");
        areaDataSet.setColor(android.graphics.Color.parseColor("#9575CD"));
        areaDataSet.setCircleColor(android.graphics.Color.parseColor("#9575CD"));
        areaDataSet.setLineWidth(2f);
        areaDataSet.setCircleRadius(4f);
        areaDataSet.setDrawFilled(true);
        areaDataSet.setFillColor(android.graphics.Color.parseColor("#B39DDB"));
        areaDataSet.setDrawValues(false);
        LineData areaData = new LineData(areaDataSet);
        areaChart.setData(areaData);
        areaChart.getDescription().setEnabled(false);
        areaChart.getLegend().setEnabled(false);
        final String[] meses = {"April", "May", "June"};
        areaChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                return (i >= 0 && i < meses.length) ? meses[i] : "";
            }
        });
        areaChart.getXAxis().setGranularity(1f);
        areaChart.getXAxis().setGranularityEnabled(true);
        areaChart.getXAxis().setDrawGridLines(false);
        areaChart.getAxisRight().setEnabled(false);
        areaChart.getAxisLeft().setDrawGridLines(false);
        areaChart.invalidate();
        // Gráfico de dona (ventas por tour más vendido)
        PieChart pieChart = view.findViewById(R.id.pieChartVentas);
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(4500, "Cusco"));
        pieEntries.add(new PieEntry(2500, "Oxapampa"));
        pieEntries.add(new PieEntry(1400, "Arequipa"));
        pieEntries.add(new PieEntry(400, "Amazonas"));
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(android.graphics.Color.parseColor("#FFD54F"));
        colors.add(android.graphics.Color.parseColor("#80CBC4"));
        colors.add(android.graphics.Color.parseColor("#B2DFDB"));
        colors.add(android.graphics.Color.parseColor("#F8BBD0"));
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextColor(android.graphics.Color.TRANSPARENT);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
        return view;
    }
}
