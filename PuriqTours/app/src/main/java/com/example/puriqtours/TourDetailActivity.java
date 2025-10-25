package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

public class TourDetailActivity extends AppCompatActivity {

    private TextView tvHoraInicio, tvDuracion, tvCosto, tvIdiomas, tvFechaTour;
    private TextView tvNombreServicio1, tvPrecioServicio1, tvDescripcionServicio1;
    private TextView tvNombreServicio2, tvPrecioServicio2, tvDescripcionServicio2;
    private ImageView imgServicio1, imgServicio2;
    private TextView tvUbicacion1, tvActividades1, tvUbicacion2, tvActividades2, tvUbicacion3, tvActividades3;
    private Button btnCerrar, btnEditarTour;
    
    private int tourId;
    private String tourName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_detail);

        // Configurar toolbar
        setupToolbar();
        
        // Inicializar vistas
        initViews();
        
        // Cargar datos del tour
        loadTourData();
        
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
        
        // Ubicaciones
        tvUbicacion1 = findViewById(R.id.tvUbicacion1);
        tvActividades1 = findViewById(R.id.tvActividades1);
        tvUbicacion2 = findViewById(R.id.tvUbicacion2);
        tvActividades2 = findViewById(R.id.tvActividades2);
        tvUbicacion3 = findViewById(R.id.tvUbicacion3);
        tvActividades3 = findViewById(R.id.tvActividades3);
        
        // Botones
        btnCerrar = findViewById(R.id.btnCerrar);
        btnEditarTour = findViewById(R.id.btnEditarTour);
    }

    private void loadTourData() {
        // Obtener datos del intent
        tourId = getIntent().getIntExtra("tour_id", 1);
        tourName = getIntent().getStringExtra("tour_name");

        if (tourName != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(tourName);
        }

        // Cargar datos de ejemplo (en una implementación real vendrían de la base de datos)
        
        // Datos básicos del tour
        tvHoraInicio.setText("8:00 AM");
        tvDuracion.setText("6 horas");
        tvCosto.setText("30 soles");
        tvIdiomas.setText("Español - Inglés");
        tvFechaTour.setText("Abril 24, 2025");
        
        // Servicio 1 - Canotaje
        tvNombreServicio1.setText("Desayuno");
        tvPrecioServicio1.setText("Costo: 30 soles por persona");
        tvDescripcionServicio1.setText("Descripción: Desayuno típico de la ciudad");
        imgServicio1.setImageResource(R.drawable.servicio_1);
        
        // Servicio 2 - Canopi
        tvNombreServicio2.setText("Equipo de canotaje");
        tvPrecioServicio2.setText("Costo: Gratis");
        tvDescripcionServicio2.setText("Descripción: Equipo de canotaje necesario para el tour");
        imgServicio2.setImageResource(R.drawable.servicio_2);
        
        // Ubicaciones y actividades
        tvUbicacion1.setText("Plaza de Armas");
        tvActividades1.setText("Caminata guiada");
        
        tvUbicacion2.setText("Río Urubamba");
        tvActividades2.setText("Canotaje y almuerzo");
        
        tvUbicacion3.setText("Mercado Central");
        tvActividades3.setText("Compras y descanso");
    }

    private void setupListeners() {
        // Botón cerrar
        btnCerrar.setOnClickListener(v -> finish());
        
        // Botón editar tour
        btnEditarTour.setOnClickListener(v -> {
            Intent intent = new Intent(TourDetailActivity.this, EditTourActivity.class);
            intent.putExtra("tour_id", tourId);
            intent.putExtra("tour_name", tourName);
            startActivityForResult(intent, 300);
        });
        
        // Toolbar back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 300 && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("tour_updated", false)) {
                Toast.makeText(this, "Tour actualizado correctamente", Toast.LENGTH_SHORT).show();
                
                // Recargar los datos del tour
                loadTourData();
                
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
