package com.example.puriqtours;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    // 👇 NUEVO: variables para el Drawer
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_activity);

        // ✅ Ajustar paddings con insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Drawer references
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // 👉 abrir menú lateral con ☰
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_perfil) {
                // ya estás en perfil
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
            } else if (id == R.id.nav_logout) {
                // 🔹 Sin Firebase: solo volver al login
                Intent intent = new Intent(this, MainActivity.class); // o LoginActivity
                startActivity(intent);
                finish(); // cerrar ProfileActivity
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


        // 🔹 BottomNavigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_perfil);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_perfil) {
                return true;
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
                overridePendingTransition(0,0);
                return true;
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            return false;
        });

        // 🔹 Referencias a vistas
        Button btnUpdate = findViewById(R.id.btnUpdate);
        Button btnSave = findViewById(R.id.btnSave);
        ShapeableImageView profileImage = findViewById(R.id.profileImage);
        EditText etNumeroTelefonico = findViewById(R.id.etNumeroTelefonico);

        EditText etNombre = findViewById(R.id.etNombre);
        EditText etApellido = findViewById(R.id.etApellido);
        EditText etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        EditText etNumeroDocumento = findViewById(R.id.etNumeroDocumento);
        EditText etTipoDocumento = findViewById(R.id.etTipoDocumento);
        EditText etDireccion = findViewById(R.id.etDireccion);
        EditText etCorreo = findViewById(R.id.etCorreo);

        btnSave.setVisibility(View.GONE);

        btnUpdate.setOnClickListener(v -> {
            etNumeroTelefonico.setEnabled(true);
            etNumeroTelefonico.requestFocus();
            profileImage.setClickable(true);
            Toast.makeText(this, "Ahora puedes editar el teléfono o la foto", Toast.LENGTH_SHORT).show();

            btnUpdate.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
        });

        btnSave.setOnClickListener(v -> {
            etNumeroTelefonico.setEnabled(false);
            profileImage.setClickable(false);

            Dialog dialog = new Dialog(ProfileActivity.this);
            dialog.setContentView(R.layout.dialog_success);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setCancelable(false);

            Button btnClose = dialog.findViewById(R.id.btnClose);
            btnClose.setOnClickListener(closeView -> dialog.dismiss());

            dialog.show();

            btnSave.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.VISIBLE);
        });
    }




}

