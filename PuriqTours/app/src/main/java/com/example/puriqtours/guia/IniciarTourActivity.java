package com.example.puriqtours.guia;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;

import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.google.firebase.firestore.FirebaseFirestore;

public class IniciarTourActivity extends AppCompatActivity {

    private String idReserva;
    private String idTour;
    private String tokenInicioEsperado;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_tour);

        db = FirebaseFirestore.getInstance();

        // 🔹 Recibir datos desde ToursFragment
        idReserva = getIntent().getStringExtra("idReserva");
        idTour = getIntent().getStringExtra("idTour");
        tokenInicioEsperado = getIntent().getStringExtra("tokenInicio");

        // Mostrar el diálogo para ingresar el token
        mostrarDialogToken();
    }

    private void mostrarDialogToken() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verificar Token de Inicio");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Ingrese el token");
        builder.setView(input);

        builder.setPositiveButton("Validar", (dialog, which) -> {
            String tokenIngresado = input.getText().toString().trim();

            if (tokenIngresado.isEmpty()) {
                Toast.makeText(this, "Debe ingresar un token.", Toast.LENGTH_SHORT).show();
                return;
            }

            validarToken(tokenIngresado);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void validarToken(String tokenIngresado) {

        // Si ya enviamos el token desde el fragment, esta verificación es inmediata
        if (tokenInicioEsperado != null) {

            if (tokenIngresado.equals(tokenInicioEsperado)) {

                // 🔹 Actualizar estado a "En proceso"
                actualizarEstadoReserva();

            } else {
                Toast.makeText(this, "Token incorrecto", Toast.LENGTH_SHORT).show();
            }

            return;
        }

        // 🔹 Si no vino el token, lo buscamos en Firestore (fallback)
        db.collection("reservas")
                .document(idReserva)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "Reserva no encontrada", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String tokenInicioDb = doc.getString("tokenInicio");

                    if (tokenInicioDb == null) {
                        Toast.makeText(this, "No se encontró token de inicio", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (tokenIngresado.equals(tokenInicioDb)) {

                        // 🔹 Actualizar estado a "En proceso"
                        actualizarEstadoReserva();

                    } else {
                        Toast.makeText(this, "Token incorrecto", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void abrirMapa() {
        Toast.makeText(this, "Token validado correctamente ✔", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MapaTourActivity.class);
        intent.putExtra("idReserva", idReserva);
        intent.putExtra("idTour", idTour);
        startActivity(intent);
        finish();
    }

    private void actualizarEstadoReserva() {

        db.collection("reservas")
                .document(idReserva)
                .update("estado", "En proceso")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Tour iniciado ✔", Toast.LENGTH_SHORT).show();
                    abrirMapa();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar estado", Toast.LENGTH_SHORT).show();
                });
    }

}
