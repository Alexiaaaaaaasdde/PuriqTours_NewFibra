package com.example.puriqtours.guia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;

import android.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.puriqtours.entity.ReservaIndividual;
import com.example.puriqtours.entity.TourGuia;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IniciarTourActivity extends AppCompatActivity {

    private String idReserva;
    private String idTour;
    private Long clientesTotales, clientesVerificados;
    private List<ReservaIndividual> listaReservas = new ArrayList<>();
    private Button btnIniciarTour, btnEscanear;
    private TextView tvClientesContador;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_tour);

        //Referencias a las vistas
        tvClientesContador = findViewById(R.id.tvClientesContador);
        btnIniciarTour = findViewById(R.id.btnIniciarTour);
        btnEscanear = findViewById(R.id.btnEscanear);
        //Predeterminado
        btnIniciarTour.setEnabled(false);

        db = FirebaseFirestore.getInstance();

        // 🔹 Recibir datos desde ToursFragment
        idReserva = getIntent().getStringExtra("idReserva");
        idTour = getIntent().getStringExtra("idTour");

        cargarInformacionReserva();
        actualizarContadorVista();

        evaluarInicioTour();

        // Mostrar el diálogo para ingresar el token
        btnIniciarTour.setOnClickListener(v -> {
            actualizarEstadoReserva();
            abrirMapa();
        });
        btnEscanear.setOnClickListener(v -> {
            abrirCamaraQR();
        });
    }


    private void abrirMapa() {
        Toast.makeText(this, "Token validado correctamente ✔", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MapaTourActivity.class);
        intent.putExtra("idReserva", idReserva);
        startActivity(intent);
        finish();
    }

    private void actualizarEstadoReserva() {

        db.collection("reservas")
                .document(idReserva)
                .update("status", "En proceso")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Tour iniciado ✔", Toast.LENGTH_SHORT).show();
                    abrirMapa();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar estado", Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarClientesVerificados(String idReservaIndividual){
        //Actualizar número de clientes verificados
        db.collection("reservas")
                        .document(idReserva)
                        .update("verifiedClients", clientesVerificados);

        //Actualizar estado de Cliente
        db.collection("reservas")
                .document(idReserva)
                .collection("reservaIndividual")
                .document(idReservaIndividual)
                .update("verified", true);

    }

    private void cargarInformacionReserva(){
        db.collection("reservas")
                .document(idReserva)
                .get()
                .addOnSuccessListener(doc -> {
                    clientesTotales = doc.getLong("totalClients");
                    clientesVerificados = doc.getLong("verifiedClients");
                });
        cargarInformacionReservasIndividuales();
    }
    private void cargarInformacionReservasIndividuales(){
        db.collection("reservas")
                .document(idReserva)
                .collection("reservaIndividual")
                .get()
                .addOnSuccessListener(reservaDoc -> {

                    listaReservas.clear();

                    for (DocumentSnapshot doc : reservaDoc){
                        ReservaIndividual ri = doc.toObject(ReservaIndividual.class);
                        ri.setIdReservaIndividual(doc.getId());
                        listaReservas.add(ri);
                    }
                })
                .addOnFailureListener(e ->{
                    Log.e("FIREBASE", "Error cargando las reservas individuales");
                });
    }

    private void abrirCamaraQR() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea el QR de inicio del tour");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    //Para recepcionar los datos del QR
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            } else {
                procesarQR(result.getContents());
            }
        }
    }

    private void procesarQR(String contenidoQR) {
        try {
            JSONObject json = new JSONObject(contenidoQR);

            String idClienteQR = json.getString("idCliente");
            String tokenStartQR = json.getString("tokenStart");
            String idReservaQR = json.getString("idReserva");

            // ✅ Validación básica
            if (idClienteQR.isEmpty() || tokenStartQR.isEmpty() || idReservaQR.isEmpty()) {
                Toast.makeText(this, "QR inválido", Toast.LENGTH_SHORT).show();
                Log.e("QR", "El QR no tiene los datos completos");
            } else {
                if(idReservaQR.equals(idReserva)){
                    for (ReservaIndividual reserva : listaReservas){
                        if(reserva.getIdCliente().equals(idClienteQR)){
                            if(reserva.getTokenStart().equals(tokenStartQR)){
                                if(reserva.isVerified()){
                                    Toast.makeText(this, "El cliente ya está validado", Toast.LENGTH_SHORT).show();
                                    Log.e("QR", "El clienta ya está validado");
                                } else {
                                    clientesVerificados += 1;
                                    actualizarClientesVerificados(reserva.getIdReservaIndividual());
                                    evaluarInicioTour();
                                    actualizarContadorVista();
                                }
                            }else {
                                Toast.makeText(this, "QR inválido", Toast.LENGTH_SHORT).show();
                                Log.e("QR", "El Token de Inicio no coincide");
                                return;
                            }
                        }
                    }
                    Toast.makeText(this, "QR inválido", Toast.LENGTH_SHORT).show();
                    Log.e("QR", "No se encontró el ID del Cliente");
                } else {
                    Toast.makeText(this, "QR inválido", Toast.LENGTH_SHORT).show();
                    Log.e("QR", "El ID de reserva no concuerda");
                }
            }

        } catch (JSONException e) {
            Toast.makeText(this, "El QR no es válido", Toast.LENGTH_SHORT).show();
            Log.e("QR", "Error parsing JSON", e);
        }
    }

    private void evaluarInicioTour() {

        if (clientesTotales <= 0) {
            btnIniciarTour.setEnabled(false);
            return;
        }

        // Mitad o más (redondeo hacia arriba)
        int minimoRequerido = (int) Math.ceil(clientesTotales / 2.0);

        boolean puedeIniciar = clientesVerificados >= minimoRequerido;

        btnIniciarTour.setEnabled(puedeIniciar);

        // (Opcional) feedback visual
        btnIniciarTour.setAlpha(puedeIniciar ? 1.0f : 0.5f);
    }

    @SuppressLint("SetTextI18n")
    private void actualizarContadorVista(){
        tvClientesContador.setText(clientesVerificados + "/" + clientesTotales);
        Log.e("CONTROL", "Clientes Verificados: " + clientesVerificados + ", Clientes Totales: " + clientesTotales);
    }




}
