package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etNewPass, etRepeatPass;
    private Button btnReset;
    private ImageButton btnBack;
    private LocalAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        auth = new LocalAuth(this);

        etEmail = findViewById(R.id.etEmailReset);
        etNewPass = findViewById(R.id.etNewPass);
        etRepeatPass = findViewById(R.id.etRepeatPass);
        btnReset = findViewById(R.id.btnResetOk);
        btnBack = findViewById(R.id.btnBack);

        // Botón atrás → vuelve a TempPasswordActivity
        btnBack.setOnClickListener(v -> finish());

        // Si venimos desde TempPasswordActivity, recupera email
        String email = getIntent().getStringExtra("email");
        if (email != null) {
            etEmail.setText(email);
        }

        // Acción de resetear
        btnReset.setOnClickListener(v -> {
            String correo = etEmail.getText().toString().trim();
            String pass1 = etNewPass.getText().toString();
            String pass2 = etRepeatPass.getText().toString();

            if (correo.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!auth.userExists(correo)) {
                Toast.makeText(this, "El correo no está registrado", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass1.equals(pass2)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.updatePassword(correo, pass1);

            new AlertDialog.Builder(this)
                    .setTitle("Éxito")
                    .setMessage("Tu contraseña ha sido restablecida correctamente 🎉")
                    .setPositiveButton("Ir al login", (dialog, which) -> {
                        Intent i = new Intent(this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    })
                    .show();
        });
    }
}

