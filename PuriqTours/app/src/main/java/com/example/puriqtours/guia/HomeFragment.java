package com.example.puriqtours.guia;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.adapter.SolicitudAdapter;
import com.example.puriqtours.entity.Solicitud;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private SolicitudAdapter adapter;
    private List<Solicitud> listaSolicitudes;
    private FirebaseFirestore db;

    private FirebaseAuth mAuth;

    private ChipGroup chipGroupEstados;
    private Chip chipTodos, chipPendiente, chipAceptado, chipRechazado;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // ------------------------------------------------------
        // 🔹 Inicializar RecyclerView
        // ------------------------------------------------------
        recyclerView = view.findViewById(R.id.recyclerViewSolicitudes);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // ------------------------------------------------------
        // 🔹 Firestore, Auth y lista
        // ------------------------------------------------------
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listaSolicitudes = new ArrayList<>();

        // ------------------------------------------------------
        // 🔹 Inicializar Adapter
        // ------------------------------------------------------
        adapter = new SolicitudAdapter(listaSolicitudes, getParentFragmentManager());
        recyclerView.setAdapter(adapter);

        // ------------------------------------------------------
        // 🔹 Inicializar Chips
        // ------------------------------------------------------
        chipGroupEstados = view.findViewById(R.id.chipGroupEstados);
        chipTodos = view.findViewById(R.id.chipTodos);
        chipPendiente = view.findViewById(R.id.chipPendiente);
        chipAceptado = view.findViewById(R.id.chipAceptado);
        chipRechazado = view.findViewById(R.id.chipRechazado);

        configurarFiltroChips();

        // ------------------------------------------------------
        // 🔹 Cargar solicitudes
        // ------------------------------------------------------
        cargarSolicitudesFirebase();

        return view;
    }

    // ==========================================================
    // 🔥 LÓGICA DEL FILTRO CON CHIPS
    // ==========================================================
    private void configurarFiltroChips() {
        chipGroupEstados.setOnCheckedStateChangeListener((group, checkedIds) -> {

            if (checkedIds.isEmpty()) {
                adapter.setEstadoFiltro("Todos");
                return;
            }

            int id = checkedIds.get(0);

            if (id == R.id.chipPendiente) {
                adapter.setEstadoFiltro("Pendiente");
            } else if (id == R.id.chipAceptado) {
                adapter.setEstadoFiltro("Aceptado");
            } else if (id == R.id.chipRechazado) {
                adapter.setEstadoFiltro("Rechazado");
            } else {
                adapter.setEstadoFiltro("Todos");
            }
        });
    }

    // ==========================================================
    // 🔥 CARGA DE SOLICITUDES FIREBASE
    // ==========================================================
    private void cargarSolicitudesFirebase() {

        if (mAuth.getCurrentUser() == null) return;

        String uidGuia = mAuth.getCurrentUser().getUid();
        Log.d("FIREBASE", "ID guia: " + uidGuia);

        db.collection("solicitudes")
                .whereEqualTo("idGuia", uidGuia)
                .addSnapshotListener((querySnapshot, error) -> {

                    if (error != null || querySnapshot == null) return;

                    listaSolicitudes.clear();

                    for (DocumentSnapshot doc : querySnapshot) {

                        Solicitud solicitud = doc.toObject(Solicitud.class);
                        solicitud.setIdSolicitud(doc.getId());

                        if (solicitud == null) continue;

                        String idTour = solicitud.getIdTour();
                        Log.d("FIRESTORE", "Id del tour: " + idTour);

                        // 🔹 Obtener imagen desde el documento tour
                        db.collection("tours")
                                .document(idTour)
                                .get()
                                .addOnSuccessListener(tourDoc -> {

                                    if (tourDoc.exists()) {
                                        String url = tourDoc.getString("imageUrl");
                                        solicitud.setImageUrl(url);
                                    }

                                    listaSolicitudes.add(solicitud);
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e ->
                                        Log.e("FIRESTORE",
                                                "Error obteniendo imagen del tour " + idTour, e)
                                );
                    }
                });
    }
}
