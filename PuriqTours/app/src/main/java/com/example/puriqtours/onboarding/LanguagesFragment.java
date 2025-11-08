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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Usuario;
import com.example.puriqtours.helper.UserSessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LanguagesFragment extends Fragment {

    private RadioGroup group;
    private RadioButton rbOther;
    private EditText etOther;
    private Button btnContinue;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserSessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_languages_simple, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        group       = v.findViewById(R.id.groupLangs);
        rbOther     = v.findViewById(R.id.rbOther);
        etOther     = v.findViewById(R.id.etOtherLanguage);
        btnContinue = v.findViewById(R.id.btnLangContinue);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        session = new UserSessionManager(requireContext());

        group.setOnCheckedChangeListener((g, checkedId) -> {
            boolean isOther = (checkedId == R.id.rbOther);
            etOther.setVisibility(isOther ? View.VISIBLE : View.GONE);
            if (!isOther) etOther.setError(null);
            updateButtonEnabled();
        });

        etOther.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateButtonEnabled(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnContinue.setOnClickListener(view -> {
            if (rbOther.isChecked() && TextUtils.isEmpty(etOther.getText().toString().trim())) {
                etOther.setError("Escribe el idioma");
                etOther.requestFocus();
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            String language = mapSelectionToCode();
            String uid = currentUser.getUid();

            Map<String, Object> updates = new HashMap<>();
            updates.put("language", language);

            db.collection("users")
                    .document(uid)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        // ✅ Actualizar sesión local
                        Usuario user = session.getUser();
                        if (user != null) {
                            user.setLanguage(language);
                            session.saveUser(user);
                        }

                        Toast.makeText(requireContext(), "Idioma guardado correctamente", Toast.LENGTH_SHORT).show();

                        // 👉 Ir a RegionsActivity
                        Intent i = new Intent(requireContext(), com.example.puriqtours.onboarding.RegionsActivity.class);
                        startActivity(i);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error al guardar idioma: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
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
