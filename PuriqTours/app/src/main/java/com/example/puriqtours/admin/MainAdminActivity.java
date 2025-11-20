package com.example.puriqtours.admin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.TourAdmin;
import com.example.puriqtours.entity.GuideAdmin;
import com.example.puriqtours.helper.FirestoreHelper;
import com.example.puriqtours.helper.NotificationHelper;
import com.example.puriqtours.helper.TourConverter;
import com.example.puriqtours.helper.GuideConverter;
import com.example.puriqtours.helper.UserSessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainAdminActivity extends AppCompatActivity {

    private static final String TAG = "MainAdminActivity";
    private NotificationHelper notificationHelper;
    private FirestoreHelper firestoreHelper;
    private UserSessionManager sessionManager;
    
    // Vistas de tour
    private CardView cardLatestTour;
    private TextView tourTitle;
    private TextView tourDescription;
    private ImageView tourImage;
    private ImageView logoCenter; // Imagen de perfil del usuario
    
    // Contenedor de guías
    private LinearLayout guidesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);
        
        // Inicializar Firestore helper
        firestoreHelper = new FirestoreHelper();
        sessionManager = new UserSessionManager(this);
        
        // Inicializar sistema de notificaciones
        initializeNotificationSystem();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        initViews();
        
        // 🔹 Icono de notificaciones en toolbar (probar todas las notificaciones)
        ImageView notificationIcon = findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                // Simular TODAS las notificaciones para demostración
                if (notificationHelper != null) {
                    notificationHelper.simulateAllNotifications();
                    Toast.makeText(this, "🔔 Probando todas las notificaciones del sistema...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 🔹 Configurar toolbar con botón de logout
        setupToolbar();

        // 🔹 Navegación a vista de tours
        TextView tvLatestTours = findViewById(R.id.tvLatestTours);
        TextView tvViewMore = findViewById(R.id.tvViewMore);

        if (tvLatestTours != null) {
            tvLatestTours.setOnClickListener(v -> {
                Intent intent = new Intent(MainAdminActivity.this, ToursAdminActivity.class);
                startActivity(intent);
            });
        }

        if (tvViewMore != null) {
            tvViewMore.setOnClickListener(v -> {
                Intent intent = new Intent(MainAdminActivity.this, ToursAdminActivity.class);
                startActivity(intent);
            });
        }

        // 🔹 Navegación a vista de guías
        TextView tvGuidesList = findViewById(R.id.tvGuidesList);
        if (tvGuidesList != null) {
            tvGuidesList.setOnClickListener(v -> {
                Intent intent = new Intent(MainAdminActivity.this, GuidesActivity.class);
                startActivity(intent);
            });
        }

        // 🔹 BottomNavigation
        setupBottomNavigation();
        
        // 🔹 Cargar datos desde Firestore
        loadLatestTour();
        loadLatestGuides();
    }
    
    private void initViews() {
        // Vistas de tour
        cardLatestTour = findViewById(R.id.cardLatestTour);
        tourTitle = findViewById(R.id.tourTitle);
        tourDescription = findViewById(R.id.tourDescription);
        tourImage = findViewById(R.id.tourImage);
        logoCenter = findViewById(R.id.logoCenter);
        
        // Contenedor de guías
        guidesContainer = findViewById(R.id.guidesContainer);
        
        // Cargar imagen de perfil del usuario
        loadUserProfileImage();
    }
    
    private void loadUserProfileImage() {
        String currentUid = sessionManager.getUid();
        if (currentUid != null && !currentUid.isEmpty()) {
            firestoreHelper.loadAdminProfile(currentUid, admin -> {
                if (admin != null && admin.getProfile_image() != null && !admin.getProfile_image().isEmpty()) {
                    Picasso.get()
                        .load(admin.getProfile_image())
                        .placeholder(R.drawable.logo_empresa)
                        .error(R.drawable.logo_empresa)
                        .into(logoCenter);
                }
            });
        }
    }
    
    private void loadLatestTour() {
        firestoreHelper.loadTours(tours -> {
            if (tours != null && !tours.isEmpty()) {
                // Obtener el último tour creado (el más reciente)
                com.example.puriqtours.entity.Tour latestTour = tours.get(tours.size() - 1);
                
                // Convertir a TourAdmin para mostrar en UI
                TourAdmin tourAdmin = TourConverter.tourToTourAdmin(latestTour);
                
                Log.d(TAG, "Tour más reciente cargado: " + tourAdmin.getName());
                
                // Mostrar datos en la card
                tourTitle.setText(tourAdmin.getName());
                tourDescription.setText(tourAdmin.getDescription());
                
                // Cargar imagen del tour desde Firebase Storage con Picasso
                if (tourAdmin.getImageUrl() != null && !tourAdmin.getImageUrl().isEmpty()) {
                    Picasso.get()
                        .load(tourAdmin.getImageUrl())
                        .placeholder(R.drawable.kuelap)
                        .error(R.drawable.kuelap)
                        .into(tourImage);
                } else {
                    tourImage.setImageResource(R.drawable.kuelap);
                }
                
                // Configurar click para ir a detalles
                cardLatestTour.setOnClickListener(v -> {
                    Intent intent = new Intent(MainAdminActivity.this, TourDetailActivity.class);
                    intent.putExtra("tour_id", tourAdmin.getId());
                    intent.putExtra("tour_name", tourAdmin.getName());
                    startActivity(intent);
                });
                
                Log.d(TAG, "Tour más reciente cargado: " + tourAdmin.getName());
            } else {
                Log.d(TAG, "No hay tours disponibles");
                tourTitle.setText("No hay tours");
                tourDescription.setText("Crea tu primer tour");
                cardLatestTour.setOnClickListener(null);
            }
        });
    }
    
    private void loadLatestGuides() {
        firestoreHelper.loadGuides(usuarios -> {
            if (usuarios != null && !usuarios.isEmpty()) {
                // Convertir Usuarios a GuideAdmins
                List<GuideAdmin> guideAdmins = GuideConverter.usuariosToGuideAdmins(usuarios);
                
                if (!guideAdmins.isEmpty()) {
                    // Limpiar contenedor
                    guidesContainer.removeAllViews();
                    
                    // Obtener las últimas 3 guías (o las que hayan disponibles)
                    int guidesToShow = Math.min(3, guideAdmins.size());
                    List<GuideAdmin> latestGuides = guideAdmins.subList(
                        Math.max(0, guideAdmins.size() - guidesToShow), 
                        guideAdmins.size()
                    );
                    
                    // Crear vista para cada guía
                    for (GuideAdmin guide : latestGuides) {
                        addGuideView(guide);
                    }
                    
                    Log.d(TAG, "Guías cargadas: " + latestGuides.size());
                } else {
                    Log.d(TAG, "No hay guías disponibles");
                    // Mantener el layout por defecto si no hay guías
                }
            } else {
                Log.d(TAG, "No hay guías disponibles");
                // Mantener el layout por defecto si no hay guías
            }
        });
    }
    
    private void addGuideView(GuideAdmin guide) {
        // Crear layout vertical para cada guía
        LinearLayout guideLayout = new LinearLayout(this);
        guideLayout.setOrientation(LinearLayout.VERTICAL);
        guideLayout.setGravity(android.view.Gravity.CENTER);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        );
        params.setMargins(8, 0, 8, 0);
        guideLayout.setLayoutParams(params);
        
        // ImageView para la foto del guía
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
            (int) (60 * getResources().getDisplayMetrics().density),
            (int) (60 * getResources().getDisplayMetrics().density)
        );
        imageParams.setMargins(0, 0, 0, 8);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        // Cargar imagen de perfil con Picasso desde Firebase Storage
        if (guide.getProfileImageUrl() != null && !guide.getProfileImageUrl().isEmpty()) {
            Picasso.get()
                .load(guide.getProfileImageUrl())
                .placeholder(R.drawable.imagen_perfil)
                .error(R.drawable.imagen_perfil)
                .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.imagen_perfil); // Imagen por defecto
        }
        
        // TextView para el nombre
        TextView nameText = new TextView(this);
        nameText.setText(guide.getName());
        nameText.setTextSize(12);
        nameText.setTextColor(getResources().getColor(android.R.color.black, null));
        nameText.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.setMargins(0, 0, 0, 4);
        nameText.setLayoutParams(textParams);
        
        // View para el indicador de disponibilidad
        View indicator = new View(this);
        LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(
            (int) (12 * getResources().getDisplayMetrics().density),
            (int) (12 * getResources().getDisplayMetrics().density)
        );
        indicatorParams.setMargins(0, 4, 0, 0);
        indicator.setLayoutParams(indicatorParams);
        
        // Color según disponibilidad
        if (guide.isAvailable()) {
            indicator.setBackgroundColor(getResources().getColor(R.color.teal_700, null));
        } else {
            indicator.setBackgroundColor(getResources().getColor(R.color.gray_medium, null));
        }
        
        // Agregar vistas al layout
        guideLayout.addView(imageView);
        guideLayout.addView(nameText);
        guideLayout.addView(indicator);
        
        // Agregar al contenedor principal
        guidesContainer.addView(guideLayout);
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
                    Intent intent = new Intent(MainAdminActivity.this, com.example.puriqtours.login.LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    
                    Toast.makeText(this, "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
            
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_dashboard) {
                    return true; // Ya estás en dashboard
                } else if (id == R.id.nav_reports) {
                    startActivity(new Intent(this, ReportsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_chat) {
                    startActivity(new Intent(this, ChatListActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileAdminActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos cuando volvamos a esta actividad
        loadLatestTour();
        loadLatestGuides();
    }
    
    private void initializeNotificationSystem() {
        // Crear instancia del NotificationHelper (esto crea los canales automáticamente)
        notificationHelper = new NotificationHelper(this);
        
        // Solicitar permisos para notificaciones en Android 13+
        requestNotificationPermission();
    }
    
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, 
                    android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        100);
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos de notificación concedidos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Las notificaciones están deshabilitadas", Toast.LENGTH_LONG).show();
            }
        }
    }
}
