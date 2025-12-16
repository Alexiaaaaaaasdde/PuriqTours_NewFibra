package com.example.puriqtours.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Usuario;
import com.example.puriqtours.guia.GuidePendingActivity;
import com.example.puriqtours.helper.UserSessionManager;
import com.example.puriqtours.onboarding.InterestsOnboardingActivity;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
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
    private FirebaseUser currentUser;
    private StorageReference storageRef;

    private Uri imageUri;
    private byte[] compressedImageBytes;

    // 🔹 Datos del registro (si viene de RegisterGuideActivity)
    private HashMap<String, Object> userData;
    private boolean isNewRegistration = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        initFirebase();
        initViews();
        checkRegistrationMode();
        setupListeners();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    private void initViews() {
        imgProfile = findViewById(R.id.imgProfile);
        etUsername = findViewById(R.id.etUsername);
        btnNext = findViewById(R.id.btnNext);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnBack = findViewById(R.id.btnBackForgot);
    }

    private void checkRegistrationMode() {
        // 🔍 Verificar si viene de un nuevo registro
        userData = (HashMap<String, Object>) getIntent().getSerializableExtra("userData");
        isNewRegistration = (userData != null && userData.containsKey("password"));

        if (isNewRegistration) {
            // Modo: Nuevo registro (viene de RegisterGuideActivity)
            btnNext.setText("Completar registro");
        } else {
            // Modo: Actualizar perfil (usuario ya existe)
            btnNext.setText("Actualizar perfil");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnUploadPhoto.setOnClickListener(v -> openImageChooser());
        btnNext.setOnClickListener(v -> validateAndProceed());
    }

    // ================= VALIDACIÓN =================

    private void validateAndProceed() {
        String username = etUsername.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("El nombre de usuario es obligatorio");
            return;
        }

        if (isNewRegistration) {
            // 🆕 CREAR NUEVO USUARIO
            createNewUser(username);
        } else {
            // ✏️ ACTUALIZAR USUARIO EXISTENTE
            updateExistingUser(username);
        }
    }

    // ================= NUEVO REGISTRO =================

    private void createNewUser(String username) {
        String email = (String) userData.get("email");
        String password = (String) userData.get("password");

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Error: Datos incompletos", Toast.LENGTH_SHORT).show();
            return;
        }

        btnNext.setEnabled(false);

        // 1️⃣ Crear cuenta en Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (authResult.getUser() != null) {
                        String userId = authResult.getUser().getUid();
                        userData.put("userId", userId);
                        userData.put("username", username);

                        // 2️⃣ Subir foto si existe, sino continuar
                        if (compressedImageBytes != null) {
                            uploadImageAndSaveUser(userId);
                        } else {
                            saveNewUserToFirestore(userId, null);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    btnNext.setEnabled(true);
                    Toast.makeText(this, "Error al crear cuenta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void uploadImageAndSaveUser(String userId) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Subiendo imagen...");
        dialog.setCancelable(false);
        dialog.show();

        String imageName = userId + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storageRef.child("profile_images/" + imageName);

        fileRef.putBytes(compressedImageBytes)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            dialog.dismiss();
                            saveNewUserToFirestore(userId, uri.toString());
                        }))
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveNewUserToFirestore(userId, null);
                });
    }

    private void saveNewUserToFirestore(String userId, String imageUrl) {
        // 🔹 Remover la contraseña antes de guardar
        userData.remove("password");

        // 🔹 Agregar imagen si existe
        if (imageUrl != null) {
            userData.put("profile_image", imageUrl);
        }

        // 🔹 Agregar timestamp de registro
        userData.put("register_date", com.google.firebase.Timestamp.now());

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // ✅ Guardar sesión
                    Usuario user = createUserFromData(userId);
                    UserSessionManager session = new UserSessionManager(this);
                    session.saveUser(user);

                    Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();
                    redirectAfterRegistration(user);
                })
                .addOnFailureListener(e -> {
                    btnNext.setEnabled(true);
                    Toast.makeText(this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ================= ACTUALIZAR PERFIL EXISTENTE =================

    private void updateExistingUser(String username) {
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        btnNext.setEnabled(false);

        if (compressedImageBytes != null) {
            uploadImageAndUpdateProfile(username);
        } else {
            updateUserProfile(username, null);
        }
    }

    private void uploadImageAndUpdateProfile(String username) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Subiendo imagen...");
        dialog.setCancelable(false);
        dialog.show();

        String uid = currentUser.getUid();
        String imageName = uid + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storageRef.child("profile_images/" + imageName);

        fileRef.putBytes(compressedImageBytes)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            dialog.dismiss();
                            updateUserProfile(username, uri.toString());
                        }))
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateUserProfile(username, null);
                });
    }

    private void updateUserProfile(String username, String imageUrl) {
        String uid = currentUser.getUid();
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        if (imageUrl != null) {
            updates.put("profile_image", imageUrl);
        }

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    // Recargar usuario actualizado
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(document -> {
                                if (document.exists()) {
                                    Usuario user = Usuario.fromSnapshot(document);
                                    UserSessionManager session = new UserSessionManager(this);
                                    session.saveUser(user);

                                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();

                                    // Ir a onboarding de intereses
                                    startActivity(new Intent(this, InterestsOnboardingActivity.class));
                                    finish();
                                }
                            })
                            .addOnFailureListener(e -> {
                                btnNext.setEnabled(true);
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnNext.setEnabled(true);
                    Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ================= REDIRECCIÓN =================

    private void redirectAfterRegistration(Usuario user) {
        Intent intent;

        // 🔒 Si es guía NO habilitado → GuidePendingActivity
        if ("Guia".equalsIgnoreCase(user.getRol())
                && !"Habilitado".equalsIgnoreCase(user.getGuide_status())) {

            intent = new Intent(this, GuidePendingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            // ✅ Cliente → onboarding de intereses
            intent = new Intent(this, InterestsOnboardingActivity.class);
        }

        startActivity(intent);
        finish();
    }

    // ================= MANEJO DE IMÁGENES =================

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            imageUri = data.getData();
            Picasso.get().load(imageUri).into(imgProfile);
            compressImage(imageUri);
        }
    }

    private void compressImage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

            int maxWidth = 800;
            int maxHeight = 800;
            float ratio = Math.min(
                    (float) maxWidth / bitmap.getWidth(),
                    (float) maxHeight / bitmap.getHeight()
            );

            int newWidth = Math.round(bitmap.getWidth() * ratio);
            int newHeight = Math.round(bitmap.getHeight() * ratio);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            compressedImageBytes = baos.toByteArray();

            Toast.makeText(this, "Imagen lista para subir", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    // ================= HELPERS =================

    private Usuario createUserFromData(String userId) {
        Usuario user = new Usuario();
        user.setUid(userId);
        user.setUsername((String) userData.get("username"));
        user.setName((String) userData.get("name"));
        user.setLast_name((String) userData.get("last_name"));
        user.setEmail((String) userData.get("email"));
        user.setRol((String) userData.get("rol"));
        user.setStatus((String) userData.get("status"));

        // Solo para guías
        if (userData.containsKey("guide_status")) {
            user.setGuide_status((String) userData.get("guide_status"));
        }

        if (userData.containsKey("profile_image")) {
            user.setProfile_image((String) userData.get("profile_image"));
        }

        return user;
    }
}
