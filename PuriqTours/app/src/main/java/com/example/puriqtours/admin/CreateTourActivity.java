package com.example.puriqtours.admin;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Tour;
import com.example.puriqtours.helper.StorageHelper;
import com.example.puriqtours.helper.StorageManager;
import com.example.puriqtours.helper.FirestoreHelper;
import com.example.puriqtours.helper.TourConverter;
import com.example.puriqtours.entity.TourAdmin;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.textfield.TextInputEditText;
import java.io.IOException;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateTourActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "CreateTourActivity";
    
    // Campos del formulario
    private EditText etTituloTour, etHoraInicio, etHoraFin, etCosto, etIdiomas, etRegion, etLocation;
    private TextView tvCantidadServicios, tvImagenTourSeleccionada;
    private LinearLayout layoutServiciosExtra, layoutUbicaciones;
    private Button btnCrearTour, btnSeleccionarImagenTour, btnAgregarUbicacion;
    private Uri tourImageUri;
    
    // Google Maps
    private GoogleMap mMap;
    private Geocoder geocoder;
    private List<RouteLocation> routeLocations;
    private boolean isSelectingOnMap = false;
    private View currentLocationView;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    
    // Lista para manejar servicios extra dinámicos
    private List<ExtraService> serviciosExtra;
    private int contadorUbicaciones = 0;
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
        routeLocations = new ArrayList<>();
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        storageHelper = new StorageHelper(this);
        firestoreHelper = new FirestoreHelper();
        storageManager = new StorageManager();
        
        // Inicializar Geocoder
        geocoder = new Geocoder(this, new Locale("es", "PE"));
        
        // Configurar image picker launcher
        setupImagePickerLauncher();

        // Configurar toolbar
        setupToolbar();
        
        // Inicializar vistas
        initViews();
        
        // Configurar listeners
        setupListeners();
        
        // Configurar mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Configurar mapa centrado en Perú
        LatLng peru = new LatLng(-9.19, -75.0152);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(peru, 5));
        
        // Configurar click en el mapa
        mMap.setOnMapClickListener(latLng -> {
            if (isSelectingOnMap && currentLocationView != null) {
                onMapLocationSelected(latLng);
            }
        });
        
        Log.d(TAG, "Mapa listo");
    }
    
    private void onMapLocationSelected(LatLng latLng) {
        // Encontrar el índice de la ubicación actual
        int locationIndex = layoutUbicaciones.indexOfChild(currentLocationView);
        if (locationIndex >= 0) {
            // Actualizar la ubicación en la lista
            RouteLocation location;
            if (locationIndex < routeLocations.size()) {
                location = routeLocations.get(locationIndex);
            } else {
                location = new RouteLocation();
                routeLocations.add(location);
            }
            
            location.setLatLng(latLng);
            location.setOrder(locationIndex + 1);
            
            // Actualizar UI del item
            TextView tvUbicacionSel = currentLocationView.findViewById(R.id.tvUbicacionSeleccionada);
            TextView tvCoordenadas = currentLocationView.findViewById(R.id.tvCoordenadas);
            
            tvUbicacionSel.setText("📌 Lat: " + String.format("%.6f", latLng.latitude) + 
                                 ", Lng: " + String.format("%.6f", latLng.longitude));
            tvUbicacionSel.setVisibility(View.VISIBLE);
            tvCoordenadas.setText("Lat: " + latLng.latitude + ", Lng: " + latLng.longitude);
            
            // Actualizar mapa
            updateMapMarkers();
            
            // Desactivar modo selección
            isSelectingOnMap = false;
            currentLocationView = null;
            
            Toast.makeText(this, "Ubicación seleccionada en el mapa", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateMapMarkers() {
        if (mMap == null) return;
        
        // Limpiar mapa
        mMap.clear();
        
        // Agregar marcadores para cada ubicación válida
        List<LatLng> validLocations = new ArrayList<>();
        
        for (int i = 0; i < routeLocations.size(); i++) {
            RouteLocation location = routeLocations.get(i);
            if (location.getLatLng() != null) {
                // Agregar marcador
                MarkerOptions markerOptions = new MarkerOptions()
                    .position(location.getLatLng())
                    .title(location.getTitle() != null ? location.getTitle() : "Ubicación " + (i + 1));
                
                Marker marker = mMap.addMarker(markerOptions);
                location.setMarker(marker);
                validLocations.add(location.getLatLng());
            }
        }
        
        // Dibujar líneas conectando las ubicaciones en orden
        if (validLocations.size() > 1) {
            PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(validLocations)
                .color(Color.parseColor("#009688"))
                .width(8f);
            mMap.addPolyline(polylineOptions);
        }
        
        // Ajustar cámara para mostrar todas las ubicaciones
        if (!validLocations.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : validLocations) {
                builder.include(latLng);
            }
            try {
                LatLngBounds bounds = builder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            } catch (Exception e) {
                Log.e(TAG, "Error ajustando cámara", e);
            }
        }
    }
    
    private void initViews() {
        // Campos básicos
        etTituloTour = findViewById(R.id.etTituloTour);
        etHoraInicio = findViewById(R.id.etHoraInicio);
        etHoraFin = findViewById(R.id.etHoraFin);
        etCosto = findViewById(R.id.etCosto);
        etIdiomas = findViewById(R.id.etIdiomas);
        etRegion = findViewById(R.id.etRegion);
        etLocation = findViewById(R.id.etLocation);
        
        // Servicios extra
        tvCantidadServicios = findViewById(R.id.tvCantidadServicios);
        layoutServiciosExtra = findViewById(R.id.layoutServiciosExtra);
        
        // Imagen del tour
        btnSeleccionarImagenTour = findViewById(R.id.btnSeleccionarImagenTour);
        tvImagenTourSeleccionada = findViewById(R.id.tvImagenTourSeleccionada);
        
        // Ubicaciones
        layoutUbicaciones = findViewById(R.id.layoutUbicaciones);
        btnAgregarUbicacion = findViewById(R.id.btnAgregarUbicacion);
        
        // Botón crear
        btnCrearTour = findViewById(R.id.btnCrearTour);
    }
    
    private void setupListeners() {
        // Listeners de hora
        etHoraInicio.setOnClickListener(v -> showTimePicker(etHoraInicio));
        etHoraFin.setOnClickListener(v -> showTimePicker(etHoraFin));
        
        // Listener para servicios extra
        tvCantidadServicios.setOnClickListener(v -> showServiciosDialog());
        
        // Listener para imagen del tour
        btnSeleccionarImagenTour.setOnClickListener(v -> {
            currentImageTextView = tvImagenTourSeleccionada;
            openImagePicker();
        });
        
        // Listener para agregar ubicación
        btnAgregarUbicacion.setOnClickListener(v -> addUbicacionInput());
        
        // Listener para crear tour
        btnCrearTour.setOnClickListener(v -> createTour());
    }
    
    private void showTimePicker(EditText editText) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, min) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, min);
                    editText.setText(time);
                }, hour, minute, true);
        timePickerDialog.show();
    }
    
    private void showServiciosDialog() {
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
        View ubicacionView = LayoutInflater.from(this).inflate(R.layout.item_ubicacion_map_input, layoutUbicaciones, false);
        
        TextView tvNumero = ubicacionView.findViewById(R.id.tvNumeroUbicacion);
        TextInputEditText etTitulo = ubicacionView.findViewById(R.id.etTituloUbicacion);
        AutoCompleteTextView actvBuscar = ubicacionView.findViewById(R.id.actvBuscarUbicacion);
        Button btnSeleccionarMapa = ubicacionView.findViewById(R.id.btnSeleccionarEnMapa);
        Button btnEliminar = ubicacionView.findViewById(R.id.btnEliminarUbicacion);
        Button btnMoveUp = ubicacionView.findViewById(R.id.btnMoveUp);
        Button btnMoveDown = ubicacionView.findViewById(R.id.btnMoveDown);
        TextView tvUbicacionSel = ubicacionView.findViewById(R.id.tvUbicacionSeleccionada);
        TextView tvCoordenadas = ubicacionView.findViewById(R.id.tvCoordenadas);
        
        int currentPosition = layoutUbicaciones.getChildCount();
        tvNumero.setText("Ubicación " + (currentPosition + 1));
        
        // Crear ubicación en la lista
        RouteLocation newLocation = new RouteLocation();
        newLocation.setOrder(currentPosition + 1);
        routeLocations.add(newLocation);
        
        // Configurar autocomplete con Places API
        setupPlacesAutocomplete(actvBuscar, ubicacionView, currentPosition);
        
        // Botón seleccionar en mapa
        btnSeleccionarMapa.setOnClickListener(v -> {
            isSelectingOnMap = true;
            currentLocationView = ubicacionView;
            Toast.makeText(this, "Toca en el mapa para seleccionar la ubicación", Toast.LENGTH_LONG).show();
        });
        
        // Botón eliminar
        btnEliminar.setOnClickListener(v -> {
            if (layoutUbicaciones.getChildCount() > 0) {
                int index = layoutUbicaciones.indexOfChild(ubicacionView);
                layoutUbicaciones.removeView(ubicacionView);
                if (index < routeLocations.size()) {
                    RouteLocation removed = routeLocations.remove(index);
                    if (removed.getMarker() != null) {
                        removed.getMarker().remove();
                    }
                }
                updateUbicacionNumbers();
                updateMapMarkers();
                Toast.makeText(this, "Ubicación eliminada", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Botón mover arriba
        btnMoveUp.setOnClickListener(v -> moveLocation(ubicacionView, -1));
        
        // Botón mover abajo
        btnMoveDown.setOnClickListener(v -> moveLocation(ubicacionView, 1));
        
        // Listener para actualizar título en la ubicación
        etTitulo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int index = layoutUbicaciones.indexOfChild(ubicacionView);
                if (index >= 0 && index < routeLocations.size()) {
                    routeLocations.get(index).setTitle(s.toString());
                    updateMapMarkers();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        layoutUbicaciones.addView(ubicacionView);
        contadorUbicaciones++;
    }
    
    private void setupPlacesAutocomplete(AutoCompleteTextView actvBuscar, View ubicacionView, int position) {
        actvBuscar.setThreshold(3);
        
        actvBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                if (s.length() >= 3) {
                    searchRunnable = () -> searchPlaces(s.toString(), actvBuscar, ubicacionView);
                    searchHandler.postDelayed(searchRunnable, 500);
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        actvBuscar.setOnItemClickListener((parent, view, itemPosition, id) -> {
            // Recuperar la lista original de Address
            @SuppressWarnings("unchecked")
            List<Address> addresses = (List<Address>) actvBuscar.getTag(R.id.actvBuscarUbicacion);
            
            if (addresses != null && itemPosition < addresses.size()) {
                Address address = addresses.get(itemPosition);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                String placeName = address.getFeatureName() != null ? address.getFeatureName() : address.getLocality();
                updateLocationFromSearch(ubicacionView, latLng, placeName);
            }
        });
    }
    
    private void searchPlaces(String query, AutoCompleteTextView actvBuscar, View ubicacionView) {
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(query + ", Perú", 5);
                
                if (addresses != null && !addresses.isEmpty()) {
                    // Crear lista de strings formateados
                    List<String> addressStrings = new ArrayList<>();
                    for (Address address : addresses) {
                        StringBuilder builder = new StringBuilder();
                        if (address.getFeatureName() != null) {
                            builder.append(address.getFeatureName());
                        }
                        if (address.getLocality() != null) {
                            if (builder.length() > 0) builder.append(", ");
                            builder.append(address.getLocality());
                        }
                        if (address.getAdminArea() != null) {
                            if (builder.length() > 0) builder.append(", ");
                            builder.append(address.getAdminArea());
                        }
                        addressStrings.add(builder.toString());
                    }
                    
                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            addressStrings
                        );
                        
                        actvBuscar.setAdapter(adapter);
                        actvBuscar.setTag(R.id.actvBuscarUbicacion, addresses); // Guardar lista de Address original
                        adapter.notifyDataSetChanged();
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Error buscando lugares con Geocoder", e);
                runOnUiThread(() -> 
                    Toast.makeText(this, "Error buscando ubicaciones", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
    
    private void updateLocationFromSearch(View ubicacionView, LatLng latLng, String placeName) {
        TextView tvCoordenadas = ubicacionView.findViewById(R.id.tvCoordenadas);
        TextView tvUbicacionSel = ubicacionView.findViewById(R.id.tvUbicacionSeleccionada);
        TextInputEditText etTitulo = ubicacionView.findViewById(R.id.etTituloUbicacion);
        
        tvCoordenadas.setText("Lat: " + latLng.latitude + ", Lng: " + latLng.longitude);
        tvUbicacionSel.setText("📌 " + placeName);
        tvUbicacionSel.setVisibility(View.VISIBLE);
        
        // Si no hay título, usar el nombre del lugar
        if (etTitulo.getText() == null || etTitulo.getText().toString().trim().isEmpty()) {
            etTitulo.setText(placeName);
        }
        
        // Actualizar RouteLocation
        int position = layoutUbicaciones.indexOfChild(ubicacionView);
        if (position >= 0 && position < routeLocations.size()) {
            RouteLocation routeLoc = routeLocations.get(position);
            routeLoc.setLatLng(latLng);
            if (routeLoc.getTitle() == null || routeLoc.getTitle().isEmpty()) {
                routeLoc.setTitle(placeName);
            }
        }
        
        updateMapMarkers();
        Toast.makeText(this, "Ubicación agregada", Toast.LENGTH_SHORT).show();
    }
    
    private void moveLocation(View ubicacionView, int direction) {
        int currentIndex = layoutUbicaciones.indexOfChild(ubicacionView);
        int newIndex = currentIndex + direction;
        
        if (newIndex < 0 || newIndex >= layoutUbicaciones.getChildCount()) {
            Toast.makeText(this, "No se puede mover más en esa dirección", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Mover en la vista
        layoutUbicaciones.removeView(ubicacionView);
        layoutUbicaciones.addView(ubicacionView, newIndex);
        
        // Mover en la lista
        RouteLocation temp = routeLocations.get(currentIndex);
        routeLocations.set(currentIndex, routeLocations.get(newIndex));
        routeLocations.set(newIndex, temp);
        
        // Actualizar números y mapa
        updateUbicacionNumbers();
        updateMapMarkers();
        
        Toast.makeText(this, "Ubicación reordenada", Toast.LENGTH_SHORT).show();
    }
    
    private void updateUbicacionNumbers() {
        for (int i = 0; i < layoutUbicaciones.getChildCount(); i++) {
            View child = layoutUbicaciones.getChildAt(i);
            TextView tvNumero = child.findViewById(R.id.tvNumeroUbicacion);
            tvNumero.setText("Ubicación " + (i + 1));
            
            if (i < routeLocations.size()) {
                routeLocations.get(i).setOrder(i + 1);
            }
        }
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

    private void createTour() {
        Log.d(TAG, "createTour() llamado");
        
        if (validateForm()) {
            Log.d(TAG, "Formulario válido");
            
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
            
            Log.d(TAG, "Datos recopilados - Título: " + tituloTour + ", ID Empresa: " + idEmpresa);
            
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
            nuevoTour.setIdGuia(""); // Inicialmente sin guía asignado
            nuevoTour.setImageUrl(""); // Se actualizará al subir la imagen
            nuevoTour.setRating(5); // Rating inicial
            
            // Mostrar diálogo de progreso
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Subiendo imágenes...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            
            Log.d(TAG, "tourImageUri: " + (tourImageUri != null ? "presente" : "null"));
            
            // Primero subir imagen del tour, luego servicios
            if (tourImageUri != null) {
                uploadTourImageAndContinue(nuevoTour, idEmpresa, tituloTour);
            } else {
                // Sin imagen del tour, continuar con servicios
                Log.d(TAG, "Sin imagen de tour, continuando con servicios");
                uploadServiceImagesAndCreateTour(nuevoTour, idEmpresa, tituloTour);
            }
        } else {
            Log.e(TAG, "Formulario NO válido");
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
        
        Log.d(TAG, "=== INICIANDO GUARDADO DE TOUR ===");
        Log.d(TAG, "Título: " + nuevoTour.getTitle());
        Log.d(TAG, "ID Empresa: " + nuevoTour.getIdEmpresa());
        Log.d(TAG, "Precio: " + nuevoTour.getPrice());
        Log.d(TAG, "Región: " + nuevoTour.getRegion());
        
        // Preparar ubicaciones para guardar en subcolección
        List<Tour.Ubicacion> ubicaciones = new ArrayList<>();
        Log.d(TAG, "Route locations size: " + routeLocations.size());
        
        for (RouteLocation routeLoc : routeLocations) {
            if (routeLoc.getLatLng() != null) {
                Tour.Ubicacion ubicacion = new Tour.Ubicacion();
                ubicacion.setTitle(routeLoc.getTitle() != null ? routeLoc.getTitle() : "Ubicación " + routeLoc.getOrder());
                ubicacion.setOrder(routeLoc.getOrder());
                ubicacion.setLat(routeLoc.getLat());
                ubicacion.setLng(routeLoc.getLng());
                ubicaciones.add(ubicacion);
                Log.d(TAG, "Ubicación agregada: " + ubicacion.getTitle() + " - Lat:" + ubicacion.getLat() + " Lng:" + ubicacion.getLng());
            }
        }
        
        Log.d(TAG, "Total ubicaciones a guardar: " + ubicaciones.size());
        
        // Guardar en Firestore
        final List<Tour.Ubicacion> finalUbicaciones = ubicaciones;
        firestoreHelper.createTour(nuevoTour, (success, tourId) -> {
            Log.d(TAG, "Callback createTour - success: " + success + ", tourId: " + tourId);
            
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            
            if (success && tourId != null) {
                // Guardar ubicaciones en subcolección si hay
                if (!finalUbicaciones.isEmpty()) {
                    Log.d(TAG, "Guardando " + finalUbicaciones.size() + " ubicaciones");
                    firestoreHelper.saveLocationsSubcollection(tourId, finalUbicaciones);
                }
                
                String mensajeUbicaciones = finalUbicaciones.isEmpty() ? "" : " con " + finalUbicaciones.size() + " ubicaciones";
                Toast.makeText(this, "¡Tour creado exitosamente" + mensajeUbicaciones + "!", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Tour guardado exitosamente con ID: " + tourId);
                finishCreation();
            } else {
                Log.e(TAG, "Error al guardar tour en Firestore");
                Toast.makeText(this, "Error al crear tour", Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void finishCreation() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("tour_created", true);
        setResult(RESULT_OK, resultIntent);
        finish();
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
        
        // Agregar información de ubicaciones
        int numLocations = routeLocations.size();
        if (numLocations > 0) {
            descripcion.append("Ruta con ").append(numLocations).append(" ubicación");
            if (numLocations > 1) {
                descripcion.append("es");
            }
            descripcion.append(". ");
        }
        
        // Agregar información de servicios
        if (!serviciosExtra.isEmpty()) {
            descripcion.append("Incluye ").append(serviciosExtra.size()).append(" servicio(s) extra. ");
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
            EditText etTitulo = child.findViewById(R.id.etTituloUbicacion);
            TextView tvCoordenadas = child.findViewById(R.id.tvCoordenadas);

            if (etTitulo != null && etTitulo.getText().toString().trim().isEmpty()) {
                etTitulo.setError("El título de la ubicación es obligatorio");
                etTitulo.requestFocus();
                return false;
            }

            // Validar que la ubicación tenga coordenadas seleccionadas
            if (i >= routeLocations.size() || routeLocations.get(i).getLatLng() == null) {
                Toast.makeText(this, "Debe seleccionar una ubicación en el mapa para la ubicación " + (i + 1), Toast.LENGTH_SHORT).show();
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
