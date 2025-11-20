package com.example.puriqtours.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.SplashActivity;
import com.example.puriqtours.entity.Usuario;
import com.example.puriqtours.guia.MainGuiaActivity;
import com.example.puriqtours.helper.UserSessionManager;
import com.example.puriqtours.onboarding.InterestsOnboardingActivity;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.puriqtours.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class SetupProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ShapeableImageView imgProfile;
    private EditText etUsername;
    private Button btnUploadPhoto, btnNext;
    private ImageButton btnBack;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private StorageReference storageRef;
    private Uri imageUri;
    private byte[] compressedImageBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        imgProfile = findViewById(R.id.imgProfile);
        etUsername = findViewById(R.id.etUsername);
        btnNext = findViewById(R.id.btnNext);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnBack = findViewById(R.id.btnBackForgot);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        btnBack.setOnClickListener(v -> finish());

        btnUploadPhoto.setOnClickListener(v -> openImageChooser());

        btnNext.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) {
                etUsername.setError("El nombre de usuario es obligatorio");
                return;
            }
            if (compressedImageBytes != null) {
                uploadCompressedImage(username);
            } else {
                saveUserData(username, null);
            }
        });
    }

    // --- Comprimir imagen antes de subir ---
    private void compressImage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

            // Escalar si la imagen es muy grande
            int maxWidth = 800;
            int maxHeight = 800;
            float ratio = Math.min((float) maxWidth / bitmap.getWidth(), (float) maxHeight / bitmap.getHeight());
            int newWidth = Math.round(bitmap.getWidth() * ratio);
            int newHeight = Math.round(bitmap.getHeight() * ratio);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos); // 70% calidad
            compressedImageBytes = baos.toByteArray();

            Toast.makeText(this, "Imagen lista para subir (comprimida)", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Abrir galería ---
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*"); // valida solo imágenes
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    // --- Recibir imagen seleccionada ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(imgProfile);
            compressImage(imageUri);
        }
    }

    // --- Subir imagen comprimida a Firebase Storage ---
    private void uploadCompressedImage(String username) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Subiendo imagen...");
        dialog.setCancelable(false);
        dialog.show();

        String uid = user.getUid();

        // 🔹 Carpeta profile_images + nombre único
        String imageName = uid + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storageRef.child("profile_images/" + imageName);

        fileRef.putBytes(compressedImageBytes)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            dialog.dismiss();
                            saveUserData(username, uri.toString());
                        }))
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    // --- Guardar datos del usuario en Firestore ---
    private void saveUserData(String username, String imageUrl) {
        String uid = user.getUid();
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        if (imageUrl != null) updates.put("profile_image", imageUrl);

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(document -> {
                                if (document.exists()) {
                                    Usuario user = Usuario.fromSnapshot(document);
                                    UserSessionManager session = new UserSessionManager(this);
                                    session.saveUser(user);

                                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, InterestsOnboardingActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(this, "No se encontró el usuario", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error al obtener usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
