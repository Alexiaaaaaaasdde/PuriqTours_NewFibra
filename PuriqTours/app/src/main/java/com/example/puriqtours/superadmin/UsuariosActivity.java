package com.example.puriqtours.superadmin;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.puriqtours.R;
public class UsuariosActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_usuarios);
        findViewById(R.id.btnPrincipal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsuariosActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        findViewById(R.id.btnReportes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsuariosActivity.this, ReportesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        findViewById(R.id.btnLogs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsuariosActivity.this, LogsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        findViewById(R.id.btnClientes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new UsuariosClientesFragment());
                highlightFilter(R.id.btnClientes);
            }
        });
        findViewById(R.id.btnGuias).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new UsuariosGuiasFragment());
                highlightFilter(R.id.btnGuias);
            }
        });
        findViewById(R.id.btnAdministradores).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new UsuariosAdministradoresFragment());
                highlightFilter(R.id.btnAdministradores);
            }
        });
        findViewById(R.id.btnTodos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new UsuariosTodosFragment());
                highlightFilter(R.id.btnTodos);
            }
        });
        // Mostrar Guías por defecto (como el layout actual)
        showFragment(new UsuariosGuiasFragment());
        highlightFilter(R.id.btnGuias);
    }
    private void showFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.usuariosContentContainer, fragment);
        ft.commit();
    }

    private void highlightFilter(int selectedId) {
        int[] ids = {R.id.btnClientes, R.id.btnGuias, R.id.btnAdministradores, R.id.btnTodos};
        for (int id : ids) {
            View btn = findViewById(id);
            if (btn != null) {
                btn.setBackgroundColor(getResources().getColor(id == selectedId ? R.color.teal_50 : android.R.color.white));
            }
        }
    }
}
