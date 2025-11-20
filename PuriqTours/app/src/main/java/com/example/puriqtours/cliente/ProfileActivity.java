package com.example.puriqtours.cliente;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import com.example.puriqtours.R;
import com.example.puriqtours.login.LoginLegacyActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private static final int PICK_IMAGE_REQUEST = 1;

    private ShapeableImageView profileImage;
    private EditText etNombre, etApellido, etCorreo, etFechaNacimiento, etNumeroDocumento,
            etNumeroTelefonico, etTipoDocumento, etDireccion, etIdioma, etActividades;

    private Button btnUpdate, btnSave;

    FirebaseFirestore db;
    FirebaseAuth auth;
    String uid;
    String photoUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate iniciado");

        // ⭐ VALIDACIÓN CRÍTICA - DEBE SER LO PRIMERO ⭐
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "Usuario no autenticado, redirigiendo al login");
            redirigirAlLogin();
            return; // ⚠️ CRÍTICO: Detener ejecución aquí
        }

        uid = currentUser.getUid();
        Log.d(TAG, "Usuario autenticado con UID: " + uid);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();

        // Ajuste de bordes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarVistas();
        configurarToolbarYNavegacion();
        cargarDatosUsuario();

        btnSave.setVisibility(android.view.View.GONE);

        btnUpdate.setOnClickListener(v -> habilitarEdicion());
        btnSave.setOnClickListener(v -> guardarCambios());
    }

    private void inicializarVistas() {
        try {
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);

            profileImage = findViewById(R.id.profileImage);
            etNombre = findViewById(R.id.etNombre);
            etApellido = findViewById(R.id.etApellido);
            etCorreo = findViewById(R.id.etCorreo);
            etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
            etNumeroDocumento = findViewById(R.id.etNumeroDocumento);
            etNumeroTelefonico = findViewById(R.id.etNumeroTelefonico);
            etTipoDocumento = findViewById(R.id.etTipoDocumento);
            etDireccion = findViewById(R.id.etDireccion);
            etIdioma = findViewById(R.id.etIdioma);
            etActividades = findViewById(R.id.etActividades);

            btnUpdate = findViewById(R.id.btnUpdate);
            btnSave = findViewById(R.id.btnSave);

            Log.d(TAG, "Vistas inicializadas correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar vistas: " + e.getMessage());
            Toast.makeText(this, "Error al cargar la interfaz", Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarToolbarYNavegacion() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_perfil) {
                // Ya estamos aquí
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
            } else if (id == R.id.nav_logout) {
                cerrarSesion();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_perfil);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_perfil) return true;

            if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    private void cargarDatosUsuario() {
        Log.d(TAG, "Cargando datos del usuario...");

        // ⭐ Verificación adicional antes de cargar
        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "UID es null o vacío");
            redirigirAlLogin();
            return;
        }

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Log.d(TAG, "Documento encontrado");

                        // ⭐ Usar valores predeterminados si los campos son null
                        etNombre.setText(doc.getString("name") != null ? doc.getString("name") : "");
                        etApellido.setText(doc.getString("last_name") != null ? doc.getString("last_name") : "");
                        etCorreo.setText(doc.getString("email") != null ? doc.getString("email") : "");
                        etFechaNacimiento.setText(doc.getString("birthdate") != null ? doc.getString("birthdate") : "");
                        etNumeroDocumento.setText(doc.getString("document") != null ? doc.getString("document") : "");
                        etNumeroTelefonico.setText(doc.getString("phone") != null ? doc.getString("phone") : "");
                        etDireccion.setText(doc.getString("address") != null ? doc.getString("address") : "");
                        etTipoDocumento.setText(doc.getString("doc_type") != null ? doc.getString("doc_type") : "");
                        etIdioma.setText(doc.getString("language") != null ? doc.getString("language") : "");

                        // ⭐ Manejo especial para actividades (puede ser una lista)
                        Object activitiesObj = doc.get("activities");
                        if (activitiesObj != null) {
                            etActividades.setText(activitiesObj.toString());
                        } else {
                            etActividades.setText("");
                        }

                        photoUrl = doc.getString("profile_image");
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Picasso.get()
                                    .load(photoUrl)
                                    .error(R.drawable.imagen_perfil)
                                    .into(profileImage);
                        }

                        Log.d(TAG, "Datos cargados exitosamente");
                    } else {
                        Log.w(TAG, "Documento no existe en Firestore");
                        Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void habilitarEdicion() {
        etNumeroTelefonico.setEnabled(true);
        profileImage.setClickable(true);

        Toast.makeText(this, "Puedes cambiar tu número o foto", Toast.LENGTH_SHORT).show();

        profileImage.setOnClickListener(img -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK);
            pickPhoto.setType("image/*");
            startActivityForResult(pickPhoto, PICK_IMAGE_REQUEST);
        });

        btnUpdate.setVisibility(android.view.View.GONE);
        btnSave.setVisibility(android.view.View.VISIBLE);
    }

    private void guardarCambios() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("phone", etNumeroTelefonico.getText().toString());
        updates.put("profile_image", photoUrl);

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Datos actualizados exitosamente");
                    mostrarDialogo();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar: " + e.getMessage());
                    Toast.makeText(this, "Error al guardar cambios", Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDialogo() {
        Dialog dialog = new Dialog(ProfileActivity.this);
        dialog.setContentView(R.layout.dialog_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.findViewById(R.id.btnClose).setOnClickListener(closeView -> dialog.dismiss());
        dialog.show();

        btnSave.setVisibility(android.view.View.GONE);
        btnUpdate.setVisibility(android.view.View.VISIBLE);
        etNumeroTelefonico.setEnabled(false);
        profileImage.setClickable(false);
    }

    private void cerrarSesion() {
        auth.signOut();
        Log.d(TAG, "Sesión cerrada");
        redirigirAlLogin();
    }

    private void redirigirAlLogin() {
        Intent i = new Intent(this, LoginLegacyActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                profileImage.setImageURI(selectedImage);
                photoUrl = selectedImage.toString();
                Log.d(TAG, "Imagen seleccionada: " + photoUrl);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verificar si el usuario sigue autenticado
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "Usuario no autenticado en onResume");
            redirigirAlLogin();
        }
    }
}
