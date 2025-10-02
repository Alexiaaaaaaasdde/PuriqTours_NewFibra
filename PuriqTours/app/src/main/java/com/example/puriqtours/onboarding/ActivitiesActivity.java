package com.example.puriqtours.onboarding;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.google.android.material.chip.Chip;
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
                if (c instanceof Chip && ((Chip) c).isChecked()) {
                    sel.add(((Chip) c).getText().toString());
                }
            }

            // Guardar simple
            getSharedPreferences("onboarding", MODE_PRIVATE)
                    .edit().putString("activities_list", android.text.TextUtils.join(",", sel))
                    .apply();

            Toast.makeText(this, "Intereses guardados (" + sel.size() + ")", Toast.LENGTH_SHORT).show();

            // Aquí decide a dónde ir (Home, SetupProfile, etc.). Por ahora solo cerramos:
            finish();
        });
    }
}
