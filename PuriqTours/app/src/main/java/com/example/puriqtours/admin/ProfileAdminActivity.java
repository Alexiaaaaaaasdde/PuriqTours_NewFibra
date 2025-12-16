package com.example.puriqtours.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.puriqtours.MainActivity;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.Usuario;
import com.example.puriqtours.helper.FirestoreHelper;
import com.example.puriqtours.helper.UserSessionManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ProfileAdminActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ProfileAdminActivity";
    
    // Firebase helpers
    private FirestoreHelper firestoreHelper;
    private UserSessionManager sessionManager;
    private String currentAdminUid;
    private Usuario currentAdmin;
    
    // Google Maps
    private GoogleMap mMap;
    private Geocoder geocoder;
    private CardView cardMapContainer;

    // Views para formulario
    private ScrollView completeProfileView;
    private TextInputEditText etCompanyName, etPhone, etEmail;
    private MaterialButton btnSave;

    // Views para perfil completo
    private ScrollView profileView;
    private TextView tvCompanyName, tvPhone, tvEmail, tvAddress;
    private ImageView ivProfileImage;
    private LinearLayout readOnlyView, editView;
    private TextInputEditText etEditCompanyName, etEditPhone, etEditEmail;
    private MaterialButton btnEdit;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_admin);

        // Inicializar Firebase helpers
        firestoreHelper = new FirestoreHelper();
        sessionManager = new UserSessionManager(this);
        
        // Obtener UID del usuario actual
        currentAdminUid = sessionManager.getUid();
        
        // Inicializar Geocoder
        geocoder = new Geocoder(this, new Locale("es", "PE"));

        initViews();
        setupMap();
        
        // Ocultar ambas vistas inicialmente para evitar el flash
        if (completeProfileView != null) {
            completeProfileView.setVisibility(View.GONE);
        }
        if (profileView != null) {
            profileView.setVisibility(View.GONE);
        }
        
        setupBottomNavigation();
        checkProfileStatus();
    }

    private void initViews() {
        // Icono de notificaciones en toolbar
        android.widget.ImageView notificationIcon = findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                Intent intent = new Intent(this, NotificationsActivity.class);
                startActivity(intent);
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
        ivProfileImage = findViewById(R.id.ivProfileImage);
        
        // Vistas de edición
        readOnlyView = findViewById(R.id.readOnlyView);
        editView = findViewById(R.id.editView);
        etEditCompanyName = findViewById(R.id.etEditCompanyName);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditEmail = findViewById(R.id.etEditEmail);
        btnEdit = findViewById(R.id.btnEdit);
        
        // Contenedor del mapa
        cardMapContainer = findViewById(R.id.cardMapContainer);

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
                // Cerrar sesión
                cerrarSesion();
            });
        }
    }
    
    private void cerrarSesion() {
        // Mostrar diálogo de confirmación
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
                    // 1. Cerrar sesión de Firebase Authentication
                    FirebaseAuth.getInstance().signOut();
                    
                    // 2. Limpiar datos de sesión en SharedPreferences
                    android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    
                    // 3. Ir al login
                    Intent intent = new Intent(ProfileAdminActivity.this, com.example.puriqtours.login.LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    
                    Toast.makeText(this, "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void checkProfileStatus() {
        if (currentAdminUid == null || currentAdminUid.isEmpty()) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            showCompleteProfileForm();
            return;
        }
        
        // Cargar perfil desde Firestore
        firestoreHelper.loadAdminProfile(currentAdminUid, admin -> {
            if (admin != null && admin.getRol() != null && admin.getRol().equals("Admin")) {
                currentAdmin = admin;
                // Verificar si el perfil tiene datos completos
                if (isProfileComplete(admin)) {
                    showCompletedProfile();
                } else {
                    showCompleteProfileForm();
                }
            } else {
                Toast.makeText(this, "Error: No se pudo cargar el perfil", Toast.LENGTH_SHORT).show();
                showCompleteProfileForm();
            }
        });
    }
    
    private boolean isProfileComplete(Usuario admin) {
        return admin.getName() != null && !admin.getName().isEmpty() &&
               admin.getPhone() != null && !admin.getPhone().isEmpty() &&
               admin.getEmail() != null && !admin.getEmail().isEmpty();
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
        if (currentAdmin == null) {
            Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Cargar datos del usuario actual desde Firestore
        String companyName = currentAdmin.getName() != null ? currentAdmin.getName() : "Sin nombre";
        String phone = currentAdmin.getPhone() != null ? currentAdmin.getPhone() : "Sin teléfono";
        String email = currentAdmin.getEmail() != null ? currentAdmin.getEmail() : "Sin correo";
        String address = currentAdmin.getAddress() != null ? currentAdmin.getAddress() : "Sin dirección";

        tvCompanyName.setText(companyName);
        tvPhone.setText(phone);
        tvEmail.setText(email);
        tvAddress.setText(address);
        
        // Mostrar ubicación en el mapa
        if (mMap != null) {
            showLocationOnMap(address);
        }
        
        // Cargar imagen de perfil desde Firebase Storage
        String profileImageUrl = currentAdmin.getProfile_image();
        if (profileImageUrl != null && !profileImageUrl.isEmpty() && ivProfileImage != null) {
            Picasso.get()
                .load(profileImageUrl)
                .placeholder(R.drawable.logo_empresa)
                .error(R.drawable.logo_empresa)
                .into(ivProfileImage);
        } else if (ivProfileImage != null) {
            ivProfileImage.setImageResource(R.drawable.logo_empresa);
        }
        
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
        String address = tvAddress.getText().toString(); // La dirección no se edita por ahora

        // Validaciones usando los campos de edición
        if (!validateInputs(companyName, phone, email, etEditCompanyName, etEditPhone, etEditEmail)) {
            return;
        }

        // Guardar en Firestore
        firestoreHelper.updateAdminProfile(currentAdminUid, companyName, phone, email, address, success -> {
            if (success) {
                // Actualizar objeto local
                currentAdmin.setName(companyName);
                currentAdmin.setPhone(phone);
                currentAdmin.setEmail(email);
                
                // Actualizar vistas
                tvCompanyName.setText(companyName);
                tvPhone.setText(phone);
                tvEmail.setText(email);

                Toast.makeText(this, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
                
                // Salir del modo de edición
                exitEditMode();
            } else {
                Toast.makeText(this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String companyName = etCompanyName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Validaciones
        if (!validateInputs(companyName, phone, email)) {
            return;
        }

        // Guardar en Firestore
        String address = currentAdmin != null && currentAdmin.getAddress() != null ? 
                         currentAdmin.getAddress() : "Sin dirección";
        
        firestoreHelper.updateAdminProfile(currentAdminUid, companyName, phone, email, address, success -> {
            if (success) {
                // Actualizar objeto local
                if (currentAdmin != null) {
                    currentAdmin.setName(companyName);
                    currentAdmin.setPhone(phone);
                    currentAdmin.setEmail(email);
                }
                
                Toast.makeText(this, "Perfil guardado exitosamente", Toast.LENGTH_SHORT).show();

                // Cambiar a vista de perfil completo
                showCompletedProfile();
            } else {
                Toast.makeText(this, "Error al guardar perfil", Toast.LENGTH_SHORT).show();
            }
        });
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
                    startActivity(new Intent(this, MainAdminActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_reports) {
                    startActivity(new Intent(this, ReportsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_chat) {
                    startActivity(new Intent(this, ChatListActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_profile) {
                    return true; // Ya estás en perfil
                }
                return false;
            });
        }
    }
    
    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragmentProfile);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Configurar click en el mapa para abrir Google Maps
        if (cardMapContainer != null) {
            cardMapContainer.setOnClickListener(v -> openInGoogleMaps());
        }
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Configurar estilo del mapa
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        
        Log.d(TAG, "Mapa listo para mostrar ubicación");
        
        // Si ya tenemos los datos del admin, mostrar ubicación
        if (currentAdmin != null && currentAdmin.getAddress() != null) {
            showLocationOnMap(currentAdmin.getAddress());
        }
    }
    
    private void showLocationOnMap(String address) {
        if (mMap == null || address == null || address.trim().isEmpty() || address.equals("Sin dirección")) {
            Log.d(TAG, "No se puede mostrar ubicación: mapa o dirección no válida");
            return;
        }
        
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(address + ", Perú", 1);
                
                if (addresses != null && !addresses.isEmpty()) {
                    Address location = addresses.get(0);
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    
                    runOnUiThread(() -> {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(currentAdmin.getName() != null ? currentAdmin.getName() : "Empresa"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        
                        Log.d(TAG, "Ubicación mostrada: " + latLng.toString());
                    });
                } else {
                    runOnUiThread(() -> {
                        Log.e(TAG, "No se encontró la dirección: " + address);
                        Toast.makeText(this, "No se pudo encontrar la ubicación", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error al geocodificar dirección", e);
                    Toast.makeText(this, "Error al cargar ubicación", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void openInGoogleMaps() {
        if (currentAdmin == null || currentAdmin.getAddress() == null || 
            currentAdmin.getAddress().trim().isEmpty() || currentAdmin.getAddress().equals("Sin dirección")) {
            Toast.makeText(this, "No hay dirección configurada", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Crear URI para Google Maps
        String address = currentAdmin.getAddress();
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address + ", Perú"));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        
        // Verificar si Google Maps está instalado
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Si no está instalado, abrir en el navegador
            Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address + ", Perú"));
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            startActivity(webIntent);
        }
    }
}
