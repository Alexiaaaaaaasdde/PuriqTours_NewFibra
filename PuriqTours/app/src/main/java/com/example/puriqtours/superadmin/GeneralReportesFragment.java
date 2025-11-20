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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GeneralReportesFragment extends Fragment {
    private TextView tvVentasTotal, tvEmpresasActivas, tvReservasMes;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_general_reportes, container, false);

        tvVentasTotal = view.findViewById(R.id.tvVentasTotal);
        tvEmpresasActivas = view.findViewById(R.id.tvEmpresasActivas);
        tvReservasMes = view.findViewById(R.id.tvReservasMes);
        db = FirebaseFirestore.getInstance();

        loadCardData();
        setupPieChart(view);

        return view;
    }

    private void loadCardData() {
        // 1. Suma de costos de todos los tours terminados
        db.collection("tours")
                .whereEqualTo("estado", "terminado")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalVentas = 0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long costo = document.getLong("costo");
                        if (costo != null) {
                            totalVentas += costo.doubleValue();
                        }
                    }
                    tvVentasTotal.setText(String.format("%.0f", totalVentas));
                });

        // 2. Contar número de empresas
        db.collection("empresas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int countEmpresas = queryDocumentSnapshots.size();
                    tvEmpresasActivas.setText(String.valueOf(countEmpresas));
                });

        // 3. Contar tours del último mes
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Date unMesAtras = cal.getTime();

        Calendar calFin = Calendar.getInstance();
        Log.d("ReservaMes", "Fecha inicio: " + unMesAtras);
        Log.d("ReservaMes", "Fecha fin: " + calFin.getTime());
        
        db.collection("tours")
                .whereEqualTo("estado", "terminado")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int countToursUltimoMes = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Timestamp timestamp = doc.getTimestamp("inicioTour");
                            if (timestamp != null) {
                                Date fechaTour = timestamp.toDate();
                                if (fechaTour.after(unMesAtras) && 
                                    fechaTour.before(calFin.getTime())) {
                                    countToursUltimoMes++;
                                }
                                Log.d("ReservaMes", "Tour ID: " + doc.getId() + 
                                      " Fecha: " + fechaTour +
                                      " Estado: " + doc.getString("estado"));
                            }
                        } catch (Exception e) {
                            Log.e("ReservaMes", "Error procesando tour: " + doc.getId(), e);
                        }
                    }
                    Log.d("ReservaMes", "Número de tours encontrados: " + countToursUltimoMes);
                    tvReservasMes.setText(String.valueOf(countToursUltimoMes));
                });
    }

    private void setupPieChart(View view) {
        PieChart pieChart = view.findViewById(R.id.pieChartUsuarios);
        db.collection("users")
                .get()
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
                });
    }


}