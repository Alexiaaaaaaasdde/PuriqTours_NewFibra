package com.example.puriqtours.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.puriqtours.R;

public class LanguagesFragment extends Fragment {

    private RadioGroup group;
    private RadioButton rbOther;
    private EditText etOther;
    private Button btnContinue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // usa el layout que ya tienes con estos ids
        return inflater.inflate(R.layout.fragment_languages_simple, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        group       = v.findViewById(R.id.groupLangs);
        rbOther     = v.findViewById(R.id.rbOther);
        etOther     = v.findViewById(R.id.etOtherLanguage);
        btnContinue = v.findViewById(R.id.btnLangContinue);

        // mostrar/ocultar campo "Otro"
        group.setOnCheckedChangeListener((g, checkedId) -> {
            boolean isOther = (checkedId == R.id.rbOther);
            etOther.setVisibility(isOther ? View.VISIBLE : View.GONE);
            if (!isOther) etOther.setError(null);
            updateButtonEnabled();
        });

        // habilitar botón cuando “Otro” tiene texto
        etOther.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateButtonEnabled(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // CONTINUAR -> abrir RegionsActivity (sin fragments)
        btnContinue.setOnClickListener(view -> {
            if (rbOther.isChecked() && TextUtils.isEmpty(etOther.getText().toString().trim())) {
                etOther.setError("Escribe el idioma");
                etOther.requestFocus();
                return;
            }

            try {
                // (opcional) guarda el idioma si lo necesitas
                // String code = mapSelectionToCode();
                // requireActivity().getSharedPreferences("onboarding", android.content.Context.MODE_PRIVATE)
                //         .edit().putString("language", code).apply();

                // Confirmación visual
                android.widget.Toast.makeText(requireContext(), "Abriendo Regiones…", android.widget.Toast.LENGTH_SHORT).show();

                // 👉 Cambia a la Activity de Regiones
                Intent i = new Intent(requireContext(), com.example.puriqtours.onboarding.RegionsActivity.class);
                startActivity(i);

            } catch (Throwable e) {
                android.widget.Toast.makeText(
                        requireContext(),
                        "No se pudo abrir Regiones: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                        android.widget.Toast.LENGTH_LONG
                ).show();
                android.util.Log.e("LanguagesFragment", "Error abriendo RegionsActivity", e);
            }
        });

        updateButtonEnabled();
    }

    private void updateButtonEnabled() {
        boolean enable = true;
        if (rbOther.isChecked()) {
            enable = !TextUtils.isEmpty(etOther.getText().toString().trim());
        }
        btnContinue.setEnabled(enable);
        btnContinue.setAlpha(enable ? 1f : 0.5f);
    }

    private String mapSelectionToCode() {
        int id = group.getCheckedRadioButtonId();
        if (id == R.id.rbOther) {
            String txt = etOther.getText() == null ? "" : etOther.getText().toString().trim();
            return "other:" + txt;
        }
        if (id == R.id.rbEn)      return "en";
        if (id == R.id.rbQu)      return "qu";
        if (id == R.id.rbAy)      return "ay";
        if (id == R.id.rbEsLatam) return "es-419";
        if (id == R.id.rbEsPe)    return "es-PE";
        return "es-PE";
    }
}
