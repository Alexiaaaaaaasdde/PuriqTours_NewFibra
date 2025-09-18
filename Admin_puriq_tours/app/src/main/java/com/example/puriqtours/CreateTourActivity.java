package com.example.puriqtours;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateTourActivity extends AppCompatActivity {

    // Campos del formulario
    private EditText etHoraInicio, etDuracion, etCosto, etIdiomas, etFechaTour;
    private TextView tvCantidadServicios;
    private LinearLayout layoutServiciosExtra, layoutUbicaciones;
    private Button btnCrearTour;
    
    // Lista para manejar servicios extra dinámicos
    private List<ExtraService> serviciosExtra;
    private int contadorUbicaciones = 1;
    private int contadorServicios = 1;
    
    // Calendario para fecha
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tour);

        // Inicializar variables
        serviciosExtra = new ArrayList<>();
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Configurar toolbar
        setupToolbar();
        
        // Inicializar vistas
        initViews();
        
        // Configurar listeners
        setupListeners();
        
        // Agregar primera ubicación por defecto
        addUbicacionInput();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Crear nuevo tour");
        }
    }

    private void initViews() {
        etHoraInicio = findViewById(R.id.etHoraInicio);
        etDuracion = findViewById(R.id.etDuracion);
        etCosto = findViewById(R.id.etCosto);
        etIdiomas = findViewById(R.id.etIdiomas);
        etFechaTour = findViewById(R.id.etFechaTour);
        tvCantidadServicios = findViewById(R.id.tvCantidadServicios);
        layoutServiciosExtra = findViewById(R.id.layoutServiciosExtra);
        layoutUbicaciones = findViewById(R.id.layoutUbicaciones);
        btnCrearTour = findViewById(R.id.btnCrearTour);
        
        // Actualizar texto inicial de servicios
        updateServiciosText();
    }

    private void setupListeners() {
        // Time picker para hora de inicio
        etHoraInicio.setOnClickListener(v -> showTimePicker());
        
        // Date picker para fecha del tour
        etFechaTour.setOnClickListener(v -> showDatePicker());
        
        // Agregar servicio extra - hacer clickeable todo el layout
        layoutServiciosExtra.setOnClickListener(v -> showExtraServiceDialog());
        
        // Crear tour
        btnCrearTour.setOnClickListener(v -> createTour());
    }

    private void showTimePicker() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, min) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, min);
                    etHoraInicio.setText(time);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    etFechaTour.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        // No permitir fechas pasadas
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showExtraServiceDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_extra_service, null);
        
        LinearLayout layoutServiciosDinamicos = dialogView.findViewById(R.id.layoutServiciosDinamicos);
        Button btnAgregarOtroServicio = dialogView.findViewById(R.id.btnAgregarOtroServicio);
        Button btnGuardarServicios = dialogView.findViewById(R.id.btnGuardarServicios);

        // Lista temporal para manejar servicios en el dialog
        List<View> serviciosViews = new ArrayList<>();
        
        // Agregar primer servicio por defecto
        addServicioToDialog(layoutServiciosDinamicos, serviciosViews, 1);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Botón para agregar otro servicio
        btnAgregarOtroServicio.setOnClickListener(v -> {
            contadorServicios++;
            addServicioToDialog(layoutServiciosDinamicos, serviciosViews, contadorServicios);
        });

        // Botón guardar servicios
        btnGuardarServicios.setOnClickListener(v -> {
            if (validateAndSaveServices(serviciosViews)) {
                updateServiciosText();
                dialog.dismiss();
                Toast.makeText(this, "Servicios guardados correctamente", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void addServicioToDialog(LinearLayout container, List<View> serviciosViews, int numeroServicio) {
        View servicioView = LayoutInflater.from(this).inflate(R.layout.item_servicio_extra, null);
        
        TextView tvNumeroServicio = servicioView.findViewById(R.id.tvNumeroServicio);
        Button btnEliminarServicio = servicioView.findViewById(R.id.btnEliminarServicio);
        Button btnAdjuntarImagen = servicioView.findViewById(R.id.btnAdjuntarImagen);
        TextView tvImagenSeleccionada = servicioView.findViewById(R.id.tvImagenSeleccionada);

        tvNumeroServicio.setText("Servicio " + numeroServicio);

        // Listener para eliminar servicio (solo si hay más de 1)
        btnEliminarServicio.setOnClickListener(v -> {
            if (serviciosViews.size() > 1) {
                container.removeView(servicioView);
                serviciosViews.remove(servicioView);
                updateServiciosNumbers(serviciosViews);
            } else {
                Toast.makeText(this, "Debe haber al menos un servicio", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener para adjuntar imagen (placeholder)
        btnAdjuntarImagen.setOnClickListener(v -> {
            tvImagenSeleccionada.setText("imagen_servicio_" + numeroServicio + ".jpg");
            Toast.makeText(this, "Funcionalidad de imagen pendiente", Toast.LENGTH_SHORT).show();
        });

        serviciosViews.add(servicioView);
        container.addView(servicioView);
    }

    private void updateServiciosNumbers(List<View> serviciosViews) {
        for (int i = 0; i < serviciosViews.size(); i++) {
            View servicioView = serviciosViews.get(i);
            TextView tvNumero = servicioView.findViewById(R.id.tvNumeroServicio);
            tvNumero.setText("Servicio " + (i + 1));
        }
        contadorServicios = serviciosViews.size();
    }

    private boolean validateAndSaveServices(List<View> serviciosViews) {
        serviciosExtra.clear(); // Limpiar lista anterior
        
        for (int i = 0; i < serviciosViews.size(); i++) {
            View servicioView = serviciosViews.get(i);
            EditText etNombre = servicioView.findViewById(R.id.etNombreServicio);
            EditText etPrecio = servicioView.findViewById(R.id.etPrecioServicio);
            EditText etDescripcion = servicioView.findViewById(R.id.etDescripcionServicio);

            String nombre = etNombre.getText().toString().trim();
            String precio = etPrecio.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();

            if (nombre.isEmpty()) {
                etNombre.setError("El nombre del servicio es obligatorio");
                etNombre.requestFocus();
                return false;
            }
            if (precio.isEmpty()) {
                etPrecio.setError("El precio del servicio es obligatorio");
                etPrecio.requestFocus();
                return false;
            }
            if (descripcion.isEmpty()) {
                etDescripcion.setError("La descripción del servicio es obligatoria");
                etDescripcion.requestFocus();
                return false;
            }

            ExtraService service = new ExtraService(nombre, precio, descripcion, "");
            serviciosExtra.add(service);
        }
        return true;
    }

    private void updateServiciosText() {
        if (serviciosExtra.isEmpty()) {
            tvCantidadServicios.setText("Haz click para agregar servicios extra");
        } else {
            tvCantidadServicios.setText(serviciosExtra.size() + " servicio(s) agregado(s)");
        }
    }

    private void addUbicacionInput() {
        View ubicacionView = LayoutInflater.from(this).inflate(R.layout.item_ubicacion_input, null);
        
        TextView tvNumeroUbicacion = ubicacionView.findViewById(R.id.tvNumeroUbicacion);
        EditText etNombreUbicacion = ubicacionView.findViewById(R.id.etNombreUbicacion);
        EditText etActividadesUbicacion = ubicacionView.findViewById(R.id.etActividadesUbicacion);
        Button btnAgregarUbicacion = ubicacionView.findViewById(R.id.btnAgregarUbicacion);
        Button btnEliminarUbicacion = ubicacionView.findViewById(R.id.btnEliminarUbicacion);

        tvNumeroUbicacion.setText("Ubicación " + contadorUbicaciones);
        
        // Listener para agregar nueva ubicación
        btnAgregarUbicacion.setOnClickListener(v -> {
            contadorUbicaciones++;
            addUbicacionInput();
        });

        // Listener para eliminar ubicación (solo si hay más de 1)
        btnEliminarUbicacion.setOnClickListener(v -> {
            if (layoutUbicaciones.getChildCount() > 1) {
                layoutUbicaciones.removeView(ubicacionView);
                updateUbicacionNumbers();
            } else {
                Toast.makeText(this, "Debe haber al menos una ubicación", Toast.LENGTH_SHORT).show();
            }
        });

        layoutUbicaciones.addView(ubicacionView);
    }

    private void updateUbicacionNumbers() {
        for (int i = 0; i < layoutUbicaciones.getChildCount(); i++) {
            View child = layoutUbicaciones.getChildAt(i);
            TextView tvNumero = child.findViewById(R.id.tvNumeroUbicacion);
            tvNumero.setText("Ubicación " + (i + 1));
        }
        contadorUbicaciones = layoutUbicaciones.getChildCount();
    }

    private void createTour() {
        if (validateForm()) {
            // Aquí procesarías los datos del tour
            Toast.makeText(this, "Tour creado exitosamente", Toast.LENGTH_SHORT).show();
            
            // Retornar a ToursActivity con resultado
            Intent resultIntent = new Intent();
            resultIntent.putExtra("tour_created", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private boolean validateForm() {
        if (etHoraInicio.getText().toString().trim().isEmpty()) {
            etHoraInicio.setError("La hora de inicio es obligatoria");
            etHoraInicio.requestFocus();
            return false;
        }

        if (etDuracion.getText().toString().trim().isEmpty()) {
            etDuracion.setError("La duración es obligatoria");
            etDuracion.requestFocus();
            return false;
        }

        if (etCosto.getText().toString().trim().isEmpty()) {
            etCosto.setError("El costo por persona es obligatorio");
            etCosto.requestFocus();
            return false;
        }

        if (etIdiomas.getText().toString().trim().isEmpty()) {
            etIdiomas.setError("Los idiomas ofrecidos son obligatorios");
            etIdiomas.requestFocus();
            return false;
        }

        if (etFechaTour.getText().toString().trim().isEmpty()) {
            etFechaTour.setError("La fecha del tour es obligatoria");
            etFechaTour.requestFocus();
            return false;
        }

        // Validar que todas las ubicaciones tengan datos
        for (int i = 0; i < layoutUbicaciones.getChildCount(); i++) {
            View child = layoutUbicaciones.getChildAt(i);
            EditText etNombre = child.findViewById(R.id.etNombreUbicacion);
            EditText etActividades = child.findViewById(R.id.etActividadesUbicacion);

            if (etNombre.getText().toString().trim().isEmpty()) {
                etNombre.setError("El nombre de la ubicación es obligatorio");
                etNombre.requestFocus();
                return false;
            }

            if (etActividades.getText().toString().trim().isEmpty()) {
                etActividades.setError("Las actividades son obligatorias");
                etActividades.requestFocus();
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Clase auxiliar para servicios extra
    private static class ExtraService {
        String nombre;
        String precio;
        String descripcion;
        String imagen;

        ExtraService(String nombre, String precio, String descripcion, String imagen) {
            this.nombre = nombre;
            this.precio = precio;
            this.descripcion = descripcion;
            this.imagen = imagen;
        }
    }
}
