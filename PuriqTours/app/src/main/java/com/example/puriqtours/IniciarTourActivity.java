package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class IniciarTourActivity extends AppCompatActivity {

    Button btnEscanear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_tour);

        // Vincular el botón
        btnEscanear = findViewById(R.id.btnEscanear);

        // Acción temporal (solo muestra un mensaje)
        btnEscanear.setOnClickListener(v -> {
            Intent intent = new Intent(IniciarTourActivity.this, MapaTourActivity.class);
            startActivity(intent);
        });

    }
}
