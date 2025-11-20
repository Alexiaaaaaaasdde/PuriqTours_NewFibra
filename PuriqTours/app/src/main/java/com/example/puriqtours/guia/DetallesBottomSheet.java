package com.example.puriqtours.guia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.CheckpointReserva;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.firestore.*;

import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class DetallesBottomSheet extends BottomSheetDialogFragment implements OnMapReadyCallback {

    private String idReserva;

    private double pago;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private MapView mapView;
    private GoogleMap gMap;

    private TextView tvActividades, tvDuracion, tvCosto, tvServiciosExtra, tvHorarioFecha;

    private List<CheckpointReserva> checkpointList = new ArrayList<>();

    private List<String> activitiesList = new ArrayList<>();

    private String fecha;

    public DetallesBottomSheet(String idReserva, double pago) {
        this.idReserva = idReserva;
        this.pago = pago;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottomsheet_detalles, container, false);

        mapView = v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        tvActividades = v.findViewById(R.id.tvActividades);
        tvDuracion   = v.findViewById(R.id.tvDuracion);
        tvCosto      = v.findViewById(R.id.tvCosto);
        tvServiciosExtra = v.findViewById(R.id.tvServiciosExtra);
        tvHorarioFecha = v.findViewById(R.id.tvHorarioFecha);

        cargarDatosReserva();

        return v;
    }

    private void cargarDatosReserva() {
        db.collection("reservas").document(idReserva)
                .get()
                .addOnSuccessListener(reserva -> {
                    if (!reserva.exists()) return;

                    String idTour = reserva.getString("idTour");

                    fecha  = reserva.getString("date");


                    cargarDatosTour(idTour);
                    cargarActividades();
                    cargarCheckpoints();
                });
    }

    private void cargarDatosTour(String idTour) {
        db.collection("tours").document(idTour)
                .get()
                .addOnSuccessListener(tour -> {
                    if (!tour.exists()) return;

                    tvActividades.setText(tour.getString("desc")); // descripción del tour
                    String inicio = tour.getString("startTime");
                    String fin = tour.getString("endTime");
                    int hInicio = Integer.parseInt(inicio.split(":")[0]);
                    int hFin = Integer.parseInt(fin.split(":")[0]);
                    int duracion = hFin - hInicio;
                    tvDuracion.setText("Duración total: " + duracion + " horas");
                    tvCosto.setText("Paga del guía: S/ " + pago);
                    tvHorarioFecha.setText("Inicio: " + inicio + " - Fin: " + fin + " | Fecha: " + fecha);
                });
    }

    private void cargarCheckpoints() {
        db.collection("reservas")
                .document(idReserva)
                .collection("checkpoints")
                .get()
                .addOnSuccessListener(result -> {
                    checkpointList.clear();

                    for (DocumentSnapshot doc : result) {
                        CheckpointReserva checkpoint = new CheckpointReserva();
                        checkpoint.setLat(doc.getDouble("lat"));
                        checkpoint.setLng(doc.getDouble("lng"));
                        checkpoint.setTitle(doc.getString("title"));
                        checkpointList.add(checkpoint);
                    }

                    if (gMap != null) {
                        dibujarRuta();
                    }
                });
    }

    private void cargarActividades(){
        db.collection("reservas")
                .document(idReserva)
                .collection("addedServices")
                .get()
                .addOnSuccessListener(result -> {
                    activitiesList.clear();

                    for (DocumentSnapshot doc : result){
                        String activity = doc.getString("title");
                        activitiesList.add(activity);
                    }

                    if (activitiesList != null && !activitiesList.isEmpty()) {
                        tvServiciosExtra.setText(String.join(", ", activitiesList));
                    } else {
                        tvServiciosExtra.setText("Sin servicios adicionales");
                    }
                });
    }

    private void dibujarRuta() {
        if (checkpointList.isEmpty()) return;

        PolylineOptions polylineOptions = new PolylineOptions()
                .width(8)
                .geodesic(true);

        for (CheckpointReserva check : checkpointList) {
            LatLng point = new LatLng(check.getLat(), check.getLng());
            gMap.addMarker(new MarkerOptions().position(point).title(check.getTitle()));
            polylineOptions.add(point);
        }
        CheckpointReserva checki = checkpointList.get(0);
        LatLng point0 = new LatLng(checki.getLat(), checki.getLng());

        gMap.addPolyline(polylineOptions);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point0, 14f));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        if (!checkpointList.isEmpty()) {
            dibujarRuta();
        }
    }

    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}
