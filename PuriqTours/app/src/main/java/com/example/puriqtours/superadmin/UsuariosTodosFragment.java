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

public class UsuariosTodosFragment extends Fragment {

    private UsuariosAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_usuarios_todos, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerUsuariosTodos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        int bottomBarHeightPx = (int) (70 * getResources().getDisplayMetrics().density);
        recyclerView.setPadding(0, 0, 0, bottomBarHeightPx);
        recyclerView.setClipToPadding(false);

        List<Usuario> usuarios = new ArrayList<>();
        adapter = new UsuariosAdapter(getContext(), usuarios);
        recyclerView.setAdapter(adapter);

        // Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get(Source.SERVER)
                .addOnSuccessListener(snapshot -> {

                    List<Usuario> list = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        Usuario u = new Usuario();  // instancia vacía

                        // ✔ USANDO SETTERS (CORRECTO)
                        u.setUid(doc.getId());

                        String name = doc.getString("name");
                        if (name == null) name = doc.getString("username");
                        u.setName(name);

                        u.setAddress(doc.getString("address"));
                        u.setRol(doc.getString("rol"));
                        u.setStatus(doc.getString("status"));
                        u.setProfile_image(doc.getString("profile_image"));

                        list.add(u);
                    }

                    adapter.setUsuarios(list);
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
