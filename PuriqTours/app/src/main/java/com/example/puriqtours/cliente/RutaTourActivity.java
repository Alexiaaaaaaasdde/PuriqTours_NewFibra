package com.example.puriqtours.cliente;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RutaTourActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "RutaTourActivity";

    private GoogleMap mMap;
    private MapView mapView;
    private String tourId;
    private String tituloTour;
    private FirebaseFirestore db;
    private TextView tvTituloRuta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_ruta_tour);

            // 🔹 OBTENER DATOS DEL INTENT
            tourId = getIntent().getStringExtra("tourId");
            tituloTour = getIntent().getStringExtra("titulo");

            Log.d(TAG, "=================================");
            Log.d(TAG, "onCreate - tourId recibido: " + tourId);
            Log.d(TAG, "onCreate - título recibido: " + tituloTour);
            Log.d(TAG, "=================================");

            // 🔹 VALIDAR QUE EL TOUR ID NO SEA NULO
            if (tourId == null || tourId.isEmpty()) {
                Log.e(TAG, "ERROR: tourId es null o vacío");
                Toast.makeText(this, "Error: ID del tour no disponible", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            db = FirebaseFirestore.getInstance();

            // 🔹 CONFIGURAR TÍTULO
            tvTituloRuta = findViewById(R.id.tvTituloRuta);
            if (tituloTour != null && !tituloTour.isEmpty()) {
                tvTituloRuta.setText("Ruta: " + tituloTour);
            } else {
                tvTituloRuta.setText("Ruta del Tour");
            }

            // 🔹 CONFIGURAR MAPVIEW
            mapView = findViewById(R.id.mapRuta);
            if (mapView != null) {
                Log.d(TAG, "MapView encontrado, inicializando...");
                mapView.onCreate(savedInstanceState);
                mapView.getMapAsync(this);
            } else {
                Log.e(TAG, "ERROR: MapView no encontrado en el layout");
                Toast.makeText(this, "Error: No se pudo cargar el mapa", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // 🔹 BOTÓN VOLVER
            findViewById(R.id.btnVolverRuta).setOnClickListener(v -> {
                Log.d(TAG, "Botón volver presionado");
                finish();
            });

        } catch (Exception e) {
            Log.e(TAG, "ERROR en onCreate", e);
            Toast.makeText(this, "Error al inicializar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        try {
            mMap = googleMap;
            Log.d(TAG, "✅ Mapa listo y configurado");

            // 🔹 CONFIGURAR UI DEL MAPA
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            // 🔹 CARGAR RUTA DESDE FIRESTORE
            cargarYMostrarRuta();

        } catch (Exception e) {
            Log.e(TAG, "ERROR en onMapReady", e);
            Toast.makeText(this, "Error al configurar mapa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarYMostrarRuta() {
        Log.d(TAG, "📍 Iniciando carga de ruta desde Firestore");
        Log.d(TAG, "📍 Ruta: tours/" + tourId + "/locations");

        db.collection("tours")
                .document(tourId)
                .collection("locations")
                .orderBy("order")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    try {
                        Log.d(TAG, "✅ Consulta exitosa. Documentos encontrados: " + querySnapshot.size());

                        if (querySnapshot.isEmpty()) {
                            Log.w(TAG, "⚠️ No hay ubicaciones en la ruta");
                            Toast.makeText(this, "No hay ruta disponible para este tour", Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<LatLng> rutaPuntos = new ArrayList<>();
                        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                        int index = 1;
                        for (DocumentSnapshot doc : querySnapshot) {
                            String title = doc.getString("title");
                            Double lat = doc.getDouble("lat");
                            Double lng = doc.getDouble("lng");

                            Log.d(TAG, "Punto " + index + ": " + title + " - Lat: " + lat + ", Lng: " + lng);

                            if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                                LatLng punto = new LatLng(lat, lng);
                                rutaPuntos.add(punto);
                                boundsBuilder.include(punto);

                                // 🗺️ AGREGAR MARCADOR
                                mMap.addMarker(new MarkerOptions()
                                        .position(punto)
                                        .title(index + ". " + title)
                                        .icon(BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_AZURE)));

                                index++;
                            } else {
                                Log.w(TAG, "⚠️ Coordenadas inválidas para: " + title);
                            }
                        }

                        Log.d(TAG, "Total de puntos válidos agregados: " + rutaPuntos.size());

                        // 🗺️ DIBUJAR LÍNEA DE LA RUTA
                        if (rutaPuntos.size() > 1) {
                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .addAll(rutaPuntos)
                                    .width(10f)
                                    .color(getResources().getColor(R.color.teal_700))
                                    .geodesic(true);

                            mMap.addPolyline(polylineOptions);

                            // 🗺️ AJUSTAR CÁMARA PARA MOSTRAR TODA LA RUTA
                            LatLngBounds bounds = boundsBuilder.build();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));

                            Log.d(TAG, "✅ Ruta dibujada exitosamente con " + rutaPuntos.size() + " puntos");

                        } else if (rutaPuntos.size() == 1) {
                            // Solo hay un punto, centrar ahí
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(rutaPuntos.get(0), 15f));
                            Log.d(TAG, "✅ Solo un punto, cámara centrada");

                        } else {
                            Log.w(TAG, "⚠️ No hay puntos válidos para mostrar");
                            Toast.makeText(this, "No hay coordenadas válidas en la ruta", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "ERROR al procesar datos de la ruta", e);
                        Toast.makeText(this, "Error al procesar ruta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ ERROR en consulta Firestore", e);
                    Toast.makeText(this, "Error al cargar ruta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ============================================
    // CICLO DE VIDA DEL MAPVIEW
    // ============================================

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
}
