package com.example.puriqtours.cliente;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.puriqtours.BaseActivity;
import com.example.puriqtours.R;
import com.example.puriqtours.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ShapeableImageView profileImage;
    private EditText etNombre, etApellido, etCorreo, etFechaNacimiento, etNumeroDocumento,
            etNumeroTelefonico, etTipoDocumento, etDireccion, etIdioma;

    private Button btnUpdate, btnSave;
    private FloatingActionButton fabEditPhoto;

    FirebaseFirestore db;
    FirebaseAuth auth;
    String uid;
    String photoUrl = "";
    boolean fotoSubiendo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 🔥 Toolbar unificado
        setupSharedToolbar();

        enableDrawerIcon();

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        uid = user.getUid();
        db = FirebaseFirestore.getInstance();

        inicializarVistas();
        cargarDatosUsuario();
        configurarBottomNav();

        btnUpdate.setOnClickListener(v -> habilitarEdicion());
        btnSave.setOnClickListener(v -> guardarCambios());
        fabEditPhoto.setOnClickListener(v -> abrirGaleria());
    }

    private void inicializarVistas() {
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

        btnUpdate = findViewById(R.id.btnUpdate);
        btnSave = findViewById(R.id.btnSave);
        fabEditPhoto = findViewById(R.id.fabEditPhoto);

        btnSave.setVisibility(View.GONE);
        fabEditPhoto.setVisibility(View.GONE);
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
                        setProfileIcon(photoUrl); // 🔥 para el toolbar global
                    }
                });
    }

    private void habilitarEdicion() {
        etNumeroTelefonico.setEnabled(true);
        profileImage.setClickable(true);
        fabEditPhoto.setVisibility(View.VISIBLE);

        btnUpdate.setVisibility(View.GONE);
        btnSave.setVisibility(View.VISIBLE);

        profileImage.setOnClickListener(v -> abrirGaleria());
    }

    private void abrirGaleria() {
        Intent pick = new Intent(Intent.ACTION_PICK);
        pick.setType("image/*");
        startActivityForResult(pick, PICK_IMAGE_REQUEST);
    }

    private void subirImagen(Uri uri) {
        fotoSubiendo = true;

        StorageReference ref =
                FirebaseStorage.getInstance().getReference("profile_images/" + uid + ".jpg");

        ref.putFile(uri).addOnSuccessListener(task ->
                ref.getDownloadUrl().addOnSuccessListener(url -> {

                    photoUrl = url.toString();
                    fotoSubiendo = false;

                    Picasso.get().load(photoUrl).into(profileImage);
                    setProfileIcon(photoUrl);

                })
        ).addOnFailureListener(e -> {
            fotoSubiendo = false;
            Toast.makeText(this, "Error subiendo imagen", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validarTelefono() {
        return etNumeroTelefonico.getText().toString().matches("\\d{9}");
    }

    private void guardarCambios() {

        if (fotoSubiendo) {
            Toast.makeText(this, "Espera que la foto termine de subir", Toast.LENGTH_SHORT).show();
            return;
        }

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
        dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        fabEditPhoto.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);
        btnUpdate.setVisibility(View.VISIBLE);
        etNumeroTelefonico.setEnabled(false);
    }

    private void configurarBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_perfil);

        nav.setOnItemSelectedListener(item -> {

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
            if (item.getItemId() == R.id.nav_historial) {
                overridePendingTransition(0, 0);
                return true;
            }

            return true;
        });
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (req == PICK_IMAGE_REQUEST && res == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                profileImage.setImageURI(uri);
                subirImagen(uri);
            }
        }
    }
}
