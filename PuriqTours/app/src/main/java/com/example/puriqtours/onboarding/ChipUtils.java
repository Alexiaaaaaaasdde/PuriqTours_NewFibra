package com.example.puriqtours.onboarding;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;

class ChipUtils {
    static ArrayList<String> getCheckedTexts(ChipGroup group) {
        ArrayList<String> sel = new ArrayList<>();
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip c = (Chip) group.getChildAt(i);
            if (c.isChecked()) sel.add(c.getText().toString());
        }
        return sel;
    }
}
