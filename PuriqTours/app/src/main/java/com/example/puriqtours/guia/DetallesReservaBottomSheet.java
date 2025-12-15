package com.example.puriqtours.guia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.puriqtours.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.puriqtours.entity.TourGuia;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class DetallesReservaBottomSheet extends BottomSheetDialogFragment {

    private TourGuia tour;

    public DetallesReservaBottomSheet(TourGuia tour) {
        this.tour = tour;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottomsheet_detalles_reserva, container, false);

        MaterialTextView tvDescripcion = view.findViewById(R.id.tvDescripcion);
        MaterialTextView tvDuracion = view.findViewById(R.id.tvDuracion);
        MaterialTextView tvTotalClients = view.findViewById(R.id.tvTotalClients);
        MaterialTextView tvHorarioFecha = view.findViewById(R.id.tvHorarioFecha);

        // 🔹 Descripción
        tvDescripcion.setText(tour.getDesc());

        // 🔹 Calcular duración total
        String inicio = tour.getStartTime();   // formato HH:mm
        String fin = tour.getEndTime();

        int hInicio = Integer.parseInt(inicio.split(":")[0]);
        int hFin = Integer.parseInt(fin.split(":")[0]);
        int duracion = hFin - hInicio;

        tvDuracion.setText("Duración total: " + duracion + "h");
        tvTotalClients.setText("Clientes totales: " + tour.getTotalClients());
        // 🔹 Fecha + horas
        tvHorarioFecha.setText(
                "Inicio: " + inicio +
                        " - Fin: " + fin +
                        " | Fecha: " + tour.getDate()
        );

        return view;
    }
}
