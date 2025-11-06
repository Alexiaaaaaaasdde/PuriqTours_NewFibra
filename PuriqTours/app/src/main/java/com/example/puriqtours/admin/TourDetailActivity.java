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

import java.util.List;

public class TourDetailActivity extends AppCompatActivity {

    private TextView tvHoraInicio, tvDuracion, tvCosto, tvIdiomas, tvFechaTour;
    private TextView tvNombreServicio1, tvPrecioServicio1, tvDescripcionServicio1;
    private TextView tvNombreServicio2, tvPrecioServicio2, tvDescripcionServicio2;
    private ImageView imgServicio1, imgServicio2;
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
        tvFechaTour = findViewById(R.id.tvFechaTour);
        
        // Servicios extra
        tvNombreServicio1 = findViewById(R.id.tvNombreServicio1);
        tvPrecioServicio1 = findViewById(R.id.tvPrecioServicio1);
        tvDescripcionServicio1 = findViewById(R.id.tvDescripcionServicio1);
        imgServicio1 = findViewById(R.id.imgServicio1);
        
        tvNombreServicio2 = findViewById(R.id.tvNombreServicio2);
        tvPrecioServicio2 = findViewById(R.id.tvPrecioServicio2);
        tvDescripcionServicio2 = findViewById(R.id.tvDescripcionServicio2);
        imgServicio2 = findViewById(R.id.imgServicio2);
        
        // Contenedor de ubicaciones dinámico
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
        android.util.Log.d("TourDetail", "Fecha: " + tour.getDate());
        android.util.Log.d("TourDetail", "Idiomas: " + tour.getIdiomas());
        android.util.Log.d("TourDetail", "Servicios extras: " + (tour.getServiciosExtras() != null ? tour.getServiciosExtras().size() : "null"));
        android.util.Log.d("TourDetail", "Ruta: " + (tour.getRuta() != null ? tour.getRuta().size() : "null"));
        
        // Datos básicos del tour desde Firestore
        tvHoraInicio.setText(tour.getStartTime() != null && !tour.getStartTime().isEmpty() ? tour.getStartTime() : "No especificado");
        tvDuracion.setText(tour.getEndTime() != null && !tour.getEndTime().isEmpty() ? tour.getEndTime() : "No especificado");
        tvCosto.setText(tour.getPrice() != null ? tour.getPrice() + " soles" : "0 soles");
        tvFechaTour.setText(tour.getDate() != null && !tour.getDate().isEmpty() ? tour.getDate() : "Sin fecha");
        
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
            android.util.Log.d("TourDetail", "No hay servicios extras, cargando por defecto");
            loadDefaultServicesAndLocations();
        }
        
        // Cargar ubicaciones
        if (tour.getRuta() != null && !tour.getRuta().isEmpty()) {
            loadUbicaciones(tour.getRuta());
        } else {
            android.util.Log.d("TourDetail", "No hay ruta, cargando por defecto");
            loadDefaultLocations();
        }
    }
    
    private void loadServiciosExtras(List<Tour.ServicioExtra> servicios) {
        // Servicio 1
        if (servicios.size() > 0) {
            Tour.ServicioExtra servicio1 = servicios.get(0);
            tvNombreServicio1.setText(servicio1.getNombre() != null ? servicio1.getNombre() : "Servicio 1");
            tvPrecioServicio1.setText("Costo: " + (servicio1.getPrecio() != null ? servicio1.getPrecio() : "Gratis"));
            tvDescripcionServicio1.setText("Descripción: " + (servicio1.getDescripcion() != null ? servicio1.getDescripcion() : "Sin descripción"));
            imgServicio1.setImageResource(R.drawable.servicio_1);
        }
        
        // Servicio 2
        if (servicios.size() > 1) {
            Tour.ServicioExtra servicio2 = servicios.get(1);
            tvNombreServicio2.setText(servicio2.getNombre() != null ? servicio2.getNombre() : "Servicio 2");
            tvPrecioServicio2.setText("Costo: " + (servicio2.getPrecio() != null ? servicio2.getPrecio() : "Gratis"));
            tvDescripcionServicio2.setText("Descripción: " + (servicio2.getDescripcion() != null ? servicio2.getDescripcion() : "Sin descripción"));
            imgServicio2.setImageResource(R.drawable.servicio_2);
        } else {
            // Servicio 2 por defecto si solo hay 1 servicio
            tvNombreServicio2.setText("No especificado");
            tvPrecioServicio2.setText("Costo: -");
            tvDescripcionServicio2.setText("Descripción: No disponible");
            imgServicio2.setImageResource(R.drawable.servicio_2);
        }
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
        tvUbicacion.setText(ubicacion.getNombre() != null ? ubicacion.getNombre() : "Sin nombre");
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
        
        // Label de actividades
        TextView labelActividades = new TextView(this);
        labelActividades.setText("🚶 Actividades");
        labelActividades.setTextSize(14);
        labelActividades.setTextColor(getResources().getColor(R.color.black, null));
        LinearLayout.LayoutParams labelActParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelActParams.setMargins(0, 0, 0, (int) (4 * getResources().getDisplayMetrics().density));
        labelActividades.setLayoutParams(labelActParams);
        
        // TextView con las actividades
        TextView tvActividades = new TextView(this);
        tvActividades.setText(ubicacion.getActividades() != null && !ubicacion.getActividades().isEmpty() 
            ? ubicacion.getActividades() : "Sin actividades");
        tvActividades.setTextSize(12);
        tvActividades.setTextColor(getResources().getColor(R.color.gray, null));
        tvActividades.setBackgroundResource(R.drawable.rounded_edittext);
        tvActividades.setPadding(
            (int) (8 * getResources().getDisplayMetrics().density),
            (int) (8 * getResources().getDisplayMetrics().density),
            (int) (8 * getResources().getDisplayMetrics().density),
            (int) (8 * getResources().getDisplayMetrics().density)
        );
        
        rightLayout.addView(labelActividades);
        rightLayout.addView(tvActividades);
        
        // Agregar ambos layouts al row
        rowLayout.addView(leftLayout);
        rowLayout.addView(rightLayout);
        
        // Agregar el row al contenedor principal
        layoutUbicacionesContainer.addView(rowLayout);
    }
    
    private void loadDefaultLocations() {
        // Limpiar contenedor
        layoutUbicacionesContainer.removeAllViews();
        
        // Crear ubicaciones por defecto
        Tour.Ubicacion ubicacion1 = new Tour.Ubicacion();
        ubicacion1.setNombre("Plaza de Armas");
        ubicacion1.setActividades("Caminata guiada");
        addUbicacionView(1, ubicacion1);
        
        Tour.Ubicacion ubicacion2 = new Tour.Ubicacion();
        ubicacion2.setNombre("Río Urubamba");
        ubicacion2.setActividades("Canotaje y almuerzo");
        addUbicacionView(2, ubicacion2);
        
        Tour.Ubicacion ubicacion3 = new Tour.Ubicacion();
        ubicacion3.setNombre("Mercado Central");
        ubicacion3.setActividades("Compras y descanso");
        addUbicacionView(3, ubicacion3);
    }
    
    private void loadDefaultData() {
        // Cargar datos de ejemplo si no hay datos en Firestore
        tvHoraInicio.setText("8:00 AM");
        tvDuracion.setText("6 horas");
        tvCosto.setText("30 soles");
        tvIdiomas.setText("Español - Inglés");
        tvFechaTour.setText("Abril 24, 2025");
        
        loadDefaultServicesAndLocations();
    }
    
    private void loadDefaultServicesAndLocations() {
        // Servicio 1 - Desayuno
        tvNombreServicio1.setText("Desayuno");
        tvPrecioServicio1.setText("Costo: 30 soles por persona");
        tvDescripcionServicio1.setText("Descripción: Desayuno típico de la ciudad");
        imgServicio1.setImageResource(R.drawable.servicio_1);
        
        // Servicio 2 - Equipo
        tvNombreServicio2.setText("Equipo de canotaje");
        tvPrecioServicio2.setText("Costo: Gratis");
        tvDescripcionServicio2.setText("Descripción: Equipo de canotaje necesario para el tour");
        imgServicio2.setImageResource(R.drawable.servicio_2);
        
        // Cargar ubicaciones por defecto
        loadDefaultLocations();
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
