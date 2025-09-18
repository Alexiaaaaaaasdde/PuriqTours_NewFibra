package com.example.puriqtours;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class MapaTourActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_tour);

        MaterialButton btnMarcar = findViewById(R.id.btnMarcarUbicacion);

        btnMarcar.setOnClickListener(v -> {
            FinalizarTourBottomSheet bottomSheet = new FinalizarTourBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

    }
}
