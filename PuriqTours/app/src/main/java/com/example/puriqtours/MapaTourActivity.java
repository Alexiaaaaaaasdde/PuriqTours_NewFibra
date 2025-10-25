package com.example.puriqtours;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapaTourActivity extends AppCompatActivity {

    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_tour);

        // Configuración osmdroid
        Configuration.getInstance().setUserAgentValue(getPackageName());

        map = findViewById(R.id.osmMap);
        map.setMultiTouchControls(true);

        // Punto inicial (ejemplo: PUCP)
        GeoPoint pucp = new GeoPoint(-12.069, -77.079);
        map.getController().setZoom(16.0);
        map.getController().setCenter(pucp);

        // Marcador
        Marker marker = new Marker(map);
        marker.setPosition(pucp);
        marker.setTitle("PUCP");
        map.getOverlays().add(marker);

        MaterialButton btnMarcar = findViewById(R.id.btnMarcarUbicacion);

        btnMarcar.setOnClickListener(v -> {
            FinalizarTourBottomSheet bottomSheet = new FinalizarTourBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

    }
}
