package com.example.puriqtours.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.puriqtours.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SetPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilPassword, tilConfirm;
    private EditText etPassword, etConfirm;
    private ProgressBar pbStrength;
    private TextView tvStrength;
    private Button btnContinue;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private HashMap<String, Object> userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tilPassword = findViewById(R.id.tilPassword);
        tilConfirm  = findViewById(R.id.tilConfirm);
        etPassword  = findViewById(R.id.etPassword);
        etConfirm   = findViewById(R.id.etConfirm);
        pbStrength  = findViewById(R.id.pbStrength);
        tvStrength  = findViewById(R.id.tvStrength);
        btnContinue = findViewById(R.id.btnContinue);

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        Serializable extra = getIntent().getSerializableExtra("userData");
        if (extra instanceof HashMap) {
            //noinspection unchecked
            userData = (HashMap<String, Object>) extra;
        } else {
            userData = new HashMap<>();
        }

        // Listener para actualizar nivel de seguridad
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateUI(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etPassword.addTextChangedListener(watcher);
        etConfirm.addTextChangedListener(watcher);
        updateUI();

        btnContinue.setOnClickListener(v -> {
            Log.d("DEBUG_FLOW", "Botón CONTINUAR presionado");
            if (!validate()) return;
            String pass = etPassword.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();
            Log.d("DEBUG_FLOW", "Validado");
            if (pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Completa ambos campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = userData.get("email").toString();
            if (userData == null || userData.get("email") == null) {
                Toast.makeText(this, "Datos de registro incompletos", Toast.LENGTH_LONG).show();
                return;
            }
            Log.d("DEBUG_FLOW", "Intentando crear usuario con " + userData.get("email"));
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, task -> {
                        Log.d("DEBUG_FLOW", "createUserWithEmailAndPassword completado. Success=" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();
                            saveUserToFirestore(uid);
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DEBUG_FLOW", "Fallo grave en Firebase: ", e);
                        Toast.makeText(this, "Fallo Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

        });
    }

    private void saveUserToFirestore(String uid) {
        userData.put("activities", new ArrayList<String>());
        db.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "Documento creado correctamente");
                    Intent intent = new Intent(this, SetupProfileActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void updateUI() {
        String p = etPassword.getText() == null ? "" : etPassword.getText().toString();
        int score = calcStrength(p);
        pbStrength.setProgress(score);
        String label = new String[]{"Muy débil","Débil","Media","Fuerte","Muy fuerte"}[score];
        tvStrength.setText("Seguridad: " + label);

        boolean ok = score >= 2 &&
                !TextUtils.isEmpty(p) &&
                p.equals(etConfirm.getText() == null ? "" : etConfirm.getText().toString());
        btnContinue.setEnabled(ok);
        btnContinue.setAlpha(ok ? 1f : 0.5f);
    }

    private boolean validate() {
        tilPassword.setError(null);
        tilConfirm.setError(null);

        String p = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
        String c = etConfirm.getText() == null ? "" : etConfirm.getText().toString().trim();

        if (TextUtils.isEmpty(p)) { tilPassword.setError("Ingresa una contraseña"); return false; }
        if (p.length() < 8)       { tilPassword.setError("Mínimo 8 caracteres");   return false; }
        if (!p.matches(".*\\d.*")){ tilPassword.setError("Incluye al menos un número"); return false; }
        if (!p.matches(".*[A-Z].*")){ tilPassword.setError("Incluye al menos una mayúscula"); return false; }
        if (TextUtils.isEmpty(c)) { tilConfirm.setError("Confirma tu contraseña");  return false; }
        if (!p.equals(c))         { tilConfirm.setError("Las contraseñas no coinciden"); return false; }
        return true;
    }

    /** 0..4 */
    private int calcStrength(String p) {
        int s = 0;
        if (p.length() >= 8) s++;
        if (p.matches(".*\\d.*")) s++;
        if (p.matches(".*[A-Z].*")) s++;
        if (p.matches(".*[a-z].*") && p.matches(".*[^A-Za-z0-9].*")) s++;
        if (s > 4) s = 4;
        return s;
    }
}
