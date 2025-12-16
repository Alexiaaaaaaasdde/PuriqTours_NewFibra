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
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.example.puriqtours.BaseActivity;
import com.example.puriqtours.R;
import com.example.puriqtours.entity.CheckpointReserva;
import com.example.puriqtours.entity.ReservaCliente;
import com.example.puriqtours.entity.ReservaIndividual;
import com.example.puriqtours.helper.TokenUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PagoActivity extends BaseActivity {

    // 🔹 Launcher para pedir permiso de notificaciones
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private String img, reservaId;
    private ArrayList<String> extrasDetalle;
    private ReservaCliente reservaCliente = new ReservaCliente();
    private ReservaIndividual reservaIndividual = new ReservaIndividual();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);
        setupSharedToolbar();

        TextView tvExtrasDetalle = findViewById(R.id.tvExtrasDetalle);


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
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        String idCliente = mAuth.getCurrentUser().getUid();

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

        reservaCliente.setDate(getIntent().getStringExtra("fecha"));
        reservaCliente.setHour(getIntent().getStringExtra("hora"));
        reservaCliente.setIdTour(getIntent().getStringExtra("tourId"));
        reservaCliente.setTitle(getIntent().getStringExtra("titulo"));
        reservaCliente.setIdGuia(getIntent().getStringExtra("idGuia"));

        reservaIndividual.setTravelers(getIntent().getStringExtra("viajeros"));
        reservaIndividual.setPrice((double) getIntent().getFloatExtra("precio", 0));
        reservaIndividual.setIdCliente(idCliente);

        // 🔹 Recuperar datos enviados desde DetalleTourActivity
        String viajeros = getIntent().getStringExtra("viajeros");
        String titulo = getIntent().getStringExtra("titulo");
        String fecha = getIntent().getStringExtra("fecha");
        String hora = getIntent().getStringExtra("hora");
        String ubicacion = getIntent().getStringExtra("ubicacion");
        String opiniones = getIntent().getStringExtra("opiniones");
        int rating = getIntent().getIntExtra("rating", 5);
        img = getIntent().getStringExtra("img");
        extrasDetalle = getIntent().getStringArrayListExtra("extrasDetalle");


        // ------- SETEAR DATOS EN LA UI -------
        if (fecha != null) tvFechaPago.setText("Fecha: " + fecha);
        if (viajeros != null) tvViajerosPago.setText("Viajeros: " + viajeros);
        if (reservaIndividual.getPrice() != null) tvPrecioPago.setText("S/." + reservaIndividual.getPrice());
        if (hora != null) tvHoraPago.setText("Hora: " + hora);

        if (extrasDetalle != null && !extrasDetalle.isEmpty() && !extrasDetalle.contains("Sin extras")) {
            // Unir la lista con saltos de línea
            String extrasTexto = String.join("\n", extrasDetalle);
            tvExtrasDetalle.setText(extrasTexto);
        } else {
            tvExtrasDetalle.setText("Sin extras");
        }


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
        RadioGroup rgPago = findViewById(R.id.radioGroupPago);
        RadioButton rbGooglePay = findViewById(R.id.rbGooglePay);
        RadioButton rbTarjeta = findViewById(R.id.rbTarjeta);

// 👉 Evento para selección de forma de pago
        rgPago.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbTarjeta) {
                mostrarDialogoTarjeta();
            } else if (checkedId == R.id.rbGooglePay) {
                mostrarDialogoYapePlin();
            }
        });

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
            reservaIndividual.setMetodoPago("Tarjeta");
            reservaIndividual.setCodigoOperacion("N/A");

            guardarReservaEnFirestore();

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

    public interface OnReservaCheckListener {
        void onResult(boolean existe);
        void onError(Exception e);
    }

    private void verificarExistenciaReserva(OnReservaCheckListener listener){
        db.collection("reservas")
                .whereEqualTo("date", reservaCliente.getDate())
                .whereEqualTo("hour", reservaCliente.getHour())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean existe = !querySnapshot.isEmpty();
                    listener.onResult(existe);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al buscar reserva", e);
                    listener.onError(e);
                });
    }

    private void guardarReservaEnFirestore() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        verificarExistenciaReserva(new OnReservaCheckListener() {
            @Override
            public void onResult(boolean existe) {
                //Si ya existe una Reserva para la misma fecha y hora
                if(existe){
                    db.collection("reservas")
                            .whereEqualTo("date", reservaCliente.getDate())
                            .whereEqualTo("hour", reservaCliente.getHour())
                            .limit(1).get()
                            .addOnSuccessListener(doc -> {
                                for(DocumentSnapshot documentSnapshot: doc){
                                    reservaId = documentSnapshot.getId();
                                }
                                db.collection("reservas")
                                        .document(reservaId)
                                        .update("totalClients", FieldValue.increment(1));
                                db.collection("reservas")
                                        .document(reservaId)
                                        .update("idClientes", FieldValue.arrayUnion(reservaIndividual.getIdCliente()));


                                guardarReservaIndividual();
                                copiarCheckpoints();
                            });
                //Si no existe una Reserva para la fecha y hora
                } else {
                    // 🔹 CREAR ID DE RESERVA MANUALMENTE
                    reservaId = db.collection("reservas").document().getId();
                    List<String> idClientes = new ArrayList<>();
                    idClientes.add(reservaIndividual.getIdCliente());

                    Map<String, Object> reserva = new HashMap<>();
                    reserva.put("idTour", reservaCliente.getIdTour());
                    reserva.put("idGuia", reservaCliente.getIdGuia());
                    reserva.put("title", reservaCliente.getTitle());
                    reserva.put("date", reservaCliente.getDate());
                    reserva.put("hour", reservaCliente.getHour());
                    reserva.put("idClientes", idClientes);
                    reserva.put("totalClients", 1);
                    reserva.put("verifiedClients", 0);
                    reserva.put("finishedClients", 0);
                    reserva.put("status", "Reservado");
                    reserva.put("timestamp", System.currentTimeMillis());
                    reserva.put("imageUrl", img);

                    // 🔥 GUARDAR RESERVA CON ID ESPECÍFICO
                    db.collection("reservas")
                            .document(reservaId)  // ⭐ USAR .document() en vez de .add()
                            .set(reserva)
                            .addOnSuccessListener(r -> {
                                System.out.println("✔ RESERVA GUARDADA: " + reservaId);

                                // 🔥 COPIAR CHECKPOINTS DEL TOUR
                                guardarReservaIndividual();
                                copiarCheckpoints();
                            })
                            .addOnFailureListener(e -> {
                                System.out.println("❌ ERROR FIRESTORE " + e.getMessage());
                            });
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("Firestore", "Error al validar Reserva");
            }
        });


    }

    private void guardarReservaIndividual(){
        Map<String, Object> reservaExtra = new HashMap<>();
        reservaExtra.put("idCliente", reservaIndividual.getIdCliente());
        reservaExtra.put("price", reservaIndividual.getPrice());
        reservaExtra.put("tokenStart", TokenUtil.generarToken());
        reservaExtra.put("tokenEnd", TokenUtil.generarToken());
        reservaExtra.put("travelers", reservaIndividual.getTravelers());
        reservaExtra.put("codigoOperacion", reservaIndividual.getCodigoOperacion());
        reservaExtra.put("metodoPago", reservaIndividual.getMetodoPago());
        reservaExtra.put("verified", false);
        reservaExtra.put("finished", false);
        reservaExtra.put("addedServices", extrasDetalle);

        db.collection("reservas")
                .document(reservaId)
                .collection("reservaIndividual")
                .add(reservaExtra)
                .addOnSuccessListener(doc -> {
                    Log.e("Firestore", "Reserva Individual Guardada");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al guardar reserva individual: " + e.getMessage());
                });

    }

    private void copiarCheckpoints() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tours")
                .document(reservaCliente.getIdTour())
                .collection("locations")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        Log.w("CHECKPOINTS", "El tour no tiene locations");
                        return;
                    }
                    for(DocumentSnapshot doc : querySnapshot){
                        Map<String, Object> checkpoint = new HashMap<>();
                        checkpoint.put("title", doc.getString("title"));
                        checkpoint.put("order", doc.getLong("order"));
                        checkpoint.put("lat", doc.getDouble("lat"));
                        checkpoint.put("lng", doc.getDouble("lng"));
                        checkpoint.put("status", "Pendiente");
                        db.collection("reservas")
                                .document(reservaId)
                                .collection("checkpoints")
                                .add(checkpoint)
                                .addOnSuccessListener(d -> {
                                    System.out.println("✔ Checkpoint copiado: " + checkpoint.get("title"));
                                })
                                .addOnFailureListener(e -> {
                                    System.out.println("❌ Error al copiar checkpoint: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("❌ Error al obtener tour: " + e.getMessage());
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

    private void mostrarDialogoYapePlin() {

        Dialog dialogYape = new Dialog(PagoActivity.this);
        dialogYape.setContentView(R.layout.dialog_qr_yape);
        dialogYape.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvMonto = dialogYape.findViewById(R.id.tvMontoYape);
        TextView tvCodigo = dialogYape.findViewById(R.id.tvCodigoOperacion);
        Button btnYaPague = dialogYape.findViewById(R.id.btnYaPague);

        // 🔹 Recuperar el monto
        tvMonto.setText("Monto: S/." + reservaIndividual.getPrice());

        // 🔹 Generar código automáticamente
        String codigoOperacion = generarCodigoOperacion();
        tvCodigo.setText("Cod. operación: " + codigoOperacion);
        reservaIndividual.setCodigoOperacion(codigoOperacion);
        reservaIndividual.setMetodoPago("Yape/Plin/GooglePay");

        btnYaPague.setOnClickListener(v -> {

            dialogYape.dismiss();

            // 🔥 Guardar la reserva indicando método Yape/Plin
            guardarReservaEnFirestore();

            mostrarDialogoReserva();
        });

        dialogYape.show();
    }


    private String generarCodigoOperacion() {
        int num = (int) (Math.random() * 900000) + 100000; // 6 dígitos
        return "PAY-" + num;
    }

}
