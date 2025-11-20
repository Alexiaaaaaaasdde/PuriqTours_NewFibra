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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Usuario;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;

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

    // Cargar guías con state == "deshabilitado" y mostrarlos en el RecyclerView horizontal
    private void loadDisabledGuides() {
        if (db == null) return;

        db.collection("users")
                .whereEqualTo("rol", "Guia")
                .whereEqualTo("state", "deshabilitado")
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(snapshot -> {

                    Log.d("MainSuperAdmin", "Guías deshabilitados obtenidos: " + snapshot.size());

                    java.util.List<Usuario> guias = new java.util.ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        Usuario guia = Usuario.fromSnapshot(doc);

                        // fallback de nombre
                        if (guia.getName() == null)
                            guia.setName("Guía");

                        // fallback de imagen
                        if (guia.getProfile_image() == null)
                            guia.setProfile_image("");

                        guias.add(guia);
                    }

                    if (guiasAdapter != null) {
                        guiasAdapter.setGuias(guias);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainSuperAdmin", "Error al obtener guías deshabilitados: " + e.getMessage(), e);
                });
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
            rvGuiasHorizontal.setLayoutManager(
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            );

            // Cambia UsuarioGuia → Usuario
            java.util.List<Usuario> guias = new java.util.ArrayList<>();

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