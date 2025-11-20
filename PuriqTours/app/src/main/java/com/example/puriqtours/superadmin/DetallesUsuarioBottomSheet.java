package com.example.puriqtours.superadmin;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Usuario;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DetallesUsuarioBottomSheet extends BottomSheetDialogFragment {

    private final Usuario usuario;

    public DetallesUsuarioBottomSheet(Usuario usuario) {
        this.usuario = usuario;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottomsheet_detalles_usuario, container, false);

        TextView tvNombre = view.findViewById(R.id.tvNombre);
        TextView tvRol = view.findViewById(R.id.tvRol);
        LinearLayout containerCampos = view.findViewById(R.id.containerCampos);

        tvNombre.setText(usuario.getName() + " " + usuario.getLast_name());
        tvRol.setText("Rol: " + usuario.getRol());

        // 🔹 SIEMPRE mostramos estos (existen para todos)
        addCampo(containerCampos, "Correo", usuario.getEmail());
        addCampo(containerCampos, "Teléfono", usuario.getPhone());
        addCampo(containerCampos, "Dirección", usuario.getAddress());
        addCampo(containerCampos, "Idioma", usuario.getLanguage());
        addCampo(containerCampos, "Estado", usuario.getStatus());

        // 🔹 Datos por rol
        switch (usuario.getRol().toLowerCase()) {
            case "cliente":
                addCampo(containerCampos, "Tipo de documento", usuario.getDoc_type());
                addCampo(containerCampos, "Número documento", usuario.getDocument());
                addCampo(containerCampos, "Fecha nacimiento", usuario.getBirthdate());
                // addCampo(containerCampos, "Actividades", usuario.getActivities() != null ?
                //        usuario.getActivities().toString() : "-");
                break;

            case "guia":
            case "admin":
            case "empresa":
            case "superadmin":
                // Estos no tienen campos extras
                break;
        }

        return view;
    }

    private void addCampo(LinearLayout parent, String label, String value) {
        TextView tv = new TextView(getContext());
        tv.setText(label + ": " + (value == null ? "-" : value));
        tv.setTextSize(16);
        tv.setTextColor(Color.parseColor("#2B746C"));
        tv.setPadding(0, 8, 0, 8);
        parent.addView(tv);
    }
}
