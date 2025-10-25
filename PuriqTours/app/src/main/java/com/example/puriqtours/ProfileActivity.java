package com.example.puriqtours;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ProfileActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private static final int PICK_IMAGE_REQUEST = 1;

    private ShapeableImageView profileImage;
    private EditText etNombre, etApellido, etCorreo, etFechaNacimiento,
            etNumeroDocumento, etNumeroTelefonico, etTipoDocumento, etDireccion,
            etIdioma, etActividades;

    private Button btnUpdate, btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_admin);

        // ✅ Ajuste automático con los bordes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Drawer (menú lateral)
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_perfil) {
                // Ya estás aquí
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
            } else if (id == R.id.nav_logout) {
                LocalAuth localAuth = new LocalAuth(this);
                localAuth.setLogged(false); // 🔹 solo cerrar sesión, sin borrar info

                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // 🔹 Bottom navigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_perfil);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_perfil) return true;
            if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        // 🔹 Referencias
        profileImage = findViewById(R.id.profileImage);
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etCorreo = findViewById(R.id.etCorreo);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etNumeroDocumento = findViewById(R.id.etNumeroDocumento);
        etNumeroTelefonico = findViewById(R.id.etNumeroTelefonico);
        etTipoDocumento = findViewById(R.id.etTipoDocumento);
        etDireccion = findViewById(R.id.etDireccion);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnSave = findViewById(R.id.btnSave);

        // 🔹 Nuevos campos dinámicos para idioma y actividades
        etIdioma = new EditText(this);
        etActividades = new EditText(this);

        // Cargar datos del usuario
        LocalAuth localAuth = new LocalAuth(this);
        etNombre.setText(localAuth.getName());
        etApellido.setText(localAuth.getLastname());
        etCorreo.setText(localAuth.getEmail());
        etFechaNacimiento.setText(localAuth.getBirthdate());
        etNumeroDocumento.setText(localAuth.getDocument());
        etNumeroTelefonico.setText(localAuth.getPhone());
        etDireccion.setText(localAuth.getAddress());
        etTipoDocumento.setText(localAuth.getDocType());

        // 🔹 Mostrar idioma y actividades en consola (por ahora)
        android.util.Log.d("PROFILE_DATA", "Idioma: " + localAuth.getLanguage());
        android.util.Log.d("PROFILE_DATA", "Actividades: " + localAuth.getActivities());

        // Mostrar imagen de perfil
        if (localAuth.getPhotoUri() != null && !localAuth.getPhotoUri().isEmpty()) {
            profileImage.setImageURI(Uri.parse(localAuth.getPhotoUri()));
        }

        btnSave.setVisibility(View.GONE);

        // 🔹 Botón para editar teléfono o foto
        btnUpdate.setOnClickListener(v -> {
            etNumeroTelefonico.setEnabled(true);
            profileImage.setClickable(true);

            Toast.makeText(this, "Puedes cambiar tu número o foto", Toast.LENGTH_SHORT).show();

            profileImage.setOnClickListener(img -> {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK);
                pickPhoto.setType("image/*");
                startActivityForResult(pickPhoto, PICK_IMAGE_REQUEST);
            });

            btnUpdate.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
        });

        // 🔹 Guardar cambios locales
        btnSave.setOnClickListener(v -> {
            etNumeroTelefonico.setEnabled(false);
            profileImage.setClickable(false);

            // Guardar los nuevos datos
            localAuth.saveUser(
                    localAuth.getName(),
                    localAuth.getLastname(),
                    localAuth.getEmail(),
                    localAuth.getPassword(),
                    localAuth.getBirthdate(),
                    etNumeroDocumento.getText().toString(),
                    etNumeroTelefonico.getText().toString(),
                    etDireccion.getText().toString(),
                    etTipoDocumento.getText().toString(),
                    localAuth.getLanguage(),
                    localAuth.getActivities(),
                    localAuth.getPhotoUri()
            );

            // Mostrar éxito
            Dialog dialog = new Dialog(ProfileActivity.this);
            dialog.setContentView(R.layout.dialog_success);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setCancelable(false);
            dialog.findViewById(R.id.btnClose).setOnClickListener(closeView -> dialog.dismiss());
            dialog.show();

            btnSave.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.VISIBLE);
        });
    }

    // ✅ Selección de imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            profileImage.setImageURI(selectedImage);

            LocalAuth localAuth = new LocalAuth(this);
            localAuth.saveUser(
                    localAuth.getName(),
                    localAuth.getLastname(),
                    localAuth.getEmail(),
                    localAuth.getPassword(),
                    localAuth.getBirthdate(),
                    localAuth.getDocument(),
                    localAuth.getPhone(),
                    localAuth.getAddress(),
                    localAuth.getDocType(),
                    localAuth.getLanguage(),
                    localAuth.getActivities(),
                    selectedImage.toString() // ✅ guardar nueva foto
            );
        }
    }
}
