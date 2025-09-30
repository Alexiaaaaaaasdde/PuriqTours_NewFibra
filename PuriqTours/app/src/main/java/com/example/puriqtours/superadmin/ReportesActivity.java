package com.example.puriqtours.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.puriqtours.R;
import java.util.ArrayList;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ReportesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_reportes);

        // BottomBar navegación universal
        findViewById(R.id.btnPrincipal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportesActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        findViewById(R.id.btnUsuarios).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportesActivity.this, UsuariosActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        findViewById(R.id.btnReportes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ya estás en Reportes
            }
        });
        findViewById(R.id.btnLogs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportesActivity.this, LogsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // Filtros y cambio de fragment
        findViewById(R.id.btnGeneral).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new GeneralReportesFragment());
                highlightFilter(R.id.btnGeneral);
            }
        });
        findViewById(R.id.btnReservas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new ReservasReportesFragment());
                highlightFilter(R.id.btnReservas);
            }
        });
        findViewById(R.id.btnIngresos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new IngresosReportesFragment());
                highlightFilter(R.id.btnIngresos);
            }
        });
        findViewById(R.id.btnUsuariosFiltro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new UsuariosReportesFragment());
                highlightFilter(R.id.btnUsuariosFiltro);
            }
        });
        findViewById(R.id.btnGuias).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new GuiasReportesFragment());
                highlightFilter(R.id.btnGuias);
            }
        });
        findViewById(R.id.btnEmpresas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new EmpresasReportesFragment());
                highlightFilter(R.id.btnEmpresas);
            }
        });
        // Mostrar General por defecto
        showFragment(new GeneralReportesFragment());
        highlightFilter(R.id.btnGeneral);
    }
    private void showFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.reportesContentContainer, fragment);
        ft.commit();
    }
    private void highlightFilter(int selectedId) {
        int[] ids = {R.id.btnGeneral, R.id.btnReservas, R.id.btnIngresos, R.id.btnUsuariosFiltro, R.id.btnGuias, R.id.btnEmpresas};
        for (int id : ids) {
            View btn = findViewById(id);
            if (btn != null) {
                btn.setBackgroundColor(getResources().getColor(id == selectedId ? R.color.teal_50 : android.R.color.white));
            }
        }
    }
}
