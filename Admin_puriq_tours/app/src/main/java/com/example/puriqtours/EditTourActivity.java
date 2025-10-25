package com.example.puriqtours;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class EditTourActivity extends AppCompatActivity {

    private TextInputEditText etHoraInicio, etDuracion, etCosto, etIdiomas, etFechaTour;
    private LinearLayout layoutServicios, layoutUbicaciones;
    private Button btnAgregarServicio, btnAgregarRuta, btnGuardarTour, btnCancelar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tour);

        initViews();
        setupToolbar();
        loadExistingData();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etHoraInicio = findViewById(R.id.etHoraInicio);
        etDuracion = findViewById(R.id.etDuracion);
        etCosto = findViewById(R.id.etCosto);
        etIdiomas = findViewById(R.id.etIdiomas);
        etFechaTour = findViewById(R.id.etFechaTour);
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
        // Cargar datos existentes del tour
        etHoraInicio.setText("8:00 AM");
        etDuracion.setText("6 horas");
        etCosto.setText("30 soles");
        etIdiomas.setText("Español - Inglés");
        etFechaTour.setText("Abril 24, 2025");

        // Agregar servicios existentes
        addExistingServicio("Desayuno", "30 soles por persona", "Desayuno típico de la ciudad");
        addExistingServicio("Equipo de canotaje", "Gratis", "Equipo de canotaje necesario para el tour");

        // Agregar ubicaciones existentes
        addExistingUbicacion("Ubicación 1", "Caminata");
        addExistingUbicacion("Ubicación 2", "Almuerzo");
        addExistingUbicacion("Ubicación 3", "Caminata");
    }

    private void addExistingServicio(String nombre, String precio, String descripcion) {
        View servicioView = LayoutInflater.from(this).inflate(R.layout.item_servicio_extra, layoutServicios, false);
        
        EditText etNombre = servicioView.findViewById(R.id.etNombreServicio);
        EditText etPrecio = servicioView.findViewById(R.id.etPrecioServicio);
        EditText etDescripcion = servicioView.findViewById(R.id.etDescripcionServicio);
        Button btnEliminar = servicioView.findViewById(R.id.btnEliminarServicio);

        etNombre.setText(nombre);
        etPrecio.setText(precio);
        etDescripcion.setText(descripcion);

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
        etHoraInicio.setOnClickListener(v -> showTimePicker());
        etFechaTour.setOnClickListener(v -> showDatePicker());
        
        btnAgregarServicio.setOnClickListener(v -> showAgregarServicioDialog());
        btnAgregarRuta.setOnClickListener(v -> agregarNuevaUbicacion());
        
        btnGuardarTour.setOnClickListener(v -> guardarTour());
        btnCancelar.setOnClickListener(v -> onBackPressed());
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    String amPm = hourOfDay >= 12 ? "PM" : "AM";
                    int displayHour = hourOfDay > 12 ? hourOfDay - 12 : hourOfDay;
                    if (displayHour == 0) displayHour = 12;
                    
                    String time = String.format("%d:%02d %s", displayHour, minuteOfHour, amPm);
                    etHoraInicio.setText(time);
                }, hour, minute, false);

        timePickerDialog.show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
                    
                    String fecha = meses[selectedMonth] + " " + selectedDay + ", " + selectedYear;
                    etFechaTour.setText(fecha);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showAgregarServicioDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar servicio extra");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_extra_service, null);
        builder.setView(dialogView);

        EditText etNombre = dialogView.findViewById(R.id.etNombreServicio);
        EditText etPrecio = dialogView.findViewById(R.id.etPrecioServicio);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcionServicio);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombre = etNombre.getText().toString().trim();
            String precio = etPrecio.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();

            if (!nombre.isEmpty() && !precio.isEmpty() && !descripcion.isEmpty()) {
                agregarServicioExtra(nombre, precio, descripcion);
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
        EditText etDescripcion = servicioView.findViewById(R.id.etDescripcionServicio);
        Button btnEliminar = servicioView.findViewById(R.id.btnEliminarServicio);

        etNombre.setText(nombre);
        etPrecio.setText(precio);
        etDescripcion.setText(descripcion);

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
            etDuracion.getText().toString().trim().isEmpty() ||
            etCosto.getText().toString().trim().isEmpty() ||
            etIdiomas.getText().toString().trim().isEmpty() ||
            etFechaTour.getText().toString().trim().isEmpty()) {
            
            Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (layoutUbicaciones.getChildCount() == 0) {
            Toast.makeText(this, "Agrega al menos una ubicación para la ruta", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simular guardado exitoso
        Toast.makeText(this, "Tour actualizado exitosamente", Toast.LENGTH_SHORT).show();
        
        // Volver a la actividad anterior
        Intent resultIntent = new Intent();
        resultIntent.putExtra("tour_updated", true);
        setResult(RESULT_OK, resultIntent);
        finish();
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
