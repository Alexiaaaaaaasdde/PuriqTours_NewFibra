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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_logs);

        RecyclerView rvLogs = findViewById(R.id.rvLogs);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        logsAdapter = new LogsAdapter(this, getLogsForType("General"));
        rvLogs.setAdapter(logsAdapter);

        findViewById(R.id.btnGeneral).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logsAdapter.setLogs(getLogsForType("General"));
            }
        });
        findViewById(R.id.btnGuias).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logsAdapter.setLogs(getLogsForType("Guía"));
            }
        });
        findViewById(R.id.btnEmpresas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logsAdapter.setLogs(getLogsForType("Empresa"));
            }
        });
        findViewById(R.id.btnPagos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logsAdapter.setLogs(getLogsForType("Pago"));
            }
        });
        findViewById(R.id.btnUsuarios).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logsAdapter.setLogs(getLogsForType("Usuario"));
            }
        });
        findViewById(R.id.btnPrincipal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogsActivity.this, MainActivity.class);
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


