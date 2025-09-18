package com.example.puriqtours;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DetalleTourActivity extends AppCompatActivity {

    private TextView tvTitulo, tvPrecio, tvFecha, tvViajeros;
    private ImageView imgTour, btnCalendario;

    // 👉 Variables globales
    private int desayuno = 0, canotaje = 0;
    private int adultos = 2, ninos = 0, bebes = 0;
    private int precioTotal = 0;
    private Dialog dialogDisponibilidad;
    private String fechaSeleccionadaGlobal = "Martes, 15 de Marzo de 2025";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_tour);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
        // Referencias
        tvTitulo = findViewById(R.id.tvTitulo);
        tvPrecio = findViewById(R.id.tvPreciokuelap);
        imgTour = findViewById(R.id.imgTour);
        tvFecha = findViewById(R.id.tvFecha);
        btnCalendario = findViewById(R.id.btnCalendario);
        tvViajeros = findViewById(R.id.tvViajeros);
        Button btnDisponibilidad = findViewById(R.id.btnDisponibilidad);

        // Abrir popups
        tvViajeros.setOnClickListener(v -> mostrarDialogoViajeros(tvViajeros, null));
        btnDisponibilidad.setOnClickListener(v -> mostrarDialogoDisponibilidad());

        // Recuperar datos
        String titulo = getIntent().getStringExtra("titulo");
        String precio = getIntent().getStringExtra("precio");

        if (titulo != null) tvTitulo.setText(titulo);
        if (precio != null) tvPrecio.setText(precio);

        imgTour.setImageResource(R.drawable.kuelap);

        // Calendario
        btnCalendario.setOnClickListener(v -> mostrarDatePicker());
        tvFecha.setOnClickListener(v -> mostrarDatePicker());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_tours);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0,0);
                return true;

            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0,0);
                return true;

            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                overridePendingTransition(0,0);
                return true;
            }

            return false;
        });



    }

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

                    // ✅ actualizar también dentro del popup si está abierto
                    if (dialogDisponibilidad != null && dialogDisponibilidad.isShowing()) {
                        TextView tvFechaSel = dialogDisponibilidad.findViewById(R.id.tvFechaSeleccionada);
                        TextView tvDetalles = dialogDisponibilidad.findViewById(R.id.tvDetalles);
                        if (tvFechaSel != null) tvFechaSel.setText(fechaSeleccionadaGlobal);
                        if (tvDetalles != null) tvDetalles.setText("2 opciones disponibles para el " + fechaSeleccionadaGlobal);
                    }
                },
                año, mes, dia
        );
        datePickerDialog.show();
    }

    private void actualizarResumen(TextView tvResumen, TextView tvPrecioDestino) {
        String resumen = adultos + " adultos";
        if (ninos > 0) resumen += ", " + ninos + " niños";
        if (bebes > 0) resumen += ", " + bebes + " bebés";

        precioTotal = (adultos * 165) + (ninos * 20);

        if (tvResumen != null) tvResumen.setText(resumen);
        if (tvPrecioDestino != null) tvPrecioDestino.setText("Total: S/. " + precioTotal);
    }

    // 👉 ahora acepta referencias opcionales del popup
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

        // Adultos
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
            actualizarResumen(tvViajeros, null); // siempre actualizar principal
            dialog.dismiss();
        });

        dialog.show();
    }

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

        // 👉 ahora al tocar viajeros en el popup, abre el mismo diálogo pero actualiza también precio
        tvViajerosSel.setOnClickListener(v -> mostrarDialogoViajeros(tvViajerosSel, tvPrecioDisp));

        btnVerDetalles.setOnClickListener(v -> mostrarDialogoExtras(tvPrecioDisp, tvViajerosSel));

        // Sincronizar
        tvFechaSel.setText(fechaSeleccionadaGlobal);
        actualizarResumen(tvViajerosSel, tvPrecioDisp);

        // Calendario dentro del popup
        btnCalendarioDisp.setOnClickListener(v -> mostrarDatePicker());

        // Botón Reservar
        Button btnReservar = dialogDisponibilidad.findViewById(R.id.btnReservar);
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
            String fecha = tvFechaSel.getText().toString();
            String viajeros = tvViajerosSel.getText().toString();
            String precio = tvPrecioDisp.getText().toString();

            dialogDisponibilidad.dismiss();

            Intent intent = new Intent(DetalleTourActivity.this, PagoActivity.class);
            intent.putExtra("fecha", fecha);
            intent.putExtra("viajeros", viajeros);
            intent.putExtra("precio", precio);
            intent.putExtra("hora", horaSeleccionada[0]);
            startActivity(intent);
        });

        dialogDisponibilidad.show();
    }

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
}
