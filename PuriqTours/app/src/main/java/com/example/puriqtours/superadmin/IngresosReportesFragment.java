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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class IngresosReportesFragment extends Fragment {
    private static final String TAG = "IngresosReportesFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingresos_reportes, container, false);
        
        // Gráficos dinámicos con Firestore
        LineChart areaChart = view.findViewById(R.id.areaChartIngresos);
        PieChart pieChart = view.findViewById(R.id.pieChartVentas);
        // TextView para mostrar el total de ingresos
        android.widget.TextView tvIngresos = view.findViewById(R.id.tvTotalIngresosResumen);

        // Configuración inicial de los gráficos
        setupInitialChartState(areaChart, pieChart);        // Configuración y consulta de Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tours").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot.isEmpty()) {
                    android.util.Log.d(TAG, "No se encontraron tours");
                    updateUIForNoData(tvIngresos, areaChart, pieChart);
                    return;
                }

                // Para el gráfico de área: costos por mes
                HashMap<String, Float> costosPorMes = new HashMap<>();
                // Para el gráfico de dona: costos por departamento
                HashMap<String, Float> costosPorDepartamento = new HashMap<>();
                float sumaTotalCostos = 0f;
                SimpleDateFormat sdf = new SimpleDateFormat("d 'de' MMMM 'de' yyyy, HH:mm:ss a. 'UTC'X", new Locale("es", "ES"));
                
                android.util.Log.d(TAG, "Iniciando procesamiento de documentos...");
                for (QueryDocumentSnapshot doc : snapshot) {
                    android.util.Log.d(TAG, "Procesando documento ID: " + doc.getId());
                    
                    android.util.Log.d(TAG, "Documento completo: " + doc.getData());
                    
                    Object costoObj = doc.get("costo");
                    String departamentoObj = doc.getString("departamento"); // Cambiado a getString
                    android.util.Log.d(TAG, "Datos raw - costo: " + costoObj + ", departamento (getString): " + departamentoObj);
                    
                    // Intentar obtener departamento de diferentes formas si es null
                    if (departamentoObj == null) {
                        Object deptObj = doc.get("departamento");
                        android.util.Log.d(TAG, "Intentando obtener departamento con get(): " + deptObj);
                        if (deptObj != null) {
                            departamentoObj = deptObj.toString();
                        }
                    }
                    
                    float costo = 0f;
                    if (costoObj != null) {
                        try {
                            costo = Float.parseFloat(costoObj.toString());
                            android.util.Log.d(TAG, "Costo parseado correctamente: " + costo);
                        } catch (Exception e) {
                            android.util.Log.e(TAG, "Error parseando costo: " + e.getMessage());
                        }
                    } else {
                        android.util.Log.d(TAG, "Costo es null");
                    }
                    sumaTotalCostos += costo;
                    // Agrupar por mes
                    Date date = null;
                    try {
                        // Si es Timestamp, obtén como Date
                        date = doc.getDate("inicioTour");
                        android.util.Log.d(TAG, "Fecha obtenida como Timestamp: " + date);
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error obteniendo fecha como Timestamp: " + e.getMessage());
                    }
                    
                    if (date == null) {
                        // Si no es Date, intenta parsear como String
                        Object inicioTourObj = doc.get("inicioTour");
                        android.util.Log.d(TAG, "Intentando parsear fecha de String. Valor: " + inicioTourObj);
                        if (inicioTourObj != null) {
                            try {
                                date = sdf.parse(inicioTourObj.toString());
                                android.util.Log.d(TAG, "Fecha parseada de String: " + date);
                            } catch (Exception e) {
                                android.util.Log.e(TAG, "Error parseando fecha de String: " + e.getMessage());
                            }
                        }
                    }
                    if (date != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        String mes = new SimpleDateFormat("MMM yyyy", new Locale("es", "ES")).format(date);
                        float actual = costosPorMes.containsKey(mes) ? costosPorMes.get(mes) : 0f;
                        costosPorMes.put(mes, actual + costo);
                    }
                    // Agrupar por departamento
                    String departamento = departamentoObj != null ? departamentoObj : "Sin departamento";
                    android.util.Log.d(TAG, "Departamento final usado: " + departamento);
                    float actual = costosPorDepartamento.containsKey(departamento) ? costosPorDepartamento.get(departamento) : 0f;
                    costosPorDepartamento.put(departamento, actual + costo);
                }
                // Mostrar la suma total de ingresos en el TextView
                if (tvIngresos != null) {
                    tvIngresos.setText(String.format(Locale.US, "%.2f$", sumaTotalCostos));
                }
                android.util.Log.d(TAG, "Total de costos encontrados: " + sumaTotalCostos);
                // Actualizar el TextView con el total
                if (tvIngresos != null) {
                    tvIngresos.setText(String.format(Locale.US, "%.2f$", sumaTotalCostos));
                }

                android.util.Log.d(TAG, "Resumen de datos procesados:");
                android.util.Log.d(TAG, "Costos por mes: " + costosPorMes.toString());
                android.util.Log.d(TAG, "Costos por departamento: " + costosPorDepartamento.toString());
                
                if (costosPorMes.isEmpty() && costosPorDepartamento.isEmpty()) {
                    android.util.Log.d(TAG, "No hay datos válidos para mostrar en los gráficos");
                    updateUIForNoData(tvIngresos, areaChart, pieChart);
                    return;
                }

                android.util.Log.d(TAG, "Iniciando configuración de gráficos");
                // Gráfico de área: mostrar meses ordenados
                ArrayList<String> mesesOrdenados = new ArrayList<>(costosPorMes.keySet());
                mesesOrdenados.sort((a, b) -> {
                    try {
                        Date da = new SimpleDateFormat("MMM yyyy", new Locale("es", "ES")).parse(a);
                        Date db_ = new SimpleDateFormat("MMM yyyy", new Locale("es", "ES")).parse(b);
                        return da.compareTo(db_);
                    } catch (Exception e) { 
                        android.util.Log.e(TAG, "Error ordenando fechas: " + e.getMessage());
                        return 0; 
                    }
                });
                ArrayList<Entry> areaEntries = new ArrayList<>();
                for (int i = 0; i < mesesOrdenados.size(); i++) {
                    String mes = mesesOrdenados.get(i);
                    areaEntries.add(new Entry(i, costosPorMes.get(mes)));
                }
                LineDataSet areaDataSet = new LineDataSet(areaEntries, "Ingresos por mes");
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
                
                // Configuración del eje X
                areaChart.getXAxis().setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        int i = (int) value;
                        return (i >= 0 && i < mesesOrdenados.size()) ? mesesOrdenados.get(i) : "";
                    }
                });
                areaChart.getXAxis().setGranularity(1f);
                areaChart.getXAxis().setGranularityEnabled(true);
                areaChart.getXAxis().setDrawGridLines(false);
                
                // Configuración de ejes Y
                areaChart.getAxisRight().setEnabled(false);
                areaChart.getAxisLeft().setDrawGridLines(false);
                areaChart.getAxisLeft().setAxisMinimum(0f); // Empezar desde 0
                
                // Si solo hay un mes, ajustar el rango del eje X
                if (mesesOrdenados.size() == 1) {
                    areaChart.getXAxis().setAxisMinimum(-0.5f);
                    areaChart.getXAxis().setAxisMaximum(0.5f);
                }
                areaChart.invalidate();
                // Gráfico de dona: proporción por departamento
                ArrayList<PieEntry> pieEntries = new ArrayList<>();
                for (String departamento : costosPorDepartamento.keySet()) {
                    float value = costosPorDepartamento.get(departamento);
                    if (value > 0) {
                        pieEntries.add(new PieEntry(value, departamento));
                    }
                }
                if (pieEntries.size() > 0) {
                    PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
                    ArrayList<Integer> colors = new ArrayList<>();
                    colors.add(android.graphics.Color.parseColor("#FFD54F")); // Amarillo
                    colors.add(android.graphics.Color.parseColor("#80CBC4")); // Verde
                    colors.add(android.graphics.Color.parseColor("#B2DFDB")); // Celeste
                    colors.add(android.graphics.Color.parseColor("#F8BBD0")); // Rosa
                    colors.add(android.graphics.Color.parseColor("#A5D6A7")); // Verde claro
                    colors.add(android.graphics.Color.parseColor("#FFAB91")); // Naranja
                    pieDataSet.setColors(colors);
                    pieDataSet.setValueTextColor(android.graphics.Color.TRANSPARENT);
                    PieData pieData = new PieData(pieDataSet);
                    pieChart.setData(pieData);
                    pieChart.getDescription().setEnabled(false);
                    pieChart.getLegend().setEnabled(true);
                    pieChart.setDrawEntryLabels(false);
                    pieChart.invalidate();
                } else {
                    pieChart.clear();
                }
            }
        });
        return view;
    }

    private void setupInitialChartState(LineChart areaChart, PieChart pieChart) {
        // Configuración inicial del gráfico de área
        areaChart.setNoDataText("No hay datos disponibles");
        areaChart.setNoDataTextColor(android.graphics.Color.parseColor("#26A69A"));
        
        // Configuración inicial del gráfico de dona
        pieChart.setNoDataText("No hay datos disponibles");
        pieChart.setNoDataTextColor(android.graphics.Color.parseColor("#26A69A"));
    }

    private void updateUIForNoData(android.widget.TextView tvIngresos, LineChart areaChart, PieChart pieChart) {
        if (tvIngresos != null) {
            tvIngresos.setText("0.00$");
        }
        areaChart.clear();
        pieChart.clear();
        areaChart.invalidate();
        pieChart.invalidate();
    }
}
