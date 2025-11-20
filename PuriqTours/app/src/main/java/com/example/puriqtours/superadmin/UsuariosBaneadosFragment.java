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

public class UsuariosBaneadosFragment extends Fragment {

    private UsuariosAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_usuarios_baneados, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerUsuariosBaneados);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        int bottomBarHeightPx = (int) (70 * getResources().getDisplayMetrics().density);
        recyclerView.setPadding(0, 0, 0, bottomBarHeightPx);
        recyclerView.setClipToPadding(false);

        List<Usuario> usuarios = new ArrayList<>();
        adapter = new UsuariosAdapter(getContext(), usuarios);
        recyclerView.setAdapter(adapter);

        // 🔥 Cargar clientes baneados (status = Inactivo)
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("rol", "Cliente")
                .whereEqualTo("status", "Inactivo")
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null || snapshot == null) return;

                    List<Usuario> list = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        Usuario u = Usuario.fromSnapshot(doc);
                        if (u == null) continue;

                        list.add(u);
                    }

                    adapter.setUsuarios(list);  // 🔥 Actualiza solo automáticamente
                });


        return view;
    }

    public void filtrarTexto(String texto) {
        if (adapter != null) adapter.filter(texto);
    }

    public void sortByName() {
        if (adapter != null) adapter.sortByNameAsc();
    }
}
