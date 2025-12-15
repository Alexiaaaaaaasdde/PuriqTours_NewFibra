package com.example.puriqtours.superadmin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SolicitudesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitudes);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationSuperAdmin);
        bottomNav.setSelectedItemId(R.id.nav_solicitudes);
    }
}
