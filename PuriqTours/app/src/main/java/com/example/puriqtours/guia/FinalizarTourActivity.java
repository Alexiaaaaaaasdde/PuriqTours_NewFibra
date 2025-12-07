package com.example.puriqtours.guia;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.puriqtours.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.HashMap;
import java.util.Map;

public class FinalizarTourActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 200;

    private DecoratedBarcodeView barcodeView;
    private String idReserva;

    private FirebaseFirestore db;
    private boolean escaneoActivo = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner_finalizar);

        db = FirebaseFirestore.getInstance();
        idReserva = getIntent().getStringExtra("idReserva");

        barcodeView = findViewById(R.id.barcode_scanner);

        if (verificarPermisos()) iniciarEscaner();
        else solicitarPermisos();
    }

    private boolean verificarPermisos() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void solicitarPermisos() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_CODE);
    }

    private void iniciarEscaner() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (!escaneoActivo) return;

                String qr = result.getText();
                escaneoActivo = false;

                procesarQR(qr);
            }
        });
    }

    private void procesarQR(String qr) {
        if (!qr.contains(idReserva) || !qr.contains("_fin_")) {
            Toast.makeText(this, "❌ QR inválido o no es QR-Fin", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db.collection("reservas")
                .document(idReserva)
                .get()
                .addOnSuccessListener(doc -> {

                    String esperado = doc.getString("tokenFin");

                    if (esperado == null || !esperado.equals(qr)) {
                        Toast.makeText(this, "❌ QR-Fin no coincide", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    // Actualizar la reserva como finalizada
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "Finalizado");
                    updates.put("checkOutTime", Timestamp.now());

                    String idCliente = doc.getString("idCliente");

                    db.collection("reservas")
                            .document(idReserva)
                            .update(updates)
                            .addOnSuccessListener(a -> {

                                Toast.makeText(this,
                                        "✅ Tour finalizado correctamente",
                                        Toast.LENGTH_LONG).show();

                                // Enviar notificación al cliente (Firestore Listener)
                                enviarNotificacionDeCobro(idCliente);

                                finish();
                            });
                });
    }

    // 🔥 ESTE MÉTODO **SÍ FUNCIONA** — SIN SERVER KEY
    private void enviarNotificacionDeCobro(String idCliente) {

        if (idCliente == null) {
            Log.e("Notificacion", "idCliente nulo");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("titulo", "Tour finalizado");
        data.put("mensaje", "Se ha realizado el cobro de tu tour");
        data.put("timestamp", Timestamp.now());

        FirebaseFirestore.getInstance()
                .collection("notificaciones")
                .document(idCliente)
                .set(data)
                .addOnSuccessListener(a -> Log.d("Notificacion", "Notificación enviada"))
                .addOnFailureListener(e -> Log.e("Notificacion", "Error al enviar", e));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // ⭐ AGREGADO

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                iniciarEscaner();

            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
