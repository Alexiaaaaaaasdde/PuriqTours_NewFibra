package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainGuiaActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_guia);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.topAppBar);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // 🔹 Listener del Drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            Fragment fragment = null;
            String title = getString(R.string.app_name);

            if (id == R.id.nav_perfil) {
                fragment = new ProfileFragment();
                title = "Perfil";
            } else if (id == R.id.nav_tours) {
                fragment = new ToursFragment();
                title = "Tours";
            } else if (id == R.id.nav_historial) {
                fragment = new HomeFragment();
                title = "Historial";
            } else if (id == R.id.nav_logout) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                toolbar.setTitle(title);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // 🔹 Cargar fragment por defecto
        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_tours);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ToursFragment())
                    .commit();
            toolbar.setTitle("Puriq Tours");
        }

        // Fragment inicial
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_home){
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_tours) {
                selectedFragment = new ToursFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }
}
