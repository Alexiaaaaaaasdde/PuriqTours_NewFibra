package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TempPasswordActivity extends AppCompatActivity {

    private EditText code1, code2, code3, code4;
    private Button btnTempOk;

    // Código temporal fijo para la demo (lo ideal sería generarlo y enviarlo por correo)
    private final String TEMP_CODE = "1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_password);

        code1 = findViewById(R.id.code1);
        code2 = findViewById(R.id.code2);
        code3 = findViewById(R.id.code3);
        code4 = findViewById(R.id.code4);
        btnTempOk = findViewById(R.id.btnTempOk);

        // Mover el cursor automáticamente al siguiente campo
        setupAutoMove(code1, code2);
        setupAutoMove(code2, code3);
        setupAutoMove(code3, code4);

        btnTempOk.setOnClickListener(v -> {
            String codeEntered = code1.getText().toString() +
                    code2.getText().toString() +
                    code3.getText().toString() +
                    code4.getText().toString();

            if (codeEntered.length() < 4) {
                Toast.makeText(this, "Ingresa los 4 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (codeEntered.equals(TEMP_CODE)) {
                Toast.makeText(this, "Código correcto ✅", Toast.LENGTH_SHORT).show();

                // Pasar al ResetPasswordActivity
                Intent intent = new Intent(this, ResetPasswordActivity.class);

                // También podemos pasar el correo ingresado en la vista anterior (si lo tuvieras en el intent)
                String email = getIntent().getStringExtra("email");
                if (email != null) {
                    intent.putExtra("email", email);
                }

                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Código incorrecto ❌", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Función para mover cursor al siguiente campo automáticamente
    private void setupAutoMove(EditText from, EditText to) {
        from.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    to.requestFocus();
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}

