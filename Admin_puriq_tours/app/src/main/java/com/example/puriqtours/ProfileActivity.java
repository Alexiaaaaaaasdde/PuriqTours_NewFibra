package com.example.puriqtours;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "CompanyProfile";
    private static final String KEY_PROFILE_COMPLETED = "profile_completed";
    private static final String KEY_COMPANY_NAME = "company_name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_EMAIL = "email";

    // Views para formulario
    private ScrollView completeProfileView;
    private TextInputEditText etCompanyName, etPhone, etEmail;
    private MaterialButton btnSave;

    // Views para perfil completo
    private ScrollView profileView;
    private TextView tvCompanyName, tvPhone, tvEmail, tvAddress;
    private LinearLayout readOnlyView, editView;
    private TextInputEditText etEditCompanyName, etEditPhone, etEditEmail;
    private MaterialButton btnEdit;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupBottomNavigation();
        checkProfileStatus();
    }

    private void initViews() {
        // Icono de notificaciones en toolbar
        android.widget.ImageView notificationIcon = findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show();
            });
        }

        // Configurar toolbar con botón de logout
        setupToolbar();

        // Vistas del formulario
        completeProfileView = findViewById(R.id.completeProfileView);
        etCompanyName = findViewById(R.id.etCompanyName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);

        // Vistas del perfil completo
        profileView = findViewById(R.id.profileView);
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvAddress = findViewById(R.id.tvAddress);
        
        // Vistas de edición
        readOnlyView = findViewById(R.id.readOnlyView);
        editView = findViewById(R.id.editView);
        etEditCompanyName = findViewById(R.id.etEditCompanyName);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditEmail = findViewById(R.id.etEditEmail);
        btnEdit = findViewById(R.id.btnEdit);

        // Configurar botón guardar
        btnSave.setOnClickListener(v -> saveProfile());
        
        // Configurar botón editar
        btnEdit.setOnClickListener(v -> {
            if (isEditing) {
                saveEditedProfile();
            } else {
                enterEditMode();
            }
        });
    }

    private void setupToolbar() {
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                // TODO: Implementar cerrar sesión
                Toast.makeText(this, "Cerrar sesión", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void checkProfileStatus() {
        boolean profileCompleted = sharedPreferences.getBoolean(KEY_PROFILE_COMPLETED, false);
        
        if (profileCompleted) {
            // Mostrar perfil completo
            showCompletedProfile();
        } else {
            // Mostrar formulario para completar perfil
            showCompleteProfileForm();
        }
    }

    private void showCompleteProfileForm() {
        completeProfileView.setVisibility(View.VISIBLE);
        profileView.setVisibility(View.GONE);
    }

    private void showCompletedProfile() {
        completeProfileView.setVisibility(View.GONE);
        profileView.setVisibility(View.VISIBLE);
        loadProfileData();
    }

    private void loadProfileData() {
        String companyName = sharedPreferences.getString(KEY_COMPANY_NAME, "Perú Aventura");
        String phone = sharedPreferences.getString(KEY_PHONE, "987654321");
        String email = sharedPreferences.getString(KEY_EMAIL, "email123@gmail.com");

        tvCompanyName.setText(companyName);
        tvPhone.setText(phone);
        tvEmail.setText(email);
        tvAddress.setText("Santander 165"); // Dirección fija actualizada
        
        // Resetear modo de edición
        exitEditMode();
    }

    private void enterEditMode() {
        isEditing = true;
        readOnlyView.setVisibility(View.GONE);
        editView.setVisibility(View.VISIBLE);
        btnEdit.setText("Guardar");
        
        // Cargar datos actuales en los campos de edición
        etEditCompanyName.setText(tvCompanyName.getText().toString());
        etEditPhone.setText(tvPhone.getText().toString());
        etEditEmail.setText(tvEmail.getText().toString());
    }

    private void exitEditMode() {
        isEditing = false;
        readOnlyView.setVisibility(View.VISIBLE);
        editView.setVisibility(View.GONE);
        btnEdit.setText("Editar");
    }

    private void saveEditedProfile() {
        String companyName = etEditCompanyName.getText().toString().trim();
        String phone = etEditPhone.getText().toString().trim();
        String email = etEditEmail.getText().toString().trim();

        // Validaciones usando los campos de edición
        if (!validateInputs(companyName, phone, email, etEditCompanyName, etEditPhone, etEditEmail)) {
            return;
        }

        // Guardar en SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_COMPANY_NAME, companyName);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_EMAIL, email);
        editor.apply();

        // Actualizar vistas
        tvCompanyName.setText(companyName);
        tvPhone.setText(phone);
        tvEmail.setText(email);

        Toast.makeText(this, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
        
        // Salir del modo de edición
        exitEditMode();
    }

    private void saveProfile() {
        String companyName = etCompanyName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Validaciones
        if (!validateInputs(companyName, phone, email)) {
            return;
        }

        // Guardar en SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_PROFILE_COMPLETED, true);
        editor.putString(KEY_COMPANY_NAME, companyName);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_EMAIL, email);
        editor.apply();

        Toast.makeText(this, "Perfil guardado exitosamente", Toast.LENGTH_SHORT).show();

        // Cambiar a vista de perfil completo
        showCompletedProfile();
    }

    private boolean validateInputs(String companyName, String phone, String email) {
        return validateInputs(companyName, phone, email, etCompanyName, etPhone, etEmail);
    }

    private boolean validateInputs(String companyName, String phone, String email, 
                                 TextInputEditText nameField, TextInputEditText phoneField, TextInputEditText emailField) {
        // Validar nombre de empresa
        if (TextUtils.isEmpty(companyName)) {
            nameField.setError("El nombre de la empresa es obligatorio");
            nameField.requestFocus();
            return false;
        }

        if (companyName.length() < 2) {
            nameField.setError("El nombre debe tener al menos 2 caracteres");
            nameField.requestFocus();
            return false;
        }

        // Validar teléfono
        if (TextUtils.isEmpty(phone)) {
            phoneField.setError("El teléfono es obligatorio");
            phoneField.requestFocus();
            return false;
        }

        if (phone.length() != 9) {
            phoneField.setError("El teléfono debe tener exactamente 9 dígitos");
            phoneField.requestFocus();
            return false;
        }

        if (!phone.matches("\\d+")) {
            phoneField.setError("El teléfono solo debe contener números");
            phoneField.requestFocus();
            return false;
        }

        // Validar email
        if (TextUtils.isEmpty(email)) {
            emailField.setError("El correo es obligatorio");
            emailField.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Ingrese un correo válido");
            emailField.requestFocus();
            return false;
        }

        return true;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_profile);
            
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_dashboard) {
                    startActivity(new Intent(this, MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_reports) {
                    startActivity(new Intent(this, ReportsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_profile) {
                    return true; // Ya estás en perfil
                }
                return false;
            });
        }
    }
}
