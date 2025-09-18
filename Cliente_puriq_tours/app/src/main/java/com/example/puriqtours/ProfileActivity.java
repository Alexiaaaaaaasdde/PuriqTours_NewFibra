package com.example.puriqtours;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Habilita EdgeToEdge
        EdgeToEdge.enable(this);

        // Conecta el layout
        setContentView(R.layout.activity_profile_activity);

        // Configuración de insets para pantallas modernas
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 BottomNavigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_perfil) {
                return true; // Ya estás en perfil
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
                return true;
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                return true;
            }
            return false;
        });

        // 🔹 Referencias a vistas
        Button btnUpdate = findViewById(R.id.btnUpdate);
        Button btnSave = findViewById(R.id.btnSave);
        ShapeableImageView profileImage = findViewById(R.id.profileImage);
        EditText etNumeroTelefonico = findViewById(R.id.etNumeroTelefonico);

        // Otros campos (que quedarán bloqueados siempre)
        EditText etNombre = findViewById(R.id.etNombre);
        EditText etApellido = findViewById(R.id.etApellido);
        EditText etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        EditText etNumeroDocumento = findViewById(R.id.etNumeroDocumento);
        EditText etTipoDocumento = findViewById(R.id.etTipoDocumento);
        EditText etDireccion = findViewById(R.id.etDireccion);
        EditText etCorreo = findViewById(R.id.etCorreo);

        // 🔹 Al inicio, botón guardar oculto
        btnSave.setVisibility(Button.GONE);

        // 👉 Cuando presionas "Actualizar datos"
        btnUpdate.setOnClickListener(v -> {
            etNumeroTelefonico.setEnabled(true);
            etNumeroTelefonico.requestFocus();

            profileImage.setClickable(true);
            Toast.makeText(this, "Ahora puedes editar el teléfono o la foto", Toast.LENGTH_SHORT).show();

            btnUpdate.setVisibility(Button.GONE);
            btnSave.setVisibility(Button.VISIBLE);
        });

        // 👉 Cuando presionas "Guardar cambios"
        btnSave.setOnClickListener(v -> {
            etNumeroTelefonico.setEnabled(false);
            profileImage.setClickable(false);

            // ✅ Mostrar el popup personalizado
            Dialog dialog = new Dialog(ProfileActivity.this);
            dialog.setContentView(R.layout.dialog_success);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setCancelable(false);

            Button btnClose = dialog.findViewById(R.id.btnClose);
            btnClose.setOnClickListener(closeView -> dialog.dismiss());

            dialog.show();

            btnSave.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.VISIBLE);
        });

    }
}
