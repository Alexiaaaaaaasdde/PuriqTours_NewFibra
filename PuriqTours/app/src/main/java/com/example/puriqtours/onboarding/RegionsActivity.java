package com.example.puriqtours.onboarding;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class RegionsActivity extends AppCompatActivity {

    private ChipGroup grpRegions, grpTowns;
    private Chip chipOtherRegion, chipOtherTown;
    private EditText etOtherRegion, etOtherTown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regions);

        findViewById(R.id.btnBackRegions).setOnClickListener(v -> finish());

        grpRegions      = findViewById(R.id.grpRegions);
        grpTowns        = findViewById(R.id.grpTowns);
        chipOtherRegion = findViewById(R.id.chipOtherRegion);
        chipOtherTown   = findViewById(R.id.chipOtherTown);
        etOtherRegion   = findViewById(R.id.etOtherRegion);
        etOtherTown     = findViewById(R.id.etOtherTown);

        // Mostrar/ocultar “Otro”
        chipOtherRegion.setOnCheckedChangeListener((b, checked) -> {
            etOtherRegion.setVisibility(checked ? View.VISIBLE : View.GONE);
            if (!checked) etOtherRegion.setText(null);
        });
        chipOtherTown.setOnCheckedChangeListener((b, checked) -> {
            etOtherTown.setVisibility(checked ? View.VISIBLE : View.GONE);
            if (!checked) etOtherTown.setText(null);
        });

        findViewById(R.id.btnRegionsContinue).setOnClickListener(v -> {
            // recoge seleccionados (rápido)
            ArrayList<String> sel = new ArrayList<>();
            addChecked(grpRegions, sel);
            addChecked(grpTowns, sel);
            if (chipOtherRegion.isChecked() && !isEmpty(etOtherRegion)) sel.add(etOtherRegion.getText().toString().trim());
            if (chipOtherTown.isChecked() && !isEmpty(etOtherTown)) sel.add(etOtherTown.getText().toString().trim());

            startActivity(new android.content.Intent(
                    RegionsActivity.this,
                    com.example.puriqtours.onboarding.ActivitiesActivity.class
            ));

            // guarda simple en SharedPreferences (opcional)
            getSharedPreferences("onboarding", MODE_PRIVATE)
                    .edit().putString("regions_list", TextUtils.join(",", sel)).apply();

            Toast.makeText(this, "Regiones guardadas (" + sel.size() + ")", Toast.LENGTH_SHORT).show();
            finish(); // regresa al flujo o aquí podrías abrir PreferencesActivity si la tienes
        });
    }

    private void addChecked(ChipGroup group, ArrayList<String> out) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View c = group.getChildAt(i);
            if (c instanceof Chip && ((Chip) c).isChecked()) {
                out.add(((Chip) c).getText().toString());
            }
        }
    }

    private boolean isEmpty(EditText et) {
        return et.getText() == null || et.getText().toString().trim().isEmpty();
    }
}
