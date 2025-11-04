package com.example.puriqtours.login;

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

import com.example.puriqtours.entity.LocalAuth;
import com.example.puriqtours.R;

public class SetupProfileLegacyActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_setup_profile_legacy);

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

            // ✅ Guardar username y foto en LocalAuth
            LocalAuth localAuth = new LocalAuth(this);
            String photoUri = (selectedImageUri != null) ? selectedImageUri.toString() : "";

            // 🔹 Guardamos con los datos existentes y actualizamos foto
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
                    localAuth.getLanguage(),      // idioma aún vacío
                    localAuth.getActivities(),    // actividades aún vacías
                    photoUri                      // nueva foto
            );

            Toast.makeText(this, "Perfil guardado correctamente", Toast.LENGTH_SHORT).show();

            // ➡️ Siguiente pantalla: selección de idioma o intereses
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
