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

public class UsuariosClientesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usuarios_clientes, container, false);

        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setId(View.generateViewId());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        int bottomBarHeightPx = (int) (70 * getResources().getDisplayMetrics().density); // 70dp en px
        recyclerView.setPadding(0, 0, 0, bottomBarHeightPx); // padding bottom para BottomBar
        recyclerView.setClipToPadding(false);

        List<Usuario> usuarios = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            usuarios.add(new UsuarioCliente("Usuario " + i, "Ciudad " + i));
        }

        UsuariosAdapter adapter = new UsuariosAdapter(getContext(), usuarios);
        recyclerView.setAdapter(adapter);

        // Reemplaza el contenido del ScrollView por el RecyclerView
        ViewGroup root = (ViewGroup) view;
        root.removeAllViews();
        root.addView(recyclerView);

        return view;
    }
}

