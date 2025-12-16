package com.example.puriqtours.guia;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.puriqtours.SplashActivity;
import com.example.puriqtours.entity.Usuario;
import com.example.puriqtours.helper.UserSessionManager;
import com.example.puriqtours.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class MainGuiaActivity extends AppCompatActivity implements PerfilActualizadoListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private ShapeableImageView profileicon;
    private FirebaseAuth auth;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_guia);

        auth = FirebaseAuth.getInstance();
        sessionManager = new UserSessionManager(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.topAppBar);
        profileicon = findViewById(R.id.profileIcon);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        // 🔹 Cargar imagen de perfil en toolbar
        cargarFotoToolbar();

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // 🔹 MENÚ LATERAL
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            Fragment fragment = null;
            String title = getString(R.string.app_name);

            if (id == R.id.nav_logout) {
                auth.signOut();
                sessionManager.clearSession();
                startActivity(new Intent(this, SplashActivity.class));
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

        // 🔹 Fragment inicial
        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_tours);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ToursFragment())
                    .commit();
            toolbar.setTitle("Puriq Tours");
        }

        // 🔹 Segundo fragment inicial (tu código original lo tenía, lo mantengo)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // 🔹 Bottom Navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
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

    // -----------------------------------------------------------------------------------
    // 🔹 Metodo para recargar la foto del usuario en la toolbar
    // -----------------------------------------------------------------------------------
    private void cargarFotoToolbar() {
        Usuario user = sessionManager.getUser();
        if (user == null) return;

        String imageUrl = user.getProfile_image();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.profile_image_dummy)
                    .error(R.drawable.profile_image_dummy)
                    .into(profileicon);
        }
    }

    // -----------------------------------------------------------------------------------
    // 🔹 Metodo que se llama automáticamente cuando el perfil se actualiza
    // -----------------------------------------------------------------------------------
    @Override
    public void onPerfilActualizado() {
        cargarFotoToolbar();  // ← Recarga la foto en la toolbar
    }
}
