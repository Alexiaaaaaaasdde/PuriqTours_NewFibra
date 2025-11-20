package com.example.puriqtours.superadmin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReservasReportesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservas_reportes, container, false);
        // TextView resumen (total cost + total reservas)
        TextView tvTotalResumen = view.findViewById(R.id.tvTotalReservasResumen);

        // LineChart de reservas por mes
        LineChart lineChart = view.findViewById(R.id.lineChartReservas);

        // Inicializar Firestore y obtener datos
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tours")
                .whereEqualTo("estado", "terminado")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        // Preparar últimos 6 meses (0 = 5 meses atrás, 5 = mes actual)
                        List<Date> monthStarts = new ArrayList<>();
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        for (int i = 5; i >= 0; i--) {
                            Calendar tmp = (Calendar) cal.clone();
                            tmp.add(Calendar.MONTH, -i);
                            monthStarts.add(tmp.getTime());
                        }

                        // Log month ranges for debugging
                        for (int i = 0; i < monthStarts.size(); i++) {
                            Date s = monthStarts.get(i);
                            Calendar cEndLog = Calendar.getInstance();
                            cEndLog.setTime(s);
                            cEndLog.add(Calendar.MONTH, 1);
                            Date e = cEndLog.getTime();
                            Log.d("ReservasDebug", "Month idx=" + i + " start=" + s + " end=" + e);
                        }

                        // Map para conteo por mes (indices 0..5)
                        Map<Integer, Integer> counts = new HashMap<>();
                        for (int i = 0; i < 6; i++) counts.put(i, 0);

                        // Map para conteo por empresa (empresaId -> cantidad de tours)
                        Map<String, Integer> empresaCounts = new HashMap<>();

                        double totalCost = 0;
                        int totalTours = 0;

                        Log.d("ReservasDebug", "Query returned docs=" + queryDocumentSnapshots.size());

                        Log.d("ReservasData", "Total documents found: " + queryDocumentSnapshots.size());
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Log.d("ReservasData", "\n--- Tour Details ---");
                            Log.d("ReservasData", "Tour ID: " + doc.getId());
                            Log.d("ReservasData", "Estado: " + doc.getString("estado"));
                            
                            Object inicioObj = doc.get("inicioTour");
                            Log.d("ReservasData", "inicioTour (raw): " + inicioObj);
                            Log.d("ReservasData", "inicioTour type: " + (inicioObj != null ? inicioObj.getClass().getName() : "null"));
                            
                            Timestamp ts = null;
                            Date d = null;
                            Long costo = doc.getLong("costo");
                            if (costo != null) {
                                totalCost += costo.doubleValue();
                                Log.d("ReservasData", "Costo: " + costo);
                            }

                            // Count every doc as a tour (even if date missing) for the totalTours summary
                            totalTours++;

                            // Contar por empresa (campo 'empresa' guarda el id de la empresa)
                            String empresaId = doc.getString("empresa");
                            if (empresaId != null) {
                                Integer prev = empresaCounts.get(empresaId);
                                empresaCounts.put(empresaId, (prev == null) ? 1 : prev + 1);
                            }

                            // Normalize inicioTour whether it's a Timestamp or a String
                            if (inicioObj instanceof Timestamp) {
                                ts = (Timestamp) inicioObj;
                                d = ts.toDate();
                            } else if (inicioObj instanceof String) {
                                String fechaStr = (String) inicioObj;
                                try {
                                    SimpleDateFormat sdfParse = new SimpleDateFormat("d 'de' MMMM 'de' yyyy, h:mm:ss a z", new Locale("es", "ES"));
                                    d = sdfParse.parse(fechaStr);
                                } catch (Exception e) {
                                    Log.e("ReservasDebug", "Failed to parse inicioTour string for doc=" + doc.getId() + " value=" + fechaStr, e);
                                }
                            } else if (inicioObj != null) {
                                // attempt to log unexpected types
                                Log.d("ReservasDebug", "Doc=" + doc.getId() + " inicioTour type=" + inicioObj.getClass().getName() + " value=" + inicioObj.toString());
                            } else {
                                Log.d("ReservasDebug", "Doc=" + doc.getId() + " has no inicioTour field");
                            }

                            if (d == null) {
                                Log.e("ReservasData", "ERROR: Doc=" + doc.getId() + " date is null or unparsable");
                                continue; // cannot assign month if no date
                            }
                            Log.d("ReservasData", "Fecha procesada final: " + d);

                            // Encontrar mes correspondiente (con logs detallados)
                            boolean matched = false;
                            for (int i = 0; i < monthStarts.size(); i++) {
                                Date start = monthStarts.get(i);
                                Calendar cEnd = Calendar.getInstance();
                                cEnd.setTime(start);
                                cEnd.add(Calendar.MONTH, 1);
                                Date end = cEnd.getTime();
                                boolean geStart = !d.before(start);
                                boolean ltEnd = d.before(end);
                                Log.d("ReservasDebug", "Doc=" + doc.getId() + " date=" + d + " checking idx=" + i + " start=" + start + " end=" + end + " geStart=" + geStart + " ltEnd=" + ltEnd);
                                if (geStart && ltEnd) {
                                    int currentCount = counts.get(i);
                                    counts.put(i, currentCount + 1);
                                    Log.d("ReservasCount", "Adding tour to month " + i + ": " + d + " - Current count: " + (currentCount + 1));
                                    matched = true;
                                    break;
                                }
                            }
                            if (!matched) {
                                Log.e("ReservasData", "ALERTA: Tour " + doc.getId() + " con fecha " + d + " no cayó en ningún rango de meses");
                            } else {
                                Log.d("ReservasData", "Tour contabilizado correctamente");
                            }
                        }

                        // Preparar entradas (usar 1f si es 0 para evitar crash; mostraremos "0" visualmente)
                        ArrayList<Entry> lineEntries = new ArrayList<>();
                        boolean hasRealData = false;
                        // Debug total counts
                        Log.d("ReservasTotal", "Total counts by month:");
                        for (int i = 0; i < 6; i++) {
                            Log.d("ReservasTotal", "Month " + i + ": " + counts.get(i));
                        }
                        
                        for (int i = 0; i < 6; i++) {
                            int c = counts.get(i);
                            if (c > 0) hasRealData = true;
                            // Use the real count as the entry Y value (0f when no tours).
                            float v = (float) c;
                            lineEntries.add(new Entry(i, v));
                            Log.d("ReservasChart", "Mes idx=" + i + " count=" + c + " value=" + v);
                        }

                        // Actualizar texto resumen
                        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("es", "ES"));
                        nf.setMinimumFractionDigits(2);
                        nf.setMaximumFractionDigits(2);
                        String totalCostStr = nf.format(totalCost);
                        tvTotalResumen.setText(totalCostStr + "\n" + totalTours + " Reservas");

                        // Crear dataset y formateadores
                        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Reservas");
                        lineDataSet.setColor(android.graphics.Color.parseColor("#26A69A"));
                        lineDataSet.setCircleColor(android.graphics.Color.parseColor("#26A69A"));
                        lineDataSet.setLineWidth(2f);
                        lineDataSet.setCircleRadius(5f);
                        lineDataSet.setDrawValues(true);
                        lineDataSet.setDrawCircles(true);
                        lineDataSet.setDrawFilled(false);
                        // Mostrar "0" cuando el valor interno es ~1f
                        lineDataSet.setValueFormatter(new ValueFormatter() {
                            @Override
                            public String getFormattedValue(float value) {
                                // value is the Y value we set on the Entry, so return it directly
                                if (Math.abs(value) < 0.001f) return "0";
                                return String.format(Locale.getDefault(), "%.0f", value);
                            }
                        });

                        LineData lineData = new LineData(lineDataSet);
                        lineChart.setData(lineData);
                        lineChart.getDescription().setEnabled(false);
                        lineChart.getLegend().setEnabled(true);
                        lineChart.setTouchEnabled(true);
                        lineChart.setDragEnabled(true);
                        lineChart.setScaleEnabled(true);
                        lineChart.setPinchZoom(true);
                        lineChart.setNoDataText("No hay datos disponibles");

                        // Formatear etiquetas X con nombres de meses basados en monthStarts
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM", new Locale("es", "ES"));
                        final List<Date> monthStartsFinal = monthStarts;
                        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
                            @Override
                            public String getFormattedValue(float value) {
                                int i = (int) value;
                                if (i >= 0 && i < monthStartsFinal.size()) {
                                    String m = sdf.format(monthStartsFinal.get(i));
                                    return m.substring(0, 1).toUpperCase() + m.substring(1);
                                }
                                return "";
                            }
                        });

                        lineChart.getXAxis().setGranularity(1f);
                        lineChart.getXAxis().setGranularityEnabled(true);
                        lineChart.getXAxis().setDrawGridLines(false);
                        lineChart.getAxisRight().setEnabled(false);
                        lineChart.getAxisLeft().setDrawGridLines(false);
                        lineChart.invalidate();

                        // ----------------
                        // Construir gráfico de barras dinámico: reservas por empresa
                        // ----------------
                        BarChart barChart = view.findViewById(R.id.barChartEmpresas);
                        if (empresaCounts.isEmpty()) {
                            // no hay datos
                            barChart.clear();
                            barChart.getDescription().setEnabled(false);
                            barChart.invalidate();
                        } else {
                            List<String> ids = new ArrayList<>(empresaCounts.keySet());
                            Map<String, String> empresaNames = new HashMap<>();
                            final int[] remaining = new int[]{ids.size()};
                            for (String id : ids) {
                                db.collection("empresas").document(id).get()
                                        .addOnSuccessListener(docSnap -> {
                                            String name = null;
                                            if (docSnap.exists()) {
                                                name = docSnap.getString("nombre");
                                                if (name == null) name = docSnap.getString("name");
                                            }
                                            if (name == null) name = id;
                                            empresaNames.put(id, name);
                                            remaining[0]--;
                                            if (remaining[0] == 0) {
                                                buildBarChart(barChart, ids, empresaCounts, empresaNames);
                                            }
                                        }).addOnFailureListener(e -> {
                                    empresaNames.put(id, id);
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        buildBarChart(barChart, ids, empresaCounts, empresaNames);
                                    }
                                });
                            }
                        }

                    } catch (Exception e) {
                        Log.e("ReservasReportes", "Error procesando datos para reporte de reservas", e);
                    }
                });
        // El BarChart de empresas se construye dinámicamente dentro del listener de Firestore
        return view;
    }

    // Helper para construir el BarChart una vez que tenemos ids, conteos y nombres
    private void buildBarChart(BarChart barChart, List<String> ids, Map<String, Integer> empresaCounts, Map<String, String> empresaNames) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            int c = empresaCounts.containsKey(id) ? empresaCounts.get(id) : 0;
            barEntries.add(new BarEntry(i, (float) c));
            labels.add(empresaNames.getOrDefault(id, id));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Empresas");
        barDataSet.setColor(android.graphics.Color.parseColor("#26A69A"));
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                return (i >= 0 && i < labels.size()) ? labels.get(i) : "";
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
    }
}
