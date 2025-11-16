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
import java.util.ArrayList;
import java.util.List;

public class UsuariosTodosFragment extends Fragment {
    private UsuariosAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usuarios_todos, container, false);

        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setId(View.generateViewId());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        int bottomBarHeightPx = (int) (70 * getResources().getDisplayMetrics().density); // 70dp en px
        recyclerView.setPadding(0, 0, 0, bottomBarHeightPx); // padding bottom para BottomBar
        recyclerView.setClipToPadding(false);

        List<Usuario> usuarios = new ArrayList<>();
        adapter = new UsuariosAdapter(getContext(), usuarios);
        recyclerView.setAdapter(adapter);

        // Reemplaza el contenido del ScrollView por el RecyclerView
        ViewGroup root = (ViewGroup) view;
        root.removeAllViews();
        root.addView(recyclerView);

        // Cargar todos los usuarios desde Firestore (sin filtrar)
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("users").get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(snapshot -> {
                List<Usuario> list = new ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshot) {
                    String rol = doc.getString("rol");
                    String uid = doc.getId();
                    String name = doc.getString("name");
                    if (name == null) name = doc.getString("username");
                    String city = doc.getString("address");
                    String state = doc.getString("state");
                    if (rol != null) {
                        String r = rol.toLowerCase();
                        if (r.contains("cli")) {
                            list.add(new UsuarioCliente(uid, name != null ? name : "Usuario", city != null ? city : "", state != null ? state : "habilitado"));
                        } else if (r.contains("guia")) {
                            int rating = 0; try { Object rv = doc.get("valoracion"); if (rv instanceof Number) rating = ((Number)rv).intValue(); } catch(Exception ex){}
                            String profile = doc.getString("profile_image");
                            list.add(new UsuarioGuia(uid, name != null ? name : "Guía", city != null ? city : "", rating, profile, state != null ? state : "habilitado"));
                        } else if (r.contains("admin")) {
                            Object registroObj = doc.get("registro");
                            String registro = "";
                            if (registroObj instanceof String) registro = (String) registroObj;
                            else if (registroObj instanceof com.google.firebase.Timestamp) registro = ((com.google.firebase.Timestamp) registroObj).toDate().toString();
                            else if (registroObj instanceof java.util.Date) registro = ((java.util.Date) registroObj).toString();
                            else if (registroObj != null) registro = registroObj.toString();
                            String empresaId = doc.getString("empresa");
                            UsuarioAdministrador ua = new UsuarioAdministrador(uid, name != null ? name : "Admin", city != null ? city : "", empresaId != null ? empresaId : "", registro, state != null ? state : "habilitado");
                            list.add(ua);
                        } else {
                            // rol desconocido: tratar como cliente por defecto
                            list.add(new UsuarioCliente(uid, name != null ? name : "Usuario", city != null ? city : "", state != null ? state : "habilitado"));
                        }
                    } else {
                        list.add(new UsuarioCliente(uid, name != null ? name : "Usuario", city != null ? city : "", state != null ? state : "habilitado"));
                    }
                }
                adapter.setUsuarios(list);
            });

        return view;
    }

    public void sortByName() {
        if (adapter != null) adapter.sortByNameAsc();
    }
}
