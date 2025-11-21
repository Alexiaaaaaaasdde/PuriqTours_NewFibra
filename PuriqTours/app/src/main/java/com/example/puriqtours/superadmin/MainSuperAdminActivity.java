package com.example.puriqtours.superadmin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.util.TypedValue;
import android.view.ViewGroup;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;

public class MainSuperAdminActivity extends AppCompatActivity {

    private TextView tvActiveUsersCount;
    private TextView tvEnabledGuidesCount;
    private TextView tvRegisteredCompaniesCount;
    private LinearLayout llToursBarsContainer;
    private FirebaseFirestore db;
    private ListenerRegistration usersListener;
    private GuiasHorizontalAdapter guiasAdapter;
    
    private void setupUsersListener() {
        if (usersListener != null) {
            usersListener.remove();
        }

        // Configurar el listener con source específico para forzar server
        db.collection("users")
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(snapshot -> {
                Log.d("MainSuperAdmin", "Datos obtenidos del servidor - total docs: " + snapshot.size());
                int activeUsers = 0;
                int enabledGuides = 0;
                
                for (QueryDocumentSnapshot doc : snapshot) {
                    String rol = doc.getString("rol");
                    String state = doc.getString("state");
                    String username = doc.getString("username");
                    
                    Log.d("MainSuperAdmin", "Usuario: " + username + ", rol: " + rol + ", state: " + state);
                    
                    // Contar usuarios activos (no SuperAdmin)
                    if (rol == null || !"SuperAdmin".equalsIgnoreCase(rol)) {
                        activeUsers++;
                    }
                    
                    // Contar guías habilitados
                    if ("Guia".equalsIgnoreCase(rol) && "habilitado".equalsIgnoreCase(state)) {
                        enabledGuides++;
                    }
                }
                
                Log.d("MainSuperAdmin", "Total usuarios activos (no SuperAdmin): " + activeUsers);
                Log.d("MainSuperAdmin", "Total guías habilitados: " + enabledGuides);
                
                if (tvActiveUsersCount != null) {
                    tvActiveUsersCount.setText(String.valueOf(activeUsers));
                }
                if (tvEnabledGuidesCount != null) {
                    tvEnabledGuidesCount.setText(String.valueOf(enabledGuides));
                }

                // Consultar el total de empresas
                db.collection("empresas")
                    .get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(empresasSnapshot -> {
                        Log.d("MainSuperAdmin", "Total empresas registradas: " + empresasSnapshot.size());
                        if (tvRegisteredCompaniesCount != null) {
                            tvRegisteredCompaniesCount.setText(String.valueOf(empresasSnapshot.size()));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainSuperAdmin", "Error al obtener empresas: " + e.getMessage(), e);
                        if (tvRegisteredCompaniesCount != null) {
                            tvRegisteredCompaniesCount.setText("--");
                        }
                    });
                
                // Consultar tours y construir gráfico de barras simple (usuarios por tour)
                db.collection("tours")
                    .get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(toursSnapshot -> {
                        Log.d("MainSuperAdmin", "Tours obtenidos: " + toursSnapshot.size());
                        // Map para mantener orden de inserción
                        Map<String, Integer> tourCounts = new LinkedHashMap<>();
                        int maxCount = 0;
                        for (QueryDocumentSnapshot tourDoc : toursSnapshot) {
                            String tourName = tourDoc.getString("nombre");
                            if (tourName == null) tourName = tourDoc.getId();
                            int count = 0;
                            // Contar campos que empiezan por 'usuario'
                            for (String key : tourDoc.getData().keySet()) {
                                if (key != null && key.toLowerCase().startsWith("usuario")) {
                                    Object val = tourDoc.get(key);
                                    if (val != null) count++;
                                }
                            }
                            tourCounts.put(tourName, count);
                            if (count > maxCount) maxCount = count;
                        }

                        // Construir vistas en la UI
                        final int finalMaxCount = maxCount; // capturar para uso dentro del lambda
                        final int maxBarHeightDp = 100; // altura máxima de barra en dp
                        runOnUiThread(() -> {
                            if (llToursBarsContainer == null) return;
                            llToursBarsContainer.removeAllViews();
                            for (Map.Entry<String, Integer> entry : tourCounts.entrySet()) {
                                String name = entry.getKey();
                                int cnt = entry.getValue();

                                // calcular altura proporcional
                                int barHeightDp = (finalMaxCount > 0) ? Math.max(4, Math.round((cnt / (float) finalMaxCount) * maxBarHeightDp)) : 4;

                                LinearLayout item = new LinearLayout(MainSuperAdminActivity.this);
                                item.setOrientation(LinearLayout.VERTICAL);
                                item.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                                LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                                        dpToPx(60), ViewGroup.LayoutParams.MATCH_PARENT);
                                itemParams.setMargins(dpToPx(8), 0, dpToPx(8), 0);
                                item.setLayoutParams(itemParams);

                                View bar = new View(MainSuperAdminActivity.this);
                                LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(barHeightDp));
                                barParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                            bar.setLayoutParams(barParams);
                            bar.setBackgroundColor(Color.parseColor("#B2DFDB"));
                            
                            // Agregar OnClickListener para mostrar el valor
                            final int finalCount = cnt;
                            item.setOnClickListener(v -> {
                                android.widget.Toast.makeText(MainSuperAdminActivity.this, 
                                    String.format(Locale.getDefault(), "Usuarios: %d", finalCount),
                                    android.widget.Toast.LENGTH_SHORT).show();
                            });

                            TextView tvName = new TextView(MainSuperAdminActivity.this);
                                LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                nameParams.topMargin = dpToPx(6);
                                tvName.setLayoutParams(nameParams);
                                tvName.setText(name);
                                tvName.setTextSize(12f);
                                tvName.setTextColor(Color.DKGRAY);
                                tvName.setGravity(Gravity.CENTER);

                                item.addView(bar);
                                item.addView(tvName);
                                llToursBarsContainer.addView(item);
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainSuperAdmin", "Error al obtener tours: " + e.getMessage(), e);
                    });
            })
            .addOnFailureListener(e -> {
                Log.e("MainSuperAdmin", "Error al obtener usuarios: " + e.getMessage(), e);
                if (tvActiveUsersCount != null) {
                    tvActiveUsersCount.setText("--");
                }
                if (tvEnabledGuidesCount != null) {
                    tvEnabledGuidesCount.setText("--");
                }
            });
    }

    // Cargar empresas y agregar conteo por mes (últimos 6 meses) al LineChart
    private void loadCompaniesMonthlyData(LineChart lineChart) {
        // Asegurarnos de tener una instancia de Firestore aunque loadCompaniesMonthlyData
        // se llame antes de inicializar `db` en onCreate.
        if (db == null) db = FirebaseFirestore.getInstance();
        if (lineChart == null) return;

        // Preparar etiquetas y contador inicial con ceros para los últimos 6 meses.
        Calendar now = Calendar.getInstance();
        now.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM", new Locale("es"));
        final ArrayList<String> labels = new ArrayList<>();
        final ArrayList<Integer> counts = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            Calendar c = (Calendar) now.clone();
            c.add(Calendar.MONTH, -i);
            String label = sdf.format(c.getTime()).substring(0, 3);
            label = label.substring(0, 1).toUpperCase() + label.substring(1);
            labels.add(label);
            counts.add(0);
        }

        // Consulta Firestore y conteo por mes. Solo busca y setea datos en el gráfico.
        db.collection("empresas")
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(snapshot -> {
                for (QueryDocumentSnapshot doc : snapshot) {
                    Object tsObj = doc.get("fechaRegistro");
                    Date date = null;
                    if (tsObj instanceof Timestamp) {
                        date = ((Timestamp) tsObj).toDate();
                    } else if (tsObj instanceof Date) {
                        date = (Date) tsObj;
                    } else {
                        // Si no es Timestamp/Date, ignorar (campo ausente o formato desconocido)
                        continue;
                    }
                    if (date == null) continue;

                    Calendar docCal = Calendar.getInstance();
                    docCal.setTime(date);
                    // Calcular diferencia en meses respecto al mes actual
                    int yearDiff = now.get(Calendar.YEAR) - docCal.get(Calendar.YEAR);
                    int monthDiff = now.get(Calendar.MONTH) - docCal.get(Calendar.MONTH);
                    int monthsDiff = yearDiff * 12 + monthDiff; // 0 = mismo mes, 1 = mes anterior, etc.
                    if (monthsDiff >= 0 && monthsDiff <= 5) {
                        int index = 5 - monthsDiff; // 5 = mes actual, 0 = más viejo
                        counts.set(index, counts.get(index) + 1);
                    }
                }

                // Construir arrays separados:
                // - originalCounts: lo que realmente proviene de la BD (0 si no hay datos)
                // - plottedCounts: lo que se dibuja en el gráfico (sustituye 0 por 1 para evitar crash)
                // - displayCounts: lo que se muestra como etiquetas/marker (mantiene 0 si originalmente era 0)
                final ArrayList<Integer> originalCounts = new ArrayList<>(counts);
                final ArrayList<Integer> plottedCounts = new ArrayList<>(counts);
                final ArrayList<Integer> displayCounts = new ArrayList<>(counts);
                for (int i = 0; i < originalCounts.size(); i++) {
                    if (originalCounts.get(i) == null || originalCounts.get(i) == 0) {
                        plottedCounts.set(i, 1); // valor para representar en el chart
                        displayCounts.set(i, 0); // valor que queremos mostrar externamente
                    } else {
                        // mantener valores reales
                        plottedCounts.set(i, originalCounts.get(i));
                        displayCounts.set(i, originalCounts.get(i));
                    }
                }

                // Construir entries y setear datos en el chart
                runOnUiThread(() -> {
                    java.util.List<Entry> entries = new ArrayList<>();
                    for (int i = 0; i < plottedCounts.size(); i++) {
                        entries.add(new Entry(i, plottedCounts.get(i)));
                    }
                    LineDataSet dataSet = new LineDataSet(entries, "Empresas");
                    dataSet.setColor(Color.parseColor("#26A69A"));
                    dataSet.setCircleColor(Color.parseColor("#26A69A"));
                    dataSet.setLineWidth(2f);
                    dataSet.setCircleRadius(5f);
                    dataSet.setDrawValues(true);
                    dataSet.setValueTextSize(12f);
                    dataSet.setValueTextColor(Color.parseColor("#26A69A"));

                    // Formateador para mostrar 0 donde correspondía originalmente, aunque el punto se dibuje con Y=1
                    final ArrayList<Integer> finalDisplay = displayCounts;
                    dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                        @Override
                        public String getPointLabel(Entry e) {
                            int idx = Math.round(e.getX());
                            if (idx >= 0 && idx < finalDisplay.size()) return String.valueOf(finalDisplay.get(idx));
                            return super.getPointLabel(e);
                        }
                    });

                    LineData lineData = new LineData(dataSet);
                    lineChart.setData(lineData);
                    lineChart.getDescription().setEnabled(false);
                    lineChart.getLegend().setEnabled(false);

                    // Marker simple que muestra el valor 'display' en lugar del valor real del punto
                    lineChart.setMarker(new com.github.mikephil.charting.components.MarkerView(this, R.layout.chart_marker_view) {
                        private TextView tvContent;
                        {
                            tvContent = findViewById(R.id.tvContent);
                        }
                        @Override
                        public void refreshContent(Entry e, com.github.mikephil.charting.highlight.Highlight highlight) {
                            if (tvContent != null) {
                                int idx = Math.round(e.getX());
                                if (idx >= 0 && idx < finalDisplay.size()) tvContent.setText(String.valueOf(finalDisplay.get(idx)));
                            }
                            super.refreshContent(e, highlight);
                        }
                    });

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setDrawGridLines(false);
                    xAxis.setGranularity(1f);
                    xAxis.setTextColor(Color.parseColor("#26A69A"));
                    xAxis.setTextSize(12f);
                    final ArrayList<String> finalLabels = labels;
                    xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int index = Math.round(value);
                            if (index >= 0 && index < finalLabels.size()) return finalLabels.get(index);
                            return "";
                        }
                    });
                    xAxis.setLabelCount(finalLabels.size(), true);

                    lineChart.getAxisLeft().setDrawGridLines(false);
                    lineChart.getAxisRight().setEnabled(false);
                    lineChart.getAxisLeft().setTextColor(Color.parseColor("#26A69A"));
                    lineChart.setExtraOffsets(10, 10, 10, 10);
                    lineChart.invalidate();
                });
            })
            .addOnFailureListener(e -> {
                // En fallo, preparar plotted/display arrays (plotted: evita 0s, display: mantiene 0s)
                final ArrayList<Integer> originalCountsFail = new ArrayList<>(counts);
                final ArrayList<Integer> plottedCountsFail = new ArrayList<>(counts);
                final ArrayList<Integer> displayCountsFail = new ArrayList<>(counts);
                for (int i = 0; i < originalCountsFail.size(); i++) {
                    if (originalCountsFail.get(i) == null || originalCountsFail.get(i) == 0) {
                        plottedCountsFail.set(i, 1);
                        displayCountsFail.set(i, 0);
                    } else {
                        plottedCountsFail.set(i, originalCountsFail.get(i));
                        displayCountsFail.set(i, originalCountsFail.get(i));
                    }
                }

                runOnUiThread(() -> {
                    java.util.List<Entry> entries = new ArrayList<>();
                    for (int i = 0; i < plottedCountsFail.size(); i++) {
                        entries.add(new Entry(i, plottedCountsFail.get(i)));
                    }
                    LineDataSet dataSet = new LineDataSet(entries, "Empresas");
                    dataSet.setColor(Color.parseColor("#26A69A"));
                    dataSet.setCircleColor(Color.parseColor("#26A69A"));
                    dataSet.setLineWidth(2f);
                    dataSet.setCircleRadius(5f);
                    dataSet.setDrawValues(true);
                    dataSet.setValueTextSize(12f);
                    dataSet.setValueTextColor(Color.parseColor("#26A69A"));

                    final ArrayList<Integer> finalDisplayFail = displayCountsFail;
                    dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                        @Override
                        public String getPointLabel(Entry e) {
                            int idx = Math.round(e.getX());
                            if (idx >= 0 && idx < finalDisplayFail.size()) return String.valueOf(finalDisplayFail.get(idx));
                            return super.getPointLabel(e);
                        }
                    });

                    LineData lineData = new LineData(dataSet);
                    lineChart.setData(lineData);
                    lineChart.getDescription().setEnabled(false);
                    lineChart.getLegend().setEnabled(false);

                    // Marker que usa displayCountsFail
                    lineChart.setMarker(new com.github.mikephil.charting.components.MarkerView(this, R.layout.chart_marker_view) {
                        private TextView tvContent;
                        {
                            tvContent = findViewById(R.id.tvContent);
                        }
                        @Override
                        public void refreshContent(Entry e, com.github.mikephil.charting.highlight.Highlight highlight) {
                            if (tvContent != null) {
                                int idx = Math.round(e.getX());
                                if (idx >= 0 && idx < finalDisplayFail.size()) tvContent.setText(String.valueOf(finalDisplayFail.get(idx)));
                            }
                            super.refreshContent(e, highlight);
                        }
                    });

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setDrawGridLines(false);
                    xAxis.setGranularity(1f);
                    xAxis.setTextColor(Color.parseColor("#26A69A"));
                    xAxis.setTextSize(12f);
                    final ArrayList<String> finalLabels = labels;
                    xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int index = Math.round(value);
                            if (index >= 0 && index < finalLabels.size()) return finalLabels.get(index);
                            return "";
                        }
                    });
                    xAxis.setLabelCount(finalLabels.size(), true);
                    lineChart.getAxisLeft().setDrawGridLines(false);
                    lineChart.getAxisRight().setEnabled(false);
                    lineChart.getAxisLeft().setTextColor(Color.parseColor("#26A69A"));
                    lineChart.setExtraOffsets(10, 10, 10, 10);
                    lineChart.invalidate();
                });
            });
    }

    // Cargar guías con state == "deshabilitado" y mostrarlos en el RecyclerView horizontal
    private void loadDisabledGuides() {
        if (db == null) return;
        db.collection("users")
            .whereEqualTo("rol", "Guia")
            .whereEqualTo("state", "deshabilitado")
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(snapshot -> {
                Log.d("MainSuperAdmin", "Guías deshabilitados obtenidos: " + snapshot.size());
                java.util.List<UsuarioGuia> guias = new java.util.ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshot) {
                    String name = doc.getString("name");
                    if (name == null) name = doc.getString("username");
                    String profile = doc.getString("profile_image");
                    // valoracion y ciudad no están disponibles en el usuario, usar valores por defecto
                    UsuarioGuia ug = new UsuarioGuia(name != null ? name : "Guía", "", 0, profile);
                    guias.add(ug);
                }
                if (guiasAdapter != null) {
                    guiasAdapter.setGuias(guias);
                }
            })
            .addOnFailureListener(e -> {
                Log.e("MainSuperAdmin", "Error al obtener guías deshabilitados: " + e.getMessage(), e);
            });
    }

    // Cargar total de usuarios registrados por mes y mostrar en LineChart
    private void setupTotalUsuariosRegistrados() {
        LineChart lineChart = findViewById(R.id.lineChartUsuariosRegistrados);
        if (lineChart == null) return;

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
                        Log.e("TAG", "Error procesando fecha: " + e.getMessage());
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
                }
                
                runOnUiThread(() -> {
                    LineDataSet lineDataSet = new LineDataSet(lineEntries, "Usuarios por mes");
                    lineDataSet.setColor(Color.parseColor("#26A69A"));
                    lineDataSet.setCircleColor(Color.parseColor("#26A69A"));
                    lineDataSet.setLineWidth(2f);
                    lineDataSet.setCircleRadius(5f);
                    lineDataSet.setDrawValues(true);
                    lineDataSet.setValueTextColor(Color.parseColor("#26A69A"));
                    lineDataSet.setValueTextSize(10f);
                    lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    lineDataSet.setDrawFilled(true);
                    lineDataSet.setFillColor(Color.parseColor("#80CBC4"));
                    LineData lineData = new LineData(lineDataSet);
                    lineChart.setData(lineData);
                    lineChart.getDescription().setEnabled(false);
                    lineChart.getLegend().setEnabled(false);

                    // Configurar eje X con los meses
                    final ArrayList<String> finalMesesOrdenados = mesesOrdenados;
                    lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int i = (int) value;
                            return (i >= 0 && i < finalMesesOrdenados.size()) ? finalMesesOrdenados.get(i) : "";
                        }
                    });
                    lineChart.getXAxis().setGranularity(1f);
                    lineChart.getXAxis().setGranularityEnabled(true);
                    lineChart.getXAxis().setDrawGridLines(false);
                    
                    // Configurar ejes Y
                    lineChart.getAxisRight().setEnabled(false);
                    lineChart.getAxisLeft().setDrawGridLines(false);
                    lineChart.getAxisLeft().setAxisMinimum(0f);
                    
                    // Si solo hay datos para un mes
                    if (mesesOrdenados.size() == 1) {
                        lineChart.getXAxis().setAxisMinimum(-0.5f);
                        lineChart.getXAxis().setAxisMaximum(0.5f);
                    }
                    
                    lineChart.invalidate();
                });
            }
        });
    }

    // Cargar distribución de usuarios y mostrar en PieChart
    private void setupUserDistributionChart() {
        PieChart pieChart = findViewById(R.id.pieChartUsuarios);
        if (pieChart == null) return;
        
        db.collection("users")
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int countClientes = 0;
                    int countAdmins = 0;
                    int countGuias = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String rol = document.getString("rol");
                        if (rol != null) {
                            switch (rol) {
                                case "Cliente":
                                    countClientes++;
                                    break;
                                case "Admin":
                                    countAdmins++;
                                    break;
                                case "Guia":
                                    countGuias++;
                                    break;
                                default:
                                    Log.d("RolDebug", "Rol no reconocido: " + rol);
                                    break;
                            }
                        }
                    }

                    ArrayList<PieEntry> pieEntries = new ArrayList<>();
                    if (countClientes > 0) pieEntries.add(new PieEntry(countClientes, "Clientes"));
                    if (countAdmins > 0) pieEntries.add(new PieEntry(countAdmins, "Administrador"));
                    if (countGuias > 0) pieEntries.add(new PieEntry(countGuias, "Guía"));

                    if (pieEntries.isEmpty()) {
                        pieEntries.add(new PieEntry(1f, "Sin datos"));
                    }

                    runOnUiThread(() -> {
                        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
                        ArrayList<Integer> colors = new ArrayList<>();
                        colors.add(Color.parseColor("#FFD54F"));
                        colors.add(Color.parseColor("#80CBC4"));
                        colors.add(Color.parseColor("#B2DFDB"));
                        pieDataSet.setColors(colors);
                        pieDataSet.setValueTextColor(Color.TRANSPARENT);
                        PieData pieData = new PieData(pieDataSet);
                        pieChart.setData(pieData);
                        pieChart.getDescription().setEnabled(false);
                        pieChart.getLegend().setEnabled(true);
                        pieChart.setDrawEntryLabels(false);
                        pieChart.invalidate();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("MainSuperAdmin", "Error al obtener usuarios: " + e.getMessage(), e);
                });
    }

    // Cargar ingresos totales y gráficos de ventas
    private void setupIngresosCharts() {
        LineChart areaChart = findViewById(R.id.areaChartIngresos);
        PieChart pieChart = findViewById(R.id.pieChartVentas);
        TextView tvIngresos = findViewById(R.id.tvTotalIngresosResumen);
        
        if (areaChart == null || pieChart == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tours").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                HashMap<String, Float> costosPorMes = new HashMap<>();
                HashMap<String, Float> costosPorDepartamento = new HashMap<>();
                float sumaTotalCostos = 0f;
                SimpleDateFormat sdf = new SimpleDateFormat("d 'de' MMMM 'de' yyyy, HH:mm:ss a. 'UTC'X", new Locale("es", "ES"));
                
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Object costoObj = doc.get("costo");
                    String departamentoObj = doc.getString("departamento");
                    
                    if (departamentoObj == null) {
                        Object deptObj = doc.get("departamento");
                        if (deptObj != null) {
                            departamentoObj = deptObj.toString();
                        }
                    }
                    
                    float costo = 0f;
                    if (costoObj != null) {
                        try {
                            costo = Float.parseFloat(costoObj.toString());
                        } catch (Exception e) {
                            Log.e("MainSuperAdmin", "Error parseando costo: " + e.getMessage());
                        }
                    }
                    sumaTotalCostos += costo;
                    
                    // Agrupar por mes
                    Date date = null;
                    try {
                        date = doc.getDate("inicioTour");
                    } catch (Exception e) {
                        Log.e("MainSuperAdmin", "Error obteniendo fecha como Timestamp: " + e.getMessage());
                    }
                    
                    if (date == null) {
                        Object inicioTourObj = doc.get("inicioTour");
                        if (inicioTourObj != null) {
                            try {
                                date = sdf.parse(inicioTourObj.toString());
                            } catch (Exception e) {
                                Log.e("MainSuperAdmin", "Error parseando fecha de String: " + e.getMessage());
                            }
                        }
                    }
                    if (date != null) {
                        String mes = new SimpleDateFormat("MMM yyyy", new Locale("es", "ES")).format(date);
                        float actual = costosPorMes.containsKey(mes) ? costosPorMes.get(mes) : 0f;
                        costosPorMes.put(mes, actual + costo);
                    }
                    
                    // Agrupar por departamento
                    String departamento = departamentoObj != null ? departamentoObj : "Sin departamento";
                    float actualDept = costosPorDepartamento.containsKey(departamento) ? costosPorDepartamento.get(departamento) : 0f;
                    costosPorDepartamento.put(departamento, actualDept + costo);
                }
                
                final float finalSumaTotalCostos = sumaTotalCostos;
                runOnUiThread(() -> {
                    // Mostrar la suma total de ingresos
                    if (tvIngresos != null) {
                        tvIngresos.setText(String.format(Locale.US, "%.2f$", finalSumaTotalCostos));
                    }

                    // Gráfico de área: mostrar meses ordenados
                    ArrayList<String> mesesOrdenados = new ArrayList<>(costosPorMes.keySet());
                    mesesOrdenados.sort((a, b) -> {
                        try {
                            Date da = new SimpleDateFormat("MMM yyyy", new Locale("es", "ES")).parse(a);
                            Date db_ = new SimpleDateFormat("MMM yyyy", new Locale("es", "ES")).parse(b);
                            return da.compareTo(db_);
                        } catch (Exception e) { 
                            return 0; 
                        }
                    });
                    ArrayList<Entry> areaEntries = new ArrayList<>();
                    for (int i = 0; i < mesesOrdenados.size(); i++) {
                        String mes = mesesOrdenados.get(i);
                        areaEntries.add(new Entry(i, costosPorMes.get(mes)));
                    }
                    LineDataSet areaDataSet = new LineDataSet(areaEntries, "Ingresos por mes");
                    areaDataSet.setColor(Color.parseColor("#9575CD"));
                    areaDataSet.setCircleColor(Color.parseColor("#9575CD"));
                    areaDataSet.setLineWidth(2f);
                    areaDataSet.setCircleRadius(4f);
                    areaDataSet.setDrawFilled(true);
                    areaDataSet.setFillColor(Color.parseColor("#B39DDB"));
                    areaDataSet.setDrawValues(false);
                    LineData areaData = new LineData(areaDataSet);
                    areaChart.setData(areaData);
                    areaChart.getDescription().setEnabled(false);
                    areaChart.getLegend().setEnabled(false);
                    
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
                    
                    areaChart.getAxisRight().setEnabled(false);
                    areaChart.getAxisLeft().setDrawGridLines(false);
                    areaChart.getAxisLeft().setAxisMinimum(0f);
                    
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
                        colors.add(Color.parseColor("#FFD54F"));
                        colors.add(Color.parseColor("#80CBC4"));
                        colors.add(Color.parseColor("#B2DFDB"));
                        colors.add(Color.parseColor("#F8BBD0"));
                        colors.add(Color.parseColor("#A5D6A7"));
                        colors.add(Color.parseColor("#FFAB91"));
                        pieDataSet.setColors(colors);
                        pieDataSet.setValueTextColor(Color.TRANSPARENT);
                        PieData pieData = new PieData(pieDataSet);
                        pieChart.setData(pieData);
                        pieChart.getDescription().setEnabled(false);
                        pieChart.getLegend().setEnabled(true);
                        pieChart.setDrawEntryLabels(false);
                        pieChart.invalidate();
                    } else {
                        pieChart.clear();
                    }
                });
            }
        });
    }

    // Cargar reservas y gráficos de reservas por empresa
    private void setupReservasCharts() {
        TextView tvTotalResumen = findViewById(R.id.tvTotalReservasResumen);
        LineChart lineChart = findViewById(R.id.lineChartReservas);
        BarChart barChart = findViewById(R.id.barChartEmpresas);
        
        if (lineChart == null || barChart == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tours")
                .whereEqualTo("estado", "terminado")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        // Preparar últimos 6 meses
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

                        // Map para conteo por mes
                        Map<Integer, Integer> counts = new HashMap<>();
                        for (int i = 0; i < 6; i++) counts.put(i, 0);

                        // Map para conteo por empresa
                        Map<String, Integer> empresaCounts = new HashMap<>();

                        double totalCost = 0;
                        int totalTours = 0;

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Object inicioObj = doc.get("inicioTour");
                            
                            Timestamp ts = null;
                            Date d = null;
                            Long costo = doc.getLong("costo");
                            if (costo != null) {
                                totalCost += costo.doubleValue();
                            }

                            totalTours++;

                            // Contar por empresa
                            String empresaId = doc.getString("empresa");
                            if (empresaId != null) {
                                Integer prev = empresaCounts.get(empresaId);
                                empresaCounts.put(empresaId, (prev == null) ? 1 : prev + 1);
                            }

                            // Normalizar inicioTour
                            if (inicioObj instanceof Timestamp) {
                                ts = (Timestamp) inicioObj;
                                d = ts.toDate();
                            } else if (inicioObj instanceof String) {
                                String fechaStr = (String) inicioObj;
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("d 'de' MMMM 'de' yyyy, HH:mm:ss a. 'UTC'X", new Locale("es", "ES"));
                                    d = sdf.parse(fechaStr);
                                } catch (Exception e) {
                                    Log.e("MainSuperAdmin", "Error parseando fecha: " + e.getMessage());
                                }
                            }

                            if (d == null) {
                                continue;
                            }

                            // Encontrar mes correspondiente
                            boolean matched = false;
                            for (int i = 0; i < monthStarts.size(); i++) {
                                Date start = monthStarts.get(i);
                                Calendar cEnd = Calendar.getInstance();
                                cEnd.setTime(start);
                                cEnd.add(Calendar.MONTH, 1);
                                Date end = cEnd.getTime();
                                if (!d.before(start) && d.before(end)) {
                                    int currentCount = counts.get(i);
                                    counts.put(i, currentCount + 1);
                                    matched = true;
                                    break;
                                }
                            }
                        }

                        // Preparar entradas
                        ArrayList<Entry> lineEntries = new ArrayList<>();
                        for (int i = 0; i < 6; i++) {
                            int c = counts.get(i);
                            float v = (float) c;
                            lineEntries.add(new Entry(i, v));
                        }

                        // Actualizar texto resumen
                        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("es", "ES"));
                        nf.setMinimumFractionDigits(2);
                        nf.setMaximumFractionDigits(2);
                        String totalCostStr = nf.format(totalCost);
                        final double finalTotalCost = totalCost;
                        final int finalTotalTours = totalTours;

                        // Guardar empresaCounts para procesar después
                        final Map<String, Integer> finalEmpresaCounts = empresaCounts;

                        runOnUiThread(() -> {
                            if (tvTotalResumen != null) {
                                tvTotalResumen.setText(String.format("%.2f\n%d Reservas", finalTotalCost, finalTotalTours));
                            }

                            // Crear dataset y formateadores
                            LineDataSet lineDataSet = new LineDataSet(lineEntries, "Reservas");
                            lineDataSet.setColor(Color.parseColor("#26A69A"));
                            lineDataSet.setCircleColor(Color.parseColor("#26A69A"));
                            lineDataSet.setLineWidth(2f);
                            lineDataSet.setCircleRadius(5f);
                            lineDataSet.setDrawValues(true);
                            lineDataSet.setDrawCircles(true);
                            lineDataSet.setDrawFilled(false);
                            
                            LineData lineData = new LineData(lineDataSet);
                            lineChart.setData(lineData);
                            lineChart.getDescription().setEnabled(false);
                            lineChart.getLegend().setEnabled(true);
                            lineChart.setTouchEnabled(true);
                            lineChart.setDragEnabled(true);
                            lineChart.setScaleEnabled(true);
                            lineChart.setPinchZoom(true);
                            lineChart.setNoDataText("No hay datos disponibles");

                            // Formatear etiquetas X con nombres de meses
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

                            // Construir gráfico de barras dinámico: reservas por empresa
                            if (finalEmpresaCounts.isEmpty()) {
                                barChart.clear();
                                barChart.getDescription().setEnabled(false);
                                barChart.invalidate();
                            } else {
                                List<String> ids = new ArrayList<>(finalEmpresaCounts.keySet());
                                Map<String, String> empresaNames = new HashMap<>();
                                final int[] remaining = new int[]{ids.size()};
                                for (String id : ids) {
                                    db.collection("empresas").document(id).get()
                                            .addOnSuccessListener(docSnap -> {
                                                String name = null;
                                                if (docSnap.exists()) {
                                                    name = docSnap.getString("nombre");
                                                }
                                                if (name == null) name = id;
                                                empresaNames.put(id, name);
                                                remaining[0]--;
                                                if (remaining[0] == 0) {
                                                    buildBarChart(barChart, ids, finalEmpresaCounts, empresaNames);
                                                }
                                            }).addOnFailureListener(e -> {
                                        empresaNames.put(id, id);
                                        remaining[0]--;
                                        if (remaining[0] == 0) {
                                            buildBarChart(barChart, ids, finalEmpresaCounts, empresaNames);
                                        }
                                    });
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e("MainSuperAdmin", "Error procesando datos para reporte de reservas", e);
                    }
                });
    }

    // Helper para construir el BarChart
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
        barDataSet.setColor(Color.parseColor("#26A69A"));
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
        // Cambiar color del TopAppBar al mismo que otras secciones
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(Color.parseColor("#009688")); // Cambiar de #1DE9B6 a #009688
        }
        // Navegación al hacer click en el botón Usuarios
        findViewById(R.id.btnUsuarios).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainSuperAdminActivity.this, com.example.puriqtours.superadmin.UsuariosActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        // Navegación al hacer click en el botón Reportes
        findViewById(R.id.btnReportes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainSuperAdminActivity.this, com.example.puriqtours.superadmin.ReportesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        // Navegación al hacer click en el botón Logs
        findViewById(R.id.btnLogs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainSuperAdminActivity.this, com.example.puriqtours.superadmin.LogsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        // Inicializar gráfico de líneas de empresas registradas usando datos reales
        LineChart lineChart = findViewById(R.id.lineChartEmpresas);
        if (lineChart != null) {
            // Cargar datos reales desde Firestore y poblar el gráfico
            loadCompaniesMonthlyData(lineChart);
        }

        // Inicializar RecyclerView horizontal de guías (adapter vacío, se llenará desde Firestore)
        RecyclerView rvGuiasHorizontal = findViewById(R.id.rvGuiasHorizontal);
        if (rvGuiasHorizontal != null) {
            rvGuiasHorizontal.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            java.util.List<UsuarioGuia> guias = new java.util.ArrayList<>();
            guiasAdapter = new GuiasHorizontalAdapter(this, guias);
            rvGuiasHorizontal.setAdapter(guiasAdapter);
        }

        // TextViews para mostrar estadísticas
        tvActiveUsersCount = findViewById(R.id.tvActiveUsersCount);
        tvEnabledGuidesCount = findViewById(R.id.tvEnabledGuidesCount);
        tvRegisteredCompaniesCount = findViewById(R.id.tvRegisteredCompaniesCount);
    llToursBarsContainer = findViewById(R.id.llToursBarsContainer);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();
        
        // Verificar conectividad primero
        db.enableNetwork()
            .addOnSuccessListener(aVoid -> {
                Log.d("MainSuperAdmin", "Red habilitada exitosamente");
                // Solo después de confirmar la conectividad, consultamos los usuarios
                setupUsersListener();
                // Cargar guías deshabilitados para la sección de solicitudes
                loadDisabledGuides();
                // Cargar gráfico de distribución de usuarios
                setupUserDistributionChart();
                // Cargar gráfico de total de usuarios registrados
                setupTotalUsuariosRegistrados();
                // Cargar gráficos de ingresos
                setupIngresosCharts();
                // Cargar gráficos de reservas
                setupReservasCharts();
            })
            .addOnFailureListener(e -> {
                Log.e("MainSuperAdmin", "Error al habilitar la red: " + e.getMessage(), e);
                if (tvActiveUsersCount != null) {
                    tvActiveUsersCount.setText("--");
                }
            });
            
        Log.d("MainSuperAdmin", "Iniciando conexión a Firestore...");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remover listener para evitar fugas de memoria cuando la activity no esté visible
        if (usersListener != null) {
            usersListener.remove();
            usersListener = null;
        }
    }

    // Helper para convertir dp a pixels
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}