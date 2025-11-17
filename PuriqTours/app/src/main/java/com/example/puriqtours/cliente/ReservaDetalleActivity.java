package com.example.puriqtours.cliente;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.puriqtours.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class ReservaDetalleActivity extends AppCompatActivity {

    // ✅ CORREGIDO: Usar los IDs que SÍ existen en el XML
    private TextView tvTitulo, tvEstado, tvFecha, tvPersonas, tvCosto, tvPago;
    private ImageView imgTour, imgQR;
    private ImageButton btnBack;

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

        inicializarVistas();
        cargarDatos();
    }

    private void inicializarVistas() {
        tvTitulo = findViewById(R.id.tvTitulo);
        tvEstado = findViewById(R.id.tvEstado);
        tvFecha = findViewById(R.id.tvFecha);
        tvPersonas = findViewById(R.id.tvPersonas);
        tvCosto = findViewById(R.id.tvCosto);      // ✅ Cambiado de tvPrecio
        tvPago = findViewById(R.id.tvPago);
        imgTour = findViewById(R.id.imgTour);
        imgQR = findViewById(R.id.imgQR);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
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
        String pago = doc.getString("medioPago");
        String img = doc.getString("imageUrl");

        tvTitulo.setText(titulo);
        tvEstado.setText("Estado: " + estado);
        tvFecha.setText("Fecha: " + fecha);
        tvPersonas.setText("Personas: " + viajeros);
        tvCosto.setText("" + precio);  // ✅ Ahora usa tvCosto
        tvPago.setText("Medio de pago: " + (pago != null ? pago : "No registrado"));

        Glide.with(this)
                .load(img)
                .placeholder(R.drawable.kuelap)
                .into(imgTour);

        generarQR(reservaId, titulo, fecha, viajeros);
    }

    private void generarQR(String id, String titulo, String fecha, String viajeros) {
        try {
            String data = "RESERVA:" + id + "\n" +
                    "TOUR:" + titulo + "\n" +
                    "FECHA:" + fecha + "\n" +
                    "VIAJEROS:" + viajeros;

            BitMatrix matrix = new QRCodeWriter()
                    .encode(data, BarcodeFormat.QR_CODE, 500, 500);

            Bitmap bmp = Bitmap.createBitmap(500, 500, Bitmap.Config.RGB_565);

            for (int x = 0; x < 500; x++) {
                for (int y = 0; y < 500; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            imgQR.setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}