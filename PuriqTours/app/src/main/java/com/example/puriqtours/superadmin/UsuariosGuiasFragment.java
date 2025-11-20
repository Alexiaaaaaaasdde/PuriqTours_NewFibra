package com.example.puriqtours.superadmin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Usuario;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class UsuariosGuiasFragment extends Fragment {

    private UsuariosAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_usuarios_guias, container, false);

        // 🟢 Usamos el RecyclerView del XML
        RecyclerView recyclerView = view.findViewById(R.id.recyclerUsuariosGuias);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        int bottomBarHeightPx = (int) (70 * getResources().getDisplayMetrics().density);
        recyclerView.setPadding(0, 0, 0, bottomBarHeightPx);
        recyclerView.setClipToPadding(false);

        // Adaptador vacío
        List<Usuario> usuarios = new ArrayList<>();
        adapter = new UsuariosAdapter(getContext(), usuarios);
        recyclerView.setAdapter(adapter);

        // 🟣 Cargar GUÍAS desde Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("rol", "Guia")
                .whereEqualTo("status", "Activo")
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null || snapshot == null) return;

                    List<Usuario> guias = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Usuario u = Usuario.fromSnapshot(doc);

                        if (u.getName() == null)
                            u.setName(doc.getString("username"));

                        guias.add(u);
                    }

                    adapter.setUsuarios(guias);
                });


        return view;
    }

    // Buscador
    public void filtrarTexto(String texto) {
        if (adapter != null) adapter.filter(texto);
    }

    // Ordenar
    public void sortByName() {
        if (adapter != null) adapter.sortByNameAsc();
    }
}
