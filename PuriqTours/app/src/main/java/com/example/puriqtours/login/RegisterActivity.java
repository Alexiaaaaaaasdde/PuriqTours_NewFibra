package com.example.puriqtours.login;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etLastName, etEmail, etBirthDate, etPhone, etAddress, etDocumentNumber;
    private Spinner spnDocumentType;
    private Button btnRegister;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private HashMap<String, String> userData = new HashMap<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etBirthDate = findViewById(R.id.etBirthDate);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etDocumentNumber = findViewById(R.id.etDocumentNumber);
        spnDocumentType = findViewById(R.id.spnDocumentType);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBackRegister);
        progressBar = findViewById(R.id.progressBarRegister);

        db = FirebaseFirestore.getInstance();

        // Spinner de tipo de documento
        String[] docTypes = {"Selecciona tipo de documento", "DNI", "Carnet de extranjería", "Pasaporte"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, docTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDocumentType.setAdapter(adapter);

        // Fecha de nacimiento
        etBirthDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(
                    this,
                    (view, y, m, d) -> etBirthDate.setText(d + "/" + (m + 1) + "/" + y),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            );
            dp.show();
        });

        btnBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            if (!validateForm()) return;
            verifyEmailInFirestore();
        });
    }

    private void verifyEmailInFirestore() {
        String email = etEmail.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);

                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                // Ya existe
                                etEmail.setError("Este correo ya está registrado");
                                etEmail.requestFocus();
                                Toast.makeText(RegisterActivity.this,
                                        "El correo ya está en uso. Usa otro.",
                                        Toast.LENGTH_LONG).show();
                                Log.d("REGISTER_FIRESTORE", "Correo duplicado: " + email);
                            } else {
                                // No existe → continuar
                                Log.d("REGISTER_FIRESTORE", "El correo está disponible");
                                proceedToSetPassword();
                            }
                        } else {
                            Exception e = task.getException();
                            Toast.makeText(RegisterActivity.this,
                                    "Error al verificar correo: " + (e != null ? e.getMessage() : "desconocido"),
                                    Toast.LENGTH_LONG).show();
                            Log.e("REGISTER_FIRESTORE", "Error verificando correo", e);
                        }
                    }
                });
    }

    private void proceedToSetPassword() {
        userData.put("name", etName.getText().toString().trim());
        userData.put("last_name", etLastName.getText().toString().trim());
        userData.put("email", etEmail.getText().toString().trim());
        userData.put("birthdate", etBirthDate.getText().toString().trim());
        userData.put("phone", etPhone.getText().toString().trim());
        userData.put("address", etAddress.getText().toString().trim());
        userData.put("doc_type", spnDocumentType.getSelectedItem().toString());
        userData.put("document", etDocumentNumber.getText().toString().trim());
        userData.put("rol", "Cliente");
        userData.put("status", "Activo");
        userData.put("language", "es");

        Intent intent = new Intent(this, SetPasswordActivity.class);
        intent.putExtra("userData", userData);
        startActivity(intent);
    }

    private boolean validateForm() {
        clearErrors();
        boolean ok = true;

        if (isEmpty(etName)) { setError(etName, "Ingresa tu nombre"); ok = false; }
        if (isEmpty(etLastName)) { setError(etLastName, "Ingresa tus apellidos"); ok = false; }

        String email = safeText(etEmail);
        if (TextUtils.isEmpty(email)) { setError(etEmail, "Ingresa tu correo"); ok = false; }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { setError(etEmail, "Correo inválido"); ok = false; }

        if (spnDocumentType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecciona tipo de documento", Toast.LENGTH_SHORT).show();
            ok = false;
        }

        String docNum = safeText(etDocumentNumber);
        if (TextUtils.isEmpty(docNum)) {
            setError(etDocumentNumber, "Ingresa tu documento"); ok = false;
        } else if (spnDocumentType.getSelectedItemPosition() == 1 && !docNum.matches("\\d{8}")) {
            setError(etDocumentNumber, "El DNI debe tener 8 dígitos"); ok = false;
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
}
