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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
public class UsuariosReportesFragment extends Fragment {
    private static final String TAG = "UsuariosReportesFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usuarios_reportes, container, false);
        
        // Gráfico de líneas: Total de usuarios registrados
        LineChart lineChart = view.findViewById(R.id.lineChartUsuarios);
        setupInitialChartState(lineChart);

        // Obtener datos de Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                HashMap<String, Integer> usuariosPorMes = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("d 'de' MMMM 'de' yyyy, HH:mm:ss a. 'UTC'X", new Locale("es", "ES"));
                
                // Inicializar todos los meses del año actual con 0
                Calendar cal = Calendar.getInstance();
                int currentYear = cal.get(Calendar.YEAR);
                for (int i = 0; i < 12; i++) {
                    cal.set(currentYear, i, 1);
                    String mesKey = new SimpleDateFormat("MMM yyyy", new Locale("es", "ES")).format(cal.getTime());
                    usuariosPorMes.put(mesKey, 0);
                }

                // Procesar cada usuario
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    try {
                        Object registroObj = doc.get("registro");
                        Date fechaRegistro;
                        
                        if (registroObj instanceof com.google.firebase.Timestamp) {
                            fechaRegistro = ((com.google.firebase.Timestamp) registroObj).toDate();
                        } else if (registroObj != null) {
                            fechaRegistro = sdf.parse(registroObj.toString());
                        } else {
                            continue;
                        }

                        String mesKey = new SimpleDateFormat("MMM yyyy", new Locale("es", "ES")).format(fechaRegistro);
                        usuariosPorMes.put(mesKey, usuariosPorMes.getOrDefault(mesKey, 0) + 1);
                        
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error procesando fecha: " + e.getMessage());
                    }
                }

                // Ordenar meses y crear entradas para el gráfico
                ArrayList<String> mesesOrdenados = new ArrayList<>(usuariosPorMes.keySet());
                mesesOrdenados.sort((a, b) -> {
                    try {
                        Date dateA = new SimpleDateFormat("MMM yyyy", new Locale("es", "ES")).parse(a);
                        Date dateB = new SimpleDateFormat("MMM yyyy", new Locale("es", "ES")).parse(b);
                        return dateA.compareTo(dateB);
                    } catch (Exception e) {
                        return 0;
                    }
                });

                ArrayList<Entry> lineEntries = new ArrayList<>();
                for (int i = 0; i < mesesOrdenados.size(); i++) {
                    String mes = mesesOrdenados.get(i);
                    int cantidad = usuariosPorMes.get(mes);
                    lineEntries.add(new Entry(i, cantidad));
                    android.util.Log.d(TAG, "Mes: " + mes + ", Usuarios: " + cantidad);
                }
                LineDataSet lineDataSet = new LineDataSet(lineEntries, "Usuarios por mes");
                lineDataSet.setColor(android.graphics.Color.parseColor("#26A69A"));
                lineDataSet.setCircleColor(android.graphics.Color.parseColor("#26A69A"));
                lineDataSet.setLineWidth(2f);
                lineDataSet.setCircleRadius(5f);
                lineDataSet.setDrawValues(true); // Mostrar valores
                lineDataSet.setValueTextColor(android.graphics.Color.parseColor("#26A69A"));
                lineDataSet.setValueTextSize(10f);
                lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Curva suave
                lineDataSet.setDrawFilled(true); // Área bajo la línea
                lineDataSet.setFillColor(android.graphics.Color.parseColor("#80CBC4"));
                LineData lineData = new LineData(lineDataSet);
                lineChart.setData(lineData);
                lineChart.getDescription().setEnabled(false);
                lineChart.getLegend().setEnabled(false);

                // Configurar eje X con los meses
                lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        int i = (int) value;
                        return (i >= 0 && i < mesesOrdenados.size()) ? mesesOrdenados.get(i) : "";
                    }
                });
                lineChart.getXAxis().setGranularity(1f);
                lineChart.getXAxis().setGranularityEnabled(true);
                lineChart.getXAxis().setDrawGridLines(false);
                
                // Configurar ejes Y
                lineChart.getAxisRight().setEnabled(false);
                lineChart.getAxisLeft().setDrawGridLines(false);
                lineChart.getAxisLeft().setAxisMinimum(0f); // Empezar desde 0
                
                // Si solo hay datos para un mes
                if (mesesOrdenados.size() == 1) {
                    lineChart.getXAxis().setAxisMinimum(-0.5f);
                    lineChart.getXAxis().setAxisMaximum(0.5f);
                }
                
                lineChart.invalidate();
            }
        });

        // PieChart: Distribución de usuarios
        PieChart pieChart = view.findViewById(R.id.pieChartDistribucionUsuarios);
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

        // RecyclerView: Usuarios bloqueados
        RecyclerView rvUsuariosBloqueados = view.findViewById(R.id.rvUsuariosBloqueados);
        rvUsuariosBloqueados.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        java.util.List<String> nombres = new java.util.ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            nombres.add("Usuario " + i);
        }
        AvatarNombreAdapter adapter = new AvatarNombreAdapter(getContext(), nombres, R.drawable.avatar1);
        rvUsuariosBloqueados.setAdapter(adapter);

        return view;
    }

    private void setupInitialChartState(LineChart lineChart) {
        lineChart.setNoDataText("Cargando datos...");
        lineChart.setNoDataTextColor(android.graphics.Color.parseColor("#26A69A"));
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDoubleTapToZoomEnabled(false);
    }
}
