package com.example.puriqtours.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.MainActivity;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.Reserva;
import com.example.puriqtours.entity.Solicitud;
import com.example.puriqtours.helper.FirestoreHelper;
import com.example.puriqtours.helper.UserSessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GuideDetailActivity extends AppCompatActivity {

    private ImageView imgGuidePhoto;
    private TextView tvNombres, tvApellidos, tvFechaNacimiento, tvNumeroDocumento;
    private TextView tvTelefono, tvCorreo, tvDomicilio, tvIdiomas, tvPuntuacion;
    private Button btnAsignarTour;
    
    private int guideId;
    private String guideUid;
    private String guideName;
    private String guideLocation;
    private int guideRating;
    private boolean guideAvailable;
    private String guideProfileImage;
    
    private FirestoreHelper firestoreHelper;
    private UserSessionManager sessionManager;
    private Reserva reservaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_detail);

        firestoreHelper = new FirestoreHelper();
        sessionManager = new UserSessionManager(this);
        
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
        guideUid = getIntent().getStringExtra("guide_uid");
        guideName = getIntent().getStringExtra("guide_name");
        guideLocation = getIntent().getStringExtra("guide_location");
        guideRating = getIntent().getIntExtra("guide_rating", 5);
        guideAvailable = getIntent().getBooleanExtra("guide_available", true);
        guideProfileImage = getIntent().getStringExtra("guide_profile_image");

        // Cargar imagen de perfil con Picasso desde Firebase Storage
        if (guideProfileImage != null && !guideProfileImage.isEmpty()) {
            Picasso.get()
                    .load(guideProfileImage)
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .into(imgGuidePhoto);
        } else {
            imgGuidePhoto.setImageResource(R.drawable.avatar);
        }
        
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

        // Configurar botón según disponibilidad (guide_status)
        if (guideAvailable) {
            btnAsignarTour.setEnabled(true);
            btnAsignarTour.setText("Proponer Reserva");
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
        // Cargar reservas sin guía asignado desde Firestore
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("Cargando reservas disponibles...")
                .setCancelable(false)
                .create();
        loadingDialog.show();
        
        firestoreHelper.loadReservasWithoutGuide(reservas -> {
            loadingDialog.dismiss();
            
            if (reservas == null || reservas.isEmpty()) {
                Toast.makeText(this, "No hay reservas disponibles sin guía asignado", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Crear dialog con lista de reservas
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_lista_reservas, null);
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            
            // Configurar el contenedor de reservas
            LinearLayout containerReservas = dialogView.findViewById(R.id.containerReservas);
            
            if (containerReservas != null) {
                for (Reserva reserva : reservas) {
                    View itemView = LayoutInflater.from(this).inflate(R.layout.item_reserva_simple, containerReservas, false);
                    
                    TextView tvTitulo = itemView.findViewById(R.id.tvReservaTitle);
                    TextView tvFecha = itemView.findViewById(R.id.tvReservaDate);
                    TextView tvPrecio = itemView.findViewById(R.id.tvReservaPrice);
                    
                    if (tvTitulo != null) tvTitulo.setText(reserva.getTitle() != null ? reserva.getTitle() : "Reserva sin título");
                    if (tvFecha != null) tvFecha.setText(reserva.getDate() + " - " + reserva.getHour());
                    if (tvPrecio != null) tvPrecio.setText("S/. " + (reserva.getPrice() != null ? reserva.getPrice() : "0.00"));
                    
                    itemView.setOnClickListener(v -> {
                        reservaSeleccionada = reserva;
                        dialog.dismiss();
                        showFormularioSolicitudDialog();
                    });
                    
                    containerReservas.addView(itemView);
                }
            }
            
            Button btnCerrar = dialogView.findViewById(R.id.btnCerrar);
            if (btnCerrar != null) {
                btnCerrar.setOnClickListener(v -> dialog.dismiss());
            }
            
            dialog.show();
        });
    }

    private void showFormularioSolicitudDialog() {
        if (reservaSeleccionada == null) {
            Toast.makeText(this, "Error: No se seleccionó ninguna reserva", Toast.LENGTH_SHORT).show();
            return;
        }
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_formulario_solicitud, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Referencias a los campos
        TextInputEditText etTitulo = dialogView.findViewById(R.id.etTituloSolicitud);
        TextInputEditText etDescripcion = dialogView.findViewById(R.id.etDescripcionSolicitud);
        TextInputEditText etPago = dialogView.findViewById(R.id.etPagoSolicitud);
        TextView tvReservaInfo = dialogView.findViewById(R.id.tvReservaInfo);
        Button btnEnviar = dialogView.findViewById(R.id.btnEnviarSolicitud);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        // Mostrar información de la reserva seleccionada
        if (tvReservaInfo != null) {
            String info = "Reserva: " + reservaSeleccionada.getTitle() + " - " + reservaSeleccionada.getDate();
            tvReservaInfo.setText(info);
        }

        // Botón cancelar
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        // Botón enviar
        if (btnEnviar != null) {
            btnEnviar.setOnClickListener(v -> {
                String titulo = etTitulo != null ? etTitulo.getText().toString().trim() : "";
                String descripcion = etDescripcion != null ? etDescripcion.getText().toString().trim() : "";
                String pagoStr = etPago != null ? etPago.getText().toString().trim() : "";

                // Validaciones
                if (titulo.isEmpty()) {
                    Toast.makeText(this, "Por favor ingrese un título", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (descripcion.isEmpty()) {
                    Toast.makeText(this, "Por favor ingrese una descripción", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pagoStr.isEmpty()) {
                    Toast.makeText(this, "Por favor ingrese el pago", Toast.LENGTH_SHORT).show();
                    return;
                }

                float pago;
                try {
                    pago = Float.parseFloat(pagoStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "El pago debe ser un número válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                crearSolicitud(titulo, descripcion, pago);
            });
        }

        dialog.show();
    }

    private void crearSolicitud(String titulo, String descripcion, float pago) {
        // Obtener UID del admin actual
        String adminUid = sessionManager.getUid();
        
        if (adminUid == null || adminUid.isEmpty()) {
            Toast.makeText(this, "Error: No se pudo obtener el usuario actual", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear objeto Solicitud
        Solicitud solicitud = new Solicitud();
        solicitud.setTitle(titulo);
        solicitud.setDesc(descripcion);
        solicitud.setPay(pago);
        solicitud.setIdGuia(guideUid);
        solicitud.setIdEmpresa(adminUid);
        solicitud.setIdReserva(reservaSeleccionada.getIdReserva());
        solicitud.setStatus("Pendiente");

        // Guardar en Firestore
        firestoreHelper.createSolicitud(solicitud, (success, solicitudId) -> {
            if (success) {
                Toast.makeText(this, "Solicitud enviada al guía exitosamente", Toast.LENGTH_LONG).show();
                showPropuestaEnviadaDialog();
                // TODO: Enviar notificación push al guía
            } else {
                Toast.makeText(this, "Error al enviar la solicitud", Toast.LENGTH_SHORT).show();
            }
        });
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
                    startActivity(new Intent(this, MainAdminActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_reports) {
                    startActivity(new Intent(this, ReportsActivity.class));
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
}
