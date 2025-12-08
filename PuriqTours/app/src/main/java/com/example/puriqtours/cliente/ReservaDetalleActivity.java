package com.example.puriqtours.cliente;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.puriqtours.BaseActivity;
import com.example.puriqtours.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class ReservaDetalleActivity extends BaseActivity {

    private TextView tvTitulo, tvEstado, tvFecha, tvPersonas, tvCosto, tvPago;
    private TextView tvQrFinBloqueado;
    private ImageView imgTour, imgQrInicio, imgQrFin;
    private LinearLayout layoutQrFin;

    private FirebaseFirestore db;
    private String reservaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserva_detalle);

        db = FirebaseFirestore.getInstance();
        reservaId = getIntent().getStringExtra("RESERVA_ID");

        if (reservaId == null) {
            Toast.makeText(this, "Error: reserva no recibida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ⭐ Usa el toolbar de BaseActivity con foto y menú
        setupSharedToolbar();

        inicializarVistas();
        configurarNavegacion();
        cargarDatos();
    }

    private void configurarNavegacion() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setSelectedItemId(R.id.nav_historial);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void inicializarVistas() {
        tvTitulo = findViewById(R.id.tvTitulo);
        tvEstado = findViewById(R.id.tvEstado);
        tvFecha = findViewById(R.id.tvFecha);
        tvPersonas = findViewById(R.id.tvPersonas);
        tvCosto = findViewById(R.id.tvCosto);
        tvPago = findViewById(R.id.tvPago);
        imgTour = findViewById(R.id.imgTour);

        imgQrInicio = findViewById(R.id.imgQrInicio);
        imgQrFin = findViewById(R.id.imgQrFin);
        tvQrFinBloqueado = findViewById(R.id.tvQrFinBloqueado);
        layoutQrFin = findViewById(R.id.layoutQrFin);
    }

    private void cargarDatos() {
        db.collection("reservas")
                .document(reservaId)
                .get()
                .addOnSuccessListener(this::mostrarDatos)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void mostrarDatos(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Reserva no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String titulo = doc.getString("titulo");
        String fecha = doc.getString("fecha");
        String estado = doc.getString("estado");
        String precio = doc.getString("precio");
        String viajeros = doc.getString("viajeros");
        String pago = doc.getString("metodoPago");
        String img = doc.getString("imageUrl");
        String tokenInicio = doc.getString("tokenInicio");
        String tokenFin = doc.getString("tokenFin");

        if (estado == null) estado = "Desconocido";

        //Si el tour ya finalizó → NO mostrar QR de inicio
        if (estado.equalsIgnoreCase("Finalizado")) {
            imgQrInicio.setImageAlpha(50); // semitransparente o desactivado
        }

        //Si el tour está Finalizado → QR FINAL debe mostrarse SIEMPRE
        if (estado.equalsIgnoreCase("Finalizado")) {
            tvQrFinBloqueado.setVisibility(View.GONE);
            layoutQrFin.setVisibility(View.VISIBLE);

            // si hay token de fin → mostrar QR final
            if (tokenFin != null && !tokenFin.isEmpty()) {
                generarYMostrarQR(tokenFin, imgQrFin);
            } else {
                generarYGuardarToken(reservaId, "fin");
            }
        }


        tvTitulo.setText(titulo);
        tvEstado.setText("Estado: " + estado);
        tvFecha.setText("Fecha: " + fecha);
        tvPersonas.setText("Personas: " + viajeros);
        tvCosto.setText(precio);
        tvPago.setText("Medio de pago: " + (pago != null ? pago : "No registrado"));

        Glide.with(this)
                .load(img)
                .placeholder(R.drawable.kuelap)
                .into(imgTour);

        // QR Inicio
        if (tokenInicio != null && !tokenInicio.isEmpty()) {
            generarYMostrarQR(tokenInicio, imgQrInicio);
        } else {
            generarYGuardarToken(reservaId, "inicio");
        }

        // QR Fin
        if (estado.equalsIgnoreCase("En proceso")) {
            tvQrFinBloqueado.setVisibility(View.GONE);
            layoutQrFin.setVisibility(View.VISIBLE);

            if (tokenFin != null && !tokenFin.isEmpty()) {
                generarYMostrarQR(tokenFin, imgQrFin);
            } else {
                generarYGuardarToken(reservaId, "fin");
            }

        } else if (estado.equalsIgnoreCase("Finalizado")) {
            tvQrFinBloqueado.setVisibility(View.GONE);
            layoutQrFin.setVisibility(View.VISIBLE);

            if (tokenFin != null && !tokenFin.isEmpty()) {
                generarYMostrarQR(tokenFin, imgQrFin);
            }

        } else {
            tvQrFinBloqueado.setVisibility(View.VISIBLE);
            layoutQrFin.setVisibility(View.GONE);
        }
    }

    private void generarYGuardarToken(String reservaId, String tipo) {
        String token = reservaId + "_" + tipo + "_" + System.currentTimeMillis();

        String campo = tipo.equals("inicio") ? "tokenInicio" : "tokenFin";
        db.collection("reservas")
                .document(reservaId)
                .update(campo, token)
                .addOnSuccessListener(aVoid -> {
                    ImageView targetImg = tipo.equals("inicio") ? imgQrInicio : imgQrFin;
                    generarYMostrarQR(token, targetImg);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al generar token", Toast.LENGTH_SHORT).show()
                );
    }

    private void generarYMostrarQR(String contenido, ImageView imageView) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, 500, 500);

            Bitmap bmp = Bitmap.createBitmap(500, 500, Bitmap.Config.RGB_565);

            for (int x = 0; x < 500; x++) {
                for (int y = 0; y < 500; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            imageView.setImageBitmap(bmp);

        } catch (Exception e) {
            Toast.makeText(this, "Error al generar código QR", Toast.LENGTH_SHORT).show();
        }
    }
}
