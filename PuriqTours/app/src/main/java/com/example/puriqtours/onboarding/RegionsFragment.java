package com.example.puriqtours.onboarding;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class RegionsFragment extends Fragment {

    private ChipGroup grpRegions, grpTowns;
    private Chip chipOtherRegion, chipOtherTown;
    private EditText etOtherRegion, etOtherTown;
    private Button btnContinue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_regions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        grpRegions     = v.findViewById(R.id.grpRegions);
        grpTowns       = v.findViewById(R.id.grpTowns);
        chipOtherRegion= v.findViewById(R.id.chipOtherRegion);
        chipOtherTown  = v.findViewById(R.id.chipOtherTown);
        etOtherRegion  = v.findViewById(R.id.etOtherRegion);
        etOtherTown    = v.findViewById(R.id.etOtherTown);
        btnContinue    = v.findViewById(R.id.btnRegionsContinue);

        // Mostrar/ocultar campos "Otro"
        chipOtherRegion.setOnCheckedChangeListener((c, isChecked) ->
                etOtherRegion.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        chipOtherTown.setOnCheckedChangeListener((c, isChecked) ->
                etOtherTown.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        btnContinue.setOnClickListener(view -> {
            ArrayList<String> selections = new ArrayList<>();
            selections.addAll(getCheckedTexts(grpRegions));
            selections.addAll(getCheckedTexts(grpTowns));

            if (chipOtherRegion.isChecked()) {
                String val = safeText(etOtherRegion);
                if (TextUtils.isEmpty(val)) { etOtherRegion.setError("Escribe una región/ciudad"); etOtherRegion.requestFocus(); return; }
                selections.add("other_region:" + val);
            }
            if (chipOtherTown.isChecked()) {
                String val = safeText(etOtherTown);
                if (TextUtils.isEmpty(val)) { etOtherTown.setError("Escribe un lugar/pueblo"); etOtherTown.requestFocus(); return; }
                selections.add("other_town:" + val);
            }

            if (selections.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona al menos una región o pueblo", Toast.LENGTH_SHORT).show();
                return;
            }

            ((InterestsOnboardingActivity) requireActivity()).onRegionsChosen(selections);
        });
    }

    private List<String> getCheckedTexts(ChipGroup group) {
        List<String> res = new ArrayList<>();
        for (Integer id : group.getCheckedChipIds()) {
            Chip chip = group.findViewById(id);
            if (chip != null && chip != chipOtherRegion && chip != chipOtherTown) {
                res.add(chip.getText().toString());
            }
        }
        return res;
    }

    private String safeText(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
