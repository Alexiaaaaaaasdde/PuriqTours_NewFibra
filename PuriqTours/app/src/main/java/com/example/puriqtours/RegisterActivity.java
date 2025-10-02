package com.example.puriqtours;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private ScrollView scroll;
    private EditText etName, etLastName, etEmail, etBirthDate, etPhone, etAddress, etDocumentNumber;
    private Spinner spnDocumentType;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageButton back = findViewById(R.id.btnBackRegister);
        if (back != null) back.setOnClickListener(v -> finish());

        // refs
        scroll           = findViewById(R.id.scroll);
        etName           = findViewById(R.id.etName);
        etLastName       = findViewById(R.id.etLastName);
        etEmail          = findViewById(R.id.etEmail);
        etBirthDate      = findViewById(R.id.etBirthDate);
        etPhone          = findViewById(R.id.etPhone);
        etAddress        = findViewById(R.id.etAddress);
        spnDocumentType  = findViewById(R.id.spnDocumentType);
        etDocumentNumber = findViewById(R.id.etDocumentNumber);
        btnRegister      = findViewById(R.id.btnRegister);

        // fecha
        etBirthDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(
                    this,
                    (view, y, m, d) -> etBirthDate.setText(d + "/" + (m + 1) + "/" + y),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            );
            dp.show();
        });

        // spinner
        String[] docTypes = {"Selecciona tipo de documento", "DNI", "Carnet de extranjería", "Pasaporte"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, docTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDocumentType.setAdapter(adapter);

        // onClick
        btnRegister.setOnClickListener(v -> {
            if (!validateForm()) return;

            // ✅ Paso 1: ir a crear contraseña (como estaba antes)
            Intent i = new Intent(RegisterActivity.this, SetPasswordActivity.class);
            i.putExtra("prefill_username", safeText(etName));
            startActivity(i);

            // (si NO quieres volver con back) descomenta:
            // finish();
        });
    }

    private boolean validateForm() {
        clearErrors();
        boolean ok = true;
        View firstError = null;

        if (isEmpty(etName)) {
            setError(etName, "Ingresa tu nombre"); ok = false; firstError = firstError == null ? etName : firstError;
        }

        if (isEmpty(etLastName)) {
            setError(etLastName, "Ingresa tus apellidos"); ok = false; firstError = firstError == null ? etLastName : firstError;
        }

        String email = safeText(etEmail);
        if (TextUtils.isEmpty(email)) {
            setError(etEmail, "Ingresa tu correo"); ok = false; firstError = firstError == null ? etEmail : firstError;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError(etEmail, "Correo inválido"); ok = false; firstError = firstError == null ? etEmail : firstError;
        }

        if (spnDocumentType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Por favor selecciona un tipo de documento", Toast.LENGTH_SHORT).show();
            View v = spnDocumentType.getSelectedView();
            if (v instanceof TextView) ((TextView) v).setError("");
            ok = false; if (firstError == null) firstError = spnDocumentType;
        }

        String docNum = safeText(etDocumentNumber);
        if (TextUtils.isEmpty(docNum)) {
            setError(etDocumentNumber, "Ingresa tu número de documento"); ok = false; firstError = firstError == null ? etDocumentNumber : firstError;
        } else if (spnDocumentType.getSelectedItemPosition() == 1 && !docNum.matches("\\d{8}")) {
            setError(etDocumentNumber, "El DNI debe tener 8 dígitos"); ok = false; firstError = firstError == null ? etDocumentNumber : firstError;
        }

        if (!ok && firstError != null && scroll != null) {
            final View target = firstError;
            target.requestFocus();
            final int y = Math.max(0, target.getTop() - dpToPx(24));
            scroll.post(() -> scroll.smoothScrollTo(0, y));
        }
        return ok;
    }

    private String safeText(EditText et) {
        return et == null || et.getText() == null ? "" : et.getText().toString().trim();
    }

    private boolean isEmpty(EditText et) {
        return et == null || TextUtils.isEmpty(safeText(et));
    }

    private void setError(EditText et, String msg) {
        if (et != null) et.setError(msg);
    }

    private void clearErrors() {
        EditText[] ets = {etName, etLastName, etEmail, etDocumentNumber};
        for (EditText et : ets) if (et != null) et.setError(null);
    }

    private int dpToPx(int dp) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(dp * d);
    }
}



