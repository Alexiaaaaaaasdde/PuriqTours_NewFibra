package com.example.puriqtours;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    SolicitudAdapter adapter;
    List<Solicitud> lista;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewSolicitudes);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        lista = new ArrayList<>();
        lista.add(new Solicitud("Tour 1", "Esta es la descripción completa de la solicitud 1", R.drawable.machupicchu, "Lima", "18/09/2025", LocalTime.of(18, 0), LocalTime.of(19, 0), "TOURLIMA"));
        lista.add(new Solicitud("Tour 2", "Texto laaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaargo de descripción de la solicitud 2", R.drawable.machupicchu, "Cusco", "26/09/2025", LocalTime.of(7, 0), LocalTime.of(17, 0), "TOURCUSCO"));

        SolicitudAdapter adapter = new SolicitudAdapter(lista, getParentFragmentManager());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        return view;
    }
}
