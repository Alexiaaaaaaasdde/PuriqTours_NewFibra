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

        adapter = new TourGuiaAdapter(getContext(), tourList, t -> {
            // Acción del botón "Iniciar"
            Intent intent = new Intent(getContext(), IniciarTourActivity.class);
            intent.putExtra("idReserva", t.getIdReserva());
            intent.putExtra("idTour", t.getIdTour());
            intent.putExtra("tokenInicio", t.getTokenInicio());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        cargarToursDelGuia();

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
                                });
                    }
                });
    }

}
