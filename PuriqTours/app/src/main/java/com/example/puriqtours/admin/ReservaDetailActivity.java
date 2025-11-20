package com.example.puriqtours.admin;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Reserva;

public class ReservaDetailActivity extends AppCompatActivity {

    private Reserva reserva;
    private TextView tvTourName, tvStatus, tvDate, tvHour, tvPrice;
    private TextView tvTravelers, tvMetodoPago, tvCodigoOperacion;
    private TextView tvQrStart, tvQrEnd, tvIdCliente, tvIdGuia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserva_detail);

        // Configurar toolbar
        setupToolbar();

        // Inicializar vistas
        initViews();

        // Obtener reserva del intent
        reserva = (Reserva) getIntent().getSerializableExtra("reserva");

        if (reserva != null) {
            displayReservaData();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Detalle de Reserva");
        }
    }

    private void initViews() {
        tvTourName = findViewById(R.id.tvTourName);
        tvStatus = findViewById(R.id.tvStatus);
        tvDate = findViewById(R.id.tvDate);
        tvHour = findViewById(R.id.tvHour);
        tvPrice = findViewById(R.id.tvPrice);
        tvTravelers = findViewById(R.id.tvTravelers);
        tvMetodoPago = findViewById(R.id.tvMetodoPago);
        tvCodigoOperacion = findViewById(R.id.tvCodigoOperacion);
        tvQrStart = findViewById(R.id.tvQrStart);
        tvQrEnd = findViewById(R.id.tvQrEnd);
        tvIdCliente = findViewById(R.id.tvIdCliente);
        tvIdGuia = findViewById(R.id.tvIdGuia);
    }

    private void displayReservaData() {
        tvTourName.setText(reserva.getTitle() != null ? reserva.getTitle() : "Sin título");
        tvStatus.setText(reserva.getStatus() != null ? reserva.getStatus() : "Pendiente");
        tvDate.setText(reserva.getDate() != null ? reserva.getDate() : "Sin fecha");
        tvHour.setText(reserva.getHour() != null ? reserva.getHour() : "Sin hora");
        tvPrice.setText(reserva.getPrice() != null ? "S/ " + reserva.getPrice() : "S/ 0");
        tvTravelers.setText(reserva.getTravelers() != null ? reserva.getTravelers() : "N/A");
        tvMetodoPago.setText(reserva.getMetodoPago() != null ? reserva.getMetodoPago() : "N/A");
        tvCodigoOperacion.setText(reserva.getCodigoOperacion() != null ? reserva.getCodigoOperacion() : "N/A");
        tvQrStart.setText(reserva.getQrStart() != null ? reserva.getQrStart() : "N/A");
        tvQrEnd.setText(reserva.getQrEnd() != null ? reserva.getQrEnd() : "N/A");
        tvIdCliente.setText(reserva.getIdCliente() != null ? reserva.getIdCliente() : "N/A");
        tvIdGuia.setText(reserva.getIdGuia() != null ? reserva.getIdGuia() : "Sin asignar");

        // Cambiar color del status
        String status = reserva.getStatus();
        if ("Finalizado".equalsIgnoreCase(status)) {
            tvStatus.setTextColor(getResources().getColor(R.color.green));
        } else if ("En proceso".equalsIgnoreCase(status)) {
            tvStatus.setTextColor(getResources().getColor(R.color.teal_700));
        } else {
            tvStatus.setTextColor(getResources().getColor(R.color.orange));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
