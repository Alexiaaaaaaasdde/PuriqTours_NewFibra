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
        // Clientes
        usuarios.add(new UsuarioCliente("Usuario 1", "Cusco"));
        usuarios.add(new UsuarioCliente("Usuario 2", "Tumbes"));
        usuarios.add(new UsuarioCliente("Usuario 3", "Lima"));
        usuarios.add(new UsuarioCliente("Usuario 4", "Amazonas"));
        usuarios.add(new UsuarioCliente("Usuario 5", "Oxapampa"));
        // Guías
        usuarios.add(new UsuarioGuia("Guía número 1", "Cusco", 5));
        usuarios.add(new UsuarioGuia("Guía número 2", "Tumbes", 4));
        usuarios.add(new UsuarioGuia("Guía número 3", "Lima", 3));
        usuarios.add(new UsuarioGuia("Guía número 4", "Amazonas", 4));
        usuarios.add(new UsuarioGuia("Guía número 5", "Oxapampa", 5));
        // Administradores
        usuarios.add(new UsuarioAdministrador("Administrador 1", "Cusco", "Silk Road", "03/09/2025"));
        usuarios.add(new UsuarioAdministrador("Administrador 2", "Oxapampa", "Silk Road", "03/09/2025"));
        usuarios.add(new UsuarioAdministrador("Administrador 3", "Oxapampa", "Silk Road", "03/09/2025"));
        usuarios.add(new UsuarioAdministrador("Administrador 4", "Oxapampa", "Silk Road", "03/09/2025"));
        usuarios.add(new UsuarioAdministrador("Administrador 5", "Oxapampa", "Silk Road", "03/09/2025"));

        UsuariosAdapter adapter = new UsuariosAdapter(getContext(), usuarios);
        recyclerView.setAdapter(adapter);

        // Reemplaza el contenido del ScrollView por el RecyclerView
        ViewGroup root = (ViewGroup) view;
        root.removeAllViews();
        root.addView(recyclerView);

        return view;
    }
}
