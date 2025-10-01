package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SetPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilPassword, tilConfirm;
    private TextInputEditText etPassword, etConfirm;
    private ProgressBar pbStrength;
    private TextView tvStrength;
    private Button btnContinue;
    private String prefillUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        tilPassword = findViewById(R.id.tilPassword);
        tilConfirm  = findViewById(R.id.tilConfirm);
        etPassword  = findViewById(R.id.etPassword);
        etConfirm   = findViewById(R.id.etConfirm);
        pbStrength  = findViewById(R.id.pbStrength);
        tvStrength  = findViewById(R.id.tvStrength);
        btnContinue = findViewById(R.id.btnContinue);

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        prefillUsername = getIntent().getStringExtra("prefill_username");

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateUI(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etPassword.addTextChangedListener(watcher);
        etConfirm.addTextChangedListener(watcher);
        updateUI();

        btnContinue.setOnClickListener(v -> {
            if (!validate()) return;

            Intent i = new Intent(this, SetupProfileActivity.class);
            if (!TextUtils.isEmpty(prefillUsername)) {
                i.putExtra("prefill_username", prefillUsername);
            }
            startActivity(i);
        });
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
