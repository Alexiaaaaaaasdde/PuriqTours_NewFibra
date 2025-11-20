package com.example.puriqtours.guia;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Solicitud;
import com.example.puriqtours.entity.Reserva;
import com.example.puriqtours.entity.Tour;
import com.example.puriqtours.adapter.SolicitudAdapter;
import com.example.puriqtours.helper.FirestoreHelper;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    
    private RecyclerView recyclerView;
    private SolicitudAdapter adapter;
    private List<Solicitud> lista;
    private ProgressBar progressBar;
    private TextView tvNoSolicitudes;
    private FirestoreHelper firestoreHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewSolicitudes);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Inicializar vistas adicionales si existen
        progressBar = view.findViewById(R.id.progressBar);
        tvNoSolicitudes = view.findViewById(R.id.tvNoSolicitudes);

        lista = new ArrayList<>();
        firestoreHelper = new FirestoreHelper();
        
        adapter = new SolicitudAdapter(lista, getParentFragmentManager());
        recyclerView.setAdapter(adapter);
        
        loadSolicitudesFromFirestore();
        
        return view;
    }
    
    /**
     * Cargar solicitudes (reservas sin guía) desde Firestore
     */
    private void loadSolicitudesFromFirestore() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        firestoreHelper.loadReservasWithoutGuide(reservas -> {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            
            if (reservas == null || reservas.isEmpty()) {
                // Mostrar mensaje de no hay solicitudes
                if (tvNoSolicitudes != null) {
                    tvNoSolicitudes.setVisibility(View.VISIBLE);
                }
                recyclerView.setVisibility(View.GONE);
                return;
            }
            
            if (tvNoSolicitudes != null) {
                tvNoSolicitudes.setVisibility(View.GONE);
            }
            recyclerView.setVisibility(View.VISIBLE);
            
            // Convertir Reservas a Solicitudes
            convertReservasToSolicitudes(reservas);
        });
    }
    
    /**
     * Convertir lista de Reservas a Solicitudes para mostrar en el adapter
     */
    private void convertReservasToSolicitudes(List<Reserva> reservas) {
        lista.clear();
        
        // TODO: Esta sección necesita ser actualizada por el desarrollador del módulo Guía
        // La estructura de Solicitud ha cambiado para coincidir con la base de datos
        // Ahora debe cargar Solicitudes desde Firestore en lugar de crearlas desde Reservas
        
        Log.d(TAG, "HomeFragment: Sistema de solicitudes pendiente de actualización");
        
        // TEMPORAL: Dejar vacío por ahora para evitar errores de compilación
        // El desarrollador del módulo Guía debe implementar la carga de solicitudes desde Firestore
    }
    
    /**
     * Parsear string de hora a LocalTime
     */
    private LocalTime parseHora(String hora) {
        if (hora == null || hora.isEmpty()) {
            return LocalTime.of(9, 0); // Hora por defecto
        }
        
        try {
            // Intentar parsear formato HH:mm
            if (hora.contains(":")) {
                return LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"));
            }
            // Si es solo número, asumir que es la hora
            int hourValue = Integer.parseInt(hora);
            return LocalTime.of(hourValue, 0);
        } catch (Exception e) {
            Log.w(TAG, "Error parseando hora: " + hora, e);
            return LocalTime.of(9, 0);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Recargar solicitudes cuando el fragment vuelve a ser visible
        loadSolicitudesFromFirestore();
    }
}
