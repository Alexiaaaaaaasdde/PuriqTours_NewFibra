package com.example.puriqtours.guia;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.CheckpointReserva;
import com.google.android.gms.location.*;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class MapaTourActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private String idReserva, idTour, tokenFin;
    private FirebaseFirestore db;

    private List<CheckpointReserva> checkpointList = new ArrayList<>();
    private CheckpointReserva checkpointActual;

    private MaterialButton btnMarcar, btnFinalizar;
    private TextView tvNombreDestino, tvDistancia;

    private Location guiaLocation;

    // 🔹 Distancia mínima para marcar un checkpoint (50 metros)
    private static final float DISTANCIA_MINIMA = 50f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_tour);

        // Obtener datos de intent
        idReserva = getIntent().getStringExtra("idReserva");
        idTour = getIntent().getStringExtra("idTour");

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // UI
        btnMarcar = findViewById(R.id.btnMarcarUbicacion);
        btnFinalizar = findViewById(R.id.btnFinalizarTour);
        tvNombreDestino = findViewById(R.id.tvNombreDestino);
        tvDistancia = findViewById(R.id.tvDistancia);

        btnFinalizar.setEnabled(false);

        // Mapa
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Firestore
        cargarTokenFin();
        cargarCheckpoints();

        // Botones
        btnMarcar.setOnClickListener(v -> marcarCheckpoint());
        btnFinalizar.setOnClickListener(v -> dialogFinalizar());
    }

    // ============================================================================
    // MAPA LISTO
    // ============================================================================
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1000
            );
            return;
        }

        mMap.setMyLocationEnabled(true);
        iniciarGps();
    }

    // ============================================================================
    // GPS EN TIEMPO REAL
    // ============================================================================
    private void iniciarGps() {

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(2500)
                .setFastestInterval(1500)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        guiaLocation = locationResult.getLastLocation();
                        actualizarBottomSheet();
                    }
                },
                getMainLooper()
        );
    }

    // ============================================================================
    // TOKEN FIN
    // ============================================================================
    private void cargarTokenFin() {
        db.collection("reservas").document(idReserva)
                .get()
                .addOnSuccessListener(doc -> tokenFin = doc.getString("qrFin"));
    }

    // ============================================================================
    // CARGAR CHECKPOINTS
    // ============================================================================
    private void cargarCheckpoints() {

        db.collection("reservas")
                .document(idReserva)
                .collection("checkpoints")
                .orderBy("order")
                .addSnapshotListener((snap, err) -> {

                    if (err != null || snap == null) {
                        Log.e("MAPA", "Error al cargar checkpoints", err);
                        return;
                    }

                    checkpointList.clear();

                    for (DocumentSnapshot ds : snap.getDocuments()) {
                        CheckpointReserva cp = ds.toObject(CheckpointReserva.class);
                        cp.setId(ds.getId());
                        checkpointList.add(cp);
                    }

                    pintarCheckpoints();
                    pintarRuta();
                    actualizarBottomSheet();
                });
    }

    // ============================================================================
    // PINTAR MARKERS
    // ============================================================================
    private void pintarCheckpoints() {

        mMap.clear();

        for (CheckpointReserva cp : checkpointList) {

            LatLng pos = new LatLng(cp.getLat(), cp.getLng());

            float color = cp.getStatus().equals("Visitado")
                    ? BitmapDescriptorFactory.HUE_GREEN
                    : BitmapDescriptorFactory.HUE_RED;

            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(cp.getTitle())
                    .snippet(cp.getStatus())
                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
        }

        if (!checkpointList.isEmpty()) {
            LatLng first = new LatLng(checkpointList.get(0).getLat(), checkpointList.get(0).getLng());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(first, 15f));
        }
    }

    // ============================================================================
    // RUTA POLYLINE
    // ============================================================================
    private void pintarRuta() {

        if (checkpointList.size() < 2) return;

        PolylineOptions poly = new PolylineOptions()
                .width(10)
                .color(getColor(R.color.teal_700));

        for (CheckpointReserva cp : checkpointList) {
            poly.add(new LatLng(cp.getLat(), cp.getLng()));
        }

        mMap.addPolyline(poly);
    }

    // ============================================================================
    // CHECKPOINT MÁS CERCANO
    // ============================================================================
    private CheckpointReserva obtenerMasCercano() {

        if (guiaLocation == null) return null;

        CheckpointReserva mejor = null;
        float minDist = Float.MAX_VALUE;

        for (CheckpointReserva cp : checkpointList) {

            float[] res = new float[1];

            Location.distanceBetween(
                    guiaLocation.getLatitude(), guiaLocation.getLongitude(),
                    cp.getLat(), cp.getLng(),
                    res
            );

            if (res[0] < minDist) {
                minDist = res[0];
                mejor = cp;
            }
        }

        return mejor;
    }

    // ============================================================================
    // BOTTOM SHEET DINÁMICO
    // ============================================================================
    private void actualizarBottomSheet() {

        if (guiaLocation == null || checkpointList.isEmpty()) return;

        CheckpointReserva masCercano = obtenerMasCercano();
        if (masCercano == null) return;

        checkpointActual = masCercano;

        float[] dist = new float[1];
        Location.distanceBetween(
                guiaLocation.getLatitude(), guiaLocation.getLongitude(),
                masCercano.getLat(), masCercano.getLng(),
                dist
        );

        int d = (int) dist[0];

        tvNombreDestino.setText(masCercano.getTitle());
        tvDistancia.setText("Distancia: " + d + " m");

        LatLng pos = new LatLng(masCercano.getLat(), masCercano.getLng());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 17f));
    }

    // ============================================================================
    // MARCAR CHECKPOINT (con distancia mínima)
    // ============================================================================
    private void marcarCheckpoint() {

        if (checkpointActual == null || guiaLocation == null) {
            Toast.makeText(this, "No se pudo determinar el checkpoint actual.", Toast.LENGTH_SHORT).show();
            return;
        }

        float[] distancia = new float[1];
        Location.distanceBetween(
                guiaLocation.getLatitude(), guiaLocation.getLongitude(),
                checkpointActual.getLat(), checkpointActual.getLng(),
                distancia
        );

        if (distancia[0] > DISTANCIA_MINIMA) {
            Toast.makeText(
                    this,
                    "Acércate al checkpoint (mínimo " + (int) DISTANCIA_MINIMA + " m)",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        db.collection("reservas")
                .document(idReserva)
                .collection("checkpoints")
                .document(checkpointActual.getId())
                .update("status", "Visitado")
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, checkpointActual.getTitle() + " visitado ✔", Toast.LENGTH_SHORT).show();
                    verificarFinalizacion();
                });
    }

    // ============================================================================
    // ACTIVAR FINALIZACIÓN DEL TOUR
    // ============================================================================
    private void verificarFinalizacion() {

        for (CheckpointReserva cp : checkpointList) {
            if (!"Visitado".equals(cp.getStatus())) {
                return;
            }
        }

        btnFinalizar.setEnabled(true);
        Toast.makeText(this, "Todos los checkpoints visitados ✔", Toast.LENGTH_LONG).show();
    }

    // ============================================================================
    // FINALIZAR TOUR
    // ============================================================================
    private void dialogFinalizar() {

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Finalizar Tour");
        b.setMessage("Ingrese el token de finalización:");

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        b.setView(input);

        b.setPositiveButton("Validar", (d, w) -> {

            String token = input.getText().toString().trim();

            if (!token.equals(tokenFin)) {
                Toast.makeText(this, "Token incorrecto ❌", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("reservas")
                    .document(idReserva)
                    .update("status", "Finalizado");

            Toast.makeText(this, "Tour finalizado ✔", Toast.LENGTH_LONG).show();
            finish();
        });

        b.setNegativeButton("Cancelar", (d, w) -> d.cancel());
        b.show();
    }
}
