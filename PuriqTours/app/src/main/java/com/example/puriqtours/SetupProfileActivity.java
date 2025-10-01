package com.example.puriqtours;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.ImageButton;

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
        Button btnNext   = findViewById(R.id.btnNext);

        ImageButton btnBack = findViewById(R.id.btnBackForgot);
        btnBack.setOnClickListener(v ->
                        getOnBackPressedDispatcher().onBackPressed()

        );

        // Prefill opcional
        String prefill = getIntent().getStringExtra("prefill_username");
        if (prefill != null && !prefill.trim().isEmpty()) etUsername.setText(prefill.trim());

        btnUpload.setOnClickListener(v -> pickImage.launch("image/*"));

        btnNext.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            if (TextUtils.isEmpty(username)) { etUsername.setError("El usuario no puede estar vacío"); return; }

            Intent i = new Intent(SetupProfileActivity.this,
                    com.example.puriqtours.onboarding.InterestsOnboardingActivity.class);
            i.putExtra("username", username);
            startActivity(i);
            // finish();  // opcional
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
        if (selectedImageUri != null) {
            outState.putString("photo_uri", selectedImageUri.toString());
        }
    }


}

