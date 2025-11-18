package com.example.puriqtours.guia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.adapter.TourGuiaAdapter;
import com.example.puriqtours.entity.TourGuia;
import com.example.puriqtours.guia.IniciarTourActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ToursFragment extends Fragment {

    private RecyclerView recyclerView;
    private TourGuiaAdapter adapter;
    private List<TourGuia> tourList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public ToursFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tours, container, false);

        // Inicializar
        recyclerView = view.findViewById(R.id.recyclerViewSolicitudes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tourList = new ArrayList<>();

        cargarToursDelGuia();

        adapter = new TourGuiaAdapter(getContext(), tourList, t -> {

            String estado = t.getStatus();

            if (estado == null) {
                Toast.makeText(getContext(), "Estado no válido", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("TOUR", "Tour #"+ t.getIdTour() + " estado: " +  t.getStatus());
            switch (estado) {

                case "Reservado":
                    // Abrir verificación del token
                    Intent i1 = new Intent(getContext(), IniciarTourActivity.class);
                    i1.putExtra("idReserva", t.getIdReserva());
                    i1.putExtra("idTour", t.getIdTour());
                    i1.putExtra("tokenInicio", t.getTokenInicio());
                    startActivity(i1);
                    break;

                case "En proceso":
                    // Ir directamente al mapa
                    Intent i2 = new Intent(getContext(), MapaTourActivity.class);
                    i2.putExtra("idReserva", t.getIdReserva());
                    i2.putExtra("idTour", t.getIdTour());
                    startActivity(i2);
                    break;

                case "Finalizado":
                    Toast.makeText(getContext(), "El tour ya fue finalizado", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
        recyclerView.setAdapter(adapter);


        return view;
    }


    // ===========================================================================================
    // 🔹 CARGA PRINCIPAL — Filtra todas las reservas del guía actual y hace join con tours
    // ===========================================================================================
    private void cargarToursDelGuia() {

        if (mAuth.getCurrentUser() == null) return;

        String uidGuia = mAuth.getCurrentUser().getUid();
        Log.d("FIRESTORE", "UID Guia: " + uidGuia);

        db.collection("reservas")
                .whereEqualTo("idGuia", uidGuia)
                .addSnapshotListener((value, error) -> {

                    if (!isAdded() || getContext() == null) return; // EVITA CRASH

                    if (error != null) {
                        Toast.makeText(requireContext(), "Error al cargar reservas", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        tourList.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "No tiene ningún tour asignado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    tourList.clear();

                    for (DocumentSnapshot reservaDoc : value.getDocuments()) {

                        String idReserva = reservaDoc.getId();
                        String idTour = reservaDoc.getString("idTour");

                        String idCliente = reservaDoc.getString("idCliente");
                        String status = reservaDoc.getString("estado");
                        String tokenInicio = reservaDoc.getString("qrInicio");
                        String tokenFin = reservaDoc.getString("qrFin");

                        List<String> addedServices = (List<String>) reservaDoc.get("added_services");

                        db.collection("tours")
                                .document(idTour)
                                .get()
                                .addOnSuccessListener(tourDoc -> {
                                    if (!isAdded() || getContext() == null) return; // EVITA CRASH AQUI TAMBIÉN
                                    if (!tourDoc.exists()) return;
                                    TourGuia tg = new TourGuia(
                                            idReserva,
                                            idTour,
                                            idCliente,
                                            uidGuia,
                                            status,
                                            tokenInicio,
                                            tokenFin,
                                            tourDoc.getString("title"),
                                            tourDoc.getString("desc"),
                                            tourDoc.getString("location"),
                                            tourDoc.getString("date"),
                                            tourDoc.getString("startTime"),
                                            tourDoc.getString("endTime"),
                                            tourDoc.getString("imageUrl"),
                                            tourDoc.getDouble("price")
                                    );

                                    tg.setAddedServices(addedServices);

                                    tourList.add(tg);
                                    adapter.notifyDataSetChanged();

                                    Log.d("FIRESTORE", "Tours cargados: " + tourList.size());
                                    Log.d("Tour", "Tours : " + tg.getStatus());
                                });
                    }
                });
    }

}
