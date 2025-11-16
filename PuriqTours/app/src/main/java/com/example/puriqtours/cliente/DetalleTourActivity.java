package com.example.puriqtours.cliente;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.example.puriqtours.login.LoginLegacyActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DetalleTourActivity extends AppCompatActivity {

    private TextView tvTitulo, tvPrecio, tvFecha, tvViajeros;
    private ImageView imgTour, btnCalendario;

    private int desayuno = 0, canotaje = 0;
    private int adultos = 2, ninos = 0, bebes = 0;

    private Dialog dialogDisponibilidad;
    private String fechaSeleccionadaGlobal = "Martes, 15 de Marzo de 2025";
    private String tourId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_tour);

        // 🔹 Obtener ID real del tour
        tourId = getIntent().getStringExtra("tourId");

        // 🔹 Back
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // 🔹 Referencias
        tvTitulo = findViewById(R.id.tvTitulo);
        tvPrecio = findViewById(R.id.tvPreciokuelap);
        imgTour = findViewById(R.id.imgTour);
        tvFecha = findViewById(R.id.tvFecha);
        btnCalendario = findViewById(R.id.btnCalendario);
        tvViajeros = findViewById(R.id.tvViajeros);
        Button btnDisponibilidad = findViewById(R.id.btnDisponibilidad);

        // 🔹 Abrir popups
        tvViajeros.setOnClickListener(v -> mostrarDialogoViajeros(tvViajeros, null));
        btnDisponibilidad.setOnClickListener(v -> mostrarDialogoDisponibilidad());

        // 🔹 Recuperar datos recibidos
        String titulo = getIntent().getStringExtra("titulo");
        String precio = getIntent().getStringExtra("precio");

        if (titulo != null) tvTitulo.setText(titulo);
        if (precio != null) tvPrecio.setText(precio);

        imgTour.setImageResource(R.drawable.kuelap);

        // 🔹 Calendario
        btnCalendario.setOnClickListener(v -> mostrarDatePicker());
        tvFecha.setOnClickListener(v -> mostrarDatePicker());

        // 🔹 Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_tours);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, LoginLegacyActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    // 📌 Calendario
    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int año = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar seleccionada = Calendar.getInstance();
                    seleccionada.set(year, month, dayOfMonth);

                    SimpleDateFormat sdf =
                            new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                    fechaSeleccionadaGlobal = sdf.format(seleccionada.getTime());

                    tvFecha.setText(fechaSeleccionadaGlobal);

                    // Actualizar dentro del popup si está abierto
                    if (dialogDisponibilidad != null && dialogDisponibilidad.isShowing()) {
                        TextView tvFechaSel = dialogDisponibilidad.findViewById(R.id.tvFechaSeleccionada);
                        TextView tvDetalles = dialogDisponibilidad.findViewById(R.id.tvDetalles);
                        if (tvFechaSel != null) tvFechaSel.setText(fechaSeleccionadaGlobal);
                        if (tvDetalles != null)
                            tvDetalles.setText("2 opciones disponibles para el " + fechaSeleccionadaGlobal);
                    }
                },
                año, mes, dia
        );
        datePickerDialog.show();
    }

    // 📌 Mostrar popup de viajeros
    private void mostrarDialogoViajeros(TextView tvResumenDestino, TextView tvPrecioDestino) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_viajeros);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvAdultos = dialog.findViewById(R.id.tvCantidadAdultos);
        TextView tvNinos = dialog.findViewById(R.id.tvCantidadNinos);
        TextView tvBebes = dialog.findViewById(R.id.tvCantidadBebes);
        Button btnAceptar = dialog.findViewById(R.id.btnAceptarViajeros);

        tvAdultos.setText(String.valueOf(adultos));
        tvNinos.setText(String.valueOf(ninos));
        tvBebes.setText(String.valueOf(bebes));

        // Eventos
        dialog.findViewById(R.id.btnMasAdultos).setOnClickListener(v -> {
            adultos++;
            tvAdultos.setText(String.valueOf(adultos));
            actualizarResumen(tvResumenDestino, tvPrecioDestino);
        });
        dialog.findViewById(R.id.btnMenosAdultos).setOnClickListener(v -> {
            if (adultos > 0) adultos--;
            tvAdultos.setText(String.valueOf(adultos));
            actualizarResumen(tvResumenDestino, tvPrecioDestino);
        });

        // Niños
        dialog.findViewById(R.id.btnMasNinos).setOnClickListener(v -> {
            ninos++;
            tvNinos.setText(String.valueOf(ninos));
            actualizarResumen(tvResumenDestino, tvPrecioDestino);
        });
        dialog.findViewById(R.id.btnMenosNinos).setOnClickListener(v -> {
            if (ninos > 0) ninos--;
            tvNinos.setText(String.valueOf(ninos));
            actualizarResumen(tvResumenDestino, tvPrecioDestino);
        });

        // Bebés
        dialog.findViewById(R.id.btnMasBebes).setOnClickListener(v -> {
            bebes++;
            tvBebes.setText(String.valueOf(bebes));
            actualizarResumen(tvResumenDestino, tvPrecioDestino);
        });
        dialog.findViewById(R.id.btnMenosBebes).setOnClickListener(v -> {
            if (bebes > 0) bebes--;
            tvBebes.setText(String.valueOf(bebes));
            actualizarResumen(tvResumenDestino, tvPrecioDestino);
        });

        btnAceptar.setOnClickListener(v -> {
            actualizarResumen(tvViajeros, null);
            dialog.dismiss();
        });

        dialog.show();
    }

    // 📌 Actualizar resumen de viajeros
    private void actualizarResumen(TextView tvResumen, TextView tvPrecioDestino) {
        String resumen = adultos + " adultos";
        if (ninos > 0) resumen += ", " + ninos + " niños";
        if (bebes > 0) resumen += ", " + bebes + " bebés";

        if (tvResumen != null) tvResumen.setText(resumen);
        if (tvPrecioDestino != null) {
            int precioBase = (adultos * 165) + (ninos * 20);
            tvPrecioDestino.setText("Total: S/. " + precioBase);
        }
    }

    // 📌 Popup de disponibilidad
    private void mostrarDialogoDisponibilidad() {
        dialogDisponibilidad = new Dialog(this);
        dialogDisponibilidad.setContentView(R.layout.dialog_disponibilidad);
        dialogDisponibilidad.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button btnVerDetalles = dialogDisponibilidad.findViewById(R.id.btnDetalles);
        TextView tvFechaSel = dialogDisponibilidad.findViewById(R.id.tvFechaSeleccionada);
        TextView tvViajerosSel = dialogDisponibilidad.findViewById(R.id.tvViajerosSeleccionados);
        TextView tvPrecioDisp = dialogDisponibilidad.findViewById(R.id.tvPrecio);
        TextView tvDetalles = dialogDisponibilidad.findViewById(R.id.tvDetalles);
        ImageView btnCalendarioDisp = dialogDisponibilidad.findViewById(R.id.btnCalendarioDisponibilidad);

        // Viajeros en popup
        tvViajerosSel.setOnClickListener(v -> mostrarDialogoViajeros(tvViajerosSel, tvPrecioDisp));

        btnVerDetalles.setOnClickListener(v -> mostrarDialogoExtras(tvPrecioDisp, tvViajerosSel));

        // Sincronizar
        tvFechaSel.setText(fechaSeleccionadaGlobal);
        actualizarResumen(tvViajerosSel, tvPrecioDisp);

        // Calendario en popup
        btnCalendarioDisp.setOnClickListener(v -> mostrarDatePicker());

        // 🔥 Botón reservar ahora
        Button btnReservar = dialogDisponibilidad.findViewById(R.id.btnReserva);
        btnReservar.setEnabled(false);
        btnReservar.setAlpha(0.5f);

        Button btnHora1 = dialogDisponibilidad.findViewById(R.id.btnHora1);
        Button btnHora2 = dialogDisponibilidad.findViewById(R.id.btnHora2);
        final String[] horaSeleccionada = {""};

        View.OnClickListener horarioClickListener = v -> {
            btnReservar.setEnabled(true);
            btnReservar.setAlpha(1f);
            btnHora1.setSelected(false);
            btnHora2.setSelected(false);
            v.setSelected(true);
            horaSeleccionada[0] = ((Button) v).getText().toString();
        };

        btnHora1.setOnClickListener(horarioClickListener);
        btnHora2.setOnClickListener(horarioClickListener);

        btnReservar.setOnClickListener(v -> {
            Intent intent = new Intent(DetalleTourActivity.this, PagoActivity.class);
            intent.putExtra("fecha", tvFechaSel.getText().toString());
            intent.putExtra("viajeros", tvViajerosSel.getText().toString());
            intent.putExtra("precio", tvPrecioDisp.getText().toString());
            intent.putExtra("hora", horaSeleccionada[0]);
            startActivity(intent);
        });

        dialogDisponibilidad.show();
    }

    // 📌 Popup de extras
    private void mostrarDialogoExtras(TextView tvPrecioDisp, TextView tvViajerosSel) {
        Dialog dialogExtras = new Dialog(this);
        dialogExtras.setContentView(R.layout.dialog_detalles);
        dialogExtras.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvCantDesayuno = dialogExtras.findViewById(R.id.tvCantidadDesayuno);
        TextView tvCantCanotaje = dialogExtras.findViewById(R.id.tvCantidadCanotaje);

        tvCantDesayuno.setText(String.valueOf(desayuno));
        tvCantCanotaje.setText(String.valueOf(canotaje));

        dialogExtras.findViewById(R.id.btnMasDesayuno).setOnClickListener(v -> {
            desayuno++;
            tvCantDesayuno.setText(String.valueOf(desayuno));
        });
        dialogExtras.findViewById(R.id.btnMenosDesayuno).setOnClickListener(v -> {
            if (desayuno > 0) desayuno--;
            tvCantDesayuno.setText(String.valueOf(desayuno));
        });

        dialogExtras.findViewById(R.id.btnMasCanotaje).setOnClickListener(v -> {
            canotaje++;
            tvCantCanotaje.setText(String.valueOf(canotaje));
        });
        dialogExtras.findViewById(R.id.btnMenosCanotaje).setOnClickListener(v -> {
            if (canotaje > 0) canotaje--;
            tvCantCanotaje.setText(String.valueOf(canotaje));
        });

        dialogExtras.findViewById(R.id.btnAgregarExtras).setOnClickListener(v -> {
            int precioBase = (adultos * 165) + (ninos * 20);
            int precioExtras = (desayuno * 10) + (canotaje * 30);
            int precioFinal = precioBase + precioExtras;

            tvPrecioDisp.setText("Total: S/. " + precioFinal);
            actualizarResumen(tvViajerosSel, null);

            dialogExtras.dismiss();
        });

        dialogExtras.show();
    }

    // 📌 Registrar reserva en Firebase
    private void registrarReserva() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        String idCliente = mAuth.getCurrentUser().getUid();

        Map<String, Object> reserva = new HashMap<>();
        reserva.put("idCliente", idCliente);
        reserva.put("idTour", tourId);
        reserva.put("fechaReserva", new Date());
        reserva.put("estado", "reservado");
        reserva.put("qrInicio", idCliente + "_" + tourId + "_inicio");
        reserva.put("qrFin", idCliente + "_" + tourId + "_fin");

        db.collection("reservas")
                .add(reserva)
                .addOnSuccessListener(docRef -> mostrarDialogoReserva())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // 📌 Popup de confirmación de reserva
    private void mostrarDialogoReserva() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_reserva_registrada);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnCerrar = dialog.findViewById(R.id.btnCloseReserva);

        btnCerrar.setOnClickListener(v -> {
            dialog.dismiss();
            Intent i = new Intent(DetalleTourActivity.this, ReservadoActivity.class);
            i.putExtra("tourId", tourId);
            startActivity(i);
            finish();
        });

        dialog.show();
    }
}
