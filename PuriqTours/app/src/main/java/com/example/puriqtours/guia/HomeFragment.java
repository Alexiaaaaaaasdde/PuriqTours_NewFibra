package com.example.puriqtours.guia;

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
import com.example.puriqtours.adapter.SolicitudAdapter;
import com.example.puriqtours.entity.Solicitud;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private SolicitudAdapter adapter;
    private List<Solicitud> listaSolicitudes;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewSolicitudes);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 🔹 Inicializar Firestore y la lista
        db = FirebaseFirestore.getInstance();
        listaSolicitudes = new ArrayList<>();

        // 🔹 Crear el adapter (vacío por ahora)
        adapter = new SolicitudAdapter(listaSolicitudes, getParentFragmentManager());
        recyclerView.setAdapter(adapter);

        // 🔹 Cargar los datos desde Firebase
        cargarSolicitudesFirebase();

        return view;
    }

    private void cargarSolicitudesFirebase() {
        db.collection("solicitudes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaSolicitudes.clear(); // limpiar por si hay datos previos

                    for (DocumentSnapshot doc : querySnapshot) {
                        Solicitud solicitud = doc.toObject(Solicitud.class);
                        if (solicitud != null) {
                            listaSolicitudes.add(solicitud);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    Log.d("FIRESTORE", "Solicitudes cargadas: " + listaSolicitudes.size());
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_ERROR", "Error al cargar solicitudes", e);
                    Toast.makeText(requireContext(),
                            "Error al cargar solicitudes: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
