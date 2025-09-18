package com.example.puriqtours;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DetalleTourActivity extends AppCompatActivity {

    private TextView tvTitulo, tvPrecio, tvFecha, tvViajeros;
    private ImageView imgTour, btnCalendario;

    // 👉 Variables globales
    private int adultos = 2;
    private int ninos = 0;
    private int bebes = 0;
    private int precioTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_tour);

        // Referencias de vistas
        tvTitulo = findViewById(R.id.tvTitulo);
        tvPrecio = findViewById(R.id.tvPreciokuelap);
        imgTour = findViewById(R.id.imgTour);
        tvFecha = findViewById(R.id.tvFecha);
        btnCalendario = findViewById(R.id.btnCalendario);
        tvViajeros = findViewById(R.id.tvViajeros);
        Button btnDisponibilidad = findViewById(R.id.btnDisponibilidad);

        // Abrir popup de viajeros
        tvViajeros.setOnClickListener(v -> mostrarDialogoViajeros());

        // Abrir popup de disponibilidad
        btnDisponibilidad.setOnClickListener(v -> mostrarDialogoDisponibilidad());

        // 🔹 Recuperar datos del intent
        String titulo = getIntent().getStringExtra("titulo");
        String precio = getIntent().getStringExtra("precio");

        // 🔹 Setear datos
        tvTitulo.setText(titulo);
        tvPrecio.setText(precio);

        // 🔹 Imagen fija de Kuélap
        imgTour.setImageResource(R.drawable.kuelap);

        // 📅 Listener para abrir el calendario
        btnCalendario.setOnClickListener(v -> mostrarDatePicker());
        tvFecha.setOnClickListener(v -> mostrarDatePicker()); // opcional
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
                    String fechaSeleccionada = sdf.format(seleccionada.getTime());

                    tvFecha.setText(fechaSeleccionada);
                },
                año, mes, dia
        );

        datePickerDialog.show();
    }

    // 👉 Método auxiliar para actualizar resumen y precio
    private void actualizarResumen(TextView tvResumen, TextView tvPrecioDestino) {
        String resumen = adultos + " adultos";
        if (ninos > 0) resumen += ", " + ninos + " niños";
        if (bebes > 0) resumen += ", " + bebes + " bebés";

        precioTotal = (adultos * 60) + (ninos * 20); // bebés gratis

        if (tvResumen != null) tvResumen.setText(resumen);
        if (tvPrecioDestino != null) tvPrecioDestino.setText("Total: S/. " + precioTotal);
    }

    private void mostrarDialogoViajeros() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_viajeros);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Referencias
        TextView tvAdultos = dialog.findViewById(R.id.tvCantidadAdultos);
        TextView tvNinos = dialog.findViewById(R.id.tvCantidadNinos);
        TextView tvBebes = dialog.findViewById(R.id.tvCantidadBebes);
        Button btnAceptar = dialog.findViewById(R.id.btnAceptarViajeros);

        // Setear valores actuales
        tvAdultos.setText(String.valueOf(adultos));
        tvNinos.setText(String.valueOf(ninos));
        tvBebes.setText(String.valueOf(bebes));

        // Botones Adultos
        dialog.findViewById(R.id.btnMasAdultos).setOnClickListener(v -> {
            adultos++;
            tvAdultos.setText(String.valueOf(adultos));
        });
        dialog.findViewById(R.id.btnMenosAdultos).setOnClickListener(v -> {
            if (adultos > 0) adultos--;
            tvAdultos.setText(String.valueOf(adultos));
        });

        // Botones Niños
        dialog.findViewById(R.id.btnMasNinos).setOnClickListener(v -> {
            ninos++;
            tvNinos.setText(String.valueOf(ninos));
        });
        dialog.findViewById(R.id.btnMenosNinos).setOnClickListener(v -> {
            if (ninos > 0) ninos--;
            tvNinos.setText(String.valueOf(ninos));
        });

        // Botones Bebés
        dialog.findViewById(R.id.btnMasBebes).setOnClickListener(v -> {
            bebes++;
            tvBebes.setText(String.valueOf(bebes));
        });
        dialog.findViewById(R.id.btnMenosBebes).setOnClickListener(v -> {
            if (bebes > 0) bebes--;
            tvBebes.setText(String.valueOf(bebes));
        });

        // Botón Aceptar
        btnAceptar.setOnClickListener(v -> {
            actualizarResumen(tvViajeros, null); // actualiza la vista principal
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogoDisponibilidad() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_disponibilidad);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Referencias dentro del popup
        TextView tvFechaSel = dialog.findViewById(R.id.tvFechaSeleccionada);
        TextView tvViajerosSel = dialog.findViewById(R.id.tvViajerosSeleccionados);
        TextView tvPrecioDisp = dialog.findViewById(R.id.tvPrecio);
        ImageView btnCalendarioDisp = dialog.findViewById(R.id.btnCalendarioDisponibilidad);

        // Sincronizar valores actuales
        tvFechaSel.setText(tvFecha.getText().toString());
        actualizarResumen(tvViajerosSel, tvPrecioDisp);

        // Listener calendario en popup
        btnCalendarioDisp.setOnClickListener(v -> {
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
                        String fechaSeleccionada = sdf.format(seleccionada.getTime());

                        tvFechaSel.setText(fechaSeleccionada);
                        tvFecha.setText(fechaSeleccionada);
                    },
                    año, mes, dia
            );
            datePickerDialog.show();
        });

        // Botón reservar
        Button btnReservar = dialog.findViewById(R.id.btnReservar);
        btnReservar.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Reserva realizada 🎉", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}
