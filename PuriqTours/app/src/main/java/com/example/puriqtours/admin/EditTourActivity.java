package com.example.puriqtours.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Tour;
import com.example.puriqtours.helper.FirestoreHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

import java.util.Calendar;

public class EditTourActivity extends AppCompatActivity {

    private TextInputEditText etHoraInicio, etHoraFin, etCosto, etIdiomas, etRegion, etLocation;
    private LinearLayout layoutServicios, layoutUbicaciones;
    private Button btnAgregarServicio, btnAgregarRuta, btnGuardarTour, btnCancelar;
    private Toolbar toolbar;
    
    private FirestoreHelper firestoreHelper;
    private String tourId;
    private Tour currentTour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tour);

        firestoreHelper = new FirestoreHelper();
        tourId = getIntent().getStringExtra("tourId");
        
        if (tourId == null || tourId.isEmpty()) {
            Toast.makeText(this, "Error: ID de tour no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadExistingData();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etHoraInicio = findViewById(R.id.etHoraInicio);
        etHoraFin = findViewById(R.id.etHoraFin);
        etCosto = findViewById(R.id.etCosto);
        etIdiomas = findViewById(R.id.etIdiomas);
        etRegion = findViewById(R.id.etRegion);
        etLocation = findViewById(R.id.etLocation);
        layoutServicios = findViewById(R.id.layoutServicios);
        layoutUbicaciones = findViewById(R.id.layoutUbicaciones);
        btnAgregarServicio = findViewById(R.id.btnAgregarServicio);
        btnAgregarRuta = findViewById(R.id.btnAgregarRuta);
        btnGuardarTour = findViewById(R.id.btnGuardarTour);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Editar tour");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadExistingData() {
        // Cargar datos del tour desde Firestore
        firestoreHelper.loadTourById(tourId, tour -> {
            if (tour != null) {
                currentTour = tour;
                displayTourData(tour);
            } else {
                Toast.makeText(EditTourActivity.this, "Error al cargar el tour", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void displayTourData(Tour tour) {
        etHoraInicio.setText(tour.getStartTime() != null ? tour.getStartTime() : "");
        etHoraFin.setText(tour.getEndTime() != null ? tour.getEndTime() : "");
        etCosto.setText(tour.getPrice() != null ? String.valueOf(tour.getPrice()) : "");
        etIdiomas.setText(tour.getIdiomas() != null ? tour.getIdiomas() : "");
        etRegion.setText(tour.getRegion() != null ? tour.getRegion() : "");
        etLocation.setText(tour.getLocation() != null ? tour.getLocation() : "");

        // Agregar servicios existentes si los hay
        if (tour.getServiciosExtras() != null && !tour.getServiciosExtras().isEmpty()) {
            for (Tour.ServicioExtra servicio : tour.getServiciosExtras()) {
                addExistingServicio(servicio.getTitle(), String.valueOf(servicio.getPrice()), "");
            }
        }

        // Agregar ubicaciones/ruta existente si las hay
        if (tour.getRuta() != null && !tour.getRuta().isEmpty()) {
            for (Tour.Ubicacion ubicacion : tour.getRuta()) {
                addExistingUbicacion(ubicacion.getTitle(), "");
            }
        }
    }

    private void addExistingServicio(String nombre, String precio, String descripcion) {
        View servicioView = LayoutInflater.from(this).inflate(R.layout.item_servicio_extra, layoutServicios, false);
        
        EditText etNombre = servicioView.findViewById(R.id.etNombreServicio);
        EditText etPrecio = servicioView.findViewById(R.id.etPrecioServicio);
        Button btnEliminar = servicioView.findViewById(R.id.btnEliminarServicio);

        etNombre.setText(nombre);
        etPrecio.setText(precio);
        // Descripción ya no se usa

        btnEliminar.setOnClickListener(v -> layoutServicios.removeView(servicioView));

        layoutServicios.addView(servicioView);
    }

    private void addExistingUbicacion(String ubicacion, String actividades) {
        View ubicacionView = LayoutInflater.from(this).inflate(R.layout.item_ubicacion_input, layoutUbicaciones, false);
        
        EditText etUbicacion = ubicacionView.findViewById(R.id.etNombreUbicacion);
        EditText etActividades = ubicacionView.findViewById(R.id.etActividadesUbicacion);
        Button btnEliminar = ubicacionView.findViewById(R.id.btnEliminarUbicacion);

        etUbicacion.setText(ubicacion);
        etActividades.setText(actividades);

        btnEliminar.setOnClickListener(v -> layoutUbicaciones.removeView(ubicacionView));

        layoutUbicaciones.addView(ubicacionView);
    }

    private void setupClickListeners() {
        etHoraInicio.setOnClickListener(v -> showTimePickerInicio());
        etHoraFin.setOnClickListener(v -> showTimePickerFin());
        
        btnAgregarServicio.setOnClickListener(v -> showAgregarServicioDialog());
        btnAgregarRuta.setOnClickListener(v -> agregarNuevaUbicacion());
        
        btnGuardarTour.setOnClickListener(v -> guardarTour());
        btnCancelar.setOnClickListener(v -> onBackPressed());
    }

    private void showTimePickerInicio() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    etHoraInicio.setText(time);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private void showTimePickerFin() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    etHoraFin.setText(time);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private void showAgregarServicioDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar servicio extra");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_extra_service, null);
        builder.setView(dialogView);

        EditText etNombre = dialogView.findViewById(R.id.etNombreServicio);
        EditText etPrecio = dialogView.findViewById(R.id.etPrecioServicio);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombre = etNombre.getText().toString().trim();
            String precio = etPrecio.getText().toString().trim();

            if (!nombre.isEmpty() && !precio.isEmpty()) {
                agregarServicioExtra(nombre, precio, "");
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void agregarServicioExtra(String nombre, String precio, String descripcion) {
        View servicioView = LayoutInflater.from(this).inflate(R.layout.item_servicio_extra, layoutServicios, false);
        
        EditText etNombre = servicioView.findViewById(R.id.etNombreServicio);
        EditText etPrecio = servicioView.findViewById(R.id.etPrecioServicio);
        Button btnEliminar = servicioView.findViewById(R.id.btnEliminarServicio);

        etNombre.setText(nombre);
        etPrecio.setText(precio);
        // Descripción ya no se usa

        btnEliminar.setOnClickListener(v -> layoutServicios.removeView(servicioView));

        layoutServicios.addView(servicioView);
    }

    private void agregarNuevaUbicacion() {
        View ubicacionView = LayoutInflater.from(this).inflate(R.layout.item_ubicacion_input, layoutUbicaciones, false);
        
        Button btnEliminar = ubicacionView.findViewById(R.id.btnEliminarUbicacion);
        btnEliminar.setOnClickListener(v -> layoutUbicaciones.removeView(ubicacionView));

        layoutUbicaciones.addView(ubicacionView);
    }

    private void guardarTour() {
        // Validar campos obligatorios
        if (etHoraInicio.getText().toString().trim().isEmpty() ||
            etHoraFin.getText().toString().trim().isEmpty() ||
            etCosto.getText().toString().trim().isEmpty() ||
            etIdiomas.getText().toString().trim().isEmpty() ||
            etRegion.getText().toString().trim().isEmpty() ||
            etLocation.getText().toString().trim().isEmpty()) {
            
            Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (layoutUbicaciones.getChildCount() == 0) {
            Toast.makeText(this, "Agrega al menos una ubicación para la ruta", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar datos del tour actual
        currentTour.setStartTime(etHoraInicio.getText().toString().trim());
        currentTour.setEndTime(etHoraFin.getText().toString().trim());
        
        // Convertir costo a Float
        String costoStr = etCosto.getText().toString().trim();
        try {
            Float costo = Float.parseFloat(costoStr);
            currentTour.setPrice(costo);
        } catch (NumberFormatException e) {
            currentTour.setPrice(0.0f);
        }
        
        currentTour.setIdiomas(etIdiomas.getText().toString().trim());
        currentTour.setRegion(etRegion.getText().toString().trim());
        currentTour.setLocation(etLocation.getText().toString().trim());
        
        // Recopilar servicios extras
        java.util.List<Tour.ServicioExtra> servicios = new java.util.ArrayList<>();
        for (int i = 0; i < layoutServicios.getChildCount(); i++) {
            View servicioView = layoutServicios.getChildAt(i);
            EditText etNombre = servicioView.findViewById(R.id.etNombreServicio);
            EditText etPrecio = servicioView.findViewById(R.id.etPrecioServicio);
            
            String nombre = etNombre.getText().toString().trim();
            String precioStr = etPrecio.getText().toString().trim();
            
            if (!nombre.isEmpty() && !precioStr.isEmpty()) {
                Tour.ServicioExtra servicio = new Tour.ServicioExtra();
                servicio.setTitle(nombre);
                try {
                    servicio.setPrice(Float.parseFloat(precioStr));
                } catch (NumberFormatException e) {
                    servicio.setPrice(0.0f);
                }
                servicio.setImageUrl(""); // Placeholder
                servicios.add(servicio);
            }
        }
        currentTour.setServiciosExtras(servicios);
        
        // Recopilar ruta/ubicaciones
        java.util.List<Tour.Ubicacion> ruta = new java.util.ArrayList<>();
        for (int i = 0; i < layoutUbicaciones.getChildCount(); i++) {
            View ubicacionView = layoutUbicaciones.getChildAt(i);
            EditText etUbicacion = ubicacionView.findViewById(R.id.etNombreUbicacion);
            
            String nombreUbi = etUbicacion.getText().toString().trim();
            
            if (!nombreUbi.isEmpty()) {
                Tour.Ubicacion ubicacion = new Tour.Ubicacion();
                ubicacion.setTitle(nombreUbi);
                ubicacion.setOrder(i + 1);
                ubicacion.setLat(0.0); // Placeholder
                ubicacion.setLng(0.0); // Placeholder
                ruta.add(ubicacion);
            }
        }
        currentTour.setRuta(ruta);

        // Guardar en Firestore
        firestoreHelper.updateTour(tourId, currentTour, success -> {
            if (success) {
                Toast.makeText(EditTourActivity.this, "Tour actualizado exitosamente", Toast.LENGTH_SHORT).show();
                
                Intent resultIntent = new Intent();
                resultIntent.putExtra("tour_updated", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(EditTourActivity.this, "Error al actualizar el tour", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("¿Descartar cambios?")
                .setMessage("¿Estás seguro que quieres salir sin guardar los cambios?")
                .setPositiveButton("Sí, salir", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
