package com.example.puriqtours.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.puriqtours.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

public class UsuariosActivity extends AppCompatActivity {

    TextInputEditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_usuarios);
        findViewById(R.id.btnCrearUsuario).setOnClickListener(v -> {
            CrearUsuarioDialog dialog = new CrearUsuarioDialog();
            dialog.show(getSupportFragmentManager(), "CrearUsuarioDialog");
        });

        // ---------- 🔍 BUSCADOR ----------
        etSearch = findViewById(R.id.etSearchUsuarios);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltro(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationSuperAdmin);

        if (bottomNav != null) {

            // Estamos en Rankings
            bottomNav.setSelectedItemId(R.id.nav_usuarios);

            bottomNav.setOnItemSelectedListener(item -> {

                int id = item.getItemId();

                if (id == R.id.nav_principal) {
                    startActivity(new Intent(this, MainSuperAdminActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                if (id == R.id.nav_usuarios) {
                    return true;
                }

                if (id == R.id.nav_logs) {
                    startActivity(new Intent(this, LogsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            });
        }


        // ---------- BOTONES DE FILTRO ----------
        findViewById(R.id.btnClientes).setOnClickListener(v -> {
            showFragment(new UsuariosClientesFragment());
            highlightFilter(R.id.btnClientes);
        });

        findViewById(R.id.btnGuias).setOnClickListener(v -> {
            showFragment(new UsuariosGuiasFragment());
            highlightFilter(R.id.btnGuias);
        });

        findViewById(R.id.btnAdministradores).setOnClickListener(v -> {
            showFragment(new UsuariosAdministradoresFragment());
            highlightFilter(R.id.btnAdministradores);
        });

        findViewById(R.id.btnBloqueados).setOnClickListener(v -> {
            showFragment(new UsuariosBaneadosFragment());
            highlightFilter(R.id.btnBloqueados);
        });

        findViewById(R.id.btnTodos).setOnClickListener(v -> {
            showFragment(new UsuariosTodosFragment());
            highlightFilter(R.id.btnTodos);
        });


        // ---------- ORDENAR ----------
        findViewById(R.id.ordenarLayout).setOnClickListener(v -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.usuariosContentContainer);

            if (current instanceof UsuariosClientesFragment) {
                ((UsuariosClientesFragment) current).sortByName();
            } else if (current instanceof UsuariosGuiasFragment) {
                ((UsuariosGuiasFragment) current).sortByName();
            } else if (current instanceof UsuariosAdministradoresFragment) {
                ((UsuariosAdministradoresFragment) current).sortByName();
            } else if (current instanceof UsuariosTodosFragment) {
                ((UsuariosTodosFragment) current).sortByName();
            }
        });

        // Fragment por defecto
        showFragment(new UsuariosGuiasFragment());
        highlightFilter(R.id.btnGuias);
    }


    // =====================================================
    // FUNCIONES
    // =====================================================

    private void showFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.usuariosContentContainer, fragment);
        ft.commit();

        // Aplicar el filtro actual al cambiar de fragment
        if (etSearch != null) {
            aplicarFiltro(etSearch.getText().toString());
        }
    }

    private void aplicarFiltro(String texto) {
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.usuariosContentContainer);

        if (frag instanceof UsuariosClientesFragment) {
            ((UsuariosClientesFragment) frag).filtrarTexto(texto);
        }
        if (frag instanceof UsuariosGuiasFragment) {
            ((UsuariosGuiasFragment) frag).filtrarTexto(texto);
        }
        if (frag instanceof UsuariosAdministradoresFragment) {
            ((UsuariosAdministradoresFragment) frag).filtrarTexto(texto);
        }
        if (frag instanceof UsuariosBaneadosFragment) {
            ((UsuariosBaneadosFragment) frag).filtrarTexto(texto);
        }
        if (frag instanceof UsuariosTodosFragment) {
            ((UsuariosTodosFragment) frag).filtrarTexto(texto);
        }
    }

    private void highlightFilter(int selectedId) {
        int[] ids = {R.id.btnClientes, R.id.btnGuias, R.id.btnAdministradores, R.id.btnBloqueados, R.id.btnTodos};
        for (int id : ids) {
            View btn = findViewById(id);
            if (btn != null) {
                btn.setBackgroundColor(getResources().getColor(id == selectedId ? R.color.teal_50 : android.R.color.white));
            }
        }
    }
}
