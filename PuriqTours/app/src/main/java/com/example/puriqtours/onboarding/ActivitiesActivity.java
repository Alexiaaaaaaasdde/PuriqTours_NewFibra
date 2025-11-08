package com.example.puriqtours.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Usuario;
import com.example.puriqtours.helper.UserSessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivitiesActivity extends AppCompatActivity {

    private ChipGroup grpActivities;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserSessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);

        grpActivities = findViewById(R.id.grpActivities);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        session = new UserSessionManager(this);

        // Botón atrás flotante
        View back = findViewById(R.id.btnBackActivities);
        if (back != null) back.setOnClickListener(v -> finish());

        // Botón continuar/finalizar
        findViewById(R.id.btnActivitiesFinish).setOnClickListener(v -> {
            ArrayList<String> sel = new ArrayList<>();
            for (int i = 0; i < grpActivities.getChildCount(); i++) {
                View c = grpActivities.getChildAt(i);
                if (c instanceof Chip && ((Chip) c).isChecked()) {
                    sel.add(((Chip) c).getText().toString());
                }
            }

            if (sel.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos una actividad o interés", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = currentUser.getUid();
            Map<String, Object> updates = new HashMap<>();
            updates.put("activities", sel); // 🔥 guardamos como lista de strings

            db.collection("users")
                    .document(uid)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        // ✅ Actualizar sesión local
                        Usuario usuario = session.getUser();
                        if (usuario != null) {
                            usuario.setActivities(sel);
                            session.saveUser(usuario);
                        }

                        Toast.makeText(this, "Actividades guardadas correctamente", Toast.LENGTH_SHORT).show();

                        // 👉 Ir al perfil final
                        startActivity(new Intent(
                                ActivitiesActivity.this,
                                com.example.puriqtours.SplashActivity.class
                        ));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al guardar actividades: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}
