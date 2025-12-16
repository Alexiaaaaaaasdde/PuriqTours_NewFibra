package com.example.puriqtours.superadmin;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.util.TypedValue;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.Tour;
import com.example.puriqtours.entity.Usuario;
import com.example.puriqtours.login.LoginActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
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
    private TextView tvDisabledGuidesCount;

    private TextView tvActiveUsersCount;
    private TextView tvEnabledGuidesCount;
    private TextView tvRegisteredCompaniesCount;
    private LinearLayout llToursBarsContainer;
    private FirebaseFirestore db;
    private ListenerRegistration usersListener;
    private GuiasHorizontalAdapter guiasAdapter;
    // 🧠 Últimos valores para evitar re-animaciones innecesarias
    private int lastActiveUsers = -1;
    private int lastEnabledGuides = -1;
    private int lastAdminCount = -1;

    private void setupUsersListener() {

        // 🔁 Remover listener previo si existe
        if (usersListener != null) {
            usersListener.remove();
            usersListener = null;
        }

        usersListener = db.collection("users")
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null || snapshot == null) {
                        Log.e("MainSuperAdmin", "Listener error: ", error);
                        return;
                    }

                    int activeUsers = 0;
                    int enabledGuides = 0;
                    int adminCount = 0;

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String rol = doc.getString("rol");
                        String status = doc.getString("status");

                        // 🟢 USUARIOS ACTIVOS (NO SuperAdmin + Activo)
                        if (
                                rol != null &&
                                        !"SuperAdmin".equalsIgnoreCase(rol) &&
                                        "Activo".equalsIgnoreCase(status)
                        ) {
                            activeUsers++;
                        }

                        // 🧭 GUÍAS HABILITADOS
                        if ("Guia".equalsIgnoreCase(rol)
                                && "Activo".equalsIgnoreCase(status)
                                && "Habilitado".equalsIgnoreCase(doc.getString("guide_status"))) {
                            enabledGuides++;
                        }


                        // 🏢 EMPRESAS REGISTRADAS (Admins activos)
                        if ("Admin".equalsIgnoreCase(rol)
                                && "Activo".equalsIgnoreCase(status)) {
                            adminCount++;
                        }
                    }

                    // 🔄 ACTUALIZAR UI EN TIEMPO REAL
                    if (activeUsers != lastActiveUsers) {
                        animateCounter(tvActiveUsersCount, activeUsers);
                        lastActiveUsers = activeUsers;
                    }
                    if (enabledGuides != lastEnabledGuides) {
                        animateCounter(tvEnabledGuidesCount, enabledGuides);
                        lastEnabledGuides = enabledGuides;
                    }

                    if (adminCount != lastAdminCount) {
                        animateCounter(tvRegisteredCompaniesCount, adminCount);
                        lastAdminCount = adminCount;
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
                .whereEqualTo("guide_status", "No habilitado") // <-- CAMPO REAL
                .get()
                .addOnSuccessListener(snapshot -> {

                    int disabledCount = snapshot.size(); // contador real

                    Log.d("MainSuperAdmin", "Guías no habilitados: " + disabledCount);

                    // Mostrar en el TextView
                    if (tvDisabledGuidesCount != null) {
                        animateCounter(tvDisabledGuidesCount, disabledCount);
                    }

                    // Lista para el RecyclerView
                    java.util.List<Usuario> guias = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        Usuario guia = Usuario.fromSnapshot(doc);

                        if (guia.getName() == null)
                            guia.setName("Guía");

                        if (guia.getProfile_image() == null)
                            guia.setProfile_image("");

                        guias.add(guia);
                    }

                    if (guiasAdapter != null) {
                        guiasAdapter.setGuias(guias);
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e("MainSuperAdmin", "Error al cargar guías no habilitados: " + e.getMessage(), e);

                    if (tvDisabledGuidesCount != null) {
                        tvDisabledGuidesCount.setText("--");
                    }
                });
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_home);
        // 🔹 Toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        // 🔹 Firestore
        db = FirebaseFirestore.getInstance();

        // 🔹 Status bar
        getWindow().setStatusBarColor(Color.WHITE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().getInsetsController().setSystemBarsAppearance(
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            );
        }

        // 🔹 Toolbar
        if (toolbar != null) {
            toolbar.setBackgroundColor(Color.parseColor("#009688"));
        }


        // 🔹 Cards (NO CAMBIADAS)
        findViewById(R.id.cardRankings).setOnClickListener(v ->
                startActivity(new Intent(this, TopToursActivity.class))
        );

        findViewById(R.id.cardPreferencias).setOnClickListener(v ->
                startActivity(new Intent(this, IdiomasActivity.class))
        );

        findViewById(R.id.cardActividad).setOnClickListener(v ->
                startActivity(new Intent(this, ActividadSistemaActivity.class))
        );

        findViewById(R.id.cardCrecimiento).setOnClickListener(v ->
                startActivity(new Intent(this, CrecimientoActivity.class))
        );

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationSuperAdmin);

// Marca "Principal" como activo
        bottomNav.setSelectedItemId(R.id.nav_principal);

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_principal) {
                // Ya estás en Home
                return true;
            }
            if (id == R.id.nav_solicitudes) {
                startActivity(new Intent(this, SolicitudesActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }


            if (id == R.id.nav_usuarios) {
                startActivity(new Intent(this, UsuariosActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            if (id == R.id.nav_logs) {
                startActivity(new Intent(this, LogsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });

        // 🔹 TextViews (NO CAMBIADOS)
        tvActiveUsersCount = findViewById(R.id.tvActiveUsersCount);
        tvEnabledGuidesCount = findViewById(R.id.tvEnabledGuidesCount);
        tvRegisteredCompaniesCount = findViewById(R.id.tvRegisteredCompaniesCount);
        tvDisabledGuidesCount = findViewById(R.id.tvDisabledGuidesCount);


    }
    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {

        if (item.getItemId() == R.id.action_logout) {

            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 🔄 Siempre se ejecuta al volver a esta pantalla
        setupUsersListener();
        loadDisabledGuides();
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
    private void animateCounter(TextView textView, int finalValue) {

        ValueAnimator animator = ValueAnimator.ofInt(0, finalValue);
        animator.setDuration(900); // duracion animacion
        animator.setInterpolator(new DecelerateInterpolator()); // animacion suave

        animator.addUpdateListener(valueAnimator -> {
            int animatedValue = (int) valueAnimator.getAnimatedValue();
            textView.setText(String.valueOf(animatedValue));
        });

        animator.start();
    }
    private void cargarTopTours(RecyclerView rv) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("reservas")
                .get()
                .addOnSuccessListener(reservasSnap -> {

                    Map<String, Integer> conteo = new HashMap<>();

                    for (QueryDocumentSnapshot r : reservasSnap) {
                        String idTour = r.getString("idTour");
                        if (idTour != null) {
                            conteo.put(idTour, conteo.getOrDefault(idTour, 0) + 1);
                        }
                    }

                    obtenerTours(conteo, rv);
                });
    }
    private void obtenerTours(Map<String,Integer> conteo, RecyclerView rv) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tours")
                .get()
                .addOnSuccessListener(toursSnap -> {

                    // Aquí guardamos tours completos SIN crear clases nuevas
                    ArrayList<Tour> lista = new ArrayList<>();

                    for (QueryDocumentSnapshot t : toursSnap) {

                        String id = t.getId();
                        if (!conteo.containsKey(id)) continue;

                        Tour tour = t.toObject(Tour.class);
                        tour.setIdTour(id); // porque Firestore no lo asigna solo

                        lista.add(tour);
                    }

                    // Ordenamos de mayor a menor reservas
                    lista.sort((a, b) ->
                            conteo.get(b.getIdTour()) - conteo.get(a.getIdTour())
                    );

                    // Top 10
                    if (lista.size() > 10)
                        lista.subList(10, lista.size()).clear();

                    rv.setAdapter(new TopToursAdapter(lista, conteo));
                });
    }

    public class TopToursAdapter extends RecyclerView.Adapter<TopToursAdapter.ViewHolder> {

        private List<Tour> lista;
        private Map<String, Integer> conteo;
        private int maxValor;

        public TopToursAdapter(List<Tour> lista, Map<String,Integer> conteo) {
            this.lista = lista;
            this.conteo = conteo;

            maxValor = 1;
            for (Tour t : lista)
                if (conteo.get(t.getIdTour()) > maxValor)
                    maxValor = conteo.get(t.getIdTour());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_tour_ranking_sensortower, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            Tour tour = lista.get(pos);
            int reservas = conteo.get(tour.getIdTour());

            h.tvTitle.setText(tour.getTitle());
            h.tvCount.setText(String.valueOf(reservas));

            float porcentaje = (float) reservas / maxValor * 100f;
            h.progress.setProgress((int) porcentaje);

            Glide.with(h.itemView.getContext())
                    .load(tour.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(h.imgTour);
        }

        @Override
        public int getItemCount() {
            return lista.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvCount;
            ImageView imgTour;
            ProgressBar progress;

            ViewHolder(View item) {
                super(item);
                tvTitle = item.findViewById(R.id.tvTitle);
                tvCount = item.findViewById(R.id.tvCount);
                imgTour = item.findViewById(R.id.imgTour);
                progress = item.findViewById(R.id.progressBar);
            }
        }
    }


}