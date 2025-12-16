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
import org.json.JSONObject;
import org.json.JSONException;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.puriqtours.BaseActivity;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.HistorialTour;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
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
    private HistorialTour historialTour = new HistorialTour();
    private LinearLayout layoutQrFin;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String reservaId, qrString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserva_detalle);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
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

        historialTour.setTitulo(doc.getString("title"));
        historialTour.setFecha(doc.getString("date"));
        historialTour.setEstado(doc.getString("status"));
        historialTour.setImageUrl(doc.getString("imageUrl"));

        db.collection("reservas")
                .document(reservaId)
                .collection("reservaIndividual")
                .whereEqualTo("idCliente", auth.getCurrentUser().getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if(!querySnapshot.isEmpty()){
                        DocumentSnapshot doc2 = querySnapshot.getDocuments().get(0);
                        historialTour.setViajeros(doc2.getString("travelers"));
                        String pago = doc2.getString("metodoPago");
                        historialTour.setPrecio(doc2.getDouble("price"));
                        historialTour.setTokenInicio(doc2.getString("tokenStart"));
                        historialTour.setTokenFin(doc2.getString("tokenEnd"));



                        if (historialTour.getEstado() == null) historialTour.setEstado("Desconocido");
                        //Si el tour ya finalizó → NO mostrar QR de inicio
                        if (historialTour.getEstado().equalsIgnoreCase("Finalizado")) {
                            imgQrInicio.setImageAlpha(50); // semitransparente o desactivado
                        }
                        if (historialTour.getEstado().equalsIgnoreCase("En proceso")) {
                            imgQrInicio.setImageAlpha(50); // semitransparente o desactivado
                        }

                        //Si el tour está Finalizado → QR FINAL debe mostrarse SIEMPRE
                        if (historialTour.getEstado().equalsIgnoreCase("Finalizado")) {
                            tvQrFinBloqueado.setVisibility(View.GONE);
                            layoutQrFin.setVisibility(View.VISIBLE);

                            // si hay token de fin → mostrar QR final
                            if (historialTour.getTokenFin() != null && !historialTour.getTokenFin().isEmpty()) {
                                qrString = generarJSON("tokenFin");
                                generarYMostrarQR(qrString, imgQrFin);
                            }
                        }


                        tvTitulo.setText(historialTour.getTitulo());
                        tvEstado.setText("Estado: " + historialTour.getEstado());
                        tvFecha.setText("Fecha: " + historialTour.getFecha());
                        tvPersonas.setText("Personas: " + historialTour.getViajeros());
                        tvCosto.setText(historialTour.getPrecio().toString());
                        tvPago.setText("Medio de pago: " + (pago != null ? pago : "No registrado"));

                        Glide.with(this)
                                .load(historialTour.getImageUrl())
                                .placeholder(R.drawable.kuelap)
                                .into(imgTour);

                        // QR Inicio
                        if (historialTour.getTokenInicio() != null && !historialTour.getTokenInicio().isEmpty()) {
                            qrString = generarJSON("tokenInicio");
                            generarYMostrarQR(qrString, imgQrInicio);
                        }

                        // QR Fin
                        if (historialTour.getEstado().equalsIgnoreCase("En proceso")) {
                            tvQrFinBloqueado.setVisibility(View.GONE);
                            layoutQrFin.setVisibility(View.VISIBLE);

                            if (historialTour.getTokenFin() != null && !historialTour.getTokenFin().isEmpty()) {
                                qrString = generarJSON("tokenFin");
                                generarYMostrarQR(qrString, imgQrFin);
                            }

                        } else if (historialTour.getEstado().equalsIgnoreCase("Finalizado")) {
                            tvQrFinBloqueado.setVisibility(View.GONE);
                            layoutQrFin.setVisibility(View.VISIBLE);

                            if (historialTour.getTokenFin() != null && !historialTour.getTokenFin().isEmpty()) {
                                qrString = generarJSON("tokenFin");
                                generarYMostrarQR(qrString, imgQrFin);
                            }

                        } else {
                            tvQrFinBloqueado.setVisibility(View.VISIBLE);
                            layoutQrFin.setVisibility(View.GONE);
                        }
                    }
                });

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
    
    private String generarJSON(String token){
        JSONObject json = new JSONObject();
        String jsonString = "";
        if(token.equals("tokenInicio")){
            try {
                json.put("idCliente", auth.getCurrentUser().getUid());
                json.put("tokenStart", historialTour.getTokenInicio());
                json.put("idReserva", reservaId);
                jsonString = json.toString();
            }catch (JSONException e){
                e.printStackTrace();
            }
        } else if (token.equals("tokenFin")) {
            try {
                json.put("idCliente", auth.getCurrentUser().getUid());
                json.put("tokenEnd", historialTour.getTokenFin());
                json.put("idReserva", reservaId);
                jsonString = json.toString();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return jsonString;
    }
}
