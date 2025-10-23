package com.example.puriqtours;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ValoracionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valoracion);

        // Referencias a los RatingBars
        RatingBar ratingTour = findViewById(R.id.ratingExpectativasTour);
        RatingBar ratingServicios = findViewById(R.id.ratingServiciosTour);
        RatingBar ratingGuia = findViewById(R.id.ratingGuia);

        // Botón enviar valoración
        Button btnEnviar = findViewById(R.id.btnEnviarValoracion);
        btnEnviar.setOnClickListener(v -> {
            // 🔹 Capturar las valoraciones
            float estrellasTour = ratingTour.getRating();
            float estrellasServicios = ratingServicios.getRating();
            float estrellasGuia = ratingGuia.getRating();

            // 🔹 Mostrar en un Toast (solo para prueba visual)
            Toast.makeText(this,
                    "Tour: " + estrellasTour + "★ | Servicios: " + estrellasServicios + "★ | Guía: " + estrellasGuia + "★",
                    Toast.LENGTH_LONG).show();

            // 🔹 Mostrar popup de confirmación
            mostrarPopupConfirmacion();
        });
    }

    private void mostrarPopupConfirmacion() {
        Dialog dialog = new Dialog(ValoracionActivity.this);
        dialog.setContentView(R.layout.dialog_valoracion); // tu popup XML
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT)
        );

        // Botón cerrar del popup
        Button btnCerrar = dialog.findViewById(R.id.btnCerrarValoracion);
        btnCerrar.setOnClickListener(c -> {
            dialog.dismiss();
            // Redirigir al HistorialActivity
            Intent intent = new Intent(ValoracionActivity.this, HistorialActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // cerrar esta activity
        });
        dialog.show();
    }
}
