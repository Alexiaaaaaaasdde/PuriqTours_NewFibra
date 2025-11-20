package com.example.puriqtours.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import android.widget.Toast;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Tour;
import com.example.puriqtours.helper.FirestoreHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TourDetailActivity extends AppCompatActivity {

    private TextView tvHoraInicio, tvDuracion, tvCosto, tvIdiomas, tvRegion, tvLocation;
    private LinearLayout layoutServiciosExtrasContainer;
    private LinearLayout layoutUbicacionesContainer;
    private Button btnCerrar, btnEditarTour, btnEliminarTour;
    
    private NestedScrollView scrollViewContent;
    private String tourId;
    private String tourName;
    private FirestoreHelper firestoreHelper;
    private Tour currentTour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Obtener datos del intent ANTES de setContentView
        tourId = getIntent().getStringExtra("tour_id");
        tourName = getIntent().getStringExtra("tour_name");
        
        // Inicializar Firestore helper
        firestoreHelper = new FirestoreHelper();
        
        // Validar que tenemos ID
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Error: ID de tour no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Cargar datos PRIMERO, luego mostrar la vista
        firestoreHelper.loadTourById(tourId, tour -> {
            if (tour != null) {
                currentTour = tour;
                // Ahora sí crear la vista con los datos listos
                setupUIWithData();
            } else {
                Toast.makeText(this, "Error al cargar tour desde Firestore", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void setupUIWithData() {
        // AHORA crear la vista cuando ya tenemos los datos
        setContentView(R.layout.activity_tour_detail);
        
        // Configurar toolbar
        setupToolbar();
        
        // Inicializar vistas
        initViews();
        
        // Mostrar los datos que ya tenemos cargados
        displayTourData(currentTour);
        
        // Hacer visible el contenido
        scrollViewContent.setVisibility(View.VISIBLE);
        
        // Configurar listeners
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Detalles del tour");
        }
    }

    private void initViews() {
        // ScrollView principal (iniciará invisible)
        scrollViewContent = findViewById(R.id.scrollViewContent);
        
        // Datos básicos
        tvHoraInicio = findViewById(R.id.tvHoraInicio);
        tvDuracion = findViewById(R.id.tvDuracion);
        tvCosto = findViewById(R.id.tvCosto);
        tvIdiomas = findViewById(R.id.tvIdiomas);
        tvRegion = findViewById(R.id.tvRegion);
        tvLocation = findViewById(R.id.tvLocation);
        
        // Contenedores dinámicos
        layoutServiciosExtrasContainer = findViewById(R.id.layoutServiciosExtrasContainer);
        layoutUbicacionesContainer = findViewById(R.id.layoutUbicacionesContainer);
        
        // Botones
        btnCerrar = findViewById(R.id.btnCerrar);
        btnEditarTour = findViewById(R.id.btnEditarTour);
        btnEliminarTour = findViewById(R.id.btnEliminarTour);
    }
    
    private void displayTourData(Tour tour) {
        // Log para debug
        android.util.Log.d("TourDetail", "=== DATOS DEL TOUR ===");
        android.util.Log.d("TourDetail", "ID: " + tour.getIdTour());
        android.util.Log.d("TourDetail", "Título: " + tour.getTitle());
        android.util.Log.d("TourDetail", "Hora inicio: " + tour.getStartTime());
        android.util.Log.d("TourDetail", "Hora fin: " + tour.getEndTime());
        android.util.Log.d("TourDetail", "Precio: " + tour.getPrice());
        android.util.Log.d("TourDetail", "Región: " + tour.getRegion());
        android.util.Log.d("TourDetail", "Ubicación: " + tour.getLocation());
        android.util.Log.d("TourDetail", "Idiomas: " + tour.getIdiomas());
        android.util.Log.d("TourDetail", "Servicios extras: " + (tour.getServiciosExtras() != null ? tour.getServiciosExtras().size() : "null"));
        android.util.Log.d("TourDetail", "Ruta: " + (tour.getRuta() != null ? tour.getRuta().size() : "null"));
        
        // Datos básicos del tour desde Firestore
        tvHoraInicio.setText(tour.getStartTime() != null && !tour.getStartTime().isEmpty() ? tour.getStartTime() : "No especificado");
        tvDuracion.setText(tour.getEndTime() != null && !tour.getEndTime().isEmpty() ? tour.getEndTime() : "No especificado");
        tvCosto.setText(tour.getPrice() != null ? tour.getPrice() + " soles" : "0 soles");
        tvRegion.setText(tour.getRegion() != null && !tour.getRegion().isEmpty() ? tour.getRegion() : "No especificada");
        tvLocation.setText(tour.getLocation() != null && !tour.getLocation().isEmpty() ? tour.getLocation() : "No especificada");
        
        // Idiomas
        if (tour.getIdiomas() != null && !tour.getIdiomas().isEmpty()) {
            tvIdiomas.setText(tour.getIdiomas());
        } else {
            tvIdiomas.setText("Español - Inglés");
        }
        
        // Cargar servicios extras
        if (tour.getServiciosExtras() != null && !tour.getServiciosExtras().isEmpty()) {
            loadServiciosExtras(tour.getServiciosExtras());
        } else {
            android.util.Log.d("TourDetail", "No hay servicios extras");
        }
        
        // Cargar ubicaciones
        if (tour.getRuta() != null && !tour.getRuta().isEmpty()) {
            loadUbicaciones(tour.getRuta());
        } else {
            android.util.Log.d("TourDetail", "No hay ruta definida");
        }
    }
    
    private void loadServiciosExtras(List<Tour.ServicioExtra> servicios) {
        // Limpiar contenedor
        layoutServiciosExtrasContainer.removeAllViews();
        
        if (servicios == null || servicios.isEmpty()) {
            // Mostrar mensaje de "Sin servicios"
            TextView tvSinServicios = new TextView(this);
            tvSinServicios.setText("No hay servicios extra disponibles");
            tvSinServicios.setTextSize(14);
            tvSinServicios.setTextColor(getResources().getColor(R.color.gray));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, (int) (16 * getResources().getDisplayMetrics().density));
            tvSinServicios.setLayoutParams(params);
            layoutServiciosExtrasContainer.addView(tvSinServicios);
            return;
        }
        
        // Crear una tarjeta para cada servicio
        for (Tour.ServicioExtra servicio : servicios) {
            addServicioExtraView(servicio);
        }
    }
    
    private void addServicioExtraView(Tour.ServicioExtra servicio) {
        // Crear contenedor de servicio
        LinearLayout servicioLayout = new LinearLayout(this);
        servicioLayout.setOrientation(LinearLayout.VERTICAL);
        servicioLayout.setBackgroundResource(R.drawable.rounded_corners);
        servicioLayout.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));
        servicioLayout.setElevation(2 * getResources().getDisplayMetrics().density);
        
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        servicioLayout.setPadding(padding, padding, padding, padding);
        
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, padding);
        servicioLayout.setLayoutParams(layoutParams);
        
        // Nombre del servicio
        TextView tvNombre = new TextView(this);
        tvNombre.setText(servicio.getTitle() != null ? servicio.getTitle() : "Servicio");
        tvNombre.setTextSize(16);
        tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);
        tvNombre.setTextColor(getResources().getColor(R.color.black));
        LinearLayout.LayoutParams nombreParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nombreParams.setMargins(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density));
        tvNombre.setLayoutParams(nombreParams);
        servicioLayout.addView(tvNombre);
        
        // Precio del servicio
        TextView tvPrecio = new TextView(this);
        tvPrecio.setText("Costo: " + (servicio.getPrice() != null ? servicio.getPrice() + " soles por persona" : "Gratis"));
        tvPrecio.setTextSize(14);
        tvPrecio.setTextColor(getResources().getColor(R.color.gray));
        LinearLayout.LayoutParams precioParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        precioParams.setMargins(0, 0, 0, (int) (12 * getResources().getDisplayMetrics().density));
        tvPrecio.setLayoutParams(precioParams);
        servicioLayout.addView(tvPrecio);
        
        // Imagen del servicio
        ImageView imgServicio = new ImageView(this);
        imgServicio.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgServicio.setBackgroundResource(R.drawable.rounded_corners);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (int) (150 * getResources().getDisplayMetrics().density)
        );
        imgServicio.setLayoutParams(imgParams);
        
        // Cargar imagen con Picasso
        if (servicio.getImageUrl() != null && !servicio.getImageUrl().isEmpty()) {
            android.util.Log.d("TourDetail", "Cargando imagen: " + servicio.getImageUrl());
            Picasso.get()
                .load(servicio.getImageUrl())
                .placeholder(R.drawable.servicio_1)
                .error(R.drawable.servicio_1)
                .into(imgServicio);
        } else {
            android.util.Log.d("TourDetail", "Sin URL de imagen, usando placeholder");
            imgServicio.setImageResource(R.drawable.servicio_1);
        }
        
        servicioLayout.addView(imgServicio);
        
        // Agregar tarjeta al contenedor
        layoutServiciosExtrasContainer.addView(servicioLayout);
    }
    
    private void loadUbicaciones(List<Tour.Ubicacion> ubicaciones) {
        // Limpiar contenedor antes de agregar nuevas ubicaciones
        layoutUbicacionesContainer.removeAllViews();
        
        // Crear una vista para cada ubicación
        for (int i = 0; i < ubicaciones.size(); i++) {
            Tour.Ubicacion ubicacion = ubicaciones.get(i);
            addUbicacionView(i + 1, ubicacion);
        }
    }
    
    private void addUbicacionView(int numero, Tour.Ubicacion ubicacion) {
        // Crear el layout horizontal para la ubicación
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, (int) (16 * getResources().getDisplayMetrics().density));
        rowLayout.setLayoutParams(rowParams);
        
        // Layout izquierdo (Ubicación)
        LinearLayout leftLayout = new LinearLayout(this);
        leftLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        );
        leftParams.setMargins(0, 0, (int) (8 * getResources().getDisplayMetrics().density), 0);
        leftLayout.setLayoutParams(leftParams);
        
        // Label de ubicación
        TextView labelUbicacion = new TextView(this);
        labelUbicacion.setText("🗺️ Ubicación " + numero);
        labelUbicacion.setTextSize(14);
        labelUbicacion.setTextColor(getResources().getColor(R.color.black, null));
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, 0, 0, (int) (4 * getResources().getDisplayMetrics().density));
        labelUbicacion.setLayoutParams(labelParams);
        
        // TextView con el nombre de la ubicación
        TextView tvUbicacion = new TextView(this);
        tvUbicacion.setText(ubicacion.getTitle() != null ? ubicacion.getTitle() : "Sin nombre");
        tvUbicacion.setTextSize(12);
        tvUbicacion.setTextColor(getResources().getColor(R.color.gray, null));
        tvUbicacion.setBackgroundResource(R.drawable.rounded_edittext);
        tvUbicacion.setPadding(
            (int) (8 * getResources().getDisplayMetrics().density),
            (int) (8 * getResources().getDisplayMetrics().density),
            (int) (8 * getResources().getDisplayMetrics().density),
            (int) (8 * getResources().getDisplayMetrics().density)
        );
        
        leftLayout.addView(labelUbicacion);
        leftLayout.addView(tvUbicacion);
        
        // Layout derecho (Actividades)
        LinearLayout rightLayout = new LinearLayout(this);
        rightLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        );
        rightLayout.setLayoutParams(rightParams);
        
        // Label de coordenadas
        TextView labelCoordenadas = new TextView(this);
        labelCoordenadas.setText("📍 Coordenadas");
        labelCoordenadas.setTextSize(14);
        labelCoordenadas.setTextColor(getResources().getColor(R.color.black, null));
        LinearLayout.LayoutParams labelCoordParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelCoordParams.setMargins(0, 0, 0, (int) (4 * getResources().getDisplayMetrics().density));
        labelCoordenadas.setLayoutParams(labelCoordParams);
        
        // TextView con las coordenadas
        TextView tvCoordenadas = new TextView(this);
        String coordenadas = "Lat: " + (ubicacion.getLat() != null ? ubicacion.getLat() : "0.0") + 
                           "\nLng: " + (ubicacion.getLng() != null ? ubicacion.getLng() : "0.0");
        tvCoordenadas.setText(coordenadas);
        tvCoordenadas.setTextSize(12);
        tvCoordenadas.setTextColor(getResources().getColor(R.color.gray, null));
        tvCoordenadas.setBackgroundResource(R.drawable.rounded_edittext);
        tvCoordenadas.setPadding(
            (int) (8 * getResources().getDisplayMetrics().density),
            (int) (8 * getResources().getDisplayMetrics().density),
            (int) (8 * getResources().getDisplayMetrics().density),
            (int) (8 * getResources().getDisplayMetrics().density)
        );
        
        rightLayout.addView(labelCoordenadas);
        rightLayout.addView(tvCoordenadas);
        
        // Agregar ambos layouts al row
        rowLayout.addView(leftLayout);
        rowLayout.addView(rightLayout);
        
        // Agregar el row al contenedor principal
        layoutUbicacionesContainer.addView(rowLayout);
    }
    
    private void setupListeners() {
        // Botón cerrar
        btnCerrar.setOnClickListener(v -> finish());
        
        // Botón editar tour
        btnEditarTour.setOnClickListener(v -> {
            Intent intent = new Intent(TourDetailActivity.this, EditTourActivity.class);
            intent.putExtra("tourId", tourId);
            intent.putExtra("tour_name", tourName);
            startActivityForResult(intent, 300);
        });
        
        // Botón eliminar tour
        if (btnEliminarTour != null) {
            btnEliminarTour.setOnClickListener(v -> confirmarEliminarTour());
        }
        
        // Toolbar back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }
    
    private void confirmarEliminarTour() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Eliminar tour")
                .setMessage("¿Estás seguro de que deseas eliminar este tour? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarTour())
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    private void eliminarTour() {
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Error: ID de tour inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        
        firestoreHelper.deleteTour(tourId, success -> {
            if (success) {
                Toast.makeText(this, "Tour eliminado exitosamente", Toast.LENGTH_SHORT).show();
                
                // Enviar resultado de vuelta a ToursActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("tour_deleted", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Error al eliminar el tour", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 300 && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("tour_updated", false)) {
                Toast.makeText(this, "Tour actualizado correctamente", Toast.LENGTH_SHORT).show();
                
                // Recargar los datos del tour
                firestoreHelper.loadTourById(tourId, tour -> {
                    if (tour != null) {
                        currentTour = tour;
                        displayTourData(tour);
                    }
                });
                
                // Enviar resultado de vuelta a ToursActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("tour_updated", true);
                setResult(RESULT_OK, resultIntent);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
