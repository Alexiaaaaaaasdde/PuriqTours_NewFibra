package com.example.puriqtours.guia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.TestLooperManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.ReservaIndividual;
import com.example.puriqtours.utils.NotificationHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FinalizarTourBottomSheet extends BottomSheetDialogFragment {

    private Long clientesVerificados = 0L;
    private Long clientesFinalizados = 0L;
    private String idReserva;
    private List<ReservaIndividual> listaVerificados = new ArrayList<>();
    private Button btnFinalizarTour, btnEscanearQRFinalizar;
    private ActivityResultLauncher<Intent> qrLauncher;
    private TextView tvClientesFinalizadoContador;

    private FirebaseFirestore db;
    private String tourId;
    private String tourName;
    private String guideId;
    private String guideName;

    public static FinalizarTourBottomSheet newInstance(String idReserva) {
        FinalizarTourBottomSheet sheet = new FinalizarTourBottomSheet();
        Bundle args = new Bundle();
        args.putString("idReserva", idReserva);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_finalizar_tour, container, false);

        btnFinalizarTour = view.findViewById(R.id.btnFinalizarTour);
        btnEscanearQRFinalizar = view.findViewById(R.id.btnEscanearQRFinalizar);
        tvClientesFinalizadoContador = view.findViewById(R.id.tvClientesFinalizadoContador);

        if (getArguments() != null) {
            idReserva = getArguments().getString("idReserva");
        }
        Log.e("FIRESTORE", "ID Reserva: " + idReserva);

        db = FirebaseFirestore.getInstance();
        cargarInformacionReserva();

        qrLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                        IntentResult intentResult =
                                IntentIntegrator.parseActivityResult(
                                        IntentIntegrator.REQUEST_CODE,
                                        result.getResultCode(),
                                        result.getData()
                                );

                        if (intentResult != null) {
                            if (intentResult.getContents() == null) {
                                Toast.makeText(getContext(),
                                        "Escaneo cancelado",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                procesarQR(intentResult.getContents());
                            }
                        }
                    }
                }
        );

        btnFinalizarTour.setEnabled(false);
        btnFinalizarTour.setOnClickListener(v -> {
            dismiss();
            finalizarTour();

            Intent intent = new Intent(requireContext(), MainGuiaActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            requireActivity().finish();
        });
        btnEscanearQRFinalizar.setOnClickListener(v -> abrirCamaraQR());

        return view;
    }


    // ============================================================================
    // ESCANEAR QR
    // ============================================================================
    private void abrirCamaraQR() {

        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea el QR de finalización");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        qrLauncher.launch(integrator.createScanIntent());
    }

    // ============================================================================
    // PROCESAR DATOS DEL QR
    // ============================================================================
    private void procesarQR(String contenidoQR) {
        try {
            JSONObject json = new JSONObject(contenidoQR);

            String idClienteQR = json.getString("idCliente");
            String tokenEndQR = json.getString("tokenEnd");
            String idReservaQR = json.getString("idReserva");

            // ✅ Validación básica
            if (idClienteQR.isEmpty() || tokenEndQR.isEmpty() || idReservaQR.isEmpty()) {
                Toast.makeText(getContext(), "QR inválido", Toast.LENGTH_SHORT).show();
                Log.e("QR", "El QR no tiene los datos completos");
            } else {
                if(idReservaQR.equals(idReserva)){
                    for (ReservaIndividual reserva : listaVerificados){
                        if(reserva.getIdCliente().equals(idClienteQR)){
                            if(reserva.getTokenEnd().equals(tokenEndQR)){
                                if(reserva.isFinished()){
                                    Toast.makeText(getContext(), "El cliente ya finalizó su Tour", Toast.LENGTH_SHORT).show();
                                    Log.e("QR", "El clienta ya finalizó su Tour");
                                } else {
                                    clientesFinalizados += 1;
                                    actualizarClientesFinalizados(reserva.getIdReservaIndividual());
                                    evaluarFinalizarTour();
                                    actualizarContadorVista();
                                }
                            }else {
                                Toast.makeText(getContext(), "QR inválido", Toast.LENGTH_SHORT).show();
                                Log.e("QR", "El Token de Inicio no coincide");
                            }
                            return;
                        }
                    }
                    Toast.makeText(getContext(), "QR inválido", Toast.LENGTH_SHORT).show();
                    Log.e("QR", "No se encontró el ID del Cliente");
                } else {
                    Toast.makeText(getContext(), "QR inválido", Toast.LENGTH_SHORT).show();
                    Log.e("QR", "El ID de reserva no concuerda");
                }
            }

        } catch (JSONException e) {
            Toast.makeText(getContext(), "El QR no es válido", Toast.LENGTH_SHORT).show();
            Log.e("QR", "Error parsing JSON", e);
        }
    }

    private void evaluarFinalizarTour() {

        if (clientesVerificados <= 0) {
            btnFinalizarTour.setEnabled(false);
            return;
        }

        boolean puedeIniciar = clientesFinalizados >= clientesVerificados;

        btnFinalizarTour.setEnabled(puedeIniciar);

        // (Opcional) feedback visual
        btnFinalizarTour.setAlpha(puedeIniciar ? 1.0f : 0.5f);
    }

    private void actualizarClientesFinalizados(String idReservaIndividual){
        //Actualizar número de clientes verificados
        db.collection("reservas")
                .document(idReserva)
                .update("finishedClients", clientesFinalizados);

        //Actualizar estado de Cliente
        db.collection("reservas")
                .document(idReserva)
                .collection("reservaIndividual")
                .document(idReservaIndividual)
                .update("finished", true);

    }

    private void finalizarTour(){
        db.collection("reservas")
                .document(idReserva)
                .update("status", "Finalizado")
                .addOnSuccessListener(aVoid -> {
                    // Crear notificación local para el admin
                    crearNotificacionParaAdmin();
                });
    }

    private void crearNotificacionParaAdmin() {
        // Obtener datos del tour y guía
        db.collection("reservas")
                .document(idReserva)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tourId = doc.getString("tourId");
                        tourName = doc.getString("tourName");
                        guideId = doc.getString("guideId");
                        
                        // Obtener nombre del guía
                        if (guideId != null) {
                            db.collection("users")
                                    .document(guideId)
                                    .get()
                                    .addOnSuccessListener(guideDoc -> {
                                        if (guideDoc.exists()) {
                                            guideName = guideDoc.getString("name");
                                            
                                            // Crear la notificación
                                            NotificationHelper helper = new NotificationHelper(requireContext());
                                            helper.addNotification(
                                                    "tour_finalizado",
                                                    tourId != null ? tourId : "",
                                                    tourName != null ? tourName : "Tour",
                                                    idReserva,
                                                    guideName != null ? guideName : "Guía",
                                                    clientesFinalizados.intValue()
                                            );
                                            
                                            Log.d("NOTIFICATION", "Notificación creada para el admin");
                                        }
                                    });
                        }
                    }
                });
    }

    // ============================================================================
    // CARGAR DATOS DE RESERVAS
    // ============================================================================
    private void cargarInformacionReserva(){
        db.collection("reservas")
                .document(idReserva)
                .get()
                .addOnSuccessListener(doc -> {
                    clientesVerificados = doc.getLong("verifiedClients");
                    clientesFinalizados = doc.getLong("finishedClients");
                    actualizarContadorVista();
                    evaluarFinalizarTour();
                    Log.e("FIRESTORE", "Clientes finalizados: " + doc.getLong("finishedClients") + ", Clientes Verificados: " + doc.getLong("verifiedClients"));
                });
        cargarInformacionReservasIndividuales();
    }
    private void cargarInformacionReservasIndividuales(){
        db.collection("reservas")
                .document(idReserva)
                .collection("reservaIndividual")
                .get()
                .addOnSuccessListener(reservaDoc -> {

                    listaVerificados.clear();

                    for (DocumentSnapshot doc : reservaDoc){
                        ReservaIndividual ri = doc.toObject(ReservaIndividual.class);
                        ri.setIdReservaIndividual(doc.getId());
                        if(ri.isVerified()){
                            listaVerificados.add(ri);
                        }
                    }
                })
                .addOnFailureListener(e ->{
                    Log.e("FIREBASE", "Error cargando las reservas individuales");
                });
    }

    @SuppressLint("SetTextI18n")
    private void actualizarContadorVista(){
        tvClientesFinalizadoContador.setText(clientesFinalizados + "/" + clientesVerificados);
        Log.e("CLIENTES", "Clientes finalizados: " + clientesFinalizados + ", Clientes Verificados: " + clientesVerificados);
    }
}
