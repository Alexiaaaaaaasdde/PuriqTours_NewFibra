package com.example.puriqtours.cliente;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.puriqtours.R;
import com.example.puriqtours.login.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private ShapeableImageView profileImage, profileIcon;
    private FloatingActionButton fabEditPhoto;

    private EditText etNombre, etApellido, etCorreo, etFechaNacimiento, etNumeroDocumento,
            etNumeroTelefonico, etTipoDocumento, etDireccion, etIdioma;

    private Button btnUpdate, btnSave;

    FirebaseFirestore db;
    FirebaseAuth auth;
    String uid;
    String photoUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            redirigirAlLogin();
            return;
        }

        uid = currentUser.getUid();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarVistas();
        configurarToolbarYNavegacion();
        cargarDatosUsuario();

        btnSave.setVisibility(View.GONE);

        btnUpdate.setOnClickListener(v -> habilitarEdicion());
        btnSave.setOnClickListener(v -> guardarCambios());
        fabEditPhoto.setOnClickListener(v -> abrirGaleria());
    }

    private void inicializarVistas() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        profileImage = findViewById(R.id.profileImage);
        profileIcon = findViewById(R.id.profileIcon);

        fabEditPhoto = findViewById(R.id.fabEditPhoto);

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etCorreo = findViewById(R.id.etCorreo);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etNumeroDocumento = findViewById(R.id.etNumeroDocumento);
        etNumeroTelefonico = findViewById(R.id.etNumeroTelefonico);
        etTipoDocumento = findViewById(R.id.etTipoDocumento);
        etDireccion = findViewById(R.id.etDireccion);
        etIdioma = findViewById(R.id.etIdioma);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnSave = findViewById(R.id.btnSave);
    }

    private void configurarToolbarYNavegacion() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        profileIcon.setOnClickListener(v -> mostrarMenuCerrarSesion(profileIcon));

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_perfil);

        bottomNavigation.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            if (item.getItemId() == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return true;
        });
    }

    private void mostrarMenuCerrarSesion(ShapeableImageView anchor) {
        PopupMenu popup = new PopupMenu(ProfileActivity.this, anchor);

        popup.getMenu().add(0, 1, 0, "  Cerrar Sesión")
                .setIcon(R.drawable.ic_logout);

        // Forzar iconos visibles
        try {
            Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(popup);
            Method setForceIcons = menuPopupHelper.getClass()
                    .getDeclaredMethod("setForceShowIcon", boolean.class);
            setForceIcons.invoke(menuPopupHelper, true);
        } catch (Exception ignored) {}

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) cerrarSesion();
            return true;
        });

        popup.show();
    }

    private void cargarDatosUsuario() {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    etNombre.setText(doc.getString("name"));
                    etApellido.setText(doc.getString("last_name"));
                    etCorreo.setText(doc.getString("email"));
                    etFechaNacimiento.setText(doc.getString("birthdate"));
                    etNumeroDocumento.setText(doc.getString("document"));
                    etNumeroTelefonico.setText(doc.getString("phone"));
                    etDireccion.setText(doc.getString("address"));
                    etTipoDocumento.setText(doc.getString("doc_type"));
                    etIdioma.setText(doc.getString("language"));

                    photoUrl = doc.getString("profile_image");

                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Picasso.get().load(photoUrl).into(profileImage);
                        Picasso.get().load(photoUrl).into(profileIcon);
                    }
                });
    }

    private void habilitarEdicion() {
        etNumeroTelefonico.setEnabled(true);
        fabEditPhoto.setVisibility(View.VISIBLE);
        profileImage.setClickable(true);

        profileImage.setOnClickListener(v -> abrirGaleria());

        btnUpdate.setVisibility(View.GONE);
        btnSave.setVisibility(View.VISIBLE);
    }

    private void abrirGaleria() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK);
        pickPhoto.setType("image/*");
        startActivityForResult(pickPhoto, PICK_IMAGE_REQUEST);
    }

    private boolean validarTelefono() {
        return etNumeroTelefonico.getText().toString().matches("\\d{9}");
    }

    private void guardarCambios() {

        if (!validarTelefono()) {
            etNumeroTelefonico.setError("Debe tener 9 dígitos");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("phone", etNumeroTelefonico.getText().toString());
        updates.put("profile_image", photoUrl);

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(v -> mostrarDialogo())
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Error al guardar", Toast.LENGTH_SHORT).show());
    }

    private void mostrarDialogo() {
        Dialog dialog = new Dialog(ProfileActivity.this);
        dialog.setContentView(R.layout.dialog_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.findViewById(R.id.btnClose).setOnClickListener(c -> dialog.dismiss());
        dialog.show();

        btnSave.setVisibility(View.GONE);
        btnUpdate.setVisibility(View.VISIBLE);
        fabEditPhoto.setVisibility(View.GONE);
        etNumeroTelefonico.setEnabled(false);
    }

    private void cerrarSesion() {
        auth.signOut();
        redirigirAlLogin();
    }

    private void redirigirAlLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    /** 🔥 SUBIR FOTO A FIREBASE STORAGE */
    private void subirImagenAFirebase(Uri imageUri) {

        StorageReference storageRef =
                FirebaseStorage.getInstance().getReference("profile_images/" + uid + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(task -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                    photoUrl = uri.toString(); // URL permanente

                    db.collection("users").document(uid)
                            .update("profile_image", photoUrl);

                    Picasso.get().load(photoUrl).into(profileImage);
                    Picasso.get().load(photoUrl).into(profileIcon);

                    Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show();

                }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                profileImage.setImageURI(uri);
                profileIcon.setImageURI(uri);

                subirImagenAFirebase(uri);
            }
        }
    }
}
