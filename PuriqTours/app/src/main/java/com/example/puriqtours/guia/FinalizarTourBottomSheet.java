package com.example.puriqtours.guia;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.puriqtours.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FinalizarTourBottomSheet extends BottomSheetDialogFragment {

    private String idReserva;

    public static FinalizarTourBottomSheet newInstance(String idReserva) {
        FinalizarTourBottomSheet fragment = new FinalizarTourBottomSheet();
        Bundle args = new Bundle();
        args.putString("idReserva", idReserva);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtener el idReserva de los argumentos
        if (getArguments() != null) {
            idReserva = getArguments().getString("idReserva");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_finalizar_tour, container, false);

        Button btnFinalizar = view.findViewById(R.id.btnEscanearFinalizar);
        btnFinalizar.setOnClickListener(v -> {
            dismiss(); // Cerrar el bottom sheet

            Intent intent = new Intent(requireContext(), FinalizarTourActivity.class);
            intent.putExtra("idReserva", idReserva);
            startActivity(intent);
        });

        return view;
    }
}
