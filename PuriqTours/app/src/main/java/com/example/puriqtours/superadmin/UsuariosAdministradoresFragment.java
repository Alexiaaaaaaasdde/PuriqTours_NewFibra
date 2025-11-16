package com.example.puriqtours.superadmin;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.puriqtours.R;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class UsuariosAdministradoresFragment extends Fragment {
    private UsuariosAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usuarios_administradores, container, false);
        FloatingActionButton fab = view.findViewById(R.id.fabAgregarAdmin);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción para agregar administrador
            }
        });

        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setId(View.generateViewId());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        int bottomBarHeightPx = (int) (70 * getResources().getDisplayMetrics().density); // 70dp en px
        recyclerView.setPadding(0, 0, 0, bottomBarHeightPx); // padding bottom para BottomBar
        recyclerView.setClipToPadding(false);

    List<Usuario> usuarios = new ArrayList<>();
    adapter = new UsuariosAdapter(getContext(), usuarios);
    recyclerView.setAdapter(adapter);

        FrameLayout containerLayout = view.findViewById(R.id.recyclerContainer);
        containerLayout.removeAllViews();
        containerLayout.addView(recyclerView);

        // Cargar administradores desde Firestore
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("users").get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(snapshot -> {
                List<Usuario> list = new ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshot) {
                    String rol = doc.getString("rol");
                    if (rol == null) continue;
                    String r = rol.toLowerCase();
                    if (r.contains("admin")) {
                        String uid = doc.getId();
                        String name = doc.getString("name");
                        if (name == null) name = doc.getString("username");
                        String address = doc.getString("address");
                        String empresaId = doc.getString("empresa");
                        // registro puede venir como String, Timestamp, Date o incluso un Map => manejarlo sin lanzar excepciones
                        Object registroObj = doc.get("registro");
                        String registro = "";
                        if (registroObj instanceof String) {
                            registro = (String) registroObj;
                        } else if (registroObj instanceof com.google.firebase.Timestamp) {
                            registro = ((com.google.firebase.Timestamp) registroObj).toDate().toString();
                        } else if (registroObj instanceof java.util.Date) {
                            registro = ((java.util.Date) registroObj).toString();
                        } else if (registroObj instanceof java.util.Map) {
                            java.util.Map<?,?> m = (java.util.Map<?,?>) registroObj;
                            Object f = m.get("fecha");
                            if (f == null) f = m.get("date");
                            registro = f != null ? f.toString() : m.toString();
                        } else if (registroObj != null) {
                            registro = registroObj.toString();
                        }

                        String state = doc.getString("state");
                        UsuarioAdministrador ua = new UsuarioAdministrador(uid, name != null ? name : "Admin", address != null ? address : "", empresaId != null ? empresaId : "", registro, state != null ? state : "habilitado");
                        list.add(ua);

                        // si empresaId parece un id, intentar obtener nombre de empresas
                        if (empresaId != null && !empresaId.isEmpty()) {
                            String eid = empresaId;
                            db.collection("empresas").document(eid).get(com.google.firebase.firestore.Source.SERVER)
                                .addOnSuccessListener(ed -> {
                                    String nombreEmpresa = ed.getString("nombre");
                                    if (nombreEmpresa != null) {
                                        ua.empresa = nombreEmpresa;
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                        }
                    }
                }
                this.adapter.setUsuarios(list);
            });

        return view;
    }

    // permitir ordenar desde la Activity
    public void sortByName() {
        if (adapter != null) adapter.sortByNameAsc();
    }
}
