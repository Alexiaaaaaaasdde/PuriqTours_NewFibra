package com.example.puriqtours.cliente;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ValoracionActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String reservaId;
    private String tourId;
    private String guiaId;
    private String clienteId;

    private RatingBar ratingTour, ratingServicios, ratingGuia;
    private EditText etComentarios;
    private Button btnEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valoracion);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        clienteId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        // Obtener datos del Intent
        reservaId = getIntent().getStringExtra("reservaId");
        tourId = getIntent().getStringExtra("tourId");
        guiaId = getIntent().getStringExtra("guiaId");

        // Referencias a las vistas
        ratingTour = findViewById(R.id.ratingExpectativasTour);
        ratingServicios = findViewById(R.id.ratingServiciosTour);
        ratingGuia = findViewById(R.id.ratingGuia);
        etComentarios = findViewById(R.id.etComentarios);
        btnEnviar = findViewById(R.id.btnEnviarValoracion);

        // Validar que la reserva esté finalizada
        validarReservaFinalizada();

        // Configurar botón enviar
        btnEnviar.setOnClickListener(v -> enviarValoracion());
    }

    /**
     * Valida que la reserva esté en estado "Finalizado"
     */
    private void validarReservaFinalizada() {
        if (reservaId == null || reservaId.isEmpty()) {
            Toast.makeText(this, "Error: No se encontró la reserva", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("reservas")
                .document(reservaId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Reserva no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // ✅ CAMPO CORRECTO
                    String status = doc.getString("status");
                    Boolean yaValorado = doc.getBoolean("valorada");

                    if (status == null || !status.equalsIgnoreCase("Finalizado")) {
                        Toast.makeText(
                                this,
                                "Solo puedes valorar tours finalizados",
                                Toast.LENGTH_LONG
                        ).show();
                        finish();
                        return;
                    }

                    if (yaValorado != null && yaValorado) {
                        Toast.makeText(
                                this,
                                "Ya has valorado este tour",
                                Toast.LENGTH_LONG
                        ).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Error al validar reserva: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                });
    }


    /**
     * Envía la valoración a Firestore
     */
    private void enviarValoracion() {
        // Capturar las valoraciones
        float estrellasTour = ratingTour.getRating();
        float estrellasServicios = ratingServicios.getRating();
        float estrellasGuia = ratingGuia.getRating();
        String comentario = etComentarios.getText().toString().trim();

        // Validar que todas las valoraciones estén completas
        if (estrellasTour == 0 || estrellasServicios == 0 || estrellasGuia == 0) {
            Toast.makeText(this,
                    "Por favor, completa todas las valoraciones con estrellas",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Calcular rating promedio
        float ratingPromedio = (estrellasTour + estrellasServicios + estrellasGuia) / 3;

        // Deshabilitar botón mientras se procesa
        btnEnviar.setEnabled(false);
        btnEnviar.setText("Enviando...");

        // Crear documento de valoración
        Map<String, Object> valoracion = new HashMap<>();
        valoracion.put("reservaId", reservaId);
        valoracion.put("tourId", tourId);
        valoracion.put("guiaId", guiaId);
        valoracion.put("clienteId", clienteId);
        valoracion.put("ratingTour", estrellasTour);
        valoracion.put("ratingServicios", estrellasServicios);
        valoracion.put("ratingGuia", estrellasGuia);
        valoracion.put("ratingPromedio", ratingPromedio);
        valoracion.put("comentario", comentario.isEmpty() ? "" : comentario);
        valoracion.put("fecha", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(new Date()));
        valoracion.put("timestamp", FieldValue.serverTimestamp());

        // Guardar en Firestore
        db.collection("valoraciones")
                .add(valoracion)
                .addOnSuccessListener(docRef -> {
                    // Actualizar rating del tour
                    actualizarRatingTour(tourId);

                    // Actualizar rating del guía
                    if (guiaId != null && !guiaId.isEmpty()) {
                        actualizarRatingGuia(guiaId);
                    }

                    // Marcar la reserva como valorada
                    marcarReservaValorada(reservaId);

                    // Mostrar popup de confirmación
                    mostrarPopupConfirmacion();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error al enviar valoración: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    btnEnviar.setEnabled(true);
                    btnEnviar.setText("Enviar Valoración");
                });
    }

    /**
     * Actualiza el rating del tour calculando el promedio
     */
    private void actualizarRatingTour(String tourId) {
        if (tourId == null || tourId.isEmpty()) return;

        db.collection("valoraciones")
                .whereEqualTo("tourId", tourId)
                .get()
                .addOnSuccessListener(query -> {
                    double sumaRatings = 0;
                    int totalValoraciones = query.size();

                    for (DocumentSnapshot doc : query) {
                        Double rating = doc.getDouble("ratingPromedio");
                        if (rating != null) {
                            sumaRatings += rating;
                        }
                    }

                    double promedioFinal = totalValoraciones > 0 ? sumaRatings / totalValoraciones : 0;
                    int ratingRedondeado = (int) Math.round(promedioFinal);

                    // Actualizar el tour
                    db.collection("tours")
                            .document(tourId)
                            .update("rating", ratingRedondeado)
                            .addOnFailureListener(e -> {
                                // Error al actualizar (no crítico)
                            });
                });
    }

    /**
     * Actualiza el rating del guía calculando el promedio
     */
    private void actualizarRatingGuia(String guiaId) {
        if (guiaId == null || guiaId.isEmpty()) return;

        db.collection("valoraciones")
                .whereEqualTo("guiaId", guiaId)
                .get()
                .addOnSuccessListener(query -> {
                    double sumaRatings = 0;
                    int totalValoraciones = query.size();

                    for (DocumentSnapshot doc : query) {
                        Double rating = doc.getDouble("ratingGuia");
                        if (rating != null) {
                            sumaRatings += rating;
                        }
                    }

                    double promedioFinal = totalValoraciones > 0 ? sumaRatings / totalValoraciones : 0;

                    // Actualizar el usuario (guía) con rating Double
                    db.collection("usuarios")
                            .document(guiaId)
                            .update("rating", promedioFinal)
                            .addOnFailureListener(e -> {
                                // Error al actualizar (no crítico)
                            });
                });
    }

    /**
     * Marca la reserva como valorada para evitar duplicados
     */
    private void marcarReservaValorada(String reservaId) {
        db.collection("reservas")
                .document(reservaId)
                .update("valorada", true)
                .addOnFailureListener(e -> {
                    // Error al marcar (no crítico)
                });
    }

    /**
     * Muestra el popup de confirmación
     */
    private void mostrarPopupConfirmacion() {
        Dialog dialog = new Dialog(ValoracionActivity.this);
        dialog.setContentView(R.layout.dialog_valoracion);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT)
        );

        Button btnCerrar = dialog.findViewById(R.id.btnCerrarValoracion);
        btnCerrar.setOnClickListener(c -> {
            dialog.dismiss();
            Intent intent = new Intent(ValoracionActivity.this, HistorialActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}
