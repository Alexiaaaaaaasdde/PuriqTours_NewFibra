package com.example.puriqtours;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SetupProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private EditText etUsername;
    private Uri selectedImageUri = null;

    // Picker de galería
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgProfile.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        imgProfile = findViewById(R.id.imgProfile);
        etUsername = findViewById(R.id.etUsername);
        Button btnUpload = findViewById(R.id.btnUploadPhoto);
        Button btnNext   = findViewById(R.id.btnNext);   // ✅ aquí está bien, sin punto
        ImageButton btnBack = findViewById(R.id.btnBackForgot);

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnUpload.setOnClickListener(v -> pickImage.launch("image/*"));

        // Prefill opcional
        String prefill = getIntent().getStringExtra("prefill_username");
        if (prefill != null && !prefill.trim().isEmpty()) etUsername.setText(prefill.trim());

        btnNext.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            if (TextUtils.isEmpty(username)) {
                etUsername.setError("El usuario no puede estar vacío");
                return;
            }

            // Guarda username y foto en LocalAuth
            LocalAuth localAuth = new LocalAuth(this);
            String photoUri = (selectedImageUri != null) ? selectedImageUri.toString() : "";
            localAuth.saveProfile(username, "", "", photoUri);

            Toast.makeText(this, "Perfil guardado", Toast.LENGTH_SHORT).show();

            // ➡️ Siguiente pantalla: intereses e idiomas
            Intent i = new Intent(this, com.example.puriqtours.onboarding.InterestsOnboardingActivity.class);
            startActivity(i);
            finish();
        });

        if (savedInstanceState != null) {
            String saved = savedInstanceState.getString("photo_uri");
            if (saved != null) {
                selectedImageUri = Uri.parse(saved);
                imgProfile.setImageURI(selectedImageUri);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@Nullable Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedImageUri != null && outState != null) {
            outState.putString("photo_uri", selectedImageUri.toString());
        }
    }
}
