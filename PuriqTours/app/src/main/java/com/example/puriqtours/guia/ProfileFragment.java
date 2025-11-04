package com.example.puriqtours.guia;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Usuario;
import com.example.puriqtours.helper.UserSessionManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private EditText etNombre, etApellido, etFechaNacimiento, etNumeroDocumento,
            etNumeroTelefonico, etTipoDocumento, etDireccion, etCorreo;
    private ShapeableImageView profileImage;
    private Button btnUpdate, btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflamos el layout del fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Referencias a las vistas
        profileImage = view.findViewById(R.id.profileImage);
        etNombre = view.findViewById(R.id.etNombre);
        etApellido = view.findViewById(R.id.etApellido);
        etFechaNacimiento = view.findViewById(R.id.etFechaNacimiento);
        etNumeroDocumento = view.findViewById(R.id.etNumeroDocumento);
        etNumeroTelefonico = view.findViewById(R.id.etNumeroTelefonico);
        etTipoDocumento = view.findViewById(R.id.etTipoDocumento);
        etDireccion = view.findViewById(R.id.etDireccion);
        etCorreo = view.findViewById(R.id.etCorreo);

        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnSave = view.findViewById(R.id.btnSave);

        Context context = requireContext();

        cargarDatosUsuario(context);

        // Lógica de botones
        btnUpdate.setOnClickListener(v -> {
            habilitarCampos(true);
            btnUpdate.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
        });

        btnSave.setOnClickListener(v -> {
            habilitarCampos(false);
            btnUpdate.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);

            Toast.makeText(requireContext(), "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void habilitarCampos(boolean habilitar) {
        etNombre.setEnabled(habilitar);
        etApellido.setEnabled(habilitar);
        etFechaNacimiento.setEnabled(habilitar);
        etNumeroDocumento.setEnabled(habilitar);
        etNumeroTelefonico.setEnabled(habilitar);
        etTipoDocumento.setEnabled(habilitar);
        etDireccion.setEnabled(habilitar);
        etCorreo.setEnabled(habilitar);
    }

    private void cargarDatosUsuario(Context context) {
        UserSessionManager userSessionManager = new UserSessionManager(context);
        Usuario user;
        user = userSessionManager.getUser();
        etNombre.setText(user.getName());
        etApellido.setText(user.getLast_name());
        etFechaNacimiento.setText(user.getBirthdate());
        etTipoDocumento.setText(user.getDoc_type());
        etNumeroDocumento.setText(user.getDocument());
        etNumeroTelefonico.setText(user.getPhone());
        etDireccion.setText(user.getAddress());
        etCorreo.setText(user.getEmail());
        String imageUrl = user.getProfile_image();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.imagen_perfil) // imagen temporal
                    .error(R.drawable.imagen_perfil) // si falla la carga
                    .into(profileImage);
        }

        habilitarCampos(false);
        btnSave.setVisibility(View.GONE);
    }
}
