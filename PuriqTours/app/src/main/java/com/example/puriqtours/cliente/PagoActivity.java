package com.example.puriqtours.cliente;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.app.PendingIntent;

import com.bumptech.glide.Glide;
import com.example.puriqtours.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PagoActivity extends AppCompatActivity {

    // 🔹 Launcher para pedir permiso de notificaciones
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);

        // 🔹 Inicializar launcher de permisos
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permiso concedido, enviar notificación
                        enviarNotificacion();
                    } else {
                        // Permiso denegado
                        Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 🔹 Referencias generales
        TextView tvFechaPago = findViewById(R.id.tvFechaPago);
        TextView tvViajerosPago = findViewById(R.id.tvViajerosPago);
        TextView tvPrecioPago = findViewById(R.id.tvPrecioPago);
        TextView tvHoraPago = findViewById(R.id.tvHoraPago);

        // 🔹 Referencias de la tarjeta superior
        TextView tvTituloCard = findViewById(R.id.tvTituloCard);
        TextView tvUbicacionCard = findViewById(R.id.tvUbicacionCard);
        TextView tvOpinionesCard = findViewById(R.id.tvOpiniones);
        RatingBar ratingBarCard = findViewById(R.id.ratingBar);
        ImageView imgTourCard = findViewById(R.id.imgTourCard);

        // 🔹 Recuperar datos enviados desde DetalleTourActivity
        String fecha = getIntent().getStringExtra("fecha");
        String viajeros = getIntent().getStringExtra("viajeros");
        String precio = getIntent().getStringExtra("precio");
        String hora = getIntent().getStringExtra("hora");
        String tourId = getIntent().getStringExtra("tourId");
        String titulo = getIntent().getStringExtra("titulo");
        String ubicacion = getIntent().getStringExtra("ubicacion");
        String opiniones = getIntent().getStringExtra("opiniones");
        int rating = getIntent().getIntExtra("rating", 5);
        String img = getIntent().getStringExtra("img");

        // ------- SETEAR DATOS EN LA UI -------
        if (fecha != null) tvFechaPago.setText("Fecha: " + fecha);
        if (viajeros != null) tvViajerosPago.setText("Viajeros: " + viajeros);
        if (precio != null) tvPrecioPago.setText("Total: " + precio);
        if (hora != null) tvHoraPago.setText("Hora: " + hora);

        // 🔹 Card superior
        tvTituloCard.setText(titulo != null ? titulo : "Tour");
        tvUbicacionCard.setText(ubicacion != null ? ubicacion : "Ubicación");
        tvOpinionesCard.setText(opiniones != null ? opiniones : "(0 opiniones)");
        ratingBarCard.setRating(rating);

        Glide.with(this)
                .load(img)
                .placeholder(R.drawable.kuelap)
                .into(imgTourCard);

        // ---------- NAVEGACIÓN INFERIOR ----------
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_tours);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        // ---------- DIALOG TARJETA ----------
        RadioButton rbTarjeta = findViewById(R.id.rbTarjeta);
        rbTarjeta.setOnClickListener(v -> mostrarDialogoTarjeta());
    }

    private void mostrarDialogoTarjeta() {
        Dialog dialogTarjeta = new Dialog(PagoActivity.this);
        dialogTarjeta.setContentView(R.layout.dialog_tarjeta);
        dialogTarjeta.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText etNombre = dialogTarjeta.findViewById(R.id.etNombreTitular);
        EditText etNumero = dialogTarjeta.findViewById(R.id.etNumeroTarjeta);
        EditText etMes = dialogTarjeta.findViewById(R.id.etMes);
        EditText etAno = dialogTarjeta.findViewById(R.id.etAno);
        EditText etCvv = dialogTarjeta.findViewById(R.id.etCvv);
        EditText etPais = dialogTarjeta.findViewById(R.id.etPais);
        EditText etPostal = dialogTarjeta.findViewById(R.id.etPostal);
        Button btnConfirmar = dialogTarjeta.findViewById(R.id.btnConfirmarPago);

        // Inicialmente deshabilitado
        btnConfirmar.setEnabled(false);
        btnConfirmar.setAlpha(0.5f);

        btnConfirmar.setOnClickListener(_view -> {

            dialogTarjeta.dismiss();

            guardarReservaEnFirestore(
                    getIntent().getStringExtra("tourId"),
                    getIntent().getStringExtra("titulo"),
                    getIntent().getStringExtra("fecha"),
                    getIntent().getStringExtra("hora"),
                    getIntent().getStringExtra("viajeros"),
                    getIntent().getStringExtra("precio"),
                    getIntent().getStringExtra("img")  // ⭐ NUEVO
            );


            mostrarDialogoReserva();
        });


        // 🔹 Validador de campos
        Runnable validarCampos = () -> {
            boolean completos =
                    !etNombre.getText().toString().trim().isEmpty() &&
                            !etNumero.getText().toString().trim().isEmpty() &&
                            !etMes.getText().toString().trim().isEmpty() &&
                            !etAno.getText().toString().trim().isEmpty() &&
                            !etCvv.getText().toString().trim().isEmpty() &&
                            !etPais.getText().toString().trim().isEmpty() &&
                            !etPostal.getText().toString().trim().isEmpty();

            btnConfirmar.setEnabled(completos);
            btnConfirmar.setAlpha(completos ? 1f : 0.5f);
        };

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validarCampos.run(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        etNombre.addTextChangedListener(watcher);
        etNumero.addTextChangedListener(watcher);
        etMes.addTextChangedListener(watcher);
        etAno.addTextChangedListener(watcher);
        etCvv.addTextChangedListener(watcher);
        etPais.addTextChangedListener(watcher);
        etPostal.addTextChangedListener(watcher);

        validarCampos.run();
        dialogTarjeta.show();
    }

    private void mostrarDialogoReserva() {
        Dialog dialog = new Dialog(PagoActivity.this);
        dialog.setContentView(R.layout.dialog_reserva_registrada);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button btnCerrar = dialog.findViewById(R.id.btnCloseReserva);

        btnCerrar.setOnClickListener(v -> {
            dialog.dismiss();

            Intent intent = new Intent(PagoActivity.this, ToursActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // 🔹 VERIFICAR Y PEDIR PERMISO ANTES DE ENVIAR NOTIFICACIÓN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permiso ya concedido
                enviarNotificacion();
            } else {
                // Pedir permiso
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // Android 12 o menor, no necesita permiso runtime
            enviarNotificacion();
        }

        dialog.show();
    }

    private void guardarReservaEnFirestore(String tourId, String titulo, String fecha,
                                           String hora, String viajeros, String precio,
                                           String imageUrl) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        String idCliente = mAuth.getCurrentUser().getUid();

        Map<String, Object> reserva = new HashMap<>();
        reserva.put("idCliente", idCliente);
        reserva.put("idTour", tourId);
        reserva.put("titulo", titulo);
        reserva.put("fecha", fecha);
        reserva.put("hora", hora);
        reserva.put("viajeros", viajeros);
        reserva.put("precio", precio);
        reserva.put("estado", "Reservado");
        reserva.put("timestamp", System.currentTimeMillis());
        reserva.put("imageUrl", imageUrl);

        db.collection("reservas")
                .add(reserva)
                .addOnSuccessListener(r -> {
                    System.out.println("✔ RESERVA GUARDADA CON IMAGEN");
                })
                .addOnFailureListener(e -> {
                    System.out.println("❌ ERROR FIRESTORE " + e.getMessage());
                });
    }



    // 🔹 MÉTODO SEPARADO PARA ENVIAR LA NOTIFICACIÓN
    private void enviarNotificacion() {
        Intent notifIntent = new Intent(PagoActivity.this, HistorialActivity.class);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                PagoActivity.this, 0, notifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(PagoActivity.this, "puriqtours_channel")
                .setSmallIcon(R.drawable.ic_check_circle)
                .setContentTitle("Reserva registrada")
                .setContentText("Tu reserva fue registrada con éxito. Revisa tu historial 🏞️")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(PagoActivity.this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                manager.notify(1002, builder.build());
            }
        } else {
            manager.notify(1002, builder.build());
        }
    }
}
