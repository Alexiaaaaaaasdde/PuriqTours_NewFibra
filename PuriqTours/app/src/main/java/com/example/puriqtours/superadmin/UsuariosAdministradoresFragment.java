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
        for (int i = 1; i <= 20; i++) {
            usuarios.add(new UsuarioAdministrador("Administrador " + i, "Ciudad " + i, "Empresa " + ((i%3)+1), "03/09/2025"));
        }

        UsuariosAdapter adapter = new UsuariosAdapter(getContext(), usuarios);
        recyclerView.setAdapter(adapter);

        FrameLayout containerLayout = view.findViewById(R.id.recyclerContainer);
        containerLayout.removeAllViews();
        containerLayout.addView(recyclerView);

        return view;
    }
}
