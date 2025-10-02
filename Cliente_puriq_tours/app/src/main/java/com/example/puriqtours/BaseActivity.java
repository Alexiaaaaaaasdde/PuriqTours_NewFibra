package com.example.puriqtours;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // 👇 Llamar esto en cada Activity hija después de setContentView()
    protected void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.topAppBar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_perfil) {
                    startActivity(new Intent(this, ProfileActivity.class));
                } else if (id == R.id.nav_tours) {
                    startActivity(new Intent(this, ToursActivity.class));
                } else if (id == R.id.nav_historial) {
                    startActivity(new Intent(this, HistorialActivity.class));
                } else if (id == R.id.nav_logout) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    // 👇 Método auxiliar que las Activities principales llaman si quieren ☰
    protected void enableDrawerIcon() {
        if (toolbar != null && drawerLayout != null) {
            toolbar.setNavigationIcon(R.drawable.ic_menu);
            toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }
    }
}
