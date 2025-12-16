package com.example.puriqtours.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

public class SetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etPassword, etConfirm;
    private TextView tvStrength;
    private ProgressBar pbStrength;
    private Button btnContinue;
    private ImageButton btnBack;

    private HashMap<String, Object> userData;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        mAuth = FirebaseAuth.getInstance();
        userData = (HashMap<String, Object>) getIntent().getSerializableExtra("userData");

        initViews();
        setupPasswordStrength();

        btnBack.setOnClickListener(v -> finish());
        btnContinue.setOnClickListener(v -> validateAndProceed());
    }

    private void initViews() {
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        tvStrength = findViewById(R.id.tvStrength);
        pbStrength = findViewById(R.id.pbStrength);
        btnContinue = findViewById(R.id.btnContinue);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupPasswordStrength() {
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) strength++;

        pbStrength.setProgress(strength);

        String[] labels = {"Muy débil", "Débil", "Aceptable", "Fuerte", "Muy fuerte"};
        tvStrength.setText("Seguridad: " + labels[strength]);
    }

    private void validateAndProceed() {
        String password = getText(etPassword);
        String confirm = getText(etConfirm);

        if (password.isEmpty()) {
            etPassword.setError("Ingresa una contraseña");
            return;
        }

        if (password.length() < 8) {
            etPassword.setError("Mínimo 8 caracteres");
            return;
        }

        if (!password.equals(confirm)) {
            etConfirm.setError("Las contraseñas no coinciden");
            return;
        }

        // ✅ Guardar la contraseña en userData
        userData.put("password", password);

        // 🔀 Redirigir según el tipo de usuario
        Intent intent = new Intent(this, SetupProfileActivity.class);
        intent.putExtra("userData", userData);
        startActivity(intent);
        finish();
    }

    private String getText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}