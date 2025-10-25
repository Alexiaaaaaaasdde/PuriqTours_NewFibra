package com.example.puriqtours;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GuideDetailActivity extends AppCompatActivity {

    private ImageView imgGuidePhoto;
    private TextView tvNombres, tvApellidos, tvFechaNacimiento, tvNumeroDocumento;
    private TextView tvTelefono, tvCorreo, tvDomicilio, tvIdiomas, tvPuntuacion;
    private Button btnAsignarTour;
    
    private int guideId;
    private String guideName;
    private String guideLocation;
    private int guideRating;
    private boolean guideAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_detail);

        initViews();
        setupToolbar();
        loadGuideData();
        setupListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        imgGuidePhoto = findViewById(R.id.imgGuidePhoto);
        tvNombres = findViewById(R.id.tvNombres);
        tvApellidos = findViewById(R.id.tvApellidos);
        tvFechaNacimiento = findViewById(R.id.tvFechaNacimiento);
        tvNumeroDocumento = findViewById(R.id.tvNumeroDocumento);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvCorreo = findViewById(R.id.tvCorreo);
        tvDomicilio = findViewById(R.id.tvDomicilio);
        tvIdiomas = findViewById(R.id.tvIdiomas);
        tvPuntuacion = findViewById(R.id.tvPuntuacion);
        btnAsignarTour = findViewById(R.id.btnAsignarTour);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalles del guía");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadGuideData() {
        // Obtener datos del intent
        guideId = getIntent().getIntExtra("guide_id", 1);
        guideName = getIntent().getStringExtra("guide_name");
        guideLocation = getIntent().getStringExtra("guide_location");
        guideRating = getIntent().getIntExtra("guide_rating", 5);
        guideAvailable = getIntent().getBooleanExtra("guide_available", true);

        // Cargar datos del guía (datos de ejemplo)
        imgGuidePhoto.setImageResource(R.drawable.avatar);
        
        // Dividir el nombre completo en nombres y apellidos
        if (guideName != null) {
            String[] nameParts = guideName.split(" ");
            if (nameParts.length >= 2) {
                tvNombres.setText(nameParts[0]);
                tvApellidos.setText(nameParts[1]);
            } else {
                tvNombres.setText(guideName);
                tvApellidos.setText("Apellido");
            }
        }

        // Datos de ejemplo basados en el mockup
        tvFechaNacimiento.setText("6 de enero 2000");
        tvNumeroDocumento.setText("98987876");
        tvTelefono.setText("987654321");
        tvCorreo.setText("nass.pozo@gmail.com");
        tvDomicilio.setText("Av. Salvador 165");
        tvIdiomas.setText("Español-Inglés-Quechua");
        
        // Mostrar puntuación con estrellas
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < guideRating) {
                stars.append("⭐");
            } else {
                stars.append("☆");
            }
        }
        tvPuntuacion.setText(stars.toString());

        // Configurar botón según disponibilidad
        if (guideAvailable) {
            btnAsignarTour.setEnabled(true);
            btnAsignarTour.setText("Asignar a un tour");
            btnAsignarTour.setBackgroundTintList(getColorStateList(R.color.teal_700));
        } else {
            btnAsignarTour.setEnabled(false);
            btnAsignarTour.setText("No disponible");
            btnAsignarTour.setBackgroundTintList(getColorStateList(R.color.gray_medium));
        }
    }

    private void setupListeners() {
        btnAsignarTour.setOnClickListener(v -> {
            if (guideAvailable) {
                showToursDisponiblesDialog();
            } else {
                Toast.makeText(this, "El guía no está disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showToursDisponiblesDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tours_disponibles, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Configurar cards de tours disponibles
        CardView cardTour2 = dialogView.findViewById(R.id.cardTour2);
        CardView cardTour5 = dialogView.findViewById(R.id.cardTour5);
        CardView cardTour6 = dialogView.findViewById(R.id.cardTour6);
        CardView cardTour10 = dialogView.findViewById(R.id.cardTour10);
        CardView cardTour5Duplicate = dialogView.findViewById(R.id.cardTour5Duplicate);

        // Click listeners para cada tour
        if (cardTour2 != null) {
            cardTour2.setOnClickListener(v -> {
                showConfirmarPropuestaDialog(2, "Tour número 2");
                dialog.dismiss();
            });
        }

        if (cardTour5 != null) {
            cardTour5.setOnClickListener(v -> {
                showConfirmarPropuestaDialog(5, "Tour número 5");
                dialog.dismiss();
            });
        }

        if (cardTour6 != null) {
            cardTour6.setOnClickListener(v -> {
                showConfirmarPropuestaDialog(6, "Tour número 6");
                dialog.dismiss();
            });
        }

        if (cardTour10 != null) {
            cardTour10.setOnClickListener(v -> {
                showConfirmarPropuestaDialog(10, "Tour número 10");
                dialog.dismiss();
            });
        }

        if (cardTour5Duplicate != null) {
            cardTour5Duplicate.setOnClickListener(v -> {
                showConfirmarPropuestaDialog(5, "Tour número 5 (Duplicado)");
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void showConfirmarPropuestaDialog(int tourId, String tourName) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmar_propuesta, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Configurar botones
        Button btnProponer = dialogView.findViewById(R.id.btnProponer);
        Button btnCerrar = dialogView.findViewById(R.id.btnCerrar);

        if (btnProponer != null) {
            btnProponer.setOnClickListener(v -> {
                dialog.dismiss();
                asignarGuiaATour(tourId, tourName);
            });
        }

        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void asignarGuiaATour(int tourId, String tourName) {
        // Simular el envío de la propuesta
        showPropuestaEnviadaDialog();

        // Aquí podrías enviar los datos al servidor o base de datos
        // updateGuideAssignment(guideId, tourId);

        // Opcional: actualizar el estado del guía a no disponible
        guideAvailable = false;
        loadGuideData(); // Recargar datos para actualizar el botón
    }

    private void showPropuestaEnviadaDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_propuesta_enviada, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Configurar botón cerrar
        Button btnCerrar = dialogView.findViewById(R.id.btnCerrar);
        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            // No seleccionar ningún ítem por defecto en esta vista
            
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
                    startActivity(new Intent(this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }
    }
}
