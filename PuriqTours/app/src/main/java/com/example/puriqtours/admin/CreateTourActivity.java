package com.example.puriqtours.admin;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Tour;
import com.example.puriqtours.helper.StorageHelper;
import com.example.puriqtours.helper.StorageManager;
import com.example.puriqtours.helper.FirestoreHelper;
import com.example.puriqtours.helper.TourConverter;
import com.example.puriqtours.entity.TourAdmin;

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
    private EditText etTituloTour, etHoraInicio, etHoraFin, etCosto, etIdiomas, etRegion, etLocation;
    private TextView tvCantidadServicios, tvImagenTourSeleccionada;
    private LinearLayout layoutServiciosExtra, layoutUbicaciones;
    private Button btnCrearTour, btnSeleccionarImagenTour;
    private Uri tourImageUri; // URI de la imagen del tour
    
    // Lista para manejar servicios extra dinámicos
    private List<ExtraService> serviciosExtra;
    private int contadorUbicaciones = 1;
    private int contadorServicios = 1;
    
    // Calendario para fecha
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    
    // Helpers
    private StorageHelper storageHelper;
    private FirestoreHelper firestoreHelper;
    private StorageManager storageManager;
    
    // Para selección de imagen
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private TextView currentImageTextView; // TextView que se está actualizando
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tour);

        // Inicializar variables
        serviciosExtra = new ArrayList<>();
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        storageHelper = new StorageHelper(this);
        firestoreHelper = new FirestoreHelper();
        storageManager = new StorageManager();
        
        // Configurar image picker launcher
        setupImagePickerLauncher();

        // Configurar toolbar
        setupToolbar();
        
        // Inicializar vistas
        initViews();
        
        // Configurar listeners
        setupListeners();
        
        // Agregar primera ubicación por defecto
        addUbicacionInput();
    }
    
    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null && currentImageTextView != null) {
                            currentImageTextView.setText("Imagen seleccionada ✓");
                            currentImageTextView.setTag(selectedImageUri); // Guardar URI en el tag
                            currentImageTextView.setTextColor(getResources().getColor(R.color.teal_700));
                            
                            // Si es la imagen del tour, guardarla en tourImageUri
                            if (currentImageTextView.getId() == R.id.tvImagenTourSeleccionada) {
                                tourImageUri = selectedImageUri;
                            }
                        }
                    }
                }
        );
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
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
        etTituloTour = findViewById(R.id.etTituloTour);
        etHoraInicio = findViewById(R.id.etHoraInicio);
        etHoraFin = findViewById(R.id.etHoraFin);
        etCosto = findViewById(R.id.etCosto);
        etIdiomas = findViewById(R.id.etIdiomas);
        etRegion = findViewById(R.id.etRegion);
        etLocation = findViewById(R.id.etLocation);
        tvCantidadServicios = findViewById(R.id.tvCantidadServicios);
        tvImagenTourSeleccionada = findViewById(R.id.tvImagenTourSeleccionada);
        layoutServiciosExtra = findViewById(R.id.layoutServiciosExtra);
        layoutUbicaciones = findViewById(R.id.layoutUbicaciones);
        btnCrearTour = findViewById(R.id.btnCrearTour);
        btnSeleccionarImagenTour = findViewById(R.id.btnSeleccionarImagenTour);
        
        // Actualizar texto inicial de servicios
        updateServiciosText();
    }

    private void setupListeners() {
        // Time picker para hora de inicio
        etHoraInicio.setOnClickListener(v -> showTimePickerInicio());
        
        // Time picker para hora de fin
        etHoraFin.setOnClickListener(v -> showTimePickerFin());
        
        // Agregar servicio extra - hacer clickeable todo el layout
        layoutServiciosExtra.setOnClickListener(v -> showExtraServiceDialog());
        
        // Seleccionar imagen del tour
        btnSeleccionarImagenTour.setOnClickListener(v -> {
            currentImageTextView = tvImagenTourSeleccionada;
            openImagePicker();
        });
        
        // Crear tour
        btnCrearTour.setOnClickListener(v -> createTour());
    }

    private void showTimePickerInicio() {
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

    private void showTimePickerFin() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, min) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, min);
                    etHoraFin.setText(time);
                }, hour, minute, true);
        timePickerDialog.show();
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

        // Listener para adjuntar imagen
        btnAdjuntarImagen.setOnClickListener(v -> {
            currentImageTextView = tvImagenSeleccionada;
            openImagePicker();
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
            TextView tvImagenSeleccionada = servicioView.findViewById(R.id.tvImagenSeleccionada);

            String nombre = etNombre.getText().toString().trim();
            String precio = etPrecio.getText().toString().trim();
            
            // Obtener URI de la imagen del tag (si fue seleccionada)
            Uri imageUri = (Uri) tvImagenSeleccionada.getTag();
            String imageUriString = imageUri != null ? imageUri.toString() : "";

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

            // Guardar servicio con la URI de la imagen (se subirá después al crear el tour)
            ExtraService service = new ExtraService(nombre, precio, "", imageUriString);
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
            // Recopilar datos del formulario
            String tituloTour = etTituloTour.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String region = etRegion.getText().toString().trim();
            String descripcion = getDescriptionSummary();
            String horaInicio = etHoraInicio.getText().toString();
            String horaFin = etHoraFin.getText().toString();
            String idiomas = etIdiomas.getText().toString();
            Float precio = Float.parseFloat(etCosto.getText().toString());
            
            // Obtener ID del admin/empresa logueado
            String idEmpresa = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
            
            // Crear objeto Tour para Firestore
            Tour nuevoTour = new Tour();
            nuevoTour.setTitle(tituloTour);
            nuevoTour.setLocation(location);
            nuevoTour.setRegion(region);
            nuevoTour.setDesc(descripcion);
            nuevoTour.setStartTime(horaInicio);
            nuevoTour.setEndTime(horaFin);
            nuevoTour.setIdiomas(idiomas);
            nuevoTour.setPrice(precio);
            nuevoTour.setStatus("Disponible");
            nuevoTour.setIdEmpresa(idEmpresa);
            nuevoTour.setImageUrl(""); // Se actualizará al subir la imagen
            nuevoTour.setRating(5); // Rating inicial
            
            // Mostrar diálogo de progreso
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Subiendo imágenes...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            
            // Primero subir imagen del tour, luego servicios
            if (tourImageUri != null) {
                uploadTourImageAndContinue(nuevoTour, idEmpresa, tituloTour);
            } else {
                // Sin imagen del tour, continuar con servicios
                uploadServiceImagesAndCreateTour(nuevoTour, idEmpresa, tituloTour);
            }
        }
    }
    
    private void uploadTourImageAndContinue(Tour nuevoTour, String idEmpresa, String tituloTour) {
        progressDialog.setMessage("Subiendo imagen del tour...");
        storageManager.uploadTourImage(tourImageUri, new StorageManager.OnImageUploadListener() {
            @Override
            public void onSuccess(String downloadUrl) {
                nuevoTour.setImageUrl(downloadUrl);
                // Continuar con imágenes de servicios
                uploadServiceImagesAndCreateTour(nuevoTour, idEmpresa, tituloTour);
            }

            @Override
            public void onError(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(CreateTourActivity.this, 
                    "Error al subir imagen del tour: " + errorMessage, 
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(int progress) {
                progressDialog.setMessage("Subiendo imagen del tour... " + progress + "%");
            }
        });
    }
    
    private void uploadServiceImagesAndCreateTour(Tour nuevoTour, String idEmpresa, String nombreTour) {
        List<Tour.ServicioExtra> serviciosExtras = new ArrayList<>();
        
        if (serviciosExtra.isEmpty()) {
            // No hay servicios, crear tour directamente
            nuevoTour.setServiciosExtras(serviciosExtras);
            saveTourToFirestore(nuevoTour, nombreTour);
            return;
        }
        
        // Subir imágenes de forma secuencial
        uploadNextServiceImage(0, serviciosExtras, nuevoTour, nombreTour);
    }
    
    private void uploadNextServiceImage(int index, List<Tour.ServicioExtra> serviciosExtras, Tour nuevoTour, String nombreTour) {
        if (index >= serviciosExtra.size()) {
            // Todas las imágenes subidas, guardar tour
            nuevoTour.setServiciosExtras(serviciosExtras);
            saveTourToFirestore(nuevoTour, nombreTour);
            return;
        }
        
        ExtraService servicio = serviciosExtra.get(index);
        Tour.ServicioExtra servicioExtra = new Tour.ServicioExtra();
        servicioExtra.setTitle(servicio.nombre);
        
        try {
            servicioExtra.setPrice(Float.parseFloat(servicio.precio));
        } catch (NumberFormatException e) {
            servicioExtra.setPrice(0f);
        }
        
        // Si hay URI de imagen, subirla
        if (servicio.imagen != null && !servicio.imagen.isEmpty()) {
            Uri imageUri = Uri.parse(servicio.imagen);
            storageManager.uploadExtraServiceImage(imageUri, new StorageManager.OnImageUploadListener() {
                @Override
                public void onSuccess(String downloadUrl) {
                    servicioExtra.setImageUrl(downloadUrl);
                    serviciosExtras.add(servicioExtra);
                    // Subir siguiente imagen
                    uploadNextServiceImage(index + 1, serviciosExtras, nuevoTour, nombreTour);
                }

                @Override
                public void onError(String errorMessage) {
                    // Guardar sin imagen
                    servicioExtra.setImageUrl("");
                    serviciosExtras.add(servicioExtra);
                    Toast.makeText(CreateTourActivity.this, "Error al subir imagen: " + errorMessage, Toast.LENGTH_SHORT).show();
                    // Continuar con siguiente
                    uploadNextServiceImage(index + 1, serviciosExtras, nuevoTour, nombreTour);
                }

                @Override
                public void onProgress(int progress) {
                    if (progressDialog != null) {
                        progressDialog.setMessage("Subiendo imagen " + (index + 1) + "/" + serviciosExtra.size() + "... " + progress + "%");
                    }
                }
            });
        } else {
            // No hay imagen, agregar sin imageUrl
            servicioExtra.setImageUrl("");
            serviciosExtras.add(servicioExtra);
            uploadNextServiceImage(index + 1, serviciosExtras, nuevoTour, nombreTour);
        }
    }
    
    private void saveTourToFirestore(Tour nuevoTour, String nombreTour) {
        if (progressDialog != null) {
            progressDialog.setMessage("Guardando tour...");
        }
        
        // Guardar en Firestore
        firestoreHelper.createTour(nuevoTour, (success, tourId) -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            
            if (success && tourId != null) {
                Toast.makeText(this, "¡Tour creado exitosamente!", Toast.LENGTH_SHORT).show();
                
                // Retornar a ToursActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("tour_created", true);
                resultIntent.putExtra("tour_name", nombreTour);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Error al crear tour", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private String getFirstLocationName() {
        if (layoutUbicaciones.getChildCount() > 0) {
            View firstLocation = layoutUbicaciones.getChildAt(0);
            EditText etNombre = firstLocation.findViewById(R.id.etNombreUbicacion);
            String nombre = etNombre.getText().toString().trim();
            return nombre.isEmpty() ? "Tour personalizado" : "Tour " + nombre;
        }
        return "Tour personalizado";
    }
    
    private String getDestinationSummary() {
        StringBuilder destinos = new StringBuilder();
        int count = Math.min(2, layoutUbicaciones.getChildCount()); // Máximo 2 ubicaciones en resumen
        
        for (int i = 0; i < count; i++) {
            View child = layoutUbicaciones.getChildAt(i);
            EditText etNombre = child.findViewById(R.id.etNombreUbicacion);
            String nombre = etNombre.getText().toString().trim();
            
            if (!nombre.isEmpty()) {
                if (destinos.length() > 0) {
                    destinos.append(", ");
                }
                destinos.append(nombre);
            }
        }
        
        if (layoutUbicaciones.getChildCount() > 2) {
            destinos.append(" y ").append(layoutUbicaciones.getChildCount() - 2).append(" más");
        }
        
        return destinos.length() > 0 ? destinos.toString() : "Múltiples destinos";
    }
    
    private String getDescriptionSummary() {
        StringBuilder descripcion = new StringBuilder();
        
        // Agregar información de servicios
        if (!serviciosExtra.isEmpty()) {
            descripcion.append("Incluye ").append(serviciosExtra.size()).append(" servicio(s) extra. ");
        }
        
        // Agregar primera actividad como ejemplo
        if (layoutUbicaciones.getChildCount() > 0) {
            View firstLocation = layoutUbicaciones.getChildAt(0);
            EditText etActividades = firstLocation.findViewById(R.id.etActividadesUbicacion);
            String actividades = etActividades.getText().toString().trim();
            
            if (!actividades.isEmpty()) {
                descripcion.append(actividades.length() > 100 ? 
                    actividades.substring(0, 97) + "..." : actividades);
            }
        }
        
        if (descripcion.length() == 0) {
            descripcion.append("Tour personalizado con actividades únicas");
        }
        
        return descripcion.toString();
    }

    private boolean validateForm() {
        if (etTituloTour.getText().toString().trim().isEmpty()) {
            etTituloTour.setError("El título del tour es obligatorio");
            etTituloTour.requestFocus();
            return false;
        }

        if (etHoraInicio.getText().toString().trim().isEmpty()) {
            etHoraInicio.setError("La hora de inicio es obligatoria");
            etHoraInicio.requestFocus();
            return false;
        }

        if (etHoraFin.getText().toString().trim().isEmpty()) {
            etHoraFin.setError("La hora de fin es obligatoria");
            etHoraFin.requestFocus();
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

        if (etRegion.getText().toString().trim().isEmpty()) {
            etRegion.setError("El departamento/región es obligatorio");
            etRegion.requestFocus();
            return false;
        }

        if (etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("La ubicación principal es obligatoria");
            etLocation.requestFocus();
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

            if (etActividades != null && etActividades.getText().toString().trim().isEmpty()) {
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
