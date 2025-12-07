package com.example.puriqtours.guia;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.puriqtours.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IniciarTourActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;

    private DecoratedBarcodeView barcodeView;
    private Button btnEscanear;
    private FirebaseFirestore db;

    private String idReserva;
    private String idTour;
    private boolean escaneoActivo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_tour);

        db = FirebaseFirestore.getInstance();

        // Recibir datos desde ToursFragment
        idReserva = getIntent().getStringExtra("idReserva");
        idTour = getIntent().getStringExtra("idTour");

        inicializarVistas();
        configurarBotonEscanear();
    }

    private void inicializarVistas() {
        btnEscanear = findViewById(R.id.btnEscanear);
        // La vista del escáner se inicializará cuando se presione el botón
    }

    private void configurarBotonEscanear() {
        btnEscanear.setOnClickListener(v -> {
            if (verificarPermisosCamara()) {
                iniciarEscanerQR();
            } else {
                solicitarPermisosCamara();
            }
        });
    }

    private boolean verificarPermisosCamara() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void solicitarPermisosCamara() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_CODE);
    }

    private void iniciarEscanerQR() {
        // Cambiar a layout con escáner
        setContentView(R.layout.activity_qr_scanner);

        barcodeView = findViewById(R.id.barcode_scanner);
        escaneoActivo = true;

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null && escaneoActivo) {
                    escaneoActivo = false;
                    procesarQRInicio(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        });
    }

    private void procesarQRInicio(String contenidoQr) {
        barcodeView.pause();

        // Extraer el ID de la reserva del QR
        String idReservaQr = extraerIdReserva(contenidoQr);

        if (idReservaQr == null || !contenidoQr.contains("_inicio_")) {
            Toast.makeText(this, "❌ QR inválido o no es de inicio", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Verificar que el QR sea de la reserva correcta
        if (!idReservaQr.equals(idReserva)) {
            Toast.makeText(this, "❌ Este QR no corresponde a esta reserva", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Validar contra Firestore
        validarYActualizarReserva(contenidoQr);
    }

    private String extraerIdReserva(String contenidoQr) {
        // El formato del QR es: "idReserva_tipo_timestamp"
        if (contenidoQr.contains("_")) {
            return contenidoQr.split("_")[0];
        }
        return null;
    }

    private void validarYActualizarReserva(String qrEscaneado) {
        db.collection("reservas")
                .document(idReserva)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "❌ Reserva no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String tokenInicio = doc.getString("tokenInicio");

                    if (tokenInicio == null || !tokenInicio.equals(qrEscaneado)) {
                        Toast.makeText(this, "❌ QR no válido para esta reserva", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Actualizar estado a "En proceso"
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "En proceso");
                    updates.put("checkInTime", FieldValue.serverTimestamp());

                    db.collection("reservas")
                            .document(idReserva)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "✅ Check-in exitoso - Tour iniciado", Toast.LENGTH_LONG).show();
                                abrirMapa();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "❌ Error al registrar check-in", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Error al verificar QR", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void abrirMapa() {
        Intent intent = new Intent(this, MapaTourActivity.class);
        intent.putExtra("idReserva", idReserva);
        intent.putExtra("idTour", idTour);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarEscanerQR();
            } else {
                Toast.makeText(this, "⚠️ Permiso de cámara requerido", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null && escaneoActivo) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }
}
