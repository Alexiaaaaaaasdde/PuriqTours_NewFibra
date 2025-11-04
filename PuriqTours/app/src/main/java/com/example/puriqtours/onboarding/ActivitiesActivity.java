package com.example.puriqtours.onboarding;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.LocalAuth;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class ActivitiesActivity extends AppCompatActivity {

    private ChipGroup grpActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);

        grpActivities = findViewById(R.id.grpActivities);

        // Botón atrás flotante
        View back = findViewById(R.id.btnBackActivities);
        if (back != null) back.setOnClickListener(v -> finish());

        // Continuar / Finalizar
        findViewById(R.id.btnActivitiesFinish).setOnClickListener(v -> {
            ArrayList<String> sel = new ArrayList<>();
            for (int i = 0; i < grpActivities.getChildCount(); i++) {
                View c = grpActivities.getChildAt(i);
                if (c instanceof com.google.android.material.chip.Chip && ((com.google.android.material.chip.Chip) c).isChecked()) {
                    sel.add(((com.google.android.material.chip.Chip) c).getText().toString());
                }
            }

            // ✅ Guardar selección de actividades en LocalAuth
            LocalAuth localAuth = new LocalAuth(this);
            String activities = android.text.TextUtils.join(", ", sel);

            // Guardamos con todos los datos anteriores + nuevas actividades
            localAuth.saveUser(
                    localAuth.getName(),
                    localAuth.getLastname(),
                    localAuth.getEmail(),
                    localAuth.getPassword(),
                    localAuth.getBirthdate(),
                    localAuth.getDocument(),
                    localAuth.getPhone(),
                    localAuth.getAddress(),
                    localAuth.getDocType(),
                    localAuth.getLanguage(),  // mantenemos idioma
                    activities,               // ✅ nuevas actividades
                    localAuth.getPhotoUri()
            );

            // Guardar copia en SharedPreferences (opcional)
            getSharedPreferences("onboarding", MODE_PRIVATE)
                    .edit()
                    .putString("activities_list", android.text.TextUtils.join(",", sel))
                    .apply();

            Toast.makeText(this, "Intereses guardados (" + sel.size() + ")", Toast.LENGTH_SHORT).show();

            // ✅ Ir al perfil final
            startActivity(new android.content.Intent(
                    com.example.puriqtours.onboarding.ActivitiesActivity.this,
                    com.example.puriqtours.cliente.ProfileActivity.class
            ));
            finish();
        });
    }
}
