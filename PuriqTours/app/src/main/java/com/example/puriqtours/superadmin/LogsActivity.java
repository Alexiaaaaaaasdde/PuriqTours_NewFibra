package com.example.puriqtours.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.puriqtours.R;

public class LogsActivity extends AppCompatActivity {
    private LogsAdapter logsAdapter;
    private RecyclerView rvLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_logs);

        // BottomBar navegación universal
        findViewById(R.id.btnPrincipal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        findViewById(R.id.btnUsuarios).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogsActivity.this, UsuariosActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        findViewById(R.id.btnReportes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogsActivity.this, ReportesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        findViewById(R.id.btnLogs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ya estás en Logs
            }
        });

        rvLogs = findViewById(R.id.rvLogs);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        logsAdapter = new LogsAdapter(this, getLogsForType("General")); // <-- restaurado, pasa contexto
        rvLogs.setAdapter(logsAdapter);

        // Filtros y sombreado dinámico
        findViewById(R.id.btnGeneral).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logsAdapter.setLogs(getLogsForType("General"));
                highlightFilter(R.id.btnGeneral);
            }
        });
        findViewById(R.id.btnUsuariosFiltro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logsAdapter.setLogs(getLogsForType("Usuarios"));
                highlightFilter(R.id.btnUsuariosFiltro);
            }
        });
        findViewById(R.id.btnPagos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logsAdapter.setLogs(getLogsForType("Pagos"));
                highlightFilter(R.id.btnPagos);
            }
        });
        findViewById(R.id.btnGuias).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logsAdapter.setLogs(getLogsForType("Guías"));
                highlightFilter(R.id.btnGuias);
            }
        });
        findViewById(R.id.btnEmpresas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logsAdapter.setLogs(getLogsForType("Empresas"));
                highlightFilter(R.id.btnEmpresas);
            }
        });

        // Mostrar General por defecto
        highlightFilter(R.id.btnGeneral);
    }

    private void highlightFilter(int selectedId) {
        int[] ids = {R.id.btnGeneral, R.id.btnUsuariosFiltro, R.id.btnPagos, R.id.btnGuias, R.id.btnEmpresas};
        for (int id : ids) {
            View btn = findViewById(id);
            if (btn != null) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(id == selectedId ? R.color.teal_50 : android.R.color.white)
                ));
            }
        }
    }

    private java.util.List<LogItem> getLogsForType(String tipo) {
        java.util.List<LogItem> logs = new java.util.ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String fecha = "04/09/2025 14:" + String.format("%02d", i) + ":00";
            String descripcion = "El " + tipo.toLowerCase() + " " + tipo + i + " realizó una acción de ejemplo.";
            logs.add(new LogItem(tipo, fecha, descripcion));
        }
        return logs;
    }
}



