package com.example.puriqtours.superadmin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class UsuariosAdministradoresFragment extends Fragment {

    private UsuariosAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_usuarios_administradores, container, false);

        FloatingActionButton fab = view.findViewById(R.id.fabAgregarAdmin);
        fab.setOnClickListener(v -> {
            // Acción al agregar admin
        });

        // → Usamos un RecyclerView programático (tú lo tenías así)
        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setId(View.generateViewId());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        int bottomBarHeightPx = (int) (70 * getResources().getDisplayMetrics().density);
        recyclerView.setPadding(0, 0, 0, bottomBarHeightPx);
        recyclerView.setClipToPadding(false);

        // Adapter vacío
        List<Usuario> usuarios = new ArrayList<>();
        adapter = new UsuariosAdapter(getContext(), usuarios);
        recyclerView.setAdapter(adapter);

        // Insertar RecyclerView al contenedor
        FrameLayout containerLayout = view.findViewById(R.id.recyclerContainer);
        containerLayout.removeAllViews();
        containerLayout.addView(recyclerView);

        // 🔥 Cargar administradores desde Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get(Source.SERVER)
                .addOnSuccessListener(snapshot -> {

                    List<Usuario> admins = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        String rol = doc.getString("rol");
                        if (rol == null) continue;

                        // Solo administradores
                        if (rol.equalsIgnoreCase("Admin") ||
                                rol.equalsIgnoreCase("SuperAdmin")) {

                            Usuario u = Usuario.fromSnapshot(doc);

                            // Correcciones por si faltan campos
                            if (u.getName() == null)
                                u.setName(doc.getString("username"));

                            admins.add(u);
                        }
                    }

                    adapter.setUsuarios(admins);
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
