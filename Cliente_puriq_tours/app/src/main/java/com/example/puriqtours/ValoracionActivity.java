package com.example.puriqtours;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ValoracionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valoracion);

        // Botón enviar valoración (dentro del formulario)
        Button btnEnviar = findViewById(R.id.btnEnviarValoracion);
        btnEnviar.setOnClickListener(v -> {
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
