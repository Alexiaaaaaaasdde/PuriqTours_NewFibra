package com.example.puriqtours;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.PendingIntent;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PagoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);

        // Referencias
        TextView tvFechaPago = findViewById(R.id.tvFechaPago);
        TextView tvViajerosPago = findViewById(R.id.tvViajerosPago);
        TextView tvPrecioPago = findViewById(R.id.tvPrecioPago);
        TextView tvHoraPago = findViewById(R.id.tvHoraPago);

        // Recuperar datos del intent
        String fecha = getIntent().getStringExtra("fecha");
        String viajeros = getIntent().getStringExtra("viajeros");
        String precio = getIntent().getStringExtra("precio");
        String hora = getIntent().getStringExtra("hora");

        // Setear valores
        if (fecha != null) tvFechaPago.setText("Fecha: " + fecha);
        if (viajeros != null) tvViajerosPago.setText("Viajeros: " + viajeros);
        if (precio != null) tvPrecioPago.setText("Total: " + precio);
        if (hora != null) tvHoraPago.setText("Hora: " + hora);
        // 🔹 Configurar navegación inferior
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_tours); // seleccionamos por defecto "Tours"

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

        RadioButton rbTarjeta = findViewById(R.id.rbTarjeta);

        rbTarjeta.setOnClickListener(v -> {
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

                Dialog dialogReserva = new Dialog(PagoActivity.this);
                dialogReserva.setContentView(R.layout.dialog_reserva_registrada);
                dialogReserva.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                dialogReserva.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT)
                );

                Button btnClose = dialogReserva.findViewById(R.id.btnCloseReserva);
                btnClose.setOnClickListener(view -> {
                    dialogReserva.dismiss();

                    // 👉 Redirigir a la lista de tours
                    Intent intent = new Intent(PagoActivity.this, ToursActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });

                // 🔔 Mostrar notificación de reserva registrada
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

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(PagoActivity.this);

                // 🔹 Verificar permiso en Android 13+ (API 33)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        // Si no está permitido, pedirlo
                        requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                        return; // salir para no lanzar notificación sin permiso
                    }
                }

                // ✅ Mostrar notificación
                notificationManager.notify(1001, builder.build());
                dialogReserva.show();
            });


            // 🔹 Método para validar todos los campos
            Runnable validarCampos = () -> {
                String nombre = etNombre.getText().toString().trim();
                String numero = etNumero.getText().toString().trim();
                String mes = etMes.getText().toString().trim();
                String ano = etAno.getText().toString().trim();
                String cvv = etCvv.getText().toString().trim();
                String pais = etPais.getText().toString().trim();
                String postal = etPostal.getText().toString().trim();

                boolean completos = !nombre.isEmpty() && !numero.isEmpty() &&
                        !mes.isEmpty() && !ano.isEmpty() && !cvv.isEmpty() &&
                        !pais.isEmpty() && !postal.isEmpty();

                btnConfirmar.setEnabled(completos);
                btnConfirmar.setAlpha(completos ? 1f : 0.5f);
            };

            // 🔹 TextWatcher directo
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

            validarCampos.run(); // ✅ valida al inicio

            dialogTarjeta.show();
        });
    }
}
